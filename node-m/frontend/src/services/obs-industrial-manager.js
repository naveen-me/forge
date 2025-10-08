const { Worker } = require('worker_threads')
const path = require('path')
const fs = require('fs')

class IndustrialGradeOBSManager {
  constructor() {
    this.workers = new Map()
    this.workerCount = 0
    this.mainWorker = null
    this.backupWorker = null
    this.currentWorker = null
    this.healthCheckInterval = null
    this.recoveryAttempts = 0
    this.maxRecoveryAttempts = 3
  }

  /**
   * Initialize the industrial-grade OBS management system
   */
  async initialize(config, appPath) {
    this.config = config
    this.appPath = appPath

    try {
      // Create main worker with enhanced error handling
      this.mainWorker = await this.createEnhancedWorker('main')

      // Create backup worker for redundancy
      this.backupWorker = await this.createEnhancedWorker('backup')

      // Set main worker as current
      this.currentWorker = this.mainWorker

      // Start health monitoring
      this.startHealthMonitoring()

      return true
    } catch (error) {
      throw new Error(`Failed to initialize OBS manager: ${error.message}`)
    }
  }

  /**
   * Create an enhanced worker with robust error handling
   */
  createEnhancedWorker(workerId) {
    return new Promise((resolve, reject) => {
      try {
        const worker = new Worker(path.join(__dirname, '..', 'workers', 'obs-industrial-worker.js'))

        // Enhanced error handling
        worker.on('message', (message) => {
          this.handleWorkerMessage(workerId, message)
        })

        worker.on('error', (error) => {
          console.error(`[INDUSTRIAL] Worker ${workerId} error:`, error)
          this.handleWorkerError(workerId, error)
        })

        worker.on('exit', (code) => {
          console.log(`[INDUSTRIAL] Worker ${workerId} exited with code ${code}`)
          this.handleWorkerExit(workerId, code)
        })

        // Store worker reference
        this.workers.set(workerId, worker)

        // Wait for worker to be ready with timeout
        const readyTimeout = setTimeout(() => {
          reject(new Error(`Worker ${workerId} failed to initialize within timeout`))
        }, 5000)

        // Listen for ready message
        const readyHandler = (message) => {
          if (message.type === 'worker-ready') {
            worker.removeListener('message', readyHandler)
            clearTimeout(readyTimeout)
            resolve(worker)
          }
        }

        worker.on('message', readyHandler)

      } catch (error) {
        reject(new Error(`Failed to create worker ${workerId}: ${error.message}`))
      }
    })
  }

  /**
   * Enhanced message handling with logging
   */
  handleWorkerMessage(workerId, message) {
    // Forward messages to main process listeners
    if (this.messageCallback) {
      this.messageCallback(message)
    }

    // Handle specific message types with enhanced logging
    switch (message.type) {
      case 'obs-status':
        console.log(`[INDUSTRIAL][${workerId}] OBS Status:`, message.status)
        break
      case 'obs-connection':
        console.log(`[INDUSTRIAL][${workerId}] OBS Connection:`, message.connected ? 'Connected' : 'Disconnected')
        break
      case 'obs-error':
        console.error(`[INDUSTRIAL][${workerId}] OBS Error:`, message.error)
        break
      case 'obs-log':
        if (message.level === 'error') {
          console.error(`[INDUSTRIAL][${workerId}] OBS Log:`, message.message)
        } else {
          console.log(`[INDUSTRIAL][${workerId}] OBS Log:`, message.message)
        }
        break
    }
  }

  /**
   * Enhanced worker error handling with recovery
   */
  async handleWorkerError(workerId, error) {
    console.error(`[INDUSTRIAL] Worker ${workerId} encountered an error:`, error)

    // If this was the current worker, switch to backup
    if (this.currentWorker === this.workers.get(workerId)) {
      console.log('[INDUSTRIAL] Switching to backup worker due to error')
      await this.switchToBackupWorker()
    }

    // Attempt to recreate the failed worker
    await this.recreateWorker(workerId)
  }

