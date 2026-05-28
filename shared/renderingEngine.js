/**
 * Unified Canvas Rendering Engine
 * Single source of truth for both preview and video generation
 * Ensures 100% accuracy between preview and video output
 */

export class CanvasRenderer {
  constructor(canvas, presetConfig, questionData, isPreviewMode = true) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.config = presetConfig;
    this.questionData = questionData;
    this.isPreviewMode = isPreviewMode; // Flag to determine if in preview or video generation

    // Optional debug logging (VERY noisy if enabled)
    this.debug = !!presetConfig?.debug;
    this._loggedFontFallbacks = new Set();

    if (this.debug) {
      console.log('CanvasRenderer constructor called:', {
        hasCanvas: !!canvas,
        hasConfig: !!presetConfig,
        hasQuestionData: !!questionData,
        questionDataType: typeof questionData,
        questionDataKeys: questionData ? Object.keys(questionData) : [],
        questionField: questionData?.question,
        isPreviewMode: isPreviewMode
      });
    }

    // Animation state
    this.startTime = null;
    this.animationFrameId = null;
    this.isPlaying = false;
    this.isRenderingFrame = false;
    this.staticPreviewMode = false; // When true, show all elements at once without animation

    // Initialize animation config with defaults
    this.animationConfig = this.config.animation || {
      question_display_duration: 2,
      options_display_mode: 'all_at_once',
      option_reveal_delay: 0.5,
      answer_reveal_duration: 3,
      tts_enabled: false,
      tts_voice: 'default',
      tts_speed: 1.0,
      transition_effect: 'fade',
      transition_duration: 0.3
    };

    // Media cache
    this.mediaCache = {
      images: new Map(),
      videos: new Map(),
      fonts: new Map()
    };

    // Setup canvas dimensions
    this.canvas.width = this.config.canvas.width;
    this.canvas.height = this.config.canvas.height;

