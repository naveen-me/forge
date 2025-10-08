'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class ScheduleItem extends Model {
    static associate(models) {
      ScheduleItem.belongsTo(models.Schedule, {
        foreignKey: 'scheduleId',
        as: 'schedule',
      });
      ScheduleItem.hasMany(models.ScheduleItemOverlay, {
        foreignKey: 'scheduleItemId',
        as: 'overlays',
      });
      ScheduleItem.hasMany(models.ScheduleItemCuePoint, {
        foreignKey: 'scheduleItemId',
        as: 'cuePoints',
      });
      ScheduleItem.hasMany(models.ScheduleItemAdPlacement, {
        foreignKey: 'scheduleItemId',
        as: 'adPlacements',
      });
      // A polymorphic association can be used here if MediaLibrary and Ads are separate models
      // For now, we'll handle the relation in the service layer based on itemType
    }
  }
  ScheduleItem.init({
    scheduleId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    itemId: {
      type: DataTypes.INTEGER,
      allowNull: true,
    },
    itemType: {
      type: DataTypes.ENUM('media', 'ad', 'gap'),
      allowNull: false,
    },
    startTime: {
      type: DataTypes.TIME,
      allowNull: false,
    },
    duration: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    sortOrder: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    displayName: {
      type: DataTypes.STRING,
      allowNull: true,
    },
    repeat: {
      type: DataTypes.JSON,
    },
    notes: {
      type: DataTypes.TEXT,
    },
  }, {
    sequelize,
    modelName: 'ScheduleItem',
    tableName: 'ScheduleItems',
  });
  return ScheduleItem;
};