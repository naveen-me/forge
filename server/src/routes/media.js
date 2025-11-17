import express from 'express';
import { authenticateToken } from '../middleware/auth.js';
import { listMediaItems, createFolder, addFiles, renameMediaItem, deleteMediaItems, moveMediaItems, copyMediaItems, streamMediaItem } from '../controllers/mediaController.js';

const router = express.Router();

// All media routes should be protected
router.use(authenticateToken);

// Additional import for select-files endpoint
import dialog from 'node-file-dialog';

// Import MediaItem model and Sequelize for thumbnail path fixer
import { MediaItem } from '../../models/MediaItem.js';
import { Op } from 'sequelize';

// GET /api/media?parentId=...
router.get('/', listMediaItems);

// POST /api/media/folder
router.post('/folder', createFolder);

// POST /api/media/files
router.post('/files', addFiles);

// PUT /api/media/:id/rename
router.put('/:id/rename', renameMediaItem);

// POST /api/media/move
router.post('/move', moveMediaItems);

// POST /api/media/copy
router.post('/copy', copyMediaItems);

// POST /api/media/delete
router.post('/delete', deleteMediaItems);

// GET /api/media/stream/:id
router.get('/stream/:id', streamMediaItem);


// GET /api/media/folder (get all folders for user)
router.get('/folder', async (req, res) => {
  const userId = req.user.id;
  try {
    const folders = await MediaItem.findAll({
      where: { 
        userId,
        type: 'folder'
      },
      order: [
        ['name', 'ASC']
      ]
    });
    res.json({ success: true, data: folders });
  } catch (error) {
    console.error('Error listing folders:', error);
    res.status(500).json({ success: false, message: 'Failed to list folders.' });
  }
});

// GET /api/media/folder/:id/path (get folder path for breadcrumbs)
router.get('/folder/:id/path', async (req, res) => {
  const { id } = req.params;
  const userId = req.user.id;
  let path = [];
  
  try {
    // If id is null or 'null', return empty path for root
    if (!id || id === 'null') {
      res.json({ success: true, data: path });
      return;
    }

    // Build the path by traversing up the parent hierarchy
    let currentId = parseInt(id) || id;
    const pathIds = []; // To prevent circular references
    
    while (currentId) {
      if (pathIds.includes(currentId)) {
        // Circular reference detected
        break;
      }
      pathIds.push(currentId);
      
      const folder = await MediaItem.findByPk(currentId);
      if (!folder || folder.userId !== userId) {
        break; // Stop if we can't find the folder or it doesn't belong to user
      }
      
      // Add to beginning of array to maintain correct order (root first)
      path.unshift({
        id: folder.id,
        name: folder.name
      });
      
      if (!folder.parentId) {
        break; // Reached the root
      }
      
      currentId = folder.parentId;
    }
    
    res.json({ success: true, data: path });
  } catch (error) {
    console.error('Error getting folder path:', error);
    res.status(500).json({ success: false, message: 'Failed to get folder path.' });
  }
});

// GET /api/media/select-files (for file selection dialog)
router.get('/select-files', async (req, res) => {
  try {
    let files = await dialog({ type: 'open-files' });
    if (Array.isArray(files)) {
      files = files.map(file => {
        if (typeof file === 'string') {
          return { path: file.trim() };
        }
        return file;
      });
    }
    res.json({ files: files || [] });
  } catch (e) {
    res.json({ files: [] });
  }
});

// Endpoint to fix thumbnail paths (for updating old absolute paths to web paths)
router.get('/fix-thumbnail-paths', async (req, res) => {
  try {
    // Get all media items with thumbnail paths that are absolute file paths
    const items = await MediaItem.findAll({
      where: {
        [Op.and]: [
          { thumbnailPath: { [Op.like]: '%:\\\\%' } },  // Contains Windows drive letter
          { thumbnailPath: { [Op.like]: '%thumbnails%' } }  // Contains "thumbnails" directory
        ]
      }
    });

    // Update each item's thumbnail path to be web-accessible
    const fixedItems = [];
    for (const item of items) {
      const oldPath = item.thumbnailPath;
      const fileName = oldPath.split(/[\\/]/).pop();
      if (fileName) {
        const newPath = `/thumbnails/${fileName}`;
        await item.update({ thumbnailPath: newPath });
        fixedItems.push({ id: item.id, oldPath: oldPath });
      }
    }

    res.json({
      success: true,
      message: `Fixed ${items.length} thumbnail paths`,
      fixedItems: fixedItems
    });
  } catch (error) {
    console.error('Error fixing thumbnail paths:', error);
    res.status(500).json({ success: false, message: 'Failed to fix thumbnail paths.' });
  }
});

export default router;
