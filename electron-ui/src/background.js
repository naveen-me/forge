// background.js - Main Electron process file
// This is the entry point for the Electron main process

import { app, protocol, BrowserWindow, ipcMain } from 'electron'
import { createProtocol } from 'vue-cli-plugin-electron-builder/lib'
import installExtension, { VUEJS3_DEVTOOLS } from 'electron-devtools-installer'
import path from 'path'
import { autoUpdater } from 'electron-updater'

const isDevelopment = process.env.NODE_ENV !== 'production'

// Scheme must be registered before the app is ready
protocol.registerSchemesAsPrivileged([
  { scheme: 'app', privileges: { secure: true, standard: true } }
])

async function createWindow() {
  // Create the browser window.
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      // Use pluginOptions.nodeIntegration, leave this alone
      // See nklayman.github.io/vue-cli-plugin-electron-builder/guide/security.html#node-integration for more info
      nodeIntegration: process.env.ELECTRON_NODE_INTEGRATION,
      contextIsolation: !process.env.ELECTRON_NODE_INTEGRATION,
      preload: path.join(__dirname, 'preload.js')
    },
    icon: path.join(__dirname, '../public/icon.png')
  })

  if (process.env.WEBPACK_DEV_SERVER_URL) {
    // Load the url of the dev server if in development mode
    await win.loadURL(process.env.WEBPACK_DEV_SERVER_URL)
    if (!process.env.IS_TEST) win.webContents.openDevTools()
  } else {
    createProtocol('app')
    // Load the index.html when not in development
    win.loadURL('app://./index.html')
  }
}

// Quit when all windows are closed.
app.on('window-all-closed', () => {
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (BrowserWindow.getAllWindows().length === 0) createWindow()
})

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', async () => {
  if (isDevelopment && !process.env.IS_TEST) {
    // Install Vue Devtools
    try {
      await installExtension(VUEJS3_DEVTOOLS)
    } catch (e) {
      console.error('Vue Devtools failed to install:', e.toString())
    }
  }
  createWindow()
})

// Exit cleanly on request from parent process in development mode.
if (isDevelopment) {
  if (process.platform === 'win32') {
    process.on('message', (data) => {
      if (data === 'graceful-exit') {
        app.quit()
      }
    })
  } else {
    process.on('SIGTERM', () => {
      app.quit()
    })
  }
}

// IPC handlers for communication with renderer process
ipcMain.handle('load-schedule', async (event, schedulePath) => {
  try {
    const fs = require('fs');
    const scheduleData = fs.readFileSync(schedulePath, 'utf8');
    return { success: true, data: JSON.parse(scheduleData) };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

ipcMain.handle('save-schedule', async (event, schedulePath, scheduleData) => {
  try {
    const fs = require('fs');
    fs.writeFileSync(schedulePath, JSON.stringify(scheduleData, null, 2));
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

ipcMain.handle('validate-schedule', async (event, scheduleData) => {
  // In a real implementation, this would validate against the JSON schema
  try {
    // Basic validation
    if (!scheduleData.id || !scheduleData.segments) {
      return { success: false, error: 'Invalid schedule format' };
    }
    
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

// Playout engine communication (simulated)
let playoutEngineConnected = false;

ipcMain.handle('connect-engine', async () => {
  // Simulate connection to playout engine
  playoutEngineConnected = true;
  return { success: true };
});

ipcMain.handle('disconnect-engine', async () => {
  playoutEngineConnected = false;
  return { success: false };
});

ipcMain.handle('get-engine-status', async () => {
  return { connected: playoutEngineConnected };
});