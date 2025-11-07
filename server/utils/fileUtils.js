const fs = require('fs');
const path = require('path');

/**
 * Check if a file exists at the given path
 * @param {string} filePath - Path to the file
 * @returns {boolean} - True if file exists, false otherwise
 */
function checkFileExists(filePath) {
  try {
    return fs.existsSync(filePath) && fs.statSync(filePath).isFile();
  } catch (error) {
    // If there's an error accessing the file, consider it missing
    return false;
  }
}

/**
 * Get file size in bytes
 * @param {string} filePath - Path to the file
 * @returns {number|null} - File size in bytes or null if file doesn't exist
 */
function getFileSize(filePath) {
  try {
    if (checkFileExists(filePath)) {
      const stats = fs.statSync(filePath);
      return stats.size;
    }
    return null;
  } catch (error) {
    return null;
  }
}

/**
 * Extract file extension from path
 * @param {string} filePath - Path to the file
 * @returns {string} - File extension or empty string
 */
function getFileExtension(filePath) {
  return path.extname(filePath).toLowerCase();
}

/**
 * Get MIME type based on file extension
 * @param {string} filePath - Path to the file
 * @returns {string} - MIME type
 */
function getMimeType(filePath) {
  const ext = getFileExtension(filePath);
  
  const mimeTypes = {
    '.mp4': 'video/mp4',
    '.mov': 'video/quicktime',
    '.avi': 'video/x-msvideo',
    '.mkv': 'video/x-matroska',
    '.wmv': 'video/x-ms-wmv',
    '.flv': 'video/x-flv',
    '.webm': 'video/webm',
    '.m4v': 'video/x-m4v',
    '.mp3': 'audio/mpeg',
    '.wav': 'audio/wav',
    '.flac': 'audio/flac',
    '.aac': 'audio/aac',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.png': 'image/png',
    '.gif': 'image/gif',
    '.bmp': 'image/bmp',
    '.webp': 'image/webp'
  };
  
  return mimeTypes[ext] || 'application/octet-stream';
}

module.exports = {
  checkFileExists,
  getFileSize,
  getFileExtension,
  getMimeType
};