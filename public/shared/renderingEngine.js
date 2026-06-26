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
    
    // Animation state
    this.startTime = null;
    this.animationFrameId = null;
    this.isPlaying = false;
    
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
   * Preload all media assets (images, videos, fonts)
   */
  async preloadAssets() {
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
    
    // Preload fonts
    if (this.config.question?.font_family) {
      const fontUrl = this.config.question.font_family_url || null;
      promises.push(this.loadFont(this.config.question.font_family, fontUrl));
    }
    if (this.config.options?.font_family) {
      const fontUrl = this.config.options.font_family_url || null;
      promises.push(this.loadFont(this.config.options.font_family, fontUrl));
    }
    
    await Promise.all(promises);
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
   * - If fontFamily is a system font name (no extension) and no explicit URL was provided, skip downloading.
   * - If fontFamily looks like a file name (ttf/otf/woff/woff2) load it from /storage/fonts or provided URL.
   */
  async loadFont(fontFamily, fontUrl = null) {
    if (!fontFamily || this.mediaCache.fonts.has(fontFamily)) {
      return;
    }

    const looksLikeFile = /\.(ttf|otf|woff|woff2)$/i.test(fontFamily);
    if (!fontUrl && !looksLikeFile) {
      // Treat as system font; no network fetch.
      this.mediaCache.fonts.set(fontFamily, true);
      return;
    }

    try {
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

      const url = fontUrl || `/storage/fonts/${fontFamily}`;
      const fontFamilyName = fontFamily.replace(/\.(ttf|otf|woff|woff2)$/i, '');

      const fontFace = new FontFace(fontFamilyName, `url("${url}")`);
      await loadWithTimeout(fontFace.load(), 3000);
      document.fonts.add(fontFace);
      this.mediaCache.fonts.set(fontFamily, true);
    } catch (e) {
      console.warn(`Font ${fontFamily} not loaded, using fallback. Error: ${e.message}`);
      this.mediaCache.fonts.set(fontFamily, false);
    }
  }
  
  /**
   * Clear the entire canvas
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
    
    if (!text) return;
    
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
      const fontFamily = q.font_family.replace('.ttf', '').replace('.otf', '');
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
    
    // Setup font
    const fontFamily = q.font_family.replace('.ttf', '').replace('.otf', '');
    this.ctx.font = `${q.font_size}px ${fontFamily}, sans-serif`;
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
   * Draw wrapped text (handles multi-line)
   */
  drawWrappedText(text, x, y, maxWidth, lineHeight) {
    const words = text.split(' ');
    let line = '';
    let currentY = y;
    
    for (let i = 0; i < words.length; i++) {
      const testLine = line + words[i] + ' ';
      const metrics = this.ctx.measureText(testLine);
      
      if (metrics.width > maxWidth && i > 0) {
        this.ctx.fillText(line, x, currentY);
        line = words[i] + ' ';
        currentY += lineHeight;
      } else {
        line = testLine;
      }
    }
    this.ctx.fillText(line, x, currentY);
  }
  
  /**
   * Draw all options based on layout
   */
  drawOptions(time) {
    const opts = this.config.options;
    const options = this.questionData.options || [];
    const correctIndex = this.questionData.correct_answer_index || 0;
    
    // Calculate reveal time (after timer ends)
    const timerDuration = this.config.timer.duration || 5;
    const showCorrect = time >= timerDuration;
    
    // Parse layout format (e.g., "2x2", "1x4", "1x2x1")
    const positions = this.calculateOptionPositions(opts.layout_format, options.length);
    
    options.forEach((option, index) => {
      if (positions[index]) {
        const isCorrect = index === correctIndex;
        this.drawOption(option, positions[index], index, isCorrect && showCorrect);
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
  drawOption(optionText, position, index, isCorrect) {
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
    
    // Draw text with margin applied
    const fontFamily = opts.font_family.replace('.ttf', '').replace('.otf', '');
    this.ctx.font = `${opts.font_size}px ${fontFamily}, sans-serif`;
    this.ctx.fillStyle = isCorrect ? (opts.correct_answer_font_color || opts.font_color) : opts.font_color;
    this.ctx.textBaseline = 'middle';

    // Label rendering (A/B/C/D or 1/2/3/4)
    const labelStyle = opts.label_style || 'none';
    const labelSpacing = parseInt(opts.label_spacing || 10);

    const textY = y + height / 2;

    if (labelStyle === 'none') {
      this.ctx.textAlign = opts.text_align || 'center';
      this.ctx.fillText(optionText, x + width / 2, textY);
      return;
    }

    let label = '';
    if (labelStyle === 'numeric') {
      label = String(index + 1) + '.';
    } else if (labelStyle === 'alpha' || labelStyle === 'alphabetic') {
      label = String.fromCharCode(65 + index) + '.';
    } else {
      // Unknown style, fallback to no label
      this.ctx.textAlign = opts.text_align || 'center';
      this.ctx.fillText(optionText, x + width / 2, textY);
      return;
    }

    this.ctx.textAlign = 'left';
    const effectiveMarginLeft = marginLeft > 0 ? marginLeft : 10;
    const startX = x + effectiveMarginLeft;

    // label
    this.ctx.fillText(label, startX, textY);

    // option text
    const labelWidth = this.ctx.measureText(label).width;
    const optionTextX = startX + labelWidth + labelSpacing;
    this.ctx.fillText(optionText, optionTextX, textY);
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
  drawTimer(time) {
    const timer = this.config.timer;
    const duration = timer.duration || 5;
    
    // In preview mode, always show timer with sample countdown
    let remaining;
    if (this.isPreviewMode) {
      // Show a sample countdown (cycle through values)
      remaining = Math.max(1, Math.ceil(duration - (time % (duration + 1))));
    } else {
      remaining = Math.max(0, Math.ceil(duration - time));
      if (time >= duration) {
        return; // Timer finished in video generation
      }
    }
    
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
    this.ctx.font = `bold ${timer.font_size}px Arial, sans-serif`;
    this.ctx.fillStyle = timer.font_color || '#000000';
    this.ctx.textAlign = 'center';
    this.ctx.textBaseline = 'middle';
    this.ctx.fillText(String(remaining), x + width / 2, y + height / 2);
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
   * Render a single frame at given time
   */
  renderFrame(time) {
    // Clear canvas
    this.clearCanvas();
    
    // Draw all components in order
    this.drawBackground(time);
    this.drawQuestion(time);
    this.drawOptions(time);
    this.drawTimer(time);
  }
  
  /**
   * Start animation loop (for preview)
   */
  startAnimation() {
    this.startTime = performance.now() / 1000;
    this.isPlaying = true;
    
    const loop = () => {
      if (!this.isPlaying) return;
      
      const currentTime = (performance.now() / 1000) - this.startTime;
      this.renderFrame(currentTime);
      
      this.animationFrameId = requestAnimationFrame(loop);
    };
    
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
