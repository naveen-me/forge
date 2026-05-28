import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';
import crypto from 'crypto';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import { parseFile } from 'music-metadata';
import { runFfmpeg } from './ffmpegUtils.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export class VideoGenerator {
  constructor(db) {
    this.db = db;
    this.outputDir = path.join(__dirname, '../videos');

    // Ensure output directory exists
    if (!fs.existsSync(this.outputDir)) {
      fs.mkdirSync(this.outputDir, { recursive: true });
    }

    // Clean up any orphaned temporary HTML files on startup
    this.cleanupTempFiles();
  }

  /**
   * Clean up temporary render HTML files
   */
  cleanupTempFiles() {
    try {
      if (!fs.existsSync(this.outputDir)) {
        return;
      }

      const files = fs.readdirSync(this.outputDir);
      const tempFiles = files.filter(f => f.startsWith('render_') && f.endsWith('.html'));

      if (tempFiles.length > 0) {
        console.log(`Cleaning up ${tempFiles.length} temporary render files...`);
        tempFiles.forEach(file => {
          const filePath = path.join(this.outputDir, file);
          try {
            fs.unlinkSync(filePath);
          } catch (error) {
            console.error(`Failed to delete ${file}:`, error.message);
          }
        });
        console.log('Temporary files cleaned up successfully');
      }
    } catch (error) {
      console.error('Error during temp file cleanup:', error);
    }
  }

  /**
   * Generate video for a set of questions using a preset
   */
  async getLocalMediaDurationSeconds(url) {
    try {
      if (!url) return 0;

      // Only support local storage paths like /storage/media/videos/... or /storage/media/images/...
      if (!url.startsWith('/')) return 0;

      const relativePath = url.startsWith('/') ? url.substring(1) : url;
      const absolutePath = path.join(__dirname, '..', relativePath);

      if (!fs.existsSync(absolutePath)) return 0;

      const metadata = await parseFile(absolutePath);
      const dur = metadata?.format?.duration;
      return dur ? Number(dur) : 0;
    } catch (e) {
      console.warn('Failed to read media duration for', url, e.message);
      return 0;
    }
  }

  async generateVideo(questionIds, presetId, { onProgress, voiceName, profileId, customName, setId, language } = {}) {

    let htmlPath = null;

    try {
      if (typeof onProgress === 'function') onProgress(1);
      console.log(`Starting video generation for preset ${presetId} with ${questionIds.length} questions ${setId ? `[TTS Set: ${setId}]` : ''}`);

      // 1. Load preset
      const preset = this.loadPreset(presetId);
      if (!preset) {
        throw new Error(`Preset ${presetId} not found`);
      }

      // 2. Load questions
      const setVoiceKey = setId ? (this.db.tts_sets.find(s => s.id === setId)?.voice_name || null) : null;
      const voiceKey = setVoiceKey || this.resolveVoiceKey({ preset, voiceName, profileId });
      const questions = this.loadQuestions(questionIds, { voiceKey, setId, language });
      if (questions.length === 0) {
        throw new Error('No questions found');
      }

      // 3. Calculate total duration
      const animConfig = preset.config.animation || {
        question_display_duration: 2,
        options_display_mode: 'all_at_once',
        option_reveal_delay: 0.5,
        answer_reveal_duration: 3,
        transition_duration: 0.3
      };
      const audioConfig = preset.config.audio || {};
      const timerDuration = preset.config.timer?.duration || 5;

      const perQuestionDurations = [];
      for (const q of questions) {
        // Calculate dynamic durations based on TTS audio if enabled
        const qDur = await this.getAudioDurationSeconds(q.tts_audio.question);

        let optionsDur = 0;
        const opts = q.tts_audio.options || [];
        if (audioConfig.play_options !== false) {
            if (animConfig.options_display_mode === 'one_by_one') {
                for (let i = 0; i < opts.length; i++) {
                    optionsDur += Number(animConfig.option_reveal_delay || 0.5);
                    optionsDur += await this.getAudioDurationSeconds(opts[i]);
                }
            } else {
                for (let i = 0; i < opts.length; i++) {
                    optionsDur += await this.getAudioDurationSeconds(opts[i]);
                }
            }
        } else {
            if (animConfig.options_display_mode === 'one_by_one') {
                optionsDur = opts.length * Number(animConfig.option_reveal_delay || 0.5);
            } else {
                optionsDur = Number(animConfig.transition_duration || 0.3);
            }
        }

        let answerRevealDur = Number(animConfig.answer_reveal_duration || 3);
        if (audioConfig.play_correct_answer !== false) {
            let correctAudioDur = 0;
            if (audioConfig.play_correct_answer_phrase !== false) {
                correctAudioDur += await this.getAudioDurationSeconds(q.tts_audio.correct_phrase);
            }
            correctAudioDur += await this.getAudioDurationSeconds(opts[q.correct_answer_index]);
            answerRevealDur = Math.max(answerRevealDur, correctAudioDur + 1); // +1s buffer
        }

        const qRevealDur = Math.max(Number(animConfig.question_display_duration || 2), qDur + 0.5);

        const total = qRevealDur + optionsDur + timerDuration + answerRevealDur;

        perQuestionDurations.push({
            total,
            qRevealDur,
            optionsDur,
            timerDuration,
            answerRevealDur
        });
      }

      const intro = preset.config.canvas?.intro || { type: 'none' };
      const outro = preset.config.canvas?.outro || { type: 'none' };

      const introDuration = intro.type === 'image'
        ? Number(intro.duration || 3)
        : (intro.type === 'video' ? (await this.getLocalMediaDurationSeconds(intro.url)) || 5 : 0);

      const outroDuration = outro.type === 'image'
        ? Number(outro.duration || 3)
        : (outro.type === 'video' ? (await this.getLocalMediaDurationSeconds(outro.url)) || 5 : 0);

      const questionsDuration = perQuestionDurations.reduce((a, b) => a + b.total, 0);
      const totalDuration = questionsDuration + introDuration + outroDuration;

      console.log(`Total video duration: ${totalDuration} seconds`);

      // 4. Create HTML render page
      const port = process.env.PORT || 3001;
      const baseUrl = process.env.PUBLIC_BASE_URL || `http://localhost:${port}`;
      const htmlContent = this.createRenderPage(preset, questions, baseUrl, totalDuration, perQuestionDurations);
      htmlPath = path.join(this.outputDir, `render_${Date.now()}.html`);
      fs.writeFileSync(htmlPath, htmlContent);

      // 5. Generate video with Playwright (0-85%)
      let videoPath = await this.recordVideo(htmlPath, preset, totalDuration, {
        onProgress: (p) => {
          if (typeof onProgress === 'function') {
            // Scale Playwright's 0-100 to 1-85
            onProgress(Math.floor(1 + (p * 0.84)));
          }
        },
        customName
      });

      // 5b. If enabled, mux TTS audio into the video (85-98%)
      try {
        const ttsEnabled = !!(preset?.config?.animation?.tts_enabled);
        const ttsOverride = !!(voiceName || profileId || setId);

        if (ttsEnabled || ttsOverride) {
          if (typeof onProgress === 'function') onProgress(86);
          console.log('Attempting to mux TTS audio into video...');
          const muxed = await this.muxTTSAudioIntoVideo({
            videoPath,
            preset,
            questions,
            totalDurationSeconds: totalDuration,
            customName
          });
          if (muxed) {
            if (typeof onProgress === 'function') onProgress(98);
            console.log(`TTS audio muxed successfully: ${muxed}`);
            videoPath = muxed;
          }
        }
      } catch (e) {
        console.error('TTS muxing failed:', e.message);
      }

      // 6. Finalizing (98-100%)
      if (typeof onProgress === 'function') onProgress(99);
      if (customName) {
        const sanitizedName = customName.replace(/[^a-zA-Z0-9_-]/g, '_');
        const ext = path.extname(videoPath);
        const newPath = path.join(this.outputDir, `${sanitizedName}${ext}`);

        let finalPath = newPath;
        if (fs.existsSync(finalPath)) {
          finalPath = path.join(this.outputDir, `${sanitizedName}_${Date.now()}${ext}`);
        }

        fs.renameSync(videoPath, finalPath);
        videoPath = finalPath;
      }

      if (htmlPath && fs.existsSync(htmlPath)) {
        fs.unlinkSync(htmlPath);
      }

      if (typeof onProgress === 'function') onProgress(100);
      return {
        path: videoPath,
        duration: totalDuration,
        questionCount: questions.length,
        customName: customName || null
      };

    } catch (error) {
      if (htmlPath && fs.existsSync(htmlPath)) {
        try { fs.unlinkSync(htmlPath); } catch (e) {}
      }
      throw error;
    }
  }

  resolveVoiceKey({ preset, voiceName, profileId } = {}) {
    // We store TTS cache entries with tts.profile_id (historically used as "voiceKey")
    // and compute hashes based on `${text}|${language}|${voiceKey}`.
    // Precedence:
    // 1) explicit profileId (future)
    // 2) explicit voiceName
    // 3) preset-configured voice/profile (if present)
    const cfg = preset?.config || {};
    const anim = cfg.animation || {};
    const candidate = profileId || voiceName || anim.tts_profile_id || anim.tts_voice_name || anim.tts_voice || null;
    return candidate || null;
  }

  /**
   * Load preset from database
   */
  loadPreset(presetId) {
    const preset = this.db.presets.find(p => p.id === presetId);
    if (!preset) return null;

    // Parse config if it's a string
    const config = typeof preset.config === 'string'
      ? JSON.parse(preset.config)
      : preset.config;

    return {
      ...preset,
      config
    };
  }

  /**
   * Load questions from database
   */
  loadQuestions(questionIds, { voiceKey, setId, language } = {}) {

    const questions = [];

    for (const id of questionIds) {
      const q = this.db.questions.find(question => question.id === id);
      if (q) {
        const options = Array.isArray(q.options)
          ? q.options
          : [q.option_1, q.option_2, q.option_3, q.option_4].filter(v => v !== undefined && v !== '');
        const correctIndex = Number.isInteger(q.correct_option)
          ? q.correct_option
          : (parseInt(q.correct_option, 10) || 0);

        const ttsVoiceKey = voiceKey || null;

        const findCachedEntry = (text, category) => {
          if (!text || !ttsVoiceKey) return null;
          const entries = this.db.tts_cache.filter(tts =>
            tts.category === category &&
            (tts.profile_id === ttsVoiceKey || tts.voice_name === ttsVoiceKey) &&
            tts.text === text
          );
          if (setId) {
            return entries.find(e => e.set_id === setId) || entries.find(e => e.is_default) || entries[0] || null;
          }
          return entries.find(e => e.is_default) || entries[0] || null;
        };

        // Find question TTS
        let questionTTS = null;
        questionTTS = findCachedEntry(q.question, 'questions');

        const optionsTTS = [];
        for (let i = 0; i < options.length; i++) {
          const optionText = options[i];
          let optTTS = null;
          optTTS = findCachedEntry(optionText, 'options');
          optionsTTS.push(optTTS ? optTTS.audio_url : null);
        }

        // Find phrases TTS (correct_answer_is)
        let correctPhraseTTS = null;
        if (language) {
          const lp = this.db.language_phrases.find(p => p.language === language);
          const phraseText = lp?.phrases?.correct_answer_is;
          if (phraseText) {
            correctPhraseTTS = findCachedEntry(phraseText, 'phrases');
          }
        }

        questions.push({
          id: q.id,
          question: q.question,
          options,
          correct_answer_index: correctIndex >= 0 ? correctIndex : 0,
          explanation: q.explanation || null,
          tts_audio: {
            question: questionTTS ? questionTTS.audio_url : null,
            options: optionsTTS,
            correct_phrase: correctPhraseTTS ? correctPhraseTTS.audio_url : null
          }
        });
      }
    }

    return questions;
  }

  /**
   * Generate hash for text to use as cache key (copied from TTSService)
   */
  generateTextHash(text, language, voiceKey) {
    const combined = `${text}|${language}|${voiceKey}`;
    return crypto.createHash('md5').update(combined, 'utf8').digest('hex');
  }

  /**
   * Convert relative URLs to absolute file:// URLs for Playwright
   */
  convertToAbsoluteURL(url, baseUrl = 'http://localhost:3001') {
    if (!url) return url;

    // If already absolute, return as-is
    if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('file://') || url.startsWith('data:')) {
      return url;
    }

    // For app-relative URLs ("/storage/..."), resolve against the running server.
    if (url.startsWith('/')) {
      return `${baseUrl}${url}`;
    }

    // For other relative URLs, also resolve against server root
    return `${baseUrl}/${url}`;
  }

  /**
   * Recursively convert all URLs in preset config to absolute paths
   */
  convertConfigURLs(config, baseUrl = 'http://localhost:3001') {
    const converted = JSON.parse(JSON.stringify(config)); // Deep clone

    // Convert canvas background
    if (converted.canvas?.background_url) {
      converted.canvas.background_url = this.convertToAbsoluteURL(converted.canvas.background_url, baseUrl);
    }

    // Convert intro/outro urls
    if (converted.canvas?.intro?.url) {
      converted.canvas.intro.url = this.convertToAbsoluteURL(converted.canvas.intro.url, baseUrl);
    }
    if (converted.canvas?.outro?.url) {
      converted.canvas.outro.url = this.convertToAbsoluteURL(converted.canvas.outro.url, baseUrl);
    }

    // Convert question background
    if (converted.question?.background_url) {
      converted.question.background_url = this.convertToAbsoluteURL(converted.question.background_url, baseUrl);
    }

    // Convert question font family to absolute path ONLY if it looks like a font file AND the file exists.
    if (converted.question?.font_family && /\.(ttf|otf|woff|woff2)$/i.test(converted.question.font_family)) {
      const fontFile = converted.question.font_family;
      const fontAbsPath = path.join(__dirname, '../storage/fonts', fontFile);
      if (fs.existsSync(fontAbsPath)) {
        converted.question.font_family_url = this.convertToAbsoluteURL(`/storage/fonts/${fontFile}`, baseUrl);
      } else {
        // If the font file isn't present, treat it as a system font name to avoid download/OTS warnings.
        converted.question.font_family = fontFile.replace(/\.(ttf|otf|woff|woff2)$/i, '');
        delete converted.question.font_family_url;
      }
    } else {
      delete converted.question?.font_family_url;
    }

    // Convert options backgrounds
    if (converted.options?.background_url) {
      converted.options.background_url = this.convertToAbsoluteURL(converted.options.background_url, baseUrl);
    }
    if (converted.options?.correct_answer_background_url) {
      converted.options.correct_answer_background_url = this.convertToAbsoluteURL(converted.options.correct_answer_background_url, baseUrl);
    }

    // Convert options font family to absolute path ONLY if it looks like a font file AND the file exists.
    if (converted.options?.font_family && /\.(ttf|otf|woff|woff2)$/i.test(converted.options.font_family)) {
      const fontFile = converted.options.font_family;
      const fontAbsPath = path.join(__dirname, '../storage/fonts', fontFile);
      if (fs.existsSync(fontAbsPath)) {
        converted.options.font_family_url = this.convertToAbsoluteURL(`/storage/fonts/${fontFile}`, baseUrl);
      } else {
        // If the font file isn't present, treat it as a system font name to avoid download/OTS warnings.
        converted.options.font_family = fontFile.replace(/\.(ttf|otf|woff|woff2)$/i, '');
        delete converted.options.font_family_url;
      }
    } else {
      delete converted.options?.font_family_url;
    }

    // Convert timer background
    if (converted.timer?.background_url) {
      converted.timer.background_url = this.convertToAbsoluteURL(converted.timer.background_url, baseUrl);
    }

    // Convert explanation backgrounds and images
    if (converted.explanation?.background_url) {
      converted.explanation.background_url = this.convertToAbsoluteURL(converted.explanation.background_url, baseUrl);
    }

    // Convert custom overlays
    if (converted.custom_overlays && Array.isArray(converted.custom_overlays)) {
      converted.custom_overlays.forEach(overlay => {
        if (overlay.url) {
          overlay.url = this.convertToAbsoluteURL(overlay.url, baseUrl);
        }
      });
    }

    return converted;
  }

  /**
   * Create HTML page for rendering
   * This page uses the SAME renderingEngine.js as the preview!
   */
  createRenderPage(preset, questions, baseUrl = 'http://localhost:3001', expectedTotalDurationSeconds = null, questionDurations = []) {
    const renderingEnginePath = path.join(__dirname, '../shared/renderingEngine.js');
    const renderingEngineCode = fs.readFileSync(renderingEnginePath, 'utf-8');

    // Convert all URLs in preset config to absolute http:// URLs (so Playwright loads assets exactly like preview)
    const convertedConfig = this.convertConfigURLs(preset.config, baseUrl);

    return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Video Render</title>
  <base href="${baseUrl}/">
  <style>
    body {
      margin: 0;
      padding: 0;
      background: #000;
      overflow: hidden;
    }
    canvas {
      display: block;
    }
    #overlay {
      position: fixed;
      left: 0;
      top: 0;
      width: 100vw;
      height: 100vh;
      background: #000;
      display: none;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    }
    #overlay video,
    #overlay img {
      width: 100%;
      height: 100%;
    }
  </style>
