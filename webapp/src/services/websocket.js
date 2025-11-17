import { useMediaStore } from '../stores/media';
import { useAdStore } from '../stores/ads';

const socketUrl = `ws://${window.location.host}/ws`;
let socket = null;
let reconnectTimeout = null;

function connect() {
  // Clear any existing reconnect timeout
  if (reconnectTimeout) {
    clearTimeout(reconnectTimeout);
    reconnectTimeout = null;
  }

  socket = new WebSocket(socketUrl);

  socket.onopen = () => {
    console.log('WebSocket connected');
  };

  socket.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data);
      // Access stores only when needed to prevent potential timing issues
      if (message.type === 'thumbnail-generated') {
        if (message.item && message.item.modelName === 'Ad') {
          const adStore = useAdStore();
          adStore.updateItem(message.item);
        } else if (message.item && message.item.modelName === 'MediaItem') {
          const mediaStore = useMediaStore();
          mediaStore.updateItem(message.item);
        }
      } else if (message.type === 'thumbnail-error') {
        console.error(`Thumbnail generation failed for ${message.modelName} ${message.mediaId}:`, message.error);
      }
    } catch (error) {
      console.error('Error processing WebSocket message:', error);
    }
  };

  socket.onclose = () => {
    console.log('WebSocket disconnected. Reconnecting...');
    // Use a timeout to avoid immediate reconnection attempts
    reconnectTimeout = setTimeout(connect, 1000);
  };

  socket.onerror = (error) => {
    console.error('WebSocket error:', error);
  };
}

function disconnect() {
  if (reconnectTimeout) {
    clearTimeout(reconnectTimeout);
    reconnectTimeout = null;
  }
  if (socket) {
    socket.close();
    socket = null;
  }
}

export function initWebSocket() {
  if (!socket || socket.readyState === WebSocket.CLOSED) {
    connect();
  }
  return { disconnect };
}
