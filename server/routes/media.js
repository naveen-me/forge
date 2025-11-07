const express = require('express');
const { MediaItem } = require('../models/MediaItem');
const { checkFileExists } = require('../utils/fileUtils');
const router = express.Router();

// Get items in a folder
router.get('/folder/:parentId?', async (req, res) => {
  try {
    const parentId = req.params.parentId || null;
    
    const items = await MediaItem.findAll({
      where: { parentId },
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
        size,
        mimeType,
        parentId: parentId || null
      });
      
      createdFiles.push(newFile);
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

// Delete an item
router.delete('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    
    const item = await MediaItem.findByPk(id);
    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
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

module.exports = router;