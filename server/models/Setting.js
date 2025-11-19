import { Model, DataTypes } from 'sequelize';
import sequelize from '../src/database.js';

class Setting extends Model {}

Setting.init({
  id: {
    type: DataTypes.INTEGER,
    autoIncrement: true,
    primaryKey: true,
  },
  userId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    field: 'user_id'
    // Note: No foreign key constraint to avoid issues with existing database
  },
  timezone: {
    type: DataTypes.STRING,
    defaultValue: 'UTC',
    allowNull: false,
    validate: {
      isTimeZone(value) {
        if (value) {
          // Check if it's a valid IANA timezone identifier
          const validIANATimezones = Intl.supportedValuesOf('timeZone');

          // Common timezone abbreviations (these are accepted but will be mapped to IANA identifiers in practice)
          const validAbbreviations = ['UTC', 'GMT', 'IST', 'EST', 'EDT', 'CST', 'CDT', 'MST', 'MDT', 'PST', 'PDT'];

          if (!validIANATimezones.includes(value) && !validAbbreviations.includes(value)) {
            throw new Error('Invalid timezone');
          }
        }
      }
    }
  },
  obsHost: {
    type: DataTypes.STRING,
    field: 'obs_host',
    defaultValue: 'localhost',
  },
  obsPort: {
    type: DataTypes.INTEGER,
    field: 'obs_port',
    defaultValue: 4455,
  },
  obsPassword: {
    type: DataTypes.STRING,
    field: 'obs_password',
  },
  sceneName: {
    type: DataTypes.STRING,
    field: 'scene_name',
    defaultValue: 'Media Playout',
  },
  autoStartStream: {
    type: DataTypes.BOOLEAN,
    field: 'auto_start_stream',
    defaultValue: false,
  },
  theme: {
    type: DataTypes.STRING,
    defaultValue: 'light',
  },
  createdAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    field: 'created_at'
  },
  updatedAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW,
    field: 'updated_at'
  }
}, {
  sequelize,
  modelName: 'Setting',
  tableName: 'Settings',
  timestamps: true,
});

export default Setting;