import express from 'express';
import cors from 'cors';
import session from 'express-session';
import rateLimit from 'express-rate-limit';
import dotenv from 'dotenv';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import fs from 'fs';
import { initAppState } from './services/db.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Load environment variables reliably regardless of where the server is started from.
// - Prefer `backend/.env` (next to this file)
// - Fall back to repo root `.env`
// - Support accidental `backend/.env ` (trailing space) to avoid confusing login failures
const envCandidates = [join(__dirname, '.env'), join(__dirname, '.env '), join(__dirname, '..', '.env')];

let loadedEnv = false;
for (const envPath of envCandidates) {
  try {
    if (fs.existsSync(envPath)) {
      const result = dotenv.config({ path: envPath });
      if (!result.error) {
        loadedEnv = true;
        break;
      }
    }
  } catch {
    // ignore
  }
}

if (!loadedEnv) dotenv.config();

const app = express();
const PORT = process.env.PORT || 3001;

// CloudPanel/Nginx runs behind a reverse proxy; trust it so req.ip is correct.
app.set('trust proxy', 1);

// Middleware
app.use(cors({
  origin: true,
  credentials: true
}));
app.use(express.json());

// Sessions / Auth
const SESSION_SECRET = process.env.SESSION_SECRET || 'change-me-in-env';
app.use(session({
  secret: SESSION_SECRET,
  resave: false,
  saveUninitialized: false,
  cookie: {
    httpOnly: true,
    sameSite: 'lax',
    secure: false // set true if you terminate TLS directly in node; behind CloudPanel nginx keep false
  }
}));

function isAuthenticated(req) {
  return !!req.session?.user?.authenticated;
}

function requireAuth(req, res, next) {
  if (isAuthenticated(req)) return next();

  const url = req.originalUrl || req.url || '';

  // API: return 401 (never redirect)
  if (url.startsWith('/api/')) {
    return res.status(401).json({ error: 'Unauthorized' });
  }

  // Pages: redirect to login
  const nextUrl = encodeURIComponent(url || '/');
  return res.redirect(`/login?next=${nextUrl}`);
}

// Serve shared files (rendering engine)
// NOTE: This must be registered BEFORE the general public static handler.
// Otherwise `/shared/*` may be accidentally served from `backend/public/shared/*` if that folder exists.
app.use('/shared', express.static(join(__dirname, 'shared')));

// Serve static files from public directory (new SPA structure)
app.use(express.static(join(__dirname, 'public')));

// Serve files from views directory (for designer assets)
app.use('/css', express.static(join(__dirname, 'views/css')));
app.use('/js', express.static(join(__dirname, 'views/js')));

// Serve media files
app.use('/storage/media', express.static(join(__dirname, 'storage/media')));

// Serve font files
app.use('/storage/fonts', express.static(join(__dirname, 'storage/fonts')));

// Serve TTS audio files
app.use('/storage/tts', express.static(join(__dirname, 'storage/tts')));

// Serve generated videos (avoid clashing with SPA route /videos)
app.use('/generated-videos', express.static(join(__dirname, 'videos')));

// Serve old frontend for backward compatibility (temporary)
app.use('/old', express.static(join(__dirname, 'frontend')));

// Load database (SQLite-backed JSON state)
let db = null;
let saveDb = null;
try {
  const state = await initAppState();
  db = state.db;
  saveDb = state.saveDb;

  console.log('✅ Database loaded successfully (SQLite)');
  console.log(`   - Presets: ${db.presets?.length || 0}`);
  console.log(`   - Questions: ${db.questions?.length || 0}`);
  console.log(`   - Topics: ${db.topics?.length || 0}`);
  console.log(`   - TTS Profiles: ${db.tts_profiles?.length || 0}`);
  console.log(`   - TTS Cache: ${db.tts_cache?.length || 0}`);
} catch (error) {
  console.error('❌ Error loading database:', error.message);
  process.exit(1);
}

// Auth endpoints
const loginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  limit: 20, // max attempts per IP per window
  standardHeaders: 'draft-7',
  legacyHeaders: false,
  message: { error: 'Too many login attempts. Please try again later.' }
});

app.post('/api/auth/login', loginLimiter, (req, res) => {
  // Normalize to avoid whitespace/newline surprises from env files or copy/paste
  const username = String(req.body?.username ?? '').trim();
  const password = String(req.body?.password ?? '');

  const adminUser = String(process.env.ADMIN_USERNAME || 'admin').trim();
  const adminPass = String(process.env.ADMIN_PASSWORD || 'admin');

  if (username === adminUser && password === adminPass) {
    req.session.user = { authenticated: true, username };
    return res.json({ ok: true });
  }

  return res.status(401).json({ error: 'Invalid username or password' });
});

