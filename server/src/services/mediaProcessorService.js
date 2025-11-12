import ffmpeg from 'fluent-ffmpeg';
import fs from 'fs';
import path from 'path';
import { MediaItem } from '../../models/MediaItem.js';

// Function to process a media item - extract metadata and generate thumbnail
export const processMediaItem = async (itemId) => {
  try {
    console.log(`Processing media item with ID: ${itemId}`);
    
    // Find the media item in the database
    const mediaItem = await MediaItem.findByPk(itemId);
    if (!mediaItem) {
      console.error(`Media item with ID ${itemId} not found`);
      return;
    }

    if (mediaItem.type === 'folder') {
      // Folders don't need processing
      await mediaItem.update({ status: 'available' });
      return;
    }

    const filePath = mediaItem.filePath;
    if (!filePath || !fs.existsSync(filePath)) {
      console.error(`File path does not exist: ${filePath}`);
      await mediaItem.update({ status: 'missing' });
      return;
    }

    // Get file stats for size
    const stats = fs.statSync(filePath);
    const fileSize = stats.size;

    // Determine file type and process accordingly
    const mimeType = getMimeType(filePath);
    
    if (mimeType.startsWith('image/')) {
      await processImage(mediaItem, filePath, fileSize);
    } else if (mimeType.startsWith('video/')) {
      await processVideo(mediaItem, filePath, fileSize);
    } else {
      await processOtherFile(mediaItem, filePath, fileSize, mimeType);
    }

    console.log(`Successfully processed media item with ID: ${itemId}`);
  } catch (error) {
    console.error(`Error processing media item with ID ${itemId}:`, error);
    
    try {
      // Update status to error in the database
      await MediaItem.update({ status: 'error' }, { where: { id: itemId } });
    } catch (updateError) {
      console.error(`Error updating media item status to error:`, updateError);
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
const processImage = async (mediaItem, filePath, fileSize) => {
  try {
    // Get image dimensions
    const size = await getImageDimensions(filePath);
    
    // Update the media item in the database
    await mediaItem.update({
      size: fileSize,
      dimensions: size ? `${size.width}x${size.height}` : null,
      mimeType: getMimeType(filePath),
      status: 'available'
    });
  } catch (error) {
    console.error(`Error processing image:`, error);
    await mediaItem.update({ status: 'error' });
  }
};

// Process video files
const processVideo = async (mediaItem, filePath, fileSize) => {
  try {
    // Get video metadata using ffmpeg
    const metadata = await getVideoMetadata(filePath);
    
    // Create thumbnail
    const thumbnailPath = await createThumbnail(mediaItem.id, filePath);
    
    // Update the media item in the database
    await mediaItem.update({
      size: fileSize,
      duration: metadata.duration,
      dimensions: metadata.dimensions || null,
      mimeType: getMimeType(filePath),
      thumbnailPath: thumbnailPath,
      status: 'available'
    });
  } catch (error) {
    console.error(`Error processing video:`, error);
    await mediaItem.update({ status: 'error' });
  }
};

// Process other types of files
const processOtherFile = async (mediaItem, filePath, fileSize, mimeType) => {
  try {
    // For other files, just update the size and MIME type
    await mediaItem.update({
      size: fileSize,
      mimeType: mimeType,
      status: 'available'
    });
  } catch (error) {
    console.error(`Error processing other file:`, error);
    await mediaItem.update({ status: 'error' });
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
const createThumbnail = (itemId, filePath) => {
  return new Promise((resolve, reject) => {
    // Create a thumbnail directory if it doesn't exist
    const thumbnailDir = path.join(process.cwd(), 'thumbnails');
    if (!fs.existsSync(thumbnailDir)) {
      fs.mkdirSync(thumbnailDir, { recursive: true });
    }

    // Define thumbnail path
    const thumbnailPath = path.join(thumbnailDir, `thumbnail_${itemId}.jpg`);

    // Generate thumbnail from video
    ffmpeg(filePath)
      .on('error', (err) => {
        console.error('Error creating thumbnail:', err);
        resolve(null); // Resolve with null if thumbnail creation fails
      })
      .on('end', () => {
        resolve(thumbnailPath);
      })
      .screenshots({
        timestamps: ['50%'], // Take screenshot at 50% of video duration
        filename: `thumbnail_${itemId}.jpg`,
        folder: thumbnailDir,
        size: '320x240'
      });
  });
};

// Export the function for use
export default { processMediaItem };