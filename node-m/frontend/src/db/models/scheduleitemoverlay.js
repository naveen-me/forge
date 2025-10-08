'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class ScheduleItemOverlay extends Model {
    static associate(models) {
      ScheduleItemOverlay.belongsTo(models.ScheduleItem, {
        foreignKey: 'scheduleItemId',
        as: 'scheduleItem',
      });
      ScheduleItemOverlay.belongsTo(models.Overlay, {
        foreignKey: 'overlayId',
        as: 'overlay',
      });
    }
  }
  ScheduleItemOverlay.init({
    scheduleItemId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    overlayId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    startTime: {
      type: DataTypes.INTEGER,
      allowNull: false,
      comment: 'Offset in seconds from the start of the ScheduleItem'
    },
    duration: {
      type: DataTypes.INTEGER,
      allowNull: false,
      comment: 'How long the overlay should be visible in seconds'
    },
  }, {
    sequelize,
    modelName: 'ScheduleItemOverlay',
    tableName: 'ScheduleItemOverlays',
  });
  return ScheduleItemOverlay;
};