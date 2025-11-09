const { DataTypes } = require('sequelize');
const sequelize = require('../src/database');

const Overlay = sequelize.define('Overlay', {
  id: {
    type: DataTypes.INTEGER,
    autoIncrement: true,
    primaryKey: true,
  },
  name: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  type: {
    type: DataTypes.ENUM('image', 'video', 'text', 'group'),
    allowNull: false,
  },
  source: {
    type: DataTypes.STRING,
    allowNull: true,
  },
  x: {
    type: DataTypes.INTEGER,
    defaultValue: 0,
  },
  y: {
    type: DataTypes.INTEGER,
    defaultValue: 0,
  },
  width: {
    type: DataTypes.INTEGER,
    defaultValue: 1920,
  },
  height: {
    type: DataTypes.INTEGER,
    defaultValue: 1080,
  },
  fit: {
    type: DataTypes.ENUM('fit', 'cover', 'fill'),
    defaultValue: 'fit',
  },
  opacity: {
    type: DataTypes.FLOAT,
    defaultValue: 1,
  },
  filters: {
    type: DataTypes.JSON,
    allowNull: true,
  },
  parentId: {
    type: DataTypes.INTEGER,
    allowNull: true,
    references: {
      model: 'Overlays',
      key: 'id',
    },
  },
}, {
  timestamps: true,
});

module.exports = Overlay;
