import db from '../db/database.js';
import fs from 'fs';
import path from 'path';
import { processMediaItem } from '../services/mediaProcessorService.js';

// Helper function to run database queries
const runQuery = (sql, params = []) => {
    return new Promise((resolve, reject) => {
        db.all(sql, params, (err, rows) => {
            if (err) {
                reject(err);
            } else {
                resolve(rows);
            }
        });
    });
};

// Helper function to run database commands (insert, update, delete)
const runCommand = (sql, params = []) => {
    return new Promise((resolve, reject) => {
        db.run(sql, params, function (err) {
            if (err) {
                reject(err);
            } else {
                resolve({ id: this.lastID, changes: this.changes });
            }
        });
    });
};

export const listMediaItems = async (req, res) => {
    const { parentId } = req.query;
    const userId = req.user.id; // Assuming user ID is available from authentication middleware

    let sql = `SELECT mi.id, mi.name, mi.type, mi.path, mi.parent_id, mi.status, mi.created_at, mi.updated_at,
                       mm.size, mm.format, mm.dimensions, mm.duration
                FROM media_items mi
                LEFT JOIN media_metadata mm ON mi.id = mm.media_item_id
                WHERE mi.user_id = ?`;
    const params = [userId];

    if (parentId) {
        sql += ` AND mi.parent_id = ?`;
        params.push(parentId);
    } else {
        sql += ` AND mi.parent_id IS NULL`;
    }

    try {
        const items = await runQuery(sql, params);
        res.json({ success: true, data: items });
    } catch (error) {
        console.error('Error listing media items:', error);
        res.status(500).json({ success: false, message: 'Failed to list media items.' });
    }
};

export const createFolder = async (req, res) => {
    const { name, parentId } = req.body;
    const userId = req.user.id;

    if (!name) {
        return res.status(400).json({ success: false, message: 'Folder name is required.' });
    }

    try {
        const result = await runCommand(
            `INSERT INTO media_items (name, type, parent_id, user_id) VALUES (?, 'folder', ?, ?)`, 
            [name, parentId, userId]
        );
        res.status(201).json({ success: true, message: 'Folder created successfully.', id: result.id });
    } catch (error) {
        console.error('Error creating folder:', error);
        res.status(500).json({ success: false, message: 'Failed to create folder.' });
    }
};

export const addFiles = async (req, res) => {
    const { files, parentId } = req.body; // files is an array of { name, path }
    const userId = req.user.id;

    if (!Array.isArray(files) || files.length === 0) {
        return res.status(400).json({ success: false, message: 'No files provided.' });
    }

    try {
        const insertedFiles = [];
        for (const file of files) {
            const result = await runCommand(
                `INSERT INTO media_items (name, type, path, parent_id, user_id, status) VALUES (?, 'file', ?, ?, ?, 'processing')`, 
                [file.name, file.path, parentId, userId]
            );
            insertedFiles.push({ id: result.id, name: file.name, path: file.path });
            processMediaItem(result.id); // Trigger metadata extraction
        }
        res.status(201).json({ success: true, message: 'Files added successfully.', data: insertedFiles });
    } catch (error) {
        console.error('Error adding files:', error);
        res.status(500).json({ success: false, message: 'Failed to add files.' });
    }
};

export const renameMediaItem = async (req, res) => {
    const { id } = req.params;
    const { newName } = req.body;
    const userId = req.user.id;

    if (!newName) {
        return res.status(400).json({ success: false, message: 'New name is required.' });
    }

    try {
        const result = await runCommand(
            `UPDATE media_items SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?`, 
            [newName, id, userId]
        );
        if (result.changes === 0) {
            return res.status(404).json({ success: false, message: 'Media item not found or not authorized.' });
        }
        res.json({ success: true, message: 'Media item renamed successfully.' });
    } catch (error) {
        console.error('Error renaming media item:', error);
        res.status(500).json({ success: false, message: 'Failed to rename media item.' });
    }
};

export const deleteMediaItems = async (req, res) => {
    const { ids } = req.body; // ids is an array of item IDs to delete
    const userId = req.user.id;

    if (!Array.isArray(ids) || ids.length === 0) {
        return res.status(400).json({ success: false, message: 'No media item IDs provided for deletion.' });
    }

    try {
        // Delete media items and their associated metadata
        // SQLite's ON DELETE CASCADE will handle metadata deletion
        const placeholders = ids.map(() => '?').join(',');
        const result = await runCommand(
            `DELETE FROM media_items WHERE id IN (${placeholders}) AND user_id = ?`, 
            [...ids, userId]
        );
        if (result.changes === 0) {
            return res.status(404).json({ success: false, message: 'No media items found or authorized for deletion.' });
        }
        res.json({ success: true, message: `${result.changes} media items deleted successfully.` });
    } catch (error) {
        console.error('Error deleting media items:', error);
        res.status(500).json({ success: false, message: 'Failed to delete media items.' });
    }
};

