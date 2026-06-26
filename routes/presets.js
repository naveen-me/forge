import express from 'express';

const router = express.Router();

// Helper to persist database
const saveDatabase = async (req) => {
  const { persistDb } = req.app.locals;
  if (persistDb) {
    await persistDb();
  }
};

// GET /api/presets - Get all presets
router.get('/', (req, res) => {
  try {
    const { db } = req.app.locals;
    const presets = db.presets || [];
    res.json(presets);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET /api/presets/:id - Get specific preset
router.get('/:id', (req, res) => {
  try {
    const { db } = req.app.locals;
    const preset = (db.presets || []).find(p => p.id === req.params.id);
    if (!preset) {
      return res.status(404).json({ error: 'Preset not found' });
    }
    res.json(preset);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST /api/presets - Create new preset
router.post('/', async (req, res) => {
  try {
    const { db } = req.app.locals;
    const { name, config } = req.body;

    if (!name || !config) {
      return res.status(400).json({ error: 'Name and config are required' });
    }

    const newPreset = {
      id: `preset_${Date.now()}`,
      name: name.trim(),
      canvas_width: config.canvas?.width,
      canvas_height: config.canvas?.height,
      aspect_ratio: config.canvas?.aspect_ratio,
      config: typeof config === 'string' ? config : JSON.stringify(config),
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };

    db.presets = db.presets || [];
    db.presets.push(newPreset);

    await saveDatabase(req);

    res.json(newPreset);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// PUT /api/presets/:id - Update existing preset
router.put('/:id', async (req, res) => {
  try {
    const { db } = req.app.locals;
    const { name, config } = req.body;

    const presetIndex = (db.presets || []).findIndex(p => p.id === req.params.id);
    if (presetIndex === -1) {
      return res.status(404).json({ error: 'Preset not found' });
    }

    const preset = db.presets[presetIndex];

    if (name) preset.name = name.trim();
    if (config) {
      preset.config = typeof config === 'string' ? config : JSON.stringify(config);
      preset.canvas_width = config.canvas?.width;
      preset.canvas_height = config.canvas?.height;
      preset.aspect_ratio = config.canvas?.aspect_ratio;
    }
    preset.updated_at = new Date().toISOString();

    db.presets[presetIndex] = preset;

    await saveDatabase(req);

    res.json(preset);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE /api/presets/:id - Delete preset
router.delete('/:id', async (req, res) => {
  try {
    const { db } = req.app.locals;
    const presetIndex = (db.presets || []).findIndex(p => p.id === req.params.id);
    if (presetIndex === -1) {
      return res.status(404).json({ error: 'Preset not found' });
    }

    db.presets.splice(presetIndex, 1);

    await saveDatabase(req);

    res.json({ success: true, message: 'Preset deleted' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;
