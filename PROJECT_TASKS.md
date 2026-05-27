# Project Task Log

## Status Summary
- Storage layer migrated to SQLite-backed JSON state (`services/db.js`), wired in `server.js`.
- Backend refactor in progress: topics/questions schema, video and TTS metadata adjustments.

## Strategy
1. Finish backend schema refactor (remove categories/sets/languages references).
2. Update frontend UI flows to Topic → Questions only.
3. Add CSV import/export + sample CSV download.
4. Clean up legacy references, update dashboard stats.

## Current Tasks
- [x] Add SQLite storage layer and migrate persistence from JSON file (server wired).
- [ ] Refactor backend routes/services to Topic → QA schema (topics/questions, update video + TTS metadata in progress).
- [ ] Update frontend UI (data management, TTS, video generator) to use topics/questions only.
- [ ] Implement CSV import/export with sample download for QAs.
- [ ] Clean up legacy references and update dashboard stats.

## Change Log
- Added `services/db.js` for SQLite-backed state.
- Updated `package.json` to include `sqlite3`.
- `server.js` now uses SQLite-backed state + exposes `db`/`persistDb` via `app.locals`.
- `routes/topics.js` simplified: no categories/languages, topic delete checks for questions.
- `routes/questions.js` replaced to use `topic_id` and `options` array.
- `routes/videos.js` uses request-local db, added `/generate-from-topic`.
- `services/videoGenerator.js` reads options from array.
- `services/ttsService.js` stores `topic_id` in TTS metadata.
