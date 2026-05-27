import { CanvasRenderer } from '/shared/renderingEngine.js';

class DesignerUI {
  constructor() {
    this.canvas = document.getElementById('previewCanvas');
    this.renderer = null;
    this.presetConfig = null;
    this.sampleQuestion = null;
    this.updateTimeout = null;
    this.currentPresetId = null;
    this.isRendering = false;
    this.lastConfig = null;

    this.init();
  }

  async init() {
    try {
      this.updateStatus('Loading preset configuration...', 'loading');

      // Load all presets first
      await this.loadAllPresets();
      
      // Initialize preset selector
      this.initializePresetSelector();

      // Load preset and question data
      await this.loadData();

      // Setup UI controls
      this.setupTabs();
      this.setupControls();
      this.setupPreviewControls();

      // Keep open custom selects positioned correctly (the property panel uses overflow:hidden)
      this._customSelectRepositionHandler = () => this.repositionOpenCustomSelectDropdowns();
      window.addEventListener('resize', this._customSelectRepositionHandler);
      // capture=true so we catch scroll on internal containers (like tab-content)
      document.addEventListener('scroll', this._customSelectRepositionHandler, true);

      // Initialize renderer
      await this.initRenderer();

      this.updateStatus('Ready', 'success');

    } catch (error) {
      console.error('Initialization error:', error);
      this.updateStatus('Error: ' + error.message, 'error');
    }
  }

  async loadAllPresets() {
    try {
      const response = await fetch('/api/presets');
      this.allPresets = await response.json();

      // Empty presets is a valid state (fresh install / user cleared presets)
      if (!Array.isArray(this.allPresets)) {
        this.allPresets = [];
      }

      console.log(`Loaded ${this.allPresets.length} presets`);
    } catch (error) {
      console.error('Error loading presets:', error);
      this.allPresets = [];
    }
  }

  getDefaultPresetConfig() {
    // Minimal config required by shared/renderingEngine.js + designer UI controls
    return {
      canvas: {
        width: 1920,
        height: 1080,
        aspect_ratio: '16:9',
        background_type: 'color',
        background_color: '#1a1a1a',
        background_fit: 'cover',
        background_pos_x: 0.5,
        background_pos_y: 0.5,
        intro: { type: 'none', url: '', duration: 3, fit: 'cover' },
        outro: { type: 'none', url: '', duration: 3, fit: 'cover' }
      },
      question: {
        position: { x: 120, y: 120 },
        width: 1680,
        height: 0,
        font_family: 'Arial',
        font_size: 64,
        font_color: '#ffffff',
        text_align: 'left',
        background_type: 'none',
        background_color: '#000000',
        background_fit: 'cover',
        background_pos_x: 0.5,
        background_pos_y: 0.5,
        margin: { top: 0, right: 0, bottom: 0, left: 0 },
        border_radius: 0,
        show_border_preview: true,
        numbering: 'none',
        start_number: 1,
        number_gap: 2
      },
      options: {
        container_position: { x: 120, y: 420 },
        layout_format: '2x2',
        option_width: 800,
        option_height: 180,
        spacing: { horizontal: 80, vertical: 60 },
        font_family: 'Arial',
        font_size: 48,
        font_color: '#ffffff',
        label_style: 'alphabetic',
        label_spacing: 12,
        margin: { top: 0, right: 0, bottom: 0, left: 0 },
        border_radius: 0,
        border_width: 0,
        border_color: '#ffffff',
        background_type: 'color',
        background_color: '#2c2c2c',
        background_fit: 'cover',
        background_pos_x: 0.5,
        background_pos_y: 0.5,
        correct_answer_background_type: 'color',
        correct_answer_background_color: '#1b5e20',
        correct_answer_background_fit: 'cover',
        correct_answer_background_pos_x: 0.5,
        correct_answer_background_pos_y: 0.5,
        correct_answer_font_color: '#ffffff',
        show_border_preview: true
      },
      timer: {
        duration: 5,
        position: { x: 860, y: 860 },
        width: 200,
        height: 200,
        font_family: 'Arial',
        font_size: 72,
        font_color: '#000000',
        background_type: 'color',
        background_color: '#ffffff',
        background_fit: 'cover',
        background_pos_x: 0.5,
        background_pos_y: 0.5,
        border_width: 0,
        border_color: '#ffffff',
        border_radius: 0
      },
      audio: {
        play_first_question: true,
        play_next_question: true,
        play_last_question: true,
        play_options: true,
        question_gap: 1.0,
        option_reveal_mode: 'all',
        option_delay: 0.5,
        question_to_options_gap: 0.5
      },
      animation: {
        question_display_duration: 2,
        options_display_mode: 'all_at_once',
        option_reveal_delay: 0.5,
        answer_reveal_duration: 3,
        tts_enabled: false,
        tts_voice: 'default',
        tts_speed: 1.0,
        transition_effect: 'fade',
        transition_duration: 0.3
      },
      explanation: {
        enabled: false,
        duration: 3,
        background_type: 'none',
        background_color: '#ffffff',
        background_image: '',
        background_fit: 'cover',
        font_family: 'Arial',
        font_size: 36,
        font_color: '#000000',
        text_align: 'center',
        width: 1200,
        height: 0,
        image_enabled: false,
        image_url: '',
        image_width: 400,
        image_height: 300,
        image_fit: 'contain',
        image_position: 'center'
      },
      overlays: [],
      custom_overlays: []
    };
  }

  async loadPresetById(presetId) {
    try {
      const preset = this.allPresets.find(p => p.id === presetId); // String comparison
      
      if (!preset) {
        throw new Error('Preset not found');
      }

      this.currentPresetId = preset.id;
      this.currentPresetName = preset.name;

      // Parse config if it's a string
      this.presetConfig = typeof preset.config === 'string'
        ? JSON.parse(preset.config)
        : preset.config;

      // Fetch a sample question
      const questionRes = await fetch('/api/questions?limit=1');
      const questionData = await questionRes.json();

      if (!questionData.questions || !questionData.questions.length) {
        throw new Error('No questions found');
      }

      // Load sample question and ensure it has options for preview
      this.sampleQuestion = questionData.questions[0];

      // Ensure the sample question has options for preview purposes
      if (!this.sampleQuestion.options || !Array.isArray(this.sampleQuestion.options) || this.sampleQuestion.options.length === 0) {
        this.sampleQuestion.options = ['Option A', 'Option B', 'Option C', 'Option D'];
      }

      // Ensure it has a correct answer index
      if (typeof this.sampleQuestion.correct_answer_index === 'undefined' || this.sampleQuestion.correct_answer_index === null) {
        this.sampleQuestion.correct_answer_index = 0;
      }

      // Load media library
      await this.loadMediaLibrary();

      // Load fonts library
      await this.loadFontsLibrary();

      // Populate form with preset values
      this.populateForm();

      // Warn if preset references missing font files
      this.updateFontWarnings();

      // Reinitialize renderer with new config
      if (this.renderer) {
        this.renderer.destroy();
        this.renderer = null;
      }
      await this.initRenderer();
      
      console.log(`Loaded preset: ${preset.name}`);
    } catch (error) {
      console.error('Error loading preset:', error);
      throw error;
    }
  }

  async loadData() {
    try {
      // Load the first preset by default; if none exist, initialize with a default config
      if (this.allPresets && this.allPresets.length > 0) {
        await this.loadPresetById(this.allPresets[0].id);
      } else {
        this.currentPresetId = null;
        this.currentPresetName = 'New Preset';
        this.presetConfig = this.getDefaultPresetConfig();
      }

      /* OLD CODE - kept for reference
      // Fetch preset
      const presetRes = await fetch('/api/presets');
      const presets = await presetRes.json();

      if (!presets.length) {
        throw new Error('No presets found');
      }

      const preset = presets[0];

      // Parse config if it's a string
      this.presetConfig = typeof preset.config === 'string'
        ? JSON.parse(preset.config)
        : preset.config;
      */

      // Fetch a sample question
      const questionRes = await fetch('/api/questions?limit=1');
      const questionData = await questionRes.json();

      if (!questionData.questions || !questionData.questions.length) {
        throw new Error('No questions found');
      }

      // Load sample question and ensure it has options for preview
      this.sampleQuestion = questionData.questions[0];

      // Ensure the sample question has options for preview purposes
      if (!this.sampleQuestion.options || !Array.isArray(this.sampleQuestion.options) || this.sampleQuestion.options.length === 0) {
        this.sampleQuestion.options = ['Option A', 'Option B', 'Option C', 'Option D'];
      }

      // Ensure it has a correct answer index
      if (typeof this.sampleQuestion.correct_answer_index === 'undefined' || this.sampleQuestion.correct_answer_index === null) {
        this.sampleQuestion.correct_answer_index = 0;
      }

      // Load media library
      await this.loadMediaLibrary();

      // Load fonts library
      await this.loadFontsLibrary();

      // Populate form with preset values
      this.populateForm();

      // Initialize searchable selects backed by native select elements
      this.initializeNativeSelectSearches();

      // Warn if preset references missing font files
      this.updateFontWarnings();

    } catch (error) {
      console.error('Error loading data:', error);
      throw error;
    }
  }

  async loadMediaLibrary() {
    try {
      const response = await fetch('/api/media');
      const media = await response.json();
      
      // Separate images and videos
      this.mediaLibrary = {
        images: media.filter(m => m.type === 'image'),
        videos: media.filter(m => m.type === 'video'),
        all: media
      };
      
      console.log('Media library loaded:', this.mediaLibrary);
    } catch (error) {
      console.error('Error loading media library:', error);
      this.mediaLibrary = { images: [], videos: [], all: [] };
    }
  }

  async loadFontsLibrary() {
    try {
      const response = await fetch('/api/fonts');
      const fonts = await response.json();

      const loadedFonts = [];

      // Load font faces for preview; only keep fonts that actually load.
      for (const font of Array.isArray(fonts) ? fonts : []) {
        try {
          const fontFace = new FontFace(font.name, `url(${font.url})`);
          await fontFace.load();
          document.fonts.add(fontFace);
          loadedFonts.push(font);
        } catch (error) {
          console.warn(`Failed to load font (will be hidden): ${font.name}`, error);
        }
      }

      this.fontsLibrary = loadedFonts;

      // Populate font dropdowns
      this.populateFontDropdowns();

      console.log('Fonts library loaded:', this.fontsLibrary);
    } catch (error) {
      console.error('Error loading fonts library:', error);
      this.fontsLibrary = [];
    }
  }

  populateFontDropdowns() {
    // Question font dropdown
    const qFontFamily = document.getElementById('qFontFamily');
    if (qFontFamily && this.fontsLibrary.length > 0) {
      // Store default browser fonts
      const defaultOptions = Array.from(qFontFamily.options);
      
      qFontFamily.innerHTML = '';
      
      // Add default browser fonts
      defaultOptions.forEach(opt => qFontFamily.appendChild(opt));
      
      // Add separator
      const separator = document.createElement('option');
      separator.disabled = true;
      separator.textContent = '--- Custom Fonts ---';
      qFontFamily.appendChild(separator);
      
      // Add custom fonts from database
      this.fontsLibrary.forEach(font => {
        const option = document.createElement('option');
        option.value = font.name;
        option.textContent = font.name;
        qFontFamily.appendChild(option);
      });
    }

    // Populate optFontFamily with the same fonts
    const optFontFamily = document.getElementById('optFontFamily');
    if (optFontFamily && this.fontsLibrary.length > 0) {
      // Store default browser fonts
      const defaultOptions = Array.from(optFontFamily.options);
      
      optFontFamily.innerHTML = '';
      
      // Add default browser fonts
      defaultOptions.forEach(opt => optFontFamily.appendChild(opt));
      
      // Add separator
      const separator = document.createElement('option');
      separator.disabled = true;
      separator.textContent = '--- Custom Fonts ---';
      optFontFamily.appendChild(separator);
      
      // Add custom fonts from database
      this.fontsLibrary.forEach(font => {
        const option = document.createElement('option');
        option.value = font.name;
        option.textContent = font.name;
        optFontFamily.appendChild(option);
      });
    }

    // Populate explanation font family
    const explFontFamily = document.getElementById('explFontFamily');
    if (explFontFamily && this.fontsLibrary.length > 0) {
      const defaultOptions = Array.from(explFontFamily.options);
      explFontFamily.innerHTML = '';
      defaultOptions.forEach(opt => explFontFamily.appendChild(opt));

      const separator = document.createElement('option');
      separator.disabled = true;
      separator.textContent = '--- Custom Fonts ---';
      explFontFamily.appendChild(separator);

      this.fontsLibrary.forEach(font => {
        const option = document.createElement('option');
        option.value = font.name;
        option.textContent = font.name;
        explFontFamily.appendChild(option);
      });
    }

    this.syncNativeSelectSearches();
  }

  initializePresetSelector() {
    const selectEl = document.getElementById('presetSelector');
    if (!selectEl) return;

    const trigger = selectEl.querySelector('.custom-select-trigger');
    const dropdown = selectEl.querySelector('.custom-select-dropdown');
    const searchInput = selectEl.querySelector('.custom-select-search');
    const optionsContainer = selectEl.querySelector('.custom-select-options');
    const valueDisplay = selectEl.querySelector('.custom-select-value');

    // Populate preset options
    const options = this.allPresets.map(preset => ({
      value: preset.id,
      text: preset.name,
      icon: '🎨'
    }));

    selectEl.customSelectData = { allOptions: options };

    // Toggle dropdown
    trigger.addEventListener('click', (e) => {
      e.stopPropagation();
      document.querySelectorAll('.custom-select.open').forEach(el => {
        if (el !== selectEl) el.classList.remove('open');
      });
      selectEl.classList.toggle('open');
      if (selectEl.classList.contains('open')) {
        searchInput.value = '';
        searchInput.focus();
        this.filterPresetOptions(selectEl, '');
      }
    });

    // Search functionality
    searchInput.addEventListener('input', (e) => {
      this.filterPresetOptions(selectEl, e.target.value);
    });

    // Close when clicking outside
    document.addEventListener('click', (e) => {
      if (!selectEl.contains(e.target)) {
        selectEl.classList.remove('open');
      }
    });

    // Handle preset selection
    optionsContainer.addEventListener('click', async (e) => {
      const option = e.target.closest('.custom-select-option');
      if (option && !option.classList.contains('custom-select-no-results')) {
        const presetId = option.dataset.value; // Keep as string - IDs can be UUIDs
        
        // Get text from the span (not the icon)
        const textSpan = option.querySelector('span:not(.custom-select-option-icon)');
        const text = textSpan ? textSpan.textContent.trim() : option.textContent.trim();
        
        // Update display
        valueDisplay.textContent = text;
        valueDisplay.classList.remove('placeholder');
        
        // Close dropdown
        selectEl.classList.remove('open');
        
        // Load the selected preset
        this.updateStatus('Loading preset...', 'loading');
        try {
          await this.loadPresetById(presetId);
          this.updateStatus('Ready', 'success');
        } catch (error) {
          console.error('Error loading preset:', error);
          this.updateStatus('Error loading preset', 'error');
        }
      }
    });

    // Set initial value
    if (this.allPresets.length > 0) {
      valueDisplay.textContent = this.allPresets[0].name;
      valueDisplay.classList.remove('placeholder');
    } else {
      valueDisplay.textContent = 'No presets (save to create)';
      valueDisplay.classList.add('placeholder');
    }

    // Initial render
    this.filterPresetOptions(selectEl, '');
  }

