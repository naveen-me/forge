const fs = require('fs');
const path = require('path');
const { Sequelize, DataTypes } = require('sequelize');

// Create Sequelize instance with absolute path
const dbPath = path.resolve(__dirname, '..', '..', 'db', 'database.sqlite');
const sequelize = new Sequelize({
  dialect: 'sqlite',
  storage: dbPath,
  logging: console.log // Set to console.log for debugging
});

// Read all model files
const models = {};
const modelsDir = __dirname;

fs.readdirSync(modelsDir)
  .filter(file => {
    return (file.indexOf('.') !== 0) && (file !== 'index.js') && (file.slice(-3) === '.js');
  })
  .forEach(file => {
    const model = require(path.join(modelsDir, file))(sequelize, DataTypes);
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

module.exports = db;