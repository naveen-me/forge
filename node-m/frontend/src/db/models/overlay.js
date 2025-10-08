'use strict';
const {
  Model
} = require('sequelize');
module.exports = (sequelize, DataTypes) => {
  class Overlay extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // define association here
      Overlay.belongsTo(models.Overlay, { as: 'Group', foreignKey: 'groupId', targetKey: 'id' });
      Overlay.hasMany(models.Overlay, { as: 'Members', foreignKey: 'groupId', sourceKey: 'id' });
    }
  }
  Overlay.init({
    name: DataTypes.STRING,
    type: DataTypes.STRING, // 'image', 'video'
    content: DataTypes.TEXT, // This will be the full path to the media file
    active: DataTypes.BOOLEAN,
    x: {
      type: DataTypes.INTEGER,
      defaultValue: 0,
      allowNull: false,
    },
    y: {
      type: DataTypes.INTEGER,
      defaultValue: 0,
      allowNull: false,
    },
    width: {
      type: DataTypes.INTEGER,
      defaultValue: 400,
      allowNull: false,
    },
    height: {
      type: DataTypes.INTEGER,
      defaultValue: 300,
      allowNull: false,
    },
    fit: {
      type: DataTypes.STRING,
      defaultValue: 'fit', // 'fit', 'cover', 'shrink'
      allowNull: false,
    },
    groupId: {
      type: DataTypes.INTEGER,
      allowNull: true,
    }
  }, {
    sequelize,
    modelName: 'Overlay',
  });
  return Overlay;
};