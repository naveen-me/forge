import express from 'express';
const router = express.Router();

// DB is passed from server via req.app.locals

/**
 * GET /api/topics
 * List all topics
 */
router.get('/', (req, res) => {
  try {
    const { db } = req.app.locals;
    let topics = db.topics || [];
    
    // Sort by position
    topics.sort((a, b) => (a.position || 0) - (b.position || 0));
    
    res.json(topics);
    
  } catch (error) {
    console.error('List topics error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/topics/:id
 * Get a specific topic by ID
 */
router.get('/:id', (req, res) => {
  try {
    const { id } = req.params;
    
    const { db } = req.app.locals;
    const topic = db.topics.find(t => t.id === id);
    
    if (!topic) {
      return res.status(404).json({ error: 'Topic not found' });
    }
    
    res.json(topic);
    
  } catch (error) {
    console.error('Get topic error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Helper to save database
const saveDatabase = async (req) => {
  const { persistDb } = req.app.locals;
  if (persistDb) {
    await persistDb();
  }
};

/**
 * POST /api/topics
 * Create new topic
 */
router.post('/', async (req, res) => {
  try {
    const { db } = req.app.locals;
    const { name, description, position } = req.body;
    
    if (!name) {
      return res.status(400).json({ error: 'Name is required' });
    }
    
    const topic = {
      id: `topic_${Date.now()}`,
      name: name.trim(),
      description: description ? description.trim() : '',
      position: position || 0,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };
    
    if (!db.topics) {
      db.topics = [];
    }
    
    db.topics.push(topic);
    await saveDatabase(req);
    
    res.json(topic);
    
  } catch (error) {
    console.error('Create topic error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * PUT /api/topics/:id
 * Update topic
 */
router.put('/:id', async (req, res) => {
  try {
    const { db } = req.app.locals;
    const index = db.topics.findIndex(t => t.id === req.params.id);
    
    if (index === -1) {
      return res.status(404).json({ error: 'Topic not found' });
    }
    
    const { name, description, position } = req.body;
    
    // Update fields
    if (name) db.topics[index].name = name.trim();
    if (description !== undefined) db.topics[index].description = description.trim();
    if (position !== undefined) db.topics[index].position = position;
    
    db.topics[index].updated_at = new Date().toISOString();
    
    await saveDatabase(req);
    
    res.json(db.topics[index]);
    
  } catch (error) {
    console.error('Update topic error:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * DELETE /api/topics/:id
 * Delete topic
 */
router.delete('/:id', async (req, res) => {
  try {
    const { db } = req.app.locals;
    const index = db.topics.findIndex(t => t.id === req.params.id);
    
    if (index === -1) {
      return res.status(404).json({ error: 'Topic not found' });
    }
    
    const topicId = req.params.id;
    
    const hasQuestions = (db.questions || []).some(q => q.topic_id === topicId);
    
    if (hasQuestions && !req.query.force) {
      return res.status(400).json({ 
        error: 'Topic has questions. Use force=true to delete anyway',
        has_questions: true
      });
    }
    
    if (req.query.force) {
      db.questions = (db.questions || []).filter(q => q.topic_id !== topicId);
    }
    
    db.topics.splice(index, 1);
    
    await saveDatabase(req);
    
    res.json({ success: true, message: 'Topic deleted' });
    
  } catch (error) {
    console.error('Delete topic error:', error);
    res.status(500).json({ error: error.message });
  }
});

export default router;
