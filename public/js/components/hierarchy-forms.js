/**
 * Simple forms for Topics and Questions
 */

export class HierarchyForms {
  constructor(hierarchyComponent) {
    this.hierarchy = hierarchyComponent;
  }

  renderTopicForm(mode, data = null) {
    const isEdit = mode === 'edit';
    const topic = data || {};
    return `
      <form id="topicForm" class="hierarchy-form">
        <div class="form-section">
          <div class="form-section-title">${isEdit ? 'Edit' : 'Create'} Topic</div>
          <div class="form-group">
            <label class="required">Topic Name</label>
            <input type="text" name="name" class="form-control" value="${this.escapeHtml(topic.name || '')}" required>
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea name="description" class="form-control" rows="3">${this.escapeHtml(topic.description || '')}</textarea>
          </div>
          <div class="form-group">
            <label>Position</label>
            <input type="number" name="position" class="form-control" value="${topic.position || 0}" min="0">
          </div>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary">${isEdit ? 'Update' : 'Create'} Topic</button>
          <button type="button" class="btn btn-secondary" id="cancelTopicBtn">Cancel</button>
        </div>
      </form>
    `;
  }

  attachTopicFormListener(mode, data = null) {
    const form = document.getElementById('topicForm');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const formData = new FormData(form);
      const payload = {
        name: formData.get('name'),
        description: formData.get('description'),
        position: Number(formData.get('position') || 0)
      };
      if (mode === 'edit' && data?.id) {
        await fetch(`/api/topics/${data.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
      } else {
        await fetch('/api/topics', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
      }
      await this.hierarchy.loadTopics();
      this.hierarchy.closeModal();
      this.hierarchy.renderContent();
    });

    const cancelBtn = document.getElementById('cancelTopicBtn');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', () => this.hierarchy.closeModal());
    }
  }

  renderQuestionForm(mode, data = null, topicId = null) {
    const isEdit = mode === 'edit';
    const question = data || {};
    const options = Array.isArray(question.options) ? question.options : [];
    return `
      <form id="questionForm" class="hierarchy-form">
        <div class="form-section">
          <div class="form-section-title">${isEdit ? 'Edit' : 'Create'} Question</div>
          <div class="form-group">
            <label class="required">Question</label>
            <textarea name="question" class="form-control" rows="3" required>${this.escapeHtml(question.question || '')}</textarea>
          </div>
          <div class="form-group">
            <label class="required">Option A</label>
            <input type="text" name="option_0" class="form-control" value="${this.escapeHtml(options[0] || '')}" required>
          </div>
          <div class="form-group">
            <label class="required">Option B</label>
            <input type="text" name="option_1" class="form-control" value="${this.escapeHtml(options[1] || '')}" required>
          </div>
          <div class="form-group">
            <label>Option C</label>
            <input type="text" name="option_2" class="form-control" value="${this.escapeHtml(options[2] || '')}">
          </div>
          <div class="form-group">
            <label>Option D</label>
            <input type="text" name="option_3" class="form-control" value="${this.escapeHtml(options[3] || '')}">
          </div>
          <div class="form-group">
            <label class="required">Correct Option Number (1-4)</label>
            <input type="number" name="correct_option" class="form-control" min="1" max="4" value="${Number.isInteger(question.correct_option) ? question.correct_option + 1 : 1}" required>
          </div>
          <div class="form-group">
            <label>Explanation (text or image URL)</label>
            <textarea name="explanation" class="form-control" rows="2">${this.escapeHtml(question.explanation || '')}</textarea>
          </div>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary">${isEdit ? 'Update' : 'Create'} Question</button>
          <button type="button" class="btn btn-secondary" id="cancelQuestionBtn">Cancel</button>
        </div>
      </form>
    `;
  }

  attachQuestionFormListener(mode, data = null, topicId = null) {
    const form = document.getElementById('questionForm');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const formData = new FormData(form);
      const options = [0, 1, 2, 3].map(i => formData.get(`option_${i}`)).filter(v => v !== null && v !== '');
      const correctOptionValue = Number(formData.get('correct_option') || 1);
      const payload = {
        topic_id: topicId || data?.topic_id,
        question: formData.get('question'),
        options,
        correct_option: Math.max(0, correctOptionValue - 1),
        explanation: formData.get('explanation')
      };
      if (mode === 'edit' && data?.id) {
        await fetch(`/api/questions/${data.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
      } else {
        await fetch('/api/questions', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
      }
      await this.hierarchy.loadQuestions();
      this.hierarchy.closeModal();
      this.hierarchy.renderContent();
    });

    const cancelBtn = document.getElementById('cancelQuestionBtn');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', () => this.hierarchy.closeModal());
    }
  }

  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
  }
}
