import sequelize from './database.js';
import Ad from '../models/Ad.js';
import Link from '../models/Link.js';
import MediaItem from '../models/MediaItem.js';
import Overlay from '../models/Overlay.js';
import Schedule from '../models/Schedule.js';

async function migrate() {
  const models = { Ad, Link, MediaItem, Overlay, Schedule };
  const modelNames = Object.keys(models);

  console.log('Starting database migration...');

  for (const modelName of modelNames) {
    try {
      console.log(`Syncing ${modelName}...`);
      await models[modelName].sync({ alter: true });
      console.log(`${modelName} synced successfully.`);
    } catch (error) {
      console.error(`Failed to sync ${modelName}:`, error);
      process.exit(1);
    }
  }

  console.log('Database migration complete.');
  process.exit(0);
}

migrate();
