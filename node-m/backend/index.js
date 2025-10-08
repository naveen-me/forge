const express = require('express');
const db = require('./db');
const cors = require('cors');
const { verifyToken } = require('./middleware/auth');
const path = require('path');

const app = express();
const port = 3001;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Import routes
const authRoutes = require('./routes/auth');

// Public routes (no authentication required)
app.use('/api/auth', authRoutes);

// Protected routes (require authentication)
const scheduleRoutes = require('./routes/schedules');
const mediaRoutes = require('./routes/medialibrary');
const overlayRoutes = require('./routes/overlays');
const folderRoutes = require('./routes/folders');

// Apply authentication middleware to protected routes
app.use('/api/schedules', verifyToken, scheduleRoutes);
app.use('/api/media', verifyToken, mediaRoutes);
app.use('/api/overlays', verifyToken, overlayRoutes);
app.use('/api/folders', verifyToken, folderRoutes);

// --- System Defaults Routes ---
const systemDefaultsRouter = express.Router();
systemDefaultsRouter.get('/', async (req, res) => {
    try { res.json(await db.SystemDefaults.findAll()); } catch (e) { res.status(500).json({ error: e.message }); }
});
app.use('/api/system-defaults', systemDefaultsRouter);

// --- Ads Routes ---
const adsRouter = express.Router();
adsRouter.get('/', async (req, res) => {
    try { res.json(await db.Ad.findAll()); } catch (e) { res.status(500).json({ error: e.message }); }
});
adsRouter.get('/ad-groups', async (req, res) => {
    try { res.json(await db.AdGroup.findAll()); } catch (e) { res.status(500).json({ error: e.message }); }
});
adsRouter.get('/group/:id', async (req, res) => {
    try { res.json(await db.Ad.findAll({ where: { adGroupId: req.params.id } })); } catch (e) { res.status(500).json({ error: e.message }); }
});
app.use('/api/ads', adsRouter);



// Error handling middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ 
        error: 'Something went wrong!', 
        message: process.env.NODE_ENV === 'development' ? err.message : 'Internal server error' 
    });
});

// 404 handler for undefined routes - should be last middleware
app.use((req, res) => {
    res.status(404).json({ error: 'Route not found' });
});

// Start Server
db.sequelize.sync().then(() => {
    app.listen(port, () => {
        console.log(`Node-M Backend listening on http://localhost:${port}`);
    });
});