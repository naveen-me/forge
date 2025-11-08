const ffmpeg = require('fluent-ffmpeg');
const { MediaItem } = require('./models/MediaItem');
const fs = require('fs');
const path = require('path');

/**
 * Extract video metadata using FFmpeg
 * @param {string} filePath - Path to the video file
 * @returns {Promise<Object>} - Promise that resolves to metadata object
 */
function extractVideoMetadata(filePath) {
  return new Promise((resolve, reject) => {
    ffmpeg.ffprobe(filePath, (err, metadata) => {
      if (err) {
        console.error('Error extracting metadata:', err);
        reject(err);
        return;
      }

      const format = metadata.format;
      const videoStream = metadata.streams.find(stream => stream.codec_type === 'video');

      const result = {
        duration: parseFloat(format.duration) || null,
        size: parseInt(format.size) || null,
        dimensions: null,
        mimeType: format.format_name || null
      };

      if (videoStream) {
        result.dimensions = `${videoStream.width}x${videoStream.height}`;
      }

      resolve(result);
    });
  });
}

/**
 * Process a file to extract metadata and update database
 * @param {number} fileId - ID of the file in the database
 * @param {string} filePath - Path to the file
 */
async function processFileMetadata(fileId, filePath) {
  try {
    // Get basic file info
    const stats = fs.statSync(filePath);
    let size = stats.size;

    // Extract video-specific metadata if it's a video file
    const extension = path.extname(filePath).toLowerCase();
    const videoExtensions = ['.mp4', '.mov', '.avi', '.mkv', '.wmv', '.flv', '.webm', '.m4v'];

    if (videoExtensions.includes(extension)) {
      try {
        const metadata = await extractVideoMetadata(filePath);
        // Update database with extracted metadata using Sequelize
        await MediaItem.update(
          {
            size: metadata.size || size,
            duration: metadata.duration,
            dimensions: metadata.dimensions,
            mimeType: metadata.mimeType
          },
          { where: { id: fileId } }
        );
      } catch (metadataError) {
        console.error(`Error extracting metadata for file ${filePath}:`, metadataError);
        // Still update with basic size info if metadata extraction fails
        await MediaItem.update({ 
          size: size,
          status: 'available' // Set to available even if metadata extraction failed
        }, { where: { id: fileId } });
      }
    } else {
      // For non-video files, just update size
      await MediaItem.update({ 
        size: size,
        status: 'available'
      }, { where: { id: fileId } });
    }
  } catch (error) {
    console.error(`Error processing file ${filePath}:`, error);
    // Update with error status in database
    await MediaItem.update(
      { 
        size: -1, // Use -1 to indicate error
        status: 'error'
      }, 
      { where: { id: fileId } }
    );
  }
}

/**
 * Process all pending files in the queue
 */
async function processPendingFiles() {
  try {
    // Find all files that don't have metadata yet (size is still null)
    const pendingFiles = await MediaItem.findAll({
      where: {
        type: 'file',
        filePath: { [MediaItem.sequelize.Op.not]: null },
        size: null
      }
    });

    for (const file of pendingFiles) {
      await processFileMetadata(file.id, file.filePath);
    }
  } catch (error) {
    console.error('Error processing pending files:', error);
  }
}

// Run processing when module is executed directly
if (require.main === module) {
  console.log('Starting metadata extraction worker...');
  processPendingFiles()
    .then(() => {
      console.log('Metadata extraction completed');
      process.exit(0);
    })
    .catch(error => {
      console.error('Worker error:', error);
      process.exit(1);
    });
}

module.exports = {
  extractVideoMetadata,
  processFileMetadata,
  processPendingFiles
};