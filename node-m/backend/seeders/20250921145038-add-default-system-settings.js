'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up (queryInterface, Sequelize) {
    await queryInterface.bulkInsert('SystemDefaults', [
      {
        key: 'canvasWidth',
        value: '1920',
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        key: 'canvasHeight',
        value: '1080',
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ], {});
  },

  async down (queryInterface, Sequelize) {
    await queryInterface.bulkDelete('SystemDefaults', {
      key: ['canvasWidth', 'canvasHeight']
    }, {});
  }
};
