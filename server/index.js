const express = require('express');
const cors = require('cors');
const path = require('path');
const sequelize = require('./src/database');
const Overlay = require('./models/Overlay');
const { Ad } = require('./models/Ad');
const { MediaItem } = require('./models/MediaItem');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3001; // Changed back to 3001

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Serve static files from the 'public' directory
app.use(express.static(path.join(__dirname, 'public')));

// Import routes (using dynamic imports for ES modules)
const mediaRoutes = require('./routes/media');
const streamRoutes = require('./routes/stream');
const overlayRoutes = require('./routes/overlays');
const adRoutes = require('./routes/ads');

// Import ES modules using dynamic imports
const loadRoutes = async () => {
  const { default: authRoutes } = await import('./src/routes/auth.js');
  const { default: apiRoutes } = await import('./src/routes/api.js');

  // Routes
  app.use('/api/media', mediaRoutes);
  app.use('/api/stream', streamRoutes);
  app.use('/api/overlays', overlayRoutes);
  app.use('/api/ads', adRoutes);
  app.use('/auth', authRoutes);  // Auth endpoints like /auth/login, /auth/register
  app.use('/api', apiRoutes);    // Main API endpoints with auth middleware
};

// Initialize database
const initializeDatabase = async () => {
  try {
    const models = { Ad, MediaItem };
    Ad.associate(models);
    MediaItem.associate(models);
    await sequelize.sync({ alter: true }); // Alter tables to match model definitions
    console.log('Database connected and synchronized');
  } catch (error) {
    console.error('Error initializing database:', error);
    process.exit(1);
  }
};

const http = require('http');
const { setupWebSocket } = require('./src/services/taskService');

// ... (rest of the file)

// Start server
const startServer = async () => {
  await loadRoutes();
  await initializeDatabase();

  const server = http.createServer(app);
  setupWebSocket(server);

  server.listen(PORT, () => {
    console.log(`Media Library Server running on port ${PORT}`);
  });
};

startServer().catch(console.error);

module.exports = app;