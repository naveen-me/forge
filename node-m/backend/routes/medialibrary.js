const express = require('express');
const { body, validationResult, query } = require('express-validator');
const router = express.Router();
const db = require('../db');
const fs = require('fs');
const { Op } = require('sequelize');

// Middleware for error handling
const asyncHandler = fn => (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
};

// GET /api/media - Get all media library items with pagination and search
router.get('/', [
    query('folderId').optional().isInt().toInt(),
    query('page').optional().isInt({ min: 1 }).toInt(),
    query('limit').optional().isInt({ min: 1 }).toInt(),
    query('search').optional().isString()
], asyncHandler(async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ 
            error: 'Validation failed', 
            errors: errors.array() 
        });
    }
    
    const { folderId, page = 1, limit = 20, search } = req.query;
    const options = {
        where: {},
        limit: limit,
        offset: (page - 1) * limit,
        order: [['createdAt', 'DESC']]
    };
    
    if (folderId) {
        options.where.folderId = folderId;
    }

    if (search) {
        options.where[Op.or] = [
            { filename: { [Op.like]: `%${search}%` } },
            { displayName: { [Op.like]: `%${search}%` } }
        ];
    }
    
    const { count, rows } = await db.MediaLibrary.findAndCountAll(options);
    res.json({
        totalItems: count,
        totalPages: Math.ceil(count / limit),
        currentPage: page,
        media: rows
    });
}));

// GET /api/media/types - Get supported media types
router.get('/types', (req, res) => {
    res.json(['video', 'image', 'audio', 'stream']);
});


// GET /api/media/:id - Get a specific media item
router.get('/:id', asyncHandler(async (req, res) => {
    const mediaItem = await db.MediaLibrary.findByPk(req.params.id);
    if (!mediaItem) {
        return res.status(404).json({ error: 'Media item not found' });
    }
    res.json(mediaItem);
}));

// POST /api/media - Create a new media item
router.post('/', [
    body('filename').notEmpty(),
    body('filepath').notEmpty(),
    body('type').isIn(['video', 'image', 'audio', 'stream']),
    body('duration').optional().isInt({ min: 0 }).toInt(),
    body('folderId').optional().isInt().toInt()
], asyncHandler(async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ 
            error: 'Validation failed', 
            errors: errors.array() 
        });
    }
    
    const { filename } = req.body;
    const mediaItem = await db.MediaLibrary.create({
        ...req.body,
        displayName: filename // Default displayName to filename
    });
    res.status(201).json(mediaItem);
}));

// PUT /api/media/:id - Update a media item
router.put('/:id', [
    body('filename').optional().notEmpty(),
    body('displayName').optional().trim(),
    body('folderId').optional().isInt().toInt()
], asyncHandler(async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ 
            error: 'Validation failed', 
            errors: errors.array() 
        });
    }
    
    const mediaItem = await db.MediaLibrary.findByPk(req.params.id);
    if (!mediaItem) {
        return res.status(404).json({ error: 'Media item not found' });
    }

    await mediaItem.update(req.body);
    res.json(mediaItem);
}));

// DELETE /api/media/:id - Delete a media item
router.delete('/:id', asyncHandler(async (req, res) => {
    const mediaItem = await db.MediaLibrary.findByPk(req.params.id);
    if (!mediaItem) {
        return res.status(404).json({ error: 'Media item not found' });
    }
    await mediaItem.destroy();
    res.status(204).send();
}));

// POST /api/media/add-files - Add multiple media files
router.post('/add-files', [
    body('files').isArray({ min: 1 }),
    body('folderId').optional().isInt().toInt()
], asyncHandler(async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ error: 'Validation failed', errors: errors.array() });
    }

    const { files, folderId } = req.body;
    const createdMediaItems = [];

    for (const filePath of files) {
        const extension = filePath.split('.').pop().toLowerCase();
        let fileType = 'file';
        if (['mp4', 'avi', 'mov'].includes(extension)) fileType = 'video';
        else if (['jpg', 'jpeg', 'png'].includes(extension)) fileType = 'image';
        else if (['mp3', 'wav'].includes(extension)) fileType = 'audio';

        const filename = filePath.split(/[\\/]/).pop();

        const mediaItem = await db.MediaLibrary.create({
            filename: filename,
            displayName: filename,
            filepath: filePath,
            type: fileType,
            folderId: folderId || null,
            duration: 0,
            thumbnailPath: null
        });
        createdMediaItems.push(mediaItem);
    }
    
    res.status(201).json(createdMediaItems);
}));

// POST /api/media/bulk - Bulk operations for media
router.post('/bulk', [
    body('operation').isIn(['delete', 'move']),
    body('mediaIds').isArray({ min: 1 }),
    body('targetFolderId').optional().isInt().toInt()
], asyncHandler(async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ error: 'Validation failed', errors: errors.array() });
    }

    const { operation, mediaIds, targetFolderId } = req.body;

    if (operation === 'delete') {
        await db.MediaLibrary.destroy({ where: { id: mediaIds } });
        res.json({ message: `${mediaIds.length} media items deleted successfully` });
    } else if (operation === 'move') {
        if (targetFolderId === undefined) {
            return res.status(400).json({ error: 'targetFolderId is required for move operation' });
        }
        await db.MediaLibrary.update({ folderId: targetFolderId }, { where: { id: mediaIds } });
        res.json({ message: `${mediaIds.length} media items moved to folder ${targetFolderId}` });
    } else {
        res.status(400).json({ error: 'Invalid operation' });
    }
}));

// POST /api/media/validate-files - Validate the existence of media files
router.post('/validate-files', asyncHandler(async (req, res) => {
    const mediaItems = await db.MediaLibrary.findAll();
    const validationResults = await Promise.all(mediaItems.map(async item => {
        const exists = await fs.promises.access(item.filepath, fs.constants.F_OK).then(() => true).catch(() => false);
        return { ...item.toJSON(), exists };
    }));
    res.json(validationResults);
}));

module.exports = router;