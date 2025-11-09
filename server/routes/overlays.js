const express = require('express');
const router = express.Router();
const Overlay = require('../models/Overlay');

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

// Update overlay order
router.post('/order', async (req, res) => {
  try {
    const { order } = req.body;
    for (let i = 0; i < order.length; i++) {
      await Overlay.sequelize.query(
        'UPDATE Overlays SET "order" = ? WHERE id = ?',
        {
          replacements: [i, order[i]],
          type: Overlay.sequelize.QueryTypes.UPDATE
        }
      );
    }
    res.status(200).send();
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;