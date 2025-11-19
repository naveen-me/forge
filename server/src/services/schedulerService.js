import Schedule from '../../models/Schedule.js';
import MediaItem from '../../models/MediaItem.js';
import Link from '../../models/Link.js';
import Ad from '../../models/Ad.js';
import Setting from '../../models/Setting.js';
import sequelize from '../database.js';
import { Op } from 'sequelize';
import { syncToOBS } from './taskService.js';

const BUFFER_LIST_FOLDER = '/media/buffer';

// Mapping of timezone names to their offsets in minutes (this is a simplified mapping)
const TIMEZONE_OFFSETS = {
  'UTC': 0,
  'GMT': 0,
  'EST': -300, // UTC-5
  'EDT': -240, // UTC-4
  'CST': -360, // UTC-6
  'CDT': -300, // UTC-5
  'MST': -420, // UTC-7
  'MDT': -360, // UTC-6
  'PST': -480, // UTC-8
  'PDT': -420, // UTC-7
  'IST': 330,  // UTC+5:30
  'JST': 540,  // UTC+9
  'CET': 60,   // UTC+1
  'CEST': 120, // UTC+2
  // Add more as needed
};

class SchedulerService {
  // Helper method to get timezone offset in minutes
  getTimezoneOffset(timezone) {
    return TIMEZONE_OFFSETS[timezone] || 0;
  }

  async getSchedule(channel_id, date, user_id = null) {
    // Get user's timezone if available
    let userTimezone = 'UTC'; // Default to UTC
    if (user_id) {
      const userSetting = await Setting.findOne({
        where: { userId: user_id }
      });
      if (userSetting && userSetting.timezone) {
        userTimezone = userSetting.timezone;
      }
    }

    // Convert the date string assuming it's in the user's timezone
    const dateStr = date.split('T')[0]; // Extract date part in YYYY-MM-DD format

    // Use the timezone offset to convert user's day to UTC times for database lookup
    // This requires calculating the actual timezone offset for the specific date
    // since some timezones have daylight saving changes
    let startOfDay, endOfDay;

    try {
      // Create date objects for the start and end of the user's day
      const startLocal = new Date(`${dateStr}T00:00:00`);
      const endLocal = new Date(`${dateStr}T23:59:59.999`);

      // Calculate the offset in milliseconds for the user's timezone
      // For this approach, we'll use a simple conversion based on the timezone offset mapping
      const offsetMinutes = this.getTimezoneOffset(userTimezone);
      const offsetMs = offsetMinutes * 60000;

      // Convert user's local times to UTC for database query
      startOfDay = new Date(startLocal.getTime() - offsetMs);
      endOfDay = new Date(endLocal.getTime() - offsetMs);
    } catch (error) {
      console.error("Error calculating timezone offsets:", error);
      // Fallback to default behavior - use the date as UTC
      startOfDay = new Date(`${dateStr}T00:00:00.000Z`);
      endOfDay = new Date(`${dateStr}T23:59:59.999Z`);
    }

    const scheduleItems = await Schedule.findAll({
      where: {
        channel_id,
        start_time: {
          [Op.between]: [startOfDay, endOfDay],
        },
      },
      order: [['order', 'ASC']],
    });

    // Eagerly load the associated item for each schedule entry
    const enrichedSchedule = await Promise.all(
      scheduleItems.map(async (item) => {
        const model = this.getModel(item.item_type);
        const associatedItem = await model.findByPk(item.item_id);
        // Use dataValues to get a plain object and attach the item to it
        const plainItem = item.get({ plain: true });
        plainItem.item = associatedItem ? associatedItem.get({ plain: true }) : null;
        return plainItem;
      })
    );

    return enrichedSchedule;
  }

  async recalculateSchedule(channel_id, fromOrder) {
    const t = await sequelize.transaction();
    try {
      const items = await Schedule.findAll({
        where: {
          channel_id,
          order: {
            [Op.gte]: fromOrder,
          },
        },
        order: [['order', 'ASC']],
        transaction: t,
        lock: t.LOCK.UPDATE,
      });

      let lastEndTime;
      if (fromOrder > 0) {
        const precedingItem = await Schedule.findOne({
          where: { channel_id, order: fromOrder - 1 },
          transaction: t,
        });
        if (precedingItem) {
          lastEndTime = new Date(precedingItem.end_time);
        } else {
          const startOfToday = new Date();
          startOfToday.setHours(0, 0, 0, 0);
          lastEndTime = startOfToday;
        }
      } else {
        const startOfToday = new Date();
        startOfToday.setHours(0, 0, 0, 0);
        lastEndTime = startOfToday;
      }

      for (const item of items) {
        const durationMs = item.duration * 1000;
        item.start_time = lastEndTime;
        item.end_time = new Date(lastEndTime.getTime() + durationMs);
        await item.save({ transaction: t });

        lastEndTime = item.end_time;
      }

      await t.commit();
    } catch (error) {
      await t.rollback();
      throw error;
    }
  }

