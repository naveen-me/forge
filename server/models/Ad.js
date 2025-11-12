const { DataTypes } = require('sequelize');
const sequelize = require('../src/database');

const Ad = sequelize.define('Ad', {
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
    type: DataTypes.ENUM('file', 'group'),
    allowNull: false,
    defaultValue: 'file'
  },
  filePath: {
    type: DataTypes.STRING,
    allowNull: true
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
    type: DataTypes.STRING,
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
      model: 'Ads',
      key: 'id'
    }
  },
  order: {
    type: DataTypes.INTEGER,
    defaultValue: 0,
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
  tableName: 'Ads',
  timestamps: true
});

Ad.associate = (models) => {
  Ad.hasMany(models.Ad, {
    as: 'children',
    foreignKey: 'parentId',
    onDelete: 'CASCADE'
  });

  Ad.belongsTo(models.Ad, {
    as: 'parent',
    foreignKey: 'parentId',
    onDelete: 'CASCADE'
  });
};

module.exports = { sequelize, Ad };