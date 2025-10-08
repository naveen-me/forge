const fs = require('fs');
const path = require('path');
const { promisify } = require('util');
const ffmpeg = require('fluent-ffmpeg');
const ffmpegStatic = require('ffmpeg-static');
const sharp = require('sharp');

// Set ffmpeg path
ffmpeg.setFfmpegPath(ffmpegStatic);

const access = promisify(fs.access);
const mkdir = promisify(fs.mkdir);
const unlink = promisify(fs.unlink);

class ThumbnailService {
  constructor() {
    // Use a directory that can be served by the web server
    // In development, use app path; in production, use dist folder
    try {
      const electron = require('electron');
      const isDev = !electron.app.isPackaged;
      this.thumbnailDir = isDev
        ? path.join(electron.app.getAppPath(), 'thumbnails')
        : path.join(__dirname, '..', '..', 'dist', 'thumbnails');
    } catch (error) {
      // Fallback when Electron is not available (e.g., during testing)
      this.thumbnailDir = path.join(__dirname, '..', '..', 'thumbnails');
    }
    console.log('Thumbnail directory:', this.thumbnailDir);
    this.ensureThumbnailDir();
  }

  /**
   * Ensure thumbnail directory exists
   */
  async ensureThumbnailDir() {
    try {
      await access(this.thumbnailDir, fs.constants.F_OK);
      console.log('Thumbnail directory exists');
    } catch (error) {
      console.log('Thumbnail directory does not exist, creating it');
      // Directory doesn't exist, create it
      await mkdir(this.thumbnailDir, { recursive: true });
      console.log('Thumbnail directory created');
    }
  }

  /**
   * Generate thumbnail for video file
   * @param {string} filePath
   * @param {number} duration
   * @returns {Promise<string>} Path to thumbnail
   */
  async generateVideoThumbnail(filePath, duration) {
    console.log('Generating video thumbnail for:', filePath);
    // Validate file exists
    if (!await this.fileExists(filePath)) {
      throw new Error('File does not exist');
    }

    const timestamp = Math.floor(duration * 0.1); // 10% through the video
    const fileName = `${Date.now()}_${path.basename(filePath, path.extname(filePath))}.jpg`;
    const outputPath = path.join(this.thumbnailDir, fileName);

    console.log('Thumbnail output path:', outputPath);

    return new Promise((resolve, reject) => {
      ffmpeg(filePath)
        .screenshots({
          count: 1,
          folder: this.thumbnailDir,
          filename: fileName,
          timemarks: [timestamp],
          size: '320x240' // Standard thumbnail size
        })
        .on('end', () => {
          console.log('Thumbnail generated successfully');
          resolve(outputPath);
        })
        .on('error', (err) => {
          console.error('Thumbnail generation error:', err);
          reject(new Error(`Could not generate thumbnail: ${err.message}`));
        });
    });
  }

  /**
   * For images, we'll create a thumbnail version using sharp
   * @param {string} filePath
   * @returns {Promise<string>} Path to thumbnail
   */
  async generateImageThumbnail(filePath) {
    console.log('Generating image thumbnail for:', filePath);
    // Validate file exists
    if (!await this.fileExists(filePath)) {
      throw new Error('File does not exist');
    }

    const fileName = `${Date.now()}_${path.basename(filePath, path.extname(filePath))}_thumb.jpg`;
    const outputPath = path.join(this.thumbnailDir, fileName);

    console.log('Thumbnail output path:', outputPath);

    try {
      await sharp(filePath)
        .resize(320, 240, {
          fit: 'inside',
          withoutEnlargement: true
        })
        .jpeg({ quality: 80 })
        .toFile(outputPath);

      console.log('Thumbnail generated successfully');
      return outputPath;
    } catch (error) {
      console.error('Thumbnail generation error:', error);
      throw new Error(`Could not generate image thumbnail: ${error.message}`);
    }
  }

  /**
   * Delete thumbnail file
   * @param {string} thumbnailPath
   */
  async deleteThumbnail(thumbnailPath) {
    if (!thumbnailPath) return;

    try {
      // If it's a relative path, resolve it to the full path
      let fullPath = thumbnailPath;
      if (!path.isAbsolute(thumbnailPath)) {
        fullPath = path.join(this.thumbnailDir, thumbnailPath);
      }

      // Check if it's in our thumbnails directory
      if (fullPath.includes(this.thumbnailDir)) {
        await unlink(fullPath);
      }
    } catch (error) {
      console.warn(`Could not delete thumbnail: ${error.message}`);
    }
  }

  /**
   * Delete all thumbnails
   */
  async clearThumbnails() {
    try {
      const files = await fs.promises.readdir(this.thumbnailDir);
      for (const file of files) {
        const filePath = path.join(this.thumbnailDir, file);
        await unlink(filePath);
      }
    } catch (error) {
      console.warn(`Could not clear thumbnails: ${error.message}`);
    }
  }

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
   * Regenerate thumbnail for a media item
   * @param {Object} mediaItem
   * @returns {Promise<string>} Path to new thumbnail
   */
  async regenerateThumbnail(mediaItem) {
    console.log('Regenerating thumbnail for media item:', mediaItem);
    // Delete old thumbnail if it exists
    if (mediaItem.thumbnailPath) {
      await this.deleteThumbnail(mediaItem.thumbnailPath);
    }

    let newThumbnailPath = null;
    // Generate new thumbnail
    if (mediaItem.type === 'video') {
      newThumbnailPath = await this.generateVideoThumbnail(mediaItem.filepath, mediaItem.duration);
    } else if (mediaItem.type === 'image') {
      newThumbnailPath = await this.generateImageThumbnail(mediaItem.filepath);
    }

    // Convert to relative path
    if (newThumbnailPath && newThumbnailPath.startsWith(this.thumbnailDir)) {
      newThumbnailPath = path.relative(this.thumbnailDir, newThumbnailPath);
    }

    return newThumbnailPath;
  }
}

module.exports = new ThumbnailService();