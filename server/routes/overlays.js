import express from 'express';
import Overlay from '../models/Overlay.js';
import { Op } from 'sequelize';

const router = express.Router();

// Get all overlays
router.get('/', async (req, res) => {
  try {
    const [overlays] = await Overlay.sequelize.query(
      'SELECT * FROM Overlays ORDER BY "order" ASC'
    );
    res.json(overlays);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Create an overlay
router.post('/', async (req, res) => {
  try {
    const overlay = await Overlay.create(req.body);
    res.status(201).json(overlay);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// Get an overlay by ID
router.get('/:id', async (req, res) => {
  try {
    const overlay = await Overlay.findByPk(req.params.id);
    if (overlay) {
      res.json(overlay);
    } else {
      res.status(404).json({ error: 'Overlay not found' });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update an overlay
router.put('/:id', async (req, res) => {
  try {
    const overlay = await Overlay.findByPk(req.params.id);
    if (overlay) {
      await overlay.update(req.body);
      res.json(overlay);
    } else {
      res.status(404).json({ error: 'Overlay not found' });
    }
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// Delete an overlay
router.delete('/:id', async (req, res) => {
  try {
    const overlay = await Overlay.findByPk(req.params.id);
    if (overlay) {
      await overlay.destroy();
      res.status(204).send();
    } else {
      res.status(404).json({ error: 'Overlay not found' });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Create a group from selected overlays
router.post('/group', async (req, res) => {
  const { name, overlayIds } = req.body;
  if (!overlayIds || !Array.isArray(overlayIds) || overlayIds.length === 0) {
    return res.status(400).json({ error: 'overlayIds must be a non-empty array.' });
  }

  try {
    // 1. Create the new group
    const newGroup = await Overlay.create({
      name: name || 'New Group',
      type: 'group',
    });

    // 2. Update the parentId for all selected overlays
    const [updateCount] = await Overlay.update(
      { parentId: newGroup.id },
      { where: { id: { [Op.in]: overlayIds } } }
    );

    res.status(200).json({
      message: `Successfully created group and moved ${updateCount} overlays.`,
      group: newGroup,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update overlay order
router.post('/order', async (req, res) => {
  const { orderedIds, parentId } = req.body;

  if (!orderedIds || !Array.isArray(orderedIds)) {
    return res.status(400).json({ error: 'orderedIds must be an array.' });
  }

  const t = await Overlay.sequelize.transaction();

  try {
    for (let i = 0; i < orderedIds.length; i++) {
      const overlayId = orderedIds[i];
      await Overlay.update(
        { order: i },
        {
          where: {
            id: overlayId,
            parentId: parentId, // Sequelize handles parentId: null as 'IS NULL'
          },
          transaction: t,
        }
      );
    }

    await t.commit();
    res.status(200).json({ message: 'Order updated successfully.' });
  } catch (error) {
    await t.rollback();
    res.status(500).json({ error: error.message });
  }
});

export default router;