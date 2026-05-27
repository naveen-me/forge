import textToSpeech from '@google-cloud/text-to-speech';
import fs from 'fs';
import path from 'path';
import crypto from 'crypto';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import { parseFile } from 'music-metadata';
import { EdgeTTS, listVoices as listEdgeVoices } from 'edge-tts-universal';
import { EDGE_VOICES } from '../shared/edge-voices.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const INDIAN_EDGE_LANGUAGE_CODES = new Set([
  'bn-IN',
  'en-IN',
  'gu-IN',
  'hi-IN',
  'kn-IN',
  'ml-IN',
  'mr-IN',
  'ta-IN',
  'te-IN',
  'ur-IN'
]);

function isIndianEdgeVoice(voice) {
  return (voice?.languageCodes || []).some(code => INDIAN_EDGE_LANGUAGE_CODES.has(code));
}

function getErrorMessage(error) {
  if (!error) return 'Unknown error';
  if (typeof error === 'string') return error;
  if (error.message) return error.message;
  try {
    return JSON.stringify(error);
  } catch {
    return String(error);
  }
}

export class TTSService {
  constructor(db) {
    this.db = db;
    this.db.tts_profiles = this.db.tts_profiles || [];
    this.db.tts_cache = this.db.tts_cache || [];
    this.db.tts_sets = this.db.tts_sets || [];
    this.db.language_phrases = this.db.language_phrases || [];
    this.db.tts_enabled_voices = this.db.tts_enabled_voices || [];
    this.storageDir = path.join(__dirname, '../storage/tts');
    this.client = null;
    
    // Initialize storage directories
    this.initStorage();
    
    // Try to initialize Google TTS client
    this.initGoogleClient();

    // Pre-populate edge voice catalog from hardcoded list so auto-populate works instantly
    this.db.edge_voice_catalog = this.db.edge_voice_catalog || { provider: 'edge', voices: EDGE_VOICES, updated_at: new Date().toISOString() };
    this.ensureIndianEdgeVoicesEnabled();

    // Best-effort: if a client is available and we don't have a cached voice catalog yet,
    // fetch it once at startup (async) so the UI doesn't show an empty dropdown.
    this.warmVoiceCatalog().catch(() => {});
  }
  
  /**
   * Initialize Google TTS client
   */
  initGoogleClient() {
    try {
      // Priority 1: Check database for stored credentials
      if (this.db.tts_credentials && this.db.tts_credentials.configured && this.db.tts_credentials.provider === 'google') {
        const credentials = this.db.tts_credentials.credentials;

        if (credentials) {
          try {
            // Parse credentials if stored as string
            const creds = typeof credentials === 'string' ? JSON.parse(credentials) : credentials;

            const normalized = {
              client_email: creds.client_email,
              private_key: creds.private_key
            };

            this.client = new textToSpeech.TextToSpeechClient({
              credentials: normalized,
              projectId: creds.project_id
            });
            console.log('✓ Google TTS client initialized from database credentials');
            return;
          } catch (parseError) {
            console.error('Error parsing database credentials:', parseError.message);
          }
        }
      }

      // Priority 2: Check environment variable (fallback)
      const credentialsPath = process.env.GOOGLE_APPLICATION_CREDENTIALS;

      if (credentialsPath && fs.existsSync(credentialsPath)) {
        this.client = new textToSpeech.TextToSpeechClient({
          keyFilename: credentialsPath
        });
        console.log('✓ Google TTS client initialized from environment variable');
      } else {
        // Silent: status endpoint will show configured=false
        if (this.db.tts_credentials?.provider === 'google') {
          console.warn('⚠ Google TTS credentials not configured.');
        }
      }
    } catch (error) {
      console.error('Error initializing Google TTS client:', error.message);
      this.client = null;
    }
  }