  filterPresetOptions(selectEl, searchTerm) {
    const optionsContainer = selectEl.querySelector('.custom-select-options');
    const allOptions = selectEl.customSelectData?.allOptions || [];
    
    const filtered = allOptions.filter(opt => 
      opt.text.toLowerCase().includes(searchTerm.toLowerCase())
    );
    
    optionsContainer.innerHTML = '';
    
    if (filtered.length === 0) {
      optionsContainer.innerHTML = '<div class="custom-select-no-results">No presets found</div>';
    } else {
      filtered.forEach(opt => {
        const optionEl = document.createElement('div');
        optionEl.className = 'custom-select-option';
        optionEl.dataset.value = opt.value;
        optionEl.innerHTML = `
          <span class="custom-select-option-icon">${opt.icon}</span>
          <span>${opt.text}</span>
        `;
        
        // Mark as selected if matches current preset
        if (this.currentPresetId && opt.value === this.currentPresetId) {
          optionEl.classList.add('selected');
        }
        
        optionsContainer.appendChild(optionEl);
      });
    }
  }

  populateForm() {
    // Canvas
    // Ensure intro/outro config exists
    if (!this.presetConfig.canvas.intro) {
      this.presetConfig.canvas.intro = { type: 'none', url: '', duration: 3, fit: 'cover' };
    }
    if (!this.presetConfig.canvas.outro) {
      this.presetConfig.canvas.outro = { type: 'none', url: '', duration: 3, fit: 'cover' };
    }

    const aspectRatio = this.presetConfig.canvas.aspect_ratio;
    const canvasSizeSelect = document.getElementById('canvasSize');
    
    // Check if aspect ratio matches any preset
    let foundPreset = false;
    for (let option of canvasSizeSelect.options) {
      if (option.value === aspectRatio) {
        canvasSizeSelect.value = aspectRatio;
        foundPreset = true;
        break;
      }
    }
    
    // If not a preset, set to custom and show custom inputs
    if (!foundPreset && aspectRatio) {
      canvasSizeSelect.value = 'custom';
      document.getElementById('customSizeGroup').style.display = 'block';
      document.getElementById('customHeightGroup').style.display = 'block';
      document.getElementById('customWidth').value = this.presetConfig.canvas.width || 1920;
      document.getElementById('customHeight').value = this.presetConfig.canvas.height || 1080;
    }
    
    document.getElementById('bgType').value = this.presetConfig.canvas.background_type;
    document.getElementById('bgColor').value = this.presetConfig.canvas.background_color || '#1a1a1a';
    document.getElementById('bgFit').value = this.presetConfig.canvas.background_fit || 'cover';

    // Intro/Outro
    document.getElementById('introType').value = this.presetConfig.canvas.intro.type || 'none';
    document.getElementById('introFit').value = this.presetConfig.canvas.intro.fit || 'cover';
    document.getElementById('introDuration').value = this.presetConfig.canvas.intro.duration || 3;

    document.getElementById('outroType').value = this.presetConfig.canvas.outro.type || 'none';
    document.getElementById('outroFit').value = this.presetConfig.canvas.outro.fit || 'cover';
    document.getElementById('outroDuration').value = this.presetConfig.canvas.outro.duration || 3;
    
    // Initialize media dropdowns
    this.initializeMediaSelects();
    this.syncNativeSelectSearches();

    // Question
    document.getElementById('qFontFamily').value = this.presetConfig.question.font_family;
    document.getElementById('qFontSize').value = this.presetConfig.question.font_size;
    document.getElementById('qFontColor').value = this.presetConfig.question.font_color;
    document.getElementById('qPosX').value = this.presetConfig.question.position.x;
    document.getElementById('qPosY').value = this.presetConfig.question.position.y;
    document.getElementById('qWidth').value = this.presetConfig.question.width || this.presetConfig.question.max_width || 1720;
    document.getElementById('qHeight').value = this.presetConfig.question.height || 0;
    document.getElementById('qTextAlign').value = this.presetConfig.question.text_align;
    
    // Margin setup
    if (!this.presetConfig.question.margin) {
      this.presetConfig.question.margin = { top: 0, right: 0, bottom: 0, left: 0 };
    } else if (typeof this.presetConfig.question.margin === 'number') {
      // Old format - convert to object
      const oldMargin = this.presetConfig.question.margin;
      this.presetConfig.question.margin = { top: oldMargin, right: oldMargin, bottom: oldMargin, left: oldMargin };
    }
    
    const margin = this.presetConfig.question.margin;
    document.getElementById('qMarginAll').value = margin.top || 0;
    document.getElementById('qMarginTop').value = margin.top || 0;
    document.getElementById('qMarginRight').value = margin.right || 0;
    document.getElementById('qMarginBottom').value = margin.bottom || 0;
    document.getElementById('qMarginLeft').value = margin.left || 0;
    
    // Check if all sides are equal
    const allEqual = margin.top === margin.right && margin.right === margin.bottom && margin.bottom === margin.left;
    document.getElementById('qMarginMode').value = allEqual ? 'all' : 'custom';
    
    document.getElementById('qBgType').value = this.presetConfig.question.background_type || 'none';
    document.getElementById('qBgColor').value = this.presetConfig.question.background_color || '#000000';
    document.getElementById('qBgFit').value = this.presetConfig.question.background_fit || 'cover';
    document.getElementById('qBorderRadius').value = this.presetConfig.question.border_radius || 0;
    document.getElementById('qNumbering').value = this.presetConfig.question.numbering || 'none';
    document.getElementById('qStartNumber').value = this.presetConfig.question.start_number || 1;
    document.getElementById('qNumberGap').value = this.presetConfig.question.number_gap || 2;
    document.getElementById('qShowBorder').checked = this.presetConfig.question.show_border_preview || false;

    // Options
    document.getElementById('optLayout').value = this.presetConfig.options.layout_format;
    document.getElementById('optPosX').value = this.presetConfig.options.container_position.x;
    document.getElementById('optPosY').value = this.presetConfig.options.container_position.y;
    document.getElementById('optWidth').value = this.presetConfig.options.option_width;
    document.getElementById('optHeight').value = this.presetConfig.options.option_height;
    document.getElementById('optHSpacing').value = this.presetConfig.options.spacing.horizontal;
    document.getElementById('optVSpacing').value = this.presetConfig.options.spacing.vertical;
    document.getElementById('optBorderRadius').value = this.presetConfig.options.border_radius || 0;
    document.getElementById('optShowBorder').checked = this.presetConfig.options.show_border_preview || false;
    
    // Options margin setup
    if (!this.presetConfig.options.margin) {
      this.presetConfig.options.margin = { top: 0, right: 0, bottom: 0, left: 0 };
    } else if (typeof this.presetConfig.options.margin === 'number') {
      const oldMargin = this.presetConfig.options.margin;
      this.presetConfig.options.margin = { top: oldMargin, right: oldMargin, bottom: oldMargin, left: oldMargin };
    }
    
    const optMargin = this.presetConfig.options.margin;
    document.getElementById('optMarginAll').value = optMargin.top || 0;
    document.getElementById('optMarginTop').value = optMargin.top || 0;
    document.getElementById('optMarginRight').value = optMargin.right || 0;
    document.getElementById('optMarginBottom').value = optMargin.bottom || 0;
    document.getElementById('optMarginLeft').value = optMargin.left || 0;
    
    const optAllEqual = optMargin.top === optMargin.right && optMargin.right === optMargin.bottom && optMargin.bottom === optMargin.left;
    document.getElementById('optMarginMode').value = optAllEqual ? 'all' : 'custom';
    
    // Option label style and spacing
    let labelStyle = this.presetConfig.options.label_style || 'none';
    if (labelStyle === 'alphabetic') labelStyle = 'alpha'; // Migration
    this.presetConfig.options.label_style = labelStyle; // Update config to match UI value
    document.getElementById('optLabelStyle').value = labelStyle;
    document.getElementById('optLabelSpacing').value = this.presetConfig.options.label_spacing || 10;
    
    document.getElementById('optFontFamily').value = this.presetConfig.options.font_family || 'Arial';
    document.getElementById('optFontSize').value = this.presetConfig.options.font_size;
    document.getElementById('optFontColor').value = this.presetConfig.options.font_color;
    document.getElementById('optBgType').value = this.presetConfig.options.background_type;
    document.getElementById('optBgColor').value = this.presetConfig.options.background_color || '#EEEEEE';
    document.getElementById('optCorrectFontColor').value = this.presetConfig.options.correct_answer_font_color || '#FFFFFF';
    document.getElementById('optCorrectBgType').value = this.presetConfig.options.correct_answer_background_type;
    document.getElementById('optCorrectBgColor').value = this.presetConfig.options.correct_answer_background_color || '#0aa052';

    // Timer
    document.getElementById('timerDuration').value = this.presetConfig.timer.duration;
    document.getElementById('timerPosX').value = this.presetConfig.timer.position.x;
    document.getElementById('timerPosY').value = this.presetConfig.timer.position.y;
    document.getElementById('timerWidth').value = this.presetConfig.timer.width;
    document.getElementById('timerHeight').value = this.presetConfig.timer.height;
    document.getElementById('timerFontSize').value = this.presetConfig.timer.font_size;
    document.getElementById('timerFontColor').value = this.presetConfig.timer.font_color;
    document.getElementById('timerBgType').value = this.presetConfig.timer.background_type;
    document.getElementById('timerBgColor').value = this.presetConfig.timer.background_color || '#FFFFFF';
    document.getElementById('timerBorderRadius').value = this.presetConfig.timer.border_radius;

    // Audio Narration Tab
    if (!this.presetConfig.audio) {
      this.presetConfig.audio = {
        play_first_question: true,
        play_next_question: true,
        play_last_question: true,
        play_options: true,
        question_gap: 1.0,
        option_reveal_mode: 'all',
        option_delay: 0.5,
        question_to_options_gap: 0.5
      };
    }
    
    document.getElementById('audioPlayFirstQuestion').checked = this.presetConfig.audio.play_first_question;
    document.getElementById('audioPlayNextQuestion').checked = this.presetConfig.audio.play_next_question;
    document.getElementById('audioPlayLastQuestion').checked = this.presetConfig.audio.play_last_question;
    document.getElementById('audioPlayOptions').checked = this.presetConfig.audio.play_options;
    document.getElementById('audioQuestionGap').value = this.presetConfig.audio.question_gap;
    document.getElementById('audioOptionRevealMode').value = this.presetConfig.audio.option_reveal_mode;
    document.getElementById('audioOptionDelay').value = this.presetConfig.audio.option_delay;
    document.getElementById('audioQToOptionsGap').value = this.presetConfig.audio.question_to_options_gap;

    // Explanation Tab
    if (!this.presetConfig.explanation) {
      this.presetConfig.explanation = {
        enabled: false,
        duration: 3,
        background_type: 'none',
        background_color: '#ffffff',
        background_image: '',
        background_fit: 'cover',
        font_family: 'Arial',
        font_size: 36,
        font_color: '#000000',
        text_align: 'center',
        width: 1200,
        height: 0,
        image_enabled: false,
        image_url: '',
        image_width: 400,
        image_height: 300,
        image_fit: 'contain',
        image_position: 'center'
      };
    }

    if (!Array.isArray(this.presetConfig.overlays)) {
      this.presetConfig.overlays = [];
    }
    
    document.getElementById('explEnabled').checked = this.presetConfig.explanation.enabled || false;
    document.getElementById('explDuration').value = this.presetConfig.explanation.duration || 3;
    document.getElementById('explBgType').value = this.presetConfig.explanation.background_type || 'none';
    document.getElementById('explBgColor').value = this.presetConfig.explanation.background_color || '#ffffff';
    document.getElementById('explBgImageFit').value = this.presetConfig.explanation.background_fit || 'cover';
    document.getElementById('explFontFamily').value = this.presetConfig.explanation.font_family || 'Arial';
    document.getElementById('explFontSize').value = this.presetConfig.explanation.font_size || 36;
    document.getElementById('explFontColor').value = this.presetConfig.explanation.font_color || '#000000';
    document.getElementById('explTextAlign').value = this.presetConfig.explanation.text_align || 'center';
    document.getElementById('explWidth').value = this.presetConfig.explanation.width ?? 1200;
    document.getElementById('explHeight').value = this.presetConfig.explanation.height ?? 0;
    document.getElementById('explImageEnabled').checked = this.presetConfig.explanation.image_enabled || false;
    document.getElementById('explImageWidth').value = this.presetConfig.explanation.image_width || 400;
    document.getElementById('explImageHeight').value = this.presetConfig.explanation.image_height || 300;
    document.getElementById('explImageFit').value = this.presetConfig.explanation.image_fit || 'contain';
    document.getElementById('explImagePosition').value = this.presetConfig.explanation.image_position || 'center';

    // Update all value displays
    this.updateAllValueDisplays();

    // Show/hide conditional fields
    this.toggleBackgroundFields();
    this.toggleIntroFields();
    this.toggleOutroFields();
    this.toggleQuestionBackgroundFields();
    this.toggleOptionBackgroundFields();
    this.toggleTimerBackgroundFields();
    this.toggleQuestionNumberingFields();
    this.toggleMarginMode();
    this.toggleOptionsMarginMode();
    this.toggleOptionsLabelSpacing();
    this.toggleExplanationBackgroundFields();
    this.toggleExplanationImageFields();
    this.renderOverlayList();
    
    // Force populate options background media after initialization
    setTimeout(() => {
      const optBgType = document.getElementById('optBgType')?.value;
      if (optBgType && optBgType !== 'color') {
        this.populateMediaSelect('optBgMedia', optBgType);
      }
      const optCorrectBgType = document.getElementById('optCorrectBgType')?.value;
      if (optCorrectBgType && optCorrectBgType !== 'color') {
        this.populateMediaSelect('optCorrectBgMedia', optCorrectBgType);
      }
    }, 100);
  }

