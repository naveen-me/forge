import express from 'express';
const router = express.Router();
import Setting from '../models/Setting.js';

// Get user settings
router.get('/', async (req, res) => {
  try {
    const userId = req.user.id;
    
    let setting = await Setting.findOne({
      where: { userId }
    });

    // If no settings exist, create default settings
    if (!setting) {
      setting = await Setting.create({
        userId,
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'
      });
    }

    res.json({ success: true, data: setting });
  } catch (error) {
    console.error('Error fetching settings:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

// Update user settings
router.put('/', async (req, res) => {
  try {
    const userId = req.user.id;
    const settingsData = req.body;

    // Check if settings exist for the user
    let setting = await Setting.findOne({
      where: { userId }
    });

    if (setting) {
      // Update existing settings
      await setting.update(settingsData);
    } else {
      // Create new settings
      setting = await Setting.create({
        userId,
        ...settingsData
      });
    }

    res.json({ success: true, data: setting });
  } catch (error) {
    console.error('Error updating settings:', error);
    res.status(400).json({ success: false, message: error.message });
  }
});

// Get specific setting
router.get('/:key', async (req, res) => {
  try {
    const userId = req.user.id;
    const { key } = req.params;

    const setting = await Setting.findOne({
      where: { userId }
    });

    if (!setting) {
      return res.status(404).json({ success: false, message: 'Settings not found' });
    }

    // Return only the requested setting value
    res.json({ success: true, key, value: setting[key] });
  } catch (error) {
    console.error('Error fetching specific setting:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

export default router;