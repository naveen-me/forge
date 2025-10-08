const { Worker } = require('worker_threads');
const path = require('path');
const fs = require('fs');

class OBSWorkerManager {
  constructor() {
    this.workers = new Map(); // Store workers by ID
    this.workerCount = 0;
    this.mainWorker = null;
    this.backupWorker = null;
    this.currentWorker = null;
    this.isShuttingDown = false;
  }

  /**
   * Initialize the worker manager with configuration
   */
  async initialize(config, appPath) {
    this.config = config;
    this.appPath = appPath;

    try {
      // Create main worker
      this.mainWorker = await this.createWorker('main');

      // Create backup worker
      this.backupWorker = await this.createWorker('backup');

      // Set main worker as current
      this.currentWorker = this.mainWorker;

      return true;
    } catch (error) {
      throw new Error(`Failed to initialize OBS worker manager: ${error.message}`);
    }
  }

  /**
   * Create a new worker with the specified ID
   */
  createWorker(workerId) {
    return new Promise((resolve, reject) => {
      console.log(`[WORKER] Creating worker: ${workerId}`);

      const worker = new Worker(path.join(__dirname, 'workers', 'obs-worker.js'));

      // Timeout for worker readiness
      const readyTimeout = setTimeout(() => {
        worker.terminate();
        reject(new Error(`Worker ${workerId} failed to initialize within timeout`));
      }, 15000); // 15 second timeout

      // Handle initial ready message
      const readyHandler = (message) => {
        if (message.type === 'obs-status' && message.status === 'launched') {
          worker.removeListener('message', readyHandler);
          clearTimeout(readyTimeout);
          console.log(`[WORKER] Worker ${workerId} is ready`);
          resolve(worker);
        } else if (message.type === 'obs-error') {
          worker.removeListener('message', readyHandler);
          clearTimeout(readyTimeout);
          worker.terminate();
          reject(new Error(`Worker ${workerId} failed to initialize: ${message.error}`));
        }
      };

      worker.on('message', readyHandler);

      worker.on('message', (message) => {
        this.handleWorkerMessage(workerId, message);
      });

      worker.on('error', (error) => {
        console.error(`[WORKER] Worker ${workerId} error:`, error);
        this.handleWorkerError(workerId, error);
        reject(error);
      });

      worker.on('exit', (code) => {
        console.log(`[WORKER] Worker ${workerId} exited with code ${code}`);
        this.handleWorkerExit(workerId, code);
        if (code !== 0 && !this.isShuttingDown) {
          reject(new Error(`Worker ${workerId} exited with code ${code}`));
        }
      });

      // Store worker reference
      this.workers.set(workerId, worker);
    });
  }

  /**
   * Handle messages from workers
   */
  handleWorkerMessage(workerId, message) {
    // Forward messages to main process listeners
    if (this.messageCallback) {
      this.messageCallback(message);
    }

    // Handle specific message types
    switch (message.type) {
      case 'obs-status':
        console.log(`[WORKER][${workerId}] OBS Status:`, message.status);
        break;
      case 'obs-connection':
        console.log(`[WORKER][${workerId}] OBS Connection:`, message.connected ? 'Connected' : 'Disconnected');
        break;
      case 'obs-error':
        console.error(`[WORKER][${workerId}] OBS Error:`, message.error);
        break;
      case 'obs-log':
        if (message.level === 'error') {
          console.error(`[WORKER][${workerId}] OBS Log:`, message.message);
        } else {
          console.log(`[WORKER][${workerId}] OBS Log:`, message.message);
        }
        break;
    }
  }

  /**
   * Handle worker errors
   */
  handleWorkerError(workerId, error) {
    console.error(`[WORKER] Worker ${workerId} encountered an error:`, error);

    // If this was the current worker, switch to backup
    if (this.currentWorker === this.workers.get(workerId)) {
      console.log('[WORKER] Switching to backup worker due to error');
      this.switchToBackupWorker();
    }
  }

