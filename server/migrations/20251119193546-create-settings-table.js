/** @type {import('sequelize-cli').Migration} */
export default {
  async up (queryInterface, Sequelize) {
    /**
     * Add altering commands here.
     */
    // Get current table info to check if table exists
    const tableExists = await queryInterface.tableExists('Settings');

    if (!tableExists) {
      await queryInterface.createTable('Settings', {
        id: {
          type: Sequelize.INTEGER,
          primaryKey: true,
          autoIncrement: true,
        },
        user_id: {
          type: Sequelize.INTEGER,
          allowNull: false
          // No foreign key constraint to avoid issues with existing database
        },
        timezone: {
          type: Sequelize.STRING,
          defaultValue: 'UTC',
        },
        obs_host: {
          type: Sequelize.STRING,
          defaultValue: 'localhost',
        },
        obs_port: {
          type: Sequelize.INTEGER,
          defaultValue: 4455,
        },
        obs_password: {
          type: Sequelize.STRING,
        },
        scene_name: {
          type: Sequelize.STRING,
          defaultValue: 'Media Playout',
        },
        auto_start_stream: {
          type: Sequelize.BOOLEAN,
          defaultValue: false,
        },
        theme: {
          type: Sequelize.STRING,
          defaultValue: 'light',
        },
        created_at: {
          allowNull: false,
          type: Sequelize.DATE,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
        },
        updated_at: {
          allowNull: false,
          type: Sequelize.DATE,
          defaultValue: Sequelize.literal('CURRENT_TIMESTAMP'),
        }
      });
    }
  },

  async down (queryInterface, Sequelize) {
    /**
     * Add reverting commands here.
     */
    const tableExists = await queryInterface.tableExists('Settings');
    if (tableExists) {
      await queryInterface.dropTable('Settings');
    }
  }
};
