const express = require('express');
const db = require('./db');
const cors = require('cors');
const { verifyToken } = require('./middleware/auth');
const path = require('path');

const app = express();
const port = 3001;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Import routes
const authRoutes = require('./routes/auth');
const scheduleRoutes = require('./routes/schedules');
const mediaRoutes = require('./routes/medialibrary');
const overlayRoutes = require('./routes/overlays');
const folderRoutes = require('./routes/folders');

// Public routes (no authentication required)
app.use('/api/auth', authRoutes);

// Protected routes (require authentication)
const scheduleRoutes = require('./routes/schedules');
const mediaRoutes = require('./routes/medialibrary');
const overlayRoutes = require('./routes/overlays');
const folderRoutes = require('./routes/folders');

// Apply authentication middleware to protected routes
app.use('/api/schedules', verifyToken, scheduleRoutes);
app.use('/api/media', verifyToken, mediaRoutes);
app.use('/api/overlays', verifyToken, overlayRoutes);
app.use('/api/folders', verifyToken, folderRoutes);

// Additional specific routes for schedules - GET by schedule ID with full details
app.get('/api/schedules/:id', verifyToken, async (req, res) => {
    try {
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

        const schedule = await db.Schedule.findByPk(req.params.id, {
            include: fullScheduleInclude,
            order: [[{ model: db.ScheduleItem, as: 'items' }, 'sortOrder', 'ASC']]
        });

        if (!schedule) {
            return res.status(404).json({ error: 'Schedule not found' });
        }

        res.json(schedule);
    } catch (error) {
        console.error('Error fetching schedule by ID:', error);
        res.status(500).json({ error: error.message });
    }
});

// Additional routes for schedules by date
app.get('/api/schedules/date/:date', verifyToken, async (req, res) => {
    try {
        const { date } = req.params;
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

        const schedule = await db.Schedule.findOne({
            where: { date },
            include: fullScheduleInclude,
            order: [[{ model: db.ScheduleItem, as: 'items' }, 'sortOrder', 'ASC']]
        });

        if (schedule) {
            return res.json(schedule);
        }

        // If no exact match, check for repeating schedules
        const potentialSchedules = await db.Schedule.findAll({
            where: {
                repeat: { [db.Sequelize.Op.ne]: null },
                date: { [db.Sequelize.Op.lt]: date }
            },
            order: [['date', 'DESC']]
        });

        const requestedDate = new Date(date + 'T00:00:00');
        const dayMap = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        const requestedDayStr = dayMap[requestedDate.getUTCDay()];

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
    } catch (error) {
        console.error('Error fetching schedule by date:', error);
        res.status(500).json({ error: error.message });
    }
});

// --- System Defaults Routes ---
const systemDefaultsRouter = express.Router();

systemDefaultsRouter.get('/', async (req, res) => {
    try { 
        const systemDefaults = await db.SystemDefaults.findAll(); 
        res.json(systemDefaults); 
    } catch (e) { 
        console.error('Error fetching system defaults:', e);
        res.status(500).json({ error: e.message }); 
    }
});

systemDefaultsRouter.post('/', async (req, res) => {
    try { 
        const systemDefault = await db.SystemDefaults.create(req.body);
        res.status(201).json(systemDefault); 
    } catch (e) { 
        console.error('Error creating system default:', e);
        res.status(500).json({ error: e.message }); 
    }
});

systemDefaultsRouter.put('/:id', async (req, res) => {
    try { 
        const [updatedRows] = await db.SystemDefaults.update(req.body, { where: { id: req.params.id } });
        if (updatedRows === 0) {
            return res.status(404).json({ error: 'System default not found' });
        }
        const systemDefault = await db.SystemDefaults.findByPk(req.params.id);
        res.json(systemDefault); 
    } catch (e) { 
        console.error('Error updating system default:', e);
        res.status(500).json({ error: e.message }); 
    }
});

systemDefaultsRouter.delete('/:id', async (req, res) => {
    try { 
        const deletedRows = await db.SystemDefaults.destroy({ where: { id: req.params.id } });
        if (deletedRows === 0) {
            return res.status(404).json({ error: 'System default not found' });
        }
        res.status(204).send(); 
    } catch (e) { 
        console.error('Error deleting system default:', e);
        res.status(500).json({ error: e.message }); 
    }
});

app.use('/api/system-defaults', systemDefaultsRouter);

// --- Ads Routes ---
const adsRouter = express.Router();

// Ad groups endpoints
adsRouter.get('/ad-groups', async (req, res) => {
    try { 
        const adGroups = await db.AdGroup.findAll();
        res.json(adGroups); 
    } catch (e) { 
        console.error('Error fetching ad groups:', e);
        res.status(500).json({ error: e.message }); 
    }
});

adsRouter.post('/ad-groups', async (req, res) => {
    try { 
        const adGroup = await db.AdGroup.create(req.body);
        res.status(201).json(adGroup); 
    } catch (e) { 
        console.error('Error creating ad group:', e);
        res.status(500).json({ error: e.message }); 
    }
});

