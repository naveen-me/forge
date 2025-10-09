const { contextBridge, ipcRenderer } = require('electron');

/**
 * A secure bridge between the renderer process (frontend) and the main process (Electron).
 * This exposes only a limited and controlled set of APIs to the frontend.
 */
contextBridge.exposeInMainWorld('electronAPI', {
  /**
   * Provides access to native dialogs.
   */
  dialogs: {
    /**
     * Shows a native open file dialog.
     * @param {import('electron').OpenDialogOptions} options - The options for the dialog.
     * @returns {Promise<import('electron').OpenDialogReturnValue>}
     */
    showOpenDialog: (options) => ipcRenderer.invoke('dialog:showOpenDialog', options),
  },

  /**
   * API calls to the local backend (Node.js server on port 3001)
   */
  api: {
    /**
     * Makes an API call to the local backend
     * @param {string} method - HTTP method (GET, POST, PUT, DELETE, etc.)
     * @param {string} url - API endpoint URL (will be prefixed with http://localhost:3001/api)
     * @param {Object} data - Request body data
     * @param {Object} headers - Request headers
     * @returns {Promise} Response from the backend
     */
    call: (method, url, data, headers) => ipcRenderer.invoke('api-call', { method, url, data, headers }),
  },

  /**
   * API calls to the PHP backend (matrixapi.io)
   */
  phpApi: {
    /**
     * Makes an API call to the PHP backend
     * @param {string} method - HTTP method (GET, POST, PUT, DELETE, etc.)
     * @param {string} action - Action to perform
     * @param {Object} data - Request body data
     * @returns {Promise} Response from the PHP backend
     */
    call: (method, action, data) => ipcRenderer.invoke('php-api-call', { method, action, data }),
  },

  /**
   * Provides a way to receive events from the main process.
   * This is more secure than exposing ipcRenderer directly.
   */
  receive: (channel, func) => {
    const validChannels = ['ad-item-updated', 'check-pin-required', 'tarva-engine-preview']; // Whitelist of valid channels
    if (validChannels.includes(channel)) {
      // Deliberately strip event as it includes `sender`
      ipcRenderer.on(channel, (event, ...args) => func(...args));
    }
  },

  /**
   * Send a message to the main process
   */
  send: (channel, ...args) => {
    const validChannels = ['show-main-window', 'pin-validated']; // Whitelist of valid channels
    if (validChannels.includes(channel)) {
      ipcRenderer.send(channel, ...args);
    } else {
      console.warn(`Unauthorized IPC channel: ${channel}`);
    }
  },

  /**
   * A function to remove a listener.
   */
  removeListener: (channel, func) => {
    ipcRenderer.removeListener(channel, func);
  },

  /**
   * A function to remove all listeners for a specific channel.
   */
  removeAllListeners: (channel) => {
    ipcRenderer.removeAllListeners(channel);
  },

  /**
   * Direct API methods for frequently used endpoints
   */
  getAllOverlays: () => ipcRenderer.invoke('get-all-overlays'),
  getAllAds: () => ipcRenderer.invoke('get-all-ads'),
  getMediaLibrary: () => ipcRenderer.invoke('get-media-library'),
  addVideoFile: () => ipcRenderer.invoke('add-video-file'),
  showOpenDialog: (options) => ipcRenderer.invoke('show-open-dialog', options),
});