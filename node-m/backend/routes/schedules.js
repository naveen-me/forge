const express = require('express');
const router = express.Router();
const db = require('../db');
const fs = require('fs'); // Needed for import/export, though filesystem access is limited

// Middleware for error handling
const asyncHandler = fn => (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
};

// GET /api/schedules?date=YYYY-MM-DD - Get schedule by date
router.get('/', asyncHandler(async (req, res) => {
    const { date } = req.query;
    if (!date) {
        return res.status(400).json({ error: 'A date query parameter is required.' });
    }

    const fullScheduleInclude = [
        {
            model: db.ScheduleItem,
            as: 'items',
            include: [
                { model: db.ScheduleItemOverlay, as: 'overlays', include: [{ model: db.Overlay, as: 'overlay' }] },
                { model: db.ScheduleItemCuePoint, as: 'cuePoints', include: [{ model: db.Ad, as: 'ad' }] },
                { model: db.ScheduleItemAdPlacement, as: 'adPlacements', include: [{ model: db.Ad, as: 'ad' }] }
            ]
        }
    ];

    let schedule = await db.Schedule.findOne({
        where: { date },
        include: fullScheduleInclude,
        order: [[{ model: db.ScheduleItem, as: 'items' }, 'sortOrder', 'ASC']]
    });

    if (schedule) {
        return res.json(schedule);
    }

    const potentialSchedules = await db.Schedule.findAll({
        where: {
            repeat: { [db.Sequelize.Op.ne]: null },
            date: { [db.Sequelize.Op.lt]: date }
        },
        order: [['date', 'DESC']]
    });

    const requestedDate = new Date(date + 'T00:00:00');
    const dayMap = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const requestedDayStr = dayMap[requestedDate.getUTCDay()]; // Use UTC day to match server timezone

    for (const s of potentialSchedules) {
        const repeatRules = s.repeat;
        if (!repeatRules || !repeatRules.days || !Array.isArray(repeatRules.days)) continue;

        if (repeatRules.days.includes(requestedDayStr)) {
            if (repeatRules.until && requestedDate > new Date(repeatRules.until)) {
                continue;
            }

            const repeatingSchedule = await db.Schedule.findByPk(s.id, {
                include: fullScheduleInclude,
                order: [[{ model: db.ScheduleItem, as: 'items' }, 'sortOrder', 'ASC']]
            });

            if (repeatingSchedule) {
                const virtualSchedule = repeatingSchedule.toJSON();
                virtualSchedule.date = date;
                virtualSchedule.isRepeatingInstance = true;
                return res.json(virtualSchedule);
            }
        }
    }

    res.json(null); // No schedule found
}));

// POST /api/schedules - Create a new schedule
router.post('/', asyncHandler(async (req, res) => {
    const { date, name, items = [] } = req.body;

    if (!date || !name) {
        return res.status(400).json({ error: 'Schedule must have a name and a date.' });
    }

    const existing = await db.Schedule.findOne({ where: { date } });
    if (existing) {
        return res.status(409).json({ error: `A schedule for ${date} already exists.` });
    }

    const newSchedule = await db.Schedule.create({ name, date, status: 'draft' });

    if (items.length > 0) {
        const scheduleItems = items.map(item => ({ ...item, scheduleId: newSchedule.id }));
        await db.ScheduleItem.bulkCreate(scheduleItems);
    }

    const result = await db.Schedule.findByPk(newSchedule.id, {
        include: [{ model: db.ScheduleItem, as: 'items' }]
    });

    res.status(201).json(result);
}));

// PUT /api/schedules/:id - Update a schedule
router.put('/:id', asyncHandler(async (req, res) => {
    const { id } = req.params;
    const schedule = await db.Schedule.findByPk(id);
    if (!schedule) {
        return res.status(404).json({ error: 'Schedule not found' });
    }
    await schedule.update(req.body);
    res.json(schedule);
}));

// DELETE /api/schedules/:id - Delete a schedule
router.delete('/:id', asyncHandler(async (req, res) => {
    const { id } = req.params;
    const schedule = await db.Schedule.findByPk(id);
    if (!schedule) {
        return res.status(404).json({ error: 'Schedule not found' });
    }
    await schedule.destroy();
    res.status(204).send();
}));

