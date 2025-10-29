import db from './database.js';

/**
 * Initialize payment-related tables for subscription and feature purchases
 */
export const initPaymentDb = () => {
    return new Promise((resolve, reject) => {
        db.serialize(() => {
            // Create payment_methods table to store UPI details
            db.run(`CREATE TABLE IF NOT EXISTS payment_methods (
                id INTEGER PRIMARY KEY,
                user_id INTEGER,
                upi_id TEXT NOT NULL,
                upi_vpa TEXT,
                is_default BOOLEAN DEFAULT 0,
                is_active BOOLEAN DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )`, (err) => {
                if (err) {
                    console.error('Error creating payment_methods table:', err);
                    return reject(err);
                }

                // Create payments table to store all payment transactions
                db.run(`CREATE TABLE IF NOT EXISTS payments (
                    id INTEGER PRIMARY KEY,
                    user_id INTEGER NOT NULL,
                    payment_type TEXT NOT NULL CHECK(payment_type IN ('subscription', 'feature')), -- subscription or feature
                    item_id INTEGER NOT NULL, -- plan_id or feature_id
                    amount REAL NOT NULL,
                    currency TEXT DEFAULT 'INR',
                    payment_method TEXT NOT NULL,
                    transaction_id TEXT UNIQUE, -- UPI transaction ID
                    upi_qr_data TEXT, -- QR code data for payment
                    status TEXT NOT NULL DEFAULT 'pending' CHECK(status IN ('pending', 'verifying', 'paid', 'failed', 'cancelled', 'refunded')),
                    payment_date TEXT,
                    verification_date TEXT,
                    expires_at TEXT, -- When the payment request expires
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )`, (err) => {
                    if (err) {
                        console.error('Error creating payments table:', err);
                        return reject(err);
                    }

                    // Create payment_verification table to track payment verifications
                    db.run(`CREATE TABLE IF NOT EXISTS payment_verification (
                        id INTEGER PRIMARY KEY,
                        payment_id INTEGER NOT NULL,
                        verification_status TEXT NOT NULL,
                        verification_response TEXT,
                        verified_by TEXT, -- manual or system
                        verified_at TEXT,
                        notes TEXT,
                        FOREIGN KEY (payment_id) REFERENCES payments(id)
                    )`, (err) => {
                        if (err) {
                            console.error('Error creating payment_verification table:', err);
                            return reject(err);
                        }

                        // Create payment_logs table to track payment process
                        db.run(`CREATE TABLE IF NOT EXISTS payment_logs (
                            id INTEGER PRIMARY KEY,
                            payment_id INTEGER NOT NULL,
                            action TEXT NOT NULL,
                            details TEXT,
                            timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (payment_id) REFERENCES payments(id)
                        )`, (err) => {
                            if (err) {
                                console.error('Error creating payment_logs table:', err);
                                return reject(err);
                            }

                            // Create default UPI details table for system-wide UPI ID
                            db.run(`CREATE TABLE IF NOT EXISTS system_upi_details (
                                id INTEGER PRIMARY KEY,
                                upi_id TEXT UNIQUE NOT NULL,
                                upi_vpa TEXT NOT NULL,
                                display_name TEXT,
                                is_primary BOOLEAN DEFAULT 0,
                                is_active BOOLEAN DEFAULT 1,
                                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                                updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                            )`, (err) => {
                                if (err) {
                                    console.error('Error creating system_upi_details table:', err);
                                    return reject(err);
                                }

                                // Insert default system UPI details if not exists
                                // Check if any records exist first to properly set primary
                                db.get("SELECT COUNT(*) as count FROM system_upi_details", (err, row) => {
                                    if (err) {
                                        console.error('Error checking UPI details:', err);
                                        return reject(err);
                                    }
                                    
                                    if (row.count === 0) {
                                        // Insert default UPI details as primary
                                        db.run(`INSERT INTO system_upi_details (upi_id, upi_vpa, display_name, is_primary) 
                                                VALUES ('matrixapi', 'matrixapi@upi', 'Matrix API Payment Gateway', 1)`, (err) => {
                                            if (err) {
                                                console.error('Error inserting default UPI details:', err);
                                                return reject(err);
                                            }
                                            resolve();
                                        });
                                    } else {
                                        resolve();
                                    }
                                });
                            });
                        });
                    });
                });
            });
        });
    });
};

/**
 * Add payment columns to existing subscriptions table for backward compatibility
 */
export const addPaymentColumnsToSubscriptions = () => {
    return new Promise((resolve, reject) => {
        // Check if payment_id column exists in subscriptions table
        db.get("PRAGMA table_info(subscriptions)", [], (err, rows) => {
            if (err) {
                return reject(err);
            }

            const hasPaymentId = rows.some(row => row.name === 'payment_id');
            
            if (!hasPaymentId) {
                db.run("ALTER TABLE subscriptions ADD COLUMN payment_id INTEGER", (err) => {
                    if (err) {
                        console.error('Error adding payment_id column to subscriptions:', err);
                        return reject(err);
                    }
                    console.log('Added payment_id column to subscriptions table');
                    resolve();
                });
            } else {
                resolve();
            }
        });
    });
};

/**
 * Add payment columns to existing user_features table for backward compatibility
 */
export const addPaymentColumnsToUserFeatures = () => {
    return new Promise((resolve, reject) => {
        // Check if payment_id column exists in user_features table
        db.get("PRAGMA table_info(user_features)", [], (err, rows) => {
            if (err) {
                return reject(err);
            }

            const hasPaymentId = rows.some(row => row.name === 'payment_id');
            
            if (!hasPaymentId) {
                db.run("ALTER TABLE user_features ADD COLUMN payment_id INTEGER", (err) => {
                    if (err) {
                        console.error('Error adding payment_id column to user_features:', err);
                        return reject(err);
                    }
                    console.log('Added payment_id column to user_features table');
                    resolve();
                });
            } else {
                resolve();
            }
        });
    });
};