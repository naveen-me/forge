import OBSWebSocket from 'obs-websocket-js';
import dotenv from 'dotenv';
import api from './src/api.js';

dotenv.config();

const obs = new OBSWebSocket();
let currentItemId = null; // Track the currently playing item

async function connectToOBS() {
  try {
    await obs.connect(
      `ws://${process.env.OBS_WEBSOCKET_HOST}:${process.env.OBS_WEBSOCKET_PORT}`,
      process.env.OBS_WEBSOCKET_PASSWORD
    );
    console.log('Successfully connected to OBS WebSocket');
  } catch (error) {
    console.error('Failed to connect to OBS WebSocket:', error);
  }
}

/**
 * Executes a command and logs errors.
 */
async function executeOBSCommand(command, args) {
  try {
    await obs.call(command, args);
  } catch (error) {
    console.error(`Error executing OBS command '${command}':`, error.message);
  }
}

/**
 * Handles playing a media file.
 */
async function playMedia(item) {
  console.log(`Playing media: ${item.file_path}`);
  await executeOBSCommand('SetCurrentProgramScene', { sceneName: 'Media' });
  await executeOBSCommand('SetInputSettings', {
    inputName: 'MediaSource', // Assuming a source named 'MediaSource' exists
    inputSettings: {
      local_file: item.file_path,
    },
  });
}

/**
 * Handles displaying a web link.
 */
async function playLink(item) {
  console.log(`Displaying link: ${item.url}`);
  await executeOBSCommand('SetCurrentProgramScene', { sceneName: 'Link' });
  await executeOBSCommand('SetInputSettings', {
    inputName: 'BrowserSource', // Assuming a source named 'BrowserSource' exists
    inputSettings: {
      url: item.url,
    },
  });
}

/**
 * Main loop for the scheduler worker.
 */
async function runScheduler() {
  console.log('Scheduler worker is running...');

  setInterval(async () => {
    try {
      // Assuming channel_id 1 for now
      const response = await api.getSchedule(1, new Date());
      const schedule = response.data;

      const now = new Date();
      const currentItem = schedule.find(item => {
        const startTime = new Date(item.start_time);
        const endTime = new Date(item.end_time);
        return now >= startTime && now < endTime;
      });

      if (currentItem && currentItem.id !== currentItemId) {
        console.log(`New item detected: ${currentItem.item_type} (ID: ${currentItem.item_id})`);
        currentItemId = currentItem.id;

        // Fetch detailed info for the item
        let detail = null;
        if (currentItem.item_type === 'media' || currentItem.item_type === 'ad') {
            const mediaResponse = await api.getMediaItem(currentItem.item_id);
            detail = mediaResponse.data;
        } else if (currentItem.item_type === 'link') {
            const linkResponse = await api.getLink(currentItem.item_id);
            detail = linkResponse.data;
        }

        if (!detail) {
            console.error(`Could not fetch details for item ID ${currentItem.item_id}`);
            return;
        }

        // Execute the correct action based on type
        switch (currentItem.item_type) {
          case 'media':
          case 'ad': // Ads are treated as media for playback
            await playMedia(detail);
            break;
          case 'link':
            await playLink(detail);
            break;
          default:
            console.log(`Unknown item type: ${currentItem.item_type}`);
        }
      } else if (!currentItem && currentItemId !== null) {
        // No item is currently scheduled to play
        console.log('No current item. Switching to default scene.');
        await executeOBSCommand('SetCurrentProgramScene', { sceneName: 'Default' }); // Switch to a default/blank scene
        currentItemId = null;
      }
    } catch (error) {
      console.error('Error in scheduler loop:', error);
    }
  }, 1000); // Check every second
}

// Connect to OBS and start the scheduler
connectToOBS().then(runScheduler);
