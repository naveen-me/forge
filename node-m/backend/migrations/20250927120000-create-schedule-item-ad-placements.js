'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.createTable('ScheduleItemAdPlacements', {
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
        onDelete: 'CASCADE',
        onUpdate: 'CASCADE'
      },
      adId: {
        type: Sequelize.INTEGER,
        allowNull: false,
        references: {
          model: 'Ads',
          key: 'id'
        },
        onDelete: 'CASCADE',
        onUpdate: 'CASCADE'
      },
      name: {
        type: Sequelize.STRING,
        allowNull: true
      },
      offset: {
        type: Sequelize.INTEGER,
        allowNull: false,
        comment: 'Offset in seconds from the start of the ScheduleItem'
      },
      duration: {
        type: Sequelize.INTEGER,
        allowNull: true,
        comment: 'Duration of the ad in seconds (optional, can be 0 or null for full ad duration)'
      },
      sortOrder: {
        type: Sequelize.INTEGER,
        allowNull: false,
        defaultValue: 0
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
  down: async (queryInterface, Sequelize) => {
    await queryInterface.dropTable('ScheduleItemAdPlacements');
  }
};