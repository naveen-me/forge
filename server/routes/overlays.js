import express from 'express';
import Overlay from '../models/Overlay.js';
import { Op } from 'sequelize';
import { authenticateToken } from '../src/middleware/auth.js';

const router = express.Router();

// Apply authentication to all overlay routes
router.use(authenticateToken);

// Get all overlays for the authenticated user
router.get('/', async (req, res) => {
  try {
    const userId = req.user.id; // Get userId from authenticated user
    const overlays = await Overlay.findAll({
      where: { userId },
      order: [['order', 'ASC']]
    });
    res.json(overlays);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Create an overlay
router.post('/', async (req, res) => {
  try {
    const userId = req.user.id; // Get userId from authenticated user
    const overlay = await Overlay.create({
      ...req.body,
      userId // Include userId from authenticated user
    });
    res.status(201).json(overlay);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// Get an overlay by ID
router.get('/:id', async (req, res) => {
  try {
    const userId = req.user.id; // Get userId from authenticated user
    const overlay = await Overlay.findOne({
      where: { 
        id: req.params.id,
        userId: userId 
      }
    });
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
    const userId = req.user.id; // Get userId from authenticated user
    const overlay = await Overlay.findOne({
      where: { 
        id: req.params.id,
        userId: userId 
      }
    });
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
    const userId = req.user.id; // Get userId from authenticated user
    const overlay = await Overlay.findOne({
      where: { 
        id: req.params.id,
        userId: userId 
      }
    });
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
  const userId = req.user.id; // Get userId from authenticated user
  
  if (!overlayIds || !Array.isArray(overlayIds) || overlayIds.length === 0) {
    return res.status(400).json({ error: 'overlayIds must be a non-empty array.' });
  }

  try {
    // 1. Create the new group
    const newGroup = await Overlay.create({
      name: name || 'New Group',
      type: 'group',
      userId: userId // Include userId for the group
    });

    // 2. Update the parentId for all selected overlays (only those belonging to the user)
    const [updateCount] = await Overlay.update(
      { parentId: newGroup.id },
      { 
        where: { 
          id: { [Op.in]: overlayIds },
          userId: userId // Only update overlays that belong to the user
        }
      }
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
  const userId = req.user.id; // Get userId from authenticated user

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
            userId: userId, // Only update overlays that belong to the user
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