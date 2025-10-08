'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up (queryInterface, Sequelize) {
    // This migration was manually applied through a script
    // The script successfully added all required columns to the Overlays table
    // This is just a placeholder to mark the migration as completed
    console.log('Overlay columns fix migration already applied manually');
  },

  async down (queryInterface, Sequelize) {
    // This is a safety migration, so we won't implement a down method
    // as we don't want to accidentally remove columns that should exist
  }
};
