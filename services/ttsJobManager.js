import crypto from 'crypto';

/**
 * In-memory background job manager for TTS generation.
 *
 * Goal: generation continues even if the HTTP client disconnects.
 * Jobs are cancellable.
 */
export class TTSJobManager {
  /**
   * @param {object} deps
   * @param {import('./ttsService.js').TTSService} deps.ttsService
   * @param {Function} deps.saveDb
   * @param {object} deps.db
   */
  constructor({ ttsService, saveDb, db }) {
    this.ttsService = ttsService;
    this.saveDb = typeof saveDb === 'function' ? saveDb : () => {};
    this.db = db;

    /** @type {Map<string, any>} */
    this.jobs = new Map();
  }

  createJobId() {
    return `ttsjob_${Date.now()}_${crypto.randomBytes(6).toString('hex')}`;
  }

  /**
   * Start a job and return the created job object.
   *
   * payload:
   * - language
   * - voiceName/profileId
   * - questionIds
   * - generateQuestions (boolean)
   * - generateOptions (boolean)
   * - generatePhrases (boolean)
   */
  start(payload) {
    const jobId = this.createJobId();

    const job = {
      id: jobId,
      status: 'queued', // queued | running | completed | failed | cancelled
      created_at: new Date().toISOString(),
      started_at: null,
      finished_at: null,
      cancelled_at: null,
      set_id: null,
      error: null,

      payload,

      progress: {
        total_items: 0,
        completed_items: 0,
        current_item: null,
        percentage: 0,
        summary: {
          generated: 0,
          cached: 0,
          failed: 0
        },
        errors: []
      }
    };

    this.jobs.set(jobId, job);

    // Fire-and-forget.
    this._run(jobId).catch(err => {
      const j = this.jobs.get(jobId);
      if (!j) return;
      j.status = 'failed';
      j.error = err?.message || String(err);
      j.finished_at = new Date().toISOString();
    });

    return job;
  }

  get(jobId) {
    return this.jobs.get(jobId) || null;
  }

  list() {
    return Array.from(this.jobs.values()).sort((a, b) => (b.created_at || '').localeCompare(a.created_at || ''));
  }

  cancel(jobId) {
    const job = this.jobs.get(jobId);
    if (!job) return null;
    if (job.status === 'completed' || job.status === 'failed' || job.status === 'cancelled') return job;

    job.status = 'cancelled';
    job.cancelled_at = new Date().toISOString();
    return job;
  }

  _isCancelled(job) {
    return job.status === 'cancelled';
  }

  _setProgress(job, patch) {
    job.progress = { ...job.progress, ...patch };
    const { total_items, completed_items } = job.progress;
    job.progress.percentage = total_items > 0 ? (completed_items / total_items) * 100 : 0;
  }

  _getQuestionOptionEntries(question, correctOnly = false) {
    if (Array.isArray(question?.options)) {
      return question.options
        .map((text, index) => ({
          key: `option_${index + 1}`,
          text,
          isCorrect: Number(question.correct_option) === index
        }))
        .filter(option => option.text && (!correctOnly || option.isCorrect));
    }

    const keys = correctOnly ? [question?.correct_answer] : ['option_1', 'option_2', 'option_3', 'option_4'];
    return keys
      .filter(Boolean)
      .map(key => ({ key, text: question?.[key], isCorrect: key === question?.correct_answer }))
      .filter(option => option.text);
  }

  _buildWorkItems(payload, setId = null) {
    const {
      questionIds = [],
      language,
      voiceName,
      profileId,
      provider,
      generateQuestions,
      generateOptions,
      generateCorrectOnly,
      generatePhrases
    } = payload;

    const voiceSelection = { voiceName, profileId, provider, setId };

    /** @type {Array<{category: string, text: string, language: string, voiceSelection: any, meta?: any}>} */
    const items = [];

    if (generateQuestions) {
      for (const qid of questionIds) {
        const q = (this.db.questions || []).find(x => x.id === qid);
        if (!q) continue;
        const text = (q.question || '').trim();
        if (!text) continue;
        items.push({ category: 'questions', text, language, voiceSelection, meta: { questionId: qid, kind: 'question' } });
      }
    }

    if (generateOptions) {
      for (const qid of questionIds) {
        const q = (this.db.questions || []).find(x => x.id === qid);
        if (!q) continue;
        for (const option of this._getQuestionOptionEntries(q, generateCorrectOnly)) {
          const key = option.key;
          const text = (option.text || '').trim();
          if (!text) continue;
          items.push({ category: 'options', text, language, voiceSelection, meta: { questionId: qid, kind: 'option', optionKey: key } });
        }
      }
    }

    // Phrases are handled as a unit because generatePhrasesTTS also stores mappings.
    // We'll model it as one work item.
    if (generatePhrases) {
      items.push({ category: 'phrases', text: null, language, voiceSelection, meta: { kind: 'phrases-bulk' } });
    }

    return items;
  }

  async _run(jobId) {
    const job = this.jobs.get(jobId);
    if (!job) return;

    job.status = 'running';
    job.started_at = new Date().toISOString();

    const { setName, provider, voiceName, profileId, questionIds } = job.payload;

    // 1. Create a set if requested
    let setId = null;
    if (setName) {
      try {
        const topicId = (questionIds && questionIds.length > 0) ? this.db.questions.find(q => q.id === questionIds[0])?.topic_id : null;
        const set = await this.ttsService.createTTSSet(setName, topicId, provider, voiceName || profileId);
        setId = set.id;
        job.set_id = setId;
        this.saveDb(); // Save set creation immediately
      } catch (err) {
        console.error('Error creating TTS set for job:', err);
      }
    }

    const items = this._buildWorkItems(job.payload, setId);
    this._setProgress(job, { total_items: items.length, completed_items: 0, current_item: null });

    for (let i = 0; i < items.length; i++) {
      if (this._isCancelled(job)) {
        job.finished_at = new Date().toISOString();
        return;
      }

      const item = items[i];
      this._setProgress(job, {
        current_item: { index: i + 1, ...item.meta, category: item.category }
      });

      try {
        if (item.category === 'phrases') {
          // Bulk phrases generation.
          await this.ttsService.generatePhrasesTTS(item.language, item.voiceSelection);
        } else {
          const { voiceName, profileId, setId: itemSetId } = item.voiceSelection || {};
          const voiceKey = voiceName || profileId;
          
          // Use cache only if not generating into a new set, or if specifically found in that set.
          const cached = await this.ttsService.getCachedTTS(item.text, item.language, voiceKey, itemSetId);
          
          if (cached && (cached.set_id === itemSetId || !itemSetId)) {
            job.progress.summary.cached++;
          } else {
            await this.ttsService.generateTTS(item.text, item.language, item.voiceSelection, item.category);
            job.progress.summary.generated++;
          }
        }
      } catch (e) {
        job.progress.summary.failed++;
        job.progress.errors.push({
          item: item.meta,
          category: item.category,
          error: e?.message || String(e)
        });
      }

      // Persist after each item so we keep cache even if server is later killed.
      this.saveDb();

      this._setProgress(job, { completed_items: i + 1 });
    }

    if (this._isCancelled(job)) {
      job.finished_at = new Date().toISOString();
      return;
    }

    job.status = 'completed';
    job.finished_at = new Date().toISOString();
  }
}
