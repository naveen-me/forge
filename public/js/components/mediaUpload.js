/**
 * Media Upload Component
 * Upload images and videos
 */

export class MediaUploadComponent {
  constructor(container) {
    this.container = container;
    this.uploading = false;
  }
  
  render() {
    this.container.innerHTML = `
      <div class="content-header">
        <h1 class="content-title">Upload Media</h1>
        <p class="content-description">Upload images and videos for use in your presets</p>
      </div>
      
      <div class="card media-upload-card">
        <div class="card-body">
          
          <!-- Upload Area -->
          <div id="uploadArea" class="upload-area" style="
            border: 2px dashed #4CAF50;
            border-radius: 10px;
            padding: 60px 20px;
            text-align: center;
            background: #ffffff;
            cursor: pointer;
            transition: all 0.3s;
          ">
            <div class="upload-icon"></div>
            <h3 style="color: #111827; margin-bottom: 10px;">Drop files here or click to browse</h3>
            <p style="color: #6b7280; margin-bottom: 20px;">
              Supports: JPG, PNG, GIF, MP4, WEBM<br>
              Max file size: 100MB
            </p>
            <input type="file" id="fileInput" accept="image/*,video/*" multiple style="display: none;">
            <button class="btn btn-primary" id="browseBtn">
              Browse Files
            </button>
          </div>
          
          <!-- Upload Progress -->
          <div id="uploadProgress" style="display: none; margin-top: 20px;">
            <h4 style="margin-bottom: 15px; color: #111827;">Uploading Files...</h4>
            <div id="progressList"></div>
          </div>
          
          <!-- Upload Results -->
          <div id="uploadResults" style="display: none; margin-top: 20px;"></div>
          
        </div>
      </div>
      
      <!-- Recent Uploads -->
      <div class="card mt-20">
        <div class="card-header">
          <span class="card-title">Recent Uploads</span>
          <button class="btn btn-small btn-secondary" onclick="app.navigate('/media')">
            View All Media
          </button>
        </div>
        <div class="card-body">
          <div id="recentUploads">
            <p style="color: #6b7280; text-align: center;">No recent uploads</p>
          </div>
        </div>
      </div>
    `;
    
    this.setupEventListeners();
    this.loadRecentUploads();
  }
  
  setupEventListeners() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    const browseBtn = document.getElementById('browseBtn');
    
    // Click to browse
    browseBtn.addEventListener('click', (e) => {
      e.preventDefault();
      fileInput.click();
    });
    
    uploadArea.addEventListener('click', () => {
      fileInput.click();
    });
    
    // File input change
    fileInput.addEventListener('change', (e) => {
      if (e.target.files.length > 0) {
        this.handleFiles(Array.from(e.target.files));
      }
    });
    
    // Drag and drop
    uploadArea.addEventListener('dragover', (e) => {
      e.preventDefault();
      uploadArea.style.borderColor = '#45a049';
      uploadArea.style.background = '#f0fbf3';
    });
    
    uploadArea.addEventListener('dragleave', () => {
      uploadArea.style.borderColor = '#4CAF50';
      uploadArea.style.background = '#ffffff';
    });
    
    uploadArea.addEventListener('drop', (e) => {
      e.preventDefault();
      uploadArea.style.borderColor = '#4CAF50';
      uploadArea.style.background = '#ffffff';
      
      const files = Array.from(e.dataTransfer.files).filter(file => 
        file.type.startsWith('image/') || file.type.startsWith('video/')
      );
      
      if (files.length > 0) {
        this.handleFiles(files);
      }
    });
  }
  
  async handleFiles(files) {
    if (this.uploading) {
      alert('Upload in progress. Please wait...');
      return;
    }
    
    this.uploading = true;
    
    // Show progress area
    document.getElementById('uploadProgress').style.display = 'block';
    document.getElementById('uploadResults').style.display = 'none';
    
    const progressList = document.getElementById('progressList');
    progressList.innerHTML = '';
    
    const results = {
      success: [],
      failed: []
    };
    
    // Upload files one by one
    for (const file of files) {
      const progressItem = document.createElement('div');
      progressItem.style.marginBottom = '10px';
      progressItem.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px;">
          <span style="color: #111827;">${file.name}</span>
          <span class="upload-status" style="color: #4CAF50;">⏳ Uploading...</span>
        </div>
      `;
      progressList.appendChild(progressItem);
      
      try {
        await this.uploadFile(file);
        progressItem.querySelector('.upload-status').innerHTML = '✓ Success';
        results.success.push(file.name);
      } catch (error) {
        progressItem.querySelector('.upload-status').innerHTML = '✗ Failed';
        progressItem.querySelector('.upload-status').style.color = '#f44336';
        results.failed.push({ name: file.name, error: error.message });
      }
    }
    
    this.uploading = false;
    
    // Show results
    this.showResults(results);
    
    // Reload recent uploads
    await this.loadRecentUploads();
    
    // Reset file input
    document.getElementById('fileInput').value = '';
  }
  
  async uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch('/api/media/upload', {
      method: 'POST',
      body: formData
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || 'Upload failed');
    }
    
    return await response.json();
  }
  
  showResults(results) {
    const resultsEl = document.getElementById('uploadResults');
    resultsEl.style.display = 'block';
    
    let html = '';
    
    if (results.success.length > 0) {
      html += `
        <div class="alert alert-success">
          <strong>Success!</strong> ${results.success.length} file(s) uploaded successfully.
        </div>
      `;
    }
    
    if (results.failed.length > 0) {
      html += `
        <div class="alert alert-error">
          <strong>Failed!</strong> ${results.failed.length} file(s) failed to upload:
          <ul style="margin-top: 10px; margin-left: 20px;">
            ${results.failed.map(f => `<li>${f.name}: ${f.error}</li>`).join('')}
          </ul>
        </div>
      `;
    }
    
    resultsEl.innerHTML = html;
    
    // Hide progress after 2 seconds
    setTimeout(() => {
      document.getElementById('uploadProgress').style.display = 'none';
    }, 2000);
  }
  
  async loadRecentUploads() {
    try {
      const response = await fetch('/api/media?sortBy=created_at&sortOrder=desc');
      const media = await response.json();
      
      const recent = media.slice(0, 5);
      
      if (recent.length === 0) {
        return;
      }
      
      const recentEl = document.getElementById('recentUploads');
      recentEl.innerHTML = `
        <div class="grid grid-4">
          ${recent.map(m => this.renderRecentItem(m)).join('')}
        </div>
      `;
      
    } catch (error) {
      console.error('Error loading recent uploads:', error);
    }
  }
  
  renderRecentItem(media) {
    const thumbnail = media.type === 'image' 
      ? `<img src="${media.url}" alt="${media.original_name}" style="width: 100%; height: 80px; object-fit: cover; border-radius: 4px 4px 0 0;">`
      : `<div class="media-placeholder media-placeholder--small">
          <span class="icon icon-video"></span>
        </div>`;
    
    return `
      <div class="card" style="padding: 0;">
        ${thumbnail}
        <div style="padding: 10px;">
          <div style="font-size: 12px; color: #111827; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${media.original_name}">
            ${media.original_name}
          </div>
        </div>
      </div>
    `;
  }
}
