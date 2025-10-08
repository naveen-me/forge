'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class AdGroup extends Model {
    static associate(models) {
      AdGroup.hasMany(models.Ad, {
        foreignKey: 'adGroupId',
        as: 'ads'
      });
    }
  }
  AdGroup.init({
    name: {
      type: DataTypes.STRING,
      allowNull: false,
      unique: true
    }
  }, {
    sequelize,
    modelName: 'AdGroup',
    tableName: 'AdGroups'
  });
  return AdGroup;
};
