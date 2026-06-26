/**
 * Fonts Management Component
 */

class FontsManager {
  constructor() {
    this.fonts = [];
    this.filteredFonts = [];
    this.currentSearch = '';
  }

  async init() {
    await this.loadFonts();
    this.render();
    this.attachEventListeners();
  }

  async loadFonts() {
    try {
      const response = await fetch('/api/fonts');
      if (!response.ok) throw new Error('Failed to load fonts');
      this.fonts = await response.json();
      this.filteredFonts = [...this.fonts];
      console.log('Fonts loaded successfully:', this.fonts);
    } catch (error) {
      console.error('Error loading fonts:', error);
      this.showNotification('Failed to load fonts: ' + error.message, 'error');
      this.fonts = [];
      this.filteredFonts = [];
    }
  }

  render() {
    const content = `
      <div class="content-header">
        <div>
          <h1 class="content-title">Font Management</h1>
          <p class="content-description">Upload and manage custom fonts for your videos</p>
        </div>
      </div>

      <div class="fonts-page">
        <div class="fonts-toolbar card">
          <div class="card-body">
            <div class="fonts-search-bar">
              <input 
                type="text" 
                id="fontSearch" 
                class="form-control" 
                placeholder="Search fonts by name..."
                value="${this.currentSearch}"
              >
              <span class="fonts-count">${this.filteredFonts.length} font(s)</span>
              <button id="uploadFontBtn" class="btn btn-primary">
                Upload Font
              </button>
            </div>
          </div>
        </div>

        <div class="fonts-grid">
          ${this.filteredFonts.length === 0 ? this.renderEmptyState() : this.renderFonts()}
        </div>
      </div>

      <div id="uploadModal" class="modal" style="display: none;">
        <div class="modal-content">
          <div class="modal-header">
            <h3>Upload Font</h3>
            <button class="modal-close">&times;</button>
          </div>
          <div class="modal-body">
            <form id="fontUploadForm">
              <div class="form-group">
                <label>Font Name</label>
                <input 
                  type="text" 
                  id="fontName" 
                  class="form-control" 
                  placeholder="e.g., Roboto Bold"
                  required
                >
                <small>Give your font a descriptive name</small>
              </div>

              <div class="form-group">
                <label>Font File</label>
                <input 
                  type="file" 
                  id="fontFile" 
                  accept=".ttf,.otf,.woff,.woff2"
                  required
                  style="display: none;"
                >
                <div class="file-upload-area" id="fileUploadArea">
                  <div class="file-upload-content">
                    <span class="file-upload-icon icon-file"></span>
                    <p>Click to select font file</p>
                    <small>Supported: .ttf, .otf, .woff, .woff2 (Max 10MB)</small>
                  </div>
                  <div class="file-upload-preview" style="display: none;">
                    <span class="file-name"></span>
                    <button type="button" class="btn-remove-file">×</button>
                  </div>
                </div>
              </div>

              <div class="form-actions">
                <button type="button" class="btn btn-secondary" id="cancelUpload">Cancel</button>
                <button type="submit" class="btn btn-primary" id="submitUpload">
                  Upload Font
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    `;

    document.getElementById('content').innerHTML = content;
  }

  renderEmptyState() {
    return `
      <div class="empty-state">
        <div class="empty-icon icon-font"></div>
        <h3>No Fonts Found</h3>
        <p>${this.currentSearch ? 'No fonts match your search.' : 'Upload your first custom font to get started.'}</p>
        ${!this.currentSearch ? '<button class="btn btn-primary" onclick="document.getElementById(\'uploadFontBtn\').click()">Upload Font</button>' : ''}
      </div>
    `;
  }

