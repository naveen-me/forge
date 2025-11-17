import ffmpeg from 'fluent-ffmpeg';
import fs from 'fs';
import path from 'path';
import { MediaItem } from '../../models/MediaItem.js';
import { Ad } from '../../models/Ad.js';

// Function to process a media item or ad - extract metadata and generate thumbnail
export const processMediaItem = async (itemId, modelName = 'MediaItem') => {
  try {
    console.log(`Processing ${modelName} with ID: ${itemId}`);

    // Determine which model to use based on modelName
    const Model = modelName === 'Ad' ? Ad : MediaItem;
    
    // Find the item in the database
    const item = await Model.findByPk(itemId);
    if (!item) {
      console.error(`${modelName} with ID ${itemId} not found`);
      return;
    }

    if (item.type === 'folder' || item.type === 'group') {
      // Folders and groups don't need processing
      await item.update({ status: 'available' });
      return;
    }

    const filePath = item.filePath;
    if (!filePath || !fs.existsSync(filePath)) {
      console.error(`File path does not exist: ${filePath}`);
      await item.update({ status: 'missing' });
      return;
    }

    // Get file stats for size
    const stats = fs.statSync(filePath);
    const fileSize = stats.size;

    // Determine file type and process accordingly
    const mimeType = getMimeType(filePath);

    if (mimeType.startsWith('image/')) {
      await processImage(item, filePath, fileSize, modelName);
    } else if (mimeType.startsWith('video/')) {
      await processVideo(item, filePath, fileSize, modelName);
    } else {
      await processOtherFile(item, filePath, fileSize, mimeType, modelName);
    }

    console.log(`Successfully processed ${modelName} with ID: ${itemId}`);
  } catch (error) {
    console.error(`Error processing ${modelName} with ID ${itemId}:`, error);

    try {
      // Update status to error in the database
      await Model.update({ status: 'error' }, { where: { id: itemId } });
    } catch (updateError) {
      console.error(`Error updating ${modelName} status to error:`, updateError);
    }
  }
};

// Helper function to determine MIME type based on file extension
const getMimeType = (filePath) => {
  const ext = path.extname(filePath).toLowerCase();
  const mimeTypes = {
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.png': 'image/png',
    '.gif': 'image/gif',
    '.bmp': 'image/bmp',
    '.webp': 'image/webp',
    '.mp4': 'video/mp4',
    '.avi': 'video/x-msvideo',
    '.mov': 'video/quicktime',
    '.wmv': 'video/x-ms-wmv',
    '.flv': 'video/x-flv',
    '.webm': 'video/webm',
    '.mkv': 'video/x-matroska',
    '.mp3': 'audio/mpeg',
    '.wav': 'audio/wav',
    '.flac': 'audio/flac',
    '.pdf': 'application/pdf',
    '.txt': 'text/plain',
    '.doc': 'application/msword',
    '.docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    '.xls': 'application/vnd.ms-excel',
    '.xlsx': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  };

  return mimeTypes[ext] || 'application/octet-stream';
};

// Process image files
const processImage = async (item, filePath, fileSize, modelName) => {
  try {
    // Get image dimensions
    const size = await getImageDimensions(filePath);

    // Update the item in the database
    await item.update({
      size: fileSize,
      dimensions: size ? `${size.width}x${size.height}` : null,
      mimeType: getMimeType(filePath),
      status: 'available'
    });
  } catch (error) {
    console.error(`Error processing image:`, error);
    const Model = modelName === 'Ad' ? Ad : MediaItem;
    await Model.update({ status: 'error' }, { where: { id: item.id } });
  }
};

// Process video files
const processVideo = async (item, filePath, fileSize, modelName) => {
  try {
    // Get video metadata using ffmpeg
    const metadata = await getVideoMetadata(filePath);

    // Create thumbnail
    const thumbnailPath = await createThumbnail(item.id, filePath, modelName);

    // Update the item in the database
    await item.update({
      size: fileSize,
      duration: metadata.duration,
      dimensions: metadata.dimensions || null,
      mimeType: getMimeType(filePath),
      thumbnailPath: thumbnailPath,
      status: 'available'
    });
  } catch (error) {
    console.error(`Error processing video:`, error);
    const Model = modelName === 'Ad' ? Ad : MediaItem;
    await Model.update({ status: 'error' }, { where: { id: item.id } });
  }
};

// Process other types of files
const processOtherFile = async (item, filePath, fileSize, mimeType, modelName) => {
  try {
    // For other files, just update the size and MIME type
    await item.update({
      size: fileSize,
      mimeType: mimeType,
      status: 'available'
    });
  } catch (error) {
    console.error(`Error processing other file:`, error);
    const Model = modelName === 'Ad' ? Ad : MediaItem;
    await Model.update({ status: 'error' }, { where: { id: item.id } });
  }
};

// Get image dimensions using a library or native method
const getImageDimensions = (filePath) => {
  return new Promise((resolve, reject) => {
    // This is a simplified approach - in a real application, you might want to use
    // a library like 'sharp' to get image dimensions
    // For now, we'll just return a placeholder
    resolve({ width: null, height: null });
  });
};

// Get video metadata using ffmpeg
const getVideoMetadata = (filePath) => {
  return new Promise((resolve, reject) => {
    ffmpeg.ffprobe(filePath, (err, metadata) => {
      if (err) {
        console.error('Error getting video metadata:', err);
        resolve({ duration: 0, dimensions: null });
        return;
      }

      const videoStream = metadata.streams.find(stream =>
        stream.codec_type === 'video'
      );

      if (videoStream) {
        const duration = metadata.format.duration || 0;
        const dimensions = videoStream.width && videoStream.height
          ? `${videoStream.width}x${videoStream.height}`
          : null;

        resolve({
          duration: parseFloat(duration),
          dimensions
        });
      } else {
        resolve({ duration: 0, dimensions: null });
      }
    });
  });
};

// Create thumbnail for video files
const createThumbnail = (itemId, filePath, modelName = 'MediaItem') => {
  return new Promise((resolve, reject) => {
    // Create a thumbnail directory if it doesn't exist
    const thumbnailDir = path.join(process.cwd(), 'thumbnails');
    if (!fs.existsSync(thumbnailDir)) {
      fs.mkdirSync(thumbnailDir, { recursive: true });
    }

    // Define thumbnail path - use the model name in the thumbnail filename
    const thumbnailPath = path.join(thumbnailDir, `${modelName.toLowerCase()}_${itemId}.jpg`);

    // Generate thumbnail from video
    ffmpeg(filePath)
      .on('error', (err) => {
        console.error('Error creating thumbnail:', err);
        resolve(null); // Resolve with null if thumbnail creation fails
      })
      .on('end', () => {
        // Return web-accessible path instead of full file system path
        resolve(`/thumbnails/${modelName.toLowerCase()}_${itemId}.jpg`);
      })
      .screenshots({
        timestamps: ['50%'], // Take screenshot at 50% of video duration
        filename: `${modelName.toLowerCase()}_${itemId}.jpg`,
        folder: thumbnailDir,
        size: '320x240'
      });
  });
};

// Export the function for use
export default { processMediaItem };