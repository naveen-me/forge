/**
 * Images Component
 * Image generation from quiz questions
 */

export class ImagesComponent {
  constructor(container) {
    this.container = container;
    this.selectedPreset = null;
    this.selectedTopicId = null;
    this.topics = [];
    this.presets = [];
    this.questions = [];
    this.imageSets = [];
    this.selectedQuestionIds = new Set();
    this.prefix = 'Quiz';
    this.customName = '';
    this.isGenerating = false;

    // Search and Pagination
    this.searchQuery = '';
    this.currentPage = 1;
    this.itemsPerPage = 12;

    // Select2 instances
    this.presetSelect2 = null;
    this.topicSelect2 = null;
  }

  async render() {
    this.container.innerHTML = `
      <div class="content-header">
        <div>
          <h1 class="content-title">Image Generator</h1>
          <p class="content-description">Create static images from quiz questions</p>
        </div>
      </div>

      <div class="card mb-20">
        <div class="card-header">
          <span class="card-title">Generate Images</span>
        </div>
        <div class="card-body">
          <div class="video-options-grid">
            <div>
              <label class="form-label">Preset</label>
              <select id="presetSelect" class="form-control">
                <option value="">Select preset...</option>
              </select>
            </div>
            <div>
              <label class="form-label">Topic</label>
              <select id="topicSelect" class="form-control">
                <option value="">Select topic...</option>
              </select>
            </div>
          </div>

          <div id="imageSettingsSection" style="margin-top: 20px; display: none;">
            <hr style="border: 1px solid #ddd; margin: 20px 0;">
            <div class="batch-settings-grid">
              <div>
                <label class="form-label">File Name Prefix</label>
                <input type="text" id="prefixInput" class="form-control" value="Quiz" placeholder="e.g., Biology">
              </div>
              <div>
                <label class="form-label">Folder Name (Optional)</label>
                <input type="text" id="customNameInput" class="form-control" placeholder="e.g., Week 1 Quiz">
              </div>
              <div>
                <label class="form-label">Generation Mode</label>
                <select id="modeSelect" class="form-control">
                    <option value="separate">Separate (Question & Answer)</option>
                    <option value="merged">Merged (Single Image with Answer)</option>
                </select>
              </div>
            </div>
          </div>

          <div id="questionSelectionSection" style="margin-top: 20px; display: none;">
            <hr style="border: 1px solid #ddd; margin: 20px 0;">
            <div class="question-header">
              <h3 style="margin: 0; color: #4CAF50;">📝 Select Questions</h3>
              <div class="question-actions">
                <span id="questionSelectionCount" class="badge">0 questions selected</span>
                <button id="selectAllQuestionsBtn" class="btn btn-secondary btn-small" type="button">Select All</button>
                <button id="clearQuestionSelectionBtn" class="btn btn-secondary btn-small" type="button">Clear</button>
              </div>
            </div>
            <div id="questionsList" class="questions-list">
              <p style="color: #666; text-align: center;">Loading questions...</p>
            </div>
          </div>

          <div style="margin-top: 18px;">
            <button id="generateBtn" class="btn btn-primary btn-large" disabled>📸 Generate Images</button>
            <div id="generationProgress" style="display: none; margin-top: 15px;">
                <div class="progress-bar-container" style="height: 10px; background: #eee; border-radius: 5px; overflow: hidden;">
                    <div id="progressBar" style="width: 0%; background: #4CAF50; height: 100%; transition: width 0.3s;"></div>
                </div>
                <p id="progressText" style="font-size: 13px; color: #666; margin-top: 5px;">Generating images, please wait...</p>
            </div>
          </div>
        </div>
      </div>
    `;

    await this.loadData();
    this.setupEventListeners();
    window.imagesComponent = this;
  }

