const { Worker } = require('worker_threads');
const path = require('path');
const fs = require('fs');

class EnhancedOBSManager {
  constructor() {
    this.workers = new Map();
    this.workerCount = 0;
    this.mainWorker = null;
    this.backupWorker = null;
    this.currentWorker = null;
    this.isShuttingDown = false;
    this.workerReadyPromises = new Map(); // Track worker readiness
  }

  /**
   * Initialize the enhanced worker manager with configuration
   */
  async initialize(config, appPath) {
    this.config = config;
    this.appPath = appPath;

    try {
      // Create main worker with proper readiness tracking
      console.log('[ENHANCED] Creating main worker...');
      this.mainWorker = await this.createEnhancedWorker('main');
      console.log('[ENHANCED] Main worker created.');

      // Create backup worker
      console.log('[ENHANCED] Creating backup worker...');
      this.backupWorker = await this.createEnhancedWorker('backup');
      console.log('[ENHANCED] Backup worker created.');

      // Set main worker as current
      this.currentWorker = this.mainWorker;

      console.log('[ENHANCED] OBS worker manager initialized successfully');
      return true;
    } catch (error) {
      throw new Error(`Failed to initialize OBS manager: ${error.message}`);
    }
  }

  /**
   * Create an enhanced worker with proper readiness tracking
   */
  createEnhancedWorker(workerId) {
    return new Promise((resolve, reject) => {
      console.log(`[ENHANCED] Creating worker: ${workerId}`);
      const worker = new Worker(path.join(__dirname, '..', 'workers', 'obs-enhanced-worker.js'));

      const readyTimeout = setTimeout(() => {
        reject(new Error(`Worker ${workerId} failed to initialize within timeout`));
      }, 10000); // 10 second timeout

      const readyHandler = (message) => {
        if (message.type === 'worker-ready') {
          worker.removeListener('message', readyHandler);
          clearTimeout(readyTimeout);
          console.log(`[ENHANCED] Worker ${workerId} is ready`);
          resolve(worker);
        } else if (message.type === 'worker-error') {
          worker.removeListener('message', readyHandler);
          clearTimeout(readyTimeout);
          reject(new Error(`Worker ${workerId} failed to initialize: ${message.error}`));
        }
      };

      worker.on('message', readyHandler);

      worker.on('error', (error) => {
        console.error(`[ENHANCED] Worker ${workerId} error:`, error);
        this.handleWorkerError(workerId, error);
        reject(error);
      });

      worker.on('exit', (code) => {
        console.log(`[ENHANCED] Worker ${workerId} exited with code ${code}`);
        this.handleWorkerExit(workerId, code);
        if (code !== 0) {
          reject(new Error(`Worker ${workerId} exited with code ${code}`));
        }
      });

      this.workers.set(workerId, worker);
    });
  }

  /**
   * Handle messages from workers with enhanced logging
   */
  handleWorkerMessage(workerId, message) {
    // Forward messages to main process listeners
    if (this.messageCallback) {
      this.messageCallback(message);
    }

    // Handle specific message types with enhanced logging
    switch (message.type) {
      case 'obs-status':
        console.log(`[ENHANCED][${workerId}] OBS Status:`, message.status);
        break;
      case 'obs-connection':
        console.log(`[ENHANCED][${workerId}] OBS Connection:`, message.connected ? 'Connected' : 'Disconnected');
        break;
      case 'obs-error':
        console.error(`[ENHANCED][${workerId}] OBS Error:`, message.error);
        break;
      case 'obs-log':
        if (message.level === 'error') {
          console.error(`[ENHANCED][${workerId}] OBS Log:`, message.message);
        } else {
          console.log(`[ENHANCED][${workerId}] OBS Log:`, message.message);
        }
        break;
    }
  }

  /**
   * Handle worker errors with recovery
   */
  async handleWorkerError(workerId, error) {
    console.error(`[ENHANCED] Worker ${workerId} encountered an error:`, error);

    // If this was the current worker, switch to backup
    if (this.currentWorker === this.workers.get(workerId)) {
      console.log('[ENHANCED] Switching to backup worker due to error');
      await this.switchToBackupWorker();
    }

    // Attempt to recreate the failed worker
    await this.recreateWorker(workerId);
  }

