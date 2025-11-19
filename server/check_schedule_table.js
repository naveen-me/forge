import sequelize from './src/database.js';

async function checkScheduleTable() {
  try {
    await sequelize.authenticate();
    console.log('Connected to the database.');

    // Check the current structure of Schedule table
    const [scheduleResults] = await sequelize.query("PRAGMA table_info(Schedules)");
    const scheduleColumns = Array.isArray(scheduleResults) ? scheduleResults.map(col => col.name || col['name']) : [];
    console.log("Current Schedules table structure:", scheduleColumns);
    console.log("Number of columns in Schedules table:", scheduleColumns.length);

    // Check if 'duration' column exists
    if (!scheduleColumns.includes('duration')) {
      console.log("Adding 'duration' column to Schedules table...");
      await sequelize.query("ALTER TABLE Schedules ADD COLUMN duration INTEGER DEFAULT 0 NOT NULL");
      console.log("'duration' column added to Schedules table!");
    } else {
      console.log("'duration' column already exists in Schedules table.");
    }

    // Check if 'order' column exists
    if (!scheduleColumns.includes('order')) {
      console.log("Adding 'order' column to Schedules table...");
      await sequelize.query("ALTER TABLE Schedules ADD COLUMN 'order' INTEGER DEFAULT 0 NOT NULL");
      console.log("'order' column added to Schedules table!");
    } else {
      console.log("'order' column already exists in Schedules table.");
    }

  } catch (error) {
    console.error("Error checking Schedule table:", error);
  } finally {
    await sequelize.close();
  }
}

checkScheduleTable();