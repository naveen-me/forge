/**
 * Videos Component
 * Topic-based video generation with queue system
 */

export class VideosComponent {
  constructor(container) {
    this.container = container;
    this.selectedPreset = null;
    this.selectedTopicId = null;
    this.topics = [];
    this.presets = [];
    this.videos = [];
    this.questions = [];
    this.selectedQuestionIds = new Set();
    this.batchSize = 0;
    this.videoName = '';

    // Queue management
    this.queueStatus = { current: null, queue: [], history: [] };
    this.queuePollInterval = null;

    // Video list filters
    this.searchQuery = '';
    this.currentPage = 1;
    this.itemsPerPage = 12;
    this.sortBy = 'date';
    this.sortOrder = 'desc';

    // Select2 instances for cleanup
    this.presetSelect2 = null;
    this.topicSelect2 = null;
  }

  async render() {
    this.container.innerHTML = `
      <div class="content-header">
        <div>
          <h1 class="content-title">Video Generator</h1>
          <p class="content-description">Create and manage quiz videos</p>
        </div>
      </div>

      <div class="card mb-20">
        <div class="card-header">
          <span class="card-title">Generate Videos</span>
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
            <div>
              <label class="form-label">TTS Set (Optional)</label>
              <select id="ttsSetSelect" class="form-control">
                <option value="">Default (Auto-select)</option>
              </select>
              <small class="text-muted">Pick a specific named TTS set to use for this video.</small>
            </div>
          </div>

          <div id="questionSelectionSection" style="margin-top: 20px; display: none;">
            <hr style="border: 1px solid #ddd; margin: 20px 0;">
            <div class="question-header">
              <h3 style="margin: 0; color: #2196F3;">📝 Select Questions</h3>
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

          <div id="batchSettingsSection" style="margin-top: 20px; display: none;">
            <hr style="border: 1px solid #ddd; margin: 20px 0;">
            <h3 style="margin-bottom: 15px; color: #2196F3;">⚙️ Batch Settings</h3>
            <div class="batch-settings-grid">
              <div>
                <label class="form-label">Batch Size (Questions per Video)</label>
                <input type="number" id="batchSizeInput" class="form-control" min="0" value="0" placeholder="0 = All in one video">
                <p class="text-muted" style="margin-top: 6px;">Enter number of questions per video. Use 0 to generate one video with all questions.</p>
              </div>
              <div>
                <label class="form-label">Video Name Prefix</label>
                <input type="text" id="videoNameInput" class="form-control" placeholder="e.g., Topic Quiz" maxlength="50">
              </div>
            </div>
          </div>

          <div style="margin-top: 18px;">
            <button id="generateBtn" class="btn btn-primary btn-large" disabled>🎬 Add to Queue</button>
            <p class="text-muted" style="margin-top: 10px;">Select a preset and at least one question.</p>
          </div>
        </div>
      </div>

      <!-- Queue Status Card -->
      <div id="queueStatusCard" class="card mb-20" style="display: none;">
        <div class="card-header" style="background: #f8f9fa; border-bottom: 2px solid #4CAF50;">
          <span class="card-title" style="color: #333;">⏳ Generation Queue</span>
        </div>
        <div class="card-body" style="background: #fff;">
          <div id="queueStatusContent"></div>
        </div>
      </div>
    `;

    await this.loadData();
    this.setupEventListeners();
    this.startQueuePolling();
    window.videosComponent = this;
  }

  async renderGeneratedVideosPage() {
    this.container.innerHTML = `
      <div class="content-header">
        <div>
          <h1 class="content-title">Generated Videos</h1>
          <p class="content-description">Manage and view your generated quiz videos</p>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">All Generated Videos</span>
          <button id="refreshVideosBtn" class="btn btn-small btn-secondary">🔄 Refresh</button>
        </div>
        <div class="card-body">
          <!-- Search and Filter Controls -->
          <div class="video-filters">
            <input type="text" id="videoSearchInput" class="form-control" placeholder="🔍 Search videos..." style="flex: 1;">
            <select id="videoSortSelect" class="form-control" style="max-width: 200px;">
              <option value="date-desc">Newest First</option>
              <option value="date-asc">Oldest First</option>
              <option value="name-asc">Name A-Z</option>
              <option value="name-desc">Name Z-A</option>
            </select>
          </div>

          <div id="videoList"></div>

          <!-- Pagination -->
          <div id="videoPagination" class="pagination-container"></div>
        </div>
      </div>
    `;

    await this.loadVideos();
    this.setupGeneratedVideosEventListeners();
    this.renderVideoList();
  }

