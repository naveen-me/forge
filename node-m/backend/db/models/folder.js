'use strict';
const {
  Model
} = require('sequelize');
module.exports = (sequelize, DataTypes) => {
  class Folder extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // Define self-referencing association for parent folder
      Folder.belongsTo(Folder, {
        as: 'parent',
        foreignKey: 'parentId'
      });
      
      // Define association with MediaLibrary items
      Folder.hasMany(models.MediaLibrary, {
        foreignKey: 'folderId',
        as: 'mediaItems'
      });
    }
  }
  Folder.init({
    name: DataTypes.STRING,
    parentId: DataTypes.INTEGER
  }, {
    sequelize,
    modelName: 'Folder',
  });
  return Folder;
};