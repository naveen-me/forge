'use strict';
/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('ScheduleItemCuePoints', {
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
      adId: {
        type: Sequelize.INTEGER,
        allowNull: false,
        references: {
          model: 'Ads', // Assuming an 'Ads' table exists
          key: 'id'
        },
        onUpdate: 'CASCADE',
        onDelete: 'CASCADE'
      },
      offsetTime: {
        type: Sequelize.INTEGER,
        allowNull: false,
        comment: 'Offset in seconds from the start of the media item'
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
    await queryInterface.dropTable('ScheduleItemCuePoints');
  }
};