</head>
<body>
  <div id="overlay"></div>
  <canvas id="canvas"></canvas>

  <script type="module">
    // Inline the rendering engine code
    ${renderingEngineCode}

    // Configuration and data (with absolute file:// URLs)
    const presetConfig = ${JSON.stringify(convertedConfig)};
    const questions = ${JSON.stringify(questions)};
    const questionDurations = ${JSON.stringify(questionDurations)};
    const expectedTotalDurationSeconds = ${expectedTotalDurationSeconds == null ? 'null' : Number(expectedTotalDurationSeconds)};

    // Initialize renderer
    const canvas = document.getElementById('canvas');
    const overlay = document.getElementById('overlay');
    let currentQuestionIndex = 0;
    let renderer = null;

    function clearOverlay() {
      overlay.innerHTML = '';
      overlay.style.display = 'none';
    }

    async function playScene(scene) {
      console.log('playScene called with:', JSON.stringify(scene));

      if (!scene || !scene.type || scene.type === 'none' || !scene.url) {
        console.log('Skipping scene - missing required properties:', {
          hasScene: !!scene,
          type: scene?.type,
          hasUrl: !!scene?.url
        });
        return;
      }

      overlay.style.display = 'flex';
      overlay.innerHTML = '';

      const fit = scene.fit || 'cover';

      if (scene.type === 'image') {
        const img = document.createElement('img');
        img.src = scene.url;
        img.style.objectFit = fit;
        overlay.appendChild(img);

        // wait for load best-effort, then duration
        await new Promise(resolve => {
          img.onload = resolve;
          img.onerror = resolve;
        });

        const dur = Number(scene.duration || 3);
        await new Promise(r => setTimeout(r, dur * 1000));
        clearOverlay();
        return;
      }

      if (scene.type === 'video') {
        const vid = document.createElement('video');
        vid.src = scene.url;
        vid.muted = true;
        vid.playsInline = true;
        vid.style.objectFit = fit;
        overlay.appendChild(vid);

        await new Promise(resolve => {
          vid.onloadeddata = resolve;
          vid.onerror = resolve;
        });

        // play and wait until end
        try { await vid.play(); } catch(e) {}

        await new Promise(resolve => {
          // if ended fires, great; else fallback to duration
          const fallback = setTimeout(resolve, (vid.duration && isFinite(vid.duration) ? vid.duration : 5) * 1000);
          vid.onended = () => { clearTimeout(fallback); resolve(); };
        });

        clearOverlay();
      }
    }

    async function renderQuestion(index) {
      if (index >= questions.length) {
        console.log('All questions rendered');
        return;
      }

      console.log('Rendering question ' + (index + 1) + '/' + questions.length);

      const question = questions[index];
      const qDurationData = questionDurations[index];

      // Destroy previous renderer
      if (renderer) {
        renderer.destroy();
      }

      // Create new renderer with current question
      // IMPORTANT: Set isPreviewMode to false for video generation
      renderer = new CanvasRenderer(canvas, presetConfig, question, false);

      // Apply dynamic timings if available
      if (qDurationData) {
          renderer.animationConfig.question_display_duration = qDurationData.qRevealDur;
          renderer.animationConfig.options_display_duration = qDurationData.optionsDur;
          renderer.animationConfig.answer_reveal_duration = qDurationData.answerRevealDur;
      }

      // Preload assets (never hang forever)
      try {
        await Promise.race([
          renderer.preloadAssets(),
          new Promise(resolve => setTimeout(resolve, 5000))
        ]);
      } catch (e) {
        console.warn('Asset preload failed:', e);
      }

      // Start animation
      renderer.startAnimation();

      // Optional: play TTS (Designer option)
      const ttsEnabled = !!(presetConfig?.animation?.tts_enabled);
      const audioConfig = presetConfig?.audio || {};

      if (ttsEnabled) {
        try {
          // Question audio first
          await renderer.playTTSForQuestion();

          // Speak options if enabled
          if (audioConfig.play_options !== false) {
              // If options are shown one-by-one, speak each option as it appears.
              if (presetConfig?.animation?.options_display_mode === 'one_by_one') {
                const delay = Number(presetConfig?.animation?.option_reveal_delay || 0.5);
                const optionsCount = question?.options?.length || 0;
                for (let i = 0; i < optionsCount; i++) {
                  await new Promise(r => setTimeout(r, Math.max(0, delay) * 1000));
                  await renderer.playTTSForOption(i);
                }
              } else {
                // All-at-once: speak options sequentially immediately after question.
                const optionsCount = question?.options?.length || 0;
                for (let i = 0; i < optionsCount; i++) {
                  await renderer.playTTSForOption(i);
                }
              }
          }

          // Wait for timer to finish (approximate sync with animation)
          const waitBeforeAnswer = (qDurationData ? (qDurationData.qRevealDur + qDurationData.optionsDur + qDurationData.timerDuration) : 0);
          const elapsedSoFar = renderer.getCurrentTime();
          const remainingWait = (waitBeforeAnswer - elapsedSoFar);

          if (remainingWait > 0) {
              await new Promise(r => setTimeout(r, remainingWait * 1000));
          }

          // Play correct answer audio if enabled
          if (audioConfig.play_correct_answer !== false) {
              if (audioConfig.play_correct_answer_phrase !== false && question.tts_audio.correct_phrase) {
                  const audio = new Audio(question.tts_audio.correct_phrase);
                  await new Promise(r => {
                      audio.onended = r;
                      audio.onerror = r;
                      audio.play().catch(r);
                  });
              }
              await renderer.playTTSForOption(question.correct_answer_index);
          }

        } catch (e) {
          console.warn('TTS playback failed:', e);
        }
      }

      // Calculate duration for this question
      const questionDuration = qDurationData ? qDurationData.total : renderer.getTotalDuration();

      // Wait for question duration (from start of question)
      const totalElapsed = renderer.getCurrentTime();
      const timeLeft = questionDuration - totalElapsed;
      if (timeLeft > 0) {
          await new Promise(resolve => setTimeout(resolve, timeLeft * 1000));
      }

      // Move to next question
      currentQuestionIndex++;
      await renderQuestion(currentQuestionIndex);
    }

    // Start rendering when page loads
    window.addEventListener('load', async () => {
      console.log('Starting video render...');
      window.videoComplete = false;
      window.videoError = null;

      // Hard completion fallback: even if something goes wrong in the render loop,
      // we must never hang Playwright indefinitely.
      if (typeof expectedTotalDurationSeconds === 'number' && isFinite(expectedTotalDurationSeconds)) {
        setTimeout(() => {
          if (!window.videoComplete) {
            console.warn('Hard completion timeout reached. Forcing completion.');
            window.videoComplete = true;
            window.videoError = window.videoError || 'Hard completion timeout reached';
          }
        }, Math.ceil(expectedTotalDurationSeconds * 1000) + 8000);
      }

      try {
        // Intro
        await playScene(presetConfig.canvas?.intro);

        // Questions
        await renderQuestion(0);

        // Outro
        await playScene(presetConfig.canvas?.outro);

        console.log('Video render complete!');
        window.videoComplete = true;
      } catch (e) {
        console.error('Render failed:', e);
        window.videoError = String(e?.message || e);
        // Ensure Playwright does not hang forever
        window.videoComplete = true;
      }
    });

    // Expose for external control
    window.getProgress = () => {
      return {
        current: currentQuestionIndex,
        total: questions.length,
        percentage: (currentQuestionIndex / questions.length) * 100
      };
    };
  </script>