  /**
   * Ensure client is initialized if credentials are configured in DB.
   */
  ensureClient() {
    if (this.client) return true;
    if (this.db.tts_credentials?.configured && this.db.tts_credentials?.credentials && this.db.tts_credentials.provider === 'google') {
      this.initGoogleClient();
    }
    return this.client !== null;
  }
  
  async warmVoiceCatalog() {
    try {
      // Warm Google voices if available
      if (this.isGoogleTTSAvailable()) {
        if (!this.db.tts_voice_catalog?.voices?.length) {
          await this.getAllVoices({ provider: 'google', refresh: true });
        }
      }
      
      // Edge-TTS voices are always available via fetching
      if (!this.db.edge_voice_catalog?.voices?.length) {
        await this.getAllVoices({ provider: 'edge', refresh: true });
      }
    } catch (e) {
      // ignore
    }
  }

  /**
   * Configure TTS credentials from JSON
   */
  async configureCredentials(credentialsJson, provider = 'google') {
    try {
      if (provider === 'edge') {
        // Edge-TTS doesn't need credentials, but we mark it as configured
        this.db.tts_credentials = {
          configured: true,
          provider: 'edge',
          credentials: null,
          project_id: 'edge-tts',
          created_at: this.db.tts_credentials?.created_at || new Date().toISOString(),
          updated_at: new Date().toISOString()
        };
        this.ensureIndianEdgeVoicesEnabled();
        return { success: true, message: 'Edge-TTS configured successfully' };
      }

      // Parse if string
      const credentials = typeof credentialsJson === 'string' 
        ? JSON.parse(credentialsJson) 
        : credentialsJson;
      
      // Validate credentials format
      if (!credentials.type || !credentials.project_id || !credentials.private_key || !credentials.client_email) {
        throw new Error('Invalid credentials format. Required fields: type, project_id, private_key, client_email');
      }
      
      // Initialize client with new credentials
      this.client = new textToSpeech.TextToSpeechClient({
        credentials: credentials
      });
      
      // Test the credentials
      const isValid = await this.testCredentials();
      if (isValid) {
        // Store in database
        this.db.tts_credentials = {
          configured: true,
          provider: 'google',
          credentials: credentials,
          project_id: credentials.project_id,
          created_at: this.db.tts_credentials?.created_at || new Date().toISOString(),
          updated_at: new Date().toISOString()
        };
        
        console.log('✓ Google TTS credentials configured and validated');
        return { success: true, message: 'Credentials configured successfully' };
      } else {
        this.client = null;
        throw new Error('Credentials validation failed. Please check your service account has the Cloud Text-to-Speech API enabled.');
      }
      
    } catch (error) {
      console.error('Error configuring credentials:', error);
      this.client = null;
      throw error;
    }
  }
  
  /**
   * Test if current credentials are valid
   */
  async testCredentials() {
    try {
      if (this.db.tts_credentials?.provider === 'edge') return true;
      
      if (!this.client) {
        return false;
      }
      
      // Try to list voices as a test
      const [result] = await this.client.listVoices({ languageCode: 'en-US' });
      return result && result.voices && result.voices.length > 0;
    } catch (error) {
      console.error('Credentials test failed:', error.message);
      return false;
    }
  }
  
  /**
   * Remove configured credentials
   */
  removeCredentials() {
    this.client = null;
    this.db.tts_credentials = {
      configured: false,
      provider: 'google',
      credentials: null,
      project_id: null,
      created_at: null,
      updated_at: null
    };
    console.log('✓ TTS credentials removed');
  }
  
  /**
   * Get credentials status
   */
  getCredentialsStatus() {
    const dbConfigured = !!this.db.tts_credentials?.configured;
    const provider = this.db.tts_credentials?.provider || 'google';
    
    let configured = dbConfigured;
    if (provider === 'google') {
      configured = dbConfigured || (this.client !== null);
    }

    return {
      configured,
      provider,
      project_id: this.db.tts_credentials?.project_id || null,
      has_client: provider === 'google' ? (this.client !== null) : true,
      updated_at: this.db.tts_credentials?.updated_at || null,
      source: dbConfigured ? 'db' : (this.client ? 'env' : 'none')
    };
  }
  
