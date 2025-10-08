'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up (queryInterface, Sequelize) {
    // Rename path column to filepath
    await queryInterface.renameColumn('MediaLibraries', 'path', 'filepath');
  },

  async down (queryInterface, Sequelize) {
    // Rename filepath column back to path
    await queryInterface.renameColumn('MediaLibraries', 'filepath', 'path');
  }
};