  setupGeneratedVideosEventListeners() {
    const refreshBtn = document.getElementById('refreshVideosBtn');
    if (refreshBtn) {
      refreshBtn.addEventListener('click', async () => {
        await this.loadVideos();
        this.renderVideoList();
      });
    }

    const searchInput = document.getElementById('videoSearchInput');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        this.searchQuery = e.target.value.toLowerCase();
        this.currentPage = 1;
        this.renderVideoList();
      });
    }

    const sortSelect = document.getElementById('videoSortSelect');
    if (sortSelect) {
      sortSelect.addEventListener('change', (e) => {
        const [sortBy, sortOrder] = e.target.value.split('-');
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.renderVideoList();
      });
    }
  }

  async loadData() {
    try {
      const presetsRes = await fetch('/api/presets');
      this.presets = await presetsRes.json();

      const topicsRes = await fetch('/api/topics');
      this.topics = await topicsRes.json();

      const setsRes = await fetch('/api/tts/sets');
      this.ttsSets = await setsRes.json();

      await this.loadQuestions();

      this.renderPresetSelect();
      this.renderTopicSelect();
      this.renderTTSSetSelect();
    } catch (error) {
      console.error('Error loading data:', error);
      this.container.innerHTML += `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load data. ${error.message}
        </div>
      `;
    }
  }

  renderTTSSetSelect() {
    const select = document.getElementById('ttsSetSelect');
    if (!select) return;

    // Filter sets by selected topic
    let sets = this.ttsSets || [];
    if (this.selectedTopicId) {
        sets = sets.filter(s => String(s.topic_id) === String(this.selectedTopicId));
    }

    select.innerHTML = '<option value="">Default (Auto-select)</option>' +
      sets.map(s => `<option value="${s.id}">${this.escapeHtml(s.name)} (${s.provider} &middot; ${s.item_count} items)</option>`).join('');
    this.initializeTTSSetSelect2();
  }

  initializeTTSSetSelect2() {
    if (!window.$ || !window.$.fn?.select2) return;
    const $select = window.$('#ttsSetSelect');
    if ($select.hasClass('select2-hidden-accessible')) $select.select2('destroy');
    $select.select2({ placeholder: 'Default (Auto-select)', allowClear: true, width: '100%' });
    $select.on('change', (e) => { this.selectedSetId = e.target.value || null; });
  }

  async loadQuestions() {
    const res = await fetch('/api/questions');
    const data = await res.json();
    this.questions = data.questions || data || [];
  }

  async loadVideos() {
    try {
      const res = await fetch('/api/videos/list');
      this.videos = await res.json();
    } catch (error) {
      console.error('Failed to load videos:', error);
      this.videos = [];
    }
  }

  renderPresetSelect() {
    const select = document.getElementById('presetSelect');
    if (!select) return;

    if (this.presets.length === 0) {
      select.innerHTML = '<option value="">No presets available (create one in Designer)</option>';
      select.disabled = true;
      return;
    }

    select.disabled = false;
    select.innerHTML = '<option value="">Select preset...</option>' +
      this.presets.map(p => `<option value="${p.id}">${p.name || 'Unnamed Preset'}</option>`).join('');

    // Initialize Select2 if available
    this.initializeSelect2();
  }

  renderTopicSelect() {
    const select = document.getElementById('topicSelect');
    if (!select) return;
    select.innerHTML = '<option value="">Select topic...</option>' +
      this.topics.map(t => `<option value="${t.id}">${this.escapeHtml(t.name)}</option>`).join('');

    // Initialize Select2 if available
    this.initializeSelect2();
  }

  initializeSelect2() {
    if (!window.$ || !window.$.fn?.select2) return;

    const presetSelect = document.getElementById('presetSelect');
    const topicSelect = document.getElementById('topicSelect');

    // Initialize Preset Select2
    if (presetSelect && !this.presetSelect2) {
      const $presetSelect = window.$(presetSelect);

      // Destroy existing instance if any
      if ($presetSelect.hasClass('select2-hidden-accessible')) {
        $presetSelect.select2('destroy');
      }

      // Initialize with search
      $presetSelect.select2({
        placeholder: 'Select preset...',
        allowClear: true,
        width: '100%'
      });

      // Add change listener
      $presetSelect.on('change', (e) => {
        this.selectedPreset = e.target.value || null;
        this.updateGenerateButton();
      });

      this.presetSelect2 = $presetSelect;
    }

    // Initialize Topic Select2
    if (topicSelect && !this.topicSelect2) {
      const $topicSelect = window.$(topicSelect);

      // Destroy existing instance if any
      if ($topicSelect.hasClass('select2-hidden-accessible')) {
        $topicSelect.select2('destroy');
      }

      // Initialize with search
      $topicSelect.select2({
        placeholder: 'Select topic...',
        allowClear: true,
        width: '100%'
      });

      // Add change listener
      $topicSelect.on('change', (e) => {
        this.selectedTopicId = e.target.value || null;
        this.renderQuestionSelection();
        this.renderTTSSetSelect(); // Re-filter sets when topic changes
        this.updateGenerateButton();
      });

      this.topicSelect2 = $topicSelect;
    }
  }

  setupEventListeners() {
    const topicSelect = document.getElementById('topicSelect');
    const presetSelect = document.getElementById('presetSelect');

    if (topicSelect) {
      topicSelect.addEventListener('change', (e) => {
        this.selectedTopicId = e.target.value || null;
        this.renderQuestionSelection();
        this.updateGenerateButton();
      });
    }

    if (presetSelect) {
      presetSelect.addEventListener('change', (e) => {
        this.selectedPreset = e.target.value || null;
        this.updateGenerateButton();
      });
    }

    const generateBtn = document.getElementById('generateBtn');
    if (generateBtn) {
      generateBtn.addEventListener('click', () => this.addToQueue());
    }

    const refreshBtn = document.getElementById('refreshVideosBtn');
    if (refreshBtn) {
      refreshBtn.addEventListener('click', async () => {
        await this.loadVideos();
        this.renderVideoList();
      });
    }

    const batchSizeInput = document.getElementById('batchSizeInput');
    if (batchSizeInput) {
      batchSizeInput.addEventListener('input', (e) => {
        this.batchSize = parseInt(e.target.value, 10) || 0;
      });
    }

    const videoNameInput = document.getElementById('videoNameInput');
    if (videoNameInput) {
      videoNameInput.addEventListener('input', (e) => {
        this.videoName = e.target.value;
      });
    }

    // Video search and filter
    const searchInput = document.getElementById('videoSearchInput');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        this.searchQuery = e.target.value.toLowerCase();
        this.currentPage = 1;
        this.renderVideoList();
      });
    }

    const sortSelect = document.getElementById('videoSortSelect');
    if (sortSelect) {
      sortSelect.addEventListener('change', (e) => {
        const [sortBy, sortOrder] = e.target.value.split('-');
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.renderVideoList();
      });
    }
  }

  renderQuestionSelection() {
    const section = document.getElementById('questionSelectionSection');
    const list = document.getElementById('questionsList');
    const batchSection = document.getElementById('batchSettingsSection');
    if (!section || !list || !batchSection) return;

    if (!this.selectedTopicId) {
      section.style.display = 'none';
      batchSection.style.display = 'none';
      return;
    }

    section.style.display = 'block';
    batchSection.style.display = 'block';

    // Filter questions by selected topic with proper type comparison
    const topicQuestions = this.questions.filter(q => String(q.topic_id) === String(this.selectedTopicId));

    if (topicQuestions.length === 0) {
      list.innerHTML = '<p style="color: #666; text-align: center;">No questions found in this topic.</p>';
      return;
    }

    list.innerHTML = topicQuestions.map(q => `
      <div class="question-checkbox-item">
        <label style="display: flex; align-items: flex-start; gap: 14px; cursor: pointer; padding: 16px; border-radius: 12px; background: #ffffff; margin-bottom: 10px; transition: all 0.2s; border: 1px solid #e2e8f0; box-shadow: 0 1px 3px rgba(0,0,0,0.04);">
          <input type="checkbox"
                 class="question-checkbox"
                 data-question-id="${q.id}"
                 ${this.selectedQuestionIds.has(q.id) ? 'checked' : ''}
                 style="margin-top: 4px; width: 20px; height: 20px; accent-color: #4f46e5; flex-shrink: 0;">
          <div style="flex: 1; min-width: 0;">
            <div style="color: #1e293b; font-weight: 700; margin-bottom: 6px; font-size: 15px; line-height: 1.4;">${this.escapeHtml(q.question || q.question_text || 'Untitled Question')}</div>
            <div style="color: #64748b; font-size: 12px; display: flex; gap: 16px; align-items: center;">
              <span style="display: flex; align-items: center; gap: 4px;">
                <span class="material-symbols-outlined" style="font-size: 14px;">list</span>
                <strong>${Array.isArray(q.options) ? q.options.length : 0}</strong> options
              </span>
              <span style="display: flex; align-items: center; gap: 4px;">
                <span class="material-symbols-outlined" style="font-size: 14px; color: #22c55e;">check_circle</span>
                Correct: <strong>Option ${q.correct_option !== undefined && q.correct_option !== null ? (parseInt(q.correct_option) + 1) : 'N/A'}</strong>
              </span>
            </div>
          </div>
        </label>
      </div>
    `).join('');

    // Attach checkbox listeners
    list.querySelectorAll('.question-checkbox').forEach(cb => {
      cb.addEventListener('change', (e) => {
        const qid = e.target.getAttribute('data-question-id');
        if (e.target.checked) {
          this.selectedQuestionIds.add(qid);
        } else {
          this.selectedQuestionIds.delete(qid);
        }
        this.updateQuestionCount();
        this.updateGenerateButton();
      });
    });

    const selectAllBtn = document.getElementById('selectAllQuestionsBtn');
    if (selectAllBtn) {
      selectAllBtn.onclick = () => {
        topicQuestions.forEach(q => this.selectedQuestionIds.add(q.id));
        this.renderQuestionSelection();
        this.updateQuestionCount();
        this.updateGenerateButton();
      };
    }

    const clearBtn = document.getElementById('clearQuestionSelectionBtn');
    if (clearBtn) {
      clearBtn.onclick = () => {
        this.selectedQuestionIds.clear();
        this.renderQuestionSelection();
        this.updateQuestionCount();
        this.updateGenerateButton();
      };
    }

    this.updateQuestionCount();
  }

  updateQuestionCount() {
    const badge = document.getElementById('questionSelectionCount');
    if (badge) {
      const count = this.selectedQuestionIds.size;
      badge.textContent = `${count} question${count !== 1 ? 's' : ''} selected`;
    }
  }

  updateGenerateButton() {
    const btn = document.getElementById('generateBtn');
    if (btn) {
      btn.disabled = !this.selectedPreset || this.selectedQuestionIds.size === 0;
    }
  }

  async addToQueue() {
    const questionIds = Array.from(this.selectedQuestionIds);

    if (!this.selectedPreset || questionIds.length === 0) {
      alert('Please select a preset and at least one question');
      return;
    }

    try {
      const response = await fetch('/api/videos/queue/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          questionIds,
          presetId: this.selectedPreset,
          customName: this.videoName || null,
          batchSize: this.batchSize || 0,
          setId: this.selectedSetId || null
        })
      });

      const result = await response.json();

      if (result.success) {
        alert(`✅ Added to queue! Job ID: ${result.job.id}`);
        // Clear selection
        this.selectedQuestionIds.clear();
        this.renderQuestionSelection();
        this.updateGenerateButton();
      } else {
        alert(`❌ Error: ${result.error || 'Failed to add to queue'}`);
      }
    } catch (error) {
      console.error('Error adding to queue:', error);
      alert(`❌ Error: ${error.message}`);
    }
  }

  startQueuePolling() {
    // Poll queue status every 1 second for faster updates
    this.queuePollInterval = setInterval(() => this.updateQueueStatus(), 1000);
    this.updateQueueStatus(); // Initial update
  }

  stopQueuePolling() {
    if (this.queuePollInterval) {
      clearInterval(this.queuePollInterval);
      this.queuePollInterval = null;
    }
  }

  async updateQueueStatus() {
    try {
      const res = await fetch('/api/videos/queue/status');
      this.queueStatus = await res.json();

      // Debug logging
      if (this.queueStatus.current) {
        console.log('Queue status:', {
          jobId: this.queueStatus.current.id,
          progress: this.queueStatus.current.progress,
          completedBatches: this.queueStatus.current.completedBatches,
          totalBatches: this.queueStatus.current.totalBatches,
          status: this.queueStatus.current.status
        });
      }

      this.renderQueueStatus();

      // Refresh video list if there are completed jobs
      if (this.queueStatus.history.some(j => j.status === 'completed')) {
        await this.loadVideos();
        this.renderVideoList();
      }
    } catch (error) {
      console.error('Failed to update queue status:', error);
    }
  }

  renderQueueStatus() {
    const card = document.getElementById('queueStatusCard');
    const content = document.getElementById('queueStatusContent');

    if (!card || !content) return;

    const { current, queue } = this.queueStatus;
    const hasActivity = current || queue.length > 0;

    if (!hasActivity) {
      card.style.display = 'none';
      return;
    }

    card.style.display = 'block';

    let html = '';

    // Current job
    if (current) {
      html += `
        <div class="queue-job current-job" style="border-left: 4px solid #4f46e5; background: #f5f3ff; border-radius: 12px; padding: 20px; margin-bottom: 16px; border: 1px solid #e2e8f0; border-left-width: 4px;">
          <div class="queue-job-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
            <div style="display: flex; align-items: center; gap: 10px;">
              <span class="material-symbols-outlined" style="color: #4f46e5; animation: spin 2s linear infinite;">sync</span>
              <strong style="font-size: 16px; color: #1e293b;">${this.escapeHtml(current.customName || `Job ${current.id}`)}</strong>
            </div>
            <button class="icon-btn danger" onclick="window.videosComponent.cancelJob('${current.id}')" title="Cancel Generation">
              <span class="material-symbols-outlined">close</span>
            </button>
          </div>
          <div class="queue-job-progress">
            <div class="progress-bar-container" style="height: 12px; background: #e2e8f0; border-radius: 6px; overflow: hidden; margin-bottom: 10px; border: none; box-shadow: inset 0 1px 2px rgba(0,0,0,0.1);">
              <div class="progress-bar" style="width: ${current.progress || 0}%; background: linear-gradient(90deg, #4f46e5, #818cf8); height: 100%; transition: width 0.4s ease;"></div>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-size: 13px; color: #64748b; font-weight: 600;">Batch ${current.completedBatches || 0} of ${current.totalBatches || 0}</span>
              <span style="font-size: 15px; color: #4f46e5; font-weight: 800;">${current.progress || 0}%</span>
            </div>
          </div>
        </div>
      `;
    }

    // Queued jobs
    if (queue.length > 0) {
      html += `<div style="margin: 20px 0 12px; font-size: 13px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; display: flex; align-items: center; gap: 8px;">
        <span class="material-symbols-outlined" style="font-size: 18px;">list_alt</span>
        Next in Queue (${queue.length})
      </div>`;
      queue.forEach((job, index) => {
        html += `
          <div class="queue-job queued-job" style="padding: 16px; border: 1px solid #e2e8f0; background: #fff; margin-bottom: 10px; border-left: 4px solid #94a3b8; border-radius: 10px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
            <div class="queue-job-header" style="display: flex; justify-content: space-between; align-items: center;">
              <div style="display: flex; align-items: center; gap: 10px;">
                <span class="material-symbols-outlined" style="color: #94a3b8; font-size: 20px;">schedule</span>
                <span style="font-size: 14px; font-weight: 700; color: #334155;">${this.escapeHtml(job.customName || `Job ${job.id}`)}</span>
              </div>
              <button class="icon-btn" onclick="window.videosComponent.cancelJob('${job.id}')" title="Remove from Queue">
                <span class="material-symbols-outlined">delete</span>
              </button>
            </div>
            <div style="color: #94a3b8; font-size: 12px; margin-top: 6px; padding-left: 30px; display: flex; gap: 12px;">
              <span><strong>${job.questionIds?.length || 0}</strong> questions</span>
              <span>&bull;</span>
              <span><strong>${job.totalBatches}</strong> batch${job.totalBatches !== 1 ? 'es' : ''}</span>
            </div>
          </div>
        `;
      });
    }

    content.innerHTML = html;
  }

  async cancelJob(jobId) {
    if (!confirm('Are you sure you want to cancel this job?')) return;

    try {
      const res = await fetch(`/api/videos/queue/cancel/${jobId}`, {
        method: 'POST'
      });
      const result = await res.json();

      if (result.success) {
        alert('✅ Job cancelled');
        this.updateQueueStatus();
      } else {
        alert(`❌ Error: ${result.error}`);
      }
    } catch (error) {
      console.error('Error cancelling job:', error);
      alert(`❌ Error: ${error.message}`);
    }
  }

  getFilteredAndSortedVideos() {
    if (!Array.isArray(this.videos)) {
      return [];
    }

    let filtered = [...this.videos]; // Create a copy

    // Search filter
    if (this.searchQuery) {
      filtered = filtered.filter(v =>
        v.filename && v.filename.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    }

    // Sort
    filtered.sort((a, b) => {
      if (this.sortBy === 'date') {
        const dateA = new Date(a.created || 0);
        const dateB = new Date(b.created || 0);
        return this.sortOrder === 'desc' ? dateB - dateA : dateA - dateB;
      } else if (this.sortBy === 'name') {
        const nameA = a.filename.toLowerCase();
        const nameB = b.filename.toLowerCase();
        if (this.sortOrder === 'asc') {
          return nameA.localeCompare(nameB);
        } else {
          return nameB.localeCompare(nameA);
        }
      }
      return 0;
    });

    return filtered;
  }

  renderVideoList() {
    const container = document.getElementById('videoList');
    const paginationContainer = document.getElementById('videoPagination');

    if (!container) return;

    if (!Array.isArray(this.videos) || this.videos.length === 0) {
      container.innerHTML = '<p style="text-align: center; color: #666; padding: 40px;">No videos generated yet. Create your first video above!</p>';
      if (paginationContainer) paginationContainer.innerHTML = '';
      return;
    }

    const filteredVideos = this.getFilteredAndSortedVideos();

    if (!Array.isArray(filteredVideos) || filteredVideos.length === 0) {
      container.innerHTML = '<p style="color:#666; text-align: center; padding: 40px;">No videos found matching your search.</p>';
      if (paginationContainer) paginationContainer.innerHTML = '';
      return;
    }

    // Pagination
    const totalPages = Math.ceil(filteredVideos.length / this.itemsPerPage);
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    const pageVideos = filteredVideos.slice(startIndex, endIndex);

    container.innerHTML = `
      <div class="video-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px;">
        ${pageVideos.map(v => `
          <div class="video-card" style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06); transition: all 0.2s ease;">
            <div style="display: flex; align-items: flex-start; gap: 16px; margin-bottom: 16px;">
              <div style="width: 56px; height: 56px; border-radius: 12px; background: #eef2ff; display: flex; align-items: center; justify-content: center; color: #4f46e5; flex-shrink: 0;">
                <span class="material-symbols-outlined" style="font-size: 32px;">movie</span>
              </div>
              <div style="flex: 1; min-width: 0;">
                <div class="video-name" style="font-weight: 800; color: #1e293b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px; font-size: 16px;">${this.escapeHtml(v.filename)}</div>
                <div class="video-meta" style="font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 6px;">
                  <span class="material-symbols-outlined" style="font-size: 14px;">calendar_today</span>
                  ${new Date(v.created).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                </div>
              </div>
            </div>
            <div class="video-actions" style="display: flex; gap: 10px; padding-top: 16px; border-top: 1px solid #f1f5f9;">
              <a href="/generated-videos/${v.filename}" target="_blank" class="btn btn-secondary btn-small" style="flex: 1; justify-content: center; border-radius: 8px;">
                <span class="material-symbols-outlined" style="font-size: 18px;">visibility</span>
                View Video
              </a>
              <button class="icon-btn danger" onclick="window.videosComponent.deleteVideo('${v.filename}')" title="Delete Video" style="width: 36px; height: 36px; border-radius: 8px;">
                <span class="material-symbols-outlined">delete</span>
              </button>
            </div>
          </div>
        `).join('')}
      </div>
    `;

    // Render pagination
    if (paginationContainer && totalPages > 1) {
      this.renderPagination(paginationContainer, totalPages);
    } else if (paginationContainer) {
      paginationContainer.innerHTML = '';
    }
  }

  renderPagination(container, totalPages) {
    const maxButtons = 7;
    let startPage = Math.max(1, this.currentPage - Math.floor(maxButtons / 2));
    let endPage = Math.min(totalPages, startPage + maxButtons - 1);

    if (endPage - startPage < maxButtons - 1) {
      startPage = Math.max(1, endPage - maxButtons + 1);
    }

    let html = '<div class="pagination">';

    // Previous button
    html += `
      <button class="pagination-btn ${this.currentPage === 1 ? 'disabled' : ''}"
              onclick="window.videosComponent.goToPage(${this.currentPage - 1})"
              ${this.currentPage === 1 ? 'disabled' : ''}>
        ‹ Prev
      </button>
    `;

    // First page
    if (startPage > 1) {
      html += `<button class="pagination-btn" onclick="window.videosComponent.goToPage(1)">1</button>`;
      if (startPage > 2) {
        html += `<span class="pagination-ellipsis">...</span>`;
      }
    }

    // Page numbers
    for (let i = startPage; i <= endPage; i++) {
      html += `
        <button class="pagination-btn ${i === this.currentPage ? 'active' : ''}"
                onclick="window.videosComponent.goToPage(${i})">
          ${i}
        </button>
      `;
    }

    // Last page
    if (endPage < totalPages) {
      if (endPage < totalPages - 1) {
        html += `<span class="pagination-ellipsis">...</span>`;
      }
      html += `<button class="pagination-btn" onclick="window.videosComponent.goToPage(${totalPages})">${totalPages}</button>`;
    }

    // Next button
    html += `
      <button class="pagination-btn ${this.currentPage === totalPages ? 'disabled' : ''}"
              onclick="window.videosComponent.goToPage(${this.currentPage + 1})"
              ${this.currentPage === totalPages ? 'disabled' : ''}>
        Next ›
      </button>
    `;

    html += '</div>';
    container.innerHTML = html;
  }

  goToPage(page) {
    const filteredVideos = this.getFilteredAndSortedVideos();
    const totalPages = Math.ceil(filteredVideos.length / this.itemsPerPage);

    if (page < 1 || page > totalPages) return;

    this.currentPage = page;
    this.renderVideoList();
  }

  async deleteVideo(filename) {
    if (!confirm(`Are you sure you want to delete "${filename}"?`)) return;

    try {
      const res = await fetch(`/api/videos/${filename}`, {
        method: 'DELETE'
      });

      const result = await res.json();

      if (result.success) {
        alert('✅ Video deleted');
        await this.loadVideos();
        this.renderVideoList();
      } else {
        alert(`❌ Error: ${result.error}`);
      }
    } catch (error) {
      console.error('Error deleting video:', error);
      alert(`❌ Error: ${error.message}`);
    }
  }

  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
  }

  cleanup() {
    this.stopQueuePolling();
    this.cleanupSelect2();
  }

  cleanupSelect2() {
    // Cleanup Preset Select2
    if (this.presetSelect2 && window.$?.fn?.select2) {
      try {
        this.presetSelect2.off('change');
        this.presetSelect2.select2('destroy');
      } catch (error) {
        console.warn('Failed to cleanup preset select2:', error);
      } finally {
        this.presetSelect2 = null;
      }
    }

    // Cleanup Topic Select2
    if (this.topicSelect2 && window.$?.fn?.select2) {
      try {
        this.topicSelect2.off('change');
        this.topicSelect2.select2('destroy');
      } catch (error) {
        console.warn('Failed to cleanup topic select2:', error);
      } finally {
        this.topicSelect2 = null;
      }
    }
  }
}
