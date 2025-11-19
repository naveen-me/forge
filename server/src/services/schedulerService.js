import Schedule from '../../models/Schedule.js';
import MediaItem from '../../models/MediaItem.js';
import Link from '../../models/Link.js';
import Ad from '../../models/Ad.js';
import sequelize from '../database.js';
import { Op } from 'sequelize';

const BUFFER_LIST_FOLDER = '/media/buffer';

class SchedulerService {
  async getSchedule(channel_id, date) {
    const startOfDay = new Date(date);
    startOfDay.setHours(0, 0, 0, 0);
    const endOfDay = new Date(date);
    endOfDay.setHours(23, 59, 59, 999);

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

  async addItem(channel_id, itemData) {
    const t = await sequelize.transaction();
    try {
      const { item_id, item_type, order, duration } = itemData;

      await Schedule.update(
        { order: sequelize.literal('`order` + 1') },
        {
          where: {
            channel_id,
            order: {
              [Op.gte]: order,
            },
          },
          transaction: t,
        }
      );

      let startTime;
      if (order > 0) {
        const precedingItem = await Schedule.findOne({
          where: { channel_id, order: order - 1 },
          transaction: t,
        });
        if (precedingItem) {
          startTime = new Date(precedingItem.end_time);
        } else {
          const startOfToday = new Date();
          startOfToday.setHours(0, 0, 0, 0);
          startTime = startOfToday;
        }
      } else {
        const startOfToday = new Date();
        startOfToday.setHours(0, 0, 0, 0);
        startTime = startOfToday;
      }

      const model = this.getModel(item_type);
      const media = await model.findByPk(item_id, { transaction: t });
      if (!media) throw new Error('Media item not found');

      const itemDurationSeconds = Math.round(duration || media.duration || 300);

      const newItem = await Schedule.create({
        channel_id,
        item_id,
        item_type,
        start_time: startTime,
        end_time: new Date(startTime.getTime() + itemDurationSeconds * 1000),
        duration: itemDurationSeconds,
        order,
      }, { transaction: t });

      await t.commit();
      await this.recalculateSchedule(channel_id, order);
      return newItem;
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

      // If duration is updated, we must update it in the schedule item
      if (updateData.duration) {
        item.duration = updateData.duration;
      }
      
      await item.update(updateData, { transaction: t });

      await t.commit();
      await this.recalculateSchedule(channel_id, item.order);
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

      const order = item.order;
      await item.destroy({ transaction: t });

      await Schedule.update(
        { order: sequelize.literal('`order` - 1') },
        {
          where: {
            channel_id,
            order: {
              [Op.gt]: order,
            },
          },
          transaction: t,
        }
      );

      await t.commit();
      await this.recalculateSchedule(channel_id, order);
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
