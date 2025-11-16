import express from 'express';
const router = express.Router();
import Link from '../models/Link.js';

// Get all links
router.get('/', async (req, res) => {
  try {
    const links = await Link.findAll();
    res.json(links);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// Create a new link
router.post('/', async (req, res) => {
  try {
    const link = await Link.create(req.body);
    res.status(201).json(link);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// Get a single link
router.get('/:id', async (req, res) => {
  try {
    const link = await Link.findByPk(req.params.id);
    if (link) {
      res.json(link);
    } else {
      res.status(404).json({ message: 'Link not found' });
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// Update a link
router.put('/:id', async (req, res) => {
  try {
    const link = await Link.findByPk(req.params.id);
    if (link) {
      await link.update(req.body);
      res.json(link);
    } else {
      res.status(404).json({ message: 'Link not found' });
    }
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// Delete a link
router.delete('/:id', async (req, res) => {
  try {
    const link = await Link.findByPk(req.params.id);
    if (link) {
      await link.destroy();
      res.json({ message: 'Link deleted' });
    } else {
      res.status(404).json({ message: 'Link not found' });
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

export default router;
