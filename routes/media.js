import express from 'express';
import multer from 'multer';
import { MediaManager } from '../services/mediaManager.js';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const router = express.Router();

// Configure multer for file uploads
const upload = multer({
  dest: path.join(__dirname, '../storage/temp'),
  limits: {
    fileSize: 100 * 1024 * 1024 // 100MB limit
  },
  fileFilter: (req, file, cb) => {
    // Accept images and videos
    if (file.mimetype.startsWith('image/') || file.mimetype.startsWith('video/')) {
      cb(null, true);
    } else {
      cb(new Error('Only images and videos are allowed'));
    }
  }
});

/**
 * POST /api/media/upload
 * Upload new media file
 */
router.post('/upload', upload.single('file'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'No file uploaded' });
    }

    const db = req.app.locals.db;
    const persistDb = req.app.locals.persistDb;
    const mediaManager = new MediaManager(db);

    const media = await mediaManager.saveMedia(req.file);
    await persistDb();

    res.json({
      success: true,
      media: media
    });

  } catch (error) {
    console.error('Upload error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/media
 * List all media with optional filters
 */
router.get('/', (req, res) => {
  try {
    const db = req.app.locals.db;
    const mediaManager = new MediaManager(db);

    const filters = {
      type: req.query.type,
      search: req.query.search,
      sortBy: req.query.sortBy,
      sortOrder: req.query.sortOrder
    };

    const media = mediaManager.listMedia(filters);

    res.json(media.map(item => ({
      ...item,
      label: item.original_name || item.filename
    })));

  } catch (error) {
    console.error('List media error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/media/stats
 * Get storage statistics
 */
router.get('/stats', (req, res) => {
  try {
    const db = req.app.locals.db;
    const mediaManager = new MediaManager(db);
    const stats = mediaManager.getStats();
    res.json(stats);
  } catch (error) {
    console.error('Stats error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/media/:id
 * Get specific media info
 */
router.get('/:id', (req, res) => {
  try {
    const db = req.app.locals.db;
    const mediaManager = new MediaManager(db);
    const media = mediaManager.getMedia(req.params.id);

    if (!media) {
      return res.status(404).json({ error: 'Media not found' });
    }

    res.json(media);

  } catch (error) {
    console.error('Get media error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * PUT /api/media/:id
 * Update media metadata (rename)
 */
router.put('/:id', async (req, res) => {
  try {
    const db = req.app.locals.db;
    const persistDb = req.app.locals.persistDb;
    const mediaManager = new MediaManager(db);

    const updates = {
      original_name: req.body.original_name
    };

    const media = mediaManager.updateMedia(req.params.id, updates);
    await persistDb();

    res.json(media);

  } catch (error) {
    console.error('Update media error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * DELETE /api/media/:id
 * Delete media file
 */
router.delete('/:id', async (req, res) => {
  try {
    const db = req.app.locals.db;
    const persistDb = req.app.locals.persistDb;
    const mediaManager = new MediaManager(db);

    mediaManager.deleteMedia(req.params.id);
    await persistDb();

    res.json({ success: true, message: 'Media deleted' });

  } catch (error) {
    console.error('Delete media error:', error);
    res.status(500).json({ error: error.message });
  }
});

export default router;
