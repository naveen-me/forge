import express from 'express';
import { MediaItem } from '../models/MediaItem.js';
import { checkFileExists } from '../utils/fileUtils.js';
import { addThumbnailJob } from '../src/services/taskService.js';
import { authenticateToken } from '../src/middleware/auth.js'; // Import auth middleware
import dialog from 'node-file-dialog';

const router = express.Router();

// Apply authentication middleware to all media routes
router.use(authenticateToken);

// Select files
router.get('/select-files', async (req, res) => {
  try {
    let files = await dialog({ type: 'open-files' });

    // Clean file paths by trimming whitespace including \r characters
    if (Array.isArray(files)) {
      files = files.map(file => {
        if (typeof file === 'string') {
          return file.trim(); // Remove leading/trailing whitespace including \r, \n
        }
        return file;
      });
    }

    res.json({ files });
  } catch (e) {
    res.json({ files: [] });
  }
});

// Get items in a folder
router.get('/folder/:parentId?', async (req, res) => {
  try {
    const parentId = req.params.parentId || null;

    const whereClause = { parentId: parentId };
    
    // If parentId is null or 'null', we want items where parentId is null (top-level items)
    if (parentId === null || parentId === 'null' || parentId === 'undefined') {
      whereClause.parentId = null;
    }

    const items = await MediaItem.findAll({
      where: whereClause,
      order: [
        ['type', 'ASC'], // Folders first
        ['name', 'ASC']
      ]
    });

    // Add missing file status for files
    const itemsWithStatus = items.map(item => {
      const itemData = item.toJSON();
      if (itemData.type === 'file' && itemData.filePath) {
        itemData.isMissing = !checkFileExists(itemData.filePath);
      }
      return itemData;
    });

    res.json(itemsWithStatus);
  } catch (error) {
    console.error('Error fetching folder contents:', error);
    res.status(500).json({ error: 'Failed to fetch folder contents' });
  }
});

// Create a new folder
router.post('/folder', async (req, res) => {
  try {
    const { name, parentId } = req.body;

    if (!name) {
      return res.status(400).json({ error: 'Folder name is required' });
    }

    const newFolder = await MediaItem.create({
      name,
      type: 'folder',
      parentId: parentId || null
    });

    res.status(201).json(newFolder);
  } catch (error) {
    console.error('Error creating folder:', error);
    res.status(500).json({ error: 'Failed to create folder' });
  }
});

// Get folder path for breadcrumbs
router.get('/folder/:id/path', async (req, res) => {
  try {
    const { id } = req.params;
    if (!id || id === 'null') {
      return res.json([]);
    }

    let path = [];
    let current = await MediaItem.findByPk(id);

    while (current) {
      path.unshift(current.toJSON());
      if (current.parentId) {
        current = await MediaItem.findByPk(current.parentId);
      } else {
        current = null;
      }
    }

    res.json(path);
  } catch (error) {
    console.error('Error fetching folder path:', error);
    res.status(500).json({ error: 'Failed to fetch folder path' });
  }
});



// Add files (store file paths)
router.post('/files', async (req, res) => {
  try {
    const { files, parentId } = req.body;

    if (!files || !Array.isArray(files) || files.length === 0) {
      return res.status(400).json({ error: 'Files array is required' });
    }

    const createdFiles = [];

    for (const file of files) {
      const { name, path: filePath, size, mimeType } = file;

      // Extract name from file path if not provided
      const fileName = name || filePath.split(/[\\/]/).pop();

      const newFile = await MediaItem.create({
        name: fileName,
        type: 'file',
        filePath,
        size: size || null,  // Set to null to indicate no metadata yet
        mimeType: mimeType || null,
        duration: null,      // No duration yet
        dimensions: null,    // No dimensions yet
        parentId: parentId || null,
        status: 'processing' // Mark as processing
      });

      createdFiles.push(newFile);

      addThumbnailJob(newFile.id, 'MediaItem');
    }

    res.status(201).json(createdFiles);
  } catch (error) {
    console.error('Error adding files:', error);
    res.status(500).json({ error: 'Failed to add files' });
  }
});

// Rename an item
router.put('/:id/rename', async (req, res) => {
  try {
    const { id } = req.params;
    const { name } = req.body;

    if (!name) {
      return res.status(400).json({ error: 'Name is required' });
    }

    const item = await MediaItem.findByPk(id);
    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
    }

    await item.update({ name });
    res.json(item);
  } catch (error) {
    console.error('Error renaming item:', error);
    res.status(500).json({ error: 'Failed to rename item' });
  }
});

// Move an item
router.put('/:id/move', async (req, res) => {
  try {
    const { id } = req.params;
    const { parentId } = req.body;

    const item = await MediaItem.findByPk(id);
    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
    }

    await item.update({ parentId: parentId || null });
    res.json(item);
  } catch (error) {
    console.error('Error moving item:', error);
    res.status(500).json({ error: 'Failed to move item' });
  }
});



import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

// Define __dirname for ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Delete an item
router.delete('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const item = await MediaItem.findByPk(id);
    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
    }

    // If the item has a thumbnail, delete it from the filesystem
    if (item.thumbnailPath) {
      const thumbnailPath = path.join(__dirname, '..', 'public', item.thumbnailPath);
      if (fs.existsSync(thumbnailPath)) {
        fs.unlink(thumbnailPath, (err) => {
          if (err) {
            console.error('Error deleting thumbnail:', err);
          }
        });
      }
    }

    await item.destroy();
    res.json({ message: 'Item deleted successfully' });
  } catch (error) {
    console.error('Error deleting item:', error);
    res.status(500).json({ error: 'Failed to delete item' });
  }
});

// Search items by name
router.get('/search/:query', async (req, res) => {
  try {
    const { query } = req.params;

    const items = await MediaItem.findAll({
      where: {
        name: {
          [MediaItem.sequelize.Op.like]: `%${query}%`
        }
      },
      order: [
        ['type', 'ASC'], // Folders first
        ['name', 'ASC']
      ],
      limit: 50 // Limit results
    });

    res.json(items);
  } catch (error) {
    console.error('Error searching items:', error);
    res.status(500).json({ error: 'Failed to search items' });
  }
});

export default router;