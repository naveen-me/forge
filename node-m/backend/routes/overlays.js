const express = require('express');
const { body, validationResult } = require('express-validator');
const router = express.Router();
const db = require('../db');

// Middleware for error handling
const asyncHandler = fn => (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
};

// GET /api/overlays - Get all overlays
router.get('/', asyncHandler(async (req, res) => {
    const overlays = await db.Overlay.findAll();
    res.json(overlays);
}));

// GET /api/overlays/:id - Get a specific overlay
router.get('/:id', asyncHandler(async (req, res) => {
    const overlay = await db.Overlay.findByPk(req.params.id);
    if (!overlay) {
        return res.status(404).json({ error: 'Overlay not found' });
    }
    res.json(overlay);
}));

// POST /api/overlays - Create a new overlay
router.post('/', [
    body('name').notEmpty(),
    body('type').isIn(['text', 'image', 'html']),
    body('position').optional().isObject(),
    body('content').optional().isObject()
], asyncHandler(async (req, res) => {
    // Check for validation errors
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ 
            error: 'Validation failed', 
            errors: errors.array() 
        });
    }
    
    const overlay = await db.Overlay.create(req.body);
    res.status(201).json(overlay);
}));

// PUT /api/overlays/:id - Update an overlay
router.put('/:id', [
    body('name').optional().notEmpty(),
    body('type').optional().isIn(['text', 'image', 'html']),
    body('position').optional().isObject(),
    body('content').optional().isObject()
], asyncHandler(async (req, res) => {
    // Check for validation errors
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ 
            error: 'Validation failed', 
            errors: errors.array() 
        });
    }
    
    const overlay = await db.Overlay.findByPk(req.params.id);
    if (!overlay) {
        return res.status(404).json({ error: 'Overlay not found' });
    }
    await overlay.update(req.body);
    res.json(overlay);
}));

// DELETE /api/overlays/:id - Delete an overlay
router.delete('/:id', asyncHandler(async (req, res) => {
    const overlay = await db.Overlay.findByPk(req.params.id);
    if (!overlay) {
        return res.status(404).json({ error: 'Overlay not found' });
    }
    await overlay.destroy();
    res.status(204).send();
}));

// GET /api/overlays/types - Get overlay types
router.get('/types', (req, res) => {
    res.json([
        { id: 'text', name: 'Text Overlay', description: 'Text-based overlay' },
        { id: 'image', name: 'Image Overlay', description: 'Image-based overlay' },
        { id: 'html', name: 'HTML Overlay', description: 'HTML-based overlay' }
    ]);
});

module.exports = router;