  // Helper function to convert duration in HH:MM:SS format to seconds
  convertDurationToSeconds(duration) {
    if (typeof duration === 'number') {
      return duration; // If it's already in seconds, return as is
    }
    if (typeof duration === 'string') {
      // Check if it's in HH:MM:SS format
      const timeParts = duration.split(':');
      if (timeParts.length === 3) { // HH:MM:SS
        const [hours, minutes, seconds] = timeParts.map(Number);
        return (hours * 3600) + (minutes * 60) + seconds;
      } else if (timeParts.length === 2) { // MM:SS
        const [minutes, seconds] = timeParts.map(Number);
        return (minutes * 60) + seconds;
      }
    }
    // If format is unrecognized, return the original value or 0
    return typeof duration !== 'undefined' ? Number(duration) : 0;
  }

  // Method to add item at specific time with collision detection
  async addItem(channel_id, itemData) {
    const t = await sequelize.transaction();
    try {
      const { item_id, item_type, start_time, duration } = itemData;

      // If no start_time is provided, find the next available slot
      let finalStartTime;
      if (!start_time) {
        // Find the latest item to determine the default start time
        const latestItem = await Schedule.findOne({
          where: { channel_id },
          order: [['end_time', 'DESC']],
          transaction: t
        });

        if (latestItem) {
          // Use the end time of the latest item as the start time for the new item
          finalStartTime = new Date(latestItem.end_time);
        } else {
          // For new channel or empty schedule, start at beginning of today in UTC
          finalStartTime = new Date();
          // Set to UTC midnight to have consistent starting point
          finalStartTime.setUTCHours(0, 0, 0, 0);
        }
      } else {
        // Handle different date string formats to avoid timezone issues
        if (typeof start_time === 'string') {
          // If it's a date-only string like "YYYY-MM-DD", interpret as start of day in UTC
          if (/^\d{4}-\d{2}-\d{2}$/.test(start_time)) {
            finalStartTime = new Date(`${start_time}T00:00:00.000Z`);
          } else {
            // Otherwise, create date normally (this handles ISO strings with timezone info)
            finalStartTime = new Date(start_time);
          }
        } else {
          finalStartTime = new Date(start_time);
        }
      }

      const model = this.getModel(item_type);
      const media = await model.findByPk(item_id, { transaction: t });
      if (!media) throw new Error('Media item not found');

      const itemDurationSeconds = Math.round(this.convertDurationToSeconds(duration) || media.duration || 300);
      const finalEndTime = new Date(finalStartTime.getTime() + itemDurationSeconds * 1000);

      // Check for collisions with existing items
      const conflictingItem = await Schedule.findOne({
        where: {
          channel_id,
          [Op.or]: [
            // Check if the new time range overlaps with existing items
            {
              start_time: { [Op.lt]: finalEndTime },
              end_time: { [Op.gt]: finalStartTime }
            }
          ]
        },
        transaction: t
      });

      if (conflictingItem) {
        throw new Error('Schedule conflict: The selected time overlaps with another scheduled item');
      }

      const newItem = await Schedule.create({
        channel_id,
        item_id,
        item_type,
        start_time: finalStartTime,
        end_time: finalEndTime,
        duration: itemDurationSeconds,
        offset_time: itemData.offset_time || 0,
        // For individual movement approach, we don't use order-based system
        // Instead, we rely on time-based positioning
        order: await this.getNextOrderValue(channel_id, t)
      }, { transaction: t });

      await t.commit();
      return newItem;
    } catch (error) {
      await t.rollback();
      throw error;
    }
  }

  // Helper method to get the next order value for items
  async getNextOrderValue(channel_id, transaction) {
    const maxOrder = await Schedule.max('order', {
      where: { channel_id },
      transaction: transaction
    });
    return maxOrder ? maxOrder + 1 : 1;
  }

