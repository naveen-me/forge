const path = require('path');
const config = require('./config');
const { Sequelize } = require('sequelize');
const fs = require('fs');

// Read all model files
const models = {};
const modelsDir = path.join(__dirname, 'models');

// Create Sequelize instance with proper configuration
const env = process.env.NODE_ENV || 'development';
const dbConfig = config[env];

// For SQLite, we need to handle the storage option specially
const sequelizeOptions = {
  dialect: dbConfig.dialect,
  logging: dbConfig.logging
};

// Add storage option for SQLite
if (dbConfig.dialect === 'sqlite' && dbConfig.storage) {
  sequelizeOptions.storage = dbConfig.storage;
}

const sequelize = new Sequelize(sequelizeOptions);

// Load models
fs.readdirSync(modelsDir)
  .filter(file => {
    return (file.indexOf('.') !== 0) && (file !== 'index.js') && (file.slice(-3) === '.js');
  })
  .forEach(file => {
    const model = require(path.join(modelsDir, file))(sequelize, Sequelize.DataTypes);
    models[model.name] = model;
  });

// Create associations if any
Object.keys(models).forEach(modelName => {
  if (models[modelName].associate) {
    models[modelName].associate(models);
  }
});

// Add sequelize instance and models to db object
const db = {
  ...models,
  sequelize,
  Sequelize
};

// Test the connection
async function testConnection() {
  try {
    await db.sequelize.authenticate();
    console.log('Database connection has been established successfully.');
  } catch (error) {
    console.error('Unable to connect to the database:', error);
  }
}

// Sync all models - only use this when explicitly needed
async function syncModels() {
  try {
    await db.sequelize.sync({ alter: true }); // Use { force: true } to drop and recreate tables
    console.log('All models were synchronized successfully.');
  } catch (error) {
    console.error('Error synchronizing models:', error);
  }
}

// Initialize database without automatic syncing
async function initializeDatabase() {
  try {
    await db.sequelize.authenticate();
    console.log('Database connection has been established successfully.');
    // Don't automatically sync models - rely on migrations instead
    console.log('Database initialized. Run migrations manually when needed.');
  } catch (error) {
    console.error('Database initialization failed:', error);
  }
}

module.exports = {
  ...db,
  testConnection,
  syncModels,
  initializeDatabase
};