import express from 'express';

const router = express.Router();

function csvEscape(value) {
  const str = value == null ? '' : String(value);
  if (/[",\n]/.test(str)) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}

function parseCsvLine(line) {
  const result = [];
  let current = '';
  let inQuotes = false;
  for (let i = 0; i < line.length; i++) {
    const char = line[i];
    if (char === '"') {
      if (inQuotes && line[i + 1] === '"') {
        current += '"';
        i += 1;
      } else {
        inQuotes = !inQuotes;
      }
      continue;
    }
    if (char === ',' && !inQuotes) {
      result.push(current);
      current = '';
      continue;
    }
    current += char;
  }
  result.push(current);
  return result;
}

function parseCsv(text) {
  const lines = text.split(/\r?\n/).filter(line => line.trim() !== '');
  if (!lines.length) return [];
  const headers = parseCsvLine(lines[0]).map(h => h.trim());
  return lines.slice(1).map(line => {
    const values = parseCsvLine(line);
    const row = {};
    headers.forEach((h, idx) => {
      row[h] = values[idx] ?? '';
    });
    return row;
  });
}

router.get('/sample', (req, res) => {
  const { db } = req.app.locals;
  const { topicId } = req.query;
  const headers = ['topic', 'question', 'option_1', 'option_2', 'option_3', 'option_4', 'correct_option', 'explanation'];
  const topicName = topicId
    ? (db.topics || []).find(t => t.id === topicId)?.name
    : null;
  const sampleRows = [
    {
      topic: topicName || 'Sample Topic',
      question: 'What is 2 + 2?',
      option_1: '3',
      option_2: '4',
      option_3: '5',
      option_4: '',
      correct_option: '1',
      explanation: '2 + 2 equals 4.'
    }
  ];

  const csv = [headers.join(',')]
    .concat(sampleRows.map(row => headers.map(h => csvEscape(row[h])).join(',')))
    .join('\n');

  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', 'attachment; filename="sample_questions.csv"');
  res.send(csv);
});

router.get('/export', (req, res) => {
  const { db } = req.app.locals;
  const { topicId } = req.query;
  const headers = ['topic', 'question', 'option_1', 'option_2', 'option_3', 'option_4', 'correct_option', 'explanation'];
  const topicMap = new Map((db.topics || []).map(t => [t.id, t.name]));

  const rows = (db.questions || [])
    .filter(q => !topicId || q.topic_id === topicId)
    .map(q => {
      const options = Array.isArray(q.options) ? q.options : [];
      return {
        topic: topicMap.get(q.topic_id) || '',
        question: q.question || '',
        option_1: options[0] || '',
        option_2: options[1] || '',
        option_3: options[2] || '',
        option_4: options[3] || '',
        correct_option: String((q.correct_option ?? 0) + 1),
        explanation: q.explanation || ''
      };
    });

  const csv = [headers.join(',')]
    .concat(rows.map(row => headers.map(h => csvEscape(row[h])).join(',')))
    .join('\n');

  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', 'attachment; filename="questions_export.csv"');
  res.send(csv);
});

router.post('/import', express.text({ type: '*/*', limit: '10mb' }), async (req, res) => {
  try {
    const { db, persistDb } = req.app.locals;
    const { topicId } = req.query;
    const csvText = req.body || '';
    const rows = parseCsv(csvText);

    const topicMap = new Map((db.topics || []).map(t => [t.name.toLowerCase(), t]));
    const selectedTopic = topicId
      ? (db.topics || []).find(t => t.id === topicId)
      : null;

    let createdTopics = 0;
    let createdQuestions = 0;

    rows.forEach(row => {
      const topicName = selectedTopic ? selectedTopic.name : (row.topic || '').trim();
      const questionText = (row.question || '').trim();
      if (!topicName || !questionText) return;

      let topic = selectedTopic || topicMap.get(topicName.toLowerCase());
      if (!topic) {
        topic = {
          id: `topic_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
          name: topicName,
          description: '',
          position: 0,
          created_at: new Date().toISOString(),
          updated_at: new Date().toISOString()
        };
        db.topics = db.topics || [];
        db.topics.push(topic);
        topicMap.set(topicName.toLowerCase(), topic);
        createdTopics += 1;
      }

      const options = [row.option_1, row.option_2, row.option_3, row.option_4]
        .map(opt => (opt || '').trim())
        .filter(opt => opt !== '');

      if (options.length < 2) return;

      const correctNumber = parseInt(row.correct_option, 10);
      const correctIndex = Number.isInteger(correctNumber)
        ? Math.max(0, correctNumber - 1)
        : 0;
      const newQuestion = {
        id: `question_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
        topic_id: topic.id,
        question: questionText,
        options,
        correct_option: correctIndex,
        explanation: (row.explanation || '').trim(),
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
      };

      db.questions = db.questions || [];
      db.questions.push(newQuestion);
      createdQuestions += 1;
    });

    if (persistDb) {
      await persistDb();
    }

    res.json({
      success: true,
      topics_created: createdTopics,
      questions_created: createdQuestions
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;
