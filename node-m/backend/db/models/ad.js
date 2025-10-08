'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class Ad extends Model {
    static associate(models) {
      Ad.belongsTo(models.AdGroup, {
        foreignKey: 'adGroupId',
        as: 'adGroup'
      });
    }
  }
  Ad.init({
    displayName: {
      type: DataTypes.STRING,
      allowNull: false
    },
    filename: {
      type: DataTypes.STRING,
      allowNull: false
    },
    filepath: {
      type: DataTypes.STRING,
      allowNull: false,
      unique: true
    },
    thumbnailPath: {
      type: DataTypes.STRING
    },
    duration: {
      type: DataTypes.INTEGER,
      defaultValue: 0
    },
    sortOrder: {
      type: DataTypes.INTEGER,
      defaultValue: 0
    },
    status: {
      type: DataTypes.STRING,
      defaultValue: 'processing'
    },
    adGroupId: {
      type: DataTypes.INTEGER,
      references: {
        model: 'AdGroups',
        key: 'id'
      }
    }
  }, {
    sequelize,
    modelName: 'Ad',
    tableName: 'Ads'
  });
  return Ad;
};