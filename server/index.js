import express from 'express';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import sequelize from './src/database.js';
import Overlay from './models/Overlay.js';
import { Ad } from './models/Ad.js';
import { MediaItem } from './models/MediaItem.js';
import Link from './models/Link.js';
import Schedule from './models/Schedule.js';
import dotenv from 'dotenv';
dotenv.config();

// Define __dirname for ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
const PORT = process.env.PORT || 3001; // Changed back to 3001

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Serve static files from the 'public' directory
app.use(express.static(path.join(__dirname, 'public')));

// Serve thumbnails directory
app.use('/thumbnails', express.static(path.join(__dirname, 'thumbnails')));

// Import routes using ES modules
import mediaRoutes from './src/routes/media.js';
import streamRoutes from './routes/stream.js';
import overlayRoutes from './routes/overlays.js';
import adRoutes from './routes/ads.js';
import linkRoutes from './routes/links.js';
import scheduleRoutes from './routes/schedule.js';

// Import ES modules using dynamic imports
const loadRoutes = async () => {
  const { default: authRoutes } = await import('./src/routes/auth.js');
  const { default: apiRoutes } = await import('./src/routes/api.js');

  // Authentication routes (public) - should be loaded first
  app.use('/auth', authRoutes);  // Auth endpoints like /auth/login, /auth/register

  // Main API routes (protected) - core business logic
  app.use('/api', apiRoutes);    // Main API endpoints with auth middleware

  // Resource-specific routes (protected) - organized by resource type
  app.use('/api/media', mediaRoutes);      // Media management
  app.use('/api/overlays', overlayRoutes); // Overlay management
  app.use('/api/ads', adRoutes);           // Ads management
  app.use('/api/links', linkRoutes);       // Links management
  app.use('/api/schedule', scheduleRoutes); // Schedule management
  app.use('/api/stream', streamRoutes);    // Stream management
};

// Initialize database
const initializeDatabase = async () => {
  try {
    const models = { Ad, MediaItem, Overlay, Link, Schedule };
    for (const modelName in models) {
      if (models[modelName].associate) {
        models[modelName].associate(models);
      }
    }
    
    // Try to sync the database, but don't crash if it fails
    try {
      // Using { force: true } will drop and recreate tables. This is useful in development
      // but will cause data loss. Use { alter: true } in production.
      await sequelize.query('PRAGMA foreign_keys = OFF;', null, { raw: true });
      await sequelize.sync({ force: true }); // Force sync to recreate tables
      await sequelize.query('PRAGMA foreign_keys = ON;', null, { raw: true });
      console.log('Database connected and synchronized (forced)');
    } catch (syncError) {
      console.warn('Database sync failed, continuing with existing schema:', syncError.message);
    }
  } catch (error) {
    console.error('Error initializing database:', error);
    process.exit(1);
  }
};

import http from 'http';
import { setupWebSocket } from './src/services/taskService.js';

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