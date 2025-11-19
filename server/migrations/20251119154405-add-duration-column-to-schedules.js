/** @type {import('sequelize-cli').Migration} */
export default {
  async up (queryInterface, Sequelize) {
    /**
     * Add altering commands here.
     */
    // Get current table info to check if column exists
    const tableDescription = await queryInterface.describeTable('Schedules');

    // Add column only if it doesn't exist
    if (!tableDescription.duration) {
      await queryInterface.addColumn('Schedules', 'duration', {
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
    const tableDescription = await queryInterface.describeTable('Schedules');
    if (tableDescription.duration) {
      await queryInterface.removeColumn('Schedules', 'duration');
    }
  }
};
