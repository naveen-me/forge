const { app, BrowserWindow, dialog, ipcMain, protocol } = require('electron');
const path = require('path');
const axios = require('axios');
const https = require('https');

try {
    require('electron-reloader')(module);
} catch {}

let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });

  // Default to development mode if NODE_ENV is not explicitly set to 'production'
  // This means if NODE_ENV is undefined or set to anything other than 'production', use development
  const isDev = process.env.NODE_ENV !== 'production';
  
  if (isDev) {
    mainWindow.loadURL('http://localhost:5173'); // Vite dev server
    
    // Ensure dev tools open after content loads
    mainWindow.webContents.once('dom-ready', () => {
      // Add extra delay to ensure page is fully loaded before opening dev tools
      setTimeout(() => {
        mainWindow.webContents.openDevTools({ mode: 'detach' }); // Open in separate window to make sure it's visible
      }, 500);
    });
  } else {
    // In production, the dist folder is in the frontend subdirectory
    const distPath = path.join(__dirname, 'frontend', 'dist', 'index.html');
    const fs = require('fs');
    
    if (fs.existsSync(distPath)) {
      mainWindow.loadFile(distPath);
    } else {
      // If no pre-built file found, show error
      console.error('No production build found. Please run "npm run build" in the frontend directory first.');
      // Show error page instead of trying to load non-existent file
      mainWindow.loadURL(`data:text/html;charset=utf-8,<!DOCTYPE html>
        <html>
          <head><title>Build Required</title></head>
          <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
            <h2>Application Build Missing</h2>
            <p>Please build the frontend by running:</p>
            <code style="background: #f5f5f5; padding: 10px; border-radius: 4px;">cd frontend && npm run build</code>
            <p>Then restart the application.</p>
          </body>
        </html>`);
    }
  }
}

// IPC handlers for various operations
ipcMain.handle('dialog:showOpenDialog', async (event, options) => {
  const result = await dialog.showOpenDialog(mainWindow, options);
  return result;
});

// IPC handler to act as a proxy for backend API calls (to avoid CORS issues)
ipcMain.handle('api-call', async (event, { method, url, data, headers }) => {
  try {
    // Check if URL is undefined or null
    if (!url) {
      console.error('API Call Failed: URL is undefined or null');
      return { success: false, error: { message: 'URL is required' } };
    }
    
    // Construct the full URL for the local backend
    const fullUrl = url.startsWith('http') ? url : `http://localhost:3001/api${url}`;
    
    const response = await axios({
      method,
      url: fullUrl,
      data,
      headers: headers || {},
      // You can also forward headers if needed
    });
    return { success: true, data: response.data };
  } catch (error) {
    console.error('API Call Failed:', error.message);
    return { success: false, error: error.response?.data || { message: error.message } };
  }
});

// IPC handler for PHP backend API calls
ipcMain.handle('php-api-call', async (event, { method, action, data }) => {
  try {
    const agent = new https.Agent({
      rejectUnauthorized: false
    });

    // For PHP backend calls to matrixapi.io
    const response = await axios({
      method,
      url: 'https://matrixapi.io/api/v1/action',
      data: {
        action,
        ...data
      },
      headers: {
        'Content-Type': 'application/json'
      },
      httpsAgent: agent
    });
    return { success: true, data: response.data };
  } catch (error) {
    console.error('PHP API Call Failed:', error.message);
    return { success: false, error: error.response?.data || { message: error.message } };
  }
});

// Database methods - Media
ipcMain.handle('db-get-media-by-id', async (event, mediaId) => {
  // Make API call to backend
  try {
    const response = await axios.get(`http://localhost:3001/api/media/${mediaId}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching media by ID:', error.message);
    throw error;
  }
});

ipcMain.handle('db-get-media-library', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.get('http://localhost:3001/api/media');
    return response.data;
  } catch (error) {
    console.error('Error fetching media library:', error.message);
    throw error;
  }
});

ipcMain.handle('db-add-media', async (event, mediaData) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/media', mediaData);
    return response.data;
  } catch (error) {
    console.error('Error adding media:', error.message);
    throw error;
  }
});

ipcMain.handle('db-add-media-files', async (event, filePaths, folderId) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/media/add-files', { files: filePaths, folderId });
    return response.data;
  } catch (error) {
    console.error('Error adding media files:', error.message);
    throw error;
  }
});

ipcMain.handle('db-update-media-display-name', async (event, mediaId, displayName) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/media/${mediaId}`, { displayName });
    return response.data;
  } catch (error) {
    console.error('Error updating media display name:', error.message);
    throw error;
  }
});

