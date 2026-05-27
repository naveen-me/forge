/**
 * Video Queue Manager
 * Manages video generation queue with cancellation support
 * Persists queue to file system for crash recovery
 */

import { EventEmitter } from 'events';
import { v4 as uuidv4 } from 'uuid';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

class VideoQueueManager extends EventEmitter {
  constructor() {
    super();
    this.queue = [];
    this.currentJob = null;
    this.isProcessing = false;
    this.jobHistory = new Map(); // Store completed/failed jobs
    this.persistencePath = path.join(__dirname, '../storage/queue-state.json');
    
    // Load persisted state on startup
    this.loadState();
  }

  /**
   * Load persisted state from disk
   */
  loadState() {
    try {
      if (fs.existsSync(this.persistencePath)) {
        const data = JSON.parse(fs.readFileSync(this.persistencePath, 'utf8'));
        
        // Restore queue (only jobs that were queued or processing)
        this.queue = data.queue || [];
        
        // If there was a job processing when server stopped, re-queue it
        if (data.currentJob && data.currentJob.status === 'processing') {
          data.currentJob.status = 'queued';
          data.currentJob.progress = 0;
          data.currentJob.completedBatches = 0;
          data.currentJob.startedAt = null;
          this.queue.unshift(data.currentJob); // Add to front of queue
        }
        
        // Restore history
        if (data.history && Array.isArray(data.history)) {
          this.jobHistory = new Map(data.history);
        }
        
        console.log(`✅ Queue state restored: ${this.queue.length} jobs in queue`);
        
        // Resume processing if there are jobs
        if (this.queue.length > 0) {
          console.log('🔄 Resuming queue processing...');
          setTimeout(() => this.processQueue(), 1000); // Delay to let server fully initialize
        }
      }
    } catch (error) {
      console.error('⚠️  Failed to load queue state:', error.message);
      // Continue with empty queue
    }
  }

  /**
   * Persist current state to disk
   */
  saveState() {
    try {
      const state = {
        queue: this.queue,
        currentJob: this.currentJob,
        isProcessing: this.isProcessing,
        history: Array.from(this.jobHistory.entries()),
        savedAt: new Date().toISOString()
      };
      
      // Ensure storage directory exists
      const storageDir = path.dirname(this.persistencePath);
      if (!fs.existsSync(storageDir)) {
        fs.mkdirSync(storageDir, { recursive: true });
      }
      
      fs.writeFileSync(this.persistencePath, JSON.stringify(state, null, 2), 'utf8');
    } catch (error) {
      console.error('⚠️  Failed to save queue state:', error.message);
    }
  }

  /**
   * Add a new job to the queue
   */
  addJob(jobData) {
    const job = {
      id: uuidv4(),
      ...jobData,
      status: 'queued',
      addedAt: new Date().toISOString(),
      startedAt: null,
      completedAt: null,
      error: null,
      progress: 0,
      totalBatches: jobData.batches?.length || 1,
      completedBatches: 0
    };

    this.queue.push(job);
    this.saveState(); // Persist to disk
    this.emit('jobAdded', job);
    
    // Start processing if not already running
    if (!this.isProcessing) {
      this.processQueue();
    }

    return job;
  }

  /**
   * Cancel a job (queued or running)
   */
  cancelJob(jobId) {
    // Check if it's the current job
    if (this.currentJob && this.currentJob.id === jobId) {
      this.currentJob.status = 'cancelled';
      this.currentJob.cancelRequested = true;
      this.currentJob.completedAt = new Date().toISOString();
      this.saveState();
      this.emit('jobCancelled', this.currentJob);
      return { success: true, message: 'Current job marked for cancellation' };
    }

    // Check if it's in the queue
    const index = this.queue.findIndex(job => job.id === jobId);
    if (index !== -1) {
      const job = this.queue.splice(index, 1)[0];
      job.status = 'cancelled';
      job.completedAt = new Date().toISOString();
      this.jobHistory.set(job.id, job);
      this.saveState();
      this.emit('jobCancelled', job);
      return { success: true, message: 'Job removed from queue' };
    }

    return { success: false, message: 'Job not found' };
  }

  /**
   * Get all jobs (queue + current + recent history)
   */
  getAllJobs() {
    const historyArray = Array.from(this.jobHistory.values())
      .sort((a, b) => new Date(b.completedAt) - new Date(a.completedAt))
      .slice(0, 20); // Last 20 completed/failed jobs

    return {
      current: this.currentJob,
      queue: this.queue,
      history: historyArray,
      isProcessing: this.isProcessing
    };
  }

  /**
   * Get job status by ID
   */
  getJobStatus(jobId) {
    if (this.currentJob && this.currentJob.id === jobId) {
      return this.currentJob;
    }

    const queuedJob = this.queue.find(j => j.id === jobId);
    if (queuedJob) return queuedJob;

    return this.jobHistory.get(jobId) || null;
  }

  /**
   * Update job progress
   */
  updateJobProgress(jobId, completedBatches, totalBatches, manualProgress = null) {
    if (this.currentJob && this.currentJob.id === jobId) {
      this.currentJob.completedBatches = completedBatches;
      if (manualProgress !== null) {
        this.currentJob.progress = manualProgress;
      } else {
        this.currentJob.progress = Math.round((completedBatches / totalBatches) * 100);
      }
      this.saveState(); // Persist progress
      this.emit('jobProgress', this.currentJob);
    }
  }

  /**
   * Process the queue
   */
  async processQueue() {
    if (this.isProcessing || this.queue.length === 0) {
      return;
    }

    this.isProcessing = true;
    this.saveState();

    while (this.queue.length > 0) {
      this.currentJob = this.queue.shift();
      this.currentJob.status = 'processing';
      this.currentJob.startedAt = new Date().toISOString();
      this.saveState(); // Save before starting job
      
      this.emit('jobStarted', this.currentJob);

      try {
        // The actual video generation will be handled by the caller
        // This manager just tracks the state
        await this.executeJob(this.currentJob);

        // Check if cancelled during execution
        if (this.currentJob.cancelRequested) {
          this.currentJob.status = 'cancelled';
        } else {
          this.currentJob.status = 'completed';
          this.currentJob.progress = 100;
        }
      } catch (error) {
        this.currentJob.status = 'failed';
        this.currentJob.error = error.message;
        this.emit('jobFailed', this.currentJob);
      }

      this.currentJob.completedAt = new Date().toISOString();
      this.jobHistory.set(this.currentJob.id, { ...this.currentJob });
      this.saveState(); // Save after job completion
      this.emit('jobCompleted', this.currentJob);
      
      this.currentJob = null;
    }

    this.isProcessing = false;
    this.saveState(); // Save when queue is empty
  }

  /**
   * Execute a job (to be implemented by caller via event)
   */
  async executeJob(job) {
    return new Promise((resolve, reject) => {
      // Emit event for external handler
      this.emit('executeJob', job, resolve, reject);
    });
  }

  /**
   * Clear completed jobs from history
   */
  clearHistory() {
    this.jobHistory.clear();
    this.saveState();
    this.emit('historyCleared');
  }
}

// Singleton instance
const queueManager = new VideoQueueManager();

export default queueManager;
