const express = require('express');
const fs = require('fs');
const path = require('path');
const { MediaItem } = require('../models/MediaItem');
const { checkFileExists } = require('../utils/fileUtils');
const router = express.Router();

// Stream video file
router.get('/video/:id', async (req, res) => {
  try {
    const { id } = req.params;
    
    // Get file info from database
    const file = await MediaItem.findByPk(id);
    if (!file || file.type !== 'file') {
      return res.status(404).json({ error: 'File not found' });
    }
    
    // Check if file exists
    if (!checkFileExists(file.filePath)) {
      return res.status(404).json({ error: 'File source missing' });
    }
    
    const filePath = file.filePath;
    const stat = fs.statSync(filePath);
    const fileSize = stat.size;
    const range = req.headers.range;
    
    if (range) {
      // Handle partial content requests (for seeking in videos)
      const parts = range.replace(/bytes=/, "").split("-");
      const start = parseInt(parts[0], 10);
      const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
      const chunksize = (end - start) + 1;
      
      const fileStream = fs.createReadStream(filePath, { start, end });
      
      res.writeHead(206, {
        'Content-Range': `bytes ${start}-${end}/${fileSize}`,
        'Accept-Ranges': 'bytes',
        'Content-Length': chunksize,
        'Content-Type': file.mimeType || 'video/mp4',
      });
      
      fileStream.pipe(res);
    } else {
      // Full file request
      res.writeHead(200, {
        'Content-Length': fileSize,
        'Content-Type': file.mimeType || 'video/mp4',
      });
      
      fs.createReadStream(filePath).pipe(res);
    }
  } catch (error) {
    console.error('Error streaming video:', error);
    res.status(500).json({ error: 'Failed to stream video' });
  }
});

module.exports = router;