  /**
   * Handle worker exit with recovery
   */
  async handleWorkerExit(workerId, code) {
    console.log(`[ENHANCED] Worker ${workerId} exited with code ${code}`);

    // Remove from workers map
    this.workers.delete(workerId);
    this.workerReadyPromises.delete(workerId);

    // If this was the current worker and we're not shutting down, switch to backup
    if (!this.isShuttingDown && this.currentWorker === this.workers.get(workerId)) {
      console.log('[ENHANCED] Switching to backup worker due to exit');
      await this.switchToBackupWorker();
    }

    // Recreate the worker if we're not shutting down
    if (!this.isShuttingDown) {
      await this.recreateWorker(workerId);
    }
  }

  /**
   * Recreate a failed worker
   */
  async recreateWorker(workerId) {
    try {
      console.log(`[ENHANCED] Recreating worker ${workerId}`);
      const newWorker = await this.createEnhancedWorker(workerId);

      if (workerId === 'main') {
        this.mainWorker = newWorker;
      } else if (workerId === 'backup') {
        this.backupWorker = newWorker;
      }

      console.log(`[ENHANCED] Successfully recreated worker ${workerId}`);
    } catch (error) {
      console.error(`[ENHANCED] Failed to recreate worker ${workerId}:`, error);
    }
  }

  /**
   * Switch to backup worker with validation
   */
  async switchToBackupWorker() {
    if (this.backupWorker && this.backupWorker.threadId) {
      this.currentWorker = this.backupWorker;
      console.log('[ENHANCED] Switched to backup worker');
      return true;
    } else {
      console.error('[ENHANCED] Backup worker is not available');
      return false;
    }
  }

  /**
   * Send message to current worker with error handling
   */
  sendMessageToWorker(message) {
    return new Promise((resolve, reject) => {
      if (this.currentWorker && this.currentWorker.threadId) {
        try {
          this.currentWorker.postMessage(message);
          resolve(true);
        } catch (error) {
          reject(new Error(`Failed to send message to worker: ${error.message}`));
        }
      } else {
        reject(new Error('No active worker available'));
      }
    });
  }

  /**
   * Start OBS with enhanced reliability
   */
  async startOBS() {
    if (!this.config) {
      throw new Error('Manager not initialized with config');
    }

    return this.sendMessageToWorker({
      type: 'start-obs',
      config: this.config,
      appPath: this.appPath
    });
  }

  /**
   * Connect to OBS with enhanced reliability
   */
  async connectOBS() {
    if (!this.config) {
      throw new Error('Manager not initialized with config');
    }

    return this.sendMessageToWorker({
      type: 'connect-obs',
      websocketConfig: this.config.obs.websocket
    });
  }

  /**
   * Stop OBS with enhanced reliability
   */
  async stopOBS() {
    return this.sendMessageToWorker({
      type: 'stop-obs'
    });
  }

  /**
   * Add video source with enhanced reliability
   */
  async addVideoSource(filePath) {
    return this.sendMessageToWorker({
      type: 'add-video-source',
      filePath
    });
  }

  /**
   * Start virtual camera with enhanced reliability
   */
  async startVirtualCamera() {
    return this.sendMessageToWorker({
      type: 'start-virtual-camera'
    });
  }

  /**
   * Stop virtual camera with enhanced reliability
   */
  async stopVirtualCamera() {
    return this.sendMessageToWorker({
      type: 'stop-virtual-camera'
    });
  }

  /**
   * Set callback for worker messages
   */
  onMessage(callback) {
    this.messageCallback = callback;
  }

  /**
   * Terminate all workers gracefully
   */
  async terminateAllWorkers() {
    console.log('[ENHANCED] Terminating all workers...');
    this.isShuttingDown = true;

    // Terminate all workers
    const terminationPromises = [];
    for (const [workerId, worker] of this.workers) {
      if (worker && worker.threadId) {
        terminationPromises.push(
          worker.terminate().catch(error => {
            console.error(`[ENHANCED] Error terminating worker ${workerId}:`, error);
          })
        );
      }
    }

    // Wait for all workers to terminate
    await Promise.allSettled(terminationPromises);

    this.workers.clear();
    this.workerReadyPromises.clear();
    this.currentWorker = null;
    this.mainWorker = null;
    this.backupWorker = null;

    console.log('[ENHANCED] All workers terminated');
  }
}

// Export singleton instance
module.exports = new EnhancedOBSManager();