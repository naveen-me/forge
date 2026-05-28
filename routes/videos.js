import express from 'express';
import { VideoGenerator } from '../services/videoGenerator.js';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import queueManager from '../services/videoQueueManager.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const router = express.Router();

// Initialize video generator (db passed per request)
let videoGenerator = null;

// Store for tracking video generation jobs
const jobs = new Map();

/**
 * Setup queue job execution handler
 */
export function setupVideoQueueHandler(db) {
  if (!videoGenerator || videoGenerator.db !== db) {
    videoGenerator = new VideoGenerator(db);
  }

  // Remove existing listeners to avoid duplicates if called multiple times
  queueManager.removeAllListeners('executeJob');

  queueManager.on('executeJob', async (job, resolve, reject) => {
    try {
      const { batches, presetId, customName, voiceName, profileId, setId } = job;
      const generatedVideos = [];

      // Find topic language if available
      const firstQuestion = (db.questions || []).find(q => q.id === (batches[0]?.[0]));
      const topicId = firstQuestion?.topic_id;
      const topic = topicId ? (db.topics || []).find(t => t.id === topicId) : null;
      const language = topic?.language;

      for (let i = 0; i < batches.length; i++) {
        // Check if job was cancelled
        if (job.cancelRequested) {
          reject(new Error('Job cancelled by user'));
          return;
        }

        const batch = batches[i];
        const videoName = customName 
          ? (batches.length > 1 ? `${customName}_Part${i + 1}` : customName)
          : null;

        // Generate video for this batch with inner progress tracking
        const result = await videoGenerator.generateVideo(batch, presetId, { 
          customName: videoName,
          voiceName,
          profileId,
          setId,
          language,
          onProgress: (pct) => {
            // Calculate overall progress including batches
            const batchWeight = 100 / batches.length;
            const overallPct = Math.round((i * batchWeight) + (pct * batchWeight / 100));
            queueManager.updateJobProgress(job.id, i, batches.length, overallPct);
          }
        });
        
        generatedVideos.push(result);

        // Update progress after batch completion
        queueManager.updateJobProgress(job.id, i + 1, batches.length);
      }

      job.generatedVideos = generatedVideos;
      resolve(generatedVideos);
    } catch (error) {
      reject(error);
    }
  });
}

/**
 * POST /api/videos/generate
 * Generate video (synchronous - waits for completion)
 */
router.post('/generate', async (req, res) => {
  try {
    const { db } = req.app.locals;
    if (!videoGenerator || videoGenerator.db !== db) {
      videoGenerator = new VideoGenerator(db);
    }
    const { questionIds, presetId, voiceName, profileId, customName, setId } = req.body;

    if (!questionIds || !Array.isArray(questionIds) || questionIds.length === 0) {
      return res.status(400).json({ error: 'questionIds array is required' });
    }

    if (!presetId) {
      return res.status(400).json({ error: 'presetId is required' });
    }

    // Find topic language if available
    const firstQuestion = (db.questions || []).find(q => q.id === questionIds[0]);
    const topicId = firstQuestion?.topic_id;
    const topic = topicId ? (db.topics || []).find(t => t.id === topicId) : null;
    const language = topic?.language;

    const result = await videoGenerator.generateVideo(questionIds, presetId, { voiceName, profileId, customName, setId, language });

    const filename = path.basename(result.path);

    res.json({
      success: true,
      filename: filename,
      path: result.path,
      duration: result.duration,
      questionCount: result.questionCount,
      customName: result.customName || filename,
      message: 'Video generated successfully'
    });

  } catch (error) {
    console.error('Generate video error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/videos/list
 * List all generated video files
 */
router.get('/list', (req, res) => {
  try {
    const { db } = req.app.locals;
    if (!videoGenerator || videoGenerator.db !== db) {
      videoGenerator = new VideoGenerator(db);
    }
    const videos = videoGenerator.listVideos();
    res.json(videos);

  } catch (error) {
    console.error('List videos error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * DELETE /api/videos/:filename
 */
router.delete('/:filename', async (req, res) => {
  try {
    const { filename } = req.params;
    const videosDir = path.join(__dirname, '../videos');
    const videoPath = path.join(videosDir, filename);
    
    if (filename.includes('..') || filename.includes('/') || filename.includes('\\')) {
      return res.status(400).json({ error: 'Invalid filename' });
    }
    
    if (!fs.existsSync(videoPath)) {
      return res.status(404).json({ error: 'Video not found' });
    }
    
    fs.unlinkSync(videoPath);
    res.json({ success: true, message: 'Video deleted' });
  } catch (error) {
    console.error('Delete video error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * Queue Endpoints
 */
router.post('/queue/add', async (req, res) => {
  try {
    const { db } = req.app.locals;
    setupVideoQueueHandler(db);

    const { questionIds, presetId, customName, batchSize, setId } = req.body;
    
    if (!questionIds || !Array.isArray(questionIds) || questionIds.length === 0) {
      return res.status(400).json({ error: 'questionIds array is required' });
    }
    
    if (!presetId) {
      return res.status(400).json({ error: 'presetId is required' });
    }

    const batches = [];
    const size = batchSize && batchSize > 0 ? batchSize : questionIds.length;
    
    for (let i = 0; i < questionIds.length; i += size) {
      batches.push(questionIds.slice(i, i + size));
    }

    const job = queueManager.addJob({
      questionIds,
      presetId,
      customName,
      batches,
      batchSize: size,
      setId
    });

    res.json({
      success: true,
      job: job,
      message: 'Job added to queue'
    });

  } catch (error) {
    console.error('Add to queue error:', error);
    res.status(500).json({ error: error.message });
  }
});

router.get('/queue/status', (req, res) => {
  try {
    const status = queueManager.getAllJobs();
    res.json(status);
  } catch (error) {
    console.error('Get queue status error:', error);
    res.status(500).json({ error: error.message });
  }
});

router.post('/queue/cancel/:jobId', (req, res) => {
  try {
    const { jobId } = req.params;
    const result = queueManager.cancelJob(jobId);
    
    if (!result.success) {
      return res.status(404).json({ error: result.message });
    }
    
    res.json({ success: true, message: result.message });
  } catch (error) {
    console.error('Cancel job error:', error);
    res.status(500).json({ error: error.message });
  }
});

router.delete('/queue/history', (req, res) => {
  try {
    queueManager.clearHistory();
    res.json({ success: true, message: 'History cleared' });
  } catch (error) {
    console.error('Clear history error:', error);
    res.status(500).json({ error: error.message });
  }
});

export default router;
