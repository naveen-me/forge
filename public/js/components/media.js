/**
 * Media Library Component
 * Browse, search, and manage media files
 */
export class MediaComponent {
  constructor(contentEl) {
    this.contentEl = contentEl;
    this.media = [];
    this.filteredMedia = [];
    this.currentFilter = 'all';
    this.searchQuery = '';
    this.stats = null;
  }

  async render() {
    await this.loadMedia();
    await this.loadStats();
    this.renderContent();
    this.attachEventListeners();
  }

  async loadMedia() {
    try {
      const response = await fetch('/api/media');
      this.media = await response.json();
      this.applyFilters();
    } catch (error) {
      console.error('Error loading media:', error);
      this.media = [];
      this.filteredMedia = [];
    }
  }

  async loadStats() {
    try {
      const response = await fetch('/api/media/stats');
      this.stats = await response.json();
    } catch (error) {
      console.error('Error loading stats:', error);
      this.stats = null;
    }
  }

  applyFilters() {
    let filtered = [...this.media];

    // Filter by type
    if (this.currentFilter !== 'all') {
      filtered = filtered.filter(m => m.type === this.currentFilter);
    }

    // Filter by search
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(m =>
        m.original_name.toLowerCase().includes(query) ||
        m.filename.toLowerCase().includes(query)
      );
    }

