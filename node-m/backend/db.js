const { Sequelize } = require('sequelize');
const fs = require('fs');
const path = require('path');

const sequelize = new Sequelize({
  dialect: 'sqlite',
  storage: path.join(__dirname, 'db', 'database.sqlite'),
  logging: false,
});

const db = {};

const modelsDir = path.join(__dirname, 'db', 'models');

fs.readdirSync(modelsDir)
  .filter(file => {
    return (file.indexOf('.') !== 0) && (file.slice(-3) === '.js') && (file !== 'index.js');
  })
  .forEach(file => {
    const modelFunction = require(path.join(modelsDir, file));
    if (typeof modelFunction === 'function') {
      const model = modelFunction(sequelize, Sequelize.DataTypes);
      db[model.name] = model;
    }
  });

Object.keys(db).forEach(modelName => {
  if (db[modelName].associate) {
    db[modelName].associate(db);
  }
});

db.sequelize = sequelize;
db.Sequelize = Sequelize;

module.exports = db;