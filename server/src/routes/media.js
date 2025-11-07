import express from 'express';
import { authenticateToken } from '../middleware/auth.js';
import { listMediaItems, createFolder, addFiles, renameMediaItem, deleteMediaItems, moveMediaItems, copyMediaItems, streamMediaItem } from '../controllers/mediaController.js';

const router = express.Router();

// All media routes should be protected
router.use(authenticateToken);

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

export default router;
