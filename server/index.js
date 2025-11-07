const express = require('express');
const cors = require('cors');
const path = require('path');
const { sequelize } = require('./models/MediaItem');

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Import routes
const mediaRoutes = require('./routes/media');
const streamRoutes = require('./routes/stream');

// Routes
app.use('/api/media', mediaRoutes);
app.use('/api/stream', streamRoutes);

// Initialize database
const initializeDatabase = async () => {
  try {
    await sequelize.sync({ force: false }); // Create tables if they don't exist
    console.log('Database connected and synchronized');
  } catch (error) {
    console.error('Error initializing database:', error);
    process.exit(1);
  }
};

// Start server
const startServer = async () => {
  await initializeDatabase();
  
  app.listen(PORT, () => {
    console.log(`Media Library Server running on port ${PORT}`);
  });
};

startServer().catch(console.error);

module.exports = app;