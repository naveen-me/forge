'use strict';
/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('ScheduleItemOverlays', {
      id: {
        allowNull: false,
        autoIncrement: true,
        primaryKey: true,
        type: Sequelize.INTEGER
      },
      scheduleItemId: {
        type: Sequelize.INTEGER,
        allowNull: false,
        references: {
          model: 'ScheduleItems',
          key: 'id'
        },
        onUpdate: 'CASCADE',
        onDelete: 'CASCADE'
      },
      overlayId: {
        type: Sequelize.INTEGER,
        allowNull: false,
        references: {
          model: 'Overlays', // Assuming an 'Overlays' table exists
          key: 'id'
        },
        onUpdate: 'CASCADE',
        onDelete: 'CASCADE'
      },
      startTime: {
        type: Sequelize.INTEGER,
        allowNull: false,
        comment: 'Offset in seconds from the start of the ScheduleItem'
      },
      duration: {
        type: Sequelize.INTEGER,
        allowNull: false,
        comment: 'How long the overlay should be visible in seconds'
      },
      createdAt: {
        allowNull: false,
        type: Sequelize.DATE
      },
      updatedAt: {
        allowNull: false,
        type: Sequelize.DATE
      }
    });
  },
  async down(queryInterface, Sequelize) {
    await queryInterface.dropTable('ScheduleItemOverlays');
  }
};