'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up (queryInterface, Sequelize) {
    await queryInterface.addColumn('MediaLibraries', 'status', {
      type: Sequelize.STRING,
      allowNull: false,
      defaultValue: 'ready'
    });
  },

  async down (queryInterface, Sequelize) {
    await queryInterface.removeColumn('MediaLibraries', 'status');
  }
};