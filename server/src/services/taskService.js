import { WebSocketServer } from 'ws';
import { generateThumbnail } from '../../worker.js';

let wss;
const jobQueue = [];
let isProcessing = false;

function setupWebSocket(server) {
  wss = new WebSocketServer({ server, path: '/ws' });

  wss.on('connection', ws => {
    console.log('Client connected');
    ws.on('close', () => {
      console.log('Client disconnected');
    });
  });
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
  broadcast 
};