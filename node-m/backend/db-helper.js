const { Sequelize } = require('sequelize');
const db = require('./db');

// A wrapper to handle database errors, especially those related to missing tables or columns.
// It attempts a query, and if it fails with a database-related error,
// it syncs the database schema and retries the query once.
async function robustQuery(query, ...args) {
  try {
    // Attempt the initial query
    return await query(...args);
  } catch (error) {
    // Check if the error is a Sequelize database error (e.g., table not found, column not found)
    if (error instanceof db.Sequelize.DatabaseError) {
      console.warn('Database error detected, attempting to sync schema and retry.', error.message);

      // Sync the database schema. 'alter: true' will try to update the schema without dropping data.
      await db.sequelize.sync({ alter: true });

      // Retry the query after syncing
      console.log('Retrying query after schema sync.');
      return await query(...args);
    }

    // If it's another type of error, rethrow it
    throw error;
  }
}

module.exports = {
  robustQuery,
};