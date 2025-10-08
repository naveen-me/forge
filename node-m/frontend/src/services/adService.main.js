const { BrowserWindow } = require('electron');
const db = require('../db');
const path = require('path');
const fileService = require('./fileService.main.js');
const thumbnailService = require('./thumbnailService.main.js');

class AdService {
  // Ad Group methods
  async getAllAdGroups() {
    const groups = await db.AdGroup.findAll({
      attributes: {
        include: [[db.sequelize.fn('COUNT', db.sequelize.col('ads.id')), 'adCount']],
      },
      include: [{
        model: db.Ad,
        as: 'ads',
        attributes: [],
      }],
      group: ['AdGroup.id'],
      order: [['name', 'ASC']],
    });
    return groups.map(group => group.toJSON());
  }

  async createAdGroup(name) {
    const group = await db.AdGroup.create({ name });
    return group.toJSON();
  }

  async renameAdGroup(id, newName) {
    const group = await db.AdGroup.findByPk(id);
    if (group) {
      group.name = newName;
      await group.save();
      return group.toJSON();
    }
    throw new Error('Ad group not found');
  }

  async deleteAdGroup(id) {
    const group = await db.AdGroup.findByPk(id);
    if (group) {
      const ads = await db.Ad.findAll({ where: { adGroupId: id } });
      for (const ad of ads) {
        if (ad.thumbnailPath) {
          await thumbnailService.deleteThumbnail(ad.thumbnailPath);
        }
        await ad.destroy();
      }
      await group.destroy();
      return { success: true };
    }
    throw new Error('Ad group not found');
  }

  // Ad methods
  async getAdsByGroup(adGroupId) {
    const ads = await db.Ad.findAll({
      where: { adGroupId },
      order: [['sortOrder', 'ASC']],
    });
    return ads.map(ad => ad.toJSON());
  }

  async getUnassignedAds() {
    const ads = await db.Ad.findAll({
      where: { adGroupId: null },
      order: [['sortOrder', 'ASC']],
    });
    return ads.map(ad => ad.toJSON());
  }

  async addAdFiles(filePaths, adGroupId) {
    const newItems = [];
    for (const filePath of filePaths) {
      try {
        if (!fileService.isSupportedFile(filePath)) continue;

        const mediaData = {
          filepath: filePath,
          filename: path.basename(filePath),
          displayName: path.basename(filePath),
          status: 'processing',
          adGroupId: adGroupId,
          sortOrder: 0, // Will be updated later
        };
        const newItem = await db.Ad.create(mediaData);
        newItems.push(newItem);
      } catch (error) {
        console.error(`Failed to create initial ad record for ${filePath}:`, error);
      }
    }

    // Start background processing, but don't wait for it
    for (const item of newItems) {
      this.processAdFile(item.id);
    }

    // Return the quickly created items to the UI immediately
    return newItems.map(item => item.toJSON());
  }

  async processAdFile(adId) {
    const adItem = await db.Ad.findByPk(adId);
    if (!adItem) return;

    try {
        const filePath = adItem.filepath;
        const fileType = fileService.getFileType(path.basename(filePath));

        let metadata = {};
        let duration = 0;
        let thumbnailPath = null;

        if (fileType === 'video') {
            const videoMetadata = await fileService.extractVideoMetadata(filePath);
            metadata = videoMetadata;
            duration = videoMetadata.duration;
            thumbnailPath = await thumbnailService.generateVideoThumbnail(filePath, duration);
        } else if (fileType === 'image') {
            const imageMetadata = await fileService.extractImageMetadata(filePath);
            metadata = imageMetadata;
            thumbnailPath = await thumbnailService.generateImageThumbnail(filePath);
        }

        if (thumbnailPath && thumbnailPath.startsWith(thumbnailService.thumbnailDir)) {
            thumbnailPath = path.relative(thumbnailService.thumbnailDir, thumbnailPath);
        }

        await adItem.update({
            displayName: adItem.displayName,
            duration: duration,
            metadata: JSON.stringify(metadata),
            thumbnailPath: thumbnailPath,
            status: 'ready'
        });

        BrowserWindow.getAllWindows()[0]?.webContents.send('ad-item-updated', adItem.toJSON());
    } catch (error) {
        console.error(`Error processing file ${adItem.filepath}:`, error);
        await adItem.update({ status: 'error' });
        // Only send IPC message if Electron is available
        try {
            const { BrowserWindow } = require('electron');
            BrowserWindow.getAllWindows()[0]?.webContents.send('ad-item-updated', adItem.toJSON());
        } catch (ipcError) {
            // Electron not available, ignore
        }
    }
  }

  async deleteAd(id) {
    const ad = await db.Ad.findByPk(id);
    if (ad) {
      if (ad.thumbnailPath) {
        await thumbnailService.deleteThumbnail(ad.thumbnailPath);
      }
      await ad.destroy();
      return { success: true };
    }
    throw new Error('Ad not found');
  }

  async updateAdSortOrder(adId, sortOrder) {
    const ad = await db.Ad.findByPk(adId);
    if (ad) {
      ad.sortOrder = sortOrder;
      await ad.save();
      return ad.toJSON();
    }
    throw new Error('Ad not found');
  }

  async moveAdToGroup(adId, adGroupId) {
    const ad = await db.Ad.findByPk(adId);
    if (ad) {
      ad.adGroupId = adGroupId;
      await ad.save();
      return ad.toJSON();
    }
    throw new Error('Ad not found');
  }

  async regenerateAdThumbnail(adId) {
    const ad = await db.Ad.findByPk(adId);
    if (ad) {
      // Delete the existing thumbnail if it exists
      if (ad.thumbnailPath) {
        await thumbnailService.deleteThumbnail(ad.thumbnailPath);
      }

      // Regenerate the thumbnail
      let newThumbnailPath = await thumbnailService.regenerateThumbnail(ad);

      // Convert to relative path for the thumbnail protocol if needed
      if (newThumbnailPath && newThumbnailPath.startsWith(thumbnailService.thumbnailDir)) {
        newThumbnailPath = path.relative(thumbnailService.thumbnailDir, newThumbnailPath);
      }

      // Update database with new thumbnail path
      await ad.update({ thumbnailPath: newThumbnailPath });

      return ad.toJSON();
    }
    throw new Error('Ad not found');
  }
}

module.exports = new AdService();
