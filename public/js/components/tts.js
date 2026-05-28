class TTSComponent {
  constructor() {
    this.voices = [];
    this.selectedVoiceName = '';
    this.cacheData = [];
    this.languagePhrases = [];
    this.topics = [];
    this.questions = [];
    this.selectedTopicId = null;
    this.selectedQuestionIds = new Set();
    this.isGenerating = false;
    this.profileSelect2Instance = null;
    this.topicSelect2Instance = null;
    this.activeJobs = [];
    this.currentSetId = null;
    this.selectedSetEntries = new Set();
    // Voice Profiles picker state
    this.enabledVoices = [];        // voices saved by user (shown in dropdowns)
    this.allVoices = [];            // full voice catalog
    this.vpSearchLeft = '';         // search term for left panel
    this.vpSearchRight = '';        // search term for right panel
    this.vpLangFilter = '';         // language filter for left panel
    this.vpGenderFilter = '';       // gender filter for left panel
    this.languages = [];
  }

  async init(contentEl = null) {
    this.contentEl = contentEl || document.getElementById('content');
    window.ttsComponent = this; // Make accessible globally for onclick handlers

    this.startJobPolling();

    await this.checkTTSStatus();
    await this.loadVoices();
    await this.loadEnabledVoices();
    
    // Edge voices are persisted in the DB; this fills any missing Indian voices once.
    if (this.credentialsStatus?.provider === 'edge' && this.getVoicesForCurrentProvider().length === 0) {
      await this.autoPopulateAllEdgeVoices();
    }
    
    await this.loadCacheStats();
    await this.loadTopics();
    await this.loadQuestions();
    await this.loadProfiles();
    await this.loadLanguages();

    if (this.contentEl) {
      this.contentEl.innerHTML = this.render();
    }

    this.attachEventListeners();
    this.attachGenerateEventListeners();
    this.updateGenerateButton();
  }

  async loadLanguages() {
    try {
      const response = await fetch('/api/tts/languages');
      this.languages = await response.json();
    } catch (error) {
      console.error('Error loading languages:', error);
      this.languages = [];
    }
  }

  async loadEnabledVoices() {
    try {
      const response = await fetch('/api/tts/voice-catalog/enabled');
      if (response.ok) {
        this.enabledVoices = await response.json();
      }
    } catch (error) {
      console.error('Error loading enabled voices:', error);
      this.enabledVoices = [];
    }
  }

  async saveEnabledVoices() {
    try {
      const response = await fetch('/api/tts/voice-catalog/enabled', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ voices: this.enabledVoices })
      });
      return response.ok;
    } catch (error) {
      console.error('Error saving enabled voices:', error);
      return false;
    }
  }

  async autoPopulateAllEdgeVoices() {
    try {
      // Ensure the Edge voice catalog is loaded first — the async warmVoiceCatalog()
      // in the server constructor may not have finished by the time we get here,
      // leaving the catalog empty and causing auto-populate to return added=0.
      await this.loadVoices({ refresh: true, provider: 'edge' });

      const response = await fetch('/api/tts/voice-catalog/enabled/auto-populate-all-edge', {
        method: 'POST'
      });
      if (response.ok) {
        const result = await response.json();
        if (result.added > 0) {
          console.log(`Auto-populated ${result.added} Indian Edge TTS voices`);
          await this.loadEnabledVoices();
          this.refreshProfileDropdowns();
        }
      }
    } catch (error) {
      console.error('Error auto-populating Edge voices:', error);
    }
  }

  async loadProfiles() {
    try {
      const response = await fetch('/api/tts/profiles');
      this.profiles = await response.json();
    } catch (error) {
      console.error('Error loading profiles:', error);
      this.profiles = [];
    }
  }

  async checkTTSStatus() {
    try {
      const response = await fetch('/api/tts/status');
      const data = await response.json();
      this.ttsAvailable = data.available;
      this.googleAvailable = data.google_available;
      this.edgeAvailable = data.edge_available;
      this.ttsMessage = data.message;
      this.cacheStats = data.cache_stats;
      this.credentialsStatus = data.credentials || {};

      if (!this.ttsAvailable && this.credentialsStatus.provider === 'google') {
        this.showWarning(data.message);
      }
    } catch (error) {
      console.error('Error checking TTS status:', error);
      this.ttsAvailable = false;
    }
  }

  async loadVoices(options = {}) {
    const { refresh = false, provider = this.credentialsStatus?.provider || 'google' } = options;
    try {
      const response = await fetch(`/api/tts/voices?provider=${provider}${refresh ? '&refresh=true' : ''}`);

      if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        console.warn('Voices not available:', err?.message || err?.error || response.statusText);
        this.voices = [];
        this.selectedVoiceName = '';
        return;
      }

      const data = await response.json();
      const voices = data.voices || [];

      this.voices = voices;
      this.allVoices = voices; // keep full catalog for the picker

      const current = document.getElementById('ttsVoice')?.value || this.selectedVoiceName;
      if (current && this.voices.some(v => v.name === current)) {
        this.selectedVoiceName = current;
      } else {
        this.selectedVoiceName = this.voices[0]?.name || '';
      }
    } catch (error) {
      console.error('Error loading voices:', error);
      this.voices = [];
      this.selectedVoiceName = '';
    }
  }

  async loadCacheStats() {
    try {
      const response = await fetch('/api/tts/cache/stats');
      this.cacheStats = await response.json();
    } catch (error) {
      console.error('Error loading cache stats:', error);
    }
  }

  async loadCache(filters = {}) {
    try {
      const params = new URLSearchParams(filters);
      const response = await fetch(`/api/tts/cache?${params}`);
      this.cacheData = await response.json();
    } catch (error) {
      console.error('Error loading cache:', error);
    }
  }

  async loadTopics() {
    try {
      const response = await fetch('/api/topics');
      this.topics = await response.json();
    } catch (error) {
      console.error('Error loading topics:', error);
      this.topics = [];
    }
  }

  async loadQuestions() {
    try {
      const response = await fetch('/api/questions');
      const data = await response.json();
      this.questions = data.questions || data || [];
    } catch (error) {
      console.error('Error loading questions:', error);
      this.questions = [];
    }
  }

  async loadLanguagePhrases(language) {
    try {
      const response = await fetch(`/api/tts/phrases/${language}`);
      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error loading phrases:', error);
      return null;
    }
  }

  renderSetupWarning() {
    return `
      <div class="alert alert-warning" style="margin-bottom: 20px;">
        <h3>&#9888;&#65039; TTS Not Configured</h3>
        <p>${this.escapeHtml(this.ttsMessage || 'Google TTS credentials not configured.')}</p>
        <p>Please configure your Google Cloud credentials in the Settings tab to enable TTS functionality.</p>
      </div>
    `;
  }

  render() {
    const provider = this.credentialsStatus?.provider || 'google';
    const providerLabel = provider === 'google' ? 'Google Cloud' : 'Microsoft Edge';

    return `
      <div class="tts-container">
        <div class="tts-header">
          <h2>Text-to-Speech Management</h2>
          <div class="tts-status">
            ${this.ttsAvailable
              ? `<span class="status-badge status-success">${providerLabel} TTS Ready</span>`
              : `<span class="status-badge status-warning">${providerLabel} TTS Not Ready</span>`
            }
          </div>
        </div>

        ${(!this.ttsAvailable && provider === 'google') ? this.renderSetupWarning() : ''}

        <div class="tabs">
          <button class="tab-btn active" data-tab="generate">Generate</button>
          <button class="tab-btn" data-tab="sets">Sets</button>
          <button class="tab-btn" data-tab="phrases">Language Phrases</button>
          <button class="tab-btn" data-tab="voice-profiles">Voice Profiles</button>
          <button class="tab-btn" data-tab="settings">Settings</button>
        </div>

        <div class="tab-content active" id="tab-generate">
          ${this.renderGenerateTab()}
        </div>

        <div class="tab-content" id="tab-sets">
          ${this.renderSetsTab()}
        </div>

        <div class="tab-content" id="tab-phrases">
          ${this.renderPhrasesTab()}
        </div>

        <div class="tab-content" id="tab-voice-profiles">
          ${this.renderVoiceProfilesTab()}
        </div>

        <div class="tab-content" id="tab-settings">
          ${this.renderSettingsTab()}
        </div>
      </div>
    `;
  }

  renderGenerateTab() {
    return `
      <div class="tts-panel">
        <div class="tts-generate-steps">
          
          <!-- Step 1: Select Topic -->
          <div class="generate-step">
            <div class="step-header">
              <span class="step-number">1</span>
              <h3 class="step-title">Select Topic</h3>
            </div>
            <div class="form-group">
              <select id="ttsTopicSelect" class="form-control" style="width: 100%;">
                <option value="">Choose a topic...</option>
                ${this.topics.map(t => `<option value="${t.id}">${this.escapeHtml(t.name)} (${this.getQuestionsCount(t.id)} questions)</option>`).join('')}
              </select>
            </div>
          </div>

          <!-- Step 2: Select Questions -->
          <div id="ttsQuestionSelection" class="generate-step" style="display:none;">
            <div class="step-header">
              <span class="step-number">2</span>
              <h3 class="step-title">Select Questions</h3>
              <div class="step-actions">
                <button class="btn btn-secondary btn-small" id="ttsSelectAll">Select All</button>
                <button class="btn btn-secondary btn-small" id="ttsClearAll">Clear</button>
              </div>
            </div>
            <div id="ttsQuestionList" class="question-list"></div>
          </div>

          <!-- Step 3: Generation Options -->
          <div id="ttsGenerationOptions" class="generate-step" style="display:none;">
            <div class="step-header">
              <span class="step-number">3</span>
              <h3 class="step-title">TTS Configuration</h3>
            </div>

            <div class="form-group">
              <label class="form-label">
                <span>Voice Profile</span>
                <span class="voice-browser-link" style="font-size:0.85em; color:#666;">
                  ${this.getVoicesForCurrentProvider().length} ${this.credentialsStatus?.provider || 'google'} voice${this.getVoicesForCurrentProvider().length !== 1 ? 's' : ''} active
                </span>
              </label>
              <select id="ttsProfileSelect" class="form-control">
                <option value="">Select a voice profile...</option>
                ${this.getVoicesForCurrentProvider().map(v => {
                    const lang = (v.languageCodes || []).join(', ');
                    const gender = v.ssmlGender ? v.ssmlGender.charAt(0) + v.ssmlGender.slice(1).toLowerCase() : '';
                    return `<option value="${this.escapeHtml(v.name)}">${this.escapeHtml(v.name)} (${lang}${gender ? ' &middot; ' + gender : ''})</option>`;
                  }).join('')
                }
              </select>
              <small class="form-hint">
                ${this.getVoicesForCurrentProvider().length > 0
                  ? `${this.getVoicesForCurrentProvider().length} voice${this.getVoicesForCurrentProvider().length !== 1 ? 's' : ''} available &mdash; manage in the <strong>Voice Profiles</strong> tab`
                  : 'No voices selected. Go to the <strong>Voice Profiles</strong> tab to add voices to this dropdown.'
                }
              </small>
            </div>

            <div class="form-group">
              <label class="form-label">
                <span>Set Name (Optional)</span>
                <span class="label-hint">Naming this creates a NEW set you can pick when making videos</span>
              </label>
              <input type="text" id="ttsSetName" class="form-control" placeholder="e.g. Female Voice V1">
            </div>

            <div class="generate-action">
              <button class="btn btn-primary btn-large" id="ttsGenerateBtn" disabled>
                <span class="material-symbols-outlined">mic</span>
                Generate TTS Audio
              </button>
            </div>
          </div>

          <!-- Progress Display -->
          <div id="ttsProgressArea" class="progress-area" style="display:none;">
            <div class="progress-header">
              <div class="progress-title">
                <span class="material-symbols-outlined">sync</span>
                Generation in Progress...
              </div>
              <div id="ttsProgressPercent" class="progress-percentage">0%</div>
            </div>
            <div class="progress-bar-container">
              <div id="ttsProgressBar" class="progress-bar-fill" style="width: 0%"></div>
            </div>
            <div id="ttsProgressLog" class="progress-log"></div>
          </div>

        </div>
      </div>
    `;
  }

  getQuestionsCount(topicId) {
    return this.questions.filter(q => q.topic_id === topicId).length;
  }

  renderCacheTab() {
    const provider = this.credentialsStatus?.provider || 'google';

    // Group cache by topic, keeping only default versions (unless filtered by set)
    const byTopic = {};
    const orphanedEntries = [];
    
    const filteredCache = this.selectedCacheSetId
        ? this.cacheData.filter(e => e.set_id === this.selectedCacheSetId)
        : this.cacheData.filter(e => e.is_default);

    filteredCache.forEach(entry => {
      const topicId = entry.meta?.topic_id;
      
      if (topicId) {
        if (!byTopic[topicId]) {
          const topic = this.topics.find(t => t.id === topicId);
          byTopic[topicId] = {
            id: topicId,
            name: topic?.name || 'Unknown Topic',
            entries: []
          };
        }
        byTopic[topicId].entries.push(entry);
      } else {
        orphanedEntries.push(entry);
      }
    });

    return `
      <div class="tts-panel">
        <div class="cache-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
          <h3 style="margin:0;">TTS Cache Management</h3>
          <div style="display: flex; gap: 12px;">
            <select id="cacheSetFilter" class="form-control" style="width: 250px;">
                <option value="">All Default Audio</option>
                ${(this.sets || []).map(s => `<option value="${s.id}" ${this.selectedCacheSetId === s.id ? 'selected' : ''}>Set: ${this.escapeHtml(s.name)}</option>`).join('')}
            </select>
            <button class="btn btn-secondary btn-small" id="refreshCacheBtn">
                <span class="material-symbols-outlined" style="font-size: 18px;">refresh</span>
                Refresh
            </button>
          </div>
        </div>

        <div class="cache-stats" style="margin-bottom: 24px; padding: 20px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; display: flex; gap: 32px; flex-wrap: wrap;">
          <div>
            <div style="font-size: 11px; color: #64748b; text-transform: uppercase; font-weight: 700; margin-bottom: 4px;">Total Entries</div>
            <div style="font-size: 20px; font-weight: 800; color: #1e293b;">${this.cacheStats?.total || 0}</div>
          </div>
          <div>
            <div style="font-size: 11px; color: #64748b; text-transform: uppercase; font-weight: 700; margin-bottom: 4px;">Audio Length</div>
            <div style="font-size: 20px; font-weight: 800; color: #1e293b;">${(this.cacheStats?.total_duration || 0).toFixed(1)}s</div>
          </div>
          <div>
            <div style="font-size: 11px; color: #64748b; text-transform: uppercase; font-weight: 700; margin-bottom: 4px;">Disk Usage</div>
            <div style="font-size: 20px; font-weight: 800; color: #1e293b;">${this.formatBytes(this.cacheStats?.disk_usage || 0)}</div>
          </div>
        </div>

        <div id="cacheEntries">
          ${Object.values(byTopic).map(topic => this.renderTopicCacheGroup(topic)).join('')}
          ${orphanedEntries.length > 0 ? this.renderOrphanedCacheGroup(orphanedEntries) : ''}
          ${Object.values(byTopic).length === 0 && orphanedEntries.length === 0 ? `
            <div class="empty-state">
                <span class="material-symbols-outlined" style="font-size: 48px; color: #cbd5e1;">library_music</span>
                <p>No audio files found. ${this.selectedCacheSetId ? 'Try another set or ' : ''}generate some TTS audio first.</p>
            </div>
          ` : ''}
        </div>
      </div>
    `;
  }

  renderTopicCacheGroup(topic) {
    const entriesByCategory = {
      questions: topic.entries.filter(e => e.category === 'questions'),
      options: topic.entries.filter(e => e.category === 'options'),
      phrases: topic.entries.filter(e => e.category === 'phrases')
    };
    
    const totalEntries = topic.entries.length;
    const totalDuration = topic.entries.reduce((sum, e) => sum + (e.duration || 0), 0);
    
    return `
      <div class="topic-cache-group" style="margin-bottom: 24px; border: 2px solid #e0e0e0; border-radius: 12px; background: white; overflow: hidden;">
        <div style="padding: 16px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;">
          <h4 style="margin: 0; font-size: 1.1em; display: flex; justify-content: space-between; align-items: center;">
            <span>&#128218; ${this.escapeHtml(topic.name)}</span>
            <span style="font-size: 0.85em; font-weight: normal; opacity: 0.9;">
              ${totalEntries} entries &bull; ${totalDuration.toFixed(1)}s total
            </span>
          </h4>
        </div>
        
        <div style="padding: 16px;">
          ${entriesByCategory.questions.length > 0 ? `
            <div style="margin-bottom: 16px;">
              <h5 style="margin: 0 0 8px 0; color: #667eea; font-size: 0.95em;">
                &#10067; Questions (${entriesByCategory.questions.length})
              </h5>
              <div style="display: grid; gap: 8px;">
                ${entriesByCategory.questions.map(entry => this.renderCacheEntry(entry)).join('')}
              </div>
            </div>
          ` : ''}
          
          ${entriesByCategory.options.length > 0 ? `
            <div style="margin-bottom: 16px;">
              <h5 style="margin: 0 0 8px 0; color: #764ba2; font-size: 0.95em;">
                &#10004;&#65039; Options (${entriesByCategory.options.length})
              </h5>
              <div style="display: grid; gap: 8px;">
                ${entriesByCategory.options.map(entry => this.renderCacheEntry(entry)).join('')}
              </div>
            </div>
          ` : ''}
          
          ${entriesByCategory.phrases.length > 0 ? `
            <div>
              <h5 style="margin: 0 0 8px 0; color: #10b981; font-size: 0.95em;">
                &#128483;&#65039; Phrases (${entriesByCategory.phrases.length})
              </h5>
              <div style="display: grid; gap: 8px;">
                ${entriesByCategory.phrases.map(entry => this.renderCacheEntry(entry)).join('')}
              </div>
            </div>
          ` : ''}
        </div>
      </div>
    `;
  }

  renderOrphanedCacheGroup(entries) {
    const totalDuration = entries.reduce((sum, e) => sum + (e.duration || 0), 0);
    
    return `
      <div class="topic-cache-group" style="margin-bottom: 24px; border: 2px solid #fbbf24; border-radius: 12px; background: white; overflow: hidden;">
        <div style="padding: 16px; background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); color: white;">
          <h4 style="margin: 0; font-size: 1.1em; display: flex; justify-content: space-between; align-items: center;">
            <span>Other Entries (No Topic)</span>
            <span style="font-size: 0.85em; font-weight: normal; opacity: 0.9;">
              ${entries.length} entries ${totalDuration.toFixed(1)}s total
            </span>
          </h4>
        </div>
        
        <div style="padding: 16px;">
          <div style="display: grid; gap: 8px;">
            ${entries.map(entry => this.renderCacheEntry(entry)).join('')}
          </div>
        </div>
      </div>
    `;
  }

  renderCacheEntry(entry) {
    const profileName = this.profiles?.find(p => p.id === entry.profile_id)?.name || entry.profile_id;
    
    return `
      <div class="cache-entry" style="display: flex; align-items: center; gap: 12px; padding: 12px; background: #f9fafb; border-radius: 8px; border-left: 4px solid #10b981; transition: transform 0.1s;">
        <div style="flex: 1; min-width: 0;">
          <div style="font-size: 0.9em; font-weight: 600; color: #1e293b; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
            ${this.escapeHtml(entry.text.substring(0, 80))}${entry.text.length > 80 ? '...' : ''}
          </div>
          <div style="font-size: 0.75em; color: #64748b; display: flex; gap: 8px;">
            <span>${entry.language}</span>
            <span>&bull;</span>
            <span>${this.escapeHtml(profileName)}</span>
            <span>&bull;</span>
            <span>${entry.duration?.toFixed(2)}s</span>
          </div>
        </div>
        
        <div style="display: flex; gap: 6px; flex-shrink: 0;">
          <button class="play-btn-small" onclick="ttsComponent.playAudioInline(this, '${entry.audio_url}')" title="Play audio">
            <span class="material-symbols-outlined">play_arrow</span>
          </button>
          <button class="icon-btn danger" onclick="ttsComponent.deleteVersion('${entry.id}')" title="Delete">
            <span class="material-symbols-outlined">delete</span>
          </button>
        </div>
      </div>
    `;
  }

  playAudioInline(btn, url) {
    if (this.currentAudio) {
        this.currentAudio.pause();
        if (this.currentBtn) {
            this.currentBtn.querySelector('.material-symbols-outlined').textContent = 'play_arrow';
        }
    }

    if (this.currentAudioUrl === url) {
        this.currentAudioUrl = null;
        return;
    }

    const icon = btn.querySelector('.material-symbols-outlined');
    icon.textContent = 'pause';

    this.currentAudio = new Audio(url);
    this.currentAudioUrl = url;
    this.currentBtn = btn;

    this.currentAudio.onended = () => {
        icon.textContent = 'play_arrow';
        this.currentAudio = null;
        this.currentAudioUrl = null;
        this.currentBtn = null;
    };

    this.currentAudio.play().catch(e => {
        console.error('Audio play failed:', e);
        icon.textContent = 'play_arrow';
    });
  }

  formatBytes(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  renderPhrasesTab() {
    return `
      <div class="tts-panel">
        <h3>Language Phrases</h3>
        <div class="form-group">
            <select id="phrasesLanguageSelect" class="form-control">
                <option value="">Select Language...</option>
                ${this.renderLanguageOptions()}
            </select>
        </div>
        <div id="phrasesProfileArea" style="display:none; margin-bottom:20px;">
            <label>Voice Profile for Phrases</label>
            <select id="phrasesProfileSelect" class="form-control"></select>
        </div>
        <div id="phrasesContent"></div>
      </div>
    `;
  }

  renderVoiceProfilesTab() {
    const provider = this.credentialsStatus?.provider || 'google';
    const catalog = this.allVoices || [];
    const enabled = (this.enabledVoices || []).filter(v => (v.provider || 'google') === provider);
    const enabledNames = new Set(enabled.map(v => v.name));

    // Collect unique language codes from the full catalog
    const langSet = new Set(catalog.flatMap(v => v.languageCodes || []));
    const langOptions = [...langSet].sort().map(l =>
      `<option value="${l}" ${this.vpLangFilter === l ? 'selected' : ''}>${l}</option>`
    ).join('');

    // Filter left panel (available voices)
    let leftVoices = catalog.filter(v => !enabledNames.has(v.name));
    if (this.vpLangFilter) {
      leftVoices = leftVoices.filter(v => (v.languageCodes || []).includes(this.vpLangFilter));
    }
    if (this.vpGenderFilter) {
      leftVoices = leftVoices.filter(v => (v.ssmlGender || '').toUpperCase() === this.vpGenderFilter.toUpperCase());
    }
    if (this.vpSearchLeft) {
      const q = this.vpSearchLeft.toLowerCase();
      leftVoices = leftVoices.filter(v =>
        (v.name || '').toLowerCase().includes(q) ||
        (v.languageCodes || []).some(l => (l || '').toLowerCase().includes(q))
      );
    }

    // Filter right panel (enabled voices)
    let rightVoices = enabled;
    if (this.vpSearchRight) {
      const q = this.vpSearchRight.toLowerCase();
      rightVoices = rightVoices.filter(v =>
        (v.name || '').toLowerCase().includes(q) ||
        (v.languageCodes || []).some(l => (l || '').toLowerCase().includes(q))
      );
    }

    const noCatalog = catalog.length === 0;

    return `
      <div class="tts-panel">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
          <div>
            <h3 style="margin:0 0 4px 0;">Voice Profiles</h3>
            <p style="margin:0; color:#666; font-size:0.9em;">
              Move voices from the ${provider === 'edge' ? 'Edge TTS' : 'Google'} catalog (left) to your active list (right).
              Only voices on the right will appear in the Voice Profile dropdowns.
            </p>
          </div>
          <div style="display:flex; gap:8px;">
            <button class="btn btn-secondary" id="vpRefreshCatalogBtn" title="Refresh voice catalog${provider === 'edge' ? ' from Edge TTS' : ' from Google'}">
              <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="currentColor" style="vertical-align:middle; margin-right:4px;"><path d="M17.65 6.35A7.958 7.958 0 0 0 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0 1 12 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
              Refresh Catalog
            </button>
            <button class="btn btn-primary" id="vpSaveBtn">
              <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="currentColor" style="vertical-align:middle; margin-right:4px;"><path d="M17 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V7l-4-4zm-5 16a3 3 0 1 1 0-6 3 3 0 0 1 0 6zm3-10H5V5h10v4z"/></svg>
              Save Selection
            </button>
          </div>
        </div>

        ${noCatalog ? `
          <div style="padding:32px; text-align:center; background:#fff8e1; border-radius:10px; border:1px solid #ffe082;">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="#f59e0b" style="margin-bottom:12px;"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
            <h4 style="margin:0 0 8px 0;">No Voice Catalog Available</h4>
            <p style="color:#666; margin:0 0 16px 0;">The ${provider === 'edge' ? 'Edge TTS' : 'Google TTS'} voice catalog hasn't been loaded yet.${provider === 'edge' ? '' : ' You need to configure your Google credentials first, then'} click <strong>Refresh Catalog</strong> to fetch all available voices.</p>
            <button class="btn btn-primary" id="vpRefreshCatalogBtn2">
              <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="currentColor" style="vertical-align:middle; margin-right:4px;"><path d="M17.65 6.35A7.958 7.958 0 0 0 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0 1 12 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
              Refresh Catalog Now
            </button>
          </div>
        ` : `
          <!-- Filters row -->
          <div style="display:flex; gap:10px; margin-bottom:14px; flex-wrap:wrap; align-items:flex-end;">
            <div style="flex:1; min-width:140px;">
              <label style="font-size:0.82em; font-weight:600; color:#555; display:block; margin-bottom:4px;">Language</label>
              <select id="vpLangFilter" class="form-control" style="height:36px;">
                <option value="">All Languages</option>
                ${langOptions}
              </select>
            </div>
            <div style="min-width:130px;">
              <label style="font-size:0.82em; font-weight:600; color:#555; display:block; margin-bottom:4px;">Gender</label>
              <select id="vpGenderFilter" class="form-control" style="height:36px;">
                <option value="" ${!this.vpGenderFilter ? 'selected' : ''}>All Genders</option>
                <option value="FEMALE" ${this.vpGenderFilter === 'FEMALE' ? 'selected' : ''}>Female</option>
                <option value="MALE" ${this.vpGenderFilter === 'MALE' ? 'selected' : ''}>Male</option>
                <option value="NEUTRAL" ${this.vpGenderFilter === 'NEUTRAL' ? 'selected' : ''}>Neutral</option>
              </select>
            </div>
            <div style="flex:2; min-width:160px;">
              <label style="font-size:0.82em; font-weight:600; color:#555; display:block; margin-bottom:4px;">Search Available</label>
              <input type="text" id="vpSearchLeft" class="form-control" style="height:36px;" placeholder="Search by name or language..." value="${this.escapeHtml(this.vpSearchLeft)}">
            </div>
            <div style="flex:2; min-width:160px;">
              <label style="font-size:0.82em; font-weight:600; color:#555; display:block; margin-bottom:4px;">Search Selected</label>
              <input type="text" id="vpSearchRight" class="form-control" style="height:36px;" placeholder="Search selected voices..." value="${this.escapeHtml(this.vpSearchRight)}">
            </div>
          </div>

          <!-- Dual panel -->
          <div class="vp-dual-panel">
            <!-- LEFT: Available voices -->
            <div class="vp-panel">
              <div class="vp-panel-header">
                <span>Available Voices</span>
                <span class="vp-count">${leftVoices.length} voice${leftVoices.length !== 1 ? 's' : ''}</span>
              </div>
              <div class="vp-panel-body" id="vpLeftList">
                ${leftVoices.length === 0
                  ? '<div class="vp-empty">No voices match your filters</div>'
                  : leftVoices.map(v => this.renderVoiceCard(v, 'left')).join('')
                }
              </div>
              <div class="vp-panel-footer">
                <button class="btn btn-secondary btn-small" id="vpAddAllBtn" ${leftVoices.length === 0 ? 'disabled' : ''}>
                  Add All Visible &rarr;
                </button>
              </div>
            </div>

            <!-- Middle arrows -->
            <div class="vp-arrows">
              <button class="vp-arrow-btn" id="vpAddAllMidBtn" title="Add all visible to selection" ${leftVoices.length === 0 ? 'disabled' : ''}>
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
              </button>
              <button class="vp-arrow-btn" id="vpRemoveAllMidBtn" title="Remove all from selection" ${rightVoices.length === 0 ? 'disabled' : ''}>
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M16 19v-14l-11 7z"/></svg>
              </button>
            </div>

            <!-- RIGHT: Selected / enabled voices -->
            <div class="vp-panel">
              <div class="vp-panel-header vp-panel-header--right">
                <span>Your Active Voices</span>
                <span class="vp-count">${this.enabledVoices.length} selected</span>
              </div>
              <div class="vp-panel-body" id="vpRightList">
                ${rightVoices.length === 0
                  ? '<div class="vp-empty">No voices selected yet.<br>Move voices here to enable them in the dropdown.</div>'
                  : rightVoices.map(v => this.renderVoiceCard(v, 'right')).join('')
                }
              </div>
              <div class="vp-panel-footer">
                <button class="btn btn-danger btn-small" id="vpRemoveAllBtn" ${rightVoices.length === 0 ? 'disabled' : ''}>
                  &larr; Remove All
                </button>
              </div>
            </div>
          </div>

          <div id="vpSaveStatus" style="margin-top:12px; display:none;"></div>
        `}
      </div>
    `;
  }

  renderVoiceCard(voice, side) {
    const lang = (voice.languageCodes || []).join(', ') || 'Unknown';
    const gender = voice.ssmlGender || 'NEUTRAL';
    const genderColor = gender === 'FEMALE' ? '#ec4899' : gender === 'MALE' ? '#3b82f6' : '#6b7280';
    const genderIcon = gender === 'FEMALE'
      ? '<svg xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a5 5 0 1 0 0 10A5 5 0 0 0 12 2zm0 12c-5.33 0-8 2.67-8 4v2h16v-2c0-1.33-2.67-4-8-4z"/></svg>'
      : gender === 'MALE'
      ? '<svg xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a5 5 0 1 0 0 10A5 5 0 0 0 12 2zm0 12c-5.33 0-8 2.67-8 4v2h16v-2c0-1.33-2.67-4-8-4z"/></svg>'
      : '<svg xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/></svg>';

    // Derive voice type based on provider
    const voiceName = voice.name || '';
    const nameLower = voiceName.toLowerCase();
    let voiceType = 'Standard';
    if (voice.provider === 'edge') {
      if (nameLower.includes('neural')) voiceType = 'Neural';
    } else {
      if (nameLower.includes('studio')) voiceType = 'Studio';
      else if (nameLower.includes('neural2')) voiceType = 'Neural2';
      else if (nameLower.includes('news')) voiceType = 'News';
      else if (nameLower.includes('polyglot')) voiceType = 'Polyglot';
      else if (nameLower.includes('wavenet')) voiceType = 'WaveNet';
      else if (nameLower.includes('journey')) voiceType = 'Journey';
    }

    const typeColors = {
      Studio: '#7c3aed', Neural2: '#0891b2', WaveNet: '#059669',
      News: '#d97706', Polyglot: '#c026d3', Journey: '#e11d48', Neural: '#0ea5e9', Standard: '#6b7280'
    };
    const typeColor = typeColors[voiceType] || '#6b7280';

    const actionBtn = side === 'left'
      ? `<button class="vp-move-btn vp-move-btn--add" onclick="ttsComponent.addVoice('${this.escapeHtml(voice.name)}')" title="Add to active voices">
           <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
         </button>`
      : `<button class="vp-move-btn vp-move-btn--remove" onclick="ttsComponent.removeVoice('${this.escapeHtml(voice.name)}')" title="Remove from active voices">
           <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M19 13H5v-2h14v2z"/></svg>
         </button>`;

    return `
      <div class="vp-voice-card" data-name="${this.escapeHtml(voice.name)}">
        <div class="vp-voice-info">
          <div class="vp-voice-name">${this.escapeHtml(voice.name)}</div>
          <div class="vp-voice-meta">
            <span class="vp-badge" style="background:${typeColor}10; color:${typeColor}; border:1px solid ${typeColor}40;">${voiceType}</span>
            <span class="vp-badge" style="background:${genderColor}10; color:${genderColor}; border:1px solid ${genderColor}40;">${genderIcon} ${gender.charAt(0) + gender.slice(1).toLowerCase()}</span>
            <span style="font-size:0.78em; color:#888;">${lang}</span>
          </div>
        </div>
        ${actionBtn}
      </div>
    `;
  }

  addVoice(voiceName) {
    const voice = this.allVoices.find(v => v.name === voiceName);
    if (!voice) return;
    if (!this.enabledVoices.find(v => v.name === voiceName)) {
      const provider = this.credentialsStatus?.provider || 'google';
      this.enabledVoices.push({ ...voice, provider });
    }
    this.refreshVoiceProfilesTab();
  }

  removeVoice(voiceName) {
    this.enabledVoices = this.enabledVoices.filter(v => v.name !== voiceName);
    this.refreshVoiceProfilesTab();
  }

  addAllVisibleVoices() {
    const catalog = this.allVoices || [];
    const provider = this.credentialsStatus?.provider || 'google';
    const enabledNames = new Set(this.enabledVoices.map(v => v.name));
    let toAdd = catalog.filter(v => !enabledNames.has(v.name));
    if (this.vpLangFilter) toAdd = toAdd.filter(v => (v.languageCodes || []).includes(this.vpLangFilter));
    if (this.vpGenderFilter) toAdd = toAdd.filter(v => (v.ssmlGender || '').toUpperCase() === this.vpGenderFilter.toUpperCase());
    if (this.vpSearchLeft) {
      const q = this.vpSearchLeft.toLowerCase();
      toAdd = toAdd.filter(v => (v.name || '').toLowerCase().includes(q) || (v.languageCodes || []).some(l => (l || '').toLowerCase().includes(q)));
    }
    toAdd.forEach(v => this.enabledVoices.push({ ...v, provider: v.provider || provider }));
    this.refreshVoiceProfilesTab();
  }

  removeAllVisibleVoices() {
    let toRemove = this.enabledVoices;
    if (this.vpSearchRight) {
      const q = this.vpSearchRight.toLowerCase();
      const removeNames = new Set(toRemove
        .filter(v => (v.name || '').toLowerCase().includes(q) || (v.languageCodes || []).some(l => (l || '').toLowerCase().includes(q)))
        .map(v => v.name));
      this.enabledVoices = this.enabledVoices.filter(v => !removeNames.has(v.name));
    } else {
      this.enabledVoices = [];
    }
    this.refreshVoiceProfilesTab();
  }

  refreshProfileDropdowns() {
    const voices = this.getVoicesForCurrentProvider();
    const hint = voices.length > 0
      ? `${voices.length} voice${voices.length !== 1 ? 's' : ''} available &mdash; manage in the <strong>Voice Profiles</strong> tab`
      : 'Go to the <strong>Voice Profiles</strong> tab to select voices for this dropdown';

    const phrasesHint = voices.length > 0
      ? `${voices.length} voice${voices.length !== 1 ? 's' : ''} from Voice Profiles tab`
      : 'Configure voices in the Voice Profiles tab to populate this list';

    // Build option HTML for both dropdowns
    const generateOptions = `<option value="">Select a voice profile...</option>` +
      voices.map(v => {
        const lang = (v.languageCodes || []).join(', ');
        const gender = v.ssmlGender ? v.ssmlGender.charAt(0) + v.ssmlGender.slice(1).toLowerCase() : '';
        return `<option value="${this.escapeHtml(v.name)}">${this.escapeHtml(v.name)} (${lang}${gender ? ' &middot; ' + gender : ''})</option>`;
      }).join('');

    const phrasesOptions = `<option value="">Select profile...</option>` +
      voices.map(v => {
        const lang = (v.languageCodes || []).join(', ');
        return `<option value="${this.escapeHtml(v.name)}">${this.escapeHtml(v.name)} (${lang})</option>`;
      }).join('');

    // Update Generate tab dropdown
    const generateSelect = document.getElementById('ttsProfileSelect');
    if (generateSelect) {
      const prevVal = generateSelect.value;
      generateSelect.innerHTML = generateOptions;
      // Restore previous selection if still valid
      if (prevVal && generateSelect.querySelector(`option[value="${CSS.escape(prevVal)}"]`)) {
        generateSelect.value = prevVal;
      }
      // Notify Select2 of the change so it re-renders its UI
      if (window.$ && window.$.fn?.select2) {
        try { window.$(generateSelect).trigger('change.select2'); } catch(e) {}
      }
      // Update hint text below
      const hintEl = generateSelect.nextElementSibling;
      if (hintEl && hintEl.tagName === 'SMALL') {
        hintEl.innerHTML = hint;
      }
    }

    // Update Phrases tab dropdown
    const phrasesSelect = document.getElementById('phrasesProfileSelect');
    if (phrasesSelect) {
      const prevVal = phrasesSelect.value;
      phrasesSelect.innerHTML = phrasesOptions;
      if (prevVal && phrasesSelect.querySelector(`option[value="${CSS.escape(prevVal)}"]`)) {
        phrasesSelect.value = prevVal;
      }
      const hintEl = phrasesSelect.nextElementSibling;
      if (hintEl && hintEl.tagName === 'SMALL') {
        hintEl.innerHTML = phrasesHint;
      }
    }

    // Mark Generate and Phrases tabs as needing refresh when next visited
    this._generateTabDirty = true;
    this._phrasesTabDirty = true;
  }



  refreshVoiceProfilesTab() {
    const tabContent = document.getElementById('tab-voice-profiles');
    if (tabContent) {
      tabContent.innerHTML = this.renderVoiceProfilesTab();
      this.attachVoiceProfilesEventListeners();
    }
  }

  attachVoiceProfilesEventListeners() {
    // Refresh catalog button
    ['vpRefreshCatalogBtn', 'vpRefreshCatalogBtn2'].forEach(id => {
      const btn = document.getElementById(id);
      if (btn) {          btn.addEventListener('click', async () => {
          btn.disabled = true;
          btn.textContent = 'Loading...';
          const provider = this.credentialsStatus?.provider || 'google';
          await this.loadVoices({ refresh: true, provider });
          this.refreshVoiceProfilesTab();
        });
      }
    });

    // Save button
    const saveBtn = document.getElementById('vpSaveBtn');
    if (saveBtn) {
      saveBtn.addEventListener('click', async () => {
        saveBtn.disabled = true;
        saveBtn.textContent = 'Saving...';
        const ok = await this.saveEnabledVoices();
        const status = document.getElementById('vpSaveStatus');
        if (status) {
          status.style.display = 'block';
          if (ok) {
            status.innerHTML = '<div style="padding:10px 14px; background:#d1fae5; border-radius:6px; border-left:4px solid #10b981; color:#065f46;">&#10003; Voice selection saved! The dropdowns in Generate and Language Phrases tabs now reflect your selection.</div>';
          } else {
            status.innerHTML = '<div style="padding:10px 14px; background:#fee2e2; border-radius:6px; border-left:4px solid #ef4444; color:#7f1d1d;">&#10007; Failed to save. Please try again.</div>';
          }
          setTimeout(() => { status.style.display = 'none'; }, 4000);
        }
        // Re-render to update dropdowns in all tabs
        await this.loadEnabledVoices();
        this.refreshVoiceProfilesTab();
        this.refreshProfileDropdowns();
      });
    }

    // Filter / search inputs
    const langFilter = document.getElementById('vpLangFilter');
    if (langFilter) {
      langFilter.addEventListener('change', e => { this.vpLangFilter = e.target.value; this.refreshVoiceProfilesTab(); });
    }
    const genderFilter = document.getElementById('vpGenderFilter');
    if (genderFilter) {
      genderFilter.addEventListener('change', e => { this.vpGenderFilter = e.target.value; this.refreshVoiceProfilesTab(); });
    }
    const searchLeft = document.getElementById('vpSearchLeft');
    if (searchLeft) {
      searchLeft.addEventListener('input', e => { this.vpSearchLeft = e.target.value; this.refreshVoiceProfilesTab(); });
    }
    const searchRight = document.getElementById('vpSearchRight');
    if (searchRight) {
      searchRight.addEventListener('input', e => { this.vpSearchRight = e.target.value; this.refreshVoiceProfilesTab(); });
    }

    // Add all / Remove all buttons
    ['vpAddAllBtn', 'vpAddAllMidBtn'].forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', () => this.addAllVisibleVoices());
    });
    ['vpRemoveAllBtn', 'vpRemoveAllMidBtn'].forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', () => this.removeAllVisibleVoices());
    });
  }

  renderSettingsTab() {
    const credStatus = this.credentialsStatus || {};
    const provider = credStatus.provider || 'google';
    
    return `
      <div class="tts-panel">
        <h3>TTS Provider &amp; Configuration</h3>

        <div class="form-group" style="margin-bottom: 24px; padding: 16px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px;">
          <label class="form-label" style="font-weight: 600;">Active TTS Provider</label>
          <div style="display: flex; gap: 16px; margin-top: 8px;">
            <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
              <input type="radio" name="tts_provider" value="google" ${provider === 'google' ? 'checked' : ''}>
              <span>Google Cloud TTS (Paid)</span>
            </label>
            <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
              <input type="radio" name="tts_provider" value="edge" ${provider === 'edge' ? 'checked' : ''}>
              <span>Microsoft Edge TTS (Free)</span>
            </label>
          </div>
          <p style="margin-top: 12px; font-size: 0.85em; color: #64748b;">
            ${provider === 'google' 
              ? 'Google Cloud TTS requires a service account JSON. It offers high-quality Wavenet and Neural2 voices.' 
              : 'Microsoft Edge TTS is free and requires no configuration. It uses high-quality neural voices from Microsoft.'}
          </p>
          <button class="btn btn-secondary btn-small" id="switchProviderBtn" style="margin-top: 8px;">Switch Provider</button>
        </div>

        ${provider === 'google' ? `
          <!-- Credentials Status -->
          <div style="margin: 20px 0; padding: 16px; background: ${credStatus.configured ? '#e8f5e9' : '#fff3e0'}; border-radius: 8px; border-left: 4px solid ${credStatus.configured ? '#4caf50' : '#ff9800'};">
            <h4 style="margin:0 0 12px 0;">${credStatus.configured ? '&#10004; Credentials Configured' : '&#9888; Credentials Not Configured'}</h4>
            <div style="font-size: 0.95em; display: grid; gap: 6px;">
              <div><strong>Project ID:</strong> ${credStatus.project_id || 'N/A'}</div>
              <div><strong>Status:</strong> ${credStatus.configured ? 'Active' : 'Inactive'}</div>
              <div><strong>Source:</strong> ${credStatus.source === 'db' ? 'Database' : credStatus.source === 'env' ? 'Environment Variable' : 'None'}</div>
              ${credStatus.updated_at ? `<div><strong>Last Updated:</strong> ${new Date(credStatus.updated_at).toLocaleString()}</div>` : ''}
            </div>
          </div>

          <!-- Credentials Configuration -->
          <div style="margin: 20px 0;">
            <h4 style="margin: 0 0 8px 0;">Paste Your Credentials JSON</h4>
            <div class="form-group">
              <textarea id="credentialsInput" class="form-control credentials-textarea" rows="10" placeholder="Paste your Google Cloud service account JSON here...">${credStatus.configured && credStatus.source === 'db' ? '(JSON credentials saved)' : ''}</textarea>
            </div>

            <div style="display: flex; gap: 12px; margin-top: 12px; flex-wrap: wrap;">
              <button class="btn btn-primary" id="saveCredentialsBtn">Save Credentials</button>
              <button class="btn btn-secondary" id="testCredentialsBtn" ${!credStatus.configured ? 'disabled' : ''}>Test Credentials</button>
              ${credStatus.configured ? `<button class="btn btn-danger" id="removeCredentialsBtn">Remove Credentials</button>` : ''}
            </div>
          </div>
        ` : `
          <div style="margin: 20px 0; padding: 16px; background: #e0f2fe; border-radius: 8px; border-left: 4px solid #0ea5e9;">
            <h4 style="margin:0 0 8px 0;">&#10004; Microsoft Edge TTS Ready</h4>
            <p style="margin:0; font-size: 0.9em; color: #0369a1;">No configuration required. Edge TTS is ready to use for all Indian languages.</p>
          </div>
        `}
      </div>
    `;
  }

  renderLanguageOptions() {
    if (!this.languages || !Array.isArray(this.languages) || this.languages.length === 0) {
      return '<option value="">No languages configured</option>';
    }
    return this.languages.map(lang =>
      `<option value="${lang.code}">${this.escapeHtml(lang.name)} ${lang.native_name ? '(' + this.escapeHtml(lang.native_name) + ')' : ''}</option>`
    ).join('');
  }

  renderProfilesList() {
    if (!this.profiles || this.profiles.length === 0) {
      return '<p style="color: #888; padding: 20px; text-align: center;">No profiles configured. Click "Add New Profile" to create one.</p>';
    }

    return `
      <div style="display: grid; gap: 12px;">
        ${this.profiles.map(profile => `
          <div style="padding: 12px; border: 1px solid #ddd; border-radius: 8px; background: white; display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-weight: bold;">${this.escapeHtml(profile.name)} ${profile.is_default ? '<span style="padding: 2px 8px; background: #4caf50; color: white; border-radius: 3px; font-size: 0.75em; margin-left: 8px;">DEFAULT</span>' : ''}</div>
              <div style="font-size: 0.9em; color: #666; margin-top: 4px;">
                ${profile.language} | ${profile.voice} | Rate: ${profile.speaking_rate || 1.0} | Pitch: ${profile.pitch || 0}
              </div>
            </div>
            <div style="display: flex; gap: 8px;">
              <button class="btn btn-small btn-secondary tts-icon-btn" onclick="ttsComponent.editProfile('${profile.id}')" title="Edit profile">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
              </button>
              <button class="btn btn-small btn-danger tts-icon-btn" onclick="ttsComponent.deleteProfile('${profile.id}')" title="Delete profile">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
              </button>
            </div>
          </div>
        `).join('')}
      </div>
    `;
  }

  attachEventListeners() {
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        btn.classList.add('active');
        const tabContent = document.getElementById(`tab-${btn.dataset.tab}`);
        tabContent.classList.add('active');
        
        // Load data when switching tabs
        if (btn.dataset.tab === 'cache') {
          await this.loadCache();
          tabContent.innerHTML = this.renderCacheTab();
          this.attachCacheEventListeners();
        } else if (btn.dataset.tab === 'generate') {
          tabContent.innerHTML = this.renderGenerateTab();
          this.attachGenerateEventListeners();
          this._generateTabDirty = false;
        } else if (btn.dataset.tab === 'phrases') {
          tabContent.innerHTML = this.renderPhrasesTab();
          this.attachPhrasesEventListeners();
          this._phrasesTabDirty = false;
        } else if (btn.dataset.tab === 'voice-profiles') {
          tabContent.innerHTML = this.renderVoiceProfilesTab();
          this.attachVoiceProfilesEventListeners();
        } else if (btn.dataset.tab === 'settings') {
           await this.loadProfiles();
           tabContent.innerHTML = this.renderSettingsTab();
           this.attachSettingsEventListeners();
         } else if (btn.dataset.tab === 'sets') {
           await this.loadSets();
           tabContent.innerHTML = this.renderSetsTab();
         }
       });
     });
  }

  async deleteSet(setId) {
    if (!confirm('Are you sure? This will delete the set and ALL associated audio files from disk.')) return;
    try {
      const response = await fetch(`/api/tts/sets/${setId}`, { method: 'DELETE' });
      if (response.ok) {
        await this.loadSets();
        const tabContent = document.getElementById('tab-sets');
        if (tabContent) tabContent.innerHTML = this.renderSetsTab();
        await this.checkTTSStatus();
      }
    } catch (e) { console.error(e); }
  }

  async loadSets() {
    try {
      const response = await fetch('/api/tts/sets');
      this.sets = await response.json();
    } catch (error) {
      console.error('Error loading sets:', error);
      this.sets = [];
    }
  }

  renderSetsTab() {
    if (this.currentSetId) return this.renderSetDetails(this.currentSetId);
    const sets = this.sets || [];
    return `
      <div class="tts-panel">
        <div class="panel-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
          <h3 style="margin: 0;">TTS Generation Sets</h3>
          <button class="btn btn-secondary btn-small" onclick="ttsComponent.loadSets().then(() => ttsComponent.refreshSetsTab())">Refresh</button>
        </div>
        <div class="sets-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px;">
          ${sets.length === 0 ? '<p>No sets found.</p>' : sets.map(set => `
            <div class="tts-set-card" onclick="ttsComponent.openSet('${set.id}')" style="cursor: pointer; background: white; border: 1px solid #ddd; padding: 16px; border-radius: 8px;">
              <div class="set-name" style="font-weight:bold;">${this.escapeHtml(set.name)}</div>
              <div style="font-size: 12px; color: #666;">${set.item_count} files &middot; ${set.provider}</div>
              <button class="btn btn-danger btn-small" style="margin-top:10px;" onclick="event.stopPropagation(); ttsComponent.deleteSet('${set.id}')">Delete</button>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  }

  async openSet(setId) {
      this.currentSetId = setId;
      this.selectedSetEntries = new Set();
      await this.loadCache({ set_id: setId });
      this.refreshSetsTab();
  }

  renderSetDetails(setId) {
      const set = this.sets.find(s => s.id === setId);
      const entries = this.cacheData || [];
      return `
        <div class="tts-panel">
            <div style="display:flex; align-items:center; gap:10px; margin-bottom:20px;">
                <button class="btn btn-secondary" onclick="ttsComponent.currentSetId=null; ttsComponent.refreshSetsTab()">Back</button>
                <h3>Set: ${this.escapeHtml(set?.name)}</h3>
                <button class="btn btn-danger" id="delSelBtn" ${this.selectedSetEntries.size === 0 ? 'disabled' : ''} onclick="ttsComponent.deleteSelectedEntries()">Delete Selected (${this.selectedSetEntries.size})</button>
            </div>
            <div class="set-entries-list">
                ${entries.map(e => `
                    <div style="display:flex; align-items:center; gap:10px; padding:10px; border-bottom:1px solid #eee;">
                        <input type="checkbox" onchange="ttsComponent.toggleEntrySelection('${e.id}')" ${this.selectedSetEntries.has(e.id) ? 'checked' : ''}>
                        <div style="flex:1;">${this.escapeHtml(e.text)}</div>
                        <button class="play-btn-small" onclick="ttsComponent.playAudioInline(this, '${e.audio_url}')">Play</button>
                    </div>
                `).join('')}
            </div>
        </div>
      `;
  }

  toggleEntrySelection(id) {
      if (this.selectedSetEntries.has(id)) this.selectedSetEntries.delete(id);
      else this.selectedSetEntries.add(id);
      this.refreshSetsTab();
  }

  async deleteSelectedEntries() {
      if (!confirm('Delete selected?')) return;
      await fetch('/api/tts/cache', {
          method: 'DELETE',
          headers: {'Content-Type':'application/json'},
          body: JSON.stringify({ ids: Array.from(this.selectedSetEntries) })
      });
      this.selectedSetEntries.clear();
      await this.loadCache({ set_id: this.currentSetId });
      this.refreshSetsTab();
  }

  refreshSetsTab() {
      const el = document.getElementById('tab-sets');
      if (el) el.innerHTML = this.renderSetsTab();
  }

  attachGenerateEventListeners() {
    const topicSelect = document.getElementById('ttsTopicSelect');
    if (topicSelect) {
        const $ts = window.$(topicSelect);
        $ts.select2({ width: '100%' });
        $ts.on('change', (e) => {
            this.selectedTopicId = e.target.value;
            this.selectedQuestionIds.clear();
            this.renderQuestionSelection();
        });
        this.topicSelect2Instance = $ts;
    }

    const generateBtn = document.getElementById('ttsGenerateBtn');
    if (generateBtn) {
      generateBtn.addEventListener('click', () => this.generateTTS());
    }
  }

  attachCacheEventListeners() {
    const refreshBtn = document.getElementById('refreshCacheBtn');
    if (refreshBtn) {
      refreshBtn.addEventListener('click', async () => {
        await this.loadCache();
        await this.loadCacheStats();
        this.refreshCacheTab();
      });
    }

    const setFilter = document.getElementById('cacheSetFilter');
    if (setFilter) {
        setFilter.addEventListener('change', (e) => {
            this.selectedCacheSetId = e.target.value || null;
            this.refreshCacheTab();
        });
    }
  }

  refreshCacheTab() {
    const tabContent = document.getElementById('tab-cache');
    if (tabContent && tabContent.classList.contains('active')) {
      tabContent.innerHTML = this.renderCacheTab();
      this.attachCacheEventListeners();
    }
  }

  attachPhrasesEventListeners() {
    const languageSelect = document.getElementById('phrasesLanguageSelect');
    if (languageSelect) {
      languageSelect.addEventListener('change', async (e) => {
        await this.loadPhrases(e.target.value);
      });
    }

    const refreshBtn = document.getElementById('refreshPhrasesBtn');
    if (refreshBtn) {
      refreshBtn.addEventListener('click', async () => {
        const language = document.getElementById('phrasesLanguageSelect')?.value;
        if (language) {
          await this.loadPhrases(language);
        }
      });
    }

    const saveBtn = document.getElementById('savePhrasesBtn');
    if (saveBtn) {
      saveBtn.addEventListener('click', () => this.savePhrases());
    }

    const generateBtn = document.getElementById('generatePhrasesBtn');
    if (generateBtn) {
      generateBtn.addEventListener('click', () => this.generatePhrasesTTS());
    }
  }

  attachSettingsEventListeners() {
    const switchBtn = document.getElementById('switchProviderBtn');
    if (switchBtn) {
      switchBtn.addEventListener('click', async () => {
        const provider = document.querySelector('input[name="tts_provider"]:checked').value;
        switchBtn.disabled = true;
        switchBtn.textContent = 'Switching...';
        
        try {
          const response = await fetch('/api/tts/credentials', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ provider })
          });
          
          if (response.ok) {
            await this.checkTTSStatus();
            await this.loadVoices({ refresh: true, provider });
            await this.loadEnabledVoices();
            
            // Auto-populate persisted Indian Edge voices when switching to Edge.
            if (provider === 'edge' && this.getVoicesForCurrentProvider().length === 0) {
              await this.autoPopulateAllEdgeVoices();
            }
            
            const tabContent = document.getElementById('tab-settings');
            if (tabContent) {
              tabContent.innerHTML = this.renderSettingsTab();
              this.attachSettingsEventListeners();
            }
            
            // Update the status badge in the header
            const statusBadge = document.querySelector('.tts-status');
            if (statusBadge) {
              const providerLabel = provider === 'google' ? 'Google Cloud' : 'Microsoft Edge';
              statusBadge.innerHTML = this.ttsAvailable
                ? `<span class="status-badge status-success">${providerLabel} TTS Ready</span>`
                : `<span class="status-badge status-warning">${providerLabel} TTS Not Ready</span>`;
            }
          }
        } catch (error) {
          console.error('Error switching provider:', error);
          alert('Error switching provider');
        } finally {
          if (switchBtn) {
            switchBtn.disabled = false;
            switchBtn.textContent = 'Switch Provider';
          }
        }
      });
    }

    const saveCredBtn = document.getElementById('saveCredentialsBtn');
    if (saveCredBtn) {
      saveCredBtn.addEventListener('click', () => this.saveCredentials());
    }

    const testCredBtn = document.getElementById('testCredentialsBtn');
    if (testCredBtn) {
      testCredBtn.addEventListener('click', () => this.testCredentials());
    }

    const removeCredBtn = document.getElementById('removeCredentialsBtn');
    if (removeCredBtn) {
      removeCredBtn.addEventListener('click', () => this.removeCredentials());
    }

    const addProfileBtn = document.getElementById('addProfileBtn');
    if (addProfileBtn) {
      addProfileBtn.addEventListener('click', () => this.showAddProfileDialog());
    }
  }

  renderQuestionSelection() {
    const container = document.getElementById('ttsQuestionSelection');
    const optionsContainer = document.getElementById('ttsGenerationOptions');
    const list = document.getElementById('ttsQuestionList');
    const profileSelect = document.getElementById('ttsProfileSelect');
    
    if (!container || !list) return;

    if (!this.selectedTopicId) {
      container.style.display = 'none';
      if (optionsContainer) optionsContainer.style.display = 'none';
      return;
    }

    container.style.display = 'block';
    if (optionsContainer) optionsContainer.style.display = 'block';
    
    const questions = this.questions.filter(q => q.topic_id === this.selectedTopicId);

    list.innerHTML = questions.map(q => `
      <label>
        <input type="checkbox" data-id="${q.id}">
        <span>${this.escapeHtml(q.question)}</span>
      </label>
    `).join('') || '<p style="text-align: center; color: #888; padding: 20px;">No questions found in this topic.</p>';

    // Populate profile select with select2
    if (profileSelect) {
      this.populateProfileSelect(profileSelect, this.getVoicesForCurrentProvider());
      
      // Initialize select2 if available
      if (window.$ && window.$.fn?.select2) {
        const $select = window.$(profileSelect);
        
        // Destroy existing select2 instance if any
        if ($select.hasClass('select2-hidden-accessible')) {
          $select.select2('destroy');
        }
        
        // Initialize select2 with search
        $select.select2({
          placeholder: 'Select a voice profile...',
          allowClear: true,
          width: '100%'
        });
        
        // Add change event listener
        $select.on('change', () => {
          this.updateGenerateButton();
        });
        
        // Store instance for cleanup
        this.profileSelect2Instance = $select;
      } else {
        // Fallback: regular change listener
        profileSelect.addEventListener('change', () => {
          this.updateGenerateButton();
        });
      }
    }

    list.querySelectorAll('input[type="checkbox"]').forEach(cb => {
      cb.addEventListener('change', () => {
        if (cb.checked) this.selectedQuestionIds.add(cb.dataset.id);
        else this.selectedQuestionIds.delete(cb.dataset.id);
        this.updateGenerateButton();
      });
    });

    const selectAll = document.getElementById('ttsSelectAll');
    const clearAll = document.getElementById('ttsClearAll');
    if (selectAll) {
      selectAll.onclick = () => {
        list.querySelectorAll('input[type="checkbox"]').forEach(cb => {
          cb.checked = true;
          this.selectedQuestionIds.add(cb.dataset.id);
        });
        this.updateGenerateButton();
      };
    }
    if (clearAll) {
      clearAll.onclick = () => {
        list.querySelectorAll('input[type="checkbox"]').forEach(cb => {
          cb.checked = false;
        });
        this.selectedQuestionIds.clear();
        this.updateGenerateButton();
      };
    }

    this.updateGenerateButton();
  }

  updateGenerateButton() {
    const btn = document.getElementById('ttsGenerateBtn');
    if (!btn) return;
    
    const hasQuestions = this.selectedQuestionIds.size > 0;
    const profileId = document.getElementById('ttsProfileSelect')?.value;
    
    btn.disabled = !hasQuestions || !profileId;
    
    const micIcon = `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="currentColor" style="vertical-align:middle;"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3zm5.3 10a5.3 5.3 0 0 1-10.6 0H5a7 7 0 0 0 6 6.92V21h2v-3.08A7 7 0 0 0 19 11h-1.7z"/></svg>`;
    if (hasQuestions && profileId) {
      btn.innerHTML = `<span class="btn-icon">${micIcon}</span> Generate TTS for ${this.selectedQuestionIds.size} Question${this.selectedQuestionIds.size > 1 ? 's' : ''}`;
    } else {
      btn.innerHTML = `<span class="btn-icon">${micIcon}</span> Generate TTS Audio`;
    }
  }

  async generateTTS() {
    if (this.selectedQuestionIds.size === 0) return;
    
    const selectedValue = document.getElementById('ttsProfileSelect')?.value;
    
    if (!selectedValue) {
      alert('Please select a voice profile');
      return;
    }

    const enabledVoice = this.enabledVoices.find(v => v.name === selectedValue);
    const selectedProfile = enabledVoice ? null : (this.profiles || []).find(p => p.id === selectedValue);

    const profileId = enabledVoice ? null : selectedValue;
    const voiceName = enabledVoice ? enabledVoice.name : (selectedProfile?.voiceName || null);
    const language = enabledVoice
      ? ((enabledVoice.languageCodes || [])[0] || 'en-US')
      : (selectedProfile?.language || 'en-US');
    
    const questionIds = Array.from(this.selectedQuestionIds);
    const setName = document.getElementById('ttsSetName')?.value || null;
    
    // Show progress area
    const progressArea = document.getElementById('ttsProgressArea');
    const progressLog = document.getElementById('ttsProgressLog');
    const progressBar = document.getElementById('ttsProgressBar');
    const progressPercent = document.getElementById('ttsProgressPercent');

    if (progressArea) progressArea.style.display = 'block';
    if (progressLog) progressLog.innerHTML = '';
    
    const addLog = (msg, type = 'info') => {
        const p = document.createElement('p');
        const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        p.innerHTML = `<span class="log-time">[${time}]</span> <span class="log-msg-${type}">${msg}</span>`;
        progressLog.appendChild(p);
        progressLog.scrollTop = progressLog.scrollHeight;
    };

    const updateProgress = (pct) => {
        progressBar.style.width = `${pct}%`;
        progressPercent.textContent = `${Math.round(pct)}%`;
    };

    try {
      this.isGenerating = true;
      const generateBtn = document.getElementById('ttsGenerateBtn');
      if (generateBtn) generateBtn.disabled = true;
      
      addLog('Initializing generation...', 'info');
      updateProgress(5);

      let result;

      addLog(`Creating TTS Set: "${setName || 'Default'}"...`, 'info');
      const response = await fetch('/api/tts/jobs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          language, voiceName, profileId, provider: enabledVoice?.provider,
          setName: setName || `Set_${Date.now()}`, questionIds,
          generateQuestions: true,
          generateOptions: true,
          generateCorrectOnly: false
        })
      });

      if (!response.ok) throw new Error('Failed to start TTS job');
      updateProgress(10);
      addLog('Job started successfully! It will process in the background.', 'success');
      this.startJobPolling();
      
      await this.loadCacheStats();
      setTimeout(() => { if (progressArea) progressArea.style.display = 'none'; }, 5000);
      
    } catch (error) {
      addLog(`Error: ${error.message}`, 'error');
    } finally {
      this.isGenerating = false;
      this.updateGenerateButton();
    }
  }

  async playAudio(audioUrl) {
    if (this.currentAudio) {
      this.currentAudio.pause();
    }
    this.currentAudio = new Audio(audioUrl);
    this.currentAudio.play().catch(err => {
      console.error('Error playing audio:', err);
      alert('Error playing audio');
    });
  }

  async setDefaultVersion(cacheId) {
    try {
      const response = await fetch(`/api/tts/cache/${cacheId}/set-default`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' }
      });
      
      if (response.ok) {
        // Refresh cache display
        await this.loadCache();
        const tabContent = document.getElementById('tab-cache');
        if (tabContent && tabContent.classList.contains('active')) {
          tabContent.innerHTML = this.renderCacheTab();
          this.attachCacheEventListeners();
        }
      } else {
        alert('Error setting default version');
      }
    } catch (error) {
      console.error('Error setting default version:', error);
      alert('Error setting default version');
    }
  }

  async deleteVersion(cacheId) {
    if (!confirm('Are you sure you want to delete this TTS version?')) {
      return;
    }
    
    try {
      const response = await fetch('/api/tts/cache', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ids: [cacheId] })
      });
      
      if (response.ok) {
        // Refresh cache display
        await this.loadCache();
        await this.loadCacheStats();
        const tabContent = document.getElementById('tab-cache');
        if (tabContent && tabContent.classList.contains('active')) {
          tabContent.innerHTML = this.renderCacheTab();
          this.attachCacheEventListeners();
        }
      } else {
        alert('Error deleting version');
      }
    } catch (error) {
      console.error('Error deleting version:', error);
      alert('Error deleting version');
    }
  }

  async loadPhrases(lang) {
      const res = await fetch(`/api/tts/phrases/${lang}`);
      const data = await res.json();
      const cacheRes = await fetch('/api/tts/cache?category=phrases');
      const cache = await cacheRes.json();
      
      const content = document.getElementById('phrasesContent');
      const profileArea = document.getElementById('phrasesProfileArea');
      if (profileArea) profileArea.style.display = 'block';

      const voices = this.getVoicesForCurrentProvider().filter(v => (v.languageCodes || []).includes(lang));
      const pSelect = document.getElementById('phrasesProfileSelect');
      if (pSelect) {
          pSelect.innerHTML = voices.map(v => `<option value="${v.name}">${v.name}</option>`).join('');
      }

      content.innerHTML = Object.entries(data.phrases || {}).map(([key, text]) => {
          const audio = cache.find(c => c.text === text && c.language === lang);
          return `
            <div style="display:flex; align-items:center; gap:10px; padding:10px; background:#f9fafb; margin-bottom:8px; border-radius:8px;">
                <div style="flex:1;"><strong>${key}:</strong> ${this.escapeHtml(text)}</div>
                <div id="phrase-actions-${key}" style="display:flex; gap:5px; align-items:center;">
                    ${audio ? `
                        <button class="play-btn-small" onclick="ttsComponent.playAudioInline(this, '${audio.audio_url}')">Play</button>
                        <button class="icon-btn danger" onclick="ttsComponent.deletePhraseAudio('${audio.id}', '${lang}')">Delete</button>
                    ` : `
                        <div class="circular-progress" id="progress-${key}" style="display:none;"></div>
                        <button class="icon-btn" onclick="ttsComponent.regeneratePhrase('${key}', '${lang}')">Generate</button>
                    `}
                </div>
            </div>
          `;
      }).join('');
  }

  async regeneratePhrase(key, lang) {
      const voice = document.getElementById('phrasesProfileSelect').value;
      if (!voice) return alert('Select voice');
      const progress = document.getElementById(`progress-${key}`);
      if (progress) progress.style.display = 'inline-block';
      
      const text = (await (await fetch(`/api/tts/phrases/${lang}`)).json()).phrases[key];

      await fetch('/api/tts/generate', {
          method: 'POST',
          headers: {'Content-Type':'application/json'},
          body: JSON.stringify({ text, language: lang, voiceName: voice, category: 'phrases' })
      });
      await this.loadPhrases(lang);
  }

  async deletePhraseAudio(id, lang) {
      await fetch('/api/tts/cache', {
          method: 'DELETE',
          headers: {'Content-Type':'application/json'},
          body: JSON.stringify({ ids: [id] })
      });
      await this.loadPhrases(lang);
  }

  async savePhrases() {
    const language = document.getElementById('phrasesLanguageSelect')?.value;
    if (!language) return;
    
    const phrases = {};
    document.querySelectorAll('#phrasesContent input[data-phrase-key]').forEach(input => {
      phrases[input.dataset.phraseKey] = input.value;
    });
    
    try {
      const response = await fetch(`/api/tts/phrases/${language}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phrases })
      });
      
      if (response.ok) {
        alert('Phrases saved successfully!');
      } else {
        alert('Error saving phrases');
      }
    } catch (error) {
      console.error('Error saving phrases:', error);
      alert('Error saving phrases');
    }
  }

  async generatePhrasesTTS() {
    const language = document.getElementById('phrasesLanguageSelect')?.value;
    const selectedValue = document.getElementById('phrasesProfileSelect')?.value;
    
    if (!language || !selectedValue) {
      alert('Please select a language and voice profile');
      return;
    }

    // Resolve enabled voice vs legacy profile
    const enabledVoice = this.enabledVoices.find(v => v.name === selectedValue);
    const phraseProfileId = enabledVoice ? null : selectedValue;
    const phraseVoiceName = enabledVoice ? enabledVoice.name : null;
    
    try {
      const response = await fetch(`/api/tts/phrases/${language}/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ profileId: phraseProfileId, voiceName: phraseVoiceName, provider: enabledVoice?.provider })
      });
      
      if (response.ok) {
        const result = await response.json();
        alert(`TTS generated for ${result.results?.success || 0} phrases!`);
      } else {
        alert('Error generating TTS');
      }
    } catch (error) {
      console.error('Error generating TTS:', error);
      alert('Error generating TTS');
    }
  }

  // Settings methods
  async saveCredentials() {
    const input = document.getElementById('credentialsInput')?.value;
    if (!input) {
      alert('Please paste your credentials JSON');
      return;
    }
    
    try {
      const credentials = JSON.parse(input);
      const response = await fetch('/api/tts/credentials', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ credentials })
      });
      
      if (response.ok) {
        alert('Credentials saved successfully!');
        await this.checkTTSStatus();
        const tabContent = document.getElementById('tab-settings');
        if (tabContent && tabContent.classList.contains('active')) {
          tabContent.innerHTML = this.renderSettingsTab();
          this.attachSettingsEventListeners();
        }
      } else {
        const error = await response.json();
        alert('Error saving credentials: ' + error.error);
      }
    } catch (error) {
      console.error('Error saving credentials:', error);
      alert('Invalid JSON format or error saving credentials');
    }
  }

  async testCredentials() {
    try {
      const response = await fetch('/api/tts/credentials/test', {
        method: 'POST'
      });
      
      const result = await response.json();
      if (result.valid) {
        alert('Credentials are valid and working!');
      } else {
        alert('Credentials test failed: ' + result.message);
      }
    } catch (error) {
      console.error('Error testing credentials:', error);
      alert('Error testing credentials');
    }
  }

  async removeCredentials() {
    if (!confirm('Are you sure you want to remove the configured credentials?')) {
      return;
    }
    
    try {
      const response = await fetch('/api/tts/credentials', {
        method: 'DELETE'
      });
      
      if (response.ok) {
        alert('Credentials removed successfully');
        await this.checkTTSStatus();
        const tabContent = document.getElementById('tab-settings');
        if (tabContent && tabContent.classList.contains('active')) {
          tabContent.innerHTML = this.renderSettingsTab();
          this.attachSettingsEventListeners();
        }
      } else {
        alert('Error removing credentials');
      }
    } catch (error) {
      console.error('Error removing credentials:', error);
      alert('Error removing credentials');
    }
  }

  showAddProfileDialog() {
    // Create a modal dialog for adding profile
    const modal = document.createElement('div');
    modal.style.cssText = 'position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 10000;';
    
    modal.innerHTML = `
      <div style="background: white; padding: 24px; border-radius: 12px; width: 90%; max-width: 500px; max-height: 90vh; overflow-y: auto;">
        <h3 style="margin: 0 0 20px 0;">&#127897;&#65039; Create New Voice Profile</h3>
        
        <div class="form-group">
          <label class="form-label">
            Profile Name
            <span style="color: #ef4444;">*</span>
          </label>
          <input type="text" id="profileName" class="form-control" placeholder="e.g., Hindi Male, English Female" required>
          <small class="form-hint">Give this profile a descriptive name</small>
        </div>

        <div class="form-group">
          <label class="form-label">
            Language Code
            <span style="color: #ef4444;">*</span>
          </label>
          <select id="profileLanguage" class="form-control">
            ${this.renderLanguageOptions()}
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">
            Google Voice Name
            <span style="color: #ef4444;">*</span>
            <a href="https://cloud.google.com/text-to-speech/docs/voices" target="_blank" style="float: right; color: #3b82f6; font-size: 0.9em; text-decoration: none;">
              &#128218; Browse Voices &rarr;
            </a>
          </label>
          <input type="text" id="profileVoice" class="form-control" placeholder="e.g., en-US-Wavenet-A, hi-IN-Wavenet-B" required>
          <small class="form-hint">Copy the voice name from Google's voice list</small>
        </div>

        <div class="form-group">
          <label class="form-label">
            Speaking Rate (Optional)
          </label>
          <input type="number" id="profileRate" class="form-control" min="0.25" max="4.0" step="0.1" value="1.0" placeholder="1.0">
          <small class="form-hint">0.25 (slow) to 4.0 (fast). Default: 1.0</small>
        </div>

        <div class="form-group">
          <label class="form-label">
            Pitch (Optional)
          </label>
          <input type="number" id="profilePitch" class="form-control" min="-20" max="20" step="0.5" value="0" placeholder="0">
          <small class="form-hint">-20 (low) to +20 (high). Default: 0</small>
        </div>

        <div style="margin-top: 24px; display: flex; gap: 12px; justify-content: flex-end;">
          <button class="btn btn-secondary" id="cancelProfileBtn">Cancel</button>
          <button class="btn btn-primary" id="createProfileBtn">&#10004; Create Profile</button>
        </div>
      </div>
    `;
    
    document.body.appendChild(modal);
    
    // Focus first input
    setTimeout(() => document.getElementById('profileName')?.focus(), 100);
    
    // Handle cancel
    modal.querySelector('#cancelProfileBtn').addEventListener('click', () => {
      document.body.removeChild(modal);
    });
    
    // Handle create
    modal.querySelector('#createProfileBtn').addEventListener('click', () => {
      const name = document.getElementById('profileName')?.value.trim();
      const language = document.getElementById('profileLanguage')?.value;
      const voice = document.getElementById('profileVoice')?.value.trim();
      const speaking_rate = parseFloat(document.getElementById('profileRate')?.value) || 1.0;
      const pitch = parseFloat(document.getElementById('profilePitch')?.value) || 0;
      
      if (!name || !language || !voice) {
        alert('Please fill in all required fields (marked with *)');
        return;
      }
      
      this.createProfile({ name, language, voice, speaking_rate, pitch });
      document.body.removeChild(modal);
    });
    
    // Handle click outside
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        document.body.removeChild(modal);
      }
    });
  }

  async createProfile(profileData) {
    try {
      const response = await fetch('/api/tts/profiles', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(profileData)
      });
      
      if (response.ok) {
        alert('Profile created successfully!');
        await this.loadProfiles();
        const tabContent = document.getElementById('tab-settings');
        if (tabContent && tabContent.classList.contains('active')) {
          tabContent.innerHTML = this.renderSettingsTab();
          this.attachSettingsEventListeners();
        }
      } else {
        alert('Error creating profile');
      }
    } catch (error) {
      console.error('Error creating profile:', error);
      alert('Error creating profile');
    }
  }

  async editProfile(profileId) {
    const profile = this.profiles?.find(p => p.id === profileId);
    if (!profile) return;
    
    // Create a modal dialog for editing profile
    const modal = document.createElement('div');
    modal.style.cssText = 'position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 10000;';
    
    modal.innerHTML = `
      <div style="background: white; padding: 24px; border-radius: 12px; width: 90%; max-width: 500px; max-height: 90vh; overflow-y: auto;">
        <h3 style="margin: 0 0 20px 0;">&#9999;&#65039; Edit Voice Profile</h3>
        
        <div class="form-group">
          <label class="form-label">
            Profile Name
            <span style="color: #ef4444;">*</span>
          </label>
          <input type="text" id="editProfileName" class="form-control" value="${this.escapeHtml(profile.name)}" required>
        </div>

        <div class="form-group">
          <label class="form-label">
            Language Code
            <span style="color: #ef4444;">*</span>
          </label>
          <select id="editProfileLanguage" class="form-control">
            ${this.languages?.map(lang => 
              `<option value="${lang.code}" ${lang.code === profile.language ? 'selected' : ''}>${this.escapeHtml(lang.name)}</option>`
            ).join('') || `<option value="${profile.language}">${profile.language}</option>`}
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">
            Google Voice Name
            <span style="color: #ef4444;">*</span>
            <a href="https://cloud.google.com/text-to-speech/docs/voices" target="_blank" style="float: right; color: #3b82f6; font-size: 0.9em; text-decoration: none;">
              &#128218; Browse Voices &rarr;
            </a>
          </label>
          <input type="text" id="editProfileVoice" class="form-control" value="${this.escapeHtml(profile.voice)}" required>
        </div>

        <div class="form-group">
          <label class="form-label">Speaking Rate</label>
          <input type="number" id="editProfileRate" class="form-control" min="0.25" max="4.0" step="0.1" value="${profile.speaking_rate || 1.0}">
          <small class="form-hint">0.25 (slow) to 4.0 (fast)</small>
        </div>

        <div class="form-group">
          <label class="form-label">Pitch</label>
          <input type="number" id="editProfilePitch" class="form-control" min="-20" max="20" step="0.5" value="${profile.pitch || 0}">
          <small class="form-hint">-20 (low) to +20 (high)</small>
        </div>

        <div style="margin-top: 24px; display: flex; gap: 12px; justify-content: flex-end;">
          <button class="btn btn-secondary" id="cancelEditBtn">Cancel</button>
          <button class="btn btn-primary" id="saveEditBtn">&#10004; Save Changes</button>
        </div>
      </div>
    `;
    
    document.body.appendChild(modal);
    
    // Handle cancel
    modal.querySelector('#cancelEditBtn').addEventListener('click', () => {
      document.body.removeChild(modal);
    });
    
    // Handle save
    modal.querySelector('#saveEditBtn').addEventListener('click', async () => {
      const name = document.getElementById('editProfileName')?.value.trim();
      const language = document.getElementById('editProfileLanguage')?.value;
      const voice = document.getElementById('editProfileVoice')?.value.trim();
      const speaking_rate = parseFloat(document.getElementById('editProfileRate')?.value) || 1.0;
      const pitch = parseFloat(document.getElementById('editProfilePitch')?.value) || 0;
      
      if (!name || !language || !voice) {
        alert('Please fill in all required fields');
        return;
      }
      
      try {
        const response = await fetch(`/api/tts/profiles/${profileId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name, language, voice, speaking_rate, pitch })
        });
        
        if (response.ok) {
          alert('Profile updated successfully!');
          await this.loadProfiles();
          const tabContent = document.getElementById('tab-settings');
          if (tabContent && tabContent.classList.contains('active')) {
            tabContent.innerHTML = this.renderSettingsTab();
            this.attachSettingsEventListeners();
          }
          document.body.removeChild(modal);
        } else {
          alert('Error updating profile');
        }
      } catch (error) {
        console.error('Error updating profile:', error);
        alert('Error updating profile');
      }
    });
    
    // Handle click outside
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        document.body.removeChild(modal);
      }
    });
  }

  async deleteProfile(profileId) {
    if (!confirm('Are you sure you want to delete this profile?')) {
      return;
    }
    
    try {
      const response = await fetch(`/api/tts/profiles/${profileId}`, {
        method: 'DELETE'
      });
      
      if (response.ok) {
        alert('Profile deleted successfully!');
        await this.loadProfiles();
        const tabContent = document.getElementById('tab-settings');
        if (tabContent && tabContent.classList.contains('active')) {
          tabContent.innerHTML = this.renderSettingsTab();
          this.attachSettingsEventListeners();
        }
      } else {
        alert('Error deleting profile');
      }
    } catch (error) {
      console.error('Error deleting profile:', error);
      alert('Error deleting profile');
    }
  }

  // Language management methods
  showWarning(message) {
    console.warn(message);
  }

  cleanupSelect2() {
    if (this.profileSelect2Instance && window.$?.fn?.select2) {
      try {
        this.profileSelect2Instance.off('change');
        this.profileSelect2Instance.select2('destroy');
      } catch (error) {
        console.warn('Failed to cleanup profile select2 instance', error);
      } finally {
        this.profileSelect2Instance = null;
      }
    }
    if (this.topicSelect2Instance && window.$?.fn?.select2) {
      try {
        this.topicSelect2Instance.off('change');
        this.topicSelect2Instance.select2('destroy');
      } catch (error) {
        console.warn('Failed to cleanup topic select2 instance', error);
      } finally {
        this.topicSelect2Instance = null;
      }
    }
  }

  cleanup() {
      if (this.jobPollInterval) {
          clearInterval(this.jobPollInterval);
          this.jobPollInterval = null;
      }
      this.cleanupSelect2();
  }

  populateProfileSelect(selectElement, voices) {
    if (!voices || voices.length === 0) {
      selectElement.innerHTML = '<option value="">No voice profiles available - add voices in the Voice Profiles tab</option>';
      return;
    }

    selectElement.innerHTML = '<option value="">Select a voice profile...</option>' +
      voices.map(v => {
        const lang = (v.languageCodes || []).join(', ');
        const gender = v.ssmlGender ? v.ssmlGender.charAt(0) + v.ssmlGender.slice(1).toLowerCase() : '';
        return `<option value="${this.escapeHtml(v.name)}">${this.escapeHtml(v.name)} (${lang}${gender ? ' - ' + gender : ''})</option>`;
      }).join('');
  }

  /**
   * Get enabled voices filtered by the current TTS provider.
   * When using Edge TTS, only Edge voices show up in dropdowns.
   * When using Google, only Google voices show up.
   */
  getVoicesForCurrentProvider() {
    const provider = this.credentialsStatus?.provider || 'google';
    return (this.enabledVoices || []).filter(v => (v.provider || 'google') === provider);
  }

  startJobPolling() {
      if (this.jobPollInterval) clearInterval(this.jobPollInterval);
      this.jobPollInterval = setInterval(async () => {
          const res = await fetch('/api/tts/jobs');
          if (!res.ok) return;
          const jobs = await res.json();
          this.activeJobs = jobs.filter(j => j.status === 'running' || j.status === 'queued');
          this.updateProgressUI();
      }, 1000);
  }

  updateProgressUI() {
      const area = document.getElementById('ttsProgressArea');
      if (!this.activeJobs.length) {
          // If was active and now not, maybe hide after a delay
          return;
      }
      area.style.display = 'block';
      const job = this.activeJobs[0];
      const progressBar = document.getElementById('ttsProgressBar');
      const progressPercent = document.getElementById('ttsProgressPercent');
      const progressLog = document.getElementById('ttsProgressLog');

      if (progressBar) progressBar.style.width = `${job.progress.percentage}%`;
      if (progressPercent) progressPercent.textContent = `${Math.round(job.progress.percentage)}%`;
      if (progressLog) progressLog.innerHTML = `<p>Processing ${job.progress.completed_items} of ${job.progress.total_items} items...</p>`;
  }

  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
  }
}

export { TTSComponent };