  /**
   * Handle worker exit
   */
  handleWorkerExit(workerId, code) {
    console.log(`[WORKER] Worker ${workerId} exited with code ${code}`);

    // Remove from workers map
    this.workers.delete(workerId);

    // If this was the current worker and we're not shutting down, switch to backup
    if (!this.isShuttingDown && this.currentWorker === this.workers.get(workerId)) {
      console.log('[WORKER] Switching to backup worker due to exit');
      this.switchToBackupWorker();
    }

    // Recreate the worker if we're not shutting down
    if (!this.isShuttingDown) {
      this.recreateWorker(workerId);
    }
  }

  /**
   * Recreate a failed worker
   */
  async recreateWorker(workerId) {
    try {
      console.log(`[WORKER] Recreating worker ${workerId}`);
      const newWorker = await this.createWorker(workerId);

      if (workerId === 'main') {
        this.mainWorker = newWorker;
      } else if (workerId === 'backup') {
        this.backupWorker = newWorker;
      }

      console.log(`[WORKER] Successfully recreated worker ${workerId}`);
    } catch (error) {
      console.error(`[WORKER] Failed to recreate worker ${workerId}:`, error);
    }
  }

  /**
   * Switch to backup worker
   */
  switchToBackupWorker() {
    if (this.backupWorker && this.backupWorker.threadId) {
      this.currentWorker = this.backupWorker;
      console.log('[WORKER] Switched to backup worker');
      return true;
    } else {
      console.error('[WORKER] Backup worker is not available');
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
   * Start OBS using the worker
   */
  async startOBS() {
    if (!this.config) {
      throw new Error('Worker manager not initialized with config');
    }

    return this.sendMessageToWorker({
      type: 'start-obs',
      config: this.config,
      appPath: this.appPath
    });
  }

  /**
   * Connect to OBS using the worker with retry logic
   */
  async connectOBS(retries = 5, delay = 3000) {
    if (!this.config) {
      throw new Error('Worker manager not initialized with config');
    }

    for (let i = 0; i < retries; i++) {
      try {
        console.log(`[WORKER] Attempting to connect to OBS (attempt ${i + 1}/${retries})...`);
        await this.sendMessageToWorker({
          type: 'connect-obs',
          websocketConfig: this.config.obs.websocket
        });

        // Wait a bit to see if connection succeeds
        await new Promise(resolve => setTimeout(resolve, 2000));

        // If we get here, the connection attempt was sent
        return;
      } catch (error) {
        console.error(`[WORKER] Connection attempt ${i + 1} failed:`, error.message);
        if (i < retries - 1) {
          console.log(`[WORKER] Retrying in ${delay / 1000} seconds...`);
          await new Promise(resolve => setTimeout(resolve, delay));
        } else {
          throw new Error(`Failed to connect to OBS after ${retries} attempts: ${error.message}`);
        }
      }
    }
  }

  /**
   * Stop OBS using the worker
   */
  async stopOBS() {
    return this.sendMessageToWorker({
      type: 'stop-obs'
    });
  }

  /**
   * Add video source using the worker
   */
  async addVideoSource(filePath) {
    return this.sendMessageToWorker({
      type: 'add-video-source',
      filePath
    });
  }

  /**
   * Start virtual camera using the worker
   */
  async startVirtualCamera() {
    return this.sendMessageToWorker({
      type: 'start-virtual-camera'
    });
  }

  /**
   * Stop virtual camera using the worker
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
   * Terminate all workers
   */
  async terminateAllWorkers() {
    console.log('[WORKER] Terminating all workers...');
    this.isShuttingDown = true;

    const terminationPromises = [];
    for (const [workerId, worker] of this.workers) {
      if (worker && worker.threadId) {
        terminationPromises.push(
          worker.terminate().catch(error => {
            console.error(`[WORKER] Error terminating worker ${workerId}:`, error);
          })
        );
      }
    }

    // Wait for all workers to terminate
    await Promise.allSettled(terminationPromises);

    this.workers.clear();
    this.currentWorker = null;
    this.mainWorker = null;
    this.backupWorker = null;

    console.log('[WORKER] All workers terminated');
  }
}

// Export singleton instance
module.exports = new OBSWorkerManager();