ipcMain.handle('db-update-media-filename', async (event, mediaId, newFileName) => {
    // Make API call to backend
    try {
      const response = await axios.put(`http://localhost:3001/api/media/${mediaId}`, { filename: newFileName });
      return response.data;
    } catch (error) {
      console.error('Error updating media filename:', error.message);
      throw error;
    }
  });

ipcMain.handle('db-delete-media', async (event, mediaId) => {
  // Make API call to backend
  try {
    const response = await axios.delete(`http://localhost:3001/api/media/${mediaId}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting media:', error.message);
    throw error;
  }
});

ipcMain.handle('db-delete-media-bulk', async (event, mediaIds) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/media/bulk', { operation: 'delete', mediaIds });
    return response.data;
  } catch (error) {
    console.error('Error bulk deleting media:', error.message);
    throw error;
  }
});

ipcMain.handle('db-regenerate-thumbnail', async (event, mediaId) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/media/${mediaId}/regenerate-thumbnail`);
    return response.data;
  } catch (error) {
    console.error('Error regenerating thumbnail:', error.message);
    throw error;
  }
});

// Folder methods
ipcMain.handle('folder-create', async (event, name, parentId) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/folders', { name, parentId });
    return response.data;
  } catch (error) {
    console.error('Error creating folder:', error.message);
    throw error;
  }
});

ipcMain.handle('folder-get-all', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.get('http://localhost:3001/api/folders');
    return response.data;
  } catch (error) {
    console.error('Error getting all folders:', error.message);
    throw error;
  }
});

ipcMain.handle('folder-get-by-id', async (event, folderId) => {
  // Make API call to backend
  try {
    const response = await axios.get(`http://localhost:3001/api/folders/${folderId}`);
    return response.data;
  } catch (error) {
    console.error('Error getting folder by ID:', error.message);
    throw error;
  }
});

ipcMain.handle('folder-get-tree', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.get('http://localhost:3001/api/folders/tree');
    return response.data;
  } catch (error) {
    console.error('Error getting folder tree:', error.message);
    throw error;
  }
});

ipcMain.handle('folder-update-name', async (event, folderId, name) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/folders/${folderId}`, { name });
    return response.data;
  } catch (error) {
    console.error('Error updating folder name:', error.message);
    throw error;
  }
});

ipcMain.handle('folder-rename-new', async (event, folderId, name) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/folders/${folderId}`, { name });
    return response.data;
  } catch (error) {
    console.error('Error renaming folder:', error.message);
    throw error;
  }
});

ipcMain.handle('folder-delete', async (event, folderId) => {
  // Make API call to backend
  try {
    const response = await axios.delete(`http://localhost:3001/api/folders/${folderId}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting folder:', error.message);
    throw error;
  }
});

ipcMain.handle('folder-move-media', async (event, mediaId, folderId) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/media/${mediaId}`, { folderId });
    return response.data;
  } catch (error) {
    console.error('Error moving media to folder:', error.message);
    throw error;
  }
});

ipcMain.handle('folder-move-media-bulk', async (event, mediaIds, folderId) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/media/bulk', { operation: 'move', mediaIds, targetFolderId: folderId });
    return response.data;
  } catch (error) {
    console.error('Error bulk moving media to folder:', error.message);
    throw error;
  }
});

// Additional handlers
ipcMain.handle('validate-media-files', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/media/validate-files');
    return response.data;
  } catch (error) {
    console.error('Error validating media files:', error.message);
    throw error;
  }
});

ipcMain.handle('file-exists', async (event, filePath) => {
  // Check if file exists using Node.js fs module
  const fs = require('fs').promises;
  try {
    await fs.access(filePath);
    return true;
  } catch {
    return false;
  }
});

ipcMain.handle('cleanup-missing-files', async (event) => {
  // Make API call to backend to cleanup missing files
  try {
    // This would need to be implemented in the backend
    // For now, just return success
    return { success: true, message: 'Cleanup not implemented in this version' };
  } catch (error) {
    console.error('Error cleaning up missing files:', error.message);
    throw error;
  }
});

// Auth methods
ipcMain.handle('db-save-auth', async (event, authData) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/auth/save', authData);
    return response.data;
  } catch (error) {
    console.error('Error saving auth:', error.message);
    throw error;
  }
});

ipcMain.handle('db-get-auth', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.get('http://localhost:3001/api/auth');
    return response.data;
  } catch (error) {
    console.error('Error getting auth:', error.message);
    throw error;
  }
});

