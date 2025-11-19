import express from 'express';
const router = express.Router();
import schedulerService from '../src/services/schedulerService.js';

// Get schedule for a specific channel and date
router.get('/:channel_id/:date', async (req, res) => {
  try {
    const schedule = await schedulerService.getSchedule(
      req.params.channel_id,
      req.params.date
    );
    res.json(schedule);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// Add an item to the schedule
router.post('/:channel_id', async (req, res) => {
  try {
    const newItem = await schedulerService.addItem(
      req.params.channel_id,
      req.body
    );
    res.status(201).json(newItem);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// Update a schedule item
router.put('/:channel_id/:schedule_id', async (req, res) => {
  try {
    await schedulerService.updateItem(
      req.params.channel_id,
      req.params.schedule_id,
      req.body
    );
    res.json({ message: 'Schedule item updated' });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// Extend schedule item duration
router.put('/:channel_id/:schedule_id/extend', async (req, res) => {
  try {
    const { newDuration } = req.body;
    const updatedItem = await schedulerService.extendItemDuration(
      req.params.channel_id,
      req.params.schedule_id,
      newDuration
    );
    res.json(updatedItem);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// Create a gap in the schedule
router.post('/:channel_id/gap', async (req, res) => {
  try {
    const { start_time, duration } = req.body;
    const gap = await schedulerService.createGap(
      req.params.channel_id,
      start_time,
      duration
    );
    res.json(gap);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// Extend item by pushing next item
router.put('/:channel_id/:schedule_id/extend-with-push', async (req, res) => {
  try {
    const { extensionDuration } = req.body;
    const result = await schedulerService.extendItemByPushingNext(
      req.params.channel_id,
      req.params.schedule_id,
      extensionDuration
    );
    res.json(result);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// Update schedule order
router.put('/:channel_id/order', async (req, res) => {
  try {
    await schedulerService.updateScheduleOrder(
      req.params.channel_id,
      req.body.schedule
    );
    res.json({ message: 'Schedule order updated' });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// Delete a schedule item
router.delete('/:channel_id/:schedule_id', async (req, res) => {
  try {
    await schedulerService.deleteItem(
      req.params.channel_id,
      req.params.schedule_id
    );
    res.json({ message: 'Schedule item deleted' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

export default router;
