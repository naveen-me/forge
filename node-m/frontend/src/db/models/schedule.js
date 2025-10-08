'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class Schedule extends Model {
    static associate(models) {
      Schedule.hasMany(models.ScheduleItem, {
        foreignKey: 'scheduleId',
        as: 'items',
      });
    }
  }
  Schedule.init({
    name: {
      type: DataTypes.STRING,
      allowNull: false,
    },
    date: {
      type: DataTypes.DATEONLY,
      allowNull: false,
    },
    status: {
      type: DataTypes.ENUM('draft', 'published'),
      defaultValue: 'draft',
      allowNull: false,
    },
    repeat: {
      type: DataTypes.JSON,
    },
  }, {
    sequelize,
    modelName: 'Schedule',
    tableName: 'Schedules',
  });
  return Schedule;
};