  setupTabs() {
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    tabBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        // Remove active class from all
        tabBtns.forEach(b => b.classList.remove('active'));
        tabContents.forEach(c => c.classList.remove('active'));

        // Add active to clicked
        btn.classList.add('active');
        const tabId = 'tab-' + btn.dataset.tab;
        document.getElementById(tabId).classList.add('active');
      });
    });
    
    // Setup tab navigation arrows (mobile)
    this.setupTabNavigation();
  }
  
  setupTabNavigation() {
    const tabsContainer = document.querySelector('.panel-tabs.tabs');
    const leftArrow = document.getElementById('tabNavLeft');
    const rightArrow = document.getElementById('tabNavRight');
    
    if (!tabsContainer || !leftArrow || !rightArrow) return;
    
    // Scroll tabs with arrows
    leftArrow.addEventListener('click', () => {
      tabsContainer.scrollBy({ left: -150, behavior: 'smooth' });
    });
    
    rightArrow.addEventListener('click', () => {
      tabsContainer.scrollBy({ left: 150, behavior: 'smooth' });
    });
    
    // Update arrow states based on scroll position
    const updateArrows = () => {
      const scrollLeft = tabsContainer.scrollLeft;
      const maxScroll = tabsContainer.scrollWidth - tabsContainer.clientWidth;
      
      // Disable left arrow if at start
      if (scrollLeft <= 0) {
        leftArrow.classList.add('disabled');
      } else {
        leftArrow.classList.remove('disabled');
      }
      
      // Disable right arrow if at end
      if (scrollLeft >= maxScroll - 1) {
        rightArrow.classList.add('disabled');
      } else {
        rightArrow.classList.remove('disabled');
      }
    };
    
    // Update on scroll
    tabsContainer.addEventListener('scroll', updateArrows);
    
    // Initial update
    updateArrows();
    
    // Update on window resize
    window.addEventListener('resize', updateArrows);
    
    // Setup mobile bottom sheet behavior
    this.setupMobileBottomSheet();
  }
  
  setupMobileBottomSheet() {
    // Only on mobile
    if (window.innerWidth > 768) return;
    
    const propertyPanel = document.querySelector('.property-panel');
    const tabsContainer = document.querySelector('.tabs-container');
    
    if (!propertyPanel || !tabsContainer) return;
    
    let startY = 0;
    let currentY = 0;
    let isDragging = false;
    let isExpanded = false;
    
    // Toggle expanded state on tab container click
    tabsContainer.addEventListener('click', (e) => {
      // Don't toggle if clicking arrows or tabs
      if (e.target.closest('.tab-nav-arrow') || e.target.closest('.tab-btn')) return;
      
      isExpanded = !isExpanded;
      propertyPanel.classList.toggle('expanded', isExpanded);
    });
    
    // Swipe gesture support
    tabsContainer.addEventListener('touchstart', (e) => {
      startY = e.touches[0].clientY;
      isDragging = true;
    }, { passive: true });
    
    tabsContainer.addEventListener('touchmove', (e) => {
      if (!isDragging) return;
      currentY = e.touches[0].clientY;
    }, { passive: true });
    
    tabsContainer.addEventListener('touchend', () => {
      if (!isDragging) return;
      isDragging = false;
      
      const deltaY = currentY - startY;
      
      // Swipe down to collapse
      if (deltaY > 50 && isExpanded) {
        isExpanded = false;
        propertyPanel.classList.remove('expanded');
      }
      // Swipe up to expand
      else if (deltaY < -50 && !isExpanded) {
        isExpanded = true;
        propertyPanel.classList.add('expanded');
      }
    });
    
    // Re-setup on window resize
    window.addEventListener('resize', () => {
      if (window.innerWidth > 768) {
        propertyPanel.classList.remove('expanded');
        isExpanded = false;
      }
    });
  }

  setupControls() {
    // Canvas controls
    this.addListener('canvasSize', 'change', (value) => {
      if (value === 'custom') {
        // Show custom size inputs
        document.getElementById('customSizeGroup').style.display = 'block';
        document.getElementById('customHeightGroup').style.display = 'block';
        
        // Use current or default values
        const width = this.presetConfig.canvas.width || 1920;
        const height = this.presetConfig.canvas.height || 1080;
        document.getElementById('customWidth').value = width;
        document.getElementById('customHeight').value = height;
      } else {
        // Hide custom size inputs
        document.getElementById('customSizeGroup').style.display = 'none';
        document.getElementById('customHeightGroup').style.display = 'none';
        
        // Apply preset size
        const [width, height] = value.split(':').map(Number);
        this.presetConfig.canvas.width = width;
        this.presetConfig.canvas.height = height;
        this.presetConfig.canvas.aspect_ratio = value;
        document.getElementById('canvasInfo').textContent = `${width}x${height}`;
        this.scheduleUpdate();
      }
    });

    // Custom width input
    this.addListener('customWidth', 'input', (value) => {
      const width = parseInt(value) || 1920;
      const height = parseInt(document.getElementById('customHeight').value) || 1080;
      this.presetConfig.canvas.width = width;
      this.presetConfig.canvas.height = height;
      this.presetConfig.canvas.aspect_ratio = `${width}:${height}`;
      document.getElementById('canvasInfo').textContent = `${width}x${height}`;
      this.scheduleUpdate();
    });

    // Custom height input
    this.addListener('customHeight', 'input', (value) => {
      const width = parseInt(document.getElementById('customWidth').value) || 1920;
      const height = parseInt(value) || 1080;
      this.presetConfig.canvas.width = width;
      this.presetConfig.canvas.height = height;
      this.presetConfig.canvas.aspect_ratio = `${width}:${height}`;
      document.getElementById('canvasInfo').textContent = `${width}x${height}`;
      this.scheduleUpdate();
    });

    this.addListener('bgType', 'change', (value) => {
      this.presetConfig.canvas.background_type = value;
      this.toggleBackgroundFields();
      this.scheduleUpdate();
    });

    this.addListener('bgColor', 'input', (value) => {
      this.presetConfig.canvas.background_color = value;
      this.scheduleUpdate();
    });

    // bgUrl input removed - now using Select2 dropdown (bgMedia)

    this.addListener('bgFit', 'change', (value) => {
      this.presetConfig.canvas.background_fit = value;
      this.scheduleUpdate();
    });

    // Intro controls
    this.addListener('introType', 'change', (value) => {
      this.presetConfig.canvas.intro.type = value;
      this.toggleIntroFields();
      this.scheduleUpdate();
    });

    this.addListener('introFit', 'change', (value) => {
      this.presetConfig.canvas.intro.fit = value;
      this.scheduleUpdate();
    });

    this.addRangeListener('introDuration', (value) => {
      this.presetConfig.canvas.intro.duration = parseInt(value);
      this.scheduleUpdate();
    }, 'introDurationVal', (v) => v + 's');

    // Outro controls
    this.addListener('outroType', 'change', (value) => {
      this.presetConfig.canvas.outro.type = value;
      this.toggleOutroFields();
      this.scheduleUpdate();
    });

    this.addListener('outroFit', 'change', (value) => {
      this.presetConfig.canvas.outro.fit = value;
      this.scheduleUpdate();
    });

    this.addRangeListener('outroDuration', (value) => {
      this.presetConfig.canvas.outro.duration = parseInt(value);
      this.scheduleUpdate();
    }, 'outroDurationVal', (v) => v + 's');

    // Question controls
    this.addListener('qFontFamily', 'change', (value) => {
      this.presetConfig.question.font_family = value;
      this.updateFontWarnings();
      this.scheduleUpdate();
    });

    this.addRangeListener('qFontSize', (value) => {
      this.presetConfig.question.font_size = parseInt(value);
      this.scheduleUpdate();
    }, 'qFontSizeVal', (v) => v + 'px');

    this.addListener('qFontColor', 'input', (value) => {
      this.presetConfig.question.font_color = value;
      this.scheduleUpdate();
    });

    this.addRangeListener('qPosX', (value) => {
      this.presetConfig.question.position.x = parseInt(value);
      this.scheduleUpdate();
    }, 'qPosXVal', (v) => v + 'px');

    this.addRangeListener('qPosY', (value) => {
      this.presetConfig.question.position.y = parseInt(value);
      this.scheduleUpdate();
    }, 'qPosYVal', (v) => v + 'px');

    this.addRangeListener('qWidth', (value) => {
      this.presetConfig.question.width = parseInt(value);
      this.presetConfig.question.max_width = parseInt(value); // Keep for backward compatibility
      this.scheduleUpdate();
    }, 'qWidthVal', (v) => v + 'px');

    this.addRangeListener('qHeight', (value) => {
      const height = parseInt(value);
      this.presetConfig.question.height = height;
      this.scheduleUpdate();
    }, 'qHeightVal', (v) => v === '0' ? 'Auto' : v + 'px');

    this.addListener('qTextAlign', 'change', (value) => {
      this.presetConfig.question.text_align = value;
      this.scheduleUpdate();
    });

    // Margin mode toggle
    this.addListener('qMarginMode', 'change', (value) => {
      this.toggleMarginMode();
    });

    // Link all checkbox - no action needed, checked in individual handlers

    // All sides margin
    this.addRangeListener('qMarginAll', (value) => {
      const val = parseFloat(value);
      this.presetConfig.question.margin = { top: val, right: val, bottom: val, left: val };
      
      // Update individual sliders too
      document.getElementById('qMarginTop').value = val;
      document.getElementById('qMarginRight').value = val;
      document.getElementById('qMarginBottom').value = val;
      document.getElementById('qMarginLeft').value = val;
      document.getElementById('qMarginTopVal').textContent = val + 'px';
      document.getElementById('qMarginRightVal').textContent = val + 'px';
      document.getElementById('qMarginBottomVal').textContent = val + 'px';
      document.getElementById('qMarginLeftVal').textContent = val + 'px';
      
      this.scheduleUpdate();
    }, 'qMarginAllVal', (v) => v + 'px');

    // Individual side margin
    this.addRangeListener('qMarginTop', (value) => {
      const val = parseFloat(value);
      if (!this.presetConfig.question.margin || typeof this.presetConfig.question.margin === 'number') {
        this.presetConfig.question.margin = { top: 0, right: 0, bottom: 0, left: 0 };
      }
      this.presetConfig.question.margin.top = val;
      
      if (document.getElementById('qMarginLink').checked) {
        this.syncMarginFromSide(val);
      }
      
      this.scheduleUpdate();
    }, 'qMarginTopVal', (v) => v + 'px');

    this.addRangeListener('qMarginRight', (value) => {
      const val = parseFloat(value);
      if (!this.presetConfig.question.margin || typeof this.presetConfig.question.margin === 'number') {
        this.presetConfig.question.margin = { top: 0, right: 0, bottom: 0, left: 0 };
      }
      this.presetConfig.question.margin.right = val;
      
      if (document.getElementById('qMarginLink').checked) {
        this.syncMarginFromSide(val);
      }
      
      this.scheduleUpdate();
    }, 'qMarginRightVal', (v) => v + 'px');

    this.addRangeListener('qMarginBottom', (value) => {
      const val = parseFloat(value);
      if (!this.presetConfig.question.margin || typeof this.presetConfig.question.margin === 'number') {
        this.presetConfig.question.margin = { top: 0, right: 0, bottom: 0, left: 0 };
      }
      this.presetConfig.question.margin.bottom = val;
      
      if (document.getElementById('qMarginLink').checked) {
        this.syncMarginFromSide(val);
      }
      
      this.scheduleUpdate();
    }, 'qMarginBottomVal', (v) => v + 'px');

    this.addRangeListener('qMarginLeft', (value) => {
      const val = parseFloat(value);
      if (!this.presetConfig.question.margin || typeof this.presetConfig.question.margin === 'number') {
        this.presetConfig.question.margin = { top: 0, right: 0, bottom: 0, left: 0 };
      }
      this.presetConfig.question.margin.left = val;
      
      if (document.getElementById('qMarginLink').checked) {
        this.syncMarginFromSide(val);
      }
      
      this.scheduleUpdate();
    }, 'qMarginLeftVal', (v) => v + 'px');

    this.addListener('qBgType', 'change', (value) => {
      this.presetConfig.question.background_type = value;
      this.toggleQuestionBackgroundFields();
      this.scheduleUpdate();
    });

    this.addListener('qBgColor', 'input', (value) => {
      this.presetConfig.question.background_color = value;
      this.scheduleUpdate();
    });

    this.addListener('qBgFit', 'change', (value) => {
      this.presetConfig.question.background_fit = value;
      this.scheduleUpdate();
    });

    this.addRangeListener('qBorderRadius', (value) => {
      this.presetConfig.question.border_radius = parseFloat(value);
      this.scheduleUpdate();
    }, 'qBorderRadiusVal', (v) => v + '%');

    this.addListener('qNumbering', 'change', (value) => {
      this.presetConfig.question.numbering = value;
      this.toggleQuestionNumberingFields();
      this.scheduleUpdate();
    });

    this.addRangeListener('qStartNumber', (value) => {
      this.presetConfig.question.start_number = parseInt(value);
      this.scheduleUpdate();
    }, 'qStartNumberVal', (v) => v);

    this.addRangeListener('qNumberGap', (value) => {
      this.presetConfig.question.number_gap = parseFloat(value);
      this.scheduleUpdate();
    }, 'qNumberGapVal', (v) => v + '%');

    this.addListener('qShowBorder', 'change', (value) => {
      this.presetConfig.question.show_border_preview = document.getElementById('qShowBorder').checked;
      this.scheduleUpdate();
    });

    // Options controls
    this.addListener('optLayout', 'change', (value) => {
      this.presetConfig.options.layout_format = value;
      this.scheduleUpdate();
    });

    // Option label style
    this.addListener('optLabelStyle', 'change', (value) => {
      console.log('Label style changed to:', value);
      this.presetConfig.options.label_style = value;
      console.log('Config after change:', this.presetConfig.options.label_style);
      this.toggleOptionsLabelSpacing();
      this.scheduleUpdate();
    });

    // Option label spacing
    this.addRangeListener('optLabelSpacing', (value) => {
      this.presetConfig.options.label_spacing = parseInt(value);
      this.scheduleUpdate();
    }, 'optLabelSpacingVal', (v) => v + 'px');

    this.addRangeListener('optPosX', (value) => {
      this.presetConfig.options.container_position.x = parseFloat(value);
      this.scheduleUpdate();
    }, 'optPosXVal', (v) => Math.round(v) + 'px');

    this.addRangeListener('optPosY', (value) => {
      this.presetConfig.options.container_position.y = parseFloat(value);
      this.scheduleUpdate();
    }, 'optPosYVal', (v) => Math.round(v) + 'px');

    this.addRangeListener('optWidth', (value) => {
      this.presetConfig.options.option_width = parseInt(value);
      this.scheduleUpdate();
    }, 'optWidthVal', (v) => v + 'px');

    this.addRangeListener('optHeight', (value) => {
      this.presetConfig.options.option_height = parseInt(value);
      this.scheduleUpdate();
    }, 'optHeightVal', (v) => v + 'px');

    this.addRangeListener('optHSpacing', (value) => {
      this.presetConfig.options.spacing.horizontal = parseInt(value);
      this.scheduleUpdate();
    }, 'optHSpacingVal', (v) => v + 'px');

    this.addRangeListener('optVSpacing', (value) => {
      this.presetConfig.options.spacing.vertical = parseInt(value);
      this.scheduleUpdate();
    }, 'optVSpacingVal', (v) => v + 'px');

    this.addRangeListener('optBorderRadius', (value) => {
      this.presetConfig.options.border_radius = parseFloat(value);
      this.scheduleUpdate();
    }, 'optBorderRadiusVal', (v) => v + '%');

    this.addListener('optShowBorder', 'change', (value) => {
      this.presetConfig.options.show_border_preview = document.getElementById('optShowBorder').checked;
      this.scheduleUpdate();
    });

    // Options margin mode toggle
    this.addListener('optMarginMode', 'change', (value) => {
      this.toggleOptionsMarginMode();
    });

    // All sides margin
    this.addRangeListener('optMarginAll', (value) => {
      const val = parseFloat(value);
      this.presetConfig.options.margin = { top: val, right: val, bottom: val, left: val };
      
      document.getElementById('optMarginTop').value = val;
      document.getElementById('optMarginRight').value = val;
      document.getElementById('optMarginBottom').value = val;
      document.getElementById('optMarginLeft').value = val;
      document.getElementById('optMarginTopVal').textContent = val + '%';
      document.getElementById('optMarginRightVal').textContent = val + '%';
      document.getElementById('optMarginBottomVal').textContent = val + '%';
      document.getElementById('optMarginLeftVal').textContent = val + '%';
      
      this.scheduleUpdate();
    }, 'optMarginAllVal', (v) => v + '%');

    // Individual side margin
    this.addRangeListener('optMarginTop', (value) => {
      const val = parseFloat(value);
      if (!this.presetConfig.options.margin || typeof this.presetConfig.options.margin === 'number') {
        this.presetConfig.options.margin = { top: 0, right: 0, bottom: 0, left: 0 };
      }
      this.presetConfig.options.margin.top = val;
      
      if (document.getElementById('optMarginLink').checked) {
        this.syncOptionsMarginFromSide(val);
      }
      
      this.scheduleUpdate();
    }, 'optMarginTopVal', (v) => v + '%');

    this.addRangeListener('optMarginRight', (value) => {
      const val = parseFloat(value);
      if (!this.presetConfig.options.margin || typeof this.presetConfig.options.margin === 'number') {
        this.presetConfig.options.margin = { top: 0, right: 0, bottom: 0, left: 0 };
      }
      this.presetConfig.options.margin.right = val;
      
      if (document.getElementById('optMarginLink').checked) {
        this.syncOptionsMarginFromSide(val);
      }
      
      this.scheduleUpdate();
    }, 'optMarginRightVal', (v) => v + '%');

    this.addRangeListener('optMarginBottom', (value) => {
      const val = parseFloat(value);
      if (!this.presetConfig.options.margin || typeof this.presetConfig.options.margin === 'number') {
        this.presetConfig.options.margin = { top: 0, right: 0, bottom: 0, left: 0 };
      }
      this.presetConfig.options.margin.bottom = val;
      
      if (document.getElementById('optMarginLink').checked) {
        this.syncOptionsMarginFromSide(val);
      }
      
      this.scheduleUpdate();
    }, 'optMarginBottomVal', (v) => v + '%');

    this.addRangeListener('optMarginLeft', (value) => {
      const val = parseFloat(value);
      if (!this.presetConfig.options.margin || typeof this.presetConfig.options.margin === 'number') {
        this.presetConfig.options.margin = { top: 0, right: 0, bottom: 0, left: 0 };
      }
      this.presetConfig.options.margin.left = val;
      
      if (document.getElementById('optMarginLink').checked) {
        this.syncOptionsMarginFromSide(val);
      }
      
      this.scheduleUpdate();
    }, 'optMarginLeftVal', (v) => v + '%');

    this.addListener('optFontFamily', 'change', (value) => {
      this.presetConfig.options.font_family = value;
      this.updateFontWarnings();
      this.scheduleUpdate();
    });

    this.addRangeListener('optFontSize', (value) => {
      this.presetConfig.options.font_size = parseInt(value);
      this.scheduleUpdate();
    }, 'optFontSizeVal', (v) => v + 'px');

    this.addListener('optFontColor', 'input', (value) => {
      this.presetConfig.options.font_color = value;
      this.scheduleUpdate();
    });

    this.addListener('optBgType', 'change', (value) => {
      this.presetConfig.options.background_type = value;
      this.toggleOptionBackgroundFields();
      this.scheduleUpdate();
    });

    this.addListener('optBgColor', 'input', (value) => {
      this.presetConfig.options.background_color = value;
      this.scheduleUpdate();
    });

    // optBgUrl input removed - now using Select2 dropdown (optBgMedia)

    this.addListener('optCorrectFontColor', 'input', (value) => {
      this.presetConfig.options.correct_answer_font_color = value;
      this.scheduleUpdate();
    });

    this.addListener('optCorrectBgType', 'change', (value) => {
      this.presetConfig.options.correct_answer_background_type = value;
      this.toggleOptionBackgroundFields();
      this.scheduleUpdate();
    });

    this.addListener('optCorrectBgColor', 'input', (value) => {
      this.presetConfig.options.correct_answer_background_color = value;
      this.scheduleUpdate();
    });

    // optCorrectBgUrl input removed - now using Select2 dropdown (optCorrectBgMedia)

    // Timer controls
    this.addRangeListener('timerDuration', (value) => {
      this.presetConfig.timer.duration = parseInt(value);
      this.scheduleUpdate();
    }, 'timerDurationVal', (v) => v + ' seconds');

    this.addRangeListener('timerPosX', (value) => {
      this.presetConfig.timer.position.x = parseFloat(value);
      this.scheduleUpdate();
    }, 'timerPosXVal', (v) => Math.round(v) + 'px');

    this.addRangeListener('timerPosY', (value) => {
      this.presetConfig.timer.position.y = parseFloat(value);
      this.scheduleUpdate();
    }, 'timerPosYVal', (v) => Math.round(v) + 'px');

    this.addRangeListener('timerWidth', (value) => {
      this.presetConfig.timer.width = parseInt(value);
      this.scheduleUpdate();
    }, 'timerWidthVal', (v) => v + 'px');

    this.addRangeListener('timerHeight', (value) => {
      this.presetConfig.timer.height = parseInt(value);
      this.scheduleUpdate();
    }, 'timerHeightVal', (v) => v + 'px');

    this.addRangeListener('timerFontSize', (value) => {
      this.presetConfig.timer.font_size = parseInt(value);
      this.scheduleUpdate();
    }, 'timerFontSizeVal', (v) => v + 'px');

    this.addListener('timerFontColor', 'input', (value) => {
      this.presetConfig.timer.font_color = value;
      this.scheduleUpdate();
    });

    this.addListener('timerBgType', 'change', (value) => {
      this.presetConfig.timer.background_type = value;
      this.toggleTimerBackgroundFields();
      this.scheduleUpdate();
    });

    this.addListener('timerBgColor', 'input', (value) => {
      this.presetConfig.timer.background_color = value;
      this.scheduleUpdate();
    });

    // timerBgUrl input removed - now using Select2 dropdown (timerBgMedia)

    this.addRangeListener('timerBorderRadius', (value) => {
      this.presetConfig.timer.border_radius = parseInt(value);
      this.scheduleUpdate();
    }, 'timerBorderRadiusVal', (v) => v + 'px');

    // Explanation controls
    this.addListener('explEnabled', 'change', (value) => {
      this.presetConfig.explanation.enabled = document.getElementById('explEnabled').checked;
      this.scheduleUpdate();
    });

    this.addRangeListener('explDuration', (value) => {
      this.presetConfig.explanation.duration = parseFloat(value);
      this.scheduleUpdate();
    }, 'explDurationVal', (v) => v);

    this.addListener('explBgType', 'change', (value) => {
      this.presetConfig.explanation.background_type = value;
      this.toggleExplanationBackgroundFields();
      this.scheduleUpdate();
    });

    this.addListener('explBgColor', 'input', (value) => {
      this.presetConfig.explanation.background_color = value;
      this.scheduleUpdate();
    });

    this.addListener('explBgImageFit', 'change', (value) => {
      this.presetConfig.explanation.background_fit = value;
      this.scheduleUpdate();
    });

    this.addListener('explFontFamily', 'change', (value) => {
      this.presetConfig.explanation.font_family = value;
      this.scheduleUpdate();
    });

    this.addRangeListener('explFontSize', (value) => {
      this.presetConfig.explanation.font_size = parseInt(value);
      this.scheduleUpdate();
    }, 'explFontSizeVal', (v) => v + 'px');

    this.addListener('explFontColor', 'input', (value) => {
      this.presetConfig.explanation.font_color = value;
      this.scheduleUpdate();
    });

    this.addListener('explTextAlign', 'change', (value) => {
      this.presetConfig.explanation.text_align = value;
      this.scheduleUpdate();
    });

    this.addRangeListener('explWidth', (value) => {
      this.presetConfig.explanation.width = parseInt(value);
      this.scheduleUpdate();
    }, 'explWidthVal', (v) => v + 'px');

    this.addRangeListener('explHeight', (value) => {
      this.presetConfig.explanation.height = parseInt(value);
      this.scheduleUpdate();
    }, 'explHeightVal', (v) => v === '0' ? 'Auto' : v + 'px');

    this.addListener('explImageEnabled', 'change', (value) => {
      this.presetConfig.explanation.image_enabled = document.getElementById('explImageEnabled').checked;
      this.toggleExplanationImageFields();
      this.scheduleUpdate();
    });

    this.addRangeListener('explImageWidth', (value) => {
      this.presetConfig.explanation.image_width = parseInt(value);
      this.scheduleUpdate();
    }, 'explImageWidthVal', (v) => v + 'px');

    this.addRangeListener('explImageHeight', (value) => {
      this.presetConfig.explanation.image_height = parseInt(value);
      this.scheduleUpdate();
    }, 'explImageHeightVal', (v) => v + 'px');

    this.addListener('explImageFit', 'change', (value) => {
      this.presetConfig.explanation.image_fit = value;
      this.scheduleUpdate();
    });

    this.addListener('explImagePosition', 'change', (value) => {
      this.presetConfig.explanation.image_position = value;
      this.scheduleUpdate();
    });

    const addOverlayBtn = document.getElementById('addOverlayBtn');
    if (addOverlayBtn) {
      addOverlayBtn.addEventListener('click', () => {
        this.addOverlayItem();
      });
    }

    // Audio Narration Event Listeners
    this.addListener('audioPlayFirstQuestion', 'change', (value) => {
      this.presetConfig.audio.play_first_question = document.getElementById('audioPlayFirstQuestion').checked;
    });

    this.addListener('audioPlayNextQuestion', 'change', (value) => {
      this.presetConfig.audio.play_next_question = document.getElementById('audioPlayNextQuestion').checked;
    });

    this.addListener('audioPlayLastQuestion', 'change', (value) => {
      this.presetConfig.audio.play_last_question = document.getElementById('audioPlayLastQuestion').checked;
    });

    this.addListener('audioPlayOptions', 'change', (value) => {
      this.presetConfig.audio.play_options = document.getElementById('audioPlayOptions').checked;
    });

    this.addRangeListener('audioQuestionGap', (value) => {
      this.presetConfig.audio.question_gap = parseFloat(value);
    }, 'audioQuestionGapVal', (v) => v + 's');

    this.addListener('audioOptionRevealMode', 'change', (value) => {
      this.presetConfig.audio.option_reveal_mode = value;
      // Show/hide option delay based on mode
      const optionDelayGroup = document.getElementById('audioOptionDelayGroup');
      if (value === 'sequential') {
        optionDelayGroup.style.display = 'block';
      } else {
        optionDelayGroup.style.display = 'none';
      }
    });

    this.addRangeListener('audioOptionDelay', (value) => {
      this.presetConfig.audio.option_delay = parseFloat(value);
    }, 'audioOptionDelayVal', (v) => v + 's');

    this.addRangeListener('audioQToOptionsGap', (value) => {
      this.presetConfig.audio.question_to_options_gap = parseFloat(value);
    }, 'audioQToOptionsGapVal', (v) => v + 's');

    // Save button - saves current preset
    document.getElementById('saveBtn').addEventListener('click', () => this.savePreset());

    // Dropdown toggle
    const dropdownBtn = document.getElementById('saveDropdownBtn');
    const dropdownMenu = document.getElementById('saveDropdownMenu');
    
    if (dropdownBtn && dropdownMenu) {
      dropdownBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        dropdownMenu.classList.toggle('show');
      });

      // Close dropdown when clicking outside
      document.addEventListener('click', (e) => {
        if (!e.target.closest('.save-preset-group')) {
          dropdownMenu.classList.remove('show');
        }
      });

      // Save as copy button
      const saveAsCopyBtn = document.getElementById('saveAsCopyBtn');
      if (saveAsCopyBtn) {
        saveAsCopyBtn.addEventListener('click', (e) => {
          e.preventDefault();
          dropdownMenu.classList.remove('show');
          this.showSaveAsModal();
        });
      }

      // Delete preset button
      const deletePresetBtn = document.getElementById('deletePresetBtn');
      if (deletePresetBtn) {
        deletePresetBtn.addEventListener('click', (e) => {
          e.preventDefault();
          dropdownMenu.classList.remove('show');
          this.showDeleteModal();
        });
      }
    }

    // Modal controls
    const saveAsModalClose = document.getElementById('saveAsModalClose');
    const saveAsCancelBtn = document.getElementById('saveAsCancelBtn');
    const saveAsForm = document.getElementById('saveAsForm');
    
    if (saveAsModalClose) {
      saveAsModalClose.addEventListener('click', () => this.hideSaveAsModal());
    }
    
    if (saveAsCancelBtn) {
      saveAsCancelBtn.addEventListener('click', () => this.hideSaveAsModal());
    }
    
    if (saveAsForm) {
      saveAsForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.savePresetAsCopy();
      });
    }

    // Delete modal controls
    const deleteModalClose = document.getElementById('deleteModalClose');
    const deleteCancelBtn = document.getElementById('deleteCancelBtn');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    
    if (deleteModalClose) {
      deleteModalClose.addEventListener('click', () => this.hideDeleteModal());
    }
    
    if (deleteCancelBtn) {
      deleteCancelBtn.addEventListener('click', () => this.hideDeleteModal());
    }
    
    if (confirmDeleteBtn) {
      confirmDeleteBtn.addEventListener('click', () => this.deletePreset());
    }
  }

  setupPreviewControls() {
    document.getElementById('playBtn').addEventListener('click', () => {
      if (this.renderer) {
        // Switch to animation mode and play for 3 QA cycles max
        this.renderer.startAnimation(3);
        this.startTimeUpdate();
        this.updateStatus('Playing animation...', 'loading');
      }
    });

    document.getElementById('pauseBtn').addEventListener('click', () => {
      if (this.renderer) {
        this.renderer.stopAnimation();
        this.stopTimeUpdate();
        // Return to static preview mode
        this.renderer.enableStaticPreview();
        this.updateStatus('Preview paused (Static view)', 'success');
      }
    });

    document.getElementById('seekStartBtn').addEventListener('click', () => {
      if (this.renderer) {
        this.renderer.seekTo(0);
        this.updateTimeDisplay();
      }
    });

    document.getElementById('seekEndBtn').addEventListener('click', () => {
      if (this.renderer) {
        const duration = this.presetConfig.timer.duration || 5;
        this.renderer.seekTo(duration + 2);
        this.updateTimeDisplay();
      }
    });

    const fullscreenBtn = document.getElementById('fullscreenBtn');
    if (fullscreenBtn) {
      fullscreenBtn.addEventListener('click', () => {
        const previewPanel = document.querySelector('.preview-panel');
        if (!previewPanel) return;

        if (!document.fullscreenElement) {
          previewPanel.requestFullscreen?.().catch(() => {});
          previewPanel.classList.add('is-fullscreen');
          fullscreenBtn.textContent = '⤢';
        } else {
          document.exitFullscreen?.();
          previewPanel.classList.remove('is-fullscreen');
          fullscreenBtn.textContent = '⛶';
        }
      });

      document.addEventListener('fullscreenchange', () => {
        const previewPanel = document.querySelector('.preview-panel');
        if (!previewPanel) return;
        if (!document.fullscreenElement) {
          previewPanel.classList.remove('is-fullscreen');
          fullscreenBtn.textContent = '⛶';
        }
      });
    }
  }

  addListener(id, event, callback) {
    const element = document.getElementById(id);
    if (element) {
      element.addEventListener(event, (e) => callback(e.target.value));
    }
  }

  addRangeListener(id, callback, displayId, formatter) {
    const element = document.getElementById(id);
    const display = document.getElementById(displayId);

    if (element) {
      element.addEventListener('input', (e) => {
        const value = e.target.value;
        if (display && formatter) {
          display.textContent = formatter(value);
        }
        callback(value);
      });
    }
  }

  updateAllValueDisplays() {
    // Update all range display values
    const ranges = [
      { id: 'qFontSize', displayId: 'qFontSizeVal', formatter: (v) => v + 'px' },
      { id: 'qPosX', displayId: 'qPosXVal', formatter: (v) => v + 'px' },
      { id: 'qPosY', displayId: 'qPosYVal', formatter: (v) => v + 'px' },
      { id: 'qWidth', displayId: 'qWidthVal', formatter: (v) => v + 'px' },
      { id: 'qHeight', displayId: 'qHeightVal', formatter: (v) => v === '0' ? 'Auto' : v + 'px' },
      { id: 'qMarginAll', displayId: 'qMarginAllVal', formatter: (v) => v + '%' },
      { id: 'qMarginTop', displayId: 'qMarginTopVal', formatter: (v) => v + '%' },
      { id: 'qMarginRight', displayId: 'qMarginRightVal', formatter: (v) => v + '%' },
      { id: 'qMarginBottom', displayId: 'qMarginBottomVal', formatter: (v) => v + '%' },
      { id: 'qMarginLeft', displayId: 'qMarginLeftVal', formatter: (v) => v + '%' },
      { id: 'qBorderRadius', displayId: 'qBorderRadiusVal', formatter: (v) => v + '%' },
      { id: 'qStartNumber', displayId: 'qStartNumberVal', formatter: (v) => v },
      { id: 'qNumberGap', displayId: 'qNumberGapVal', formatter: (v) => v + '%' },
      { id: 'optPosX', displayId: 'optPosXVal', formatter: (v) => Math.round(v) + 'px' },
      { id: 'optPosY', displayId: 'optPosYVal', formatter: (v) => Math.round(v) + 'px' },
      { id: 'optWidth', displayId: 'optWidthVal', formatter: (v) => v + 'px' },
      { id: 'optHeight', displayId: 'optHeightVal', formatter: (v) => v + 'px' },
      { id: 'optHSpacing', displayId: 'optHSpacingVal', formatter: (v) => v + 'px' },
      { id: 'optVSpacing', displayId: 'optVSpacingVal', formatter: (v) => v + 'px' },
      { id: 'optBorderRadius', displayId: 'optBorderRadiusVal', formatter: (v) => v + '%' },
      { id: 'optLabelSpacing', displayId: 'optLabelSpacingVal', formatter: (v) => v + 'px' },
      { id: 'optMarginAll', displayId: 'optMarginAllVal', formatter: (v) => v + '%' },
      { id: 'optMarginTop', displayId: 'optMarginTopVal', formatter: (v) => v + '%' },
      { id: 'optMarginRight', displayId: 'optMarginRightVal', formatter: (v) => v + '%' },
      { id: 'optMarginBottom', displayId: 'optMarginBottomVal', formatter: (v) => v + '%' },
      { id: 'optMarginLeft', displayId: 'optMarginLeftVal', formatter: (v) => v + '%' },
      { id: 'optFontSize', displayId: 'optFontSizeVal', formatter: (v) => v + 'px' },
      { id: 'timerDuration', displayId: 'timerDurationVal', formatter: (v) => v + ' seconds' },
      { id: 'timerPosX', displayId: 'timerPosXVal', formatter: (v) => Math.round(v) + 'px' },
      { id: 'timerPosY', displayId: 'timerPosYVal', formatter: (v) => Math.round(v) + 'px' },
      { id: 'timerWidth', displayId: 'timerWidthVal', formatter: (v) => v + 'px' },
      { id: 'timerHeight', displayId: 'timerHeightVal', formatter: (v) => v + 'px' },
      { id: 'timerFontSize', displayId: 'timerFontSizeVal', formatter: (v) => v + 'px' },
      { id: 'explWidth', displayId: 'explWidthVal', formatter: (v) => v + 'px' },
      { id: 'explHeight', displayId: 'explHeightVal', formatter: (v) => v === '0' ? 'Auto' : v + 'px' },
      { id: 'timerBorderRadius', displayId: 'timerBorderRadiusVal', formatter: (v) => v + 'px' }
    ];

    ranges.forEach(({ id, displayId, formatter }) => {
      const element = document.getElementById(id);
      const display = document.getElementById(displayId);
      if (element && display && formatter) {
        display.textContent = formatter(element.value);
      }
    });
  }

  initializeMediaSelects() {
    // Initialize custom dropdowns
    const selects = [
      { id: 'bgMedia', type: 'bgType', config: this.presetConfig.canvas, urlKey: 'background_url' },
      { id: 'introMedia', type: 'introType', config: this.presetConfig.canvas.intro, urlKey: 'url' },
      { id: 'outroMedia', type: 'outroType', config: this.presetConfig.canvas.outro, urlKey: 'url' },
      { id: 'qBgMedia', type: 'qBgType', config: this.presetConfig.question, urlKey: 'background_url' },
      { id: 'optBgMedia', type: 'optBgType', config: this.presetConfig.options, urlKey: 'background_url' },
      { id: 'optCorrectBgMedia', type: 'optCorrectBgType', config: this.presetConfig.options, urlKey: 'correct_answer_background_url' },
      { id: 'timerBgMedia', type: 'timerBgType', config: this.presetConfig.timer, urlKey: 'background_url' }
    ];

    selects.forEach(({ id, type, config, urlKey }) => {
      const selectEl = document.getElementById(id);
      
      // Initialize custom select
      this.initCustomSelect(selectEl, config, urlKey);

      // Populate options based on type
      const selectedType = document.getElementById(type).value;
      this.populateMediaSelect(id, selectedType);

      // Set current value if exists
      if (config && config[urlKey]) {
        this.setCustomSelectValue(selectEl, config[urlKey]);
      }
    });
  }

  initCustomSelect(selectEl, config, urlKey) {
    if (!selectEl) return;

    // Avoid double-binding listeners when populateForm() re-runs
    if (selectEl.dataset.customSelectInitialized === 'true') {
      // Still update the backing data reference
      selectEl.customSelectData = { config, urlKey, allOptions: selectEl.customSelectData?.allOptions || [] };
      return;
    }

    const trigger = selectEl.querySelector('.custom-select-trigger');
    const dropdown = selectEl.querySelector('.custom-select-dropdown');
    const searchInput = selectEl.querySelector('.custom-select-search');
    const optionsContainer = selectEl.querySelector('.custom-select-options');
    const valueDisplay = selectEl.querySelector('.custom-select-value');

    if (!trigger || !dropdown || !searchInput || !optionsContainer || !valueDisplay) {
      console.warn('Custom select markup incomplete for', selectEl.id);
      return;
    }

    // Store reference
    selectEl.customSelectData = { config, urlKey, allOptions: [] };
    selectEl.dataset.customSelectInitialized = 'true';

    if (selectEl.tagName === 'SELECT') {
      this.buildCustomSelectFromNative(selectEl);
    }

    // Toggle dropdown
    trigger.addEventListener('click', (e) => {
      e.stopPropagation();

      // Close other dropdowns
      document.querySelectorAll('.custom-select.open').forEach(el => {
        if (el !== selectEl) {
          el.classList.remove('open');
          this.resetCustomSelectDropdown(el);
        }
      });

      selectEl.classList.toggle('open');

      if (selectEl.classList.contains('open')) {
        searchInput.value = '';
        searchInput.focus();
        this.filterCustomSelectOptions(selectEl, '');
        this.positionCustomSelectDropdown(selectEl);
      } else {
        this.resetCustomSelectDropdown(selectEl);
      }
    });

    // Search functionality
    searchInput.addEventListener('input', (e) => {
      this.filterCustomSelectOptions(selectEl, e.target.value);
    });

    // Close when clicking outside
    document.addEventListener('click', (e) => {
      if (!selectEl.contains(e.target)) {
        selectEl.classList.remove('open');
        this.resetCustomSelectDropdown(selectEl);
      }
    });

    // Handle option selection (delegated)
    optionsContainer.addEventListener('click', (e) => {
      const option = e.target.closest('.custom-select-option');
      if (option && !option.classList.contains('custom-select-no-results')) {
        const value = option.dataset.value;
        const textSpan = option.querySelector('span:not(.custom-select-option-icon)');
        const text = textSpan ? textSpan.textContent : option.textContent;

        // Update display
        valueDisplay.textContent = text;
        valueDisplay.classList.remove('placeholder');

        // Update config
        if (config && urlKey) {
          config[urlKey] = value;
        }

        if (selectEl.dataset.selectTarget) {
          const nativeSelect = document.getElementById(selectEl.dataset.selectTarget);
          if (nativeSelect) {
            nativeSelect.value = value;
            nativeSelect.dispatchEvent(new Event('change', { bubbles: true }));
          }
        }

        // Update selected state
        optionsContainer.querySelectorAll('.custom-select-option').forEach(opt => {
          opt.classList.remove('selected');
        });
        option.classList.add('selected');

        selectEl.dispatchEvent(new CustomEvent('custom-select-change', { detail: { value, text } }));

        // Close dropdown
        selectEl.classList.remove('open');
        this.resetCustomSelectDropdown(selectEl);

        // Trigger update
        this.scheduleUpdate();
      }
    });
  }

  positionCustomSelectDropdown(selectEl) {
    const dropdown = selectEl?.querySelector('.custom-select-dropdown');
    const trigger = selectEl?.querySelector('.custom-select-trigger');
    if (!dropdown || !trigger) return;

    // Persist original inline styles once so we can restore them later
    if (!dropdown.dataset.originalStyle) {
      dropdown.dataset.originalStyle = dropdown.getAttribute('style') || '';
    }

    const rect = trigger.getBoundingClientRect();

    // Render as an overlay so it's not clipped by overflow:hidden containers
    dropdown.style.position = 'fixed';
    dropdown.style.left = `${Math.round(rect.left)}px`;
    dropdown.style.top = `${Math.round(rect.bottom)}px`;
    dropdown.style.width = `${Math.round(rect.width)}px`;
    dropdown.style.right = 'auto';
    dropdown.style.zIndex = '2000';

    // Keep within viewport vertically
    const maxHeight = 300;
    const margin = 8;
    const availableBelow = window.innerHeight - rect.bottom - margin;
    const availableAbove = rect.top - margin;

    dropdown.style.maxHeight = `${Math.max(150, Math.min(maxHeight, Math.max(availableBelow, availableAbove)))}px`;

    if (availableBelow < 150 && availableAbove > availableBelow) {
      // Open upwards
      dropdown.style.top = 'auto';
      dropdown.style.bottom = `${Math.round(window.innerHeight - rect.top)}px`;
      dropdown.style.borderTop = '1px solid #4CAF50';
      dropdown.style.borderBottom = 'none';
      dropdown.style.borderTopLeftRadius = '6px';
      dropdown.style.borderTopRightRadius = '6px';
      dropdown.style.borderBottomLeftRadius = '0';
      dropdown.style.borderBottomRightRadius = '0';
    } else {
      // Open downwards (default)
      dropdown.style.bottom = 'auto';
      dropdown.style.borderTop = 'none';
      dropdown.style.borderBottom = '1px solid #4CAF50';
      dropdown.style.borderTopLeftRadius = '0';
      dropdown.style.borderTopRightRadius = '0';
      dropdown.style.borderBottomLeftRadius = '6px';
      dropdown.style.borderBottomRightRadius = '6px';
    }
  }

  resetCustomSelectDropdown(selectEl) {
    const dropdown = selectEl?.querySelector('.custom-select-dropdown');
    if (!dropdown) return;

    // Restore any previous inline styles
    if (dropdown.dataset.originalStyle !== undefined) {
      if (dropdown.dataset.originalStyle) {
        dropdown.setAttribute('style', dropdown.dataset.originalStyle);
      } else {
        dropdown.removeAttribute('style');
      }
      delete dropdown.dataset.originalStyle;
    } else {
      // Fallback: just clear the properties we touch
      dropdown.style.position = '';
      dropdown.style.left = '';
      dropdown.style.top = '';
      dropdown.style.bottom = '';
      dropdown.style.width = '';
      dropdown.style.right = '';
      dropdown.style.zIndex = '';
      dropdown.style.maxHeight = '';
      dropdown.style.borderTop = '';
      dropdown.style.borderBottom = '';
      dropdown.style.borderTopLeftRadius = '';
      dropdown.style.borderTopRightRadius = '';
      dropdown.style.borderBottomLeftRadius = '';
      dropdown.style.borderBottomRightRadius = '';
    }
  }

  repositionOpenCustomSelectDropdowns() {
    document.querySelectorAll('.custom-select.open').forEach(el => {
      this.positionCustomSelectDropdown(el);
    });
  }

  setCustomSelectValue(selectEl, value) {
    const valueDisplay = selectEl.querySelector('.custom-select-value');
    const optionsContainer = selectEl.querySelector('.custom-select-options');
    
    // Find matching option
    const options = selectEl.customSelectData?.allOptions || [];
    const match = options.find(opt => opt.value === value);
    
    if (match) {
      valueDisplay.textContent = match.text;
      valueDisplay.classList.remove('placeholder');
      
      // Update selected state
      optionsContainer.querySelectorAll('.custom-select-option').forEach(opt => {
        if (opt.dataset.value === value) {
          opt.classList.add('selected');
        } else {
          opt.classList.remove('selected');
        }
      });
    }
  }

  filterCustomSelectOptions(selectEl, searchTerm) {
    if (!selectEl || !selectEl.customSelectData) {
      return;
    }

    const optionsContainer = selectEl.querySelector('.custom-select-options');
    if (!optionsContainer) {
      return;
    }

    const allOptions = selectEl.customSelectData?.allOptions || [];

    const filtered = allOptions.filter(opt =>
      opt.text.toLowerCase().includes(searchTerm.toLowerCase())
    );

    optionsContainer.innerHTML = '';

    if (filtered.length === 0) {
      optionsContainer.innerHTML = '<div class="custom-select-no-results">No options found</div>';
    } else {
      filtered.forEach(opt => {
        const optionEl = document.createElement('div');
        optionEl.className = 'custom-select-option';
        optionEl.dataset.value = opt.value;
        optionEl.innerHTML = `
          <span class="custom-select-option-icon">${opt.icon ?? ''}</span>
          <span>${opt.text}</span>
        `;

        const currentValue = selectEl.customSelectData.config?.[selectEl.customSelectData.urlKey] ?? selectEl.value;
        if (opt.value === currentValue) {
          optionEl.classList.add('selected');
        }

        optionsContainer.appendChild(optionEl);
      });
    }
  }

  buildCustomSelectFromNative(customEl, sourceSelect) {
    if (!customEl) return;
    const selectEl = sourceSelect || (customEl.dataset?.selectTarget ? document.getElementById(customEl.dataset.selectTarget) : null);
    if (!selectEl) return;

    const options = Array.from(selectEl.options).map(option => ({
      value: option.value,
      text: option.textContent,
      icon: '',
      disabled: option.disabled
    }));

    customEl.customSelectData = customEl.customSelectData || { config: null, urlKey: null, allOptions: [] };
    customEl.customSelectData.allOptions = options;
    this.filterCustomSelectOptions(customEl, '');
    this.setCustomSelectValue(customEl, selectEl.value || options.find(opt => !opt.disabled)?.value);
  }

  initializeNativeSelectSearches() {
    const selectMappings = [
      { selectId: 'canvasSize', customId: 'canvasSizeSelect', placeholder: 'Select size...' },
      { selectId: 'bgType', customId: 'bgTypeSelect', placeholder: 'Select background type...' },
      { selectId: 'bgFit', customId: 'bgFitSelect', placeholder: 'Select fit...' },
      { selectId: 'introType', customId: 'introTypeSelect', placeholder: 'Select intro type...' },
      { selectId: 'introFit', customId: 'introFitSelect', placeholder: 'Select intro fit...' },
      { selectId: 'outroType', customId: 'outroTypeSelect', placeholder: 'Select outro type...' },
      { selectId: 'outroFit', customId: 'outroFitSelect', placeholder: 'Select outro fit...' },
      { selectId: 'qFontFamily', customId: 'qFontFamilySelect', placeholder: 'Select font...' },
      { selectId: 'optFontFamily', customId: 'optFontFamilySelect', placeholder: 'Select font...' },
      { selectId: 'explFontFamily', customId: 'explFontFamilySelect', placeholder: 'Select font...' }
    ];

    selectMappings.forEach(({ selectId, customId, placeholder }) => {
      const selectEl = document.getElementById(selectId);
      const customEl = document.getElementById(customId);

      if (!selectEl || !customEl) return;

      selectEl.classList.add('native-select-hidden');
      customEl.dataset.selectTarget = selectId;
      this.initCustomSelect(customEl, null, null);
      this.buildCustomSelectFromNative(customEl, selectEl);

      const valueDisplay = customEl.querySelector('.custom-select-value');
      if (valueDisplay && !selectEl.value) {
        valueDisplay.textContent = placeholder;
        valueDisplay.classList.add('placeholder');
      }

      customEl.addEventListener('custom-select-change', (e) => {
        const { value } = e.detail || {};
        if (!value) return;
        selectEl.value = value;
        selectEl.dispatchEvent(new Event('change', { bubbles: true }));
      });

      selectEl.addEventListener('change', () => {
        this.buildCustomSelectFromNative(customEl, selectEl);
        this.setCustomSelectValue(customEl, selectEl.value);
      });
    });
  }

  syncNativeSelectSearches() {
    document.querySelectorAll('.custom-select[data-select-target], .custom-select[data-select-target]').forEach(customEl => {
      const selectId = customEl.dataset.selectTarget;
      if (!selectId) return;
      const selectEl = document.getElementById(selectId);
      if (!selectEl) return;
      this.buildCustomSelectFromNative(customEl, selectEl);
      this.setCustomSelectValue(customEl, selectEl.value);
    });
  }

  populateMediaSelect(selectId, mediaType) {
    const selectEl = document.getElementById(selectId);

    if (!selectEl) {
      return;
    }

    // Get media based on type
    let mediaList = [];
    if (mediaType === 'image') {
      mediaList = this.mediaLibrary.images;
    } else if (mediaType === 'video') {
      mediaList = this.mediaLibrary.videos;
    }

    const options = mediaList.map(media => ({
      value: media.url,
      text: media.label || media.original_name || media.filename,
      icon: media.type === 'image' ? '🖼️' : '🎥'
    }));

    if (selectEl.customSelectData) {
      // Store options
      selectEl.customSelectData.allOptions = options;

      // Render options
      this.filterCustomSelectOptions(selectEl, '');
      return;
    }

    if (selectEl.tagName === 'SELECT') {
      this.populateNativeSelectOptions(selectEl, options);
    }
  }

  populateNativeSelectOptions(selectEl, options) {
    if (!selectEl) return;
    const currentValue = selectEl.value;
    selectEl.innerHTML = '';

    options.forEach(opt => {
      const option = document.createElement('option');
      option.value = opt.value;
      option.textContent = opt.text;
      if (opt.disabled) option.disabled = true;
      selectEl.appendChild(option);
    });

    if (currentValue) {
      selectEl.value = currentValue;
    }

    this.syncNativeSelectSearches();
  }

  toggleBackgroundFields() {
    const bgType = document.getElementById('bgType').value;
    const urlGroup = document.getElementById('bgUrlGroup');
    const colorGroup = document.getElementById('bgColorGroup');
    const fitGroup = document.getElementById('bgFitGroup');

    if (bgType === 'color') {
      colorGroup.style.display = 'block';
      urlGroup.style.display = 'none';
      fitGroup.style.display = 'none';
    } else {
      colorGroup.style.display = 'none';
      urlGroup.style.display = 'block';
      fitGroup.style.display = 'block';
      
      // Update media dropdown for new type
      this.populateMediaSelect('bgMedia', bgType);
    }
  }

  toggleIntroFields() {
    const type = document.getElementById('introType')?.value || 'none';
    const mediaGroup = document.getElementById('introMediaGroup');
    const durationGroup = document.getElementById('introDurationGroup');
    const fitGroup = document.getElementById('introFitGroup');

    if (!mediaGroup || !durationGroup || !fitGroup) return;

    if (type === 'none') {
      mediaGroup.style.display = 'none';
      durationGroup.style.display = 'none';
      fitGroup.style.display = 'none';
    } else {
      mediaGroup.style.display = 'block';
      fitGroup.style.display = 'block';
      durationGroup.style.display = (type === 'image') ? 'block' : 'none';
      this.populateMediaSelect('introMedia', type);
    }
  }

  toggleOutroFields() {
    const type = document.getElementById('outroType')?.value || 'none';
    const mediaGroup = document.getElementById('outroMediaGroup');
    const durationGroup = document.getElementById('outroDurationGroup');
    const fitGroup = document.getElementById('outroFitGroup');

    if (!mediaGroup || !durationGroup || !fitGroup) return;

    if (type === 'none') {
      mediaGroup.style.display = 'none';
      durationGroup.style.display = 'none';
      fitGroup.style.display = 'none';
    } else {
      mediaGroup.style.display = 'block';
      fitGroup.style.display = 'block';
      durationGroup.style.display = (type === 'image') ? 'block' : 'none';
      this.populateMediaSelect('outroMedia', type);
    }
  }

  toggleQuestionBackgroundFields() {
    const qBgType = document.getElementById('qBgType').value;
    const qBgColorGroup = document.getElementById('qBgColorGroup');
    const qBgUrlGroup = document.getElementById('qBgUrlGroup');
    const qBgFitGroup = document.getElementById('qBgFitGroup');

    if (qBgType === 'none') {
      qBgColorGroup.style.display = 'none';
      qBgUrlGroup.style.display = 'none';
      qBgFitGroup.style.display = 'none';
    } else if (qBgType === 'color') {
      qBgColorGroup.style.display = 'block';
      qBgUrlGroup.style.display = 'none';
      qBgFitGroup.style.display = 'none';
    } else {
      qBgColorGroup.style.display = 'none';
      qBgUrlGroup.style.display = 'block';
      qBgFitGroup.style.display = 'block';
      
      // Update media dropdown for new type
      this.populateMediaSelect('qBgMedia', qBgType);
    }
  }

  toggleQuestionNumberingFields() {
    const qNumbering = document.getElementById('qNumbering').value;
    const qStartNumberGroup = document.getElementById('qStartNumberGroup');
    const qNumberGapGroup = document.getElementById('qNumberGapGroup');

    if (qNumbering === 'manual') {
      qStartNumberGroup.style.display = 'block';
      qNumberGapGroup.style.display = 'block';
    } else if (qNumbering === 'auto') {
      qStartNumberGroup.style.display = 'none';
      qNumberGapGroup.style.display = 'block';
    } else {
      qStartNumberGroup.style.display = 'none';
      qNumberGapGroup.style.display = 'none';
    }
  }

  toggleMarginMode() {
    const mode = document.getElementById('qMarginMode').value;
    const allGroup = document.getElementById('qMarginAllGroup');
    const customGroup = document.getElementById('qMarginCustomGroup');

    if (mode === 'all') {
      allGroup.style.display = 'block';
      customGroup.style.display = 'none';
    } else {
      allGroup.style.display = 'none';
      customGroup.style.display = 'block';
    }
  }

  syncMarginFromSide(value) {
    // Sync all sides when link is checked
    this.presetConfig.question.margin = {
      top: value,
      right: value,
      bottom: value,
      left: value
    };
    
    // Update all sliders
    document.getElementById('qMarginAll').value = value;
    document.getElementById('qMarginTop').value = value;
    document.getElementById('qMarginRight').value = value;
    document.getElementById('qMarginBottom').value = value;
    document.getElementById('qMarginLeft').value = value;
    
    // Update all displays
    document.getElementById('qMarginAllVal').textContent = value + '%';
    document.getElementById('qMarginTopVal').textContent = value + '%';
    document.getElementById('qMarginRightVal').textContent = value + '%';
    document.getElementById('qMarginBottomVal').textContent = value + '%';
    document.getElementById('qMarginLeftVal').textContent = value + '%';
  }

  toggleOptionsMarginMode() {
    const mode = document.getElementById('optMarginMode').value;
    const allGroup = document.getElementById('optMarginAllGroup');
    const customGroup = document.getElementById('optMarginCustomGroup');

    if (mode === 'all') {
      allGroup.style.display = 'block';
      customGroup.style.display = 'none';
    } else {
      allGroup.style.display = 'none';
      customGroup.style.display = 'block';
    }
  }

  toggleOptionsLabelSpacing() {
    const labelStyle = document.getElementById('optLabelStyle').value;
    const spacingGroup = document.getElementById('optLabelSpacingGroup');
    
    if (labelStyle === 'none') {
      spacingGroup.style.display = 'none';
    } else {
      spacingGroup.style.display = 'block';
    }
  }

  syncOptionsMarginFromSide(value) {
    // Sync all sides when link is checked
    this.presetConfig.options.margin = {
      top: value,
      right: value,
      bottom: value,
      left: value
    };
    
    // Update all sliders
    document.getElementById('optMarginAll').value = value;
    document.getElementById('optMarginTop').value = value;
    document.getElementById('optMarginRight').value = value;
    document.getElementById('optMarginBottom').value = value;
    document.getElementById('optMarginLeft').value = value;
    
    // Update all displays
    document.getElementById('optMarginAllVal').textContent = value + '%';
    document.getElementById('optMarginTopVal').textContent = value + '%';
    document.getElementById('optMarginRightVal').textContent = value + '%';
    document.getElementById('optMarginBottomVal').textContent = value + '%';
    document.getElementById('optMarginLeftVal').textContent = value + '%';
  }

  toggleOptionBackgroundFields() {
    const optBgType = document.getElementById('optBgType').value;
    const optBgColorGroup = document.getElementById('optBgColorGroup');
    const optBgUrlGroup = document.getElementById('optBgUrlGroup');

    if (optBgType === 'color') {
      optBgColorGroup.style.display = 'block';
      optBgUrlGroup.style.display = 'none';
    } else {
      optBgColorGroup.style.display = 'none';
      optBgUrlGroup.style.display = 'block';
      
      // Update media dropdown for new type
      this.populateMediaSelect('optBgMedia', optBgType);
    }

    const optCorrectBgType = document.getElementById('optCorrectBgType').value;
    const optCorrectBgColorGroup = document.getElementById('optCorrectBgColorGroup');
    const optCorrectBgUrlGroup = document.getElementById('optCorrectBgUrlGroup');

    if (optCorrectBgType === 'color') {
      optCorrectBgColorGroup.style.display = 'block';
      optCorrectBgUrlGroup.style.display = 'none';
    } else {
      optCorrectBgColorGroup.style.display = 'none';
      optCorrectBgUrlGroup.style.display = 'block';
      
      // Update media dropdown for new type
      this.populateMediaSelect('optCorrectBgMedia', optCorrectBgType);
    }
  }

  toggleTimerBackgroundFields() {
    const timerBgType = document.getElementById('timerBgType').value;
    const timerBgColorGroup = document.getElementById('timerBgColorGroup');
    const timerBgUrlGroup = document.getElementById('timerBgUrlGroup');

    if (timerBgType === 'color') {
      timerBgColorGroup.style.display = 'block';
      timerBgUrlGroup.style.display = 'none';
    } else {
      timerBgColorGroup.style.display = 'none';
      timerBgUrlGroup.style.display = 'block';
      
      // Update media dropdown for new type
      this.populateMediaSelect('timerBgMedia', timerBgType);
    }
  }

  scheduleUpdate() {
    // DEBOUNCED RENDERING APPROACH
    // Wait for user to finish making changes before rendering
    // This eliminates race conditions and trailing borders
    
    // Cancel any pending update
    if (this.updateTimeout) {
      clearTimeout(this.updateTimeout);
    }

    // Wait 150ms after last change before rendering
    // This allows rapid changes (like dragging sliders) to complete
    // before we do the expensive render operation
    this.updateTimeout = setTimeout(() => {
      this.updateRenderer();
    }, 150);
  }

  async initRenderer() {
    try {
      this.updateStatus('Initializing renderer...', 'loading');

      // Destroy old renderer if exists
      if (this.renderer) {
        this.renderer.destroy();
      }

      // Create new renderer
      this.renderer = new CanvasRenderer(
        this.canvas,
        this.presetConfig,
        this.sampleQuestion
      );

      // Preload assets
      await this.renderer.preloadAssets();

      // Enable static preview mode - show all elements at once
      this.renderer.enableStaticPreview();

      this.updateStatus('Preview ready (Click Play to animate)', 'success');
      this.updateLastUpdated();

    } catch (error) {
      console.error('Renderer initialization error:', error);
      this.updateStatus('Renderer error: ' + error.message, 'error');
    }
  }

  async updateRenderer() {
    try {
      // Don't recreate renderer, just update config and re-render
      if (this.renderer) {
        // Skip if already rendering (prevents concurrent updates)
        if (this.isRendering) {
          return;
        }
        
        this.isRendering = true;
        
        // Update renderer's config reference
        this.renderer.config = this.presetConfig;
        
        // Update canvas size if changed
        if (this.canvas.width !== this.presetConfig.canvas.width || 
            this.canvas.height !== this.presetConfig.canvas.height) {
          this.canvas.width = this.presetConfig.canvas.width;
          this.canvas.height = this.presetConfig.canvas.height;
        }
        
        // Only preload assets if media/fonts changed (expensive operation)
        // For style changes (colors, positions, sizes), just re-render
        const needsAssetReload = this.checkIfAssetsChanged();
        if (needsAssetReload) {
          await this.renderer.preloadAssets();
        }
        
        // IMPORTANT: renderFrame will handle canvas clearing internally via clearCanvas()
        // Don't clear here - let the renderer handle it to prevent double-clearing
        
        // Re-render the canvas immediately
        // If not playing animation, use static preview mode
        if (!this.renderer.isPlaying) {
          this.renderer.enableStaticPreview();
        } else {
          // Re-render current frame during animation
          const currentTime = this.renderer.getCurrentTime();
          this.renderer.renderFrame(currentTime);
        }
        
        this.updateLastUpdated();
        this.isRendering = false;
      } else {
        // First time, initialize renderer
        await this.initRenderer();
      }
    } catch (error) {
      console.error('Update renderer error:', error);
      console.error('Error stack:', error.stack);
      this.updateStatus('Update failed: ' + error.message, 'error');
      this.isRendering = false;
    }
  }

  checkIfAssetsChanged() {
    // Check if background images, fonts, or media changed
    // This avoids expensive preloadAssets() calls for style-only changes
    
    if (!this.lastConfig) {
      this.lastConfig = JSON.parse(JSON.stringify(this.presetConfig));
      return true;
    }
    
    const current = this.presetConfig;
    const last = this.lastConfig;
    
    // Check for asset-related changes (use correct property paths)
    const overlayUrls = (current.overlays || []).map((overlay) => overlay?.image_url || '');
    const lastOverlayUrls = (last.overlays || []).map((overlay) => overlay?.image_url || '');
    const overlayAssetsChanged = overlayUrls.length !== lastOverlayUrls.length ||
      overlayUrls.some((url, idx) => url !== lastOverlayUrls[idx]);

    const assetsChanged = 
      current.canvas.background_url !== last.canvas.background_url ||
      current.canvas.intro?.url !== last.canvas.intro?.url ||
      current.canvas.outro?.url !== last.canvas.outro?.url ||
      current.question.background_url !== last.question.background_url ||
      current.options.background_url !== last.options.background_url ||
      current.options.correct_answer_background_url !== last.options.correct_answer_background_url ||
      current.timer.background_url !== last.timer.background_url ||
      current.explanation?.background_image !== last.explanation?.background_image ||
      current.explanation?.image_url !== last.explanation?.image_url ||
      overlayAssetsChanged ||
      current.question.font_family !== last.question.font_family ||
      current.options.font_family !== last.options.font_family ||
      current.timer.font_family !== last.timer.font_family ||
      current.explanation?.font_family !== last.explanation?.font_family;
    
    // Update lastConfig
    this.lastConfig = JSON.parse(JSON.stringify(current));
    
    return assetsChanged;
  }

  startTimeUpdate() {
    this.timeUpdateInterval = setInterval(() => {
      this.updateTimeDisplay();
    }, 100);
  }

  stopTimeUpdate() {
    if (this.timeUpdateInterval) {
      clearInterval(this.timeUpdateInterval);
    }
  }

  updateTimeDisplay() {
    if (this.renderer) {
      const time = this.renderer.getCurrentTime();
      document.getElementById('timeDisplay').textContent = time.toFixed(2) + 's';
    }
  }

  getMissingFontFiles() {
    const cfg = this.presetConfig || {};
    const candidates = [
      cfg?.question?.font_family,
      cfg?.options?.font_family,
      cfg?.timer?.font_family
    ].filter(Boolean);

    const looksLikeFile = (v) => /\.(ttf|otf|woff|woff2)$/i.test(v);

    // Build a set of known fonts from the Fonts library
    const known = new Set();
    for (const f of (this.fontsLibrary || [])) {
      if (f?.name) known.add(String(f.name));
      if (f?.filename) known.add(String(f.filename));
      if (f?.originalName) known.add(String(f.originalName));
    }

    const missing = [];
    for (const font of candidates) {
      if (!looksLikeFile(font)) continue; // system font, ignore
      if (!known.has(font)) missing.push(font);
    }

    // unique
    return Array.from(new Set(missing));
  }

  updateFontWarnings() {
    const el = document.getElementById('fontWarnings');
    if (!el) return;

    const missing = this.getMissingFontFiles();
    if (!missing.length) {
      el.style.display = 'none';
      el.innerHTML = '';
      return;
    }

    const items = missing.map(f => `<li><code>${f}</code></li>`).join('');
    el.innerHTML = `
      <div style="font-weight:600; margin-bottom:6px;">Missing font files referenced by this preset</div>
      <div style="margin-bottom:6px;">The preset is using font file names, but those files are not installed in the app. The preview/video will fall back to a default font.</div>
      <ul style="margin: 0 0 8px 18px; padding:0;">${items}</ul>
      <a href="/fonts" target="_blank" style="color:#ffeaa7; text-decoration:underline;">Open Fonts page to upload these fonts</a>
    `;
    el.style.display = 'block';
  }

  updateStatus(message, className = '') {
    const statusEl = document.getElementById('statusText');
    statusEl.textContent = message;
    statusEl.className = 'info-value ' + className;
  }

  updateLastUpdated() {
    const now = new Date();
    const timeStr = now.toLocaleTimeString();
    document.getElementById('lastUpdated').textContent = timeStr;
  }

  async savePreset() {
    // Create or update a preset
    // - If no presets exist yet, this will POST and create the first preset
    // - If a preset is selected, this will PUT and update it
    try {
      this.updateStatus('Saving preset...', 'loading');

      const isNew = !this.currentPresetId;

      // Prefer currentPresetName if set; otherwise ask user (first save)
      let presetName = (this.currentPresetName || '').trim();
      if (!presetName || presetName === 'New Preset') {
        presetName = prompt('Enter preset name:', presetName || 'My Preset') || 'Untitled Preset';
      }

      const method = isNew ? 'POST' : 'PUT';
      const url = isNew ? '/api/presets' : `/api/presets/${this.currentPresetId}`;

      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: presetName, config: this.presetConfig })
      });

      if (!response.ok) {
        const text = await response.text().catch(() => '');
        throw new Error(text || 'Failed to save preset');
      }

      const savedPreset = await response.json();

      // Update local state
      this.currentPresetId = savedPreset.id;
      this.currentPresetName = savedPreset.name;

      // Reload presets list and refresh selector options
      await this.loadAllPresets();
      const selectEl = document.getElementById('presetSelector');
      if (selectEl) {
        selectEl.customSelectData = {
          allOptions: this.allPresets.map(p => ({ value: p.id, text: p.name, icon: '🎨' }))
        };
        this.filterPresetOptions(selectEl, '');
        const valueDisplay = selectEl.querySelector('.custom-select-value');
        if (valueDisplay) {
          valueDisplay.textContent = this.currentPresetName;
          valueDisplay.classList.remove('placeholder');
        }
      }

      this.updateStatus('✓ Preset saved successfully!', 'success');
      setTimeout(() => this.updateStatus('Ready', 'success'), 2000);
    } catch (error) {
      console.error('Save error:', error);
      this.updateStatus('Save failed: ' + error.message, 'error');
    }
  }

  showSaveAsModal() {
    const modal = document.getElementById('saveAsModal');
    const nameInput = document.getElementById('copyPresetName');
    
    if (!modal || !nameInput) return;
    
    // Suggest a name
    nameInput.value = `${this.currentPresetName || 'Preset'} (Copy)`;
    
    modal.style.display = 'flex';
    setTimeout(() => {
      nameInput.focus();
      nameInput.select();
    }, 100);
  }

  hideSaveAsModal() {
    const modal = document.getElementById('saveAsModal');
    if (modal) {
      modal.style.display = 'none';
      document.getElementById('saveAsForm').reset();
    }
  }

  async savePresetAsCopy() {
    const nameInput = document.getElementById('copyPresetName');
    const name = nameInput.value.trim();
    
    if (!name) {
      this.updateStatus('Please enter a name for the copy', 'error');
      return;
    }

    try {
      // Create new preset
      const response = await fetch('/api/presets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: name,
          config: this.presetConfig
        })
      });

      if (!response.ok) throw new Error('Failed to save copy');

      const newPreset = await response.json();
      
      // Reload all presets
      await this.loadAllPresets();
      
      // Update preset selector
      const selectEl = document.getElementById('presetSelector');
      if (selectEl) {
        this.filterPresetOptions(selectEl, '');
      }
      
      // Load the new copy
      await this.loadPresetById(newPreset.id);
      
      // Update selector display
      const valueDisplay = document.querySelector('#presetSelector .custom-select-value');
      if (valueDisplay) {
        valueDisplay.textContent = name;
        valueDisplay.classList.remove('placeholder');
      }
      
      this.hideSaveAsModal();
      this.updateStatus('✓ Copy saved successfully!', 'success');
      setTimeout(() => this.updateStatus('Ready', 'success'), 2000);
    } catch (error) {
      console.error('Save copy error:', error);
      this.updateStatus('Failed to save copy', 'error');
    }
  }

  showDeleteModal() {
    if (!this.currentPresetId || !this.currentPresetName) {
      this.updateStatus('No preset loaded to delete', 'error');
      return;
    }

    const modal = document.getElementById('deleteModal');
    const nameDisplay = document.getElementById('deletePresetNameDisplay');
    
    if (!modal || !nameDisplay) return;
    
    nameDisplay.textContent = this.currentPresetName;
    modal.style.display = 'flex';
  }

  hideDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
      modal.style.display = 'none';
    }
  }

  async deletePreset() {
    if (!this.currentPresetId) {
      this.updateStatus('No preset to delete', 'error');
      return;
    }

    try {
      const response = await fetch(`/api/presets/${this.currentPresetId}`, {
        method: 'DELETE'
      });

      if (!response.ok) throw new Error('Failed to delete preset');

      this.hideDeleteModal();
      
      // Reload all presets
      await this.loadAllPresets();
      
      if (this.allPresets.length === 0) {
        this.updateStatus('No presets available', 'error');
        return;
      }
      
      // Update preset selector
      const selectEl = document.getElementById('presetSelector');
      if (selectEl) {
        this.filterPresetOptions(selectEl, '');
      }
      
      // Load the first preset
      await this.loadPresetById(this.allPresets[0].id);
      
      // Update selector display
      const valueDisplay = document.querySelector('#presetSelector .custom-select-value');
      if (valueDisplay) {
        valueDisplay.textContent = this.allPresets[0].name;
        valueDisplay.classList.remove('placeholder');
      }
      
      this.updateStatus('✓ Preset deleted successfully!', 'success');
      setTimeout(() => this.updateStatus('Ready', 'success'), 2000);
    } catch (error) {
      console.error('Delete error:', error);
      this.updateStatus('Failed to delete preset', 'error');
    }
  }

  toggleExplanationBackgroundFields() {
    const bgType = document.getElementById('explBgType')?.value;
    const colorGroup = document.getElementById('explBgColorGroup');
    const imageGroup = document.getElementById('explBgImageGroup');
    const imageFitGroup = document.getElementById('explBgImageFitGroup');
    
    if (colorGroup) colorGroup.style.display = bgType === 'color' ? 'block' : 'none';
    if (imageGroup) imageGroup.style.display = bgType === 'image' ? 'block' : 'none';
    if (imageFitGroup) imageFitGroup.style.display = bgType === 'image' ? 'block' : 'none';
    
    // Populate image dropdown if needed
    if (bgType === 'image' && this.mediaLibrary) {
      this.populateMediaSelect('explBgImage', 'image');
    }
  }

  toggleExplanationImageFields() {
    const enabled = document.getElementById('explImageEnabled')?.checked;
    const selectGroup = document.getElementById('explImageSelectGroup');
    const widthGroup = document.getElementById('explImageWidthGroup');
    const heightGroup = document.getElementById('explImageHeightGroup');
    const fitGroup = document.getElementById('explImageFitGroup');
    const posGroup = document.getElementById('explImagePositionGroup');
    
    const display = enabled ? 'block' : 'none';
    if (selectGroup) selectGroup.style.display = display;
    if (widthGroup) widthGroup.style.display = display;
    if (heightGroup) heightGroup.style.display = display;
    if (fitGroup) fitGroup.style.display = display;
    if (posGroup) posGroup.style.display = display;
    
    // Populate image dropdown if needed
    if (enabled && this.mediaLibrary) {
      this.populateMediaSelect('explImage', 'image');
    }
  }

  addOverlayItem() {
    if (!Array.isArray(this.presetConfig.overlays)) {
      this.presetConfig.overlays = [];
    }

    this.presetConfig.overlays.push({
      image_url: '',
      width: 300,
      height: 300,
      position: { x: 100, y: 100 },
      fit: 'contain',
      opacity: 1
    });

    this.renderOverlayList();
    this.scheduleUpdate();
  }

  updateOverlayItem(index, key, value) {
    if (!Array.isArray(this.presetConfig.overlays) || !this.presetConfig.overlays[index]) {
      return;
    }

    const overlay = this.presetConfig.overlays[index];
    if (key === 'positionX') {
      overlay.position.x = parseInt(value);
    } else if (key === 'positionY') {
      overlay.position.y = parseInt(value);
    } else {
      overlay[key] = value;
    }

    this.scheduleUpdate();
  }

  removeOverlayItem(index) {
    if (!Array.isArray(this.presetConfig.overlays)) {
      return;
    }

    this.presetConfig.overlays.splice(index, 1);
    this.renderOverlayList();
    this.scheduleUpdate();
  }

  renderOverlayList() {
    const list = document.getElementById('overlayList');
    if (!list) return;

    list.innerHTML = '';

    const overlays = Array.isArray(this.presetConfig.overlays) ? this.presetConfig.overlays : [];
    if (!overlays.length) {
      list.innerHTML = '<p style="color: #888; font-size: 12px;">No overlays added yet.</p>';
      return;
    }

    overlays.forEach((overlay, index) => {
      const row = document.createElement('div');
      row.className = 'overlay-item';
      const maxWidth = this.getOverlayMaxWidth();
      const maxHeight = this.getOverlayMaxHeight();
      row.innerHTML = `
        <div class="overlay-item-header">
          <strong>Overlay ${index + 1}</strong>
          <button type="button" class="btn btn-danger btn-sm" data-action="remove" data-index="${index}">Remove</button>
        </div>
        <div class="form-group">
          <label>Image</label>
          <select class="overlay-image" data-index="${index}">
            <option value="">Select an image...</option>
          </select>
        </div>
        <div class="form-group">
          <label>Width: <span class="overlay-width-val">${overlay.width || 300}px</span></label>
          <input type="range" class="overlay-width" min="50" max="${maxWidth}" value="${overlay.width || 300}" data-index="${index}">
        </div>
        <div class="form-group">
          <label>Height: <span class="overlay-height-val">${overlay.height || 300}px</span></label>
          <input type="range" class="overlay-height" min="50" max="${maxHeight}" value="${overlay.height || 300}" data-index="${index}">
        </div>
        <div class="form-group">
          <label>Position X: <span class="overlay-x-val">${overlay.position?.x ?? 100}px</span></label>
          <input type="range" class="overlay-x" min="0" max="${maxWidth}" value="${overlay.position?.x ?? 100}" data-index="${index}">
        </div>
        <div class="form-group">
          <label>Position Y: <span class="overlay-y-val">${overlay.position?.y ?? 100}px</span></label>
          <input type="range" class="overlay-y" min="0" max="${maxHeight}" value="${overlay.position?.y ?? 100}" data-index="${index}">
        </div>
        <div class="form-group">
          <label>Fit</label>
          <select class="overlay-fit" data-index="${index}">
            <option value="cover" ${overlay.fit === 'cover' ? 'selected' : ''}>Cover</option>
            <option value="contain" ${overlay.fit === 'contain' ? 'selected' : ''}>Contain</option>
            <option value="fill" ${overlay.fit === 'fill' ? 'selected' : ''}>Fill</option>
            <option value="none" ${overlay.fit === 'none' ? 'selected' : ''}>None</option>
          </select>
        </div>
        <div class="form-group">
          <label>Opacity: <span class="overlay-opacity-val">${Math.round((overlay.opacity ?? 1) * 100)}%</span></label>
          <input type="range" class="overlay-opacity" min="0" max="1" step="0.05" value="${overlay.opacity ?? 1}" data-index="${index}">
        </div>
      `;

      list.appendChild(row);

      const imageSelect = row.querySelector('.overlay-image');
      if (imageSelect && this.mediaLibrary) {
        const imageOptions = this.mediaLibrary.images || [];
        imageOptions.forEach(img => {
          const option = document.createElement('option');
          option.value = img.url;
          option.textContent = img.label || img.original_name || img.filename;
          if (img.url === overlay.image_url) option.selected = true;
          imageSelect.appendChild(option);
        });
      }
    });

    list.querySelectorAll('.overlay-image').forEach(select => {
      select.addEventListener('change', (e) => {
        const idx = parseInt(e.target.dataset.index);
        this.updateOverlayItem(idx, 'image_url', e.target.value);
      });
    });

    list.querySelectorAll('.overlay-width').forEach(input => {
      input.addEventListener('input', (e) => {
        const idx = parseInt(e.target.dataset.index);
        const value = e.target.value;
        e.target.closest('.form-group').querySelector('.overlay-width-val').textContent = value + 'px';
        this.updateOverlayItem(idx, 'width', parseInt(value));
      });
    });

    list.querySelectorAll('.overlay-height').forEach(input => {
      input.addEventListener('input', (e) => {
        const idx = parseInt(e.target.dataset.index);
        const value = e.target.value;
        e.target.closest('.form-group').querySelector('.overlay-height-val').textContent = value + 'px';
        this.updateOverlayItem(idx, 'height', parseInt(value));
      });
    });

    list.querySelectorAll('.overlay-x').forEach(input => {
      input.addEventListener('input', (e) => {
        const idx = parseInt(e.target.dataset.index);
        const value = e.target.value;
        e.target.closest('.form-group').querySelector('.overlay-x-val').textContent = value + 'px';
        this.updateOverlayItem(idx, 'positionX', parseInt(value));
      });
    });

    list.querySelectorAll('.overlay-y').forEach(input => {
      input.addEventListener('input', (e) => {
        const idx = parseInt(e.target.dataset.index);
        const value = e.target.value;
        e.target.closest('.form-group').querySelector('.overlay-y-val').textContent = value + 'px';
        this.updateOverlayItem(idx, 'positionY', parseInt(value));
      });
    });

    list.querySelectorAll('.overlay-fit').forEach(select => {
      select.addEventListener('change', (e) => {
        const idx = parseInt(e.target.dataset.index);
        this.updateOverlayItem(idx, 'fit', e.target.value);
      });
    });

    list.querySelectorAll('.overlay-opacity').forEach(input => {
      input.addEventListener('input', (e) => {
        const idx = parseInt(e.target.dataset.index);
        const value = parseFloat(e.target.value);
        e.target.closest('.form-group').querySelector('.overlay-opacity-val').textContent = Math.round(value * 100) + '%';
        this.updateOverlayItem(idx, 'opacity', value);
      });
    });

    list.querySelectorAll('[data-action="remove"]').forEach(button => {
      button.addEventListener('click', (e) => {
        const idx = parseInt(e.target.dataset.index);
        this.removeOverlayItem(idx);
      });
    });
  }

  getOverlayMaxWidth() {
    return this.presetConfig?.canvas?.width || 1920;
  }

  getOverlayMaxHeight() {
    return this.presetConfig?.canvas?.height || 1080;
  }

  async testVideo() {
    try {
      this.updateStatus('Generating test video...', 'loading');

      // TODO: Implement video generation
      console.log('Testing video generation with config:', this.presetConfig);

      alert('Video generation will be implemented in Phase 4!');

      this.updateStatus('Ready', 'success');

    } catch (error) {
      console.error('Video test error:', error);
      this.updateStatus('Video test failed: ' + error.message, 'error');
    }
  }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new DesignerUI();
  initNumberInputButtons();
  initAutoHideHeader();
});

