/**
 * Questions Component
 * Manage questions (topic-based)
 */

export class QuestionsComponent {
  constructor(contentEl) {
    this.contentEl = contentEl;
    this.questions = [];
    this.filteredQuestions = [];
    this.searchTerm = '';
    this.currentPage = 1;
    this.itemsPerPage = 20;
  }

  async render() {
    try {
      await this.loadQuestions();
      this.filteredQuestions = [...this.questions];
      this.renderContent();
    } catch (error) {
      console.error('Error rendering questions:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load questions. ${error.message}
        </div>
      `;
    }
  }

  async loadQuestions() {
    const response = await fetch('/api/questions');
    const data = await response.json();
    this.questions = data.questions || data || [];
  }

  renderContent() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    const paginatedQuestions = this.filteredQuestions.slice(startIndex, endIndex);
    const totalPages = Math.ceil(this.filteredQuestions.length / this.itemsPerPage);

    this.contentEl.innerHTML = `
      <div class="content-header">
        <div>
          <h1 class="content-title">❓ Questions Management</h1>
          <p class="content-description">Browse and manage your question bank</p>
        </div>
      </div>

      <div class="card mb-20">
        <div class="card-body">
          <div class="questions-header">
            <div class="search-box">
              <input 
                type="text" 
                id="searchQuestions" 
                class="form-control" 
                placeholder="🔍 Search questions..." 
                value="${this.searchTerm}"
                style="width: 400px;"
              >
            </div>
            <div class="questions-stats">
              <span class="stat-badge">
                <strong>${this.filteredQuestions.length}</strong> questions
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-body">
          ${paginatedQuestions.length === 0 ? `
            <div class="empty-state">
              <p>No questions found</p>
            </div>
          ` : `
            <div class="questions-list">
              ${paginatedQuestions.map((q, index) => `
                <div class="question-card">
                  <div class="question-header">
                    <span class="question-number">#${startIndex + index + 1}</span>
                  </div>
                  <div class="question-text">${this.escapeHtml(q.question)}</div>
                  <div class="question-options">
                    ${(q.options || []).map((opt, i) => `
                      <div class="option ${q.correct_option === i ? 'correct-option' : ''}">
                        <span class="option-label">${String.fromCharCode(65 + i)}.</span>
                        <span class="option-text">${this.escapeHtml(opt)}</span>
                        ${q.correct_option === i ? '<span class="correct-badge">✓ Correct</span>' : ''}
                      </div>
                    `).join('')}
                  </div>
                </div>
              `).join('')}
            </div>

            ${totalPages > 1 ? `
              <div class="pagination">
                <button 
                  class="btn btn-secondary btn-sm" 
                  onclick="window.questionsComponent.goToPage(${this.currentPage - 1})"
                  ${this.currentPage === 1 ? 'disabled' : ''}
                >
                  ← Previous
                </button>
                <span class="pagination-info">
                  Page ${this.currentPage} of ${totalPages}
                </span>
                <button 
                  class="btn btn-secondary btn-sm" 
                  onclick="window.questionsComponent.goToPage(${this.currentPage + 1})"
                  ${this.currentPage === totalPages ? 'disabled' : ''}
                >
                  Next →
                </button>
              </div>
            ` : ''}
          `}
        </div>
      </div>
    `;

    this.attachEventListeners();
  }

  attachEventListeners() {
    const searchInput = document.getElementById('searchQuestions');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        this.searchTerm = e.target.value.toLowerCase();
        this.filterQuestions();
      });
    }
  }

  filterQuestions() {
    this.filteredQuestions = this.questions.filter(q => {
      const matchesSearch = !this.searchTerm ||
        q.question?.toLowerCase().includes(this.searchTerm) ||
        (q.options || []).some(opt => opt?.toLowerCase().includes(this.searchTerm));

      return matchesSearch;
    });

    this.currentPage = 1;
    this.renderContent();
  }

  goToPage(page) {
    const totalPages = Math.ceil(this.filteredQuestions.length / this.itemsPerPage);
    if (page >= 1 && page <= totalPages) {
      this.currentPage = page;
      this.renderContent();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
  }
}

window.questionsComponent = null;
