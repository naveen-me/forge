import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import { ZipArchive } from 'archiver';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export class ImageGenerator {
  constructor(db) {
    this.db = db;
    this.outputDir = path.join(__dirname, '../storage/generated_images');

    // Ensure output directory exists
    if (!fs.existsSync(this.outputDir)) {
      fs.mkdirSync(this.outputDir, { recursive: true });
    }
  }

  /**
   * Generate images for a set of questions
   */
  async generateImages(questionIds, presetId, { onProgress, customName, prefix = 'Quiz', hideTimer = false, mode = 'separate' } = {}) {
    let htmlPath = null;
    const sessionId = Date.now();
    const folderName = customName ? `${customName.replace(/[^a-zA-Z0-9_-]/g, '_')}_${sessionId}` : `images_${sessionId}`;
    const sessionDir = path.join(this.outputDir, folderName);

    if (!fs.existsSync(sessionDir)) {
      fs.mkdirSync(sessionDir, { recursive: true });
    }

    try {
      if (typeof onProgress === 'function') onProgress(5);

      // 1. Load preset
      const preset = this.loadPreset(presetId);
      if (!preset) {
        throw new Error(`Preset ${presetId} not found`);
      }

      // Permanently hide timer for image generation
      if (preset.config.timer) {
        preset.config.timer.enabled = false;
      }

      // 2. Load questions
      const questions = this.loadQuestions(questionIds);
      if (questions.length === 0) {
        throw new Error('No questions found');
      }

      // 3. Create HTML render page
      const port = process.env.PORT || 3001;
      const baseUrl = process.env.PUBLIC_BASE_URL || `http://localhost:${port}`;
      const htmlContent = this.createRenderPage(preset, questions, baseUrl);
      htmlPath = path.join(this.outputDir, `render_${sessionId}.html`);
      fs.writeFileSync(htmlPath, htmlContent);

      // 4. Capture screenshots with Playwright
      await this.captureScreenshots(htmlPath, questions, preset, sessionDir, prefix, mode, onProgress);

      // 5. Cleanup
      if (htmlPath && fs.existsSync(htmlPath)) {
        fs.unlinkSync(htmlPath);
      }

      if (typeof onProgress === 'function') onProgress(100);

      return {
        folderName,
        path: sessionDir,
        questionCount: questions.length,
        imageCount: questions.length * 2
      };

    } catch (error) {
      if (htmlPath && fs.existsSync(htmlPath)) {
        try { fs.unlinkSync(htmlPath); } catch (e) {}
      }
      throw error;
    }
  }

  /**
   * Load preset from database
   */
  loadPreset(presetId) {
    const preset = this.db.presets.find(p => p.id === presetId);
    if (!preset) return null;

    const config = typeof preset.config === 'string'
      ? JSON.parse(preset.config)
      : preset.config;

    return { ...preset, config };
  }

  /**
   * Load questions from database
   */
  loadQuestions(questionIds) {
    const questions = [];
    const questionList = Array.isArray(questionIds) ? questionIds : [];

    for (const id of questionList) {
      const q = this.db.questions.find(question => question.id === id);
      if (q) {
        const options = Array.isArray(q.options)
          ? q.options
          : [q.option_1, q.option_2, q.option_3, q.option_4].filter(v => v !== undefined && v !== '');
        
        questions.push({
          id: q.id,
          question: q.question,
          options,
          correct_answer_index: q.correct_option ?? 0,
          explanation: q.explanation || null
        });
      }
    }
    return questions;
  }

  /**
   * Create HTML page for rendering
   */
  createRenderPage(preset, questions, baseUrl) {
    const renderingEnginePath = path.join(__dirname, '../shared/renderingEngine.js');
    const renderingEngineCode = fs.readFileSync(renderingEnginePath, 'utf-8');

    // Deep clone and convert URLs to absolute
    const convertedConfig = this.convertConfigURLs(preset.config, baseUrl);

    return `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <base href="${baseUrl}/">
  <style>
    body { margin: 0; padding: 0; background: #000; overflow: hidden; width: ${preset.config.canvas.width}px; height: ${preset.config.canvas.height}px; }
    canvas { display: block; width: ${preset.config.canvas.width}px; height: ${preset.config.canvas.height}px; }
  </style>
</head>
<body>
  <canvas id="canvas" width="${preset.config.canvas.width}" height="${preset.config.canvas.height}"></canvas>
  <script type="module">
    ${renderingEngineCode}
    window.presetConfig = ${JSON.stringify(convertedConfig)};
    window.questions = ${JSON.stringify(questions)};
    window.CanvasRenderer = CanvasRenderer;
    window.renderer = null;

    window.setupQuestion = async (index) => {
      const canvas = document.getElementById('canvas');
      if (window.renderer) window.renderer.destroy();
      
      const question = window.questions[index];
      window.renderer = new CanvasRenderer(canvas, window.presetConfig, question, false);
      
      // Ensure we don't use transition animations for static shots
      window.renderer.animationConfig.transition_duration = 0;
      
      await window.renderer.preloadAssets();
      
      // Wait for all images in cache to be loaded
      const images = Array.from(window.renderer.mediaCache.images.values());
      await Promise.all(images.map(img => {
        if (img.complete) return Promise.resolve();
        return new Promise(resolve => {
            img.onload = resolve;
            img.onerror = resolve;
        });
      }));

      return true;
    };

    window.renderTime = (time) => {
      if (window.renderer) {
        window.renderer.renderFrame(time);
        return true;
      }
      return false;
    };
  </script>
</body>
</html>`;
  }

  /**
   * Helper to convert URLs to absolute
   */
  convertConfigURLs(config, baseUrl) {
    const converted = JSON.parse(JSON.stringify(config));
    const toAbs = (url) => {
      if (!url || url.startsWith('http') || url.startsWith('data:')) return url;
      return `${baseUrl}${url.startsWith('/') ? '' : '/'}${url}`;
    };

    if (converted.canvas?.background_url) converted.canvas.background_url = toAbs(converted.canvas.background_url);
    if (converted.question?.background_url) converted.question.background_url = toAbs(converted.question.background_url);
    if (converted.options?.background_url) converted.options.background_url = toAbs(converted.options.background_url);
    if (converted.options?.correct_answer_background_url) converted.options.correct_answer_background_url = toAbs(converted.options.correct_answer_background_url);
    if (converted.timer?.background_url) converted.timer.background_url = toAbs(converted.timer.background_url);
    if (converted.explanation?.background_url) converted.explanation.background_url = toAbs(converted.explanation.background_url);

    // Font handling
    if (converted.question?.font_family && /\.(ttf|otf|woff|woff2)$/i.test(converted.question.font_family)) {
      converted.question.font_family_url = toAbs(`/storage/fonts/${converted.question.font_family}`);
    }
    if (converted.options?.font_family && /\.(ttf|otf|woff|woff2)$/i.test(converted.options.font_family)) {
      converted.options.font_family_url = toAbs(`/storage/fonts/${converted.options.font_family}`);
    }

    return converted;
  }

  async captureScreenshots(htmlPath, questions, preset, sessionDir, prefix, mode, onProgress) {
    const browser = await chromium.launch({ 
      headless: true,
      args: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-web-security',
        '--disable-features=IsolateOrigins,site-per-process',
        '--font-render-hinting=none'
      ]
    });
    const page = await browser.newPage({
      viewport: {
        width: preset.config.canvas.width,
        height: preset.config.canvas.height
      }
    });

    await page.goto(`file://${htmlPath}`);

    const anim = preset.config.animation || {};
    const timerDuration = preset.config.timer?.duration || 5;
    
    // Calculate static times
    const questionTime = anim.question_display_duration + (anim.options_display_duration || 1) + (timerDuration / 2);
    const answerTime = anim.question_display_duration + (anim.options_display_duration || 1) + timerDuration + (anim.answer_reveal_duration || 3) - 0.1;

    for (let i = 0; i < questions.length; i++) {
      // Setup question
      await page.evaluate((idx) => window.setupQuestion(idx), i);
      
      if (mode === 'separate') {
          // Capture Question Image
          await page.evaluate((t) => window.renderTime(t), questionTime);
          await page.waitForTimeout(200);
          await page.screenshot({
            path: path.join(sessionDir, `${prefix}_Q${i+1}_Question.png`),
            fullPage: false
          });
      }

      // Capture Answer/Merged Image
      await page.evaluate((t) => window.renderTime(t), answerTime);
      await page.waitForTimeout(200);
      const suffix = mode === 'merged' ? 'Full' : 'Answer';
      await page.screenshot({
        path: path.join(sessionDir, `${prefix}_Q${i+1}_${suffix}.png`),
        fullPage: false
      });

      if (typeof onProgress === 'function') {
        onProgress(Math.floor(10 + (i + 1) / questions.length * 85));
      }
    }

    await browser.close();
  }

  /**
   * List generated image folders
   */
  listImageSets() {
    if (!fs.existsSync(this.outputDir)) return [];

    const folders = fs.readdirSync(this.outputDir, { withFileTypes: true })
      .filter(dirent => dirent.isDirectory())
      .map(dirent => {
        const fullPath = path.join(this.outputDir, dirent.name);
        const stats = fs.statSync(fullPath);
        const images = fs.readdirSync(fullPath).filter(f => f.endsWith('.png'));
        
        return {
          id: dirent.name,
          name: dirent.name.split('_').slice(0, -1).join('_') || dirent.name,
          created: stats.birthtime,
          imageCount: images.length,
          folderName: dirent.name
        };
      })
      .sort((a, b) => b.created - a.created);

    return folders;
  }

  /**
   * Delete an image set
   */
  deleteImageSet(folderName) {
    const fullPath = path.join(this.outputDir, folderName);
    if (fs.existsSync(fullPath)) {
      fs.rmSync(fullPath, { recursive: true, force: true });
      return true;
    }
    return false;
  }

  /**
   * Create a zip of the image set
   */
  async createZip(folderName, res) {
    const fullPath = path.join(this.outputDir, folderName);
    if (!fs.existsSync(fullPath)) {
      throw new Error('Folder not found');
    }

    const archive = new ZipArchive({ zlib: { level: 9 } });
    archive.pipe(res);
    archive.directory(fullPath, false);
    await archive.finalize();
  }
}