// POST /api/schedules/:id/duplicate - Duplicate a schedule
router.post('/:id/duplicate', asyncHandler(async (req, res) => {
    const { id: scheduleId } = req.params;
    const { newDate } = req.body;

    if (!newDate) {
        return res.status(400).json({ error: 'A newDate is required to duplicate a schedule.' });
    }

    const transaction = await db.sequelize.transaction();
    try {
        const existingSchedule = await db.Schedule.findOne({ where: { date: newDate }, transaction });
        if (existingSchedule) {
            throw new Error(`A schedule for date ${newDate} already exists.`);
        }

        const sourceSchedule = await db.Schedule.findByPk(scheduleId, {
            include: [{ model: db.ScheduleItem, as: 'items', include: ['overlays', 'cuePoints', 'adPlacements'] }]
        });
        if (!sourceSchedule) {
            throw new Error('Source schedule not found.');
        }

        const newSchedule = await db.Schedule.create({
            name: `${sourceSchedule.name} (Copy)`,
            date: newDate,
            status: 'draft',
        }, { transaction });

        for (const item of sourceSchedule.items) {
            const { id, scheduleId: oldScheduleId, createdAt, updatedAt, overlays, cuePoints, adPlacements, ...itemData } = item.toJSON();
            const newItem = await db.ScheduleItem.create({ ...itemData, scheduleId: newSchedule.id }, { transaction });

            if (overlays && overlays.length > 0) {
                const overlayRecords = overlays.map(o => {
                    const { id: oldId, scheduleItemId, createdAt: c, updatedAt: u, ...d } = o;
                    return { ...d, scheduleItemId: newItem.id };
                });
                await db.ScheduleItemOverlay.bulkCreate(overlayRecords, { transaction });
            }
            if (cuePoints && cuePoints.length > 0) {
                const cuePointRecords = cuePoints.map(cp => {
                    const { id: oldId, scheduleItemId, createdAt: c, updatedAt: u, ...d } = cp;
                    return { ...d, scheduleItemId: newItem.id };
                });
                await db.ScheduleItemCuePoint.bulkCreate(cuePointRecords, { transaction });
            }
            if (adPlacements && adPlacements.length > 0) {
                const adPlacementRecords = adPlacements.map(ap => {
                    const { id: oldId, scheduleItemId, createdAt: c, updatedAt: u, ...d } = ap;
                    return { ...d, scheduleItemId: newItem.id };
                });
                await db.ScheduleItemAdPlacement.bulkCreate(adPlacementRecords, { transaction });
            }
        }

        await transaction.commit();
        const result = await db.Schedule.findByPk(newSchedule.id);
        res.status(201).json(result);
    } catch (error) {
        await transaction.rollback();
        res.status(400).json({ error: error.message });
    }
}));


// --- Schedule Items ---

// POST /api/schedules/:scheduleId/items - Create a schedule item
router.post('/:scheduleId/items', asyncHandler(async (req, res) => {
    const { scheduleId } = req.params;
    const item = await db.ScheduleItem.create({ ...req.body, scheduleId });
    res.status(201).json(item);
}));

// PUT /api/schedules/:scheduleId/items/:itemId - Update a schedule item
router.put('/:scheduleId/items/:itemId', asyncHandler(async (req, res) => {
    const { itemId } = req.params;
    const item = await db.ScheduleItem.findByPk(itemId);
    if (!item) {
        return res.status(404).json({ error: 'Schedule item not found' });
    }
    await item.update(req.body);
    res.json(item);
}));

// DELETE /api/schedules/:scheduleId/items/:itemId - Delete a schedule item
router.delete('/:scheduleId/items/:itemId', asyncHandler(async (req, res) => {
    const { itemId } = req.params;
    const item = await db.ScheduleItem.findByPk(itemId);
    if (!item) {
        return res.status(404).json({ error: 'Schedule item not found' });
    }
    await item.destroy();
    res.status(204).send();
}));

