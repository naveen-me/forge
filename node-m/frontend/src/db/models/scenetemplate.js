'use strict';
const {
  Model
} = require('sequelize');
module.exports = (sequelize, DataTypes) => {
  class SceneTemplate extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // define association here
    }
  }
  SceneTemplate.init({
    name: DataTypes.STRING,
    layout: DataTypes.TEXT,
    defaultSources: DataTypes.TEXT
  }, {
    sequelize,
    modelName: 'SceneTemplate',
  });
  return SceneTemplate;
};