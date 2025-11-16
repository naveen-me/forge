import { Model, DataTypes } from 'sequelize';
import sequelize from '../src/database.js';

class Link extends Model {}

Link.init({
  id: {
    type: DataTypes.INTEGER,
    autoIncrement: true,
    primaryKey: true,
  },
  name: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  url: {
    type: DataTypes.STRING,
    allowNull: false,
  },
}, {
  sequelize,
  modelName: 'Link',
});

export default Link;
