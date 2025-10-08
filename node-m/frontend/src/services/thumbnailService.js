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
    this.thumbnailDir = path.join(__dirname, '..', '..', 'thumbnails');
    this.ensureThumbnailDir();
  }

  /**
   * Ensure thumbnail directory exists
   */
  async ensureThumbnailDir() {
    try {
      await access(this.thumbnailDir, fs.constants.F_OK);
    } catch (error) {
      // Directory doesn't exist, create it
      await mkdir(this.thumbnailDir, { recursive: true });
    }
  }

  /**
   * Generate thumbnail for video file
   * @param {string} filePath
   * @param {number} duration
   * @returns {Promise<string>} Path to thumbnail
   */
  async generateVideoThumbnail(filePath, duration) {
    // Validate file exists
    if (!await this.fileExists(filePath)) {
      throw new Error('File does not exist');
    }

    const timestamp = Math.floor(duration * 0.1); // 10% through the video
    const fileName = `${Date.now()}_${path.basename(filePath, path.extname(filePath))}.jpg`;
    const outputPath = path.join(this.thumbnailDir, fileName);

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
          console.log(`Video thumbnail generated for ${filePath}: ${path.join('thumbnails', fileName)}`);
          resolve(path.join('thumbnails', fileName));
        })
        .on('error', (err) => {
          console.error(`Error generating video thumbnail for ${filePath}: ${err.message}`);
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
    // Validate file exists
    if (!await this.fileExists(filePath)) {
      throw new Error('File does not exist');
    }

    const fileName = `${Date.now()}_${path.basename(filePath, path.extname(filePath))}_thumb.jpg`;
    const outputPath = path.join(this.thumbnailDir, fileName);

    try {
      await sharp(filePath)
        .resize(320, 240, {
          fit: 'inside',
          withoutEnlargement: true
        })
        .jpeg({ quality: 80 })
        .toFile(outputPath);

      console.log(`Image thumbnail generated for ${filePath}: ${path.join('thumbnails', fileName)}`);
      return path.join('thumbnails', fileName);
    } catch (error) {
      console.error(`Error generating image thumbnail for ${filePath}: ${error.message}`);
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
      // Check if it's in our thumbnails directory
      if (thumbnailPath.includes(this.thumbnailDir)) {
        await unlink(thumbnailPath);
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
    // Delete old thumbnail if it exists
    if (mediaItem.thumbnailPath) {
      await this.deleteThumbnail(mediaItem.thumbnailPath);
    }

    // Generate new thumbnail
    if (mediaItem.type === 'video') {
      return await this.generateVideoThumbnail(mediaItem.filepath, mediaItem.duration);
    } else if (mediaItem.type === 'image') {
      return await this.generateImageThumbnail(mediaItem.filepath);
    }

    return null;
  }
}

export default new ThumbnailService();