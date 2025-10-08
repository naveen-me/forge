const { contextBridge, ipcRenderer } = require('electron')
const api = require('./api')

// Safe expose of IPC methods to renderer process
contextBridge.exposeInMainWorld('electronAPI', {
  showOpenDialog: (options) => ipcRenderer.invoke('show-open-dialog', options),
  // API call method to handle SSL certificates properly
  apiCall: (method, endpoint, data) => ipcRenderer.invoke('api-call', method, endpoint, data),
  // Database methods
  getMediaById: (mediaId) => ipcRenderer.invoke('db-get-media-by-id', mediaId),
  getMediaLibrary: () => ipcRenderer.invoke('db-get-media-library'),
  addMedia: (mediaData) => ipcRenderer.invoke('db-add-media', mediaData),
  addMediaFiles: (filePaths, folderId) => ipcRenderer.invoke('db-add-media-files', filePaths, folderId),
  updateMediaDisplayName: (mediaId, displayName) => ipcRenderer.invoke('db-update-media-display-name', mediaId, displayName),
  updateMediaFileName: (mediaId, newFileName) => ipcRenderer.invoke('db-update-media-filename', mediaId, newFileName),
  deleteMedia: (mediaId) => ipcRenderer.invoke('db-delete-media', mediaId),
  deleteMediaBulk: (mediaIds) => ipcRenderer.invoke('db-delete-media-bulk', mediaIds),
  regenerateThumbnail: (mediaId) => ipcRenderer.invoke('db-regenerate-thumbnail', mediaId),
  // Folder methods
  createFolder: (name, parentId) => ipcRenderer.invoke('folder-create', name, parentId),
  getFolders: () => ipcRenderer.invoke('folder-get-all'),
  getFolderById: (folderId) => ipcRenderer.invoke('folder-get-by-id', folderId),
  getFoldersTree: () => ipcRenderer.invoke('folder-get-tree'),
  updateFolderName: (folderId, name) => ipcRenderer.invoke('folder-update-name', folderId, name),
  renameFolderNew: (folderId, name) => ipcRenderer.invoke('folder-rename-new', folderId, name),
  deleteFolder: (folderId) => ipcRenderer.invoke('folder-delete', folderId),
  moveMediaToFolder: (mediaId, folderId) => ipcRenderer.invoke('folder-move-media', mediaId, folderId),
  moveMediaBulkToFolder: (mediaIds, folderId) => ipcRenderer.invoke('folder-move-media-bulk', mediaIds, folderId),
  // Overlay methods
  getAllOverlays: () => ipcRenderer.invoke('overlay-get-all'),
  createOverlay: (data) => ipcRenderer.invoke('overlay-create', data),
  updateOverlay: (id, data) => ipcRenderer.invoke('overlay-update', id, data),
  deleteOverlay: (id) => ipcRenderer.invoke('overlay-delete', id),
  groupOverlays: (overlayIds, groupName) => ipcRenderer.invoke('overlay-group', overlayIds, groupName),
  ungroupOverlays: (groupId) => ipcRenderer.invoke('overlay-ungroup', groupId),
  getOverlaysByGroupId: (groupId) => ipcRenderer.invoke('overlay-get-by-group-id', groupId),
  // System Defaults
  getAllDefaults: () => ipcRenderer.invoke('system-defaults-get-all'),
  setDefault: (key, value) => ipcRenderer.invoke('system-defaults-set', key, value),
  // Ad methods
  getAllAds: () => ipcRenderer.invoke('ad-get-all'),
  getAdById: (adId) => ipcRenderer.invoke('ad-get-by-id', adId),
  getAllAdGroups: () => ipcRenderer.invoke('ad-get-all-groups'),
  createAdGroup: (name) => ipcRenderer.invoke('ad-create-group', name),
  renameAdGroup: (id, newName) => ipcRenderer.invoke('ad-rename-group', id, newName),
  deleteAdGroup: (id) => ipcRenderer.invoke('ad-delete-group', id),
  getAdsByGroup: (adGroupId) => ipcRenderer.invoke('ad-get-by-group', adGroupId),
  getUnassignedAds: () => ipcRenderer.invoke('ad-get-unassigned'),
  addAdFiles: (filePaths, adGroupId) => ipcRenderer.invoke('ad-add-files', filePaths, adGroupId),
  deleteAd: (id) => ipcRenderer.invoke('ad-delete', id),
  updateAdSortOrder: (adId, sortOrder) => ipcRenderer.invoke('ad-update-sort-order', adId, sortOrder),
  moveAdToGroup: (adId, adGroupId) => ipcRenderer.invoke('ad-move-to-group', adId, adGroupId),
  regenerateAdThumbnail: (adId) => ipcRenderer.invoke('ad-regenerate-thumbnail', adId),
  // Scheduler methods
  getScheduleByDate: (date) => ipcRenderer.invoke('schedule-get-by-date', date),
  createSchedule: (scheduleData) => ipcRenderer.invoke('schedule-create', scheduleData),
  updateSchedule: (id, data) => ipcRenderer.invoke('schedule-update', { id, ...data }),
  deleteSchedule: (id) => ipcRenderer.invoke('schedule-delete', id),
  duplicateSchedule: (scheduleId, newDate) => ipcRenderer.invoke('schedule-duplicate', { scheduleId, newDate }),
  exportSchedule: (scheduleId) => ipcRenderer.invoke('schedule-export', scheduleId),
  importSchedule: (filePath, date) => ipcRenderer.invoke('schedule-import', { filePath, date }),
  reorderScheduleItems: (scheduleId, orderedIds) => ipcRenderer.invoke('schedule-items-reorder', { scheduleId, orderedIds }),
  createScheduleItem: (itemData) => ipcRenderer.invoke('schedule-item-create', itemData),
  updateScheduleItem: (itemData) => ipcRenderer.invoke('schedule-item-update', itemData),
  deleteScheduleItem: (id) => ipcRenderer.invoke('schedule-item-delete', id),
  // Schedule Item Ad Placement methods
  scheduleItemAdPlacementCreate: (adPlacementData) => ipcRenderer.invoke('schedule-item-ad-placement-create', adPlacementData),
  scheduleItemAdPlacementUpdate: (id, updateData) => ipcRenderer.invoke('schedule-item-ad-placement-update', { id, ...updateData }),
  scheduleItemAdPlacementDelete: (id) => ipcRenderer.invoke('schedule-item-ad-placement-delete', id),
  scheduleItemAdPlacementsGetByItem: (scheduleItemId) => ipcRenderer.invoke('schedule-item-ad-placements-get-by-item', scheduleItemId),
  // Missing files handling
  validateMediaFiles: () => ipcRenderer.invoke('validate-media-files'),
  fileExists: (filePath) => ipcRenderer.invoke('file-exists', filePath),
  cleanupMissingFiles: () => ipcRenderer.invoke('cleanup-missing-files'),
  // Auth methods
  saveAuth: (authData) => ipcRenderer.invoke('db-save-auth', authData),
  getAuth: () => ipcRenderer.invoke('db-get-auth'),
  clearAuth: () => ipcRenderer.invoke('db-clear-auth'),
  // Generic event listener
  on: (channel, callback) => ipcRenderer.on(channel, (_event, value) => callback(value)),
  send: (channel, data) => ipcRenderer.send(channel, data),
  getAppPath: () => ipcRenderer.invoke('get-app-path'),
  pathJoin: (...args) => ipcRenderer.invoke('path-join', ...args),

  // OBS API
  getObsStatus: () => api.default.getObsStatus(),
})