    this.filteredMedia = filtered;
  }

  renderContent() {
    this.contentEl.innerHTML = `
      <div class="content-header">
        <div>
          <h1 class="content-title">🖼️ Media Library</h1>
          <p class="content-description">Browse and manage your media files</p>
        </div>
        <button class="btn btn-primary" id="uploadMediaBtn">
          Upload Media
        </button>
      </div>

      <!-- Upload Modal -->
      <div id="mediaUploadModal" style="display:none; position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.5); z-index:10000; align-items:center; justify-content:center;">
        <div style="background:#fff; border-radius:12px; padding:30px; width:480px; max-width:90%; position:relative;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
            <h3 style="margin:0; color:#111827;">Upload Media</h3>
            <button id="closeMediaUploadModal" style="background:none; border:none; font-size:20px; cursor:pointer; color:#6b7280;">&times;</button>
          </div>
          <div id="mediaUploadArea" style="border:2px dashed #4CAF50; border-radius:10px; padding:40px 20px; text-align:center; cursor:pointer; transition:all 0.2s;">
            <div style="font-size:48px; margin-bottom:10px;">📁</div>
            <p style="color:#111827; font-weight:500; margin-bottom:5px;">Drop files here or click to browse</p>
            <p style="color:#6b7280; font-size:13px; margin-bottom:15px;">Supports: JPG, PNG, GIF, MP4, WEBM (Max 100MB)</p>
            <input type="file" id="mediaFileInput" accept="image/*,video/*" multiple style="display:none;">
            <button class="btn btn-primary" id="mediaBrowseBtn" type="button">Browse Files</button>
          </div>
          <div id="mediaUploadProgress" style="display:none; margin-top:15px;"></div>
        </div>
      </div>

      <!-- Stats Cards -->
      ${this.renderStatsCards()}

      <!-- Filters and Search -->
      <div class="card">
        <div class="card-body">
          <div style="display: flex; gap: 15px; align-items: center; flex-wrap: wrap;">
            
            <!-- Filter Tabs -->
            <div class="filter-tabs" style="display: flex; gap: 10px;">
              <button class="filter-tab ${this.currentFilter === 'all' ? 'active' : ''}" data-filter="all">
                All (${this.media.length})
              </button>
              <button class="filter-tab ${this.currentFilter === 'image' ? 'active' : ''}" data-filter="image">
                Images (${this.media.filter(m => m.type === 'image').length})
              </button>
              <button class="filter-tab ${this.currentFilter === 'video' ? 'active' : ''}" data-filter="video">
                Videos (${this.media.filter(m => m.type === 'video').length})
              </button>
            </div>

            <!-- Search -->
            <div style="flex: 1; min-width: 200px;">
              <input 
                type="text" 
                id="mediaSearch" 
                class="form-input" 
                placeholder="Search media..." 
                value="${this.searchQuery}"
                style="width: 100%;"
              >
            </div>

          </div>
        </div>
      </div>

      <!-- Media Grid -->
      <div class="card">
        <div class="card-body">
          ${this.renderMediaGrid()}
        </div>
      </div>
    `;
  }

  renderStatsCards() {
    if (!this.stats) return '';

    const formatSize = (bytes) => {
      if (bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
    };

    return `
      <div class="grid grid-3" style="margin-bottom: 20px;">
        <div class="card">
          <div class="card-body" style="text-align: center;">
            <div style="font-size: 32px; color: #4CAF50; margin-bottom: 5px;">📁</div>
            <div style="font-size: 24px; font-weight: 600; color: #111827;">${this.stats.total}</div>
            <div style="color: #6b7280; font-size: 14px;">Total Files</div>
          </div>
        </div>
        <div class="card">
          <div class="card-body" style="text-align: center;">
            <div style="font-size: 32px; color: #2196F3; margin-bottom: 5px;">🖼️</div>
            <div style="font-size: 24px; font-weight: 600; color: #111827;">${this.stats.images}</div>
            <div style="color: #6b7280; font-size: 14px;">Images (${formatSize(this.stats.imageSize)})</div>
          </div>
        </div>
        <div class="card">
          <div class="card-body" style="text-align: center;">
            <div style="font-size: 32px; color: #FF9800; margin-bottom: 5px;">🎬</div>
            <div style="font-size: 24px; font-weight: 600; color: #111827;">${this.stats.videos}</div>
            <div style="color: #6b7280; font-size: 14px;">Videos (${formatSize(this.stats.videoSize)})</div>
          </div>
        </div>
      </div>
    `;
  }

  renderMediaGrid() {
    if (this.filteredMedia.length === 0) {
      return `
        <div style="text-align: center; padding: 60px 20px; color: #6b7280;">
          <div style="font-size: 48px; margin-bottom: 15px;">📂</div>
          <h3 style="color: #111827; margin-bottom: 10px;">No media files found</h3>
          <p>Upload some media files to get started</p>
          <button class="btn btn-primary" style="margin-top: 20px;" onclick="document.getElementById('uploadMediaBtn').click()">
            Upload Media
          </button>
        </div>
      `;
    }

    return `
      <div class="grid grid-4" style="gap: 20px;">
        ${this.filteredMedia.map(m => this.renderMediaCard(m)).join('')}
      </div>
    `;
  }

  renderMediaCard(media) {
    const formatSize = (bytes) => {
      if (bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
    };

    const formatDate = (dateStr) => {
      const date = new Date(dateStr);
      return date.toLocaleDateString();
    };

    const thumbnail = media.type === 'image'
      ? `<img src="${media.url}" alt="${media.original_name}" style="width: 100%; height: 180px; object-fit: cover; border-radius: 6px 6px 0 0;">`
      : `<div style="width: 100%; height: 180px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 6px 6px 0 0; display: flex; align-items: center; justify-content: center;">
          <span style="font-size: 48px;">🎬</span>
        </div>`;

    return `
      <div class="card media-card" style="padding: 0; overflow: hidden; transition: transform 0.2s;" data-media-id="${media.id}">
        ${thumbnail}
        <div style="padding: 15px;">
          <div style="font-weight: 500; color: #111827; margin-bottom: 5px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${media.original_name}">
            ${media.original_name}
          </div>
          <div style="font-size: 12px; color: #6b7280; margin-bottom: 10px;">
            ${formatSize(media.size)} • ${formatDate(media.created_at)}
          </div>
          <div style="display: flex; gap: 8px;">
            <button class="btn btn-small btn-secondary view-media-btn" data-media-id="${media.id}" title="View">
              👁️
            </button>
            <button class="btn btn-small btn-secondary rename-media-btn" data-media-id="${media.id}" title="Rename">
              ✏️
            </button>
            <button class="btn btn-small btn-secondary copy-url-btn" data-media-url="${media.url}" title="Copy URL">
              📋
            </button>
            <button class="btn btn-small btn-danger delete-media-btn" data-media-id="${media.id}" title="Delete">
              🗑️
            </button>
          </div>
        </div>
      </div>
    `;
  }

  attachEventListeners() {
    // Upload button - open modal
    const uploadBtn = document.getElementById('uploadMediaBtn');
    const modal = document.getElementById('mediaUploadModal');
    const closeModalBtn = document.getElementById('closeMediaUploadModal');
    const uploadArea = document.getElementById('mediaUploadArea');
    const browseBtn = document.getElementById('mediaBrowseBtn');
    const fileInput = document.getElementById('mediaFileInput');

    if (uploadBtn && modal) {
      uploadBtn.addEventListener('click', () => {
        modal.style.display = 'flex';
      });
    }

    if (closeModalBtn && modal) {
      closeModalBtn.addEventListener('click', () => {
        modal.style.display = 'none';
      });
      modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.style.display = 'none';
      });
    }

    if (browseBtn && fileInput) {
      browseBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        fileInput.click();
      });
    }

    if (uploadArea && fileInput) {
      uploadArea.addEventListener('click', () => fileInput.click());

      uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = '#45a049';
        uploadArea.style.background = '#f0fbf3';
      });
      uploadArea.addEventListener('dragleave', () => {
        uploadArea.style.borderColor = '#4CAF50';
        uploadArea.style.background = '';
      });
      uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = '#4CAF50';
        uploadArea.style.background = '';
        const files = Array.from(e.dataTransfer.files).filter(f =>
          f.type.startsWith('image/') || f.type.startsWith('video/')
        );
        if (files.length > 0) this.handleMediaUpload(files);
      });

      fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
          this.handleMediaUpload(Array.from(e.target.files));
        }
      });
    }

    // Filter tabs
    const filterTabs = document.querySelectorAll('.filter-tab');
    filterTabs.forEach(tab => {
      tab.addEventListener('click', (e) => {
        this.currentFilter = e.target.dataset.filter;
        this.applyFilters();
        this.renderContent();
        this.attachEventListeners();
      });
    });

    // Search
    const searchInput = document.getElementById('mediaSearch');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        this.searchQuery = e.target.value;
        this.applyFilters();
        this.renderContent();
        this.attachEventListeners();
      });
    }

    // View buttons
    document.querySelectorAll('.view-media-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const mediaId = e.target.dataset.mediaId;
        this.viewMedia(mediaId);
      });
    });

    // Rename buttons
    document.querySelectorAll('.rename-media-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const mediaId = e.target.dataset.mediaId;
        this.renameMedia(mediaId);
      });
    });

    // Copy URL buttons
    document.querySelectorAll('.copy-url-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const url = window.location.origin + e.target.dataset.mediaUrl;
        navigator.clipboard.writeText(url).then(() => {
          alert('URL copied to clipboard!');
        });
      });
    });

    // Delete buttons
    document.querySelectorAll('.delete-media-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const mediaId = e.target.dataset.mediaId;
        this.deleteMedia(mediaId);
      });
    });

    // Card hover effect
    document.querySelectorAll('.media-card').forEach(card => {
      card.addEventListener('mouseenter', () => {
        card.style.transform = 'translateY(-5px)';
        card.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
      });
      card.addEventListener('mouseleave', () => {
        card.style.transform = 'translateY(0)';
        card.style.boxShadow = '';
      });
    });
  }

  async handleMediaUpload(files) {
    const progressEl = document.getElementById('mediaUploadProgress');
    const fileInput = document.getElementById('mediaFileInput');
    if (!progressEl) return;

    progressEl.style.display = 'block';
    progressEl.innerHTML = '';

    for (const file of files) {
      const item = document.createElement('div');
      item.style.cssText = 'display:flex; justify-content:space-between; align-items:center; padding:8px 12px; background:#f9fafb; border:1px solid #e5e7eb; border-radius:6px; margin-bottom:8px;';
      item.innerHTML = `<span style="color:#111827; font-size:13px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${file.name}</span><span class="up-status" style="color:#4CAF50; font-size:13px; margin-left:10px;">Uploading...</span>`;
      progressEl.appendChild(item);

      const formData = new FormData();
      formData.append('file', file);

      try {
        const response = await fetch('/api/media/upload', { method: 'POST', body: formData });
        if (!response.ok) {
          const err = await response.json().catch(() => ({ error: 'Upload failed' }));
          throw new Error(err.error || 'Upload failed');
        }
        item.querySelector('.up-status').textContent = '✓ Done';
        item.querySelector('.up-status').style.color = '#10b981';
      } catch (error) {
        item.querySelector('.up-status').textContent = '✗ ' + error.message;
        item.querySelector('.up-status').style.color = '#ef4444';
      }
    }

    if (fileInput) fileInput.value = '';

    // Reload media list after uploads
    await this.loadMedia();
    await this.loadStats();

    // Close modal after short delay and re-render
    setTimeout(async () => {
      const modal = document.getElementById('mediaUploadModal');
      if (modal) modal.style.display = 'none';
      this.renderContent();
      this.attachEventListeners();
    }, 1500);
  }

  viewMedia(mediaId) {
    const media = this.media.find(m => m.id === mediaId);
    if (!media) return;

    // Create modal to view media
    const modal = document.createElement('div');
    modal.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.9);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 10000;
      padding: 20px;
    `;

    const content = media.type === 'image'
      ? `<img src="${media.url}" style="max-width: 90%; max-height: 90%; border-radius: 8px;">`
      : `<video src="${media.url}" controls style="max-width: 90%; max-height: 90%; border-radius: 8px;"></video>`;

    modal.innerHTML = `
      <div style="position: relative; max-width: 90%; max-height: 90%;">
        ${content}
        <button style="position: absolute; top: -40px; right: 0; background: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-size: 16px;">
          ✕ Close
        </button>
      </div>
    `;

    modal.addEventListener('click', () => {
      modal.remove();
    });

    document.body.appendChild(modal);
  }

  async renameMedia(mediaId) {
    const media = this.media.find(m => m.id === mediaId);
    if (!media) return;

    const newName = prompt('Enter new name:', media.original_name);
    if (!newName || newName === media.original_name) return;

    try {
      const response = await fetch(`/api/media/${mediaId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ original_name: newName })
      });

      if (response.ok) {
        await this.loadMedia();
        await this.loadStats();
        this.renderContent();
        this.attachEventListeners();
        alert('Media renamed successfully!');
      } else {
        const error = await response.json();
        alert('Error: ' + error.error);
      }
    } catch (error) {
      console.error('Error renaming media:', error);
      alert('Failed to rename media');
    }
  }

  async deleteMedia(mediaId) {
    const media = this.media.find(m => m.id === mediaId);
    if (!media) return;

    if (!confirm(`Are you sure you want to delete "${media.original_name}"?`)) {
      return;
    }

    try {
      const response = await fetch(`/api/media/${mediaId}`, {
        method: 'DELETE'
      });

      if (response.ok) {
        await this.loadMedia();
        await this.loadStats();
        this.renderContent();
        this.attachEventListeners();
        alert('Media deleted successfully!');
      } else {
        const error = await response.json();
        alert('Error: ' + error.error);
      }
    } catch (error) {
      console.error('Error deleting media:', error);
      alert('Failed to delete media');
    }
  }
}

// Keep backward compatibility
export class MediaLibraryComponent extends MediaComponent {}