  /**
   * Enhanced worker exit handling with recovery
   */
  async handleWorkerExit(workerId, code) {
    console.log(`[INDUSTRIAL] Worker ${workerId} exited with code ${code}`)

    // Remove from workers map
    this.workers.delete(workerId)

    // Clear references if they point to this worker
    if (this.currentWorker && this.currentWorker === this.workers.get(workerId)) {
      this.currentWorker = null
    }
    if (this.mainWorker && this.mainWorker === this.workers.get(workerId)) {
      this.mainWorker = null
    }
    if (this.backupWorker && this.backupWorker === this.workers.get(workerId)) {
      this.backupWorker = null
    }

    // Check if we're shutting down (healthCheckInterval is cleared during shutdown)
    const isShuttingDown = !this.healthCheckInterval

    // If this was the current worker, switch to backup
    // But only if we're not shutting down
    if (!isShuttingDown && this.currentWorker && this.currentWorker === this.workers.get(workerId)) {
      console.log('[INDUSTRIAL] Switching to backup worker due to exit')
      await this.switchToBackupWorker()
    }

    // Recreate the worker, but only if we're not shutting down
    if (!isShuttingDown) {
      await this.recreateWorker(workerId)
    }
  }

  /**
   * Recreate a failed worker
   */
  async recreateWorker(workerId) {
    if (this.recoveryAttempts < this.maxRecoveryAttempts) {
      try {
        console.log(`[INDUSTRIAL] Attempting to recreate worker ${workerId} (attempt ${this.recoveryAttempts + 1})`)
        this.recoveryAttempts++

        const newWorker = await this.createEnhancedWorker(workerId)

        if (workerId === 'main') {
          this.mainWorker = newWorker
        } else if (workerId === 'backup') {
          this.backupWorker = newWorker
        }

        // Reset recovery attempts on success
        this.recoveryAttempts = 0
        console.log(`[INDUSTRIAL] Successfully recreated worker ${workerId}`)
      } catch (error) {
        console.error(`[INDUSTRIAL] Failed to recreate worker ${workerId}:`, error)

        // If we've exhausted recovery attempts, shut down
        if (this.recoveryAttempts >= this.maxRecoveryAttempts) {
          console.error('[INDUSTRIAL] Maximum recovery attempts exceeded. Shutting down.')
          if (this.messageCallback) {
            this.messageCallback({
              type: 'system-error',
              error: 'Maximum recovery attempts exceeded. System is shutting down.'
            })
          }
        }
      }
    }
  }

  /**
   * Switch to backup worker with validation
   */
  async switchToBackupWorker() {
    if (this.backupWorker && this.backupWorker.threadId) {
      this.currentWorker = this.backupWorker
      console.log('[INDUSTRIAL] Switched to backup worker')

      // Reinitialize OBS on the backup worker
      await this.sendMessageToWorker({
        type: 'reinitialize-obs',
        config: this.config,
        appPath: this.appPath
      })

      return true
    } else {
      console.error('[INDUSTRIAL] Backup worker is not available')
      return false
    }
  }

  /**
   * Send message to current worker with error handling
   */
  sendMessageToWorker(message) {
    return new Promise((resolve, reject) => {
      // Check if we have a valid worker
      if (this.currentWorker && this.currentWorker.threadId) {
        try {
          // Set up response handler for critical messages
          if (message.type.includes('critical') || message.type.includes('obs')) {
            const responseHandler = (response) => {
              if (response.requestId === message.requestId) {
                this.currentWorker.removeListener('message', responseHandler)
                resolve(response)
              }
            }

            this.currentWorker.on('message', responseHandler)

            // Set timeout for response
            setTimeout(() => {
              // Check if the listener is still attached before removing
              if (this.currentWorker) {
                this.currentWorker.removeListener('message', responseHandler)
              }
              reject(new Error(`Timeout waiting for response to ${message.type}`))
            }, 10000)
          }

          this.currentWorker.postMessage(message)
          resolve(true)
        } catch (error) {
          reject(new Error(`Failed to send message to worker: ${error.message}`))
        }
      } else {
        // More descriptive error message
        const workerStatus = {
          hasCurrentWorker: !!this.currentWorker,
          hasMainWorker: !!this.mainWorker,
          hasBackupWorker: !!this.backupWorker,
          workerCount: this.workers.size
        };
        reject(new Error(`No active worker available. Status: ${JSON.stringify(workerStatus)}`))
      }
    })
  }

  /**
   * Start health monitoring
   */
  startHealthMonitoring() {
    // Clear any existing interval
    if (this.healthCheckInterval) {
      clearInterval(this.healthCheckInterval)
    }

    // Start periodic health checks
    this.healthCheckInterval = setInterval(async () => {
      try {
        await this.performHealthCheck()
      } catch (error) {
        console.error('[INDUSTRIAL] Health check failed:', error)
      }
    }, 30000) // Check every 30 seconds
  }

