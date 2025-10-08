'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class ScheduleItemAdPlacement extends Model {
    static associate(models) {
      ScheduleItemAdPlacement.belongsTo(models.ScheduleItem, {
        foreignKey: 'scheduleItemId',
        as: 'scheduleItem',
      });
      ScheduleItemAdPlacement.belongsTo(models.Ad, {
        foreignKey: 'adId',
        as: 'ad',
      });
    }
  }
  ScheduleItemAdPlacement.init({
    scheduleItemId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    adId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    name: {
      type: DataTypes.STRING,
      allowNull: true,
    },
    offset: {
      type: DataTypes.INTEGER, // Offset in seconds from the start of the schedule item
      allowNull: false,
    },
    duration: {
      type: DataTypes.INTEGER, // Duration of the ad (optional, can be null for full ad duration)
      allowNull: true,
    },
    sortOrder: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 0,
    },
  }, {
    sequelize,
    modelName: 'ScheduleItemAdPlacement',
    tableName: 'ScheduleItemAdPlacements',
  });
  return ScheduleItemAdPlacement;
};