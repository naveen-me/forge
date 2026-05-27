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
    this.formsHelper = new HierarchyForms(this);
  }

  async render() {
    try {
      await this.loadTopics();
      await this.loadQuestions();
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
  }

  renderContent() {
    this.cleanupTopicSelect();
    this.contentEl.innerHTML = `
      <div class="content-header">
        <div>
          <h1 class="content-title">Data Management</h1>
          <p class="content-description">Topics &rarr; Questions</p>
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
    this.renderList();
  }

  attachEventListeners() {
    const topicSelect = document.getElementById('topicSelect');
    if (topicSelect) {
      const handleTopicChange = (value) => {
        this.selectedTopicId = value || null;
        // Only update the question list and button state, not the entire page
        this.updateAddQuestionButton();
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
            <a class="icon-btn" href="/api/csv/sample?topicId=${this.selectedTopicId}" title="Download sample CSV" aria-label="Download sample CSV">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
            </a>
            <a class="icon-btn" href="/api/csv/export?topicId=${this.selectedTopicId}" title="Export CSV" aria-label="Export CSV">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            </a>
            <button class="icon-btn" id="importCsvBtn" type="button" title="Import CSV" aria-label="Import CSV">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            </button>
            <button class="icon-btn" id="editTopicBtn" type="button" title="Edit topic" aria-label="Edit topic">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            </button>
            <button class="icon-btn danger" id="deleteTopicBtn" type="button" title="Delete topic" aria-label="Delete topic">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>
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
              ${questions.map((q, idx) => `
                <div class="question-card">
                  <div class="question-header">
                    <span class="question-number">#${idx + 1}</span>
                  </div>
                  <div class="question-text">${this.escapeHtml(q.question)}</div>
                  <div class="question-options">
                    ${(q.options || []).map((opt, i) => `
                      <div class="option ${q.correct_option === i ? 'correct-option' : ''}">
                        <span class="option-label">${String.fromCharCode(65 + i)}.</span>
                        <span class="option-text">${this.escapeHtml(opt)}</span>
                        ${q.correct_option === i ? '<span class="correct-badge">&#10003; Correct</span>' : ''}
                      </div>
                    `).join('')}
                  </div>
                  <div style="margin-top: 10px; display:flex; gap: 8px;">
                    <button class="btn btn-secondary btn-small" data-action="edit-question" data-id="${q.id}">Edit</button>
                    <button class="btn btn-danger btn-small" data-action="delete-question" data-id="${q.id}">Delete</button>
                  </div>
                </div>
              `).join('')}
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

  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
  }
}

window.hierarchyComponent = null;
