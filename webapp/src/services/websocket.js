import { useMediaStore } from '../stores/media';
import { useAdStore } from '../stores/ads';

const socketUrl = 'ws://localhost:3001';
let socket;

function connect() {
  socket = new WebSocket(socketUrl);

  socket.onopen = () => {
    console.log('WebSocket connected');
  };

  socket.onmessage = (event) => {
    const message = JSON.parse(event.data);
    const mediaStore = useMediaStore();
    const adStore = useAdStore();

    if (message.type === 'thumbnail-generated') {
      if (message.item.modelName === 'Ad') {
        adStore.updateItem(message.item);
      } else {
        mediaStore.updateItem(message.item);
      }
    } else if (message.type === 'thumbnail-error') {
      console.error(`Thumbnail generation failed for ${message.modelName} ${message.mediaId}:`, message.error);
    }
  };

  socket.onclose = () => {
    console.log('WebSocket disconnected. Reconnecting...');
    setTimeout(connect, 1000);
  };

  socket.onerror = (error) => {
    console.error('WebSocket error:', error);
    socket.close();
  };
}

export function initWebSocket() {
  if (!socket || socket.readyState === WebSocket.CLOSED) {
    connect();
  }
}