  async renderGeneratedImagesPage() {
    this.container.innerHTML = `
      <div class="content-header">
        <div>
          <h1 class="content-title">Generated Images</h1>
          <p class="content-description">View and download your generated image sets</p>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">All Generated Image Sets</span>
          <button id="refreshImagesBtn" class="btn btn-small btn-secondary">🔄 Refresh</button>
        </div>
        <div class="card-body">
          <div class="video-filters">
            <input type="text" id="imageSearchInput" class="form-control" placeholder="🔍 Search sets..." style="flex: 1;">
          </div>

          <div id="imageSetList"></div>
          <div id="imagePagination" class="pagination-container"></div>
        </div>
      </div>

      <!-- Modal for viewing images in a set -->
      <div id="imagePreviewModal" class="modal" style="display: none;">
        <div class="modal-content" style="max-width: 90%; max-height: 90%; overflow: auto;">
            <div class="modal-header">
                <h3 id="modalTitle">Image Set Preview</h3>
                <span class="close-modal">&times;</span>
            </div>
            <div class="modal-body">
                <div id="modalImageGrid" class="image-preview-grid"></div>
            </div>
        </div>
      </div>
    `;

    await this.loadImageSets();
    this.setupGeneratedImagesEventListeners();
    this.renderImageSetList();
    window.imagesComponent = this;
  }

  async loadData() {
    try {
      const [presetsRes, topicsRes, questionsRes] = await Promise.all([
        fetch('/api/presets'),
        fetch('/api/topics'),
        fetch('/api/questions')
      ]);

      this.presets = await presetsRes.json();
      this.topics = await topicsRes.json();
      const qData = await questionsRes.json();
      this.questions = qData.questions || qData || [];

      this.renderPresetSelect();
      this.renderTopicSelect();
    } catch (error) {
      console.error('Error loading data:', error);
    }
  }

  async loadImageSets() {
    try {
      const res = await fetch('/api/images/list');
      this.imageSets = await res.json();
    } catch (error) {
      console.error('Failed to load image sets:', error);
      this.imageSets = [];
    }
  }

  renderPresetSelect() {
    const select = document.getElementById('presetSelect');
    if (!select) return;
    select.innerHTML = '<option value="">Select preset...</option>' +
      this.presets.map(p => `<option value="${p.id}">${this.escapeHtml(p.name)}</option>`).join('');
    this.initializeSelect2();
  }

  renderTopicSelect() {
    const select = document.getElementById('topicSelect');
    if (!select) return;
    select.innerHTML = '<option value="">Select topic...</option>' +
      this.topics.map(t => `<option value="${t.id}">${this.escapeHtml(t.name)}</option>`).join('');
    this.initializeSelect2();
  }

  initializeSelect2() {
    if (!window.$ || !window.$.fn?.select2) return;

    const presetSelect = window.$('#presetSelect');
    const topicSelect = window.$('#topicSelect');

    if (presetSelect.length && !this.presetSelect2) {
      presetSelect.select2({ placeholder: 'Select preset...', allowClear: true, width: '100%' });
      presetSelect.on('change', (e) => {
        this.selectedPreset = e.target.value || null;
        this.updateGenerateButton();
      });
      this.presetSelect2 = presetSelect;
    }

    if (topicSelect.length && !this.topicSelect2) {
      topicSelect.select2({ placeholder: 'Select topic...', allowClear: true, width: '100%' });
      topicSelect.on('change', (e) => {
        this.selectedTopicId = e.target.value || null;
        this.renderQuestionSelection();
        this.updateGenerateButton();
      });
      this.topicSelect2 = topicSelect;
    }
  }

  setupEventListeners() {
    const prefixInput = document.getElementById('prefixInput');
    if (prefixInput) {
      prefixInput.addEventListener('input', (e) => this.prefix = e.target.value);
    }

    const customNameInput = document.getElementById('customNameInput');
    if (customNameInput) {
      customNameInput.addEventListener('input', (e) => this.customName = e.target.value);
    }

    const generateBtn = document.getElementById('generateBtn');
    if (generateBtn) {
      generateBtn.addEventListener('click', () => this.generateImages());
    }
  }

