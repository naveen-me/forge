import ffmpeg from 'fluent-ffmpeg';
import db from '../db/database.js';
import fs from 'fs';

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

export const processMediaItem = async (mediaItemId) => {
    try {
        const item = (await runCommand(
            `SELECT path FROM media_items WHERE id = ?`,
            [mediaItemId]
        ))[0];

        if (!item || !item.path) {
            console.error(`Media item ${mediaItemId} not found or path is missing.`);
            return;
        }

        const filePath = item.path;

        if (!fs.existsSync(filePath)) {
            await runCommand(
                `UPDATE media_items SET status = 'missing', updated_at = CURRENT_TIMESTAMP WHERE id = ?`,
                [mediaItemId]
            );
            console.warn(`Source file for media item ${mediaItemId} not found on disk: ${filePath}`);
            return;
        }

        ffmpeg.ffprobe(filePath, async (err, metadata) => {
            if (err) {
                console.error(`Error probing media item ${mediaItemId}:`, err);
                await runCommand(
                    `UPDATE media_items SET status = 'error', updated_at = CURRENT_TIMESTAMP WHERE id = ?`,
                    [mediaItemId]
                );
                return;
            }

            const videoStream = metadata.streams.find(s => s.codec_type === 'video');
            const format = metadata.format;

            const size = format.size;
            const duration = format.duration;
            const dimensions = videoStream ? `${videoStream.width}x${videoStream.height}` : null;

            await runCommand(
                `INSERT OR REPLACE INTO media_metadata (media_item_id, size, format, dimensions, duration) VALUES (?, ?, ?, ?, ?)`,
                [mediaItemId, size, format.format_name, dimensions, duration]
            );

            await runCommand(
                `UPDATE media_items SET status = 'available', updated_at = CURRENT_TIMESTAMP WHERE id = ?`,
                [mediaItemId]
            );
            console.log(`Metadata extracted and updated for media item ${mediaItemId}.`);
        });

    } catch (error) {
        console.error(`Failed to process media item ${mediaItemId}:`, error);
        await runCommand(
            `UPDATE media_items SET status = 'error', updated_at = CURRENT_TIMESTAMP WHERE id = ?`,
            [mediaItemId]
        );
    }
};
