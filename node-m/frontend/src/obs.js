// This file is maintained for backward compatibility but OBS operations
// are now handled by the worker system in tarva-engine-worker.js and tarva-engine-worker-manager.js

const { EventEmitter } = require('events');

class TarvaEngineService extends EventEmitter {
  constructor() {
    super();
  }

  // These methods are now handled by the worker system
  async connect() {
    // No-op - handled by worker
    return { success: true };
  }

  async disconnect() {
    // No-op - handled by worker
  }

  async addVideoSource() {
    // No-op - handled by worker
    return null;
  }

  async startVirtualCamera() {
    // No-op - handled by worker
  }

  async stopVirtualCamera() {
    // No-op - handled by worker
  }

  async getPreview() {
    // No-op - handled by worker
    return null;
  }
}

// Export a singleton instance
module.exports = new TarvaEngineService();