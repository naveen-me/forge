import { WebSocketServer } from 'ws';
import { generateThumbnail } from '../../worker.js';
import OBSWebSocket from 'obs-websocket-js';
import Setting from '../../models/Setting.js';

let wss;
const jobQueue = [];
let isProcessing = false;
let obsSocket = new OBSWebSocket();

function setupWebSocket(server) {
  wss = new WebSocketServer({ server, path: '/ws' });

  wss.on('connection', ws => {
    console.log('Client connected');

    ws.on('message', async (message) => {
      try {
        const data = JSON.parse(message);

        // Handle OBS-related commands
        if (data.type === 'obs-command' && data.command) {
          await handleObsCommand(data.command, data.payload, ws);
        }

        // Handle other commands if needed
      } catch (error) {
        console.error('Error processing WebSocket message:', error);
      }
    });

    ws.on('close', () => {
      console.log('Client disconnected');
    });
  });
}

async function handleObsCommand(command, payload, ws) {
  try {
    // Get the current user's OBS settings
    // Note: This assumes we have a way to identify the user from WebSocket connection
    // which typically involves sending an authentication token after connection

    // For now, let's try to establish OBS connection with default or user-specific settings
    const userSetting = await Setting.findOne({
      where: { userId: 1 } // Temporary: using default user ID
    });

    const obsHost = userSetting?.obs_host || 'localhost';
    const obsPort = userSetting?.obs_port || 4455;
    const obsPassword = userSetting?.obs_password;

    // Attempt to connect to OBS if not already connected
    if (obsSocket && obsSocket.state === obsSocket.constructor.StateEnum.DISCONNECTED) {
      try {
        await obsSocket.connect({
          address: `${obsHost}:${obsPort}`,
          password: obsPassword
        });
        console.log(`Connected to OBS at ${obsHost}:${obsPort}`);
      } catch (error) {
        console.error('Failed to connect to OBS:', error);
        broadcast({ type: 'obs-error', message: error.message });
        return;
      }
    }

    // Execute the OBS command based on the payload
    switch (command) {
      case 'setCurrentScene':
        await obsSocket.call('SetCurrentScene', { sceneName: payload.sceneName });
        broadcast({ type: 'obs-scene-changed', sceneName: payload.sceneName });
        break;

      case 'startStream':
        await obsSocket.call('StartStream');
        broadcast({ type: 'obs-stream-started' });
        break;

      case 'stopStream':
        await obsSocket.call('StopStream');
        broadcast({ type: 'obs-stream-stopped' });
        break;

      case 'startRecording':
        await obsSocket.call('StartRecord');
        broadcast({ type: 'obs-recording-started' });
        break;

      case 'stopRecording':
        await obsSocket.call('StopRecord');
        broadcast({ type: 'obs-recording-stopped' });
        break;

      default:
        console.warn(`Unknown OBS command: ${command}`);
    }
  } catch (error) {
    console.error(`Error executing OBS command ${command}:`, error);
    broadcast({ type: 'obs-error', message: error.message });
  }
}

// Function to synchronize specific events to OBS
async function syncToOBS(eventData) {
  try {
    // Get user's OBS settings
    const userSetting = await Setting.findOne({
      where: { userId: 1 } // Temporary: using default user ID
    });

    if (!userSetting || !userSetting.auto_start_stream) {
      return; // Don't sync if auto-start is disabled
    }

    // Connect to OBS if not already connected
    if (obsSocket && obsSocket.state === obsSocket.constructor.StateEnum.DISCONNECTED) {
      const obsHost = userSetting.obs_host || 'localhost';
      const obsPort = userSetting.obs_port || 4455;
      const obsPassword = userSetting.obs_password;

      await obsSocket.connect({
        address: `${obsHost}:${obsPort}`,
        password: obsPassword
      });
    }

    // Change scene to the configured scene name
    await obsSocket.call('SetCurrentScene', { sceneName: userSetting.scene_name || 'Media Playout' });

    broadcast({
      type: 'obs-sync-success',
      message: `Switched to scene: ${userSetting.scene_name || 'Media Playout'}`
    });
  } catch (error) {
    console.error('Error syncing to OBS:', error);
    broadcast({ type: 'obs-sync-error', message: error.message });
  }
}

function broadcast(data) {
  if (!wss) return;
  wss.clients.forEach(client => {
    if (client.readyState === 1) { // 1 corresponds to WebSocket.OPEN
      client.send(JSON.stringify(data));
    }
  });
}

async function processQueue() {
  if (isProcessing || jobQueue.length === 0) {
    return;
  }

  isProcessing = true;
  const { mediaId, modelName } = jobQueue.shift();

  try {
    // Notify that processing has started
    broadcast({ type: 'thumbnail-processing', mediaId, modelName });

    const updatedItem = await generateThumbnail(mediaId, modelName);
    broadcast({ type: 'thumbnail-generated', item: updatedItem });
  } catch (error) {
    console.error(`Error processing job for ${modelName} ${mediaId}:`, error);
    broadcast({ type: 'thumbnail-error', mediaId, modelName, error: error.message });
  } finally {
    isProcessing = false;
    processQueue();
  }
}

function addThumbnailJob(mediaId, modelName) {
  jobQueue.push({ mediaId, modelName });
  processQueue();
}

export {
  setupWebSocket,
  addThumbnailJob,
  broadcast,
  syncToOBS
};