import { Model, DataTypes } from 'sequelize';
import sequelize from '../src/database.js';

class Schedule extends Model {}

Schedule.init({
  id: {
    type: DataTypes.INTEGER,
    autoIncrement: true,
    primaryKey: true,
  },
  channel_id: {
    type: DataTypes.INTEGER,
    allowNull: false,
  },
  item_id: {
    type: DataTypes.INTEGER,
    allowNull: false,
  },
  item_type: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  start_time: {
    type: DataTypes.DATE,
    allowNull: false,
  },
      end_time: {
        type: DataTypes.DATE,
        allowNull: false,
      },
      duration: {
        type: DataTypes.INTEGER, // Duration in seconds
        allowNull: false,
      },
      offset_time: {
        type: DataTypes.INTEGER, // Video start offset in seconds, default 0
        defaultValue: 0,
        allowNull: true,
      },
      order: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },}, {
  sequelize,
  modelName: 'Schedule',
});

export default Schedule;
