/**
 * Simple Data Management Component
 * Topics -> Questions
 */

import { HierarchyForms } from './hierarchy-forms.js';

export class HierarchyComponent {
  constructor(contentEl) {
    this.contentEl = contentEl;
    this.topics = [];
    this.questions = [];
    this.selectedTopicId = null;
    this.selectedSetId = null;
    this.formsHelper = new HierarchyForms(this);
  }

  async render() {
    try {
      await Promise.all([
        this.loadTopics(),
        this.loadQuestions(),
        this.loadSets()
      ]);
      this.renderContent();
    } catch (error) {
      console.error('Error rendering data management:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load data. ${error.message}
        </div>
      `;
    }
  }

  async loadTopics() {
    const response = await fetch('/api/topics');
    this.topics = await response.json();
  }

  async loadQuestions() {
    const response = await fetch('/api/questions');
    const data = await response.json();
    this.questions = Array.isArray(data.questions) ? data.questions : data;

    // Also load TTS cache to find playable audio
    try {
        let url = '/api/tts/cache';
        if (this.selectedSetId) {
          url += `?set_id=${this.selectedSetId}`;
        }
        const ttsRes = await fetch(url);
        this.ttsCache = await ttsRes.json();
    } catch (e) {
        this.ttsCache = [];
    }
  }

  async loadSets() {
    try {
      const response = await fetch('/api/tts/sets');
      this.ttsSets = await response.json();
    } catch (error) {
      this.ttsSets = [];
    }
  }

  renderContent() {
    this.cleanupTopicSelect();
    this.cleanupSetSelect();
    this.contentEl.innerHTML = `
      <div class="content-header">
        <div>
          <h1 class="content-title">Data Management</h1>
          <p class="content-description">Topics &rarr; Questions</p>
        </div>
        <div class="header-actions" style="display: flex; gap: 10px; align-items: center;" id="setFilterContainer">
          <!-- Set filter rendered here -->
        </div>
      </div>

      <div class="card mb-20">
        <div class="card-body">
          <div class="topic-select-row">
            <label class="form-label" style="margin:0;">Topic</label>
            <select id="topicSelect" class="form-control" style="min-width: 240px;">
              <option value="">Select topic...</option>
              ${this.topics.map(t => `<option value="${t.id}" ${this.selectedTopicId === t.id ? 'selected' : ''}>${this.escapeHtml(t.name)}</option>`).join('')}
            </select>
            <button class="btn btn-primary" id="addTopicBtn">+ Add Topic</button>
          </div>
          <div class="topic-action-row">
            <button class="btn btn-secondary" id="addQuestionBtn" ${this.selectedTopicId ? '' : 'disabled'}>+ Add Question</button>
          </div>
        </div>
      </div>

      <div id="dataList"></div>

      <div class="hier-modal-overlay" id="hierModal" style="display:none">
        <div class="hier-modal">
          <div class="hier-modal-header">
            <div class="hier-modal-title" id="hierModalTitle"></div>
            <button class="btn btn-sm btn-secondary" type="button" id="hierModalClose">&times;</button>
          </div>
          <div class="hier-modal-body" id="hierModalBody"></div>
        </div>
      </div>
    `;

    this.attachEventListeners();
    this.renderSetFilter();
    this.renderList();
  }

  renderSetFilter() {
      const container = document.getElementById('setFilterContainer');
      if (!container) return;

      this.cleanupSetSelect();

      let sets = this.ttsSets || [];
      if (this.selectedTopicId) {
          sets = sets.filter(s => String(s.topic_id) === String(this.selectedTopicId));
      }

      container.innerHTML = `
        <label class="form-label" style="margin:0; font-size: 0.9em; color: #666;">Audio Set Filter:</label>
        <select id="hierarchySetFilter" class="form-control" style="width: 200px;">
          <option value="">Default Audio</option>
          ${sets.map(s => `<option value="${s.id}" ${this.selectedSetId === s.id ? 'selected' : ''}>${this.escapeHtml(s.name)}</option>`).join('')}
        </select>
      `;

      const setFilter = document.getElementById('hierarchySetFilter');
      if (setFilter) {
          if (window.$ && window.$.fn?.select2) {
              const $setSelect = window.$(setFilter);
              $setSelect.select2({ width: '200px', placeholder: 'Default Audio' });
              $setSelect.on('change', async (e) => {
                  this.selectedSetId = e.target.value || null;
                  await this.loadQuestions();
                  this.renderList();
              });
              this.setSelect2Instance = $setSelect;
          } else {
              setFilter.addEventListener('change', async (e) => {
                  this.selectedSetId = e.target.value || null;
                  await this.loadQuestions();
                  this.renderList();
              });
          }
      }
  }

  attachEventListeners() {

    const topicSelect = document.getElementById('topicSelect');
    if (topicSelect) {
      const handleTopicChange = async (value) => {
        this.selectedTopicId = value || null;
        this.selectedSetId = null; // Reset set filter when topic changes
        this.updateAddQuestionButton();
        this.renderSetFilter(); // Refresh sets for new topic
        await this.loadQuestions();
        this.renderList();
      };

      topicSelect.addEventListener('change', (e) => handleTopicChange(e.target.value));

      if (window.$ && window.$.fn?.select2) {
        const $select = window.$(topicSelect);
        $select.select2({
          width: 'style',
          placeholder: 'Select topic...'
        });
        $select.on('change', (event) => handleTopicChange(event.target.value));
        this.select2Instance = $select;
      }
    }

    const addTopicBtn = document.getElementById('addTopicBtn');
    if (addTopicBtn) {
      addTopicBtn.addEventListener('click', () => this.openTopicForm('create'));
    }

    const addQuestionBtn = document.getElementById('addQuestionBtn');
    if (addQuestionBtn) {
      addQuestionBtn.addEventListener('click', () => this.openQuestionForm('create'));
    }

    const modal = document.getElementById('hierModal');
    const modalClose = document.getElementById('hierModalClose');
    if (modal && modalClose) {
      modalClose.addEventListener('click', () => this.closeModal());
      modal.addEventListener('click', (e) => {
        if (e.target === modal) this.closeModal();
      });
    }
  }

  updateAddQuestionButton() {
    const addQuestionBtn = document.getElementById('addQuestionBtn');
    if (addQuestionBtn) {
      addQuestionBtn.disabled = !this.selectedTopicId;
    }
  }

  renderList() {
    const container = document.getElementById('dataList');
    if (!container) return;

    if (!this.selectedTopicId) {
      container.innerHTML = `
        <div class="empty-state">
          <p>Select a topic to view questions.</p>
        </div>
      `;
      return;
    }

    const topic = this.topics.find(t => t.id === this.selectedTopicId);
    const questions = this.questions.filter(q => q.topic_id === this.selectedTopicId);

    container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <span class="card-title">${this.escapeHtml(topic?.name || 'Topic')}</span>
          <div class="topic-actions topic-actions-header">
            <a class="icon-btn" href="/api/csv/sample?topicId=${this.selectedTopicId}" title="Download sample CSV">
              <span class="material-symbols-outlined">description</span>
            </a>
            <a class="icon-btn" href="/api/csv/export?topicId=${this.selectedTopicId}" title="Export CSV">
              <span class="material-symbols-outlined">download</span>
            </a>
            <button class="icon-btn" id="importCsvBtn" type="button" title="Import CSV">
              <span class="material-symbols-outlined">upload</span>
            </button>
            <button class="icon-btn" id="editTopicBtn" type="button" title="Edit topic">
              <span class="material-symbols-outlined">edit</span>
            </button>
            <button class="icon-btn danger" id="deleteTopicBtn" type="button" title="Delete topic">
              <span class="material-symbols-outlined">delete</span>
            </button>
            <input type="file" id="importCsvInput" accept=".csv" style="display:none;">
          </div>
        </div>
        <div class="card-body">
          ${questions.length === 0 ? `
            <div class="empty-state">
              <p>No questions in this topic yet.</p>
            </div>
          ` : `
            <div class="questions-list">
              ${questions.map((q, idx) => {
                const questionAudio = this.getAudioUrl(q.question, 'questions');
                return `
                <div class="question-card">
                  <div class="question-card-header">
                    <div class="question-card-main">
                      <div class="question-badge">Question #${idx + 1}</div>
                      <div class="question-text">
                        ${this.escapeHtml(q.question)}
                        ${questionAudio ? `
                          <button class="play-btn-small" onclick="hierarchyComponent.playAudio(this, '${questionAudio}')" title="Play question audio">
                            <span class="material-symbols-outlined">play_arrow</span>
                          </button>
                        ` : ''}
                      </div>
                    </div>
                    <div class="question-card-actions">
                      <button class="icon-btn" data-action="edit-question" data-id="${q.id}" title="Edit">
                        <span class="material-symbols-outlined">edit</span>
                      </button>
                      <button class="icon-btn danger" data-action="delete-question" data-id="${q.id}" title="Delete">
                        <span class="material-symbols-outlined">delete</span>
                      </button>
                    </div>
                  </div>

                  <div class="options-grid">
                    ${(q.options || []).map((opt, i) => {
                      const isCorrect = q.correct_option === i;
                      const optionAudio = this.getAudioUrl(opt, 'options');
                      return `
                      <div class="option-item ${isCorrect ? 'is-correct' : ''}">
                        <span class="option-letter">${String.fromCharCode(65 + i)}.</span>
                        <span class="option-content">${this.escapeHtml(opt)}</span>
                        <div class="audio-player-inline">
                          ${optionAudio ? `
                            <button class="play-btn-small" onclick="hierarchyComponent.playAudio(this, '${optionAudio}')" title="Play option audio">
                              <span class="material-symbols-outlined">play_arrow</span>
                            </button>
                          ` : ''}
                        </div>
                      </div>
                    `}).join('')}
                  </div>

                  ${q.explanation ? `
                    <div class="explanation-section">
                      <span class="material-symbols-outlined explanation-icon">info</span>
                      <div class="explanation-text"><strong>Explanation:</strong> ${this.escapeHtml(q.explanation)}</div>
                    </div>
                  ` : ''}
                </div>
              `}).join('')}
            </div>
          `}
        </div>
      </div>
    `;

    const editTopicBtn = document.getElementById('editTopicBtn');
    if (editTopicBtn) {
      editTopicBtn.addEventListener('click', () => this.openTopicForm('edit', topic));
    }

    const deleteTopicBtn = document.getElementById('deleteTopicBtn');
    if (deleteTopicBtn) {
      deleteTopicBtn.addEventListener('click', () => this.deleteTopic(topic?.id));
    }

    container.querySelectorAll('[data-action="edit-question"]').forEach(btn => {
      btn.addEventListener('click', () => {
        const q = this.questions.find(item => item.id === btn.dataset.id);
        this.openQuestionForm('edit', q);
      });
    });

    container.querySelectorAll('[data-action="delete-question"]').forEach(btn => {
      btn.addEventListener('click', () => this.deleteQuestion(btn.dataset.id));
    });

    const importBtn = document.getElementById('importCsvBtn');
    const importInput = document.getElementById('importCsvInput');
    if (importBtn && importInput) {
      importBtn.addEventListener('click', () => importInput.click());
      importInput.addEventListener('change', (e) => this.handleCsvImport(e));
    }
  }

  openTopicForm(mode, data = null) {
    const title = mode === 'edit' ? 'Edit Topic' : 'Create Topic';
    const body = this.formsHelper.renderTopicForm(mode, data);
    this.showModal(title, body);
    this.formsHelper.attachTopicFormListener(mode, data);
  }

  openQuestionForm(mode, data = null) {
    if (!this.selectedTopicId && mode === 'create') return;
    const title = mode === 'edit' ? 'Edit Question' : 'Create Question';
    const body = this.formsHelper.renderQuestionForm(mode, data, this.selectedTopicId);
    this.showModal(title, body);
    this.formsHelper.attachQuestionFormListener(mode, data, this.selectedTopicId);
  }

  showModal(title, bodyHtml) {
    const modal = document.getElementById('hierModal');
    const modalTitle = document.getElementById('hierModalTitle');
    const modalBody = document.getElementById('hierModalBody');
    if (!modal || !modalTitle || !modalBody) return;
    modalTitle.textContent = title;
    modalBody.innerHTML = bodyHtml;
    modal.style.display = 'flex';
  }

  closeModal() {
    const modal = document.getElementById('hierModal');
    if (modal) modal.style.display = 'none';
  }

  cleanupTopicSelect() {
    if (!this.select2Instance || !window.$?.fn?.select2) return;
    try {
      this.select2Instance.off('change');
      this.select2Instance.select2('destroy');
    } catch (error) {
      console.warn('Failed to cleanup select2 instance', error);
    } finally {
      this.select2Instance = null;
    }
  }

  cleanupSetSelect() {
    if (!this.setSelect2Instance || !window.$?.fn?.select2) return;
    try {
      this.setSelect2Instance.off('change');
      this.setSelect2Instance.select2('destroy');
    } catch (error) {
      console.warn('Failed to cleanup set select2 instance', error);
    } finally {
      this.setSelect2Instance = null;
    }
  }

  async deleteTopic(topicId) {
    if (!topicId) return;
    if (!confirm('Delete this topic and all its questions?')) return;
    await fetch(`/api/topics/${topicId}?force=true`, { method: 'DELETE' });
    await this.loadTopics();
    await this.loadQuestions();
    this.selectedTopicId = null;
    this.renderContent();
  }

  async deleteQuestion(questionId) {
    if (!questionId) return;
    if (!confirm('Delete this question?')) return;
    await fetch(`/api/questions/${questionId}`, { method: 'DELETE' });
    await this.loadQuestions();
    this.renderContent();
  }

  async handleCsvImport(event) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file || !this.selectedTopicId) return;

    try {
      const csvText = await file.text();
      const response = await fetch(`/api/csv/import?topicId=${this.selectedTopicId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/csv' },
        body: csvText
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'CSV import failed');
      }

      await this.loadTopics();
      await this.loadQuestions();
      this.renderContent();
    } catch (error) {
      console.error('CSV import failed:', error);
      alert(`CSV import failed: ${error.message}`);
    }
  }

  getAudioUrl(text, category) {
    if (!this.ttsCache || !text) return null;
    // Find a default version or any version that matches text and category
    const entry = this.ttsCache.find(c =>
        c.category === category &&
        c.text === text &&
        c.is_default
    ) || this.ttsCache.find(c =>
        c.category === category &&
        c.text === text
    );
    return entry ? entry.audio_url : null;
  }

  playAudio(btn, url) {
    if (this.currentAudio) {
        this.currentAudio.pause();
        if (this.currentBtn) {
            this.currentBtn.querySelector('.material-symbols-outlined').textContent = 'play_arrow';
            this.currentBtn.classList.remove('playing');
        }
    }

    if (this.currentAudioUrl === url) {
        this.currentAudioUrl = null;
        return;
    }

    const icon = btn.querySelector('.material-symbols-outlined');
    icon.textContent = 'pause';
    btn.classList.add('playing');

    this.currentAudio = new Audio(url);
    this.currentAudioUrl = url;
    this.currentBtn = btn;

    this.currentAudio.onended = () => {
        icon.textContent = 'play_arrow';
        btn.classList.remove('playing');
        this.currentAudio = null;
        this.currentAudioUrl = null;
        this.currentBtn = null;
    };

    this.currentAudio.play().catch(e => {
        console.error('Audio play failed:', e);
        icon.textContent = 'play_arrow';
        btn.classList.remove('playing');
    });
  }

  cleanup() {
    this.cleanupTopicSelect();
    this.cleanupSetSelect();
  }

  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
  }
}

window.hierarchyComponent = null;
