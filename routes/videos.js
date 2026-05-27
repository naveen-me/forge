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

// Flag to track if queue handler is registered
let queueHandlerRegistered = false;

// Setup queue job execution handler (called on first use)
function setupQueueHandler() {
  if (queueHandlerRegistered) return;
  queueHandlerRegistered = true;

  queueManager.on('executeJob', async (job, resolve, reject) => {
    try {
      // Ensure videoGenerator is initialized
      if (!videoGenerator) {
        reject(new Error('Video generator not initialized'));
        return;
      }

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
 *
 * Body:
 * {
 *   "questionIds": ["question_1", "question_2", ...],
 *   "presetId": "preset_123"
 * }
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

    console.log(`Generating video: ${questionIds.length} questions, preset: ${presetId}, name: ${customName || 'auto'}, setId: ${setId || 'none'}`);

    // Find topic language if available
    const firstQuestion = (db.questions || []).find(q => q.id === questionIds[0]);
    const topicId = firstQuestion?.topic_id;
    const topic = topicId ? (db.topics || []).find(t => t.id === topicId) : null;
    const language = topic?.language;

    // Generate video (wait for completion)
    const result = await videoGenerator.generateVideo(questionIds, presetId, { voiceName, profileId, customName, setId, language });

    // Extract filename from path
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
 * POST /api/videos/generate-from-topic
 * Generate video from a topic (synchronous - waits for completion)
 *
 * Body:
 * {
 *   "topicId": "topic_123",
 *   "presetId": "preset_123"
 * }
 */
router.post('/generate-from-topic', async (req, res) => {
  try {
    const { db } = req.app.locals;
    if (!videoGenerator || videoGenerator.db !== db) {
      videoGenerator = new VideoGenerator(db);
    }

    const { topicId, presetId, voiceName, profileId, maxQuestions, setId } = req.body;

    if (!topicId) {
      return res.status(400).json({ error: 'topicId is required' });
    }

    if (!presetId) {
      return res.status(400).json({ error: 'presetId is required' });
    }

    const topic = (db.topics || []).find(t => t.id === topicId);
    if (!topic) {
      return res.status(404).json({ error: 'Topic not found' });
    }

    let questionsInTopic = (db.questions || []).filter(q => q.topic_id === topicId);
    
    // Apply maxQuestions limit if specified
    if (maxQuestions && maxQuestions > 0) {
      questionsInTopic = questionsInTopic.slice(0, maxQuestions);
    }
    
    const questionIds = questionsInTopic.map(q => q.id);

    if (questionIds.length === 0) {
      return res.status(400).json({ error: 'No questions found in this topic' });
    }

    console.log(`Generating video from topic: ${topicId} (${questionIds.length} questions), preset: ${presetId}, setId: ${setId || 'none'}`);

    const result = await videoGenerator.generateVideo(questionIds, presetId, { voiceName, profileId, setId, language: topic.language });

    res.json({
      success: true,
      filename: result.customName || result.path.split('/').pop(),
      path: result.path,
      duration: result.duration,
      questionCount: result.questionCount,
      message: 'Video generated successfully from topic'
    });

  } catch (error) {
    console.error('Generate video from topic error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * POST /api/videos/generate-async
 * Start video generation (async - returns job ID)
 * 
 * Body:
 * {
 *   "questionIds": ["question_1", "question_2", ...],
 *   "presetId": "preset_123"
 * }
 */
router.post('/generate-async', async (req, res) => {
  try {
    const { db } = req.app.locals;
    if (!videoGenerator || videoGenerator.db !== db) {
      videoGenerator = new VideoGenerator(db);
    }
    const { questionIds, presetId, voiceName, profileId, setId } = req.body;
    
    if (!questionIds || !Array.isArray(questionIds) || questionIds.length === 0) {
      return res.status(400).json({ error: 'questionIds array is required' });
    }
    
    if (!presetId) {
      return res.status(400).json({ error: 'presetId is required' });
    }
    
    // Create job ID
    const jobId = 'job_' + Date.now();
    
    // Initialize job status
    jobs.set(jobId, {
      id: jobId,
      status: 'processing',
      progress: 0,
      questionCount: questionIds.length,
      createdAt: new Date().toISOString(),
      videoPath: null,
      error: null
    });
    
    // Find topic language if available
    const firstQuestion = (db.questions || []).find(q => q.id === questionIds[0]);
    const topicId = firstQuestion?.topic_id;
    const topic = topicId ? (db.topics || []).find(t => t.id === topicId) : null;
    const language = topic?.language;

    // Start video generation in background
    videoGenerator.generateVideo(questionIds, presetId, {
      voiceName,
      profileId,
      setId,
      language,
      onProgress: (pct) => {
        const cur = jobs.get(jobId);
        if (!cur || cur.status !== 'processing') return;
        jobs.set(jobId, { ...cur, progress: pct });
      }
    })
      .then(result => {
        jobs.set(jobId, {
          ...jobs.get(jobId),
          status: 'completed',
          progress: 100,
          videoPath: result.path,
          duration: result.duration,
          completedAt: new Date().toISOString()
        });
      })
      .catch(error => {
        jobs.set(jobId, {
          ...jobs.get(jobId),
          status: 'failed',
          error: error.message,
          failedAt: new Date().toISOString()
        });
      });
    
    // Return job ID immediately
    res.json({
      jobId,
      status: 'processing',
      message: 'Video generation started'
    });
    
  } catch (error) {
    console.error('Generate video error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/videos/job/:jobId
 * Get job status
 */
router.get('/job/:jobId', (req, res) => {
  try {
    const { jobId } = req.params;
    
    const job = jobs.get(jobId);
    
    if (!job) {
      return res.status(404).json({ error: 'Job not found' });
    }
    
    res.json(job);
    
  } catch (error) {
    console.error('Get job status error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/videos/download/:jobId
 * Download completed video
 */
router.get('/download/:jobId', (req, res) => {
  try {
    const { jobId } = req.params;
    
    const job = jobs.get(jobId);
    
    if (!job) {
      return res.status(404).json({ error: 'Job not found' });
    }
    
    if (job.status !== 'completed') {
      return res.status(400).json({ error: 'Video not ready yet', status: job.status });
    }
    
    if (!job.videoPath || !fs.existsSync(job.videoPath)) {
      return res.status(404).json({ error: 'Video file not found' });
    }
    
    // Send file
    res.download(job.videoPath, `video_${jobId}.webm`);
    
  } catch (error) {
    console.error('Download video error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/videos/stream/:jobId
 * Stream video for preview
 */
router.get('/stream/:jobId', (req, res) => {
  try {
    const { jobId } = req.params;
    
    const job = jobs.get(jobId);
    
    if (!job) {
      return res.status(404).json({ error: 'Job not found' });
    }
    
    if (job.status !== 'completed') {
      return res.status(400).json({ error: 'Video not ready yet' });
    }
    
    if (!job.videoPath || !fs.existsSync(job.videoPath)) {
      return res.status(404).json({ error: 'Video file not found' });
    }
    
    const stat = fs.statSync(job.videoPath);
    const fileSize = stat.size;
    const range = req.headers.range;
    
    if (range) {
      const parts = range.replace(/bytes=/, '').split('-');
      const start = parseInt(parts[0], 10);
      const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
      const chunksize = (end - start) + 1;
      const file = fs.createReadStream(job.videoPath, { start, end });
      const head = {
        'Content-Range': `bytes ${start}-${end}/${fileSize}`,
        'Accept-Ranges': 'bytes',
        'Content-Length': chunksize,
        'Content-Type': 'video/webm',
      };
      res.writeHead(206, head);
      file.pipe(res);
    } else {
      const head = {
        'Content-Length': fileSize,
        'Content-Type': 'video/webm',
      };
      res.writeHead(200, head);
      fs.createReadStream(job.videoPath).pipe(res);
    }
    
  } catch (error) {
    console.error('Stream video error:', error);
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
 * GET /api/videos/jobs
 * List all jobs
 */
router.get('/jobs', (req, res) => {
  try {
    const jobList = Array.from(jobs.values())
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    
    res.json(jobList);
    
  } catch (error) {
    console.error('List jobs error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * DELETE /api/videos/:filename
 * Delete a video file by filename
 */
router.delete('/:filename', async (req, res) => {
  try {
    const { filename } = req.params;
    const videosDir = path.join(__dirname, '../videos');
    const videoPath = path.join(videosDir, filename);
    
    // Security check: ensure filename doesn't contain path traversal
    if (filename.includes('..') || filename.includes('/') || filename.includes('\\\\')) {
      return res.status(400).json({ error: 'Invalid filename' });
    }
    
    // Check if file exists
    if (!fs.existsSync(videoPath)) {
      return res.status(404).json({ error: 'Video not found' });
    }
    
    // Try to delete file with retry logic (handles file locking on Windows)
    let deleted = false;
    let lastError = null;
    const maxRetries = 3;
    
    for (let i = 0; i < maxRetries; i++) {
      try {
        fs.unlinkSync(videoPath);
        deleted = true;
        break;
      } catch (error) {
        lastError = error;
        if (error.code === 'EBUSY' || error.code === 'EPERM') {
          // File is locked, wait and retry
          console.log(`File locked, retry ${i + 1}/${maxRetries}...`);
          await new Promise(resolve => setTimeout(resolve, 1000)); // Wait 1 second
        } else {
          // Other error, don't retry
          throw error;
        }
      }
    }
    
    if (!deleted) {
      throw lastError || new Error('Failed to delete file after retries');
    }
    
    res.json({ success: true, message: 'Video deleted' });
    
  } catch (error) {
    console.error('Delete video error:', error);
    
    // Provide more helpful error message for file locking
    if (error.code === 'EBUSY' || error.code === 'EPERM') {
      res.status(409).json({ 
        error: 'File is currently in use. Please close any applications using this video and try again.',
        code: error.code 
      });
    } else {
      res.status(500).json({ error: error.message });
    }
  }
});

/**
 * DELETE /api/videos/job/:jobId
 * Delete a video job
 */
router.delete('/job/:jobId', async (req, res) => {
  try {
    const { jobId } = req.params;
    
    const job = jobs.get(jobId);
    
    if (!job) {
      return res.status(404).json({ error: 'Job not found' });
    }
    
    // Delete video file if exists (with retry for locked files)
    if (job.videoPath && fs.existsSync(job.videoPath)) {
      let deleted = false;
      const maxRetries = 3;
      
      for (let i = 0; i < maxRetries; i++) {
        try {
          fs.unlinkSync(job.videoPath);
          deleted = true;
          break;
        } catch (error) {
          if (error.code === 'EBUSY' || error.code === 'EPERM') {
            console.log(`File locked, retry ${i + 1}/${maxRetries}...`);
            await new Promise(resolve => setTimeout(resolve, 1000));
          } else {
            throw error;
          }
        }
      }
      
      if (!deleted) {
        console.warn(`Could not delete video file ${job.videoPath} (file locked)`);
        // Continue anyway - remove job but leave file
      }
    }
    
    // Remove job from map
    jobs.delete(jobId);
    
    res.json({ success: true, message: 'Job deleted' });
    
  } catch (error) {
    console.error('Delete video error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * POST /api/videos/test
 * Generate a test video with sample questions
 */
router.post('/test', async (req, res) => {
  try {
    // Get first 3 questions from database
    const sampleQuestions = db.questions.slice(0, 3).map(q => q.id);
    
    // Get first preset
    const preset = db.presets[0];
    if (!preset) {
      return res.status(404).json({ error: 'No presets found' });
    }
    
    // Start generation
    const jobId = 'job_test_' + Date.now();
    
    jobs.set(jobId, {
      id: jobId,
      status: 'processing',
      progress: 0,
      questionCount: sampleQuestions.length,
      createdAt: new Date().toISOString(),
      videoPath: null,
      error: null
    });
    
    // Generate video
    videoGenerator.generateVideo(sampleQuestions, preset.id, {
      onProgress: (pct) => {
        const cur = jobs.get(jobId);
        if (!cur || cur.status !== 'processing') return;
        jobs.set(jobId, { ...cur, progress: pct });
      }
    })
      .then(result => {
        jobs.set(jobId, {
          ...jobs.get(jobId),
          status: 'completed',
          progress: 100,
          videoPath: result.path,
          duration: result.duration,
          completedAt: new Date().toISOString()
        });
      })
      .catch(error => {
        jobs.set(jobId, {
          ...jobs.get(jobId),
          status: 'failed',
          error: error.message,
          failedAt: new Date().toISOString()
        });
      });
    
    res.json({
      jobId,
      status: 'processing',
      message: 'Test video generation started',
      questionCount: sampleQuestions.length
    });
    
  } catch (error) {
    console.error('Test video error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * POST /api/videos/queue/add
 * Add video generation job to queue
 */
router.post('/queue/add', async (req, res) => {
  try {
    const { db } = req.app.locals;
    
    // Initialize video generator if not already done
    if (!videoGenerator || videoGenerator.db !== db) {
      videoGenerator = new VideoGenerator(db);
    }

    // Setup queue handler on first use
    setupQueueHandler();

    const { questionIds, presetId, customName, batchSize, setId } = req.body;
    
    if (!questionIds || !Array.isArray(questionIds) || questionIds.length === 0) {
      return res.status(400).json({ error: 'questionIds array is required' });
    }
    
    if (!presetId) {
      return res.status(400).json({ error: 'presetId is required' });
    }

    // Split into batches if needed
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

/**
 * GET /api/videos/queue/status
 * Get current queue status
 */
router.get('/queue/status', (req, res) => {
  try {
    const status = queueManager.getAllJobs();
    res.json(status);
  } catch (error) {
    console.error('Get queue status error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/videos/queue/job/:jobId
 * Get specific job status
 */
router.get('/queue/job/:jobId', (req, res) => {
  try {
    const { jobId } = req.params;
    const job = queueManager.getJobStatus(jobId);
    
    if (!job) {
      return res.status(404).json({ error: 'Job not found' });
    }
    
    res.json(job);
  } catch (error) {
    console.error('Get job status error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * POST /api/videos/queue/cancel/:jobId
 * Cancel a queued or running job
 */
router.post('/queue/cancel/:jobId', (req, res) => {
  try {
    const { jobId } = req.params;
    const result = queueManager.cancelJob(jobId);
    
    if (!result.success) {
      return res.status(404).json({ error: result.message });
    }
    
    res.json({
      success: true,
      message: result.message
    });
  } catch (error) {
    console.error('Cancel job error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * DELETE /api/videos/queue/history
 * Clear completed job history
 */
router.delete('/queue/history', (req, res) => {
  try {
    queueManager.clearHistory();
    res.json({
      success: true,
      message: 'History cleared'
    });
  } catch (error) {
    console.error('Clear history error:', error);
    res.status(500).json({ error: error.message });
  }
});

export default router;
