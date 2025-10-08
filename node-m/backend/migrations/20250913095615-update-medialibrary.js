'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up (queryInterface, Sequelize) {
    // Add new columns to MediaLibrary table
    await queryInterface.addColumn('MediaLibraries', 'displayName', {
      type: Sequelize.STRING,
      allowNull: true
    });

    await queryInterface.addColumn('MediaLibraries', 'filepath', {
      type: Sequelize.STRING,
      allowNull: true
    });

    await queryInterface.addColumn('MediaLibraries', 'thumbnailPath', {
      type: Sequelize.STRING,
      allowNull: true
    });

    await queryInterface.addColumn('MediaLibraries', 'folderId', {
      type: Sequelize.INTEGER,
      allowNull: true,
      references: {
        model: 'Folders',
        key: 'id'
      }
    });

    // Rename path column to be more descriptive if it exists
    // Note: We'll have to handle this carefully based on existing data
  },

  async down (queryInterface, Sequelize) {
    // Remove added columns
    await queryInterface.removeColumn('MediaLibraries', 'displayName');
    await queryInterface.removeColumn('MediaLibraries', 'filepath');
    await queryInterface.removeColumn('MediaLibraries', 'thumbnailPath');
    await queryInterface.removeColumn('MediaLibraries', 'folderId');
  }
};