export const moveMediaItems = async (req, res) => {
    const { ids, newParentId } = req.body;
    const userId = req.user.id;

    if (!Array.isArray(ids) || ids.length === 0) {
        return res.status(400).json({ success: false, message: 'No media item IDs provided for moving.' });
    }

    try {
        // Optional: Validate newParentId exists and is a folder owned by the user
        if (newParentId) {
            const parent = await runQuery(
                `SELECT id FROM media_items WHERE id = ? AND type = 'folder' AND user_id = ?`,
                [newParentId, userId]
            );
            if (parent.length === 0) {
                return res.status(404).json({ success: false, message: 'Destination folder not found or not authorized.' });
            }
        }

        const placeholders = ids.map(() => '?').join(',');
        const result = await runCommand(
            `UPDATE media_items SET parent_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id IN (${placeholders}) AND user_id = ?`,
            [newParentId, ...ids, userId]
        );

        if (result.changes === 0) {
            return res.status(404).json({ success: false, message: 'No media items found or authorized for moving.' });
        }
        res.json({ success: true, message: `${result.changes} media items moved successfully.` });
    } catch (error) {
        console.error('Error moving media items:', error);
        res.status(500).json({ success: false, message: 'Failed to move media items.' });
    }
};

export const copyMediaItems = async (req, res) => {
    const { ids, newParentId } = req.body;
    const userId = req.user.id;

    if (!Array.isArray(ids) || ids.length === 0) {
        return res.status(400).json({ success: false, message: 'No media item IDs provided for copying.' });
    }

    try {
        // Optional: Validate newParentId exists and is a folder owned by the user
        if (newParentId) {
            const parent = await runQuery(
                `SELECT id FROM media_items WHERE id = ? AND type = 'folder' AND user_id = ?`,
                [newParentId, userId]
            );
            if (parent.length === 0) {
                return res.status(404).json({ success: false, message: 'Destination folder not found or not authorized.' });
            }
        }

        const copiedItems = [];
        for (const id of ids) {
            // Fetch original item and its metadata
            const originalItem = (await runQuery(
                `SELECT mi.id, mi.name, mi.type, mi.path, mi.status, mm.size, mm.format, mm.dimensions, mm.duration
                 FROM media_items mi
                 LEFT JOIN media_metadata mm ON mi.id = mm.media_item_id
                 WHERE mi.id = ? AND mi.user_id = ?`,
                [id, userId]
            ))[0];

            if (!originalItem) {
                console.warn(`Media item ${id} not found or not authorized for copying.`);
                continue; // Skip to next item
            }

            // Start a transaction for atomicity
            await new Promise((resolve, reject) => {
                db.serialize(async () => {
                    db.run('BEGIN TRANSACTION;');
                    try {
                        // Insert new media item
                        const newName = `Copy of ${originalItem.name}`;
                        const newItemResult = await runCommand(
                            `INSERT INTO media_items (name, type, path, parent_id, user_id, status) VALUES (?, ?, ?, ?, ?, ?)`,
                            [newName, originalItem.type, originalItem.path, newParentId, userId, originalItem.status]
                        );
                        const newItemId = newItemResult.id;

                        // If it's a file, copy its metadata
                        if (originalItem.type === 'file' && originalItem.size !== null) {
                            await runCommand(
                                `INSERT INTO media_metadata (media_item_id, size, format, dimensions, duration) VALUES (?, ?, ?, ?, ?)`,
                                [newItemId, originalItem.size, originalItem.format, originalItem.dimensions, originalItem.duration]
                            );
                        }
                        
                        copiedItems.push({ id: newItemId, name: newName, type: originalItem.type });
                        db.run('COMMIT;', resolve);
                    } catch (txError) {
                        db.run('ROLLBACK;', () => reject(txError));
                    } finally {
                        // Ensure transaction is always closed
                        resolve();
                    }
                });
            });
        }

        if (copiedItems.length === 0) {
            return res.status(404).json({ success: false, message: 'No media items found or authorized for copying.' });
        }
        res.json({ success: true, message: `${copiedItems.length} media items copied successfully.`, data: copiedItems });
    } catch (error) {
        console.error('Error copying media items:', error);
        res.status(500).json({ success: false, message: 'Failed to copy media items.' });
    }
};

export const streamMediaItem = async (req, res) => {
    const { id } = req.params;
    const userId = req.user.id;

    try {
        const item = (await runQuery(
            `SELECT path FROM media_items WHERE id = ? AND type = 'file' AND user_id = ?`,
            [id, userId]
        ))[0];

        if (!item || !item.path) {
            return res.status(404).json({ success: false, message: 'Media file not found or not authorized.' });
        }

        const filePath = item.path;

        // Check if file exists
        if (!fs.existsSync(filePath)) {
            // Update status to 'missing' in DB
            await runCommand(
                `UPDATE media_items SET status = 'missing', updated_at = CURRENT_TIMESTAMP WHERE id = ?`,
                [id]
            );
            return res.status(404).json({ success: false, message: 'Source file not found on disk.' });
        }

        const stat = fs.statSync(filePath);
        const fileSize = stat.size;
        const range = req.headers.range;

        if (range) {
            const parts = range.replace(/bytes=/, '').split('-');
            const start = parseInt(parts[0], 10);
            const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
            const chunksize = (end - start) + 1;
            const file = fs.createReadStream(filePath, { start, end });
            const head = {
                'Content-Range': `bytes ${start}-${end}/${fileSize}`,
                'Accept-Ranges': 'bytes',
                'Content-Length': chunksize,
                'Content-Type': 'video/mp4', // Assuming MP4 for now, can be dynamic
            };
            res.writeHead(206, head);
            file.pipe(res);
        } else {
            const head = {
                'Content-Length': fileSize,
                'Content-Type': 'video/mp4', // Assuming MP4 for now, can be dynamic
            };
            res.writeHead(200, head);
            fs.createReadStream(filePath).pipe(res);
        }
    } catch (error) {
        console.error('Error streaming media item:', error);
        res.status(500).json({ success: false, message: 'Failed to stream media item.' });
    }
};