  renderFonts() {
    return this.filteredFonts.map(font => {
      const format = font.format || font.filename?.split('.').pop() || 'font';
      const fileSize = font.fileSize || font.size || 0;
      const originalName = font.originalName || font.original_name || font.filename;
      
      return `
        <div class="font-card" data-id="${font.id}">
          <div class="font-preview">
            <div class="font-sample" style="font-family: '${font.name}';">
              Aa Bb Cc
            </div>
          </div>
          <div class="font-info">
            <h3 class="font-name" title="${font.name}">${font.name}</h3>
            <div class="font-meta">
              <span class="font-format">${format.toUpperCase()}</span>
              <span class="font-size">${this.formatFileSize(fileSize)}</span>
            </div>
            <div class="font-filename" title="${originalName}">
              ${originalName}
            </div>
          </div>
          <div class="font-actions">
            <button class="btn-icon edit-font" data-id="${font.id}" title="Rename">
              Rename
            </button>
            <button class="btn-icon delete-font" data-id="${font.id}" title="Delete">
              Delete
            </button>
          </div>
        </div>
      `;
    }).join('');
  }

  attachEventListeners() {
    // Upload button
    const uploadBtn = document.getElementById('uploadFontBtn');
    if (uploadBtn) {
      uploadBtn.addEventListener('click', () => this.showUploadModal());
    }

    // Search
    const searchInput = document.getElementById('fontSearch');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => this.handleSearch(e.target.value));
    }

    // Modal close
    const closeBtn = document.querySelector('.modal-close');
    if (closeBtn) {
      closeBtn.addEventListener('click', () => this.hideUploadModal());
    }

    const cancelBtn = document.getElementById('cancelUpload');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', () => this.hideUploadModal());
    }

    // File upload area
    const uploadArea = document.getElementById('fileUploadArea');
    const fileInput = document.getElementById('fontFile');
    
    if (uploadArea && fileInput) {
      // Click on upload area opens file picker
      uploadArea.addEventListener('click', (e) => {
        // Don't trigger if clicking the remove button
        if (e.target.classList.contains('btn-remove-file')) {
          return;
        }
        fileInput.click();
      });
      
      // Handle file selection
      fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
          this.handleFileSelect(e.target.files[0]);
        }
      });
    }

    // Form submit
    const form = document.getElementById('fontUploadForm');
    if (form) {
      form.addEventListener('submit', (e) => this.handleUpload(e));
    }

    // Delete buttons
    document.querySelectorAll('.delete-font').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = parseInt(btn.dataset.id);
        this.handleDelete(id);
      });
    });

    // Edit buttons
    document.querySelectorAll('.edit-font').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = parseInt(btn.dataset.id);
        this.handleEdit(id);
      });
    });

    // Load font faces
    this.loadFontFaces();
  }

  async loadFontFaces() {
    for (const font of this.fonts) {
      try {
        const fontFace = new FontFace(font.name, `url(${font.url})`);
        await fontFace.load();
        document.fonts.add(fontFace);
      } catch (error) {
        console.warn(`Failed to load font: ${font.name}`, error);
      }
    }
  }

  handleFileSelect(file) {
    const uploadArea = document.getElementById('fileUploadArea');
    const preview = uploadArea.querySelector('.file-upload-preview');
    const content = uploadArea.querySelector('.file-upload-content');
    const fileName = preview.querySelector('.file-name');

    fileName.textContent = file.name;
    content.style.display = 'none';
    preview.classList.add('active');

    // Auto-populate font name if empty
    const nameInput = document.getElementById('fontName');
    if (!nameInput.value) {
      nameInput.value = file.name.replace(/\.[^/.]+$/, '').replace(/[_-]/g, ' ');
    }

    // Remove file button
    const removeBtn = preview.querySelector('.btn-remove-file');
    removeBtn.onclick = (e) => {
      e.stopPropagation();
      e.preventDefault();
      document.getElementById('fontFile').value = '';
      content.style.display = 'block';
      preview.classList.remove('active');
    };
  }

  handleSearch(query) {
    this.currentSearch = query.toLowerCase();
    
    if (!this.currentSearch) {
      this.filteredFonts = [...this.fonts];
    } else {
      this.filteredFonts = this.fonts.filter(font => {
        const originalName = (font.originalName || font.original_name || font.filename || '').toLowerCase();
        const name = (font.name || '').toLowerCase();
        return name.includes(this.currentSearch) || originalName.includes(this.currentSearch);
      });
    }
    
    this.render();
    this.attachEventListeners();
  }

  showUploadModal() {
    document.getElementById('uploadModal').style.display = 'flex';
  }

  hideUploadModal() {
    const modal = document.getElementById('uploadModal');
    if (modal) {
      modal.style.display = 'none';
    }
    
    const form = document.getElementById('fontUploadForm');
    if (form) {
      form.reset();
    }
    
    // Reset file upload area
    const uploadArea = document.getElementById('fileUploadArea');
    if (uploadArea) {
      const preview = uploadArea.querySelector('.file-upload-preview');
      const content = uploadArea.querySelector('.file-upload-content');
      if (content) content.style.display = 'block';
      if (preview) preview.classList.remove('active');
    }
  }

  async handleUpload(e) {
    e.preventDefault();

    const nameInput = document.getElementById('fontName');
    const fileInput = document.getElementById('fontFile');
    const submitBtn = document.getElementById('submitUpload');

    if (!fileInput.files[0]) {
      this.showNotification('Please select a font file', 'error');
      return;
    }

    const formData = new FormData();
    formData.append('name', nameInput.value);
    formData.append('font', fileInput.files[0]);

    submitBtn.disabled = true;
    submitBtn.textContent = 'Uploading...';

    try {
      const response = await fetch('/api/fonts/upload', {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        // Server might return JSON or plain text/HTML on error
        let message = 'Upload failed';
        const ct = response.headers.get('content-type') || '';
        try {
          if (ct.includes('application/json')) {
            const error = await response.json();
            message = error.error || message;
          } else {
            const text = await response.text();
            if (text) message = text;
          }
        } catch (_) {
          // ignore parsing errors
        }
        throw new Error(message);
      }

      const newFont = await response.json();
      this.showNotification('Font uploaded successfully!', 'success');
      this.hideUploadModal();
      await this.loadFonts();
      this.render();
      this.attachEventListeners();
    } catch (error) {
      console.error('Upload error:', error);
      this.showNotification(error.message, 'error');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Upload Font';
    }
  }

  async handleDelete(id) {
    const font = this.fonts.find(f => f.id === id);
    if (!font) return;

    if (!confirm(`Are you sure you want to delete "${font.name}"?`)) {
      return;
    }

    try {
      const response = await fetch(`/api/fonts/${id}`, {
        method: 'DELETE'
      });

      if (!response.ok) throw new Error('Failed to delete font');

      this.showNotification('Font deleted successfully', 'success');
      await this.loadFonts();
      this.render();
      this.attachEventListeners();
    } catch (error) {
      console.error('Delete error:', error);
      this.showNotification('Failed to delete font', 'error');
    }
  }

  async handleEdit(id) {
    const font = this.fonts.find(f => f.id === id);
    if (!font) return;

    const newName = prompt('Enter new font name:', font.name);
    if (!newName || newName === font.name) return;

    try {
      const response = await fetch(`/api/fonts/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: newName })
      });

      if (!response.ok) throw new Error('Failed to update font');

      this.showNotification('Font renamed successfully', 'success');
      await this.loadFonts();
      this.render();
      this.attachEventListeners();
    } catch (error) {
      console.error('Update error:', error);
      this.showNotification('Failed to rename font', 'error');
    }
  }

  formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    // Show notification
    setTimeout(() => notification.classList.add('show'), 10);
    
    // Hide and remove notification
    setTimeout(() => {
      notification.classList.remove('show');
      setTimeout(() => notification.remove(), 300);
    }, 3000);
  }
}

// Export for use in main app
window.FontsManager = FontsManager;
export default FontsManager;
