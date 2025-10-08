const express = require('express');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

// Middleware to protect routes by requiring authentication
router.use('/schedules/*', verifyToken);
router.use('/media/*', verifyToken);
router.use('/overlays/*', verifyToken);

module.exports = router;