    // Bind methods
    this.renderFrame = this.renderFrame.bind(this);
  }

  /**
   * Calculate animation phases and their timings
   * Phase 1: Question display
   * Phase 2: Options reveal
   * Phase 3: Timer countdown
   * Phase 4: Answer reveal
   */
  getAnimationPhase(time) {
    const anim = this.animationConfig;
    const timerDuration = this.config.timer.duration || 5;
    const optionsCount = this.questionData.options?.length || 4;

    // Calculate phase durations
    let currentTime = 0;

    // Phase 1: Question display
    const questionEndTime = currentTime + anim.question_display_duration;
    if (time < questionEndTime) {
      return {
        phase: 'question',
        phaseTime: time - currentTime,
        progress: time / questionEndTime
      };
    }
    currentTime = questionEndTime;

    // Phase 2: Options reveal
    const optionsPhaseDuration = anim.options_display_duration !== undefined
      ? anim.options_display_duration
      : (anim.options_display_mode === 'one_by_one'
          ? (optionsCount * anim.option_reveal_delay)
          : anim.transition_duration);

    const optionsEndTime = currentTime + optionsPhaseDuration;

    if (time < optionsEndTime) {
      const phaseTime = time - currentTime;
      let visibleOptions = optionsCount;

      if (anim.options_display_mode === 'one_by_one') {
        const revealDelay = anim.options_display_duration !== undefined
          ? (optionsPhaseDuration / optionsCount)
          : anim.option_reveal_delay;
        visibleOptions = Math.min(optionsCount, Math.floor(phaseTime / (revealDelay || 0.001)) + 1);
      }

      return {
        phase: 'options',
        phaseTime: phaseTime,
        progress: phaseTime / (optionsPhaseDuration || 1),
        visibleOptions: visibleOptions
      };
    }
    currentTime = optionsEndTime;

    // Phase 3: Timer countdown
    const timerEndTime = currentTime + timerDuration;
    if (time < timerEndTime) {
      return {
        phase: 'timer',
        phaseTime: time - currentTime,
        progress: (time - currentTime) / timerDuration,
        timeRemaining: Math.ceil(timerDuration - (time - currentTime))
      };
    }
    currentTime = timerEndTime;

    // Phase 4: Answer reveal
    const answerEndTime = currentTime + anim.answer_reveal_duration;
    if (time < answerEndTime) {
      return {
        phase: 'answer_reveal',
        phaseTime: time - currentTime,
        progress: (time - currentTime) / anim.answer_reveal_duration
      };
    }

    // Animation complete
    return {
      phase: 'complete',
      phaseTime: time - currentTime,
      progress: 1.0
    };
  }

  /**
   * Get total duration for one question
   */
  getTotalDuration() {
    const anim = this.animationConfig;
    const timerDuration = this.config.timer.duration || 5;
    const optionsCount = this.questionData.options?.length || 4;

    let total = anim.question_display_duration;

    if (anim.options_display_duration !== undefined) {
      total += anim.options_display_duration;
    } else if (anim.options_display_mode === 'one_by_one') {
      total += optionsCount * anim.option_reveal_delay;
    } else {
      total += anim.transition_duration;
    }

    total += timerDuration;
    total += anim.answer_reveal_duration;

    return total;
  }

  /**
   * Preload all media assets (images, videos, fonts)
   */
  async preloadAssets() {
    if (this.debug) console.log('🎨 Starting asset preload...');
    const promises = [];

    // Preload background
    if (this.config.canvas.background_type === 'image' && this.config.canvas.background_url) {
      promises.push(this.loadImage(this.config.canvas.background_url, 'canvas_bg'));
    } else if (this.config.canvas.background_type === 'video' && this.config.canvas.background_url) {
      promises.push(this.loadVideo(this.config.canvas.background_url, 'canvas_bg'));
    }

    // Preload question background
    if (this.config.question.background_type === 'image' && this.config.question.background_url) {
      promises.push(this.loadImage(this.config.question.background_url, 'question_bg'));
    } else if (this.config.question.background_type === 'video' && this.config.question.background_url) {
      promises.push(this.loadVideo(this.config.question.background_url, 'question_bg'));
    }

    // Preload option backgrounds
    if (this.config.options.background_type === 'image' && this.config.options.background_url) {
      promises.push(this.loadImage(this.config.options.background_url, 'option_bg'));
    }

    // Preload correct answer background
    if (this.config.options.correct_answer_background_type === 'image' &&
        this.config.options.correct_answer_background_url) {
      promises.push(this.loadImage(this.config.options.correct_answer_background_url, 'correct_bg'));
    }

    // Preload timer background
    if (this.config.timer.background_type === 'image' && this.config.timer.background_url) {
      promises.push(this.loadImage(this.config.timer.background_url, 'timer_bg'));
    }

    // Preload explanation background and image
    if (this.config.explanation && this.config.explanation.enabled) {
      if (this.config.explanation.background_type === 'image' && this.config.explanation.background_image) {
        promises.push(this.loadImage(this.config.explanation.background_image, 'expl_bg'));
      }
      if (this.config.explanation.image_enabled && this.config.explanation.image_url) {
        promises.push(this.loadImage(this.config.explanation.image_url, 'expl_image'));
      }
    }

    // Preload overlay images
    if (Array.isArray(this.config.overlays)) {
      this.config.overlays.forEach((overlay, index) => {
        if (overlay?.image_url) {
          promises.push(this.loadImage(overlay.image_url, `overlay_${index}`));
        }
      });
    }

    // Preload fonts
    if (this.config.question.font_family) {
      if (this.debug) console.log(`📝 Loading question font: ${this.config.question.font_family}`);
      const fontUrl = this.config.question.font_family_url || null;
      promises.push(this.loadFont(this.config.question.font_family, fontUrl));
    }
    if (this.config.options.font_family) {
      if (this.debug) console.log(`📝 Loading options font: ${this.config.options.font_family}`);
      const fontUrl = this.config.options.font_family_url || null;
      promises.push(this.loadFont(this.config.options.font_family, fontUrl));
    }

    await Promise.all(promises);
    if (this.debug) console.log('✅ Asset preload complete');
  }

  /**
   * Load an image and cache it
   */
  loadImage(url, key) {
    return new Promise((resolve, reject) => {
      if (this.mediaCache.images.has(key)) {
        resolve(this.mediaCache.images.get(key));
        return;
      }

      const img = new Image();
      img.crossOrigin = 'anonymous';
      img.onload = () => {
        this.mediaCache.images.set(key, img);
        resolve(img);
      };
      img.onerror = () => {
        console.warn(`Failed to load image: ${url}`);
        resolve(null);
      };
      img.src = url;
    });
  }

  /**
   * Load a video and cache it
   */
  loadVideo(url, key) {
    return new Promise((resolve, reject) => {
      if (this.mediaCache.videos.has(key)) {
        resolve(this.mediaCache.videos.get(key));
        return;
      }

      const video = document.createElement('video');
      video.crossOrigin = 'anonymous';
      video.muted = true;
      video.loop = true;
      video.playsInline = true;

      video.onloadeddata = () => {
        this.mediaCache.videos.set(key, video);
        video.play().catch(e => console.warn('Video autoplay failed:', e));
        resolve(video);
      };
      video.onerror = () => {
        console.warn(`Failed to load video: ${url}`);
        resolve(null);
      };
      video.src = url;
    });
  }

  /**
   * Load a font
   */
  async loadFont(fontFamily, fontUrl = null) {
    if (!fontFamily || this.mediaCache.fonts.has(fontFamily)) {
      return;
    }

    // If this looks like a system font name (no extension and no explicit URL), don't attempt to download.
    // This keeps preview/video generation consistent and avoids stalls when /storage/fonts is empty.
    const looksLikeFile = /\.(ttf|otf|woff|woff2)$/i.test(fontFamily);
    if (!fontUrl && !looksLikeFile) {
      this.mediaCache.fonts.set(fontFamily, true);
      return;
    }

    try {
      // Use provided font URL or construct from fontFamily
      let url = fontUrl || `/storage/fonts/${fontFamily}`;

      // Extract font family name without extension for FontFace
      const fontFamilyName = fontFamily.replace(/\.(ttf|otf|woff|woff2)$/i, '');

      const loadWithTimeout = async (promise, timeoutMs = 3000) => {
        let timeoutId;
        try {
          return await Promise.race([
            promise,
            new Promise((_, reject) => {
              timeoutId = setTimeout(() => reject(new Error('Font load timeout')), timeoutMs);
            })
          ]);
        } finally {
          if (timeoutId) clearTimeout(timeoutId);
        }
      };

      // Handle file:// URLs properly for local file access
      if (url.startsWith('file://')) {
        const fontFace = new FontFace(fontFamilyName, `url("${url}") format('truetype')`);
        await loadWithTimeout(fontFace.load(), 3000);
        document.fonts.add(fontFace);
      } else {
        const fontFace = new FontFace(fontFamilyName, `url("${url}")`);
        await loadWithTimeout(fontFace.load(), 3000);
        document.fonts.add(fontFace);
      }

      this.mediaCache.fonts.set(fontFamily, true);
      if (this.debug) console.log(`✓ Font loaded successfully: ${fontFamilyName} from ${url}`);
    } catch (e) {
      console.warn(`⚠ Font ${fontFamily} not loaded, using fallback. Error: ${e.message}`);

      // Mark this font as failed to load to trigger Arial fallback
      this.mediaCache.fonts.set(fontFamily, false);
    }
  }

  /**
   * Get safe font family string with proper fallback
   * Returns font family name without extension, or Arial if font failed to load
   */
  getSafeFontFamily(fontFamily) {
    if (!fontFamily) {
      return 'Arial';
    }

    // Remove file extension to get the actual font family name
    const cleanName = fontFamily.replace(/\.(ttf|otf|woff|woff2)$/i, '');

    // Check if font was successfully loaded
    const fontLoaded = this.mediaCache.fonts.get(fontFamily);

    // If font explicitly failed to load (set to false), use Arial
    if (fontLoaded === false) {
      if (this.debug && !this._loggedFontFallbacks.has(fontFamily)) {
        this._loggedFontFallbacks.add(fontFamily);
        console.log(`Using Arial fallback for failed font: ${fontFamily}`);
      }
      return 'Arial';
    }

    // Otherwise use the clean font name with generic fallback
    return `"${cleanName}", sans-serif`;
  }

  /**
   * Clear the entire canvas and reset state - PROFESSIONAL GRADE
   * This method ensures ZERO trailing artifacts by:
   * 1. Resetting all transformations
   * 2. Clearing with both clearRect and fillRect
   * 3. Resetting all canvas state properties
   */
  clearCanvas() {
    // Save current state
    this.ctx.save();

    // Reset ALL transforms to identity matrix (critical for clean clears)
    this.ctx.setTransform(1, 0, 0, 1, 0, 0);

    // ULTRA-AGGRESSIVE CLEARING STRATEGY to eliminate ALL trailing borders/artifacts
    // This is critical for design changes where borders change size/color/position

    // Reset ALL context properties BEFORE clearing to ensure clean state
    this.ctx.globalAlpha = 1.0;
    this.ctx.globalCompositeOperation = 'source-over';
    this.ctx.shadowBlur = 0;
    this.ctx.shadowOffsetX = 0;
    this.ctx.shadowOffsetY = 0;
    this.ctx.filter = 'none';
    this.ctx.setLineDash([]);
    this.ctx.lineWidth = 1;

    // Step 1: Clear with clearRect (removes all pixels completely)
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    // Step 2: Fill with WHITE first (ensures complete pixel reset)
    this.ctx.fillStyle = '#FFFFFF';
    this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

    // Step 3: Fill with BLACK second (double-coverage eliminates ANY artifacts)
    this.ctx.fillStyle = '#000000';
    this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

    // Step 4: Finally fill with actual background color
    // Triple-fill ensures ZERO trailing pixels even with anti-aliasing
    this.ctx.fillStyle = this.config.canvas.background_color || '#000000';
    this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

    // Restore state
    this.ctx.restore();
  }

  /**
   * Draw background (color, image, or video)
   */
  drawBackground(time) {
    const bg = this.config.canvas;

    if (bg.background_type === 'color') {
      this.ctx.fillStyle = bg.background_color || '#000000';
      this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
    } else if (bg.background_type === 'image') {
      const img = this.mediaCache.images.get('canvas_bg');
      if (img) {
        this.drawMediaWithFit(img, 0, 0, this.canvas.width, this.canvas.height, bg);
      } else {
        // Fallback to color
        this.ctx.fillStyle = bg.background_color || '#000000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
      }
    } else if (bg.background_type === 'video') {
      const video = this.mediaCache.videos.get('canvas_bg');
      if (video && video.readyState >= 2) {
        this.drawMediaWithFit(video, 0, 0, this.canvas.width, this.canvas.height, bg);
      } else {
        // Fallback to color
        this.ctx.fillStyle = bg.background_color || '#000000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
      }
    }
  }

  /**
   * Draw media (image or video) with fit options
   */
  drawMediaWithFit(media, x, y, width, height, config) {
    const mediaWidth = media.videoWidth || media.width;
    const mediaHeight = media.videoHeight || media.height;

    if (!mediaWidth || !mediaHeight) return;

    const fit = config.background_fit || 'cover';
    const posX = config.background_pos_x || 0.5;
    const posY = config.background_pos_y || 0.5;

    let drawX, drawY, drawWidth, drawHeight;

    if (fit === 'cover') {
      const scale = Math.max(width / mediaWidth, height / mediaHeight);
      drawWidth = mediaWidth * scale;
      drawHeight = mediaHeight * scale;
      drawX = x + (width - drawWidth) * posX;
      drawY = y + (height - drawHeight) * posY;
      this.ctx.drawImage(media, drawX, drawY, drawWidth, drawHeight);
    } else if (fit === 'contain') {
      const scale = Math.min(width / mediaWidth, height / mediaHeight);
      drawWidth = mediaWidth * scale;
      drawHeight = mediaHeight * scale;
      drawX = x + (width - drawWidth) * posX;
      drawY = y + (height - drawHeight) * posY;
      this.ctx.drawImage(media, drawX, drawY, drawWidth, drawHeight);
    } else if (fit === 'fill') {
      drawX = x;
      drawY = y;
      drawWidth = width;
      drawHeight = height;
      this.ctx.drawImage(media, drawX, drawY, drawWidth, drawHeight);
    } else if (fit === 'repeat' || fit === 'repeat-x' || fit === 'repeat-y') {
      // Create pattern for repeating backgrounds
      const repeatType = fit === 'repeat' ? 'repeat' :
                         fit === 'repeat-x' ? 'repeat-x' : 'repeat-y';

      // Save current state
      this.ctx.save();

      // Create pattern
      const pattern = this.ctx.createPattern(media, repeatType);

      if (pattern) {
        this.ctx.fillStyle = pattern;
        this.ctx.fillRect(x, y, width, height);
      }

      // Restore state
      this.ctx.restore();
    } else {
      // 'none' - original size
      drawWidth = mediaWidth;
      drawHeight = mediaHeight;
      drawX = x + (width - drawWidth) * posX;
      drawY = y + (height - drawHeight) * posY;
      this.ctx.drawImage(media, drawX, drawY, drawWidth, drawHeight);
    }
  }

  /**
   * Draw question text
   */
  drawQuestion(time, questionIndex = 0) {
    const q = this.config.question;
    let text = this.questionData.question || '';

    // Debug logging (extremely noisy if enabled)
    if (this.debug) {
      console.log('drawQuestion called:', {
        hasQuestionData: !!this.questionData,
        questionDataKeys: this.questionData ? Object.keys(this.questionData) : [],
        questionText: text,
        textLength: text?.length
      });
    }

    if (!text) {
      console.warn('⚠ No question text to render');
      console.warn('Question data:', JSON.stringify(this.questionData));
      return;
    }

    const x = q.position.x;
    const y = q.position.y;
    const width = q.width || q.max_width || (this.canvas.width - x - 100);
    const height = q.height || 0;

    // Add question numbering if enabled
    const hasNumbering = q.numbering === 'auto' || q.numbering === 'manual';
    let numberText = '';
    let numberWidth = 0;

    if (q.numbering === 'auto') {
      numberText = `${questionIndex + 1}. `;
    } else if (q.numbering === 'manual') {
      const startNum = q.start_number || 1;
      numberText = `${startNum + questionIndex}. `;
    }

    // Calculate number width for gap
    if (hasNumbering) {
      const fontFamily = this.getSafeFontFamily(q.font_family);
      this.ctx.font = `${q.font_size}px ${fontFamily}, sans-serif`;

      // Measure just the number without the period and space
      const numberOnly = numberText.replace('. ', '');
      const baseNumberWidth = this.ctx.measureText(numberOnly).width;

      // Add gap between number and question (percentage of width)
      const gapPct = q.number_gap !== undefined ? q.number_gap : 2;
      const gap = (gapPct / 100) * width;

      // Total width = number + period + gap
      const periodWidth = this.ctx.measureText('. ').width;
      numberWidth = baseNumberWidth + periodWidth + gap;
    }

    // Handle margin - can be number (old) or object (new) - stored as percentage
    let marginTopPct, marginRightPct, marginBottomPct, marginLeftPct;
    if (typeof q.margin === 'number') {
      marginTopPct = marginRightPct = marginBottomPct = marginLeftPct = q.margin || 0;
    } else if (q.margin && typeof q.margin === 'object') {
      marginTopPct = q.margin.top || 0;
      marginRightPct = q.margin.right || 0;
      marginBottomPct = q.margin.bottom || 0;
      marginLeftPct = q.margin.left || 0;
    } else {
      marginTopPct = marginRightPct = marginBottomPct = marginLeftPct = 0;
    }

    // Convert percentage to pixels based on width/height
    const marginTop = (marginTopPct / 100) * height || (marginTopPct / 100) * (q.font_size * 1.2 * 3);
    const marginBottom = (marginBottomPct / 100) * height || (marginBottomPct / 100) * (q.font_size * 1.2 * 3);
    const marginLeft = (marginLeftPct / 100) * width;
    const marginRight = (marginRightPct / 100) * width;

    // Border radius as percentage of smallest dimension
    const borderRadiusPct = q.border_radius || 0;
    const smallestDim = Math.min(width, height || (q.font_size * 1.2 * 3));
    const borderRadius = (borderRadiusPct / 100) * smallestDim;

    // Draw background if enabled
    if (q.background_type && q.background_type !== 'none') {
      this.ctx.save();

      // Create rounded rectangle path for background
      const bgHeight = height || (q.font_size * 1.2 * 3);

      if (borderRadius > 0) {
        this.roundRect(x, y, width, bgHeight, borderRadius);
        this.ctx.clip();
      }

      if (q.background_type === 'color') {
        this.ctx.fillStyle = q.background_color || '#000000';
        this.ctx.fillRect(x, y, width, bgHeight);
      } else if (q.background_type === 'image') {
        const img = this.mediaCache.images.get('question_bg');
        if (img) {
          this.drawMediaWithFit(img, x, y, width, bgHeight, q);
        } else {
          this.ctx.fillStyle = q.background_color || '#000000';
          this.ctx.fillRect(x, y, width, bgHeight);
        }
      } else if (q.background_type === 'video') {
        const video = this.mediaCache.videos.get('question_bg');
        if (video && video.readyState >= 2) {
          this.drawMediaWithFit(video, x, y, width, bgHeight, q);
        } else {
          this.ctx.fillStyle = q.background_color || '#000000';
          this.ctx.fillRect(x, y, width, bgHeight);
        }
      }

      this.ctx.restore();
    }

    // Draw border in preview mode only (if show_border_preview is enabled)
    if (q.show_border_preview && this.isPreviewMode) {
      this.ctx.save();
      this.ctx.strokeStyle = '#4CAF50';
      this.ctx.lineWidth = 2;
      this.ctx.setLineDash([5, 5]);

      if (height > 0) {
        this.ctx.strokeRect(x, y, width, height);
      } else {
        this.ctx.strokeRect(x, y, width, q.font_size * 1.2 * 3);
      }

      this.ctx.setLineDash([]);
      this.ctx.restore();
    }

    // Setup font with safe fallback
    const fontFamily = this.getSafeFontFamily(q.font_family);
    this.ctx.font = `${q.font_size}px ${fontFamily}`;
    this.ctx.fillStyle = q.font_color || '#FFFFFF';
    this.ctx.textAlign = q.text_align || 'left';
    this.ctx.textBaseline = 'top';

    // Calculate text starting position based on alignment and margin
    const textWidth = width - marginLeft - marginRight - numberWidth;
    let textX = x + marginLeft;

    // Draw number if enabled
    if (hasNumbering) {
      this.ctx.fillText(numberText, x + marginLeft, y + marginTop);
      textX = x + marginLeft + numberWidth;
    }

    if (q.text_align === 'center') {
      textX = x + marginLeft + numberWidth + textWidth / 2;
    } else if (q.text_align === 'right') {
      textX = x + width - marginRight;
    }

    const textY = y + marginTop;

    // Draw text with word wrap
    const lineHeight = q.font_size * 1.2;

    if (height > 0) {
      // Fixed height - clip text if it exceeds
      this.ctx.save();
      this.ctx.beginPath();
      this.ctx.rect(x + marginLeft, y + marginTop, width - marginLeft - marginRight, height - marginTop - marginBottom);
      this.ctx.clip();
      this.drawWrappedText(text, textX, textY, textWidth, lineHeight);
      this.ctx.restore();
    } else {
      // Auto height
      this.drawWrappedText(text, textX, textY, textWidth, lineHeight);
    }
  }

  /**
   * Draw wrapped text (handles multi-line) - Optimized version
   */
  drawWrappedText(text, x, y, maxWidth, lineHeight, maxLines) {
    if (text === null || text === undefined) {
      return;
    }

    const safeText = typeof text === 'string' ? text : String(text);
    const words = safeText.split(' ');
    let line = '';
    let currentY = y;
    let linesUsed = 0;
    const lineLimit = typeof maxLines === 'number' ? maxLines : null;

    const drawLine = (content) => {
      if (!content) return false;
      if (lineLimit !== null && linesUsed >= lineLimit) {
        return true;
      }
      this.ctx.fillText(content, x, currentY);
      currentY += lineHeight;
      linesUsed += 1;
      return lineLimit !== null && linesUsed >= lineLimit;
    };

    for (let i = 0; i < words.length; i++) {
      const testLine = line + words[i] + ' ';

      if (i > 0 && i % 3 === 0) {
        const metrics = this.ctx.measureText(testLine);

        if (metrics.width > maxWidth) {
          if (drawLine(line)) {
            return;
          }
          line = words[i] + ' ';
        } else {
          line = testLine;
        }
      } else {
        line = testLine;
      }
    }

    if (line.length > 0) {
      drawLine(line);
    }
  }

  /**
   * Draw all options based on layout
   */
  drawOptions(time, animPhase) {
    const opts = this.config.options;
    const options = this.questionData.options || [];
    const correctIndex = this.questionData.correct_answer_index || 0;

    // Don't show options until options phase or later
    if (animPhase.phase === 'question') {
      return;
    }

    // Show correct answer only during answer_reveal phase
    const showCorrect = animPhase.phase === 'answer_reveal' || animPhase.phase === 'complete';

    // Determine how many options to show
    const visibleCount = animPhase.visibleOptions !== undefined
      ? animPhase.visibleOptions
      : options.length;

    // Parse layout format (e.g., "2x2", "1x4", "1x2x1")
    const positions = this.calculateOptionPositions(opts.layout_format, options.length);

    options.forEach((option, index) => {
      // Only show visible options
      if (index >= visibleCount) return;

      if (positions[index]) {
        const isCorrect = index === correctIndex;

        // Calculate fade-in alpha for smooth transitions
        let alpha = 1.0;
        if (animPhase.phase === 'options' && this.animationConfig.transition_effect === 'fade') {
          if (this.animationConfig.options_display_mode === 'one_by_one') {
            const optionStartTime = index * this.animationConfig.option_reveal_delay;
            const optionProgress = Math.min(1, (animPhase.phaseTime - optionStartTime) / this.animationConfig.transition_duration);
            alpha = Math.max(0, optionProgress);
          } else {
            alpha = animPhase.progress;
          }
        }

        this.drawOption(option, positions[index], index, isCorrect && showCorrect, alpha);
      }
    });
  }

  /**
   * Calculate option positions based on layout format
   */
  calculateOptionPositions(layoutFormat, count) {
    const opts = this.config.options;
    const containerX = opts.container_position.x;
    const containerY = opts.container_position.y;
    const width = opts.option_width;
    const height = opts.option_height;
    const hSpacing = opts.spacing.horizontal;
    const vSpacing = opts.spacing.vertical;

    const positions = [];

    if (layoutFormat === '2x2') {
      // 2 columns, 2 rows
      positions.push({ x: containerX, y: containerY });
      positions.push({ x: containerX + width + hSpacing, y: containerY });
      positions.push({ x: containerX, y: containerY + height + vSpacing });
      positions.push({ x: containerX + width + hSpacing, y: containerY + height + vSpacing });
    } else if (layoutFormat === '1x4') {
      // 1 column, 4 rows
      for (let i = 0; i < count; i++) {
        positions.push({ x: containerX, y: containerY + i * (height + vSpacing) });
      }
    } else if (layoutFormat === '4x1') {
      // 4 columns, 1 row
      for (let i = 0; i < count; i++) {
        positions.push({ x: containerX + i * (width + hSpacing), y: containerY });
      }
    } else if (layoutFormat === '1x2x1') {
      // Custom layout: 1 top, 2 middle, 1 bottom
      positions.push({ x: containerX + (width + hSpacing) / 2, y: containerY });
      positions.push({ x: containerX, y: containerY + height + vSpacing });
      positions.push({ x: containerX + width + hSpacing, y: containerY + height + vSpacing });
      positions.push({ x: containerX + (width + hSpacing) / 2, y: containerY + 2 * (height + vSpacing) });
    }

    return positions;
  }

  /**
   * Draw a single option
   */
  drawOption(optionText, position, index, isCorrect, alpha = 1.0) {
    const opts = this.config.options;
    const x = position.x;
    const y = position.y;
    const width = opts.option_width;
    const height = opts.option_height;

    // Handle margin - percentage-based
    let marginTopPct, marginRightPct, marginBottomPct, marginLeftPct;
    if (typeof opts.margin === 'number') {
      marginTopPct = marginRightPct = marginBottomPct = marginLeftPct = opts.margin || 0;
    } else if (opts.margin && typeof opts.margin === 'object') {
      marginTopPct = opts.margin.top || 0;
      marginRightPct = opts.margin.right || 0;
      marginBottomPct = opts.margin.bottom || 0;
      marginLeftPct = opts.margin.left || 0;
    } else {
      marginTopPct = marginRightPct = marginBottomPct = marginLeftPct = 0;
    }

    // Convert percentage to pixels
    const marginTop = (marginTopPct / 100) * height;
    const marginBottom = (marginBottomPct / 100) * height;
    const marginLeft = (marginLeftPct / 100) * width;
    const marginRight = (marginRightPct / 100) * width;

    // Border radius as percentage of smallest dimension
    const borderRadiusPct = opts.border_radius || 0;
    const smallestDim = Math.min(width, height);
    const borderRadius = (borderRadiusPct / 100) * smallestDim;

    // Save context
    this.ctx.save();

    // Apply alpha for fade-in effect
    this.ctx.globalAlpha = alpha;

    // Create rounded rectangle path
    this.roundRect(x, y, width, height, borderRadius);
    this.ctx.clip();

    // Draw background
    if (isCorrect) {
      this.drawOptionBackground(x, y, width, height, {
        background_type: opts.correct_answer_background_type,
        background_color: opts.correct_answer_background_color,
        background_url: opts.correct_answer_background_url,
        background_fit: opts.correct_answer_background_fit,
        background_pos_x: opts.correct_answer_background_pos_x,
        background_pos_y: opts.correct_answer_background_pos_y
      }, 'correct_bg');
    } else {
      this.drawOptionBackground(x, y, width, height, {
        background_type: opts.background_type,
        background_color: opts.background_color,
        background_url: opts.background_url,
        background_fit: opts.background_fit,
        background_pos_x: opts.background_pos_x,
        background_pos_y: opts.background_pos_y
      }, 'option_bg');
    }

    // Restore context
    this.ctx.restore();

    // Draw border preview in preview mode only
    if (opts.show_border_preview && this.isPreviewMode) {
      this.ctx.save();
      this.ctx.strokeStyle = '#FF9800'; // Orange for options
      this.ctx.lineWidth = 2;
      this.ctx.setLineDash([5, 5]);
      this.roundRect(x, y, width, height, borderRadius);
      this.ctx.stroke();
      this.ctx.setLineDash([]);
      this.ctx.restore();
    }

    // Draw border
    if (opts.border_width > 0) {
      this.ctx.strokeStyle = opts.border_color || '#BDBDBD';
      this.ctx.lineWidth = opts.border_width;
      this.roundRect(x, y, width, height, borderRadius);
      this.ctx.stroke();
    }

    // Save context for text drawing with alpha
    this.ctx.save();
    this.ctx.globalAlpha = alpha;

    // Draw text with margin applied and safe font fallback
    const fontFamily = this.getSafeFontFamily(opts.font_family);
    this.ctx.font = `${opts.font_size}px ${fontFamily}`;
    this.ctx.fillStyle = isCorrect ? (opts.correct_answer_font_color || opts.font_color) : opts.font_color;
    this.ctx.textBaseline = 'middle';

    // Get label style and spacing settings
    const labelStyle = opts.label_style || 'none';
    const labelSpacing = parseInt(opts.label_spacing || 10);

    // Calculate text position based on label style
    const textY = y + height / 2;

    if (labelStyle === 'none') {
      // No label - center the option text
      this.ctx.textAlign = 'center';
      this.ctx.fillText(optionText, x + width / 2, textY);
    } else {
      // Has label (alphabetic or numeric)
      let label = '';
      if (labelStyle === 'numeric') {
        label = String(index + 1) + '.';
      } else if (labelStyle === 'alpha' || labelStyle === 'alphabetic') {
        label = String.fromCharCode(65 + index) + '.'; // A., B., C., D.
      } else {
        // Fallback to none if unknown style
        this.ctx.textAlign = 'center';
        this.ctx.fillText(optionText, x + width / 2, textY);
        this.ctx.restore();
        return;
      }

      // Measure label width
      const labelWidth = this.ctx.measureText(label).width;

      // Draw label and text from left with margin (use default margin if 0)
      const effectiveMarginLeft = marginLeft > 0 ? marginLeft : 10;
      const startX = x + effectiveMarginLeft;

      // Draw label
      this.ctx.textAlign = 'left';
      this.ctx.fillText(label, startX, textY);

      // Draw option text after label with spacing
      const optionTextX = startX + labelWidth + labelSpacing;
      const effectiveMarginRight = marginRight > 0 ? marginRight : 10;
      const availableWidth = width - effectiveMarginLeft - effectiveMarginRight - labelWidth - labelSpacing;

      // Truncate text if too long (optimized approach)
      let truncatedText = optionText;
      if (availableWidth > 0) {
        const textWidth = this.ctx.measureText(truncatedText).width;

        if (textWidth > availableWidth) {
          // Estimate characters to remove (faster than looping)
          const avgCharWidth = textWidth / truncatedText.length;
          const excessWidth = textWidth - availableWidth;
          const charsToRemove = Math.ceil(excessWidth / avgCharWidth) + 5; // +5 for ellipsis and safety

          if (charsToRemove < truncatedText.length) {
            truncatedText = truncatedText.slice(0, -charsToRemove) + '...';

            // Only verify once (not in a loop)
            if (this.ctx.measureText(truncatedText).width > availableWidth && truncatedText.length > 10) {
              // If still too long, use a more aggressive cut
              truncatedText = truncatedText.slice(0, Math.floor(truncatedText.length * 0.7)) + '...';
            }
          } else {
            truncatedText = '...';
          }
        }
      }

      this.ctx.fillText(truncatedText, optionTextX, textY);
    }

    // Restore context
    this.ctx.restore();
  }

  /**
   * Draw option background
   */
  drawOptionBackground(x, y, width, height, config, cacheKey) {
    if (config.background_type === 'color') {
      this.ctx.fillStyle = config.background_color || '#EEEEEE';
      this.ctx.fillRect(x, y, width, height);
    } else if (config.background_type === 'image') {
      const img = this.mediaCache.images.get(cacheKey);
      if (img) {
        this.drawMediaWithFit(img, x, y, width, height, config);
      } else {
        this.ctx.fillStyle = config.background_color || '#EEEEEE';
        this.ctx.fillRect(x, y, width, height);
      }
    }
  }

  /**
   * Draw timer
   */
  drawTimer(time, animPhase) {
    const timer = this.config.timer;
    const duration = timer.duration || 5;

    // Show timer during timer phase, or always in static preview mode
    if (animPhase.phase !== 'timer' && !this.staticPreviewMode) {
      return;
    }

    // Use timeRemaining from animation phase, or full duration in static mode
    const remaining = this.staticPreviewMode ? duration : (animPhase.timeRemaining || duration);

    const x = timer.position.x;
    const y = timer.position.y;
    const width = timer.width;
    const height = timer.height;

    // Save context
    this.ctx.save();

    // Create rounded rectangle path
    this.roundRect(x, y, width, height, timer.border_radius || 0);
    this.ctx.clip();

    // Draw background
    if (timer.background_type === 'color') {
      this.ctx.fillStyle = timer.background_color || '#FFFFFF';
      this.ctx.fillRect(x, y, width, height);
    } else if (timer.background_type === 'image') {
      const img = this.mediaCache.images.get('timer_bg');
      if (img) {
        this.drawMediaWithFit(img, x, y, width, height, timer);
      } else {
        this.ctx.fillStyle = timer.background_color || '#FFFFFF';
        this.ctx.fillRect(x, y, width, height);
      }
    }

    // Restore context
    this.ctx.restore();

    // Draw border
    if (timer.border_width > 0) {
      this.ctx.strokeStyle = timer.border_color || '#FFFFFF';
      this.ctx.lineWidth = timer.border_width;
      this.roundRect(x, y, width, height, timer.border_radius || 0);
      this.ctx.stroke();
    }

    // Draw countdown number
    const timerFontFamily = this.getSafeFontFamily(timer.font_family || 'Arial');
    this.ctx.font = `bold ${timer.font_size}px ${timerFontFamily}`;
    this.ctx.fillStyle = timer.font_color || '#000000';
    this.ctx.textAlign = 'center';
    this.ctx.textBaseline = 'middle';
    this.ctx.fillText(String(remaining), x + width / 2, y + height / 2);
  }

  /**
   * Draw explanation screen
   */
  drawExplanation(time, animPhase) {
    const expl = this.config.explanation;

    // Only show explanation if enabled and in static preview mode
    if (!expl.enabled || !this.staticPreviewMode) {
      return;
    }

    const canvasWidth = this.canvas.width;
    const canvasHeight = this.canvas.height;

    // Draw background
    if (expl.background_type === 'color') {
      this.ctx.fillStyle = expl.background_color || '#ffffff';
      this.ctx.fillRect(0, 0, canvasWidth, canvasHeight);
    } else if (expl.background_type === 'image' && expl.background_image) {
      const img = this.mediaCache.images.get('expl_bg');
      if (img) {
        this.drawMediaWithFit(img, 0, 0, canvasWidth, canvasHeight, expl);
      }
    }

    // Draw explanation text if available
    const explanationText = this.questionData.explanation || 'Explanation text here';

    if (explanationText) {
      const explFontFamily = this.getSafeFontFamily(expl.font_family || 'Arial');
      this.ctx.font = `${expl.font_size || 36}px ${explFontFamily}`;
      this.ctx.fillStyle = expl.font_color || '#000000';
      this.ctx.textAlign = expl.text_align || 'center';
      this.ctx.textBaseline = 'middle';

      const boxWidth = Math.min(expl.width || canvasWidth - 100, canvasWidth - 100);
      const maxHeight = expl.height && expl.height > 0 ? Math.min(expl.height, canvasHeight - 100) : null;
      const boxHeight = maxHeight ?? Math.min(canvasHeight - 100, (expl.font_size || 36) * 4);
      const boxX = (canvasWidth - boxWidth) / 2;
      const boxY = (canvasHeight - boxHeight) / 2;

      const textX = expl.text_align === 'left' ? boxX + 20 :
                    expl.text_align === 'right' ? boxX + boxWidth - 20 :
                    boxX + boxWidth / 2;
      const textY = boxY + boxHeight / 2;

      const maxWidth = boxWidth - 40;
      const lineHeight = (expl.font_size || 36) * 1.5;
      const maxLines = maxHeight ? Math.max(Math.floor(boxHeight / lineHeight), 1) : null;

      if (maxLines) {
        this.drawWrappedText(explanationText, textX, textY, maxWidth, lineHeight, maxLines);
      } else {
        this.drawWrappedText(explanationText, textX, textY, maxWidth, lineHeight);
      }
    }

    // Draw explanation image if enabled
    if (expl.image_enabled && expl.image_url) {
      const img = this.mediaCache.images.get('expl_image');
      if (img) {
        const imgWidth = expl.image_width || 400;
        const imgHeight = expl.image_height || 300;

        // Position image based on settings
        let imgX = (canvasWidth - imgWidth) / 2;
        let imgY;

        if (expl.image_position === 'top') {
          imgY = 50;
        } else if (expl.image_position === 'bottom') {
          imgY = canvasHeight - imgHeight - 50;
        } else {
          imgY = (canvasHeight - imgHeight) / 2;
        }

        // Draw image with fit mode
        this.ctx.save();
        this.ctx.beginPath();
        this.ctx.rect(imgX, imgY, imgWidth, imgHeight);
        this.ctx.clip();

        this.drawMediaWithFit(img, imgX, imgY, imgWidth, imgHeight, {
          background_fit: expl.image_fit || 'contain'
        });

        this.ctx.restore();
      }
    }
  }

  drawOverlays(animPhase) {
    const overlays = Array.isArray(this.config.overlays) ? this.config.overlays : [];
    if (!overlays.length) {
      return;
    }

    if (this.staticPreviewMode) {
      this.drawOverlayImages(overlays);
      return;
    }

    if (animPhase.phase === 'intro' || animPhase.phase === 'outro') {
      return;
    }

    this.drawOverlayImages(overlays);
  }

  drawOverlayImages(overlays) {
    const canvasWidth = this.canvas.width;
    const canvasHeight = this.canvas.height;

    overlays.forEach((overlay, index) => {
      if (!overlay?.image_url) {
        return;
      }

      const img = this.mediaCache.images.get(`overlay_${index}`);
      if (!img) {
        return;
      }

      const width = overlay.width || img.width || 0;
      const height = overlay.height || img.height || 0;
      if (!width || !height) {
        return;
      }

      const x = overlay.position?.x ?? 0;
      const y = overlay.position?.y ?? 0;
      const fit = overlay.fit || 'contain';
      const opacity = typeof overlay.opacity === 'number' ? overlay.opacity : 1;

      this.ctx.save();
      this.ctx.globalAlpha = Math.min(Math.max(opacity, 0), 1);

      const config = {
        background_fit: fit,
        background_pos_x: overlay.position?.pos_x ?? 0.5,
        background_pos_y: overlay.position?.pos_y ?? 0.5
      };

      this.drawMediaWithFit(img, x, y, width, height, config);
      this.ctx.restore();
    });
  }

  /**
   * Draw rounded rectangle (helper)
   */
  roundRect(x, y, width, height, radius) {
    if (radius === 0) {
      this.ctx.rect(x, y, width, height);
      return;
    }

    this.ctx.beginPath();
    this.ctx.moveTo(x + radius, y);
    this.ctx.lineTo(x + width - radius, y);
    this.ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
    this.ctx.lineTo(x + width, y + height - radius);
    this.ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
    this.ctx.lineTo(x + radius, y + height);
    this.ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
    this.ctx.lineTo(x, y + radius);
    this.ctx.quadraticCurveTo(x, y, x + radius, y);
    this.ctx.closePath();
  }

  /**
   * Render a single frame at given time - PROFESSIONAL GRADE
   * Ensures smooth, clean rendering with zero artifacts
   */
  renderFrame(time) {
    // Prevent concurrent renders (critical for smooth playback)
    if (this.isRenderingFrame) {
      return;
    }

    this.isRenderingFrame = true;

    try {
      // STEP 1: Complete canvas clear (removes ALL previous content)
      this.clearCanvas();

      // STEP 2: Get current animation phase
      let animPhase;

      if (this.staticPreviewMode) {
        // Static preview: show everything at once (complete state)
        animPhase = {
          phase: 'complete',
          visibleOptions: (this.questionData.options || []).length,
          showTimer: true,
          showQuestion: true,
          progress: 1.0
        };
      } else if (this.isPreviewMode) {
        animPhase = this.getPreviewAnimationPhase(time);
      } else {
        animPhase = this.getAnimationPhase(time);
      }

      // STEP 3: Draw all components in ORDER
      // Each component is isolated with save/restore to prevent style bleed
      // This ensures clean, professional rendering

      this.ctx.save();
      this.drawBackground(time);
      this.ctx.restore();

      this.ctx.save();
      this.drawQuestion(time);
      this.ctx.restore();

      this.ctx.save();
      this.drawOptions(time, animPhase);
      this.ctx.restore();

      this.ctx.save();
      this.drawTimer(time, animPhase);
      this.ctx.restore();

      this.ctx.save();
      this.drawOverlays(animPhase);
      this.ctx.restore();

      this.ctx.save();
      this.drawExplanation(time, animPhase);
      this.ctx.restore();
    } finally {
      this.isRenderingFrame = false;
    }
  }

  /**
   * Play TTS audio for current question if available
   */
  async playTTSForQuestion() {
    if (!this.questionData.tts_audio || !this.questionData.tts_audio.question) {
      return;
    }

    return new Promise((resolve) => {
      try {
        const audio = new Audio(this.questionData.tts_audio.question);
        audio.onended = () => resolve();
        audio.onerror = (e) => {
          console.warn('TTS question audio error:', e);
          resolve();
        };
        // Fallback timeout in case onended never fires
        const fallback = setTimeout(() => {
          console.warn('TTS question audio timeout');
          resolve();
        }, 30000);

        audio.play().catch(e => {
          console.warn('Could not play TTS audio for question:', e);
          clearTimeout(fallback);
          resolve();
        });
      } catch (e) {
        console.warn('TTS question audio exception:', e);
        resolve();
      }
    });
  }

  /**
   * Play TTS audio for specific option if available
   */
  async playTTSForOption(index) {
    if (!this.questionData.tts_audio || !this.questionData.tts_audio.options ||
        !this.questionData.tts_audio.options[index]) {
      return;
    }

    return new Promise((resolve) => {
      try {
        const audio = new Audio(this.questionData.tts_audio.options[index]);
        audio.onended = () => resolve();
        audio.onerror = (e) => {
          console.warn('TTS option audio error:', e);
          resolve();
        };
        // Fallback timeout
        const fallback = setTimeout(() => {
          console.warn('TTS option audio timeout');
          resolve();
        }, 20000);

        audio.play().catch(e => {
          console.warn('Could not play TTS audio for option:', e);
          clearTimeout(fallback);
          resolve();
        });
      } catch (e) {
        console.warn('TTS option audio exception:', e);
        resolve();
      }
    });
  }

  /**
   * Get animation phase for preview mode (continuous loop)
   */
  getPreviewAnimationPhase(time) {
    const totalDuration = this.getTotalDuration();
    const loopTime = time % totalDuration;
    return this.getAnimationPhase(loopTime);
  }

  /**
   * Enable static preview mode (show all elements at once)
   */
  enableStaticPreview() {
    this.staticPreviewMode = true;
    this.stopAnimation();
    this.renderFrame(0);
  }

  /**
   * Disable static preview mode (use animations)
   */
  disableStaticPreview() {
    this.staticPreviewMode = false;
  }

  /**
   * Start animation loop (for preview) - PROFESSIONAL GRADE
   * @param {number} maxCycles - Maximum number of cycles to play (0 = infinite loop)
   *
   * Features:
   * - Smooth 60fps playback
   * - No trailing borders
   * - Sequential element animations (question → options → timer → answer)
   * - Professional transitions
   */
  startAnimation(maxCycles = 0) {
    this.staticPreviewMode = false; // Disable static mode when playing
    this.startTime = performance.now() / 1000;
    this.isPlaying = true;
    this.maxCycles = maxCycles;
    this.cycleCount = 0;

    const loop = () => {
      if (!this.isPlaying) return;

      const currentTime = (performance.now() / 1000) - this.startTime;

      // Check if we've exceeded max cycles
      if (this.maxCycles > 0) {
        const totalDuration = this.getTotalDuration();
        const completedCycles = Math.floor(currentTime / totalDuration);

        if (completedCycles >= this.maxCycles) {
          // Stop at the end of the last cycle
          this.stopAnimation();
          this.renderFrame(totalDuration * this.maxCycles);
          return;
        }
      }

      // SMOOTH RENDERING: Each frame is completely cleared before drawing
      // This eliminates ALL trailing artifacts
      this.renderFrame(currentTime);

      // Request next frame at browser's optimal refresh rate (60fps)
      this.animationFrameId = requestAnimationFrame(loop);
    };

    // Start the animation loop
    loop();
  }

  /**
   * Stop animation loop
   */
  stopAnimation() {
    this.isPlaying = false;
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
  }

  /**
   * Get current playback time
   */
  getCurrentTime() {
    if (!this.startTime) return 0;
    return (performance.now() / 1000) - this.startTime;
  }

  /**
   * Seek to specific time
   */
  seekTo(time) {
    this.startTime = (performance.now() / 1000) - time;
    this.renderFrame(time);
  }

  /**
   * Clean up resources
   */
  destroy() {
    this.stopAnimation();

    // Clear video elements
    this.mediaCache.videos.forEach(video => {
      video.pause();
      video.src = '';
    });

    this.mediaCache.images.clear();
    this.mediaCache.videos.clear();
    this.mediaCache.fonts.clear();
  }
}
