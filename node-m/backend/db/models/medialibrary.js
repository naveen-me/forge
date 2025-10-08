'use strict';
const {
  Model
} = require('sequelize');
module.exports = (sequelize, DataTypes) => {
  class MediaLibrary extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // Define association with Folder
      MediaLibrary.belongsTo(models.Folder, {
        foreignKey: 'folderId',
        as: 'folder'
      });
    }
  }
  MediaLibrary.init({
    filename: DataTypes.STRING,
    displayName: DataTypes.STRING,
    filepath: DataTypes.STRING,
    type: DataTypes.STRING,
    duration: DataTypes.INTEGER,
    metadata: DataTypes.TEXT,
    thumbnailPath: DataTypes.STRING,
    folderId: DataTypes.INTEGER,
    status: {
      type: DataTypes.STRING,
      defaultValue: 'ready' // processing, ready, error
    }
  }, {
    sequelize,
    modelName: 'MediaLibrary',
  });
  return MediaLibrary;
};