app.post('/api/auth/logout', (req, res) => {
  req.session.destroy(() => {
    res.json({ ok: true });
  });
});

app.get('/api/auth/me', (req, res) => {
  if (!isAuthenticated(req)) return res.status(401).json({ authenticated: false });
  return res.json({ authenticated: true, username: req.session.user.username });
});

app.get('/login', (req, res) => {
  return res.sendFile(join(__dirname, 'views', 'login.html'));
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'ok', 
    message: 'Complete Web Video Generation API',
    database: {
      presets: db.presets?.length || 0,
      questions: db.questions?.length || 0,
      topics: db.topics?.length || 0
    }
  });
});

// Import services
import { TTSService } from './services/ttsService.js';
import { TTSJobManager } from './services/ttsJobManager.js';

// Import routes
import presetsRouter from './routes/presets.js';
import questionsRouter from './routes/questions.js';
import videosRouter from './routes/videos.js';
import mediaRouter from './routes/media.js';
import topicsRouter from './routes/topics.js';
import fontsRouter from './routes/fonts.js';
import { createTTSRouter } from './routes/tts.js';
import csvRouter from './routes/csv.js';

// Simple DB persistence helper
async function persistDb() {
  try {
    if (typeof saveDb === 'function') {
      await saveDb(db);
    }
  } catch (e) {
    console.error('❌ Failed to persist database:', e.message);
  }
}

// Initialize services
const ttsService = new TTSService(db);
const ttsJobManager = new TTSJobManager({ ttsService, saveDb: persistDb, db });

// Best-effort: warm Google voice catalog at startup and persist it once.
(async () => {
  try {
    await ttsService.warmVoiceCatalog();
    const addedIndianEdgeVoices = ttsService.ensureIndianEdgeVoicesEnabled();
    if (db.tts_voice_catalog?.voices?.length || addedIndianEdgeVoices.length > 0) {
      await persistDb();
    }
  } catch (e) {
    // ignore
  }
})();

// Protect all API routes except auth + health
app.use('/api', (req, res, next) => {
  if (req.path.startsWith('/auth/')) return next();
  if (req.path === '/health') return next();
  return requireAuth(req, res, next);
});

// Make db/persistence available to routes
app.locals.db = db;
app.locals.persistDb = persistDb;

// API Routes
app.use('/api/presets', presetsRouter);
app.use('/api/questions', questionsRouter);
app.use('/api/videos', videosRouter);
app.use('/api/media', mediaRouter);
app.use('/api/topics', topicsRouter);
app.use('/api/fonts', fontsRouter);
app.use('/api/tts', createTTSRouter(ttsService, ttsJobManager));
app.use('/api/csv', csvRouter);

// Specific route for designer - serve backend designer.html
app.get('/designer', requireAuth, (req, res) => {
  res.sendFile(join(__dirname, 'views', 'designer.html'));
});

// Serve SPA - all other routes return index.html
app.get('*', (req, res) => {
  // Login page is public
  if (req.path === '/login') {
    return res.sendFile(join(__dirname, 'views', 'login.html'));
  }

  // Protect all HTML app routes
  if (!req.path.startsWith('/storage/') && !req.path.startsWith('/generated-videos') && !req.path.startsWith('/css') && !req.path.startsWith('/js') && !req.path.startsWith('/shared') && !req.path.startsWith('/old')) {
    if (!isAuthenticated(req)) {
      const nextUrl = encodeURIComponent(req.originalUrl || '/');
      return res.redirect(`/login?next=${nextUrl}`);
    }
  }
  // Skip API routes
  if (req.path.startsWith('/api/')) {
    return res.status(404).json({ error: 'API endpoint not found' });
  }

  // Don't override designer route
  if (req.path === '/designer') {
    return res.sendFile(join(__dirname, 'views', 'designer.html'));
  }

  // Serve SPA
  res.sendFile(join(__dirname, 'views', 'index.html'));
});

// Start server
app.listen(PORT, () => {
  console.log('');
  console.log('🚀 Complete Web Video Generation Server (SPA Version)');
  console.log('==========================================');
  console.log(`✅ Server running on http://localhost:${PORT}`);
  console.log(`✅ Application: http://localhost:${PORT}`);
  console.log(`✅ API Health: http://localhost:${PORT}/api/health`);
  console.log('==========================================');
  console.log('');
});