  setupGeneratedImagesEventListeners() {
    const refreshBtn = document.getElementById('refreshImagesBtn');
    if (refreshBtn) {
      refreshBtn.addEventListener('click', async () => {
        await this.loadImageSets();
        this.renderImageSetList();
      });
    }

    const searchInput = document.getElementById('imageSearchInput');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        this.searchQuery = e.target.value.toLowerCase();
        this.currentPage = 1;
        this.renderImageSetList();
      });
    }

    const modal = document.getElementById('imagePreviewModal');
    const closeBtn = document.querySelector('.close-modal');
    if (closeBtn && modal) {
        closeBtn.onclick = () => modal.style.display = 'none';
        window.onclick = (event) => {
            if (event.target === modal) modal.style.display = 'none';
        };
    }
  }

  renderQuestionSelection() {
    const section = document.getElementById('questionSelectionSection');
    const list = document.getElementById('questionsList');
    const settingsSection = document.getElementById('imageSettingsSection');
    if (!section || !list || !settingsSection) return;

    if (!this.selectedTopicId) {
      section.style.display = 'none';
      settingsSection.style.display = 'none';
      return;
    }

    section.style.display = 'block';
    settingsSection.style.display = 'block';

    const topicQuestions = this.questions.filter(q => String(q.topic_id) === String(this.selectedTopicId));

    if (topicQuestions.length === 0) {
      list.innerHTML = '<p style="color: #666; text-align: center;">No questions found in this topic.</p>';
      return;
    }

    list.innerHTML = topicQuestions.map(q => `
      <div class="question-checkbox-item">
        <label style="display: flex; align-items: flex-start; gap: 10px; cursor: pointer; padding: 10px; border-radius: 8px; background: #f9f9f9; margin-bottom: 5px;">
          <input type="checkbox" class="question-checkbox" data-question-id="${q.id}" ${this.selectedQuestionIds.has(q.id) ? 'checked' : ''}>
          <div style="flex: 1;">
            <div style="font-weight: bold; color: #333;">${this.escapeHtml(q.question)}</div>
          </div>
        </label>
      </div>
    `).join('');

    list.querySelectorAll('.question-checkbox').forEach(cb => {
      cb.addEventListener('change', (e) => {
        const qid = e.target.getAttribute('data-question-id');
        if (e.target.checked) this.selectedQuestionIds.add(qid);
        else this.selectedQuestionIds.delete(qid);
        this.updateQuestionCount();
        this.updateGenerateButton();
      });
    });

    document.getElementById('selectAllQuestionsBtn').onclick = () => {
      topicQuestions.forEach(q => this.selectedQuestionIds.add(q.id));
      this.renderQuestionSelection();
      this.updateQuestionCount();
      this.updateGenerateButton();
    };

    document.getElementById('clearQuestionSelectionBtn').onclick = () => {
      this.selectedQuestionIds.clear();
      this.renderQuestionSelection();
      this.updateQuestionCount();
      this.updateGenerateButton();
    };

    this.updateQuestionCount();
  }

  updateQuestionCount() {
    const badge = document.getElementById('questionSelectionCount');
    if (badge) badge.textContent = `${this.selectedQuestionIds.size} questions selected`;
  }

  updateGenerateButton() {
    const btn = document.getElementById('generateBtn');
    if (btn) btn.disabled = !this.selectedPreset || this.selectedQuestionIds.size === 0 || this.isGenerating;
  }

  async generateImages() {
    const questionIds = Array.from(this.selectedQuestionIds);
    if (!this.selectedPreset || questionIds.length === 0) return;

    this.isGenerating = true;
    this.updateGenerateButton();
    
    const progressSection = document.getElementById('generationProgress');
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');
    
    progressSection.style.display = 'block';
    progressBar.style.width = '10%';
    progressText.textContent = 'Initializing generation...';

    try {
      const mode = document.getElementById('modeSelect').value;

      const response = await fetch('/api/images/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          questionIds,
          presetId: this.selectedPreset,
          customName: this.customName || null,
          prefix: this.prefix,
          mode
        })
      });

      const result = await response.json();

      if (result.success) {
        progressBar.style.width = '100%';
        progressText.textContent = 'Successfully generated images!';
        alert(`✅ Images generated! Folder: ${result.folderName}`);
        window.app.navigate('/generated-images-list');
      }
 else {
        alert(`❌ Error: ${result.error || 'Failed to generate images'}`);
      }
    } catch (error) {
      console.error('Error generating images:', error);
      alert(`❌ Error: ${error.message}`);
    } finally {
      this.isGenerating = false;
      this.updateGenerateButton();
    }
  }

  renderImageSetList() {
    const container = document.getElementById('imageSetList');
    if (!container) return;

    if (this.imageSets.length === 0) {
      container.innerHTML = '<p style="text-align: center; color: #666; padding: 40px;">No images generated yet.</p>';
      return;
    }

    let filtered = this.imageSets.filter(s => s.name.toLowerCase().includes(this.searchQuery));

    container.innerHTML = `
      <div class="video-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px;">
        ${filtered.map(s => `
          <div class="video-card" style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); transition: all 0.2s ease;">
            <div style="display: flex; align-items: flex-start; gap: 16px; margin-bottom: 16px;">
              <div style="width: 56px; height: 56px; border-radius: 12px; background: #ecfdf5; display: flex; align-items: center; justify-content: center; color: #10b981; flex-shrink: 0;">
                <span class="material-symbols-outlined" style="font-size: 32px;">image</span>
              </div>
              <div style="flex: 1; min-width: 0;">
                <div class="video-name" style="font-weight: 800; color: #1e293b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px; font-size: 16px;" title="${s.folderName}">${this.escapeHtml(s.name)}</div>
                <div class="video-meta" style="font-size: 13px; color: #64748b; display: flex; flex-direction: column; gap: 2px;">
                    <span style="display: flex; align-items: center; gap: 4px;">
                        <span class="material-symbols-outlined" style="font-size: 14px;">calendar_today</span>
                        ${new Date(s.created).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                    </span>
                    <span style="display: flex; align-items: center; gap: 4px;">
                        <span class="material-symbols-outlined" style="font-size: 14px;">photo_library</span>
                        <strong>${s.imageCount}</strong> images
                    </span>
                </div>
              </div>
            </div>
            <div class="video-actions" style="display: flex; gap: 10px; padding-top: 16px; border-top: 1px solid #f1f5f9;">
              <button class="btn btn-secondary btn-small" style="flex: 1; justify-content: center; border-radius: 8px;" onclick="window.imagesComponent.previewSet('${s.folderName}')">
                <span class="material-symbols-outlined" style="font-size: 18px;">visibility</span>
                View
              </button>
              <a href="/api/images/download/${s.folderName}" class="btn btn-primary btn-small" style="flex: 1; justify-content: center; border-radius: 8px;">
                <span class="material-symbols-outlined" style="font-size: 18px;">download</span>
                Zip
              </a>
              <button class="icon-btn danger" onclick="window.imagesComponent.deleteSet('${s.folderName}')" style="width: 36px; height: 36px; border-radius: 8px;">
                <span class="material-symbols-outlined">delete</span>
              </button>
            </div>
          </div>
        `).join('')}
      </div>
    `;
  }

  async previewSet(folderName) {
    const modal = document.getElementById('imagePreviewModal');
    const grid = document.getElementById('modalImageGrid');
    const title = document.getElementById('modalTitle');
    
    if (!modal || !grid || !title) return;
    
    title.textContent = `Preview: ${folderName}`;
    grid.innerHTML = '<p>Loading images...</p>';
    modal.style.display = 'block';

    try {
        const res = await fetch(`/api/images/set/${folderName}`);
        const images = await res.json();
        
        if (!Array.isArray(images)) {
            throw new Error(images.error || 'Failed to load images');
        }
        
        if (images.length === 0) {
            grid.innerHTML = '<p>No images found in this set.</p>';
            return;
        }

        grid.innerHTML = images.map(imgUrl => `
            <div class="preview-item">
                <img src="${imgUrl}" loading="lazy">
                <div class="preview-filename">${imgUrl.split('/').pop()}</div>
            </div>
        `).join('');
    } catch (error) {
        grid.innerHTML = `<p class="text-error">Error loading images: ${error.message}</p>`;
    }
  }

  async deleteSet(folderName) {
    if (!confirm(`Delete image set "${folderName}"?`)) return;
    try {
      const res = await fetch(`/api/images/${folderName}`, { method: 'DELETE' });
      const result = await res.json();
      if (result.success) {
        await this.loadImageSets();
        this.renderImageSetList();
      }
    } catch (error) {
      alert(`Error: ${error.message}`);
    }
  }

  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
  }

  cleanup() {
    if (this.presetSelect2) this.presetSelect2.select2('destroy');
    if (this.topicSelect2) this.topicSelect2.select2('destroy');
  }
}
