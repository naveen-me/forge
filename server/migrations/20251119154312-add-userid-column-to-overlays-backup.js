/** @type {import('sequelize-cli').Migration} */
export default {
  async up (queryInterface, Sequelize) {
    /**
     * Add altering commands here.
     */
    // Get current table info to check if column exists
    const tableDescription = await queryInterface.describeTable('Overlays_backup');

    // Add column only if it doesn't exist
    if (!tableDescription.userId) {
      await queryInterface.addColumn('Overlays_backup', 'userId', {
        type: Sequelize.INTEGER,
        defaultValue: 0,
        allowNull: true
      });
    }
  },

  async down (queryInterface, Sequelize) {
    /**
     * Add reverting commands here.
     */
    const tableDescription = await queryInterface.describeTable('Overlays_backup');
    if (tableDescription.userId) {
      await queryInterface.removeColumn('Overlays_backup', 'userId');
    }
  }
};