  /**
   * Check if Google TTS is available
   */
  isGoogleTTSAvailable() {
    return this.ensureClient();
  }

  /**
   * Check if Edge TTS is available
   */
  isEdgeTTSAvailable() {
    return true; // Always available as it doesn't need credentials
  }
  
  /**
   * Initialize storage directories
   */
  initStorage() {
    const dirs = [
      this.storageDir,
      path.join(this.storageDir, 'questions'),
      path.join(this.storageDir, 'options'),
      path.join(this.storageDir, 'phrases')
    ];
    
    dirs.forEach(dir => {
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
        console.log(`Created TTS storage directory: ${dir}`);
      }
    });
  }
  
  /**
   * Create a new TTS Set
   */
  async createTTSSet(name, topicId, provider, voiceName) {
    const set = {
      id: `set_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      name: name || `Set ${new Date().toLocaleString()}`,
      topic_id: topicId || null,
      provider: provider || 'google',
      voice_name: voiceName || null,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString(),
      item_count: 0
    };
    
    this.db.tts_sets.push(set);
    return set;
  }

  /**
   * Delete a TTS Set and its associated cache entries
   */
  async deleteTTSSet(setId) {
    const index = this.db.tts_sets.findIndex(s => s.id === setId);
    if (index === -1) return { success: false, message: 'Set not found' };

    // Find all cache entries for this set
    const entries = this.db.tts_cache.filter(c => c.set_id === setId);
    const ids = entries.map(e => e.id);
    
    // Delete files and cache entries
    if (ids.length > 0) {
      this.clearCache({ ids });
    }

    // Remove the set itself
    this.db.tts_sets.splice(index, 1);
    return { success: true, removed_items: ids.length };
  }

  /**
   * Generate hash for text to use as cache key
   */
  generateTextHash(text, language, voiceKey) {
    const combined = `${text}|${language}|${voiceKey}`;
    return crypto.createHash('md5').update(combined, 'utf8').digest('hex');
  }
  
  /**
   * Get cached TTS audio
   */
  async getCachedTTS(text, language, voiceKey, setId = null) {
    const textHash = this.generateTextHash(text, language, voiceKey);

    // Check database cache
    let entries = this.db.tts_cache.filter(c =>
      c.text_hash === textHash && (c.profile_id === voiceKey || c.voice_name === voiceKey)
    );
    
    if (entries.length === 0) {
      return null;
    }

    // If setId is provided, prioritize entry from that set
    if (setId) {
      const setEntry = entries.find(c => c.set_id === setId);
      if (setEntry && fs.existsSync(path.join(__dirname, '..', setEntry.audio_url))) {
        return setEntry;
      }
    }
    
    const defaultEntry = entries.find(c => c.is_default) || entries[0];
    
    if (defaultEntry && fs.existsSync(path.join(__dirname, '..', defaultEntry.audio_url))) {
      return defaultEntry;
    }
    
    return null;
  }
  
  /**
   * Get all cached TTS versions for a text
   */
  async getAllCachedVersions(text, language, voiceKey, setId = null) {
    const textHash = this.generateTextHash(text, language, voiceKey);
    
    let entries = this.db.tts_cache.filter(c =>
      c.text_hash === textHash && (c.profile_id === voiceKey || c.voice_name === voiceKey)
    );

    if (setId) {
      entries = entries.filter(c => c.set_id === setId);
    }
    
    return entries.filter(entry => 
      fs.existsSync(path.join(__dirname, '..', entry.audio_url))
    );
  }
  
  /**
   * Generate TTS using either Google or Edge
   */
  async generateTTS(text, language, voiceSelection, category = 'questions', meta = {}) {
    try {
      const { profileId, voiceName, provider: requestedProvider, setId } = (typeof voiceSelection === 'string')
        ? { profileId: voiceSelection }
        : (voiceSelection || {});

      // Resolve profile if profileId is provided
      let profile = null;
      if (profileId && !voiceName) {
        profile = this.db.tts_profiles.find(p => p.id === profileId);
        if (!profile) throw new Error(`TTS profile not found: ${profileId}`);
      }

      const provider = requestedProvider || profile?.provider || 'google';
      const finalVoiceName = voiceName || profile?.voice;
      
      if (!finalVoiceName) {
        throw new Error('Missing voice selection (profileId or voiceName)');
      }

      const voiceKey = finalVoiceName;

      // Check cache first (only if NO setId is provided, or if we want to allow cross-set deduplication)
      // Usually, if a user specifically requests a NEW set, they might want new generation or link to existing?
      // For now, let's allow linking to existing file to save space, but mark it with the set_id.
      const cached = await this.getCachedTTS(text, language, voiceKey, setId);
      if (cached && (cached.set_id === setId || !setId)) {
        console.log(`✓ Using cached TTS: ${cached.audio_url}`);
        return cached;
      }

      // Generate filename and paths
      const textHash = this.generateTextHash(text, language, voiceKey);
      // Include setId in filename if provided to avoid collisions between sets using same text/voice but potentially different params (future)
      const setIdPart = setId ? `_${setId.split('_').pop()}` : '';
      const filename = `${category}_${textHash}${setIdPart}.mp3`;
      const filepath = path.join(this.storageDir, category, filename);
      const relativeUrl = `/storage/tts/${category}/${filename}`;

      console.log(`Generating ${provider} TTS: "${text.substring(0, 50)}..." (${language}) using ${finalVoiceName} ${setId ? `[Set: ${setId}]` : ''}`);

      if (provider === 'edge') {
        // Edge TTS Generation
        try {
          const edgeTts = new EdgeTTS(text, finalVoiceName, {
            rate: profile ? `${Math.round((profile.speaking_rate - 1) * 100)}%` : '+0%',
            pitch: profile ? `${profile.pitch >= 0 ? '+' : ''}${profile.pitch}Hz` : '+0Hz',
            volume: profile ? `${profile.volume_gain_db >= 0 ? '+' : ''}${profile.volume_gain_db}%` : '+0%'
          });
          const result = await edgeTts.synthesize();
          const audioBuffer = Buffer.from(await result.audio.arrayBuffer());
          if (audioBuffer.length === 0) {
            throw new Error('No audio data received');
          }
          fs.writeFileSync(filepath, audioBuffer);
        } catch (edgeError) {
          console.error('Edge TTS Error:', edgeError);
          throw new Error(`Edge TTS failed: ${getErrorMessage(edgeError)}`);
        }
      } else {
        // Google TTS Generation
        if (!this.isGoogleTTSAvailable()) {
          throw new Error('Google TTS is not configured. Please set up credentials.');
        }

        const googleLanguageCode = this.toGoogleLanguageCode(language);
        const request = {
          input: { text },
          voice: {
            languageCode: googleLanguageCode,
            name: finalVoiceName
          },
          audioConfig: {
            audioEncoding: 'MP3',
            speakingRate: profile?.speaking_rate || 1.0,
            pitch: profile?.pitch || 0,
            volumeGainDb: profile?.volume_gain_db || 0
          }
        };

        const [response] = await this.client.synthesizeSpeech(request);
        fs.writeFileSync(filepath, response.audioContent, 'binary');
      }

      console.log(`✓ Audio saved: ${relativeUrl}`);
      
      // Get audio duration
      const duration = await this.getAudioDuration(filepath);
      
      // Check for versions
      const existingVersions = this.db.tts_cache.filter(c =>
        c.text_hash === textHash && c.voice_name === finalVoiceName
      );
      
      // Cache in database
      const cacheEntry = {
        id: `tts_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        text,
        text_hash: textHash,
        language,
        profile_id: profileId || provider,
        voice_name: finalVoiceName,
        provider,
        category,
        audio_url: relativeUrl,
        duration,
        is_default: existingVersions.length === 0,
        version: existingVersions.length + 1,
        set_id: setId || null,
        created_at: new Date().toISOString(),
        meta: {
          topic_id: meta.topic_id || null,
          question_id: meta.question_id || null,
          option_key: meta.option_key || null
        }
      };
      
      this.db.tts_cache.push(cacheEntry);

      // Update set item count
      if (setId) {
        const set = this.db.tts_sets.find(s => s.id === setId);
        if (set) {
          set.item_count = (set.item_count || 0) + 1;
          set.updated_at = new Date().toISOString();
        }
      }

      return cacheEntry;
      
    } catch (error) {
      console.error('TTS generation error:', error);
      throw error;
    }
  }
  
  /**
   * Get audio file duration
   */
  async getAudioDuration(filepath) {
    try {
      const metadata = await parseFile(filepath);
      return metadata.format.duration || 0;
    } catch (error) {
      console.warn('Could not determine audio duration:', error.message);
      return 0;
    }
  }
  
  /**
   * Bulk generate TTS for questions
   */
  async generateQuestionsTTS(questionIds, language, voiceSelection, onProgress) {
    const results = { total: questionIds.length, success: 0, failed: 0, cached: 0, errors: [] };
    
    for (let i = 0; i < questionIds.length; i++) {
      const questionId = questionIds[i];
      const question = this.db.questions.find(q => q.id === questionId);
      
      if (!question) {
        results.failed++;
        results.errors.push({ questionId, error: 'Question not found' });
        continue;
      }
      
      try {
        const { profileId, voiceName } = (typeof voiceSelection === 'string') ? { profileId: voiceSelection } : (voiceSelection || {});
        const voiceKey = voiceName || (profileId ? this.db.tts_profiles.find(p => p.id === profileId)?.voice : null);
        
        const cached = await this.getCachedTTS(question.question, language, voiceKey);
        if (cached) {
          results.cached++;
        } else {
          await this.generateTTS(question.question, language, voiceSelection, 'questions', {
            topic_id: question.topic_id,
            question_id: question.id
          });
          results.success++;
        }
      } catch (error) {
        results.failed++;
        results.errors.push({ questionId, error: getErrorMessage(error) });
      }
      
      if (onProgress) {
        onProgress({ current: i + 1, total: results.total, percentage: ((i + 1) / results.total) * 100 });
      }
    }
    return results;
  }

  getQuestionOptionEntries(question, correctOnly = false) {
    if (Array.isArray(question?.options)) {
      return question.options
        .map((text, index) => ({
          optionKey: `option_${index + 1}`,
          text,
          isCorrect: Number(question.correct_option) === index
        }))
        .filter(option => option.text && (!correctOnly || option.isCorrect));
    }

    const optionKeys = correctOnly ? [question?.correct_answer] : ['option_1', 'option_2', 'option_3', 'option_4'];
    return optionKeys
      .filter(Boolean)
      .map(optionKey => ({
        optionKey,
        text: question?.[optionKey],
        isCorrect: optionKey === question?.correct_answer
      }))
      .filter(option => option.text);
  }
  
  /**
   * Bulk generate TTS for options
   */
  async generateOptionsTTS(questionIds, language, voiceSelection, correctOnly = false, onProgress) {
    const results = { total: 0, success: 0, failed: 0, cached: 0, errors: [] };
    const options = [];
    
    questionIds.forEach(questionId => {
      const question = this.db.questions.find(q => q.id === questionId);
      if (question) {
        this.getQuestionOptionEntries(question, correctOnly).forEach(option => {
          options.push({
            questionId,
            topicId: question.topic_id,
            ...option
          });
        });
      }
    });
    
    results.total = options.length;
    
    for (let i = 0; i < options.length; i++) {
      const option = options[i];
      try {
        const { profileId, voiceName } = (typeof voiceSelection === 'string') ? { profileId: voiceSelection } : (voiceSelection || {});
        const voiceKey = voiceName || (profileId ? this.db.tts_profiles.find(p => p.id === profileId)?.voice : null);

        const cached = await this.getCachedTTS(option.text, language, voiceKey);
        if (cached) {
          results.cached++;
        } else {
          await this.generateTTS(option.text, language, voiceSelection, 'options', {
            topic_id: option.topicId,
            question_id: option.questionId,
            option_key: option.optionKey
          });
          results.success++;
        }
      } catch (error) {
        results.failed++;
        results.errors.push({ questionId: option.questionId, text: option.text.substring(0, 30), error: getErrorMessage(error) });
      }
      
      if (onProgress) {
        onProgress({ current: i + 1, total: results.total, percentage: ((i + 1) / results.total) * 100 });
      }
    }
    return results;
  }
  
  /**
   * Generate TTS for common phrases
   */
  async generatePhrasesTTS(language, voiceSelection, onProgress) {
    let languagePhrases = this.db.language_phrases.find(lp => lp.language === language);
    if (!languagePhrases) {
      languagePhrases = {
        id: `phrases_${language}_${Date.now()}`,
        language,
        phrases: this.getDefaultPhrases(language),
        audio_files: {}
      };
      this.db.language_phrases.push(languagePhrases);
    }
    
    const phrases = languagePhrases.phrases;
    const phraseKeys = Object.keys(phrases);
    const results = { total: phraseKeys.length, success: 0, failed: 0, cached: 0, errors: [] };
    
    for (let i = 0; i < phraseKeys.length; i++) {
      const key = phraseKeys[i];
      const text = phrases[key];
      try {
        const { profileId, voiceName } = (typeof voiceSelection === 'string') ? { profileId: voiceSelection } : (voiceSelection || {});
        const voiceKey = voiceName || (profileId ? this.db.tts_profiles.find(p => p.id === profileId)?.voice : null);

        const cached = await this.getCachedTTS(text, language, voiceKey);
        let audioUrl;
        if (cached) {
          audioUrl = cached.audio_url;
          results.cached++;
        } else {
          const ttsResult = await this.generateTTS(text, language, voiceSelection, 'phrases');
          audioUrl = ttsResult.audio_url;
          results.success++;
        }
        languagePhrases.audio_files[key] = audioUrl;
      } catch (error) {
        results.failed++;
        results.errors.push({ key, text, error: getErrorMessage(error) });
      }
      if (onProgress) {
        onProgress({ current: i + 1, total: results.total, percentage: ((i + 1) / results.total) * 100 });
      }
    }
    return results;
  }
  
  /**
   * Get default phrases for a language
   */
  getDefaultPhrases(language) {
    const phraseLanguage = {
      'bn-IN': 'bengali',
      'hi-IN': 'hindi',
      'kn-IN': 'kannada',
      'ta-IN': 'tamil',
      'te-IN': 'telugu'
    }[language] || language;

    const defaultPhrases = {
      first_question: "First Question",
      next_question: "Next Question",
      last_question: "Last Question",
      option_a: "Option A",
      option_b: "Option B",
      option_c: "Option C",
      option_d: "Option D",
      option_1: "Option 1",
      option_2: "Option 2",
      option_3: "Option 3",
      option_4: "Option 4"
    };
    
    const translations = {
      bengali: { first_question: "প্রথম প্রশ্ন", next_question: "পরবর্তী প্রশ্ন", last_question: "শেষ প্রশ্ন", option_a: "বিকল্প এ", option_b: "বিকল্প বি", option_c: "বিকল্প সি", option_d: "বিকল্প ডি", option_1: "বিকল্প ১", option_2: "বিকল্প ২", option_3: "বিকল্প ৩", option_4: "বিকল্প ৪" },
      hindi: { first_question: "पहला प्रश्न", next_question: "अगला सवाल", last_question: "अंतिम प्रश्न", option_a: "विकल्प A", option_b: "विकल्प B", option_c: "विकल्प C", option_d: "विकल्प D", option_1: "विकल्प १", option_2: "विकल्प २", option_3: "विकल्प ३", option_4: "विकल्प ४" },
      kannada: { first_question: "ಮೊದಲ ಪ್ರಶ್ನೆ", next_question: "ಮುಂದಿನ ಪ್ರಶ್ನೆ", last_question: "ಕೊನೆಯ ಪ್ರಶ್ನೆ", option_a: "ಆಯ್ಕೆ A", option_b: "ಆಯ್ಕೆ B", option_c: "ಆಯ್ಕೆ C", option_d: "ಆಯ್ಕೆ D", option_1: "ಆಯ್ಕೆ ೧", option_2: "ಆಯ್ಕೆ ೨", option_3: "ಆಯ್ಕೆ ೩", option_4: "ಆಯ್ಕೆ ೪" },
      tamil: { first_question: "முதல் கேள்வி", next_question: "அடுத்த கேள்வி", last_question: "கடைசி கேள்வி", option_a: "விருப்பம் A", option_b: "விருப்பம் B", option_c: "விருப்பம் C", option_d: "விருப்பம் D", option_1: "விருப்பம் ௧", option_2: "விருப்பம் ௨", option_3: "விருப்பம் ௩", option_4: "விருப்பம் ௪" },
      telugu: { first_question: "మొదటి ప్రశ్న", next_question: "తదుపరి ప్రశ్న", last_question: "చివరి ప్రశ్న", option_a: "ఎంపిక A", option_b: "ఎంపిక B", option_c: "ఎంపిక C", option_d: "ఎంపిక D", option_1: "ఎంపిక ౧", option_2: "ఎంపిక ౨", option_3: "ఎంపిక ౩", option_4: "ఎంపిక ౪" }
    };
    
    return translations[phraseLanguage] || defaultPhrases;
  }
  
  toGoogleLanguageCode(appLanguage) {
    const map = {
      bengali: 'bn-IN',
      english: 'en-US',
      english_plain: 'en-US',
      hindi: 'hi-IN',
      tamil: 'ta-IN',
      telugu: 'te-IN',
      kannada: 'kn-IN',
      malayalam: 'ml-IN',
      marathi: 'mr-IN',
      gujarati: 'gu-IN',
      odia: 'or-IN'
    };
    if (typeof appLanguage === 'string' && appLanguage.includes('-')) return appLanguage;
    return map[appLanguage] || 'en-US';
  }

  /**
   * Get full voice catalog for a provider
   */
  async getAllVoices(options = {}) {
    const { provider = 'google', refresh = false } = options;
    try {
      if (provider === 'edge') {
        if (!refresh && this.db.edge_voice_catalog?.voices?.length) {
          return this.db.edge_voice_catalog.voices;
        }
        const rawVoices = await listEdgeVoices();
        // Normalize Edge voices to match our common format
        const voices = (rawVoices || []).map(v => ({
          name: v.ShortName || v.Name,
          friendlyName: v.FriendlyName || v.Name,
          languageCodes: [v.Locale],
          ssmlGender: v.Gender ? v.Gender.toUpperCase() : 'NEUTRAL',
          provider: 'edge'
        }));
        this.db.edge_voice_catalog = { provider: 'edge', voices, updated_at: new Date().toISOString() };
        this.ensureIndianEdgeVoicesEnabled();
        return voices;
      } else {
        if (!this.isGoogleTTSAvailable()) return [];
        if (!refresh && this.db.tts_voice_catalog?.voices?.length) {
          return this.db.tts_voice_catalog.voices;
        }
        const [result] = await this.client.listVoices({});
        const voices = (result?.voices || []).map(voice => ({
          name: voice.name,
          languageCodes: voice.languageCodes,
          ssmlGender: voice.ssmlGender,
          naturalSampleRateHertz: voice.naturalSampleRateHertz
        }));
        this.db.tts_voice_catalog = { provider: 'google', voices, updated_at: new Date().toISOString() };
        return voices;
      }
    } catch (error) {
      console.error(`Error fetching ${provider} voices:`, error);
      return provider === 'edge' ? (this.db.edge_voice_catalog?.voices || EDGE_VOICES || []) : [];
    }
  }

  /**
   * Get enabled voices from the database
   */
  getEnabledVoices() {
    return this.db.tts_enabled_voices || [];
  }

  /**
   * Save enabled voices to the database
   */
  setEnabledVoices(voices) {
    this.db.tts_enabled_voices = voices;
  }

  /**
   * Auto-populate enabled voices with Indian-language Edge TTS voices.
   * Returns the list of voices that were added.
   */
  ensureIndianEdgeVoicesEnabled() {
    const catalog = this.db.edge_voice_catalog?.voices || EDGE_VOICES || [];
    if (catalog.length === 0) return [];

    const existing = this.db.tts_enabled_voices || [];
    const retained = existing.filter(v => (v.provider || 'google') !== 'edge' || isIndianEdgeVoice(v));
    const existingNames = new Set(retained.map(v => v.name));

    const toAdd = catalog
      .filter(isIndianEdgeVoice)
      .filter(v => !existingNames.has(v.name))
      .map(v => ({ ...v, provider: 'edge' }));
    if (toAdd.length > 0 || retained.length !== existing.length) {
      this.db.tts_enabled_voices = [...retained, ...toAdd];
    }

    return toAdd;
  }

  autoPopulateAllEdgeVoices() {
    return this.ensureIndianEdgeVoicesEnabled();
  }

  getCacheStats() {
    const stats = { total: this.db.tts_cache.length, by_category: {}, by_language: {}, by_profile: {}, by_provider: {}, total_duration: 0, disk_usage: 0 };
    this.db.tts_cache.forEach(entry => {
      stats.by_category[entry.category] = (stats.by_category[entry.category] || 0) + 1;
      stats.by_language[entry.language] = (stats.by_language[entry.language] || 0) + 1;
      stats.by_profile[entry.profile_id] = (stats.by_profile[entry.profile_id] || 0) + 1;
      stats.by_provider[entry.provider || 'google'] = (stats.by_provider[entry.provider || 'google'] || 0) + 1;
      stats.total_duration += entry.duration || 0;
      try {
        const filepath = path.join(__dirname, '..', entry.audio_url);
        if (fs.existsSync(filepath)) {
          stats.disk_usage += fs.statSync(filepath).size;
        }
      } catch (e) {}
    });
    return stats;
  }
  
  setDefaultVersion(cacheId) {
    const entry = this.db.tts_cache.find(c => c.id === cacheId);
    if (!entry) throw new Error('Cache entry not found');
    this.db.tts_cache.forEach(c => {
      if (c.text_hash === entry.text_hash && c.voice_name === entry.voice_name) {
        c.is_default = (c.id === cacheId);
      }
    });
    return entry;
  }
  
  clearCache(options = {}) {
    const { ids } = options;
    if (!Array.isArray(ids) || ids.length === 0) return { removed: 0 };
    const idSet = new Set(ids);
    let removed = 0;
    this.db.tts_cache = this.db.tts_cache.filter(entry => {
      if (!idSet.has(entry.id)) return true;
      try {
        const filepath = path.join(__dirname, '..', entry.audio_url);
        if (fs.existsSync(filepath)) fs.unlinkSync(filepath);
      } catch (e) { console.error('Error deleting file:', e); }
      removed++;
      return false;
    });
    return { removed };
  }
}
