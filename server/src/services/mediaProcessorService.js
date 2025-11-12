const ffmpeg = require('fluent-ffmpeg');
const { MediaItem } = require('../../models/MediaItem.js');
const { Ad } = require('../../models/Ad.js');
const fs = require('fs');

const processMediaItem = async (mediaItemId, type = 'MediaItem') => {
    const model = type === 'Ad' ? Ad : MediaItem;
    try {
        const item = await model.findByPk(mediaItemId);

        if (!item || !item.filePath) {
            console.error(`${type} ${mediaItemId} not found or filePath is missing.`);
            return;
        }

        const filePath = item.filePath;

        if (!fs.existsSync(filePath)) {
            await model.update(
                { status: 'missing' },
                { where: { id: mediaItemId } }
            );
            console.warn(`Source file for ${type} ${mediaItemId} not found on disk: ${filePath}`);
            return;
        }

        try {
            if (!fs.existsSync(filePath)) {
                await model.update(
                    { status: 'missing' },
                    { where: { id: mediaItemId } }
                ).catch(updateError => {
                    console.error(`Failed to update missing status for ${type} ${mediaItemId}:`, updateError);
                });
                return;
            }

            ffmpeg.ffprobe(filePath, async (err, metadata) => {
                try {
                    if (err) {
                        console.error(`Error probing ${type} ${mediaItemId}:`, err);
                        await model.update(
                            { status: 'error' },
                            { where: { id: mediaItemId } }
                        ).catch(updateError => {
                            console.error(`Failed to update error status for ${type} ${mediaItemId}:`, updateError);
                        });
                        return;
                    }

                    const videoStream = metadata.streams.find(s => s.codec_type === 'video');
                    const format = metadata.format;

                    const size = format.size ? parseInt(format.size) : null;
                    const duration = format.duration ? parseFloat(format.duration) : null;
                    const formatName = format.format_name || null;
                    const dimensions = videoStream ? `${videoStream.width}x${videoStream.height}` : null;

                    await model.update(
                        {
                            size: size,
                            duration: duration,
                            dimensions: dimensions,
                            mimeType: formatName,
                            status: 'available'
                        },
                        { where: { id: mediaItemId } }
                    ).catch(updateError => {
                        console.error(`Failed to update metadata for ${type} ${mediaItemId}:`, updateError);
                    });

                    console.log(`Metadata extracted and updated for ${type} ${mediaItemId}.`);
                } catch (callbackError) {
                    console.error(`Error in FFmpeg callback for ${type} ${mediaItemId}:`, callbackError);
                    await model.update(
                        { status: 'error' },
                        { where: { id: mediaItemId } }
                    ).catch(updateError => {
                        console.error(`Failed to update error status for ${type} ${mediaItemId}:`, updateError);
                    });
                }
            });
        } catch (ffmpegError) {
            console.error(`FFmpeg error for ${type} ${mediaItemId}:`, ffmpegError);
            await model.update(
                { 
                    status: 'available',
                    size: fs.statSync(filePath).size
                },
                { where: { id: mediaItemId } }
            ).catch(updateError => {
                console.error(`Failed to update fallback status for ${type} ${mediaItemId}:`, updateError);
            });
        }

    } catch (error) {
        console.error(`Failed to process ${type} ${mediaItemId}:`, error);
        try {
            await model.update(
                { status: 'error' },
                { where: { id: mediaItemId } }
            );
        } catch (updateError) {
            console.error(`Failed to update error status for ${type} ${mediaItemId}:`, updateError);
        }
    }
};

module.exports = {
    processMediaItem
};