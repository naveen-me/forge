import sqlite3 from 'sqlite3';
import dotenv from 'dotenv';

dotenv.config();

const dbFile = process.env.DB_FILE || 'playout.db';
const db = new sqlite3.Database(dbFile, (err) => {
    if (err) {
        console.error('Error opening database', err.message);
    } else {
        console.log('Connected to the SQLite database.');
    }
});

export const initDb = () => {
    return new Promise((resolve, reject) => {
        db.serialize(() => {
            db.run(`CREATE TABLE IF NOT EXISTS stats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                value INTEGER NOT NULL
            )`, (err) => {
                if (err) return reject(err);
                // Seed data if needed
                db.get("SELECT COUNT(*) as count FROM stats", (err, row) => {
                    if (row.count === 0) {
                        db.run("INSERT INTO stats (name, value) VALUES (?, ?), (?, ?)", ['Videos Played', 0, 'Errors', 0]);
                    }
                    resolve();
                });
            });
        });
    });
};

export default db;
