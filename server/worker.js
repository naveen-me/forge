const ffmpeg = require('fluent-ffmpeg');
const { MediaItem } = require('./models/MediaItem');
const { Ad } = require('./models/Ad');
const fs = require('fs');
const path = require('path');

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
        ffmpeg(item.filePath)
            .on('end', async () => {
                await item.update({ thumbnailPath: publicUrl, status: 'available' });
                resolve(item);
            })
            .on('error', (err) => {
                console.error('Error generating thumbnail:', err);
                item.update({ status: 'error' });
                reject(err);
            })
            .screenshots({
                count: 1,
                timemarks: ['50%'],
                filename: thumbnailFilename,
                folder: thumbnailDir,
                size: '320x180'
            });
    });
}

if (require.main === module) {
    const args = process.argv.slice(2);
    if (args[0] === '--generate-thumbnail') {
        const modelName = args[1];
        const mediaId = args[2];
        generateThumbnail(mediaId, modelName)
            .then(() => process.exit(0))
            .catch(() => process.exit(1));
    }
}

module.exports = {
    generateThumbnail
};