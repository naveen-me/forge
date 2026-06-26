import express from 'express';

const router = express.Router();

export function createTTSRouter(ttsService, jobManager = null) {
  
  /**
   * GET /api/tts/status
   * Check if TTS is configured and available
   */
  router.get('/status', (req, res) => {
    try {
      const googleAvailable = ttsService.isGoogleTTSAvailable();
      const edgeAvailable = ttsService.isEdgeTTSAvailable();
      const stats = ttsService.getCacheStats();
      const credStatus = ttsService.getCredentialsStatus();

      res.json({
        available: credStatus.provider === 'google' ? googleAvailable : edgeAvailable,
        google_available: googleAvailable,
        edge_available: edgeAvailable,
        message: credStatus.provider === 'google' 
          ? (googleAvailable ? 'Google TTS is ready' : 'Google TTS not configured')
          : 'Edge TTS is ready',
        cache_stats: stats,
        credentials: credStatus,
        sets_count: req.app.locals.db.tts_sets?.length || 0
      });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * GET /api/tts/sets
   */
  router.get('/sets', (req, res) => {
    try {
      const { db } = req.app.locals;
      res.json(db.tts_sets || []);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * POST /api/tts/sets
   */
  router.post('/sets', async (req, res) => {
    try {
      const { name, topicId, provider, voiceName } = req.body;
      const set = await ttsService.createTTSSet(name, topicId, provider, voiceName);
      if (req.app.locals.persistDb) await req.app.locals.persistDb();
      res.json(set);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * DELETE /api/tts/sets/:id
   */
  router.delete('/sets/:id', async (req, res) => {
    try {
      const result = await ttsService.deleteTTSSet(req.params.id);
      if (result.success && req.app.locals.persistDb) await req.app.locals.persistDb();
      res.json(result);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });
  
  /**
   * POST /api/tts/credentials
   * Configure TTS credentials or switch provider
   */
  router.post('/credentials', async (req, res) => {
    try {
      const { credentials, provider = 'google' } = req.body;
      
      const result = await ttsService.configureCredentials(credentials, provider);
      const { persistDb } = req.app.locals;
      if (persistDb) await persistDb();

      res.json(result);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });
  
  /**
   * GET /api/tts/voices
   * Get available voices for a provider
   */
  router.get('/voices', async (req, res) => {
    try {
      const { db, persistDb } = req.app.locals;
      const provider = req.query.provider || db.tts_credentials?.provider || 'google';
      const refresh = req.query.refresh === '1' || req.query.refresh === 'true';

      const voices = await ttsService.getAllVoices({ provider, refresh });
      if (persistDb) await persistDb();

      res.json({ voices, provider });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });
  
  /**
   * POST /api/tts/generate
   */
  router.post('/generate', async (req, res) => {
    try {
      const { persistDb } = req.app.locals;
      const { text, language, profileId, voiceName, category, provider, setId } = req.body;

      if (!text || !language || (!profileId && !voiceName)) {
        return res.status(400).json({ error: 'Missing required fields' });
      }
      
      const result = await ttsService.generateTTS(text, language, { profileId, voiceName, provider, setId }, category || 'questions');
      if (persistDb) await persistDb();
      
      res.json({ success: true, result });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * POST /api/tts/jobs
   */
  router.post('/jobs', (req, res) => {
    try {
      if (!jobManager) return res.status(503).json({ error: 'Job manager not available' });

      const {
        language,
        voiceName,
        profileId,
        provider,
        setName,
        questionIds,
        generateQuestions = true,
        generateOptions = true,
        generateCorrectOnly = false,
        generatePhrases = false
      } = req.body || {};

      const job = jobManager.start({
        language,
        voiceName,
        profileId,
        provider,
        setName,
        questionIds: Array.isArray(questionIds) ? questionIds : [],
        generateQuestions: !!generateQuestions,
        generateOptions: !!generateOptions,
        generateCorrectOnly: !!generateCorrectOnly,
        generatePhrases: !!generatePhrases
      });

      res.json({ success: true, job });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * POST /api/tts/generate/bulk
   */
  router.post('/generate/bulk', async (req, res) => {
    try {
      const { type, questionIds, language, profileId, voiceName, provider, setId, correctOnly } = req.body;

      let results;
      const voiceSelection = { profileId, voiceName, provider, setId };
      
      if (type === 'questions') {
        results = await ttsService.generateQuestionsTTS(questionIds, language, voiceSelection);
      } else if (type === 'options') {
        results = await ttsService.generateOptionsTTS(questionIds, language, voiceSelection, correctOnly);
      } else if (type === 'qa_with_correct') {
        const questionResults = await ttsService.generateQuestionsTTS(questionIds, language, voiceSelection);
        const optionResults = await ttsService.generateOptionsTTS(questionIds, language, voiceSelection, true);
        results = {
          total: (questionResults.total || 0) + (optionResults.total || 0),
          success: (questionResults.success || 0) + (optionResults.success || 0),
          failed: (questionResults.failed || 0) + (optionResults.failed || 0),
          cached: (questionResults.cached || 0) + (optionResults.cached || 0),
          errors: [...(questionResults.errors || []), ...(optionResults.errors || [])]
        };
      } else if (type === 'correct_only') {
        results = await ttsService.generateOptionsTTS(questionIds, language, voiceSelection, true);
      } else if (type === 'phrases') {
        results = await ttsService.generatePhrasesTTS(language, voiceSelection);
      } else {
        return res.status(400).json({ error: 'Invalid type' });
      }
      
      const { persistDb } = req.app.locals;
      if (persistDb) await persistDb();
      res.json({ success: true, results });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  // Keep other existing routes (profiles, cache, phrases) but they are mostly provider-agnostic
  
  router.get('/credentials', (req, res) => {
    try {
      res.json(ttsService.getCredentialsStatus());
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  router.delete('/credentials', async (req, res) => {
    try {
      ttsService.removeCredentials();
      const { persistDb } = req.app.locals;
      if (persistDb) await persistDb();
      res.json({ success: true });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  router.get('/profiles', (req, res) => {
    res.json(req.app.locals.db.tts_profiles || []);
  });

  router.post('/profiles', async (req, res) => {
    try {
      const { db, persistDb } = req.app.locals;
      const { name, provider, language, voice, gender, speaking_rate, pitch, volume_gain_db, is_default } = req.body;
      const profile = {
        id: `profile_${Date.now()}`,
        name, provider: provider || 'google', language, voice, gender: gender || 'neutral',
        speaking_rate: speaking_rate || 1.0, pitch: pitch || 0, volume_gain_db: volume_gain_db || 0, is_default: !!is_default
      };
      if (profile.is_default) {
        db.tts_profiles.forEach(p => { if (p.language === profile.language) p.is_default = false; });
      }
      db.tts_profiles.push(profile);
      if (persistDb) await persistDb();
      res.json({ success: true, profile });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  router.put('/profiles/:id', async (req, res) => {
    try {
      const { db, persistDb } = req.app.locals;
      const profile = db.tts_profiles.find(p => p.id === req.params.id);
      if (!profile) return res.status(404).json({ error: 'Profile not found' });
      Object.assign(profile, req.body);
      if (req.body.is_default) {
        db.tts_profiles.forEach(p => { if (p.language === profile.language && p.id !== profile.id) p.is_default = false; });
      }
      if (persistDb) await persistDb();
      res.json({ success: true, profile });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  router.delete('/profiles/:id', async (req, res) => {
    try {
      const { db, persistDb } = req.app.locals;
      const before = db.tts_profiles.length;
      db.tts_profiles = db.tts_profiles.filter(p => p.id !== req.params.id);
      if (db.tts_profiles.length === before) {
        return res.status(404).json({ error: 'Profile not found' });
      }
      if (persistDb) await persistDb();
      res.json({ success: true });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  router.get('/phrases/:language', (req, res) => {
    try {
      const { db } = req.app.locals;
      const language = req.params.language;
      let languagePhrases = (db.language_phrases || []).find(lp => lp.language === language);
      if (!languagePhrases) {
        languagePhrases = {
          id: `phrases_${language}_${Date.now()}`,
          language,
          phrases: ttsService.getDefaultPhrases(language),
          audio_files: {}
        };
      }
      res.json(languagePhrases);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  router.put('/phrases/:language', async (req, res) => {
    try {
      const { db, persistDb } = req.app.locals;
      const language = req.params.language;
      let languagePhrases = (db.language_phrases || []).find(lp => lp.language === language);
      if (!languagePhrases) {
        languagePhrases = {
          id: `phrases_${language}_${Date.now()}`,
          language,
          phrases: {},
          audio_files: {}
        };
        db.language_phrases.push(languagePhrases);
      }
      languagePhrases.phrases = req.body?.phrases || {};
      if (persistDb) await persistDb();
      res.json({ success: true, phrases: languagePhrases });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  router.post('/phrases/:language/generate', async (req, res) => {
    try {
      const { profileId, voiceName, provider } = req.body || {};
      const results = await ttsService.generatePhrasesTTS(req.params.language, { profileId, voiceName, provider });
      if (req.app.locals.persistDb) await req.app.locals.persistDb();
      res.json({ success: true, results });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * GET /api/tts/voice-catalog/enabled
   */
  router.get('/voice-catalog/enabled', (req, res) => {
    try {
      const voices = ttsService.getEnabledVoices();
      res.json(voices);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * PUT /api/tts/voice-catalog/enabled
   */
  router.put('/voice-catalog/enabled', async (req, res) => {
    try {
      const { voices } = req.body;
      if (!Array.isArray(voices)) {
        return res.status(400).json({ error: 'voices must be an array' });
      }
      ttsService.setEnabledVoices(voices);
      if (req.app.locals.persistDb) await req.app.locals.persistDb();
      res.json({ success: true });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * POST /api/tts/voice-catalog/enabled/auto-populate-all-edge
   */
  router.post('/voice-catalog/enabled/auto-populate-all-edge', async (req, res) => {
    try {
      const added = ttsService.ensureIndianEdgeVoicesEnabled();
      if (req.app.locals.persistDb) await req.app.locals.persistDb();
      res.json({ success: true, added: added.length, voices: ttsService.getEnabledVoices() });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * GET /api/tts/cache/stats
   */
  router.get('/cache/stats', (req, res) => {
    try {
      const stats = ttsService.getCacheStats();
      res.json(stats);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  router.get('/cache', (req, res) => {
    let cache = req.app.locals.db.tts_cache;
    if (req.query.category) cache = cache.filter(c => c.category === req.query.category);
    res.json(cache);
  });

  router.delete('/cache', async (req, res) => {
    const result = ttsService.clearCache(req.body);
    if (req.app.locals.persistDb) await req.app.locals.persistDb();
    res.json({ success: true, ...result });
  });

  router.put('/cache/:id/set-default', async (req, res) => {
    try {
      const entry = ttsService.setDefaultVersion(req.params.id);
      if (req.app.locals.persistDb) await req.app.locals.persistDb();
      res.json({ success: true, entry });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  /**
   * GET /api/tts/languages
   */
  router.get('/languages', (req, res) => {
    try {
      const { db } = req.app.locals;
      res.json(db.languages || []);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  return router;
}
