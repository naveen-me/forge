import express from 'express';
import { ImageGenerator } from '../services/imageGenerator.js';
import path from 'path';
import fs from 'fs';

const router = express.Router();
let imageGenerator = null;

router.post('/generate', async (req, res) => {
  try {
    const { db } = req.app.locals;
    if (!imageGenerator || imageGenerator.db !== db) {
      imageGenerator = new ImageGenerator(db);
    }

    const { questionIds, presetId, customName, prefix, hideTimer, mode } = req.body;

    if (!questionIds || !Array.isArray(questionIds) || questionIds.length === 0) {
      return res.status(400).json({ error: 'questionIds array is required' });
    }

    if (!presetId) {
      return res.status(400).json({ error: 'presetId is required' });
    }

    const result = await imageGenerator.generateImages(questionIds, presetId, { 
      customName, 
      prefix,
      hideTimer: !!hideTimer,
      mode: mode || 'separate'
    });

    res.json({
      success: true,
      folderName: result.folderName,
      questionCount: result.questionCount,
      imageCount: result.imageCount,
      message: 'Images generated successfully'
    });

  } catch (error) {
    console.error('Generate images error:', error);
    res.status(500).json({ error: error.message });
  }
});

router.get('/list', (req, res) => {
  try {
    const { db } = req.app.locals;
    if (!imageGenerator || imageGenerator.db !== db) {
      imageGenerator = new ImageGenerator(db);
    }
    const sets = imageGenerator.listImageSets();
    res.json(sets);
  } catch (error) {
    console.error('List image sets error:', error);
    res.status(500).json({ error: error.message });
  }
});

router.get('/download/:folderName', async (req, res) => {
  try {
    const { db } = req.app.locals;
    if (!imageGenerator || imageGenerator.db !== db) {
      imageGenerator = new ImageGenerator(db);
    }
    const { folderName } = req.params;

    res.attachment(`${folderName}.zip`);
    await imageGenerator.createZip(folderName, res);
  } catch (error) {
    console.error('Download images error:', error);
    res.status(500).json({ error: error.message });
  }
});

router.delete('/:folderName', (req, res) => {
  try {
    const { db } = req.app.locals;
    if (!imageGenerator || imageGenerator.db !== db) {
      imageGenerator = new ImageGenerator(db);
    }
    const { folderName } = req.params;
    const success = imageGenerator.deleteImageSet(folderName);
    if (success) {
      res.json({ success: true, message: 'Image set deleted' });
    } else {
      res.status(404).json({ error: 'Image set not found' });
    }
  } catch (error) {
    console.error('Delete image set error:', error);
    res.status(500).json({ error: error.message });
  }
});

// New endpoint to get images in a set
router.get('/set/:folderName', (req, res) => {
    try {
        const { folderName } = req.params;
        const setPath = path.join(process.cwd(), 'storage/generated_images', folderName);
        
        if (!fs.existsSync(setPath)) {
            return res.status(404).json({ error: 'Image set not found' });
        }

        const images = fs.readdirSync(setPath)
            .filter(f => f.endsWith('.png'))
            .map(f => `/storage/generated_images/${folderName}/${f}`);

        res.json(images);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

export default router;