  // Method to create a gap (empty time slot) in the schedule
  async createGap(channel_id, start_time, duration) {
    const t = await sequelize.transaction();
    try {
      const startTime = new Date(start_time);
      const endTime = new Date(startTime.getTime() + (duration * 1000));

      // Check for collisions with existing items
      const conflictingItem = await Schedule.findOne({
        where: {
          channel_id,
          [Op.or]: [
            // Check if the new gap overlaps with existing items
            {
              start_time: { [Op.lt]: endTime },
              end_time: { [Op.gt]: startTime }
            }
          ]
        },
        transaction: t
      });

      if (conflictingItem) {
        throw new Error('Schedule conflict: The selected time for gap overlaps with another scheduled item');
      }

      // Create a gap item with special type
      const gapItem = await Schedule.create({
        channel_id,
        item_id: null, // No associated item
        item_type: 'gap', // Special type for gaps
        start_time: startTime,
        end_time: endTime,
        duration: duration,
        offset_time: 0,
        order: await this.getNextOrderValue(channel_id, t)
      }, { transaction: t });

      await t.commit();
      return gapItem;
    } catch (error) {
      await t.rollback();
      throw error;
    }
  }

  // Method to extend item duration with collision detection
  async extendItemDuration(channel_id, schedule_id, newDuration) {
    const t = await sequelize.transaction();
    try {
      const item = await Schedule.findByPk(schedule_id, { transaction: t });
      if (!item) throw new Error('Schedule item not found');

      // Validate duration is positive
      if (newDuration <= 0) {
        throw new Error('Duration must be greater than 0 seconds');
      }

      // Check if the new end time would conflict with other items
      const newEndTime = new Date(new Date(item.start_time).getTime() + (newDuration * 1000));

      // Check for collisions with other items
      const conflictingItem = await Schedule.findOne({
        where: {
          channel_id,
          id: { [Op.ne]: schedule_id }, // Exclude the item being updated
          [Op.or]: [
            // Check if the extended time range overlaps with existing items
            {
              start_time: { [Op.lt]: newEndTime },
              end_time: { [Op.gt]: item.start_time }
            }
          ]
        },
        transaction: t
      });

      if (conflictingItem) {
        throw new Error('Schedule conflict: Extending this item would overlap with another scheduled item');
      }

      // Update the item's duration and end time
      await item.update({
        duration: newDuration,
        end_time: newEndTime
      }, { transaction: t });

      await t.commit();
      return item;
    } catch (error) {
      await t.rollback();
      throw error;
    }
  }

  // Method to extend item by pushing next item forward (if it's not locked/gap)
  async extendItemByPushingNext(channel_id, schedule_id, extensionDuration) {
    const t = await sequelize.transaction();
    try {
      const item = await Schedule.findByPk(schedule_id, { transaction: t });
      if (!item) throw new Error('Schedule item not found');

      // Validate extension is positive
      if (extensionDuration <= 0) {
        throw new Error('Extension duration must be greater than 0 seconds');
      }

      // Find the next item in the timeline (if any)
      const nextItem = await Schedule.findOne({
        where: {
          channel_id,
          start_time: { [Op.gte]: item.end_time },
        },
        order: [['start_time', 'ASC']],
        transaction: t
      });

      if (!nextItem) {
        // No next item, just extend current item
        const newDuration = item.duration + extensionDuration;
        const newEndTime = new Date(new Date(item.start_time).getTime() + (newDuration * 1000));

        await item.update({
          duration: newDuration,
          end_time: newEndTime
        }, { transaction: t });

        await t.commit();
        return item;
      }

      // Check if we can push the next item
      const newEndTime = new Date(new Date(item.end_time).getTime() + (extensionDuration * 1000));

      // For now, only allow pushing if the next item is a gap or can be safely moved
      // In a real scenario, you might have "locked" items that can't be moved
      await item.update({
        duration: item.duration + extensionDuration,
        end_time: newEndTime
      }, { transaction: t });

      // Push the next item forward by the same duration
      const newNextStartTime = newEndTime;
      const newNextEndTime = new Date(newNextStartTime.getTime() + (nextItem.duration * 1000));

      await nextItem.update({
        start_time: newNextStartTime,
        end_time: newNextEndTime
      }, { transaction: t });

      await t.commit();
      return {
        updatedItem: item,
        pushedItem: nextItem
      };
    } catch (error) {
      await t.rollback();
      throw error;
    }
  }

