import sequelize from './src/database.js';
import { MediaItem } from './models/MediaItem.js';

async function fixDatabase() {
  try {
    // First, authenticate the connection
    await sequelize.authenticate();
    console.log('Connected to the database.');

    // Check if 'order' column exists in MediaItems table by querying table info
    // We'll use raw SQL to check the table structure
    const [results] = await sequelize.query("PRAGMA table_info(MediaItems)");
    // With sequelize, the PRAGMA result might be in a different format
    // Let's inspect it first and then check for 'order' column
    console.log("MediaItems table structure:", Array.isArray(results) ? results.map(col => col.name || col['name']) : results);

    const hasOrderColumn = Array.isArray(results) && results.some(col => {
      // Different possible property names for column name depending on SQLite version
      return (col.name || col['name'] || col['cn']) === 'order';
    });

    if (!hasOrderColumn) {
      console.log("Adding 'order' column to MediaItems table...");
      await sequelize.query("ALTER TABLE MediaItems ADD COLUMN 'order' INTEGER DEFAULT 0");
      console.log("'order' column added successfully!");
    } else {
      console.log("'order' column already exists in MediaItems table.");
    }

    // Check the current structure of Overlays_backup
    try {
      const [overlaysBackupResults] = await sequelize.query("PRAGMA table_info(Overlays_backup)");
      const backupColumns = Array.isArray(overlaysBackupResults) ? overlaysBackupResults.map(col => col.name || col['name']) : [];
      console.log("Current Overlays_backup structure:", backupColumns);
    } catch (e) {
      console.log("Overlays_backup table doesn't exist or has an issue:", e.message);
    }

    // Check the current structure of Overlays table for comparison
    try {
      const [overlaysResults] = await sequelize.query("PRAGMA table_info(Overlays)");
      const overlayColumns = Array.isArray(overlaysResults) ? overlaysResults.map(col => col.name || col['name']) : [];
      console.log("Current Overlays structure:", overlayColumns);
      console.log("Number of columns in Overlays table:", overlayColumns.length);

      // Check if the Overlays_backup table is missing any columns from the Overlays table
      const [overlaysBackupResults] = await sequelize.query("PRAGMA table_info(Overlays_backup)");
      const backupColumns = Array.isArray(overlaysBackupResults) ? overlaysBackupResults.map(col => col.name || col['name']) : [];

      const missingColumns = overlayColumns.filter(col => !backupColumns.includes(col));
      if (missingColumns.length > 0) {
        console.log("Overlays_backup table is missing these columns from Overlays table:", missingColumns);
        for (const col of missingColumns) {
          if (col === 'userId') {
            console.log("Adding 'userId' column to Overlays_backup table...");
            await sequelize.query("ALTER TABLE Overlays_backup ADD COLUMN userId INTEGER DEFAULT 0");
            console.log("'userId' column added to Overlays_backup table!");
          }
          // Add handling for other missing columns if needed
        }
      } else {
        console.log("Overlays_backup table has all the columns from Overlays table.");
      }
    } catch (e) {
      console.log("Overlays table structure check failed:", e.message);
    }

    console.log("Database fixes completed!");
  } catch (error) {
    console.error("Error fixing database:", error);
  } finally {
    await sequelize.close();
  }
}

fixDatabase();