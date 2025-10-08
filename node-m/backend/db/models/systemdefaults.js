'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class SystemDefaults extends Model {
    static associate(models) {
      // define association here
    }
  }
  SystemDefaults.init({
    key: {
      type: DataTypes.STRING,
      primaryKey: true,
      allowNull: false,
    },
    value: {
      type: DataTypes.TEXT,
      allowNull: true,
    },
  }, {
    sequelize,
    modelName: 'SystemDefaults',
  });
  return SystemDefaults;
};
