const { DataTypes } = require('sequelize');
const sequelize = require('../src/database');

const Overlay = sequelize.define('Overlay', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
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
  fontFamily: {
    type: DataTypes.STRING,
    allowNull: true,
  },
  fontSize: {
    type: DataTypes.INTEGER,
    allowNull: true,
  },
  lineHeight: {
    type: DataTypes.FLOAT,
    allowNull: true,
  },
  backgroundColor: {
    type: DataTypes.STRING,
    allowNull: true,
  },
  wordWrap: {
    type: DataTypes.BOOLEAN,
    defaultValue: false,
  },
  filters: {
    type: DataTypes.JSON,
    allowNull: true,
  },
  parentId: {
    type: DataTypes.UUID,
    allowNull: true,
    references: {
      model: 'Overlays',
      key: 'id',
    },
  },
  order: {
    type: DataTypes.INTEGER,
    defaultValue: 0,
  },
}, {
  timestamps: true,
});

module.exports = Overlay;
