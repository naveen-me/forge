import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export class MediaManager {
  constructor(db) {
    this.db = db;
    this.mediaDir = path.join(__dirname, '../storage/media');
    this.imagesDir = path.join(this.mediaDir, 'images');
    this.videosDir = path.join(this.mediaDir, 'videos');
    this.thumbnailsDir = path.join(this.mediaDir, 'thumbnails');
    
    // Initialize media array in db if not exists
    if (!this.db.media) {
      this.db.media = [];
    }
  }
  
  /**
   * Save uploaded file and create database entry
   */
  async saveMedia(file) {
    try {
      const fileType = file.mimetype.startsWith('image/') ? 'image' : 'video';
      const targetDir = fileType === 'image' ? this.imagesDir : this.videosDir;
      
      // Generate unique filename
      const timestamp = Date.now();
      const ext = path.extname(file.originalname);
      const filename = `${timestamp}_${this.sanitizeFilename(file.originalname)}`;
      const filepath = path.join(targetDir, filename);
      
      // Move file to storage
      fs.renameSync(file.path, filepath);
      
      // Get file info
      const stats = fs.statSync(filepath);
      
      // Create media entry
      const mediaEntry = {
        id: `media_${timestamp}`,
        filename: filename,
        original_name: file.originalname,
        type: fileType,
        mimetype: file.mimetype,
        size: stats.size,
        url: `/storage/media/${fileType}s/${filename}`,
        path: filepath,
        created_at: new Date().toISOString()
      };
      
      // Get dimensions for images
      if (fileType === 'image') {
        mediaEntry.dimensions = await this.getImageDimensions(filepath);
      }
      
      // Add to database
      this.db.media.push(mediaEntry);
      
      // Save database
      this.saveDatabase();
      
      return mediaEntry;
      
    } catch (error) {
      console.error('Error saving media:', error);
      throw error;
    }
  }
  
  /**
   * Get all media files
   */
  listMedia(filters = {}) {
    let media = [...this.db.media];
    
    // Filter by type
    if (filters.type) {
      media = media.filter(m => m.type === filters.type);
    }
    
    // Search by name
    if (filters.search) {
      const search = filters.search.toLowerCase();
      media = media.filter(m => 
        m.filename.toLowerCase().includes(search) ||
        m.original_name.toLowerCase().includes(search)
      );
    }
    
    // Sort
    const sortBy = filters.sortBy || 'created_at';
    const sortOrder = filters.sortOrder || 'desc';
    
    media.sort((a, b) => {
      let aVal = a[sortBy];
      let bVal = b[sortBy];
      
      if (sortBy === 'created_at') {
        aVal = new Date(aVal).getTime();
        bVal = new Date(bVal).getTime();
      }
      
      if (sortOrder === 'asc') {
        return aVal > bVal ? 1 : -1;
      } else {
        return aVal < bVal ? 1 : -1;
      }
    });
    
    return media;
  }
  
  /**
   * Get single media by ID
   */
  getMedia(mediaId) {
    return this.db.media.find(m => m.id === mediaId);
  }
  
  /**
   * Update media metadata
   */
  updateMedia(mediaId, updates) {
    const index = this.db.media.findIndex(m => m.id === mediaId);
    
    if (index === -1) {
      throw new Error('Media not found');
    }
    
    // Allow updating: original_name
    if (updates.original_name) {
      this.db.media[index].original_name = updates.original_name;
    }
    
    this.db.media[index].updated_at = new Date().toISOString();
    
    this.saveDatabase();
    
    return this.db.media[index];
  }
  
  /**
   * Delete media file and database entry
   */
  deleteMedia(mediaId) {
    const index = this.db.media.findIndex(m => m.id === mediaId);
    
    if (index === -1) {
      throw new Error('Media not found');
    }
    
    const media = this.db.media[index];
    
    // Delete file
    if (fs.existsSync(media.path)) {
      fs.unlinkSync(media.path);
    }
    
    // Remove from database
    this.db.media.splice(index, 1);
    
    this.saveDatabase();
    
    return true;
  }
  
  /**
   * Get storage statistics
   */
  getStats() {
    const images = this.db.media.filter(m => m.type === 'image');
    const videos = this.db.media.filter(m => m.type === 'video');
    
    const totalSize = this.db.media.reduce((sum, m) => sum + m.size, 0);
    const imageSize = images.reduce((sum, m) => sum + m.size, 0);
    const videoSize = videos.reduce((sum, m) => sum + m.size, 0);
    
    return {
      total: this.db.media.length,
      images: images.length,
      videos: videos.length,
      totalSize: totalSize,
      imageSize: imageSize,
      videoSize: videoSize
    };
  }
  
  /**
   * Helper: Sanitize filename
   */
  sanitizeFilename(filename) {
    return filename
      .replace(/[^a-zA-Z0-9.-]/g, '_')
      .replace(/_+/g, '_')
      .toLowerCase();
  }
  
  /**
   * Helper: Get image dimensions (simplified)
   */
  async getImageDimensions(filepath) {
    // Simplified - would use a library like sharp or image-size in production
    return 'unknown';
  }
  
  /**
   * Helper: Save database to file (no-op — persistence handled by caller via persistDb)
   */
  saveDatabase() {
    // Data is stored in memory (this.db) and persisted via SQLite through persistDb()
    // called by the route handlers after each mutation.
  }
}
