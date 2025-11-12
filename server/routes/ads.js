const express = require('express');
const { Ad } = require('../models/Ad');
const { checkFileExists } = require('../utils/fileUtils');
const { addThumbnailJob } = require('../src/services/taskService');
const { authenticateToken } = require('../src/middleware/auth.js');
const router = express.Router();
const dialog = require('node-file-dialog');
const { Op } = require('sequelize');

router.use(authenticateToken);

router.get('/select-files', async (req, res) => {
  try {
    let files = await dialog({ type: 'open-files' });
    if (Array.isArray(files)) {
      files = files.map(file => {
        if (typeof file === 'string') {
          return file.trim();
        }
        return file;
      });
    }
    res.json({ files });
  } catch (e) {
    res.json({ files: [] });
  }
});

router.get('/', async (req, res) => {
    try {
      const { groupId } = req.query;
      const where = {};
      if (groupId) {
        where.parentId = groupId;
      } else {
        // only get root items, not all items
        where.parentId = null;
      }
      const ads = await Ad.findAll({
        where,
        order: [['order', 'ASC']],
      });
      res.json(ads);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

router.post('/files', async (req, res) => {
  try {
    const { files, parentId } = req.body;

    if (!files || !Array.isArray(files) || files.length === 0) {
      return res.status(400).json({ error: 'Files array is required' });
    }

    const createdFiles = [];

    for (const file of files) {
      const { name, path: filePath, size, mimeType } = file;
      const fileName = name || filePath.split(/[\\/]/).pop();

      const newFile = await Ad.create({
        name: fileName,
        type: 'file',
        filePath,
        size: size || null,
        mimeType: mimeType || null,
        duration: null,
        dimensions: null,
        parentId: parentId || null,
        status: 'processing',
      });

      createdFiles.push(newFile);
      addThumbnailJob(newFile.id, 'Ad');
    }

    res.status(201).json(createdFiles);
  } catch (error) {
    console.error('Error adding files:', error);
    res.status(500).json({ error: 'Failed to add files' });
  }
});



router.put('/:id/rename', async (req, res) => {
  try {
    const { id } = req.params;
    const { name } = req.body;

    if (!name) {
      return res.status(400).json({ error: 'Name is required' });
    }

    const item = await Ad.findByPk(id);
    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
    }

    await item.update({ name });
    res.json(item);
  } catch (error) {
    console.error('Error renaming item:', error);
    res.status(500).json({ error: 'Failed to rename item' });
  }
});

router.post('/group', async (req, res) => {
    const { name } = req.body;
    try {
      const newGroup = await Ad.create({
        name: name || 'New Group',
        type: 'group',
      });
      res.status(200).json({
        message: `Successfully created group.`,
        group: newGroup,
      });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  });

  router.post('/order', async (req, res) => {
    const { orderedIds, parentId } = req.body;

    if (!orderedIds || !Array.isArray(orderedIds)) {
      return res.status(400).json({ error: 'orderedIds must be an array.' });
    }

    const t = await Ad.sequelize.transaction();

    try {
      for (let i = 0; i < orderedIds.length; i++) {
        const adId = orderedIds[i];
        await Ad.update(
          { order: i },
          {
            where: {
              id: adId,
              parentId: parentId,
            },
            transaction: t,
          }
        );
      }

      await t.commit();
      res.status(200).json({ message: 'Order updated successfully.' });
    } catch (error) {
      await t.rollback();
      res.status(500).json({ error: error.message });
    }
  });

const fs = require('fs');
const path = require('path');

router.delete('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const item = await Ad.findByPk(id);
    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
    }

    // If the item has a thumbnail, delete it from the filesystem
    if (item.thumbnailPath) {
      const thumbnailPath = path.join(__dirname, '..', 'public', item.thumbnailPath);
      if (fs.existsSync(thumbnailPath)) {
        fs.unlink(thumbnailPath, (err) => {
          if (err) {
            console.error('Error deleting thumbnail:', err);
          }
        });
      }
    }

    await item.destroy();
    res.json({ message: 'Item deleted successfully' });
  } catch (error) {
    console.error('Error deleting item:', error);
    res.status(500).json({ error: 'Failed to delete item' });
  }
});

module.exports = router;