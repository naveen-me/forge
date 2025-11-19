/** @type {import('sequelize-cli').Migration} */
export default {
  async up (queryInterface, Sequelize) {
    /**
     * Remove the foreign key constraint from the user_id column by recreating the table
     */
    // First, check if the Settings table exists and get its description
    const tableExists = await queryInterface.tableExists('Settings');
    if (!tableExists) {
      console.log('Settings table does not exist');
      return;
    }

    // Rename the current table
    await queryInterface.renameTable('Settings', 'Settings_old');

    // Create new Settings table without foreign key constraint
    await queryInterface.createTable('Settings', {
      id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
      },
      user_id: {
        type: Sequelize.INTEGER,
        allowNull: false
        // No foreign key constraint to avoid the 'no such table: main.Users' error
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

    // Copy data from old table to new table
    await queryInterface.sequelize.query(`
      INSERT INTO Settings (id, user_id, timezone, obs_host, obs_port, obs_password, scene_name, auto_start_stream, theme, created_at, updated_at)
      SELECT id, user_id, timezone, obs_host, obs_port, obs_password, scene_name, auto_start_stream, theme, created_at, updated_at
      FROM Settings_old
    `);

    // Drop the old table
    await queryInterface.dropTable('Settings_old');
  },

  async down (queryInterface, Sequelize) {
    /**
     * Revert the changes - recreate the table with foreign key constraint
     */
    // Rename the current table
    await queryInterface.renameTable('Settings', 'Settings_new');

    // Recreate table with potential foreign key constraint
    await queryInterface.createTable('Settings', {
      id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true,
      },
      user_id: {
        type: Sequelize.INTEGER,
        allowNull: false,
        references: {
          model: 'Users',
          key: 'id',
        },
        // Note: In SQLite, foreign key constraints are not enforced by default
        // so we might need to be careful about this
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

    // Copy data back
    await queryInterface.sequelize.query(`
      INSERT INTO Settings (id, user_id, timezone, obs_host, obs_port, obs_password, scene_name, auto_start_stream, theme, created_at, updated_at)
      SELECT id, user_id, timezone, obs_host, obs_port, obs_password, scene_name, auto_start_stream, theme, created_at, updated_at
      FROM Settings_new
    `);

    // Drop the new table (old renamed)
    await queryInterface.dropTable('Settings_new');
  }
};