// POST /api/schedules/:scheduleId/items/reorder - Reorder schedule items
router.post('/:scheduleId/items/reorder', asyncHandler(async (req, res) => {
    const { scheduleId } = req.params;
    const { orderedIds } = req.body;

    const transaction = await db.sequelize.transaction();
    try {
        for (let i = 0; i < orderedIds.length; i++) {
            const id = orderedIds[i];
            await db.ScheduleItem.update(
                { sortOrder: i },
                { where: { id, scheduleId }, transaction }
            );
        }
        await transaction.commit();
        res.json({ success: true });
    } catch (error) {
        await transaction.rollback();
        throw error; // Let asyncHandler handle it
    }
}));


// --- Nested Associations ---

// POST /api/schedules/:scheduleId/items/:itemId/cuepoints - Create a cue point
router.post('/:scheduleId/items/:itemId/cuepoints', asyncHandler(async (req, res) => {
    const { itemId: scheduleItemId } = req.params;
    const cuePoint = await db.ScheduleItemCuePoint.create({ ...req.body, scheduleItemId });
    res.status(201).json(cuePoint);
}));

// DELETE /api/schedules/:scheduleId/items/:itemId/cuepoints/:cuePointId - Delete a cue point
router.delete('/:scheduleId/items/:itemId/cuepoints/:cuePointId', asyncHandler(async (req, res) => {
    const { cuePointId } = req.params;
    const cuePoint = await db.ScheduleItemCuePoint.findByPk(cuePointId);
    if (!cuePoint) {
        return res.status(404).json({ error: 'Schedule item cue point not found' });
    }
    await cuePoint.destroy();
    res.status(204).send();
}));

// POST /api/schedules/:scheduleId/items/:itemId/overlays - Create an overlay
router.post('/:scheduleId/items/:itemId/overlays', asyncHandler(async (req, res) => {
    const { itemId: scheduleItemId } = req.params;
    const overlay = await db.ScheduleItemOverlay.create({ ...req.body, scheduleItemId });
    res.status(201).json(overlay);
}));

// DELETE /api/schedules/:scheduleId/items/:itemId/overlays/:overlayId - Delete an overlay
router.delete('/:scheduleId/items/:itemId/overlays/:overlayId', asyncHandler(async (req, res) => {
    const { overlayId } = req.params;
    const overlay = await db.ScheduleItemOverlay.findByPk(overlayId);
    if (!overlay) {
        return res.status(404).json({ error: 'Schedule item overlay not found' });
    }
    await overlay.destroy();
    res.status(204).send();
}));

// GET /api/schedules/:scheduleId/items/:itemId/adplacements - Get ad placements
router.get('/:scheduleId/items/:itemId/adplacements', asyncHandler(async (req, res) => {
    const { itemId: scheduleItemId } = req.params;
    const adPlacements = await db.ScheduleItemAdPlacement.findAll({
        where: { scheduleItemId },
        order: [['sortOrder', 'ASC']]
    });
    res.json(adPlacements);
}));

// POST /api/schedules/:scheduleId/items/:itemId/adplacements - Create an ad placement
router.post('/:scheduleId/items/:itemId/adplacements', asyncHandler(async (req, res) => {
    const { itemId: scheduleItemId } = req.params;
    const adPlacement = await db.ScheduleItemAdPlacement.create({ ...req.body, scheduleItemId });
    res.status(201).json(adPlacement);
}));

// PUT /api/schedules/:scheduleId/items/:itemId/adplacements/:adPlacementId - Update an ad placement
router.put('/:scheduleId/items/:itemId/adplacements/:adPlacementId', asyncHandler(async (req, res) => {
    const { adPlacementId } = req.params;
    const adPlacement = await db.ScheduleItemAdPlacement.findByPk(adPlacementId);
    if (!adPlacement) {
        return res.status(404).json({ error: 'Schedule item ad placement not found' });
    }
    await adPlacement.update(req.body);
    res.json(adPlacement);
}));

// DELETE /api/schedules/:scheduleId/items/:itemId/adplacements/:adPlacementId - Delete an ad placement
router.delete('/:scheduleId/items/:itemId/adplacements/:adPlacementId', asyncHandler(async (req, res) => {
    const { adPlacementId } = req.params;
    const adPlacement = await db.ScheduleItemAdPlacement.findByPk(adPlacementId);
    if (!adPlacement) {
        return res.status(404).json({ error: 'Schedule item ad placement not found' });
    }
    await adPlacement.destroy();
    res.status(204).send();
}));


module.exports = router;