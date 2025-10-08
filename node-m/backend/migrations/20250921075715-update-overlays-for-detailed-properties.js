'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up (queryInterface, Sequelize) {
    // Remove the old generic columns
    await queryInterface.removeColumn('Overlays', 'position');
    await queryInterface.removeColumn('Overlays', 'size');

    // Add the new specific columns
    await queryInterface.addColumn('Overlays', 'x', {
      type: Sequelize.INTEGER,
      defaultValue: 0,
      allowNull: false,
    });
    await queryInterface.addColumn('Overlays', 'y', {
      type: Sequelize.INTEGER,
      defaultValue: 0,
      allowNull: false,
    });
    await queryInterface.addColumn('Overlays', 'width', {
      type: Sequelize.INTEGER,
      defaultValue: 400,
      allowNull: false,
    });
    await queryInterface.addColumn('Overlays', 'height', {
      type: Sequelize.INTEGER,
      defaultValue: 300,
      allowNull: false,
    });
    await queryInterface.addColumn('Overlays', 'fit', {
      type: Sequelize.STRING,
      defaultValue: 'fit',
      allowNull: false,
    });
    await queryInterface.addColumn('Overlays', 'groupId', {
      type: Sequelize.INTEGER,
      allowNull: true,
      references: {
        model: 'Overlays', // This is a self-reference for grouping
        key: 'id'
      },
      onUpdate: 'CASCADE',
      onDelete: 'SET NULL'
    });
  },

  async down (queryInterface, Sequelize) {
    // Add the old columns back
    await queryInterface.addColumn('Overlays', 'position', {
      type: Sequelize.STRING,
    });
    await queryInterface.addColumn('Overlays', 'size', {
      type: Sequelize.STRING,
    });

    // Remove the new columns
    await queryInterface.removeColumn('Overlays', 'x');
    await queryInterface.removeColumn('Overlays', 'y');
    await queryInterface.removeColumn('Overlays', 'width');
    await queryInterface.removeColumn('Overlays', 'height');
    await queryInterface.removeColumn('Overlays', 'fit');
    await queryInterface.removeColumn('Overlays', 'groupId');
  }
};