  async updateItem(channel_id, schedule_id, updateData) {
    const t = await sequelize.transaction();
    try {
      const item = await Schedule.findByPk(schedule_id, { transaction: t });
      if (!item) throw new Error('Schedule item not found');

      // If duration is updated, convert it to seconds if necessary
      if (updateData.duration) {
        updateData.duration = this.convertDurationToSeconds(updateData.duration);
      }

      // If offset_time is updated, ensure it's a valid number
      if (updateData.offset_time !== undefined) {
        updateData.offset_time = parseInt(updateData.offset_time) || 0;
      }

      // Check for start_time updates and validate against collisions
      let updatedStartTime = item.start_time;
      if (updateData.start_time) {
        // Handle different date string formats to avoid timezone issues
        if (typeof updateData.start_time === 'string') {
          // If it's a date-only string like "YYYY-MM-DD", interpret as start of day in UTC
          if (/^\d{4}-\d{2}-\d{2}$/.test(updateData.start_time)) {
            updatedStartTime = new Date(`${updateData.start_time}T00:00:00.000Z`);
          } else {
            // Otherwise, create date normally (this handles ISO strings with timezone info)
            updatedStartTime = new Date(updateData.start_time);
          }
        } else {
          updatedStartTime = new Date(updateData.start_time);
        }

        // Check for collisions with other items
        const conflictingItem = await Schedule.findOne({
          where: {
            channel_id,
            id: { [Op.ne]: schedule_id }, // Exclude the item being updated
            [Op.or]: [
              // Check if the new time range overlaps with existing items
              {
                start_time: {
                  [Op.lt]: new Date(updatedStartTime.getTime() + (item.duration * 1000))
                },
                end_time: { [Op.gt]: updatedStartTime }
              },
              {
                start_time: { [Op.lt]: updatedStartTime },
                end_time: {
                  [Op.gt]: new Date(updatedStartTime.getTime() + (item.duration * 1000))
                }
              }
            ]
          },
          transaction: t
        });

        if (conflictingItem) {
          throw new Error('Schedule conflict: The selected time overlaps with another scheduled item');
        }
      }

      // Update the item with validation
      await item.update(updateData, { transaction: t });

      // If start_time was updated, recalculate the schedule for this specific item
      if (updateData.start_time) {
        // Calculate new end_time based on the new start_time and duration
        const newEndTime = new Date(new Date(updateData.start_time).getTime() + (item.duration * 1000));
        await item.update({
          start_time: new Date(updateData.start_time),
          end_time: newEndTime
        }, { transaction: t });
      }

      await t.commit();
      // Don't recalculate the entire schedule chain as we're using individual movement
      return item;
    } catch (error) {
      await t.rollback();
      throw error;
    }
  }

  async deleteItem(channel_id, schedule_id) {
    const t = await sequelize.transaction();
    try {
      const item = await Schedule.findByPk(schedule_id, { transaction: t });
      if (!item) throw new Error('Schedule item not found');

      await item.destroy({ transaction: t });

      await t.commit();
      // Don't recalculate the entire schedule chain for individual item deletion
    } catch (error) {
      await t.rollback();
      throw error;
    }
  }

  async updateScheduleOrder(channel_id, scheduleData) {
    const t = await sequelize.transaction();
    try {
      for (const item of scheduleData) {
        await Schedule.update(
          { order: item.order },
          { where: { id: item.id, channel_id }, transaction: t }
        );
      }
      await t.commit();
      
      if (scheduleData.length > 0) {
        const firstItemOrder = Math.min(...scheduleData.map(i => i.order));
        await this.recalculateSchedule(channel_id, firstItemOrder);
      }
    } catch (error) {
      await t.rollback();
      throw error;
    }
  }

  getModel(itemType) {
    switch (itemType) {
      case 'media':
        return MediaItem;
      case 'ad':
        return Ad;
      case 'link':
        return Link;
      default:
        throw new Error(`Unknown item type: ${itemType}`);
    }
  }

  /**
   * Fills gaps in the schedule with buffer content.
   */
  async fillGaps(channel_id, date) {
    const schedule = await this.getSchedule(channel_id, date);
    const bufferList = await MediaItem.findAll({
      where: {
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

    if (schedule.length > 0) {
        const firstItem = schedule[0];
        const initialGap = new Date(firstItem.start_time).getTime() - startOfToday.getTime();
        if (initialGap > 1000) {
            this.fillGapWithRandomContent(channel_id, startOfToday, initialGap, bufferList, -1);
        }
    }

    for (let i = 0; i < schedule.length; i++) {
      const currentItem = schedule[i];
      if (lastItem) {
        const gapDuration = new Date(currentItem.start_time).getTime() - new Date(lastItem.end_time).getTime();
        if (gapDuration > 1000) {
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
        const smallerItem = bufferList.find(item => item.duration * 1000 <= remainingGap);
        if (!smallerItem) break;
        continue;
      }

      await Schedule.create({
        channel_id,
        item_id: randomItem.id,
        item_type: 'media',
        start_time: currentStartTime,
        end_time: new Date(currentStartTime.getTime() + itemDuration),
        offset_time: 0, // Default offset for buffer content
        order: precedingOrder + 1,
      });

      currentStartTime = new Date(currentStartTime.getTime() + itemDuration);
      remainingGap -= itemDuration;
      precedingOrder++;
    }

    await this.recalculateSchedule(channel_id, new Date());
  }
}

export default new SchedulerService();
