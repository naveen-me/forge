import ffmpeg from 'fluent-ffmpeg';
import { MediaItem } from './models/MediaItem.js';
import { Ad } from './models/Ad.js';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

// Define __dirname for ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

async function generateThumbnail(mediaId, modelName = 'MediaItem') {
    const Model = modelName === 'Ad' ? Ad : MediaItem;
    const item = await Model.findByPk(mediaId);

    if (!item || !item.filePath || !fs.existsSync(item.filePath)) {
        console.error(`${modelName} not found or file does not exist.`);
        return;
    }

    const thumbnailDir = path.join(__dirname, 'public', 'thumbnails');
    if (!fs.existsSync(thumbnailDir)) {
        fs.mkdirSync(thumbnailDir, { recursive: true });
    }

    const thumbnailFilename = `${modelName.toLowerCase()}-${item.id}.png`;
    const thumbnailPath = path.join(thumbnailDir, thumbnailFilename);
    const publicUrl = `/thumbnails/${thumbnailFilename}`;

    return new Promise((resolve, reject) => {
        // First, let's update the status to processing so UI can show progress
        item.update({ status: 'processing' }).catch(console.error);

        ffmpeg.ffprobe(item.filePath, (err, metadata) => {
            if (err) {
                console.error('Error probing media:', err);
                item.update({ status: 'error' }).catch(console.error);
                return reject(err);
            }

            const videoStream = metadata.streams.find(s => s.codec_type === 'video');
            const format = metadata.format;

            const size = format.size ? parseInt(format.size) : null;
            const duration = format.duration ? parseFloat(format.duration) : null;
            const formatName = format.format_name || null;
            const dimensions = videoStream ? `${videoStream.width}x${videoStream.height}` : null;

            // Use faster thumbnail generation options
            ffmpeg(item.filePath)
                .on('end', async () => {
                    try {
                        await item.update({
                            thumbnailPath: publicUrl,
                            status: 'available',
                            size,
                            duration,
                            dimensions,
                            mimeType: formatName,
                        });
                        // Refresh the item to get updated values
                        const updatedItem = await Model.findByPk(mediaId);
                        resolve(updatedItem);
                    } catch (updateErr) {
                        console.error('Error updating item after thumbnail generation:', updateErr);
                        reject(updateErr);
                    }
                })
                .on('error', async (err) => {
                    console.error('Error generating thumbnail:', err);
                    try {
                        await item.update({ status: 'error' });
                        const updatedItem = await Model.findByPk(mediaId);
                        reject(err);
                    } catch (updateErr) {
                        console.error('Error updating item status after thumbnail error:', updateErr);
                        reject(err);
                    }
                })
                .screenshots({
                    count: 1,
                    // Use the first second instead of 50% to speed up extraction
                    timemarks: ['10%'],
                    // Use a smaller size for faster processing
                    size: '320x180',
                    // Use faster extraction options
                    filename: thumbnailFilename,
                    folder: thumbnailDir,
                    // Add some encoding options for faster processing
                    fastSeek: true,
                });
        });
    });
}

// Check if the script is running directly (not imported)
import.meta.url === `file://${process.argv[1]}` ?
    runIfMain() :
    null;

async function runIfMain() {
    const args = process.argv.slice(2);
    if (args[0] === '--generate-thumbnail') {
        const modelName = args[1];
        const mediaId = args[2];
        try {
            await generateThumbnail(mediaId, modelName);
            process.exit(0);
        } catch (error) {
            console.error('Error generating thumbnail:', error);
            process.exit(1);
        }
    }
}

export { generateThumbnail };