  /**
   * Perform system health check
   */
  async performHealthCheck() {
    // Send health check message to current worker
    await this.sendMessageToWorker({
      type: 'health-check',
      requestId: `health-${Date.now()}`
    })
  }

  /**
   * Start OBS with industrial-grade reliability
   */
  async startOBS() {
    if (!this.config) {
      throw new Error('Manager not initialized with config')
    }

    const requestId = `start-obs-${Date.now()}`

    return this.sendMessageToWorker({
      type: 'start-obs-critical',
      requestId,
      config: this.config,
      appPath: this.appPath
    })
  }

  /**
   * Connect to OBS with industrial-grade reliability
   */
  async connectOBS() {
    if (!this.config) {
      throw new Error('Manager not initialized with config')
    }

    const requestId = `connect-obs-${Date.now()}`

    return this.sendMessageToWorker({
      type: 'connect-obs-critical',
      requestId,
      websocketConfig: this.config.obs.websocket
    })
  }

  /**
   * Stop OBS with industrial-grade reliability
   */
  async stopOBS() {
    const requestId = `stop-obs-${Date.now()}`

    return this.sendMessageToWorker({
      type: 'stop-obs-critical',
      requestId
    })
  }

  /**
   * Add video source with industrial-grade reliability
   */
  async addVideoSource(filePath) {
    const requestId = `add-video-${Date.now()}`

    return this.sendMessageToWorker({
      type: 'add-video-source-critical',
      requestId,
      filePath
    })
  }

  /**
   * Start virtual camera with industrial-grade reliability
   */
  async startVirtualCamera() {
    const requestId = `start-vc-${Date.now()}`

    return this.sendMessageToWorker({
      type: 'start-virtual-camera-critical',
      requestId
    })
  }

  /**
   * Stop virtual camera with industrial-grade reliability
   */
  async stopVirtualCamera() {
    const requestId = `stop-vc-${Date.now()}`

    return this.sendMessageToWorker({
      type: 'stop-virtual-camera-critical',
      requestId
    })
  }

  /**
   * Set callback for worker messages
   */
  onMessage(callback) {
    this.messageCallback = callback
  }

  /**
   * Terminate all workers gracefully
   */
  async terminateAllWorkers() {
    console.log('[INDUSTRIAL] Terminating all workers...');

    // Clear health check interval
    if (this.healthCheckInterval) {
      clearInterval(this.healthCheckInterval)
      this.healthCheckInterval = null
    }

    // Terminate all workers
    const terminationPromises = [];
    for (const [workerId, worker] of this.workers) {
      terminationPromises.push(this.terminateWorker(workerId, worker));
    }

    // Wait for all workers to terminate
    await Promise.allSettled(terminationPromises);

    this.workers.clear()
    this.currentWorker = null
    this.mainWorker = null
    this.backupWorker = null

    console.log('[INDUSTRIAL] All workers terminated');
  }

  /**
   * Terminate a specific worker
   */
  async terminateWorker(workerId, worker) {
    try {
      if (worker) {
        console.log(`[INDUSTRIAL] Terminating worker ${workerId}...`);

        // Check if worker is still active
        if (worker.threadId) {
          try {
            // Send termination signal
            worker.postMessage({ type: 'terminate' });

            // Wait a bit for graceful shutdown
            await new Promise(resolve => setTimeout(resolve, 1000));
          } catch (postError) {
            console.warn(`[INDUSTRIAL] Could not send termination signal to worker ${workerId}:`, postError.message);
          }

          try {
            // Force terminate if still alive
            await worker.terminate();
            console.log(`[INDUSTRIAL] Worker ${workerId} force terminated`);
          } catch (terminateError) {
            console.warn(`[INDUSTRIAL] Error force terminating worker ${workerId}:`, terminateError.message);
          }
        } else {
          console.log(`[INDUSTRIAL] Worker ${workerId} already terminated`);
        }
      }
    } catch (error) {
      console.error(`[INDUSTRIAL] Error terminating worker ${workerId}:`, error);
    } finally {
      // Always remove the worker from our tracking, regardless of termination success
      this.workers.delete(workerId);
    }
  }
}

// Export singleton instance
module.exports = new IndustrialGradeOBSManager()