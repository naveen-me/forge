import express from 'express';
import multer from 'multer';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const router = express.Router();

// Configure storage for font uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const fontsDir = path.join(__dirname, '../storage/fonts');
    
    // Create directory if it doesn't exist
    if (!fs.existsSync(fontsDir)) {
      fs.mkdirSync(fontsDir, { recursive: true });
    }
    
    cb(null, fontsDir);
  },
  filename: (req, file, cb) => {
    // Sanitize filename - remove spaces and special characters
    const safeName = file.originalname.replace(/[^a-zA-Z0-9.-]/g, '_');
    cb(null, Date.now() + '_' + safeName);
  }
});

// File filter - only allow font files
const fileFilter = (req, file, cb) => {
  const allowedExts = ['.ttf', '.otf', '.woff', '.woff2'];
  const ext = path.extname(file.originalname).toLowerCase();
  
  if (allowedExts.includes(ext)) {
    cb(null, true);
  } else {
    cb(new Error('Only font files (.ttf, .otf, .woff, .woff2) are allowed'), false);
  }
};

const upload = multer({ 
  storage: storage,
  fileFilter: fileFilter,
  limits: { fileSize: 10 * 1024 * 1024 } // 10MB limit
});


// Basic binary validation to avoid listing missing/corrupt fonts in the Designer dropdown.
function looksLikeFontFile(filePath) {

  try {
    if (!fs.existsSync(filePath)) return false;

    const fd = fs.openSync(filePath, 'r');
    try {
      const buf = Buffer.alloc(8);
      const bytes = fs.readSync(fd, buf, 0, buf.length, 0);
      if (bytes < 4) return false;

      const sig4 = buf.subarray(0, 4).toString('ascii');

      // WOFF/WOFF2 signatures
      if (sig4 === 'wOFF' || sig4 === 'wOF2') return true;

      // OTF signature
      if (sig4 === 'OTTO') return true;

      // TTF/TrueType collections typically start with 0x00010000 or 'true' or 'ttcf'
      const u32 = buf.readUInt32BE(0);
      if (u32 === 0x00010000) return true;
      if (sig4 === 'true' || sig4 === 'ttcf') return true;

      return false;
    } finally {
      fs.closeSync(fd);
    }
  } catch {
    return false;
  }
}

function purgeInvalidFontsFromDb(db) {
  const fontsDir = path.join(__dirname, '../storage/fonts');
  const fonts = Array.isArray(db.fonts) ? db.fonts : [];

  const validFonts = [];
  const removed = [];

  for (const font of fonts) {
    const filename = font?.filename;
    const filePath = filename ? path.join(fontsDir, filename) : null;

    const isValid = filePath ? looksLikeFontFile(filePath) : false;
    if (isValid) {
      validFonts.push(font);
      continue;
    }

    // Remove from DB; also delete physical file if it exists but is corrupt.
    if (filePath && fs.existsSync(filePath)) {
      try { fs.unlinkSync(filePath); } catch { /* ignore */ }
    }
    removed.push(font);
  }

  const changed = removed.length > 0 || fonts.length !== validFonts.length;
  if (changed) db.fonts = validFonts;

  return { changed, removed };
}

// GET all fonts
router.get('/', async (req, res) => {
  try {
    const db = req.app.locals.db;
    const persistDb = req.app.locals.persistDb;

    if (!db.fonts) db.fonts = [];

    // Auto-clean: remove missing/corrupt fonts from DB and storage.
    const purge = purgeInvalidFontsFromDb(db);
    if (purge.changed) {
      await persistDb();
    }

    const fonts = db.fonts || [];

    // Add search functionality
    const search = req.query.search?.toLowerCase();
    let filteredFonts = fonts;

    if (search) {
      filteredFonts = fonts.filter(font =>
        font.name.toLowerCase().includes(search) ||
        font.filename.toLowerCase().includes(search)
      );
    }

    res.json(filteredFonts);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch fonts' });
  }
});

// GET single font by ID
router.get('/:id', (req, res) => {
  try {
    const db = req.app.locals.db;
    const font = db.fonts?.find(f => f.id === parseInt(req.params.id));

    if (!font) {
      return res.status(404).json({ error: 'Font not found' });
    }

    res.json(font);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch font' });
  }
});

// POST upload new font
// NOTE: multer errors (invalid extension, too large, etc) do not reach the try/catch below
// unless we handle them in the callback form.
router.post('/upload', (req, res) => {
  upload.single('font')(req, res, async (err) => {
    if (err) {
      // Multer/fileFilter/limits errors
      const message = err?.message || 'Upload failed';
      return res.status(400).json({ error: message });
    }

    try {
      if (!req.file) {
        return res.status(400).json({ error: 'No font file uploaded' });
      }

      // Reject corrupt/non-font binaries even if the extension is correct
      if (!looksLikeFontFile(req.file.path)) {
        try { fs.unlinkSync(req.file.path); } catch { /* ignore */ }
        return res.status(400).json({ error: 'Uploaded file is not a valid font' });
      }

      const db = req.app.locals.db;
      const persistDb = req.app.locals.persistDb;

      if (!db.fonts) db.fonts = [];

      const fontName = req.body.name || req.file.originalname.replace(/\.[^/.]+$/, '');

      // Create font entry
      const newFont = {
        id: Date.now(),
        name: fontName,
        filename: req.file.filename,
        originalName: req.file.originalname,
        fileSize: req.file.size,
        format: path.extname(req.file.filename).toLowerCase().replace('.', ''),
        url: `/storage/fonts/${req.file.filename}`,
        uploadedAt: new Date().toISOString()
      };

      db.fonts.push(newFont);
      await persistDb();

      res.status(201).json(newFont);
    } catch (error) {
      console.error('Font upload error:', error);
      res.status(500).json({ error: 'Failed to upload font' });
    }
  });
});

// DELETE font
router.delete('/:id', async (req, res) => {
  try {
    const db = req.app.locals.db;
    const persistDb = req.app.locals.persistDb;

    const fontIndex = db.fonts?.findIndex(f => f.id === parseInt(req.params.id));

    if (fontIndex === -1 || fontIndex === undefined) {
      return res.status(404).json({ error: 'Font not found' });
    }

    const font = db.fonts[fontIndex];

    // Delete physical file
    const fontPath = path.join(__dirname, '../storage/fonts', font.filename);
    if (fs.existsSync(fontPath)) {
      fs.unlinkSync(fontPath);
    }

    // Remove from database
    db.fonts.splice(fontIndex, 1);
    await persistDb();

    res.json({ message: 'Font deleted successfully', id: parseInt(req.params.id) });
  } catch (error) {
    console.error('Font delete error:', error);
    res.status(500).json({ error: 'Failed to delete font' });
  }
});

// PUT update font name
router.put('/:id', async (req, res) => {
  try {
    const db = req.app.locals.db;
    const persistDb = req.app.locals.persistDb;

    const font = db.fonts?.find(f => f.id === parseInt(req.params.id));

    if (!font) {
      return res.status(404).json({ error: 'Font not found' });
    }

    if (req.body.name) {
      font.name = req.body.name;
    }

    await persistDb();

    res.json(font);
  } catch (error) {
    console.error('Font update error:', error);
    res.status(500).json({ error: 'Failed to update font' });
  }
});

export default router;
