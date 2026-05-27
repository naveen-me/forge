import express from 'express';

const router = express.Router();

/**
 * GET /api/questions
 * Optional query params:
 * - topic_id: filter by topic
 */
router.get('/', (req, res) => {
  try {
    const { db } = req.app.locals;
    let questions = db.questions || [];

    if (req.query.topic_id) {
      questions = questions.filter(q => q.topic_id === req.query.topic_id);
    }

    if (!req.query.page && !req.query.limit) {
      return res.json(questions);
    }

    const page = parseInt(req.query.page, 10) || 1;
    const limit = parseInt(req.query.limit, 10) || 50;
    const total = questions.length;
    const totalPages = Math.ceil(total / limit);
    const startIndex = (page - 1) * limit;
    const endIndex = startIndex + limit;

    const paginated = questions.slice(startIndex, endIndex);

    res.json({
      questions: paginated,
      pagination: {
        page,
        limit,
        total,
        totalPages
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

/**
 * GET /api/questions/:id
 */
router.get('/:id', (req, res) => {
  try {
    const { db } = req.app.locals;
    const question = (db.questions || []).find(q => q.id === req.params.id);
    if (!question) {
      return res.status(404).json({ error: 'Question not found' });
    }
    res.json(question);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

/**
 * POST /api/questions
 */
router.post('/', async (req, res) => {
  try {
    const { db, persistDb } = req.app.locals;
    const { topic_id, question, options, correct_option, explanation } = req.body;

    if (!topic_id) {
      return res.status(400).json({ error: 'topic_id is required' });
    }
    if (!question) {
      return res.status(400).json({ error: 'Question is required' });
    }
    if (!Array.isArray(options) || options.length < 2) {
      return res.status(400).json({ error: 'At least 2 options are required' });
    }

    const topic = (db.topics || []).find(t => t.id === topic_id);
    if (!topic) {
      return res.status(400).json({ error: 'Invalid topic_id' });
    }

    const newQuestion = {
      id: `question_${Date.now()}`,
      topic_id,
      question: question.trim(),
      options: options.map(opt => String(opt).trim()).filter(Boolean),
      correct_option: Number.isInteger(correct_option) ? correct_option : parseInt(correct_option, 10) || 0,
      explanation: explanation ? String(explanation).trim() : '',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };

    db.questions = db.questions || [];
    db.questions.push(newQuestion);

    if (persistDb) {
      await persistDb();
    }

    res.json(newQuestion);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

/**
 * PUT /api/questions/:id
 */
router.put('/:id', async (req, res) => {
  try {
    const { db, persistDb } = req.app.locals;
    const index = (db.questions || []).findIndex(q => q.id === req.params.id);

    if (index === -1) {
      return res.status(404).json({ error: 'Question not found' });
    }

    const { topic_id, question, options, correct_option, explanation } = req.body;

    if (topic_id !== undefined) {
      const topic = (db.topics || []).find(t => t.id === topic_id);
      if (!topic) {
        return res.status(400).json({ error: 'Invalid topic_id' });
      }
      db.questions[index].topic_id = topic_id;
    }

    if (question !== undefined) {
      db.questions[index].question = String(question).trim();
    }

    if (options !== undefined) {
      if (!Array.isArray(options) || options.length < 2) {
        return res.status(400).json({ error: 'At least 2 options are required' });
      }
      db.questions[index].options = options.map(opt => String(opt).trim()).filter(Boolean);
    }

    if (correct_option !== undefined) {
      db.questions[index].correct_option = Number.isInteger(correct_option) ? correct_option : parseInt(correct_option, 10) || 0;
    }

    if (explanation !== undefined) {
      db.questions[index].explanation = String(explanation).trim();
    }

    db.questions[index].updated_at = new Date().toISOString();

    if (persistDb) {
      await persistDb();
    }

    res.json(db.questions[index]);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

/**
 * DELETE /api/questions/:id
 */
router.delete('/:id', async (req, res) => {
  try {
    const { db, persistDb } = req.app.locals;
    const index = (db.questions || []).findIndex(q => q.id === req.params.id);

    if (index === -1) {
      return res.status(404).json({ error: 'Question not found' });
    }

    db.questions.splice(index, 1);

    if (persistDb) {
      await persistDb();
    }

    res.json({ success: true, message: 'Question deleted' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;
