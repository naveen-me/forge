/** @type {import('sequelize-cli').Migration} */
export default {
  async up (queryInterface, Sequelize) {
    /**
     * Add altering commands here.
     */
    // Get current table info to check if column exists
    const tableDescription = await queryInterface.describeTable('MediaItems');

    // Add column only if it doesn't exist
    if (!tableDescription.order) {
      await queryInterface.addColumn('MediaItems', 'order', {
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
    const tableDescription = await queryInterface.describeTable('MediaItems');
    if (tableDescription.order) {
      await queryInterface.removeColumn('MediaItems', 'order');
    }
  }
};
