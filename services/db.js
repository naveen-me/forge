import fs from 'fs';
import path from 'path';
import sqlite3 from 'sqlite3';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const dbDir = path.join(__dirname, '../data');
const dbPath = path.join(dbDir, 'app.db');

function ensureDir() {
  if (!fs.existsSync(dbDir)) {
    fs.mkdirSync(dbDir, { recursive: true });
  }
}

function defaultState() {
  return {
    presets: [],
    topics: [],
    questions: [],
    tts_profiles: [],
    tts_cache: [],
    tts_sets: [],
    language_phrases: [],
    languages: [
      { code: 'hi-IN', name: 'Hindi', native_name: 'हिन्दी' },
      { code: 'ta-IN', name: 'Tamil', native_name: 'தமிழ்' },
      { code: 'te-IN', name: 'Telugu', native_name: 'తెలుగు' },
      { code: 'mr-IN', name: 'Marathi', native_name: 'मराठी' },
      { code: 'bn-IN', name: 'Bengali', native_name: 'বাংলা' },
      { code: 'kn-IN', name: 'Kannada', native_name: 'ಕನ್ನಡ' },
      { code: 'gu-IN', name: 'Gujarati', native_name: 'ગુજરાતી' },
      { code: 'ml-IN', name: 'Malayalam', native_name: 'മലയാളം' },
      { code: 'pa-IN', name: 'Punjabi', native_name: 'ਪੰਜਾਬੀ' },
      { code: 'en-IN', name: 'English (India)', native_name: 'English' }
    ],
    tts_credentials: {
      configured: false,
      provider: 'google',
      credentials: null,
      project_id: null,
      created_at: null,
      updated_at: null
    },
    tts_voice_catalog: {
      provider: 'google',
      voices: [],
      updated_at: null
    },
    tts_enabled_voices: []
  };
}

function openSqlite() {
  ensureDir();
  return new sqlite3.Database(dbPath);
}

function run(db, sql, params = []) {
  return new Promise((resolve, reject) => {
    db.run(sql, params, function (err) {
      if (err) return reject(err);
      resolve(this);
    });
  });
}

function get(db, sql, params = []) {
  return new Promise((resolve, reject) => {
    db.get(sql, params, (err, row) => {
      if (err) return reject(err);
      resolve(row);
    });
  });
}

export async function initAppState() {
  const sqlite = openSqlite();
  await run(sqlite, 'CREATE TABLE IF NOT EXISTS app_state (id INTEGER PRIMARY KEY, data TEXT NOT NULL)');

  const existing = await get(sqlite, 'SELECT data FROM app_state WHERE id = 1');
  let state;
  if (existing?.data) {
    try {
      state = JSON.parse(existing.data);
      // Migrate: fill in any missing top-level keys from defaultState
      const defaults = defaultState();
      let migrated = false;
      for (const key of Object.keys(defaults)) {
        if (!(key in state)) {
          state[key] = defaults[key];
          migrated = true;
        }
      }
      if (migrated) {
        await run(sqlite, 'UPDATE app_state SET data = ? WHERE id = 1', [JSON.stringify(state)]);
      }
    } catch {
      state = defaultState();
    }
  } else {
    state = defaultState();
    await run(sqlite, 'INSERT INTO app_state (id, data) VALUES (1, ?)', [JSON.stringify(state)]);
  }

  return {
    db: state,
    saveDb: async (nextState) => {
      const payload = nextState || state;
      await run(sqlite, 'UPDATE app_state SET data = ? WHERE id = 1', [JSON.stringify(payload)]);
    },
    sqlite
  };
}

export { dbPath };
