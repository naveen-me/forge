import db from './database.js';

/**
 * Initialize the media library database tables
 */
export const initMediaLibraryDb = () => {
    return new Promise((resolve, reject) => {
        db.serialize(() => {
            // Create media_items table
            db.run(`CREATE TABLE IF NOT EXISTS media_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('file', 'folder')),
                path TEXT,
                parent_id INTEGER,
                user_id INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'processing' CHECK(status IN ('available', 'missing', 'processing')),
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (parent_id) REFERENCES media_items(id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )`, (err) => {
                if (err) {
                    console.error('Error creating media_items table:', err);
                    return reject(err);
                }

                // Create media_metadata table
                db.run(`CREATE TABLE IF NOT EXISTS media_metadata (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    media_item_id INTEGER NOT NULL,
                    size INTEGER,
                    format TEXT,
                    dimensions TEXT,
                    duration REAL,
                    UNIQUE(media_item_id),
                    FOREIGN KEY (media_item_id) REFERENCES media_items(id) ON DELETE CASCADE
                )`, (err) => {
                    if (err) {
                        console.error('Error creating media_metadata table:', err);
                        return reject(err);
                    }
                    resolve();
                });
            });
        });
    });
};
