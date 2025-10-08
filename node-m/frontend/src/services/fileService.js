const fs = require('fs');
const path = require('path');
const { promisify } = require('util');
const ffmpeg = require('fluent-ffmpeg');
const ffmpegStatic = require('ffmpeg-static');

// Set ffmpeg path
ffmpeg.setFfmpegPath(ffmpegStatic);

const access = promisify(fs.access);
const stat = promisify(fs.stat);

class FileService {
  /**
   * Check if a file exists
   * @param {string} filePath
   * @returns {Promise<boolean>}
   */
  async fileExists(filePath) {
    try {
      await access(filePath, fs.constants.F_OK);
      return true;
    } catch (error) {
      return false;
    }
  }

  /**
   * Get file statistics
   * @param {string} filePath
   * @returns {Promise<fs.Stats>}
   */
  async getFileStats(filePath) {
    try {
      return await stat(filePath);
    } catch (error) {
      throw new Error(`Could not get file stats: ${error.message}`);
    }
  }

  /**
   * Extract video metadata using ffmpeg
   * @param {string} filePath
   * @returns {Promise<Object>}
   */
  async extractVideoMetadata(filePath) {
    // First check if file exists
    const exists = await this.fileExists(filePath);
    if (!exists) {
      throw new Error('File does not exist');
    }

    return new Promise((resolve, reject) => {
      ffmpeg.ffprobe(filePath, (err, metadata) => {
        if (err) {
          reject(new Error(`Could not extract video metadata: ${err.message}`));
          return;
        }

        // Extract relevant information
        const format = metadata.format;
        const streams = metadata.streams;

        // Find video stream
        const videoStream = streams.find(stream => stream.codec_type === 'video');
        const audioStream = streams.find(stream => stream.codec_type === 'audio');

        const result = {
          duration: Math.floor(format.duration || 0),
          bitrate: format.bit_rate,
          size: format.size,
          video: videoStream ? {
            codec: videoStream.codec_name,
            width: videoStream.width,
            height: videoStream.height,
            fps: videoStream.avg_frame_rate,
            bitrate: videoStream.bit_rate
          } : null,
          audio: audioStream ? {
            codec: audioStream.codec_name,
            channels: audioStream.channels,
            sampleRate: audioStream.sample_rate,
            bitrate: audioStream.bit_rate
          } : null
        };

        resolve(result);
      });
    });
  }

  /**
   * Extract image metadata
   * @param {string} filePath
   * @returns {Promise<Object>}
   */
  async extractImageMetadata(filePath) {
    // First check if file exists
    const exists = await this.fileExists(filePath);
    if (!exists) {
      throw new Error('File does not exist');
    }

    // For now, we'll just get basic file info
    // In the future, we could use a library like sharp for more detailed image metadata
    try {
      const stats = await this.getFileStats(filePath);
      return {
        size: stats.size,
        // We could add more image-specific metadata here if needed
      };
    } catch (error) {
      throw new Error(`Could not extract image metadata: ${error.message}`);
    }
  }

  /**
   * Determine file type based on extension
   * @param {string} fileName
   * @returns {string} 'video' or 'image'
   */
  getFileType(fileName) {
    const videoExtensions = [
      'mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm', 'm4v', 'mpg', 'mpeg', '3gp'
    ];

    const imageExtensions = [
      'jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'tiff', 'svg'
    ];

    const ext = path.extname(fileName).toLowerCase().substring(1);

    if (videoExtensions.includes(ext)) {
      return 'video';
    }

    if (imageExtensions.includes(ext)) {
      return 'image';
    }

    return 'unknown';
  }

  /**
   * Validate if file is supported
   * @param {string} fileName
   * @returns {boolean}
   */
  isSupportedFile(fileName) {
    return this.getFileType(fileName) !== 'unknown';
  }

  /**
   * Validate all media files in the library and mark missing ones
   * @param {Array} mediaItems
   * @returns {Promise<Array>} Media items with missing file status
   */
  async validateMediaFiles(mediaItems) {
    const validatedItems = [];

    for (const item of mediaItems) {
      const exists = await this.fileExists(item.filepath);
      validatedItems.push({
        ...item,
        fileExists: exists
      });
    }

    return validatedItems;
  }
}

export default new FileService();