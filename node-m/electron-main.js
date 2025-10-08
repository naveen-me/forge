const { app, BrowserWindow, dialog, ipcMain } = require('electron');
const path = require('path');
const axios = require('axios');
const https = require('https');

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

  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:5173'); // Vite dev server
    mainWindow.webContents.openDevTools();
  } else {
    mainWindow.loadFile(path.join(__dirname, 'frontend/dist/index.html'));
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