ipcMain.handle('db-clear-auth', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.delete('http://localhost:3001/api/auth');
    return response.data;
  } catch (error) {
    console.error('Error clearing auth:', error.message);
    throw error;
  }
});

// Overlay methods
ipcMain.handle('overlay-get-all', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.get('http://localhost:3001/api/overlays');
    return response.data;
  } catch (error) {
    console.error('Error getting overlays:', error.message);
    throw error;
  }
});

ipcMain.handle('overlay-create', async (event, data) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/overlays', data);
    return response.data;
  } catch (error) {
    console.error('Error creating overlay:', error.message);
    throw error;
  }
});

ipcMain.handle('overlay-update', async (event, id, data) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/overlays/${id}`, data);
    return response.data;
  } catch (error) {
    console.error('Error updating overlay:', error.message);
    throw error;
  }
});

ipcMain.handle('overlay-delete', async (event, id) => {
  // Make API call to backend
  try {
    const response = await axios.delete(`http://localhost:3001/api/overlays/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting overlay:', error.message);
    throw error;
  }
});

ipcMain.handle('overlay-group', async (event, overlayIds, groupName) => {
  // Make API call to backend - this endpoint might need to be added
  try {
    // This operation might need to be implemented server-side
    console.log('Grouping overlays:', overlayIds, 'with name:', groupName);
    return { success: true, message: 'Group operation needs backend implementation' };
  } catch (error) {
    console.error('Error grouping overlays:', error.message);
    throw error;
  }
});

ipcMain.handle('overlay-ungroup', async (event, groupId) => {
  // Make API call to backend - this endpoint might need to be added
  try {
    // This operation might need to be implemented server-side
    console.log('Ungrouping overlays with groupId:', groupId);
    return { success: true, message: 'Ungroup operation needs backend implementation' };
  } catch (error) {
    console.error('Error ungrouping overlays:', error.message);
    throw error;
  }
});

ipcMain.handle('overlay-get-by-group-id', async (event, groupId) => {
  // Make API call to backend - this endpoint might need to be added
  try {
    // This operation might need to be implemented server-side
    console.log('Getting overlays by group ID:', groupId);
    return [];
  } catch (error) {
    console.error('Error getting overlays by group ID:', error.message);
    throw error;
  }
});

// System Defaults
ipcMain.handle('system-defaults-get-all', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.get('http://localhost:3001/api/system-defaults');
    return response.data;
  } catch (error) {
    console.error('Error getting system defaults:', error.message);
    throw error;
  }
});

ipcMain.handle('system-defaults-get-by-id', async (event, id) => {
  // Make API call to backend
  try {
    const response = await axios.get(`http://localhost:3001/api/system-defaults/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error getting system default by ID:', error.message);
    throw error;
  }
});

ipcMain.handle('system-defaults-create', async (event, data) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/system-defaults', data);
    return response.data;
  } catch (error) {
    console.error('Error creating system default:', error.message);
    throw error;
  }
});

ipcMain.handle('system-defaults-update', async (event, { id, ...data }) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/system-defaults/${id}`, data);
    return response.data;
  } catch (error) {
    console.error('Error updating system default:', error.message);
    throw error;
  }
});

