const ffmpeg = require('fluent-ffmpeg');
const { MediaItem } = require('../../models/MediaItem.js');
const fs = require('fs');

const processMediaItem = async (mediaItemId) => {
    try {
        // Find the media item using Sequelize
        const item = await MediaItem.findByPk(mediaItemId);

        if (!item || !item.filePath) {
            console.error(`Media item ${mediaItemId} not found or filePath is missing.`);
            return;
        }

        const filePath = item.filePath;

        if (!fs.existsSync(filePath)) {
            await MediaItem.update(
                { status: 'missing' },
                { where: { id: mediaItemId } }
            );
            console.warn(`Source file for media item ${mediaItemId} not found on disk: ${filePath}`);
            return;
        }

                        // Extract metadata using FFmpeg - make sure to handle potential FFmpeg issues
        try {
            // Verify the file is accessible before processing
            if (!fs.existsSync(filePath)) {
                await MediaItem.update(
                    { status: 'missing' },
                    { where: { id: mediaItemId } }
                ).catch(updateError => {
                    console.error(`Failed to update missing status for media item ${mediaItemId}:`, updateError);
                });
                return;
            }

            ffmpeg.ffprobe(filePath, async (err, metadata) => {
                try {
                    if (err) {
                        console.error(`Error probing media item ${mediaItemId}:`, err);
                        await MediaItem.update(
                            { status: 'error' },
                            { where: { id: mediaItemId } }
                        ).catch(updateError => {
                            console.error(`Failed to update error status for media item ${mediaItemId}:`, updateError);
                        });
                        return;
                    }

                    const videoStream = metadata.streams.find(s => s.codec_type === 'video');
                    const format = metadata.format;

                    // Prepare metadata
                    const size = format.size ? parseInt(format.size) : null;
                    const duration = format.duration ? parseFloat(format.duration) : null;
                    const formatName = format.format_name || null;
                    const dimensions = videoStream ? `${videoStream.width}x${videoStream.height}` : null;

                    // Update the MediaItem with metadata - using Sequelize model fields
                    await MediaItem.update(
                        {
                            size: size,
                            duration: duration,
                            dimensions: dimensions,
                            mimeType: formatName,
                            status: 'available'
                        },
                        { where: { id: mediaItemId } }
                    ).catch(updateError => {
                        console.error(`Failed to update metadata for media item ${mediaItemId}:`, updateError);
                    });

                    console.log(`Metadata extracted and updated for media item ${mediaItemId}.`);
                } catch (callbackError) {
                    console.error(`Error in FFmpeg callback for media item ${mediaItemId}:`, callbackError);
                    await MediaItem.update(
                        { status: 'error' },
                        { where: { id: mediaItemId } }
                    ).catch(updateError => {
                        console.error(`Failed to update error status for media item ${mediaItemId}:`, updateError);
                    });
                }
            });
        } catch (ffmpegError) {
            console.error(`FFmpeg error for media item ${mediaItemId}:`, ffmpegError);
            // Still update to available status but without metadata, so it doesn't stay in processing state
            await MediaItem.update(
                { 
                    status: 'available',
                    size: fs.statSync(filePath).size // At least set the file size
                },
                { where: { id: mediaItemId } }
            ).catch(updateError => {
                console.error(`Failed to update fallback status for media item ${mediaItemId}:`, updateError);
            });
        }

    } catch (error) {
        console.error(`Failed to process media item ${mediaItemId}:`, error);
        try {
            await MediaItem.update(
                { status: 'error' },
                { where: { id: mediaItemId } }
            );
        } catch (updateError) {
            console.error(`Failed to update error status for media item ${mediaItemId}:`, updateError);
        }
    }
};

module.exports = {
    processMediaItem
};