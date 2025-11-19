/** @type {import('sequelize-cli').Migration} */
export default {
  async up (queryInterface, Sequelize) {
    /**
     * Add altering commands here.
     */
    // Get current table info to check if column exists
    const tableDescription = await queryInterface.describeTable('Schedules');

    // Add column only if it doesn't exist
    if (!tableDescription.offset_time) {
      await queryInterface.addColumn('Schedules', 'offset_time', {
        type: Sequelize.INTEGER,
        defaultValue: 0, // Default to 0 seconds (no offset)
        allowNull: true
      });
    }
  },

  async down (queryInterface, Sequelize) {
    /**
     * Add reverting commands here.
     */
    const tableDescription = await queryInterface.describeTable('Schedules');
    if (tableDescription.offset_time) {
      await queryInterface.removeColumn('Schedules', 'offset_time');
    }
  }
};