ipcMain.handle('system-defaults-delete', async (event, id) => {
  // Make API call to backend
  try {
    const response = await axios.delete(`http://localhost:3001/api/system-defaults/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting system default:', error.message);
    throw error;
  }
});

// Ad methods
ipcMain.handle('ad-get-all', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.get('http://localhost:3001/api/ads');
    return response.data;
  } catch (error) {
    console.error('Error getting all ads:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-get-by-id', async (event, adId) => {
  // Make API call to backend
  try {
    const response = await axios.get(`http://localhost:3001/api/ads/${adId}`);
    return response.data;
  } catch (error) {
    console.error('Error getting ad by ID:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-get-all-groups', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.get('http://localhost:3001/api/ads/ad-groups');
    return response.data;
  } catch (error) {
    console.error('Error getting all ad groups:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-create-group', async (event, name) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/ads/ad-groups', { name });
    return response.data;
  } catch (error) {
    console.error('Error creating ad group:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-rename-group', async (event, id, newName) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/ads/ad-groups/${id}`, { name: newName });
    return response.data;
  } catch (error) {
    console.error('Error renaming ad group:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-delete-group', async (event, id) => {
  // Make API call to backend
  try {
    const response = await axios.delete(`http://localhost:3001/api/ads/ad-groups/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting ad group:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-get-by-group', async (event, adGroupId) => {
  // Make API call to backend
  try {
    const response = await axios.get(`http://localhost:3001/api/ads/group/${adGroupId}`);
    return response.data;
  } catch (error) {
    console.error('Error getting ads by group:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-get-unassigned', async (event) => {
  // Make API call to backend
  try {
    const response = await axios.get('http://localhost:3001/api/ads/unassigned');
    return response.data;
  } catch (error) {
    console.error('Error getting unassigned ads:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-add-files', async (event, filePaths, adGroupId) => {
  // First add files to media library
  try {
    const mediaResponse = await axios.post('http://localhost:3001/api/media/add-files', { 
      files: filePaths, 
      folderId: null 
    });
    
    // Then create ad records for each media item
    const adPromises = mediaResponse.data.map(async (mediaItem) => {
      // Convert media item to ad by creating an ad entry
      const adData = {
        displayName: mediaItem.displayName,
        filename: mediaItem.filename,
        filepath: mediaItem.filepath,
        thumbnailPath: mediaItem.thumbnailPath,
        duration: mediaItem.duration,
        adGroupId: adGroupId,
        status: mediaItem.status || 'ready'
      };
      
      const adResponse = await axios.post('http://localhost:3001/api/ads', adData);
      return adResponse.data;
    });
    
    const adResults = await Promise.all(adPromises);
    return adResults;
  } catch (error) {
    console.error('Error adding ad files:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-delete', async (event, id) => {
  // Make API call to backend
  try {
    const response = await axios.delete(`http://localhost:3001/api/ads/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting ad:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-update-sort-order', async (event, adId, sortOrder) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/ads/${adId}/sort-order`, { sortOrder });
    return response.data;
  } catch (error) {
    console.error('Error updating ad sort order:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-move-to-group', async (event, adId, adGroupId) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/ads/${adId}/group/${adGroupId}`);
    return response.data;
  } catch (error) {
    console.error('Error moving ad to group:', error.message);
    throw error;
  }
});

ipcMain.handle('ad-regenerate-thumbnail', async (event, adId) => {
  // Make API call to backend (using media endpoint since ads are media items)
  try {
    const response = await axios.put(`http://localhost:3001/api/media/${adId}/regenerate-thumbnail`);
    return response.data;
  } catch (error) {
    console.error('Error regenerating ad thumbnail:', error.message);
    throw error;
  }
});

// Scheduler methods
ipcMain.handle('schedule-get-by-date', async (event, date) => {
  // Make API call to backend
  try {
    const response = await axios.get(`http://localhost:3001/api/schedules/date/${date}`);
    return response.data;
  } catch (error) {
    console.error('Error getting schedule by date:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-create', async (event, scheduleData) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/schedules', scheduleData);
    return response.data;
  } catch (error) {
    console.error('Error creating schedule:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-update', async (event, { id, ...data }) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/schedules/${id}`, data);
    return response.data;
  } catch (error) {
    console.error('Error updating schedule:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-delete', async (event, id) => {
  // Make API call to backend
  try {
    const response = await axios.delete(`http://localhost:3001/api/schedules/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting schedule:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-duplicate', async (event, { scheduleId, newDate }) => {
  // Make API call to backend
  try {
    const response = await axios.post(`http://localhost:3001/api/schedules/${scheduleId}/duplicate`, { newDate });
    return response.data;
  } catch (error) {
    console.error('Error duplicating schedule:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-export', async (event, scheduleId) => {
  // Make API call to backend
  try {
    const response = await axios.post(`http://localhost:3001/api/schedules/${scheduleId}/export`);
    return response.data;
  } catch (error) {
    console.error('Error exporting schedule:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-import', async (event, { scheduleData, date }) => {
  // Make API call to backend
  try {
    const response = await axios.post('http://localhost:3001/api/schedules/import', { scheduleData, date });
    return response.data;
  } catch (error) {
    console.error('Error importing schedule:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-items-reorder', async (event, { scheduleId, orderedIds }) => {
  // Make API call to backend
  try {
    const response = await axios.post(`http://localhost:3001/api/schedules/${scheduleId}/items/reorder`, { orderedIds });
    return response.data;
  } catch (error) {
    console.error('Error reordering schedule items:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-item-create', async (event, { scheduleId, ...itemData }) => {
  // Make API call to backend
  try {
    const response = await axios.post(`http://localhost:3001/api/schedules/${scheduleId}/items`, itemData);
    return response.data;
  } catch (error) {
    console.error('Error creating schedule item:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-item-update', async (event, { id, scheduleId, ...itemData }) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/schedules/${scheduleId}/items/${id}`, itemData);
    return response.data;
  } catch (error) {
    console.error('Error updating schedule item:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-item-delete', async (event, { id, scheduleId }) => {
  // Make API call to backend
  try {
    const response = await axios.delete(`http://localhost:3001/api/schedules/${scheduleId}/items/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting schedule item:', error.message);
    throw error;
  }
});

// Schedule Item Ad Placement methods
ipcMain.handle('schedule-item-ad-placement-create', async (event, { scheduleItemId, ...adPlacementData }) => {
  // Make API call to backend
  try {
    // We need the scheduleId to make the API call - extract it from the scheduleItemId
    // For now, we'll assume the adPlacementData includes schedule-related info
    const scheduleId = adPlacementData.scheduleId;  // This should be passed from the frontend
    const response = await axios.post(`http://localhost:3001/api/schedules/${scheduleId}/items/${scheduleItemId}/adplacements`, adPlacementData);
    return response.data;
  } catch (error) {
    console.error('Error creating schedule item ad placement:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-item-ad-placement-update', async (event, { id, scheduleItemId, scheduleId, ...updateData }) => {
  // Make API call to backend
  try {
    const response = await axios.put(`http://localhost:3001/api/schedules/${scheduleId}/items/${scheduleItemId}/adplacements/${id}`, updateData);
    return response.data;
  } catch (error) {
    console.error('Error updating schedule item ad placement:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-item-ad-placement-delete', async (event, { id, scheduleItemId, scheduleId }) => {
  // Make API call to backend
  try {
    const response = await axios.delete(`http://localhost:3001/api/schedules/${scheduleId}/items/${scheduleItemId}/adplacements/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting schedule item ad placement:', error.message);
    throw error;
  }
});

ipcMain.handle('schedule-item-ad-placements-get-by-item', async (event, { scheduleItemId, scheduleId }) => {
  // Make API call to backend
  try {
    const response = await axios.get(`http://localhost:3001/api/schedules/${scheduleId}/items/${scheduleItemId}/adplacements`);
    return response.data;
  } catch (error) {
    console.error('Error getting schedule item ad placements:', error.message);
    throw error;
  }
});

// Path utilities
ipcMain.handle('get-app-path', async (event) => {
  return app.getAppPath();
});

ipcMain.handle('path-join', async (event, ...args) => {
  return path.join(...args);
});

// Direct API methods for components that need specific Electron functionality

// ControlPanel methods
ipcMain.handle('add-video-file', async (event) => {
  // Use the dialog handler we already have
  const result = await dialog.showOpenDialog(mainWindow, {
    properties: ['openFile'],
    filters: [
      { name: 'Video Files', extensions: ['mp4', 'avi', 'mov', 'mkv', 'wmv'] },
    ],
  });
  
  if (result.canceled) {
    return null;
  }
  
  return result.filePaths[0];
});

// PreviewPanel methods - virtual camera status events
// We need to add event registration methods to the electronAPI
// These will be handled through the preload script

// OverlayForm methods
ipcMain.handle('show-open-dialog', async (event, options) => {
  const result = await dialog.showOpenDialog(mainWindow, options || {});
  return result;
});

// ItemEditorModal methods
ipcMain.handle('get-all-overlays', async (event) => {
  try {
    const response = await axios.get('http://localhost:3001/api/overlays');
    return response.data;
  } catch (error) {
    console.error('Error getting all overlays:', error.message);
    throw error;
  }
});

ipcMain.handle('get-all-ads', async (event) => {
  try {
    const response = await axios.get('http://localhost:3001/api/ads');
    return response.data;
  } catch (error) {
    console.error('Error getting all ads:', error.message);
    throw error;
  }
});

ipcMain.handle('get-media-library', async (event) => {
  try {
    const response = await axios.get('http://localhost:3001/api/media');
    return response.data;
  } catch (error) {
    console.error('Error getting media library:', error.message);
    throw error;
  }
});

// IPC event listeners for main window control
ipcMain.on('show-main-window', () => {
  if (mainWindow) {
    mainWindow.show();
    mainWindow.focus();
  }
});

// Send events to renderer
function sendToRenderer(channel, ...args) {
  if (mainWindow && !mainWindow.isDestroyed()) {
    mainWindow.webContents.send(channel, ...args);
  }
}

app.whenReady().then(() => {
  protocol.registerFileProtocol('media', (request, callback) => {
    const url = request.url.substr(8); // remove 'media://'
    callback({ path: path.normalize(decodeURI(url)) });
  });

  createWindow();

  // Wait a bit for the page to load, then send the check-pin-required event
  mainWindow.webContents.once('dom-ready', () => {
    sendToRenderer('check-pin-required');
  });

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});