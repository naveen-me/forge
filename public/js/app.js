
class App {
  constructor() {
    this.contentEl = document.getElementById('content');
    this.components = {};
    this.currentRoute = null;

    this.init();
  }

  async init() {
    // Basic UI setup
    this.setupNavigation();
    this.setupSidebarToggle();
    this.setupLogout();

    // Register routes
    this.registerRoutes();

    // Handle initial route
    this.handleRoute(window.location.pathname);

    // Handle back/forward navigation
    window.addEventListener('popstate', () => {
      this.handleRoute(window.location.pathname);
    });
  }

  setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
      item.addEventListener('click', (e) => {
        // If it's a regular navigation link (not external)
        if (!item.hasAttribute('data-external')) {
          e.preventDefault();
          const path = item.getAttribute('data-route');
          this.navigate(path);

          // On mobile, close sidebar after navigation
          if (window.innerWidth <= 768) {
            document.getElementById('sidebar').classList.remove('open');
          }
        }
      });
    });
  }

  setupSidebarToggle() {
    const toggleBtn = document.getElementById('toggleSidebar');
    const sidebar = document.getElementById('sidebar');
    const app = document.getElementById('app');

    if (toggleBtn && sidebar) {
      toggleBtn.addEventListener('click', () => {
        if (window.innerWidth <= 768) {
          sidebar.classList.toggle('open');
        } else {
          sidebar.classList.toggle('collapsed');
          if (app) app.classList.toggle('sidebar-collapsed');
        }
      });
    }
  }

  setupLogout() {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', async () => {
        if (confirm('Are you sure you want to logout?')) {
          try {
            const res = await fetch('/logout', { method: 'POST' });
            if (res.ok) {
              window.location.href = '/login';
            }
          } catch (error) {
            console.error('Logout failed:', error);
            window.location.href = '/login';
          }
        }
      });
    }
  }

  registerRoutes() {
    this.routes = {
      '/': this.loadDashboard.bind(this),
      '/designer': this.loadDesigner.bind(this),
      '/videos': this.loadVideos.bind(this),
      '/generated-videos-list': this.loadGeneratedVideos.bind(this),
      '/images': this.loadImages.bind(this),
      '/generated-images-list': this.loadGeneratedImages.bind(this),
      '/tts': this.loadTTS.bind(this),
      '/hierarchy': this.loadHierarchy.bind(this),
      '/questions': this.loadQuestions.bind(this),
      '/media': this.loadMediaLibrary.bind(this),
      '/fonts': this.loadFonts.bind(this)
    };
  }

  navigate(path) {
    // Update browser history
    window.history.pushState({}, '', path);

    // Handle route
    this.handleRoute(path);
  }

  handleRoute(path) {
    // Cleanup active components if navigating away
    if (this.components.tts && path !== '/tts') {
        this.components.tts.cleanup();
    }
    if (this.components.videos && path !== '/videos' && path !== '/generated-videos-list') {
        this.components.videos.cleanup();
    }
    if (this.components.hierarchy && path !== '/hierarchy') {
        if (this.components.hierarchy.cleanup) this.components.hierarchy.cleanup();
    }

    // Update active nav item
    this.updateActiveNav(path);

    // Get route handler
    const handler = this.routes[path];

    if (handler) {
      this.currentRoute = path;
      handler();
    } else {
      this.show404();
    }
  }

  updateActiveNav(path) {
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
      const route = item.getAttribute('data-route');
      if (route === path) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });
  }

  showLoading() {
    this.contentEl.innerHTML = `
      <div class="loading">
        <div class="spinner"></div>
        <p>Loading...</p>
      </div>
    `;
  }

  show404() {
    this.contentEl.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">🚫</div>
        <h2 class="empty-state-title">Page Not Found</h2>
        <p class="empty-state-description">The page you're looking for doesn't exist.</p>
        <button class="btn btn-primary mt-20" onclick="app.navigate('/')">
          Go to Dashboard
        </button>
      </div>
    `;
  }

  // ============================================
  // Route Handlers
  // ============================================

  async loadDashboard() {
    this.showLoading();

    try {
      // Fetch stats
      const [presetsRes, questionsRes, videosRes] = await Promise.all([
        fetch('/api/presets'),
        fetch('/api/questions?limit=1'),
        fetch('/api/videos/list')
      ]);

      const presets = await presetsRes.json();
      const questions = await questionsRes.json();
      const videos = await videosRes.json();

      // Get total question count from health
      const healthRes = await fetch('/api/health');
      const health = await healthRes.json();

      this.contentEl.innerHTML = `
        <div class="content-header">
          <h1 class="content-title">Dashboard</h1>
          <p class="content-description">Welcome to the Quiz Video System</p>
        </div>

        <div class="grid grid-4">
          <div class="card">
            <div class="card-header">
              <span class="card-title">📂 Presets</span>
            </div>
            <div class="card-body">
              <h2 style="font-size: 36px; color: #4CAF50; margin-bottom: 10px;">${presets.length}</h2>
              <p style="color: #999;">Total presets</p>
            </div>
          </div>

          <div class="card">
            <div class="card-header">
              <span class="card-title">❓ Questions</span>
            </div>
            <div class="card-body">
              <h2 style="font-size: 36px; color: #2196F3; margin-bottom: 10px;">${health.database.questions}</h2>
              <p style="color: #999;">Total questions</p>
            </div>
          </div>

          <div class="card">
            <div class="card-header">
              <span class="card-title">🎬 Videos</span>
            </div>
            <div class="card-body">
              <h2 style="font-size: 36px; color: #ff9800; margin-bottom: 10px;">${videos.length}</h2>
              <p style="color: #999;">Generated videos</p>
            </div>
          </div>

          <div class="card">
            <div class="card-header">
              <span class="card-title">📚 Topics</span>
            </div>
            <div class="card-body">
              <h2 style="font-size: 36px; color: #9c27b0; margin-bottom: 10px;">${health.database.topics || 0}</h2>
              <p style="color: #999;">Total topics</p>
            </div>
          </div>
        </div>

        <div class="grid grid-2 mt-30">
          <div class="card">
            <div class="card-header">
              <span class="card-title">🚀 Quick Actions</span>
            </div>
            <div class="card-body">
              <button class="btn btn-primary mb-10" onclick="window.location.href='/designer'" style="width: 100%;">
                🎨 Open Designer
              </button>
              <button class="btn btn-secondary mb-10" onclick="app.navigate('/videos')" style="width: 100%;">
                🎬 Generate Video
              </button>
              <button class="btn btn-primary mb-10" onclick="app.navigate('/questions')" style="width: 100%;">
                ❓ Manage Questions
              </button>
            </div>
          </div>

          <div class="card">
            <div class="card-header">
              <span class="card-title">ℹ️ System Info</span>
            </div>
            <div class="card-body">
              <p style="margin-bottom: 10px;"><strong>Server Status:</strong> <span class="badge badge-success">Online</span></p>
              <p style="margin-bottom: 10px;"><strong>Version:</strong> 2.0.0</p>
              <p style="margin-bottom: 10px;"><strong>Features:</strong> Media Library, Data Management, SPA</p>
            </div>
          </div>
        </div>
      `;

    } catch (error) {
      console.error('Error loading dashboard:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load dashboard data.
        </div>
      `;
    }
  }

  async loadDesigner() {
    this.showLoading();

    // Import designer component
    if (!this.components.designer) {
      const module = await import('/js/components/designer.js');
      this.components.designer = new module.DesignerComponent(this.contentEl);
    }

    this.components.designer.render();
  }

  async loadTTS() {
    this.showLoading();

    try {
      // Import TTS component
      if (!this.components.tts) {
        const module = await import('/js/components/tts.js');
        this.components.tts = new module.TTSComponent();
      }

      // Initialize + render TTS component after it loads languages/profiles
      await this.components.tts.init(this.contentEl);

    } catch (error) {
      console.error('Error loading TTS component:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <h3>Error Loading TTS Component</h3>
          <p>${error.message}</p>
        </div>
      `;
    }
  }

  async loadHierarchy() {
    this.showLoading();

    try {
      // Import hierarchy component
      if (!this.components.hierarchy) {
        const module = await import('/js/components/hierarchy.js');
        this.components.hierarchy = new module.HierarchyComponent(this.contentEl);
        window.hierarchyComponent = this.components.hierarchy;
      }

      await this.components.hierarchy.render();
    } catch (error) {
      console.error('Error loading hierarchy component:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load hierarchical view. ${error.message}
        </div>
      `;
    }
  }

  async loadVideos() {
    this.showLoading();

    try {
      // Import videos component
      if (!this.components.videos) {
        const module = await import('/js/components/videos.js');
        this.components.videos = new module.VideosComponent(this.contentEl);
      }

      await this.components.videos.render();
    } catch (error) {
      console.error('Error loading videos component:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load video generator component. ${error.message}
        </div>
      `;
    }
  }

  async loadGeneratedVideos() {
    this.showLoading();

    try {
      // Import videos component if not already loaded
      if (!this.components.videos) {
        const module = await import('/js/components/videos.js');
        this.components.videos = new module.VideosComponent(this.contentEl);
      }

      await this.components.videos.renderGeneratedVideosPage();
    } catch (error) {
      console.error('Error loading generated videos component:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load generated videos list. ${error.message}
        </div>
      `;
    }
  }

  async loadImages() {
    this.showLoading();

    try {
      // Import images component
      if (!this.components.images) {
        const module = await import('/js/components/images.js');
        this.components.images = new module.ImagesComponent(this.contentEl);
      }

      await this.components.images.render();
    } catch (error) {
      console.error('Error loading images component:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load image generator component. ${error.message}
        </div>
      `;
    }
  }

  async loadGeneratedImages() {
    this.showLoading();

    try {
      // Import images component
      if (!this.components.images) {
        const module = await import('/js/components/images.js');
        this.components.images = new module.ImagesComponent(this.contentEl);
      }

      await this.components.images.renderGeneratedImagesPage();
    } catch (error) {
      console.error('Error loading generated images component:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load generated images list. ${error.message}
        </div>
      `;
    }
  }

  async loadQuestions() {
    this.showLoading();

    try {
      // Import questions component
      if (!this.components.questions) {
        const module = await import('/js/components/questions.js');
        this.components.questions = new module.QuestionsComponent(this.contentEl);
        window.questionsComponent = this.components.questions;
      }

      await this.components.questions.render();
    } catch (error) {
      console.error('Error loading questions component:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load questions component. ${error.message}
        </div>
      `;
    }
  }

  async loadMediaLibrary() {
    this.showLoading();

    // Import media component
    if (!this.components.media) {
      const module = await import('/js/components/media.js');
      this.components.media = new module.MediaComponent(this.contentEl);
      // Expose globally for pagination
      window.mediaComponent = this.components.media;
    }

    this.components.media.render();
  }

  async loadFonts() {
    this.showLoading();

    try {
      // Import fonts component
      if (!this.components.fonts) {
        const FontsManager = (await import('/js/components/fonts.js')).default;
        this.components.fonts = new FontsManager();
      }

      await this.components.fonts.init();
    } catch (error) {
      console.error('Error loading fonts component:', error);
      this.contentEl.innerHTML = `
        <div class="alert alert-error">
          <strong>Error:</strong> Failed to load fonts component. ${error.message}
        </div>
      `;
    }
  }
}

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  window.app = new App();
});
