import Schedule from '../../models/Schedule.js';
import MediaItem from '../../models/MediaItem.js';
import { Op } from 'sequelize';

const BUFFER_LIST_FOLDER = '/media/buffer';

class SchedulerService {
  // ... (getSchedule, recalculateSchedule, addItem, updateItem, deleteItem)

  /**
   * Fills gaps in the schedule with buffer content.
   */
  async fillGaps(channel_id, date) {
    const schedule = await this.getSchedule(channel_id, date);
    const bufferList = await MediaItem.findAll({
      where: {
        // This assumes a 'path' attribute on the MediaItem model
        path: {
          [Op.like]: `${BUFFER_LIST_FOLDER}%`,
        },
      },
    });

    if (bufferList.length === 0) {
      console.log('Buffer list is empty. Cannot fill gaps.');
      return;
    }

    let lastItem = null;
    const startOfToday = new Date(date);
    startOfToday.setHours(0, 0, 0, 0);

    // Check for a gap at the beginning of the day
    if (schedule.length > 0) {
        const firstItem = schedule[0];
        const initialGap = new Date(firstItem.start_time).getTime() - startOfToday.getTime();
        if (initialGap > 1000) {
            // We have a gap to fill before the first scheduled item
            this.fillGapWithRandomContent(channel_id, startOfToday, initialGap, bufferList, -1);
        }
    }


    for (let i = 0; i < schedule.length; i++) {
      const currentItem = schedule[i];

      if (lastItem) {
        const gapDuration = new Date(currentItem.start_time).getTime() - new Date(lastItem.end_time).getTime();

        if (gapDuration > 1000) { // If there's more than a 1-second gap
          console.log(`Found a gap of ${gapDuration / 1000}s after item order ${lastItem.order}`);
          await this.fillGapWithRandomContent(channel_id, new Date(lastItem.end_time), gapDuration, bufferList, lastItem.order);
        }
      }
      lastItem = currentItem;
    }
  }

  async fillGapWithRandomContent(channel_id, gapStartTime, gapDuration, bufferList, precedingOrder) {
    let remainingGap = gapDuration;
    let currentStartTime = new Date(gapStartTime);

    while (remainingGap > 1000 && bufferList.length > 0) {
      const randomItem = bufferList[Math.floor(Math.random() * bufferList.length)];
      const itemDuration = randomItem.duration * 1000;

      if (itemDuration > remainingGap) {
        // Find a smaller item if possible, or break
        const smallerItem = bufferList.find(item => item.duration * 1000 <= remainingGap);
        if (!smallerItem) break; // No suitable item found
        continue; // Retry with a different random item
      }

      await Schedule.create({
        channel_id,
        item_id: randomItem.id,
        item_type: 'media', // Assuming buffer items are always media
        start_time: currentStartTime,
        end_time: new Date(currentStartTime.getTime() + itemDuration),
        order: precedingOrder + 1, // This will need re-ordering
      });

      currentStartTime = new Date(currentStartTime.getTime() + itemDuration);
      remainingGap -= itemDuration;
      precedingOrder++;
    }

    // After filling gaps, the order of subsequent items is messed up. Recalculate.
    await this.recalculateSchedule(channel_id, new Date());
  }
}

export default new SchedulerService();
