const express = require('express');
const { body, validationResult } = require('express-validator');
const router = express.Router();
const db = require('../db');

// Middleware for error handling
const asyncHandler = fn => (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
};

// GET /api/folders - Get all folders
router.get('/', asyncHandler(async (req, res) => {
    try {
        // Include parent folder info and child folders
        const folders = await db.Folder.findAll({
            include: [{
                model: db.Folder,
                as: 'children',
                attributes: ['id', 'name', 'parentId'],
                separate: true
            }],
            order: [
                ['parentId', 'ASC'],
                ['name', 'ASC']
            ]
        });
        
        res.json(folders);
    } catch (error) {
        console.error('Error fetching folders:', error);
        res.status(500).json({ error: 'Failed to fetch folders' });
    }
}));

// GET /api/folders/:id - Get a specific folder
router.get('/:id', asyncHandler(async (req, res) => {
    try {
        const folder = await db.Folder.findByPk(req.params.id, {
            include: [{
                model: db.Folder,
                as: 'children',
                attributes: ['id', 'name', 'parentId']
            }, {
                model: db.MediaLibrary,
                as: 'mediaItems',
                attributes: ['id', 'filename', 'type', 'duration']
            }]
        });
        
        if (!folder) {
            return res.status(404).json({ error: 'Folder not found' });
        }
        
        res.json(folder);
    } catch (error) {
        console.error('Error fetching folder:', error);
        res.status(500).json({ error: 'Failed to fetch folder' });
    }
}));

// POST /api/folders - Create a new folder
router.post('/', [
    body('name').notEmpty().withMessage('Folder name is required'),
    body('parentId').optional({ nullable: true })
], asyncHandler(async (req, res) => {
    try {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ 
                error: 'Validation failed', 
                errors: errors.array() 
            });
        }
        
        const { name, parentId } = req.body;
        
        // Handle parentId - keep as null for root folders, or validate if provided
        let pId = parentId;
        if (parentId !== null && parentId !== undefined) {
            pId = parseInt(parentId, 10);
            if (isNaN(pId)) {
                return res.status(400).json({ error: 'Validation failed', errors: [{ msg: 'parentId must be an integer' }] });
            }
            
            // Check if parent folder exists
            const parentFolder = await db.Folder.findByPk(pId);
            if (!parentFolder) {
                return res.status(404).json({ error: 'Parent folder not found' });
            }
        } else {
            // For root folders, parentId should remain null
            pId = null;
        }
        
        const folder = await db.Folder.create({
            name,
            parentId: pId
        });
        
        res.status(201).json(folder);
    } catch (error) {
        console.error('Error creating folder:', error);
        res.status(500).json({ error: 'Failed to create folder' });
    }
}));

// PUT /api/folders/:id - Update a folder
router.put('/:id', [
    body('name').optional().notEmpty(),
    body('parentId').optional().isInt().toInt()
], asyncHandler(async (req, res) => {
    try {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ 
                error: 'Validation failed', 
                errors: errors.array() 
            });
        }
        
        const folder = await db.Folder.findByPk(req.params.id);
        if (!folder) {
            return res.status(404).json({ error: 'Folder not found' });
        }
        
        // Check if new parent exists (if parentId is provided and different from current)
        if (req.body.parentId !== undefined && req.body.parentId !== folder.parentId) {
            if (req.body.parentId !== null) {
                // Prevent circular reference
                if (req.body.parentId === folder.id) {
                    return res.status(400).json({ error: 'Cannot set folder as its own parent' });
                }
                
                const parentFolder = await db.Folder.findByPk(req.body.parentId);
                if (!parentFolder) {
                    return res.status(404).json({ error: 'Parent folder not found' });
                }
            }
        }
        
        await folder.update(req.body);
        res.json(folder);
    } catch (error) {
        console.error('Error updating folder:', error);
        res.status(500).json({ error: 'Failed to update folder' });
    }
}));

// DELETE /api/folders/:id - Delete a folder
router.delete('/:id', asyncHandler(async (req, res) => {
    try {
        const folder = await db.Folder.findByPk(req.params.id);
        if (!folder) {
            return res.status(404).json({ error: 'Folder not found' });
        }
        
        // First, move all contents to parent folder or root (if no parent)
        await db.MediaLibrary.update(
            { folderId: folder.parentId }, // Move media to parent folder
            { where: { folderId: folder.id } }
        );
        
        // Move child folders to parent folder
        await db.Folder.update(
            { parentId: folder.parentId },
            { where: { parentId: folder.id } }
        );
        
        // Finally, delete the folder
        await folder.destroy();
        
        res.status(204).send(); // No content
    } catch (error) {
        console.error('Error deleting folder:', error);
        res.status(500).json({ error: 'Failed to delete folder' });
    }
}));

// GET /api/folders/tree - Get folder tree structure
router.get('/tree', asyncHandler(async (req, res) => {
    try {
        // Get all folders
        const allFolders = await db.Folder.findAll();
        
        // Build tree structure
        const foldersMap = {};
        allFolders.forEach(folder => {
            foldersMap[folder.id] = { ...folder.toJSON(), children: [] };
        });
        
        const rootFolders = [];
        allFolders.forEach(folder => {
            const folderNode = foldersMap[folder.id];
            if (folder.parentId) {
                const parentNode = foldersMap[folder.parentId];
                if (parentNode) {
                    parentNode.children.push(folderNode);
                }
            } else {
                rootFolders.push(folderNode);
            }
        });
        
        res.json(rootFolders);
    } catch (error) {
        console.error('Error fetching folder tree:', error);
        res.status(500).json({ error: 'Failed to fetch folder tree' });
    }
}));

// GET /api/folders/:id/contents - Get folder contents (media and subfolders)
router.get('/:id/contents', asyncHandler(async (req, res) => {
    try {
        const folderId = parseInt(req.params.id);
        
        // Check if folder exists
        const folder = await db.Folder.findByPk(folderId);
        if (!folder) {
            return res.status(404).json({ error: 'Folder not found' });
        }
        
        // Get child folders and media items in this folder
        const [childFolders, mediaItems] = await Promise.all([
            db.Folder.findAll({ where: { parentId: folderId } }),
            db.MediaLibrary.findAll({ where: { folderId } })
        ]);
        
        res.json({
            folder: folder.toJSON(),
            childFolders,
            mediaItems
        });
    } catch (error) {
        console.error('Error fetching folder contents:', error);
        res.status(500).json({ error: 'Failed to fetch folder contents' });
    }
}));

module.exports = router;