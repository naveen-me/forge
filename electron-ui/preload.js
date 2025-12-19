const { contextBridge, ipcRenderer } = require('electron');

// Expose protected methods that allow the renderer process to use
// the ipcRenderer without exposing the entire object
contextBridge.exposeInMainWorld('electronAPI', {
  loadSchedule: (schedulePath) => ipcRenderer.invoke('load-schedule', schedulePath),
  saveSchedule: (schedulePath, scheduleData) => ipcRenderer.invoke('save-schedule', schedulePath, scheduleData),
  validateSchedule: (scheduleData) => ipcRenderer.invoke('validate-schedule', scheduleData),
  connectEngine: () => ipcRenderer.invoke('connect-engine'),
  disconnectEngine: () => ipcRenderer.invoke('disconnect-engine'),
  getEngineStatus: () => ipcRenderer.invoke('get-engine-status'),
  
  // Listen for events from main process
  onEngineStatusChange: (callback) => ipcRenderer.on('engine-status-change', callback),
  offEngineStatusChange: (callback) => ipcRenderer.removeListener('engine-status-change', callback),
});