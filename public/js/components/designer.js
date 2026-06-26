/**
 * Designer Component - Enhanced with Number Inputs
 * Visual preset designer with live preview
 */

export class DesignerComponent {
  constructor(container) {
    this.container = container;
    this.presetConfig = null;
    this.renderer = null;
  }

  getDefaultPresetConfig() {
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
        label_style: 'alpha',
        label_spacing: 12,
        margin: { top: 0, right: 0, bottom: 0, left: 0 },
        border_radius: 0,
        border_width: 0,
        border_color: '#ffffff',
        background_type: 'color',
        background_color: '#2c2c2c',
        correct_answer_background_type: 'color',
        correct_answer_background_color: '#1b5e20',
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
        border_width: 0,
        border_color: '#ffffff',
        border_radius: 0
      },
      animation: {
        question_display_duration: 2,
        options_display_mode: 'all_at_once',
        option_reveal_delay: 0.5,
        answer_reveal_duration: 3,
        transition_effect: 'fade',
        transition_duration: 0.3
      }
    };
  }
  
  async render() {
    this.container.innerHTML = `
      <div class="content-header">
        <h1 class="content-title">🎨 Visual Designer</h1>
        <p class="content-description">Create and edit video presets with live preview and number inputs</p>
      </div>
      
      <div style="display: grid; grid-template-columns: 350px 1fr; gap: 20px; height: calc(100vh - 180px);">
        <!-- Property Panel -->
        <div style="background: #1a1a1a; border-radius: 8px; overflow-y: auto; padding: 20px;">
          <h3 style="color: #4CAF50; margin-bottom: 20px;">Canvas Settings</h3>
          
          <div class="form-group">
            <label>Canvas Size</label>
            <select id="canvasSize" class="form-control">
              <option value="1920:1080">1920x1080 (Full HD)</option>
              <option value="1280:720">1280x720 (HD)</option>
              <option value="1080:1920">1080x1920 (Vertical)</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>Background Color</label>
            <input type="color" id="bgColor" class="form-control" value="#1a1a1a">
          </div>
          
          <h3 style="color: #4CAF50; margin: 30px 0 20px 0;">Question Settings</h3>
          
          <div class="form-group">
            <label>Font Size (px)</label>
            <input type="number" id="qFontSize" class="form-control" value="48" min="20" max="100">
          </div>
          
          <div class="form-group">
            <label>Font Color</label>
            <input type="color" id="qFontColor" class="form-control" value="#FFFFFF">
          </div>
          
          <div class="form-group">
            <label>Position X (px)</label>
            <input type="number" id="qPosX" class="form-control" value="100" min="0" max="1920">
          </div>
          
          <div class="form-group">
            <label>Position Y (px)</label>
            <input type="number" id="qPosY" class="form-control" value="150" min="0" max="1080">
          </div>
          
          <h3 style="color: #4CAF50; margin: 30px 0 20px 0;">Options Settings</h3>
          
          <div class="form-group">
            <label>Option Width (px)</label>
            <input type="number" id="optWidth" class="form-control" value="860" min="200" max="1500">
          </div>
          
          <div class="form-group">
            <label>Option Height (px)</label>
            <input type="number" id="optHeight" class="form-control" value="120" min="50" max="300">
          </div>
          
          <div class="form-group">
            <label>Font Size (px)</label>
            <input type="number" id="optFontSize" class="form-control" value="50" min="20" max="80">
          </div>
          
          <div class="form-group">
            <label>Background Color</label>
            <input type="color" id="optBgColor" class="form-control" value="#EEEEEE">
          </div>
          
          <h3 style="color: #4CAF50; margin: 30px 0 20px 0;">Timer Settings</h3>
          
          <div class="form-group">
            <label>Duration (seconds)</label>
            <input type="number" id="timerDuration" class="form-control" value="5" min="1" max="60">
          </div>
          
          <div class="form-group">
            <label>Font Size (px)</label>
            <input type="number" id="timerFontSize" class="form-control" value="61" min="20" max="100">
          </div>
          
          <button class="btn btn-primary" style="width: 100%; margin-top: 20px;" onclick="designerComponent.savePreset()">
            💾 Save Preset
          </button>
        </div>
        
        <!-- Preview Panel -->
        <div style="background: #000; border-radius: 8px; display: flex; flex-direction: column;">
          <div style="padding: 15px; background: #1a1a1a; border-radius: 8px 8px 0 0; display: flex; justify-content: space-between; align-items: center;">
            <h3 style="color: #4CAF50; margin: 0;">Live Preview</h3>
            <div style="display: flex; gap: 10px;">
              <button class="btn btn-small btn-secondary" onclick="designerComponent.startPreview()">▶️ Play</button>
              <button class="btn btn-small" onclick="designerComponent.stopPreview()">⏸️ Pause</button>
            </div>
          </div>
          <div style="flex: 1; display: flex; align-items: center; justify-content: center; padding: 20px;">
            <canvas id="previewCanvas" style="max-width: 100%; max-height: 100%; border: 1px solid #333;"></canvas>
          </div>
          <div style="padding: 15px; background: #1a1a1a; border-radius: 0 0 8px 8px; color: #999; text-align: center;">
            <strong>✨ Feature:</strong> All controls use number inputs for precise values!
          </div>
        </div>
      </div>
    `;
    
    window.designerComponent = this;
    await this.initDesigner();
  }
  
  async initDesigner() {
    try {
      // Load preset
      const response = await fetch('/api/presets');
      const presets = await response.json();
      
      if (presets.length === 0) {
        // Fresh state: no presets yet. Start with a default config and let user Save to create the first preset.
        this.presetConfig = this.getDefaultPresetConfig();
      } else {
        const preset = presets[0];
        this.presetConfig = typeof preset.config === 'string'
          ? JSON.parse(preset.config)
          : preset.config;
      }
      
      // Load sample question
      const qResponse = await fetch('/api/questions?page=1&limit=1');
      const qData = await qResponse.json();
      const question = qData.questions[0];
      
      // Import rendering engine
      const { CanvasRenderer } = await import('/shared/renderingEngine.js');
      
      const canvas = document.getElementById('previewCanvas');
      this.renderer = new CanvasRenderer(canvas, this.presetConfig, question);
      
      await this.renderer.preloadAssets();
      this.renderer.renderFrame(0);
      
      // Setup change listeners for number inputs
      this.setupInputListeners();
      
    } catch (error) {
      console.error('Designer init error:', error);
      alert('Error initializing designer: ' + error.message);
    }
  }
  
  setupInputListeners() {
    const inputs = ['qFontSize', 'qPosX', 'qPosY', 'optWidth', 'optHeight', 'optFontSize', 'timerDuration', 'timerFontSize'];
    
    inputs.forEach(id => {
      const el = document.getElementById(id);
      if (el) {
        el.addEventListener('change', () => this.updatePreview());
      }
    });
    
    ['bgColor', 'qFontColor', 'optBgColor'].forEach(id => {
      const el = document.getElementById(id);
      if (el) {
        el.addEventListener('change', () => this.updatePreview());
      }
    });
  }
  
  updatePreview() {
    if (!this.renderer) return;
    
    // Update config from inputs
    this.presetConfig.question.font_size = parseInt(document.getElementById('qFontSize').value);
    this.presetConfig.question.position.x = parseInt(document.getElementById('qPosX').value);
    this.presetConfig.question.position.y = parseInt(document.getElementById('qPosY').value);
    this.presetConfig.question.font_color = document.getElementById('qFontColor').value;
    
    this.presetConfig.options.option_width = parseInt(document.getElementById('optWidth').value);
    this.presetConfig.options.option_height = parseInt(document.getElementById('optHeight').value);
    this.presetConfig.options.font_size = parseInt(document.getElementById('optFontSize').value);
    this.presetConfig.options.background_color = document.getElementById('optBgColor').value;
    
    this.presetConfig.timer.duration = parseInt(document.getElementById('timerDuration').value);
    this.presetConfig.timer.font_size = parseInt(document.getElementById('timerFontSize').value);
    
    this.presetConfig.canvas.background_color = document.getElementById('bgColor').value;
    
    // Re-render
    this.renderer.renderFrame(0);
  }
  
  startPreview() {
    if (this.renderer) {
      this.renderer.startAnimation();
    }
  }
  
  stopPreview() {
    if (this.renderer) {
      this.renderer.stopAnimation();
    }
  }
  
  async savePreset() {
    try {
      const name = prompt('Enter preset name:', 'My Preset');
      if (!name) return;
      
      const response = await fetch('/api/presets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, config: this.presetConfig })
      });
      
      if (!response.ok) throw new Error('Failed to save');
      
      alert('Preset saved successfully!');
    } catch (error) {
      alert('Error saving preset: ' + error.message);
    }
  }
}