</body>
</html>`;
  }

  async getAudioDurationSeconds(audioUrl) {
    try {
      if (!audioUrl || typeof audioUrl !== 'string') return 0;
      if (!audioUrl.startsWith('/')) return 0;
      const relativePath = audioUrl.substring(1);
      const absolutePath = path.join(__dirname, '..', relativePath);
      if (!fs.existsSync(absolutePath)) return 0;
      const metadata = await parseFile(absolutePath);
      const dur = metadata?.format?.duration;
      return dur ? Number(dur) : 0;
    } catch (e) {
      return 0;
    }
  }

  async muxTTSAudioIntoVideo({ videoPath, preset, questions, totalDurationSeconds, customName }) {
    // Build a timeline that matches the in-page logic in createRenderPage():
    const anim = preset?.config?.animation || {};
    const audioCfg = preset?.config?.audio || {};
    const canvasCfg = preset?.config?.canvas || {};

    const intro = canvasCfg.intro || { type: 'none' };
    const introDuration = intro.type === 'image'
      ? Number(intro.duration || 3)
      : (intro.type === 'video' ? (await this.getLocalMediaDurationSeconds(intro.url)) || 5 : 0);

    const optionsMode = anim.options_display_mode || 'all_at_once';
    const optionRevealDelay = Number(anim.option_reveal_delay || 0.5);
    const timerDuration = preset?.config?.timer?.duration || 5;

    const segments = [];
    let cursor = Math.max(0, introDuration);

    for (const q of questions) {
      let t = cursor;

      // 1. Question audio
      const qAudio = q?.tts_audio?.question;
      let qDur = 0;
      if (qAudio) {
        segments.push({ url: qAudio, startSeconds: t });
        qDur = await this.getAudioDurationSeconds(qAudio);
      }

      const qRevealDur = Math.max(Number(anim.question_display_duration || 2), qDur + 0.5);
      t = cursor + qRevealDur;

      // 2. Options audio
      const opts = Array.isArray(q?.tts_audio?.options) ? q.tts_audio.options : [];
      const optionsCount = Math.min(opts.length, q?.options?.length || opts.length);

      let optionsDur = 0;
      if (audioCfg.play_options !== false) {
          if (optionsMode === 'one_by_one') {
            for (let i = 0; i < optionsCount; i++) {
              t += optionRevealDelay;
              const oAudio = opts[i];
              if (oAudio) {
                segments.push({ url: oAudio, startSeconds: t });
                const od = await this.getAudioDurationSeconds(oAudio);
                t += od;
              }
            }
          } else {
            for (let i = 0; i < optionsCount; i++) {
              const oAudio = opts[i];
              if (oAudio) {
                segments.push({ url: oAudio, startSeconds: t });
                const od = await this.getAudioDurationSeconds(oAudio);
                t += od;
              }
            }
          }
          optionsDur = t - (cursor + qRevealDur);
      } else {
          if (optionsMode === 'one_by_one') {
              optionsDur = optionsCount * optionRevealDelay;
          } else {
              optionsDur = Number(anim.transition_duration || 0.3);
          }
      }

      // 3. Timer (just visual, but we need to know when it ends)
      t = cursor + qRevealDur + optionsDur + timerDuration;

      // 4. Correct answer audio
      let answerRevealDur = Number(anim.answer_reveal_duration || 3);
      if (audioCfg.play_correct_answer !== false) {
          let at = t;
          if (audioCfg.play_correct_answer_phrase !== false && q.tts_audio.correct_phrase) {
              segments.push({ url: q.tts_audio.correct_phrase, startSeconds: at });
              at += await this.getAudioDurationSeconds(q.tts_audio.correct_phrase);
          }

          const correctOptAudio = opts[q.correct_answer_index];
          if (correctOptAudio) {
              segments.push({ url: correctOptAudio, startSeconds: at });
              at += await this.getAudioDurationSeconds(correctOptAudio);
          }
          answerRevealDur = Math.max(answerRevealDur, (at - t) + 1);
      }

      cursor += qRevealDur + optionsDur + timerDuration + answerRevealDur;
    }

    // If no audio segments, do nothing
    if (!segments.length) {
      console.log('No TTS audio segments found - skipping audio muxing');
      return null;
    }

    console.log(`Found ${segments.length} TTS audio segments to mux:`, segments.map(s => ({ url: s.url, startSeconds: s.startSeconds })));

    // Convert audio URLs to absolute file paths and ensure they exist.
    const audioInputs = segments
      .map(s => {
        if (!s.url.startsWith('/')) {
          console.warn(`Audio URL does not start with '/': ${s.url}`);
          return null;
        }
        const abs = path.join(__dirname, '..', s.url.substring(1));
        if (!fs.existsSync(abs)) {
          console.warn(`Audio file does not exist: ${abs}`);
          return null;
        }
        return { ...s, abs };
      })
      .filter(Boolean);

    if (!audioInputs.length) {
      console.warn('No valid audio files found - skipping audio muxing');
      return null;
    }

    console.log(`Muxing ${audioInputs.length} audio files into video`);

    let outPath = videoPath.replace(/\.webm$/i, '') + '_tts.webm';

    // If custom name is provided, use it for the TTS version too
    if (customName) {
      const sanitizedName = customName.replace(/[^a-zA-Z0-9_-]/g, '_');
      outPath = path.join(path.dirname(videoPath), `${sanitizedName}_tts.webm`);

      // Handle existing files
      if (fs.existsSync(outPath)) {
        const timestamp = Date.now();
        outPath = path.join(path.dirname(videoPath), `${sanitizedName}_tts_${timestamp}.webm`);
      }
    }

    // Build ffmpeg args:
    // input0: video
    // inputs1..N: audio segments
    const args = ['-i', videoPath];
    for (const a of audioInputs) {
      args.push('-i', a.abs);
    }

    const totalMs = Math.max(0, Number(totalDurationSeconds || 0)) * 1000;

    // filter_complex:
    // [1:a]adelay=ms|ms,apad[a1]; ...; anullsrc=... [base];
    // [base][a1][a2]... amix=inputs=...:duration=longest[aout]
    const filterParts = [];

    // base silence
    // Use 48k to match opus defaults.
    filterParts.push(`anullsrc=r=48000:cl=stereo,atrim=0:${(Number(totalDurationSeconds || 0)).toFixed(3)}[base]`);

    audioInputs.forEach((a, idx) => {
      const inLabel = `${idx + 1}:a`;
      const outLabel = `a${idx + 1}`;
      const delayMs = Math.max(0, Math.round(a.startSeconds * 1000));
      // Normalize each input to 48k stereo before delaying
      filterParts.push(`[${inLabel}]aresample=48000,aformat=channel_layouts=stereo,adelay=${delayMs}|${delayMs},apad,atrim=0:${(Number(totalDurationSeconds || 0)).toFixed(3)}[${outLabel}]`);
    });

    const mixInputs = ['[base]', ...audioInputs.map((_, idx) => `[a${idx + 1}]`)].join('');
    // Use volume adjustment to prevent clipping if many tracks overlap
    const volumeAdj = audioInputs.length > 3 ? `,volume=${(1/Math.sqrt(audioInputs.length)).toFixed(2)}` : '';
    filterParts.push(`${mixInputs}amix=inputs=${audioInputs.length + 1}:duration=longest:dropout_transition=0${volumeAdj}[aout]`);

    args.push(
      '-filter_complex',
      filterParts.join(';'),
      '-map', '0:v:0',
      '-map', '[aout]',
      '-c:v', 'copy',
      '-c:a', 'libopus',
      '-b:a', '128k',
      '-t', String(Number(totalDurationSeconds || 0)),
      outPath
    );

    await runFfmpeg(args, { logPrefix: 'ffmpeg-tts' });
    return outPath;
  }

  /**
   * Record video using Playwright
   */
  async recordVideo(htmlPath, preset, duration, { onProgress, customName } = {}) {
    let browser = null;
    let context = null;

    try {
      browser = await chromium.launch({
        headless: true,
        args: ['--disable-web-security', '--disable-features=IsolateOrigins,site-per-process']
      });

      const videoFileName = `video_${Date.now()}.webm`;
      const videoPath = path.join(this.outputDir, videoFileName);

      // Create context with video recording
      context = await browser.newContext({
        viewport: {
          width: preset.config.canvas.width,
          height: preset.config.canvas.height
        },
        recordVideo: {
          dir: this.outputDir,
          size: {
            width: preset.config.canvas.width,
            height: preset.config.canvas.height
          }
        }
      });

      const page = await context.newPage();

      // Listen to console messages from the browser
      page.on('console', msg => {
        const type = msg.type();
        const text = msg.text();
        if (type === 'error') {
          console.error('[Browser Error]', text);
        } else if (type === 'warning') {
          console.warn('[Browser Warning]', text);
        } else {
          console.log('[Browser]', text);
        }
      });

      // Listen to page errors
      page.on('pageerror', error => {
        console.error('[Browser Page Error]', error.message);
      });

      // Calculate timeout for this video
      // Formula: duration * 1.5 + 60 seconds buffer, minimum 2 minutes
      const timeout = Math.max((duration * 1.5 + 60) * 1000, 120000);
      console.log(`Waiting for ${duration} seconds of rendering... (timeout: ${timeout/1000}s)`);

      // Set page timeout to match our calculated timeout
      page.setDefaultTimeout(timeout);
      page.setDefaultNavigationTimeout(timeout);

      // Load the render page.
      // Using file:// causes ERR_FILE_NOT_FOUND for /storage/* and font loading issues.
      // We serve assets via the running Express server and use a base href in the HTML.
      await page.goto(`file://${htmlPath}`, { waitUntil: 'domcontentloaded' });

      // Poll progress while waiting for completion
      const startTs = Date.now();
      let lastPct = -1;
      const progressTimer = setInterval(async () => {
        try {
          if (!page || page.isClosed()) return;

          const prog = await page.evaluate(() => {
            if (typeof window.getProgress !== 'function') return null;
            return window.getProgress();
          });

          const pct = prog?.percentage;
          if (typeof pct === 'number' && isFinite(pct)) {
            // Scale progress from 0-95% (remaining 5% for finalizing video)
            const rounded = Math.max(0, Math.min(95, Math.floor(pct)));
            if (rounded > lastPct) {
              lastPct = rounded;
              if (typeof onProgress === 'function') onProgress(rounded);
            }
          } else {
            // fallback based on elapsed time if no progress available
            const elapsed = (Date.now() - startTs) / 1000;
            const est = Math.max(1, duration);
            const fallbackPct = Math.max(0, Math.min(90, Math.floor((elapsed / est) * 100)));
            if (fallbackPct > lastPct) {
              lastPct = fallbackPct;
              if (typeof onProgress === 'function') onProgress(fallbackPct);
            }
          }
        } catch (e) {
          // ignore page closed or evaluation errors during shutdown
        }
      }, 1000);

      try {
        // Wait for rendering to complete.
        // We use a slightly longer timeout than the hard deadline to allow for asset loading.
        const hardDeadlineMs = Math.ceil((duration + 20) * 1000);

        console.log(`Waiting for videoComplete signal or hard deadline (${hardDeadlineMs/1000}s)...`);

        await Promise.race([
          page.waitForFunction('window.videoComplete === true', { timeout }),
          page.waitForTimeout(hardDeadlineMs)
        ]);

        console.log('Completion signal received or deadline reached');
      } finally {
        clearInterval(progressTimer);
      }

      // If render page reported an error, surface it in logs
      try {
        const err = await page.evaluate(() => window.videoError || null);
        if (err) {
          console.error('❌ Render page reported error:', err);
        }
      } catch (e) {}

      if (typeof onProgress === 'function') onProgress(96);
      console.log('Rendering complete, finalizing video file...');

      // Get video path BEFORE closing page
      const video = await page.video();
      if (!video) {
        throw new Error('Failed to capture video from Playwright');
      }

      // Close page to finalize video
      await page.close();
      if (typeof onProgress === 'function') onProgress(98);

      // Now get the video path after closing
      const recordedVideoPath = await video.path();
      console.log(`Recorded video path: ${recordedVideoPath}`);

      await context.close();
      await browser.close();

      // Move video to final location with proper name
      if (recordedVideoPath && fs.existsSync(recordedVideoPath)) {
        fs.renameSync(recordedVideoPath, videoPath);
        console.log(`Moved video to: ${videoPath}`);
      } else {
        throw new Error(`Recorded video file not found at ${recordedVideoPath}`);
      }

      if (typeof onProgress === 'function') onProgress(100);
      return videoPath;

    } catch (error) {
      // Cleanup on error
      if (context) {
        try {
          await context.close();
        } catch (e) {
          console.error('Error closing context:', e);
        }
      }
      if (browser) {
        try {
          await browser.close();
        } catch (e) {
          console.error('Error closing browser:', e);
        }
      }
      throw error;
    }
  }

  /**
   * Get video info
   */
  getVideoInfo(videoId) {
    const videoPath = path.join(this.outputDir, `video_${videoId}.webm`);

    if (!fs.existsSync(videoPath)) {
      return null;
    }

    const stats = fs.statSync(videoPath);

    return {
      id: videoId,
      path: videoPath,
      size: stats.size,
      created: stats.birthtime
    };
  }

  /**
   * List all generated videos
   */
  listVideos() {
    if (!fs.existsSync(this.outputDir)) {
      return [];
    }

    const files = fs.readdirSync(this.outputDir);
    const videos = files
      .filter(file => file.endsWith('.webm'))
      .map(file => {
        const stats = fs.statSync(path.join(this.outputDir, file));
        return {
          filename: file,
          size: stats.size,
          created: stats.birthtime
        };
      })
      .sort((a, b) => b.created - a.created);

    return videos;
  }

  /**
   * Delete video
   */
  deleteVideo(videoId) {
    const videoPath = path.join(this.outputDir, `video_${videoId}.webm`);

    if (fs.existsSync(videoPath)) {
      fs.unlinkSync(videoPath);
      return true;
    }

    return false;
  }
}
