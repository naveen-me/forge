const { DataTypes } = require('sequelize');
const sequelize = require('../src/database');

// Define MediaItem model
const MediaItem = sequelize.define('MediaItem', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  name: {
    type: DataTypes.STRING,
    allowNull: false
  },
  type: {
    type: DataTypes.ENUM('file', 'folder'),
    allowNull: false
  },
  filePath: {
    type: DataTypes.STRING,
    allowNull: true // For folders, this will be null
  },
  mimeType: {
    type: DataTypes.STRING,
    allowNull: true
  },
  size: {
    type: DataTypes.BIGINT,
    allowNull: true
  },
  duration: {
    type: DataTypes.FLOAT,
    allowNull: true
  },
  dimensions: {
    type: DataTypes.STRING, // Format: "widthxheight"
    allowNull: true
  },
  status: {
    type: DataTypes.STRING,
    defaultValue: 'processing'
  },
  thumbnailPath: {
    type: DataTypes.STRING,
    allowNull: true
  },
  parentId: {
    type: DataTypes.INTEGER,
    allowNull: true,
    references: {
      model: 'MediaItems',
      key: 'id'
    }
  },
  createdAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW
  },
  updatedAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW
  }
}, {
  tableName: 'MediaItems',
  timestamps: true
});

// Create associations
MediaItem.associate = (models) => {
  MediaItem.hasMany(models.MediaItem, { 
    as: 'children', 
    foreignKey: 'parentId',
    onDelete: 'CASCADE'
  });
  
  MediaItem.belongsTo(models.MediaItem, { 
    as: 'parent', 
    foreignKey: 'parentId',
    onDelete: 'CASCADE'
  });
};

module.exports = { sequelize, MediaItem };