// Initialize number input +/- buttons
function initNumberInputButtons() {
  let pressTimer = null;
  let intervalTimer = null;
  
  const updateValue = (input, action) => {
    if (!input) return;

    const min = parseFloat(input.min) || 0;
    const max = parseFloat(input.max) || 100;
    const step = parseFloat(input.step) || 1;
    let value = parseFloat(input.value) || 0;

    if (action === 'increment') {
      value = Math.min(max, value + step);
    } else if (action === 'decrement') {
      value = Math.max(min, value - step);
    }

    // Round to step precision
    const decimals = step.toString().split('.')[1]?.length || 0;
    value = parseFloat(value.toFixed(decimals));

    input.value = value;
    
    // Trigger change event to update the preview
    input.dispatchEvent(new Event('input', { bubbles: true }));
  };

  const startPress = (btn, inputId, action) => {
    const input = document.getElementById(inputId);
    if (!input) return;

    // Clear any existing timers
    clearTimeout(pressTimer);
    clearInterval(intervalTimer);

    // Immediate update on press
    updateValue(input, action);

    // Start long press detection after 500ms
    pressTimer = setTimeout(() => {
      // Start continuous updates every 100ms
      intervalTimer = setInterval(() => {
        updateValue(input, action);
      }, 100);
    }, 500);
  };

  const endPress = () => {
    clearTimeout(pressTimer);
    clearInterval(intervalTimer);
  };

  // Mouse events
  document.addEventListener('mousedown', (e) => {
    const btn = e.target.closest('.number-input-btn');
    if (!btn) return;

    e.preventDefault(); // Prevent text selection
    const inputId = btn.dataset.input;
    const action = btn.dataset.action;
    startPress(btn, inputId, action);
  });

  document.addEventListener('mouseup', endPress);
  document.addEventListener('mouseleave', (e) => {
    const btn = e.target.closest('.number-input-btn');
    if (btn) endPress();
  });

  // Touch events for mobile
  document.addEventListener('touchstart', (e) => {
    const btn = e.target.closest('.number-input-btn');
    if (!btn) return;

    e.preventDefault(); // Prevent default touch behavior
    const inputId = btn.dataset.input;
    const action = btn.dataset.action;
    startPress(btn, inputId, action);
  }, { passive: false });

  document.addEventListener('touchend', endPress);
  document.addEventListener('touchcancel', endPress);
}

// Auto-hide header on scroll
function initAutoHideHeader() {
  let lastScrollTop = 0;
  const header = document.querySelector('.app-header');
  const threshold = 50;

  if (!header) return;

  const handleScroll = () => {
    const scrollTop = window.scrollY || document.documentElement.scrollTop || 0;

    if (scrollTop > lastScrollTop && scrollTop > threshold) {
      // Scrolling down & past threshold
      header.classList.add('header-hidden');
    } else if (scrollTop < lastScrollTop) {
      // Scrolling up
      header.classList.remove('header-hidden');
    }

    lastScrollTop = scrollTop;
  };

  window.addEventListener('scroll', handleScroll, { passive: true });
}