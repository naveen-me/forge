import sqlite3 from 'sqlite3';
import dotenv from 'dotenv';
import { initPaymentDb, addPaymentColumnsToSubscriptions, addPaymentColumnsToUserFeatures } from './subscription_payment_schema.js';

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
    return new Promise(async (resolve, reject) => {
        db.serialize(() => {
            db.run(`CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                uType INTEGER
            )`);

            db.run(`CREATE TABLE IF NOT EXISTS plans (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                duration_days INTEGER NOT NULL
            )`);

            db.run(`CREATE TABLE IF NOT EXISTS subscriptions (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                plan_id INTEGER NOT NULL,
                end_date TEXT NOT NULL,
                status TEXT NOT NULL,
                payment_id INTEGER, -- Added for payment tracking
                FOREIGN KEY (user_id) REFERENCES users(id)
            )`);

            db.run(`CREATE TABLE IF NOT EXISTS features (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT,
                price REAL NOT NULL
            )`);

            db.run(`CREATE TABLE IF NOT EXISTS plan_features (
                id INTEGER PRIMARY KEY,
                plan_id INTEGER NOT NULL,
                feature_id INTEGER NOT NULL,
                FOREIGN KEY (plan_id) REFERENCES plans(id),
                FOREIGN KEY (feature_id) REFERENCES features(id)
            )`);

            db.run(`CREATE TABLE IF NOT EXISTS user_features (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                feature_id INTEGER NOT NULL,
                purchase_date TEXT NOT NULL,
                payment_id INTEGER, -- Added for payment tracking
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (feature_id) REFERENCES features(id)
            )`);

            db.run(`CREATE TABLE IF NOT EXISTS stats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                value INTEGER NOT NULL
            )`, (err) => {
                if (err) return reject(err);
                
                // Seed initial data after creating tables
                const seedData = async () => {
                    // Seed initial plans
                    db.get("SELECT COUNT(*) as count FROM plans", (err, row) => {
                        if (err) {
                            console.error('Error checking plans table:', err);
                            return resolve();
                        }
                        if (row.count === 0) {
                            db.run("INSERT INTO plans (name, price, duration_days) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)",
                                ['Basic', 9.99, 30, 'Pro', 19.99, 30, 'Premium', 29.99, 30]);
                        }
                        
                        // Seed initial features
                        db.get("SELECT COUNT(*) as count FROM features", (err, row) => {
                            if (err) {
                                console.error('Error checking features table:', err);
                                return resolve();
                            }
                            if (row.count === 0) {
                                db.run("INSERT INTO features (name, description, price) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)",
                                    ['Ad-Free Experience', 'Enjoy the app without any advertisements', 4.99,
                                     'Priority Support', 'Get faster support responses from our team', 2.99,
                                     'Advanced Analytics', 'Access to premium analytics and reporting features', 9.99]);
                            }
                            
                            // Seed initial plan_features relationships
                            db.get("SELECT COUNT(*) as count FROM plan_features", (err, row) => {
                                if (err) {
                                    console.error('Error checking plan_features table:', err);
                                    return resolve();
                                }
                                if (row.count === 0) {
                                    // Basic plan gets Ad-Free Experience
                                    db.run("INSERT INTO plan_features (plan_id, feature_id) VALUES (?, ?)", [1, 1]);
                                    // Pro plan gets Ad-Free and Priority Support
                                    db.run("INSERT INTO plan_features (plan_id, feature_id) VALUES (?, ?), (?, ?)", [2, 1], [2, 2]);
                                    // Premium plan gets all features
                                    db.run("INSERT INTO plan_features (plan_id, feature_id) VALUES (?, ?), (?, ?), (?, ?)", [3, 1], [3, 2], [3, 3]);
                                }
                                
                                // Seed stats table if empty
                                db.get("SELECT COUNT(*) as count FROM stats", (err, row) => {
                                    if (row.count === 0) {
                                        db.run("INSERT INTO stats (name, value) VALUES (?, ?), (?, ?)", ['Videos Played', 0, 'Errors', 0]);
                                    }
                                    
                                    // Initialize payment-related tables after seeding
                                    initPaymentDb()
                                        .then(() => resolve())
                                        .catch(err => reject(err));
                                });
                            });
                        });
                    });
                };
                
                seedData();
            });
        });
    });
};

export default db;
