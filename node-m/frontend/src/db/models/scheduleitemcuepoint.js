'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class ScheduleItemCuePoint extends Model {
    static associate(models) {
      ScheduleItemCuePoint.belongsTo(models.ScheduleItem, {
        foreignKey: 'scheduleItemId',
        as: 'scheduleItem',
      });
      ScheduleItemCuePoint.belongsTo(models.Ad, {
        foreignKey: 'adId',
        as: 'ad',
      });
    }
  }
  ScheduleItemCuePoint.init({
    scheduleItemId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    adId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    offsetTime: {
      type: DataTypes.INTEGER,
      allowNull: false,
      comment: 'Offset in seconds from the start of the media item'
    },
  }, {
    sequelize,
    modelName: 'ScheduleItemCuePoint',
    tableName: 'ScheduleItemCuePoints',
  });
  return ScheduleItemCuePoint;
};