adsRouter.put('/ad-groups/:id', async (req, res) => {
    try { 
        const [updatedRows] = await db.AdGroup.update(req.body, { where: { id: req.params.id } });
        if (updatedRows === 0) {
            return res.status(404).json({ error: 'Ad group not found' });
        }
        const adGroup = await db.AdGroup.findByPk(req.params.id);
        res.json(adGroup); 
    } catch (e) { 
        console.error('Error updating ad group:', e);
        res.status(500).json({ error: e.message }); 
    }
});

adsRouter.delete('/ad-groups/:id', async (req, res) => {
    try { 
        const deletedRows = await db.AdGroup.destroy({ where: { id: req.params.id } });
        if (deletedRows === 0) {
            return res.status(404).json({ error: 'Ad group not found' });
        }
        res.status(204).send(); 
    } catch (e) { 
        console.error('Error deleting ad group:', e);
        res.status(500).json({ error: e.message }); 
    }
});

// Ads endpoints
adsRouter.get('/', async (req, res) => {
    try { 
        const ads = await db.Ad.findAll();
        res.json(ads); 
    } catch (e) { 
        console.error('Error fetching ads:', e);
        res.status(500).json({ error: e.message }); 
    }
});

adsRouter.get('/:id', async (req, res) => {
    try { 
        const ad = await db.Ad.findByPk(req.params.id);
        if (!ad) {
            return res.status(404).json({ error: 'Ad not found' });
        }
        res.json(ad); 
    } catch (e) { 
        console.error('Error fetching ad by ID:', e);
        res.status(500).json({ error: e.message }); 
    }
});

adsRouter.post('/', async (req, res) => {
    try { 
        const ad = await db.Ad.create(req.body);
        res.status(201).json(ad); 
    } catch (e) { 
        console.error('Error creating ad:', e);
        res.status(500).json({ error: e.message }); 
    }
});

adsRouter.put('/:id', async (req, res) => {
    try { 
        const [updatedRows] = await db.Ad.update(req.body, { where: { id: req.params.id } });
        if (updatedRows === 0) {
            return res.status(404).json({ error: 'Ad not found' });
        }
        const ad = await db.Ad.findByPk(req.params.id);
        res.json(ad); 
    } catch (e) { 
        console.error('Error updating ad:', e);
        res.status(500).json({ error: e.message }); 
    }
});

adsRouter.delete('/:id', async (req, res) => {
    try { 
        const deletedRows = await db.Ad.destroy({ where: { id: req.params.id } });
        if (deletedRows === 0) {
            return res.status(404).json({ error: 'Ad not found' });
        }
        res.status(204).send(); 
    } catch (e) { 
        console.error('Error deleting ad:', e);
        res.status(500).json({ error: e.message }); 
    }
});

// Get ads by ad group
adsRouter.get('/group/:id', async (req, res) => {
    try { 
        const ads = await db.Ad.findAll({ where: { adGroupId: req.params.id } });
        res.json(ads); 
    } catch (e) { 
        console.error('Error fetching ads by group:', e);
        res.status(500).json({ error: e.message }); 
    }
});

// Get unassigned ads
adsRouter.get('/unassigned', async (req, res) => {
    try { 
        const ads = await db.Ad.findAll({ where: { adGroupId: null } });
        res.json(ads); 
    } catch (e) { 
        console.error('Error fetching unassigned ads:', e);
        res.status(500).json({ error: e.message }); 
    }
});

// Move ad to group
adsRouter.put('/:id/group/:groupId', async (req, res) => {
    try { 
        const [updatedRows] = await db.Ad.update({ adGroupId: req.params.groupId }, { where: { id: req.params.id } });
        if (updatedRows === 0) {
            return res.status(404).json({ error: 'Ad not found' });
        }
        const ad = await db.Ad.findByPk(req.params.id);
        res.json(ad); 
    } catch (e) { 
        console.error('Error moving ad to group:', e);
        res.status(500).json({ error: e.message }); 
    }
});

// Update ad sort order
adsRouter.put('/:id/sort-order', async (req, res) => {
    try { 
        const { sortOrder } = req.body;
        if (sortOrder === undefined) {
            return res.status(400).json({ error: 'Sort order is required' });
        }
        
        const [updatedRows] = await db.Ad.update({ sortOrder }, { where: { id: req.params.id } });
        if (updatedRows === 0) {
            return res.status(404).json({ error: 'Ad not found' });
        }
        const ad = await db.Ad.findByPk(req.params.id);
        res.json(ad); 
    } catch (e) { 
        console.error('Error updating ad sort order:', e);
        res.status(500).json({ error: e.message }); 
    }
});

app.use('/api/ads', adsRouter);



// Error handling middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ 
        error: 'Something went wrong!', 
        message: process.env.NODE_ENV === 'development' ? err.message : 'Internal server error' 
    });
});

// 404 handler for undefined routes - should be last middleware
app.use((req, res) => {
    res.status(404).json({ error: 'Route not found' });
});

// Start Server
db.sequelize.sync().then(() => {
    app.listen(port, () => {
        console.log(`Node-M Backend listening on http://localhost:${port}`);
    });
});