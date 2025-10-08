const db = require('../db');
const { Op } = require('sequelize');

class OverlayService {
  async getAllOverlays() {
    return db.Overlay.findAll({
      include: [
        {
          model: db.Overlay,
          as: 'Group'
        },
        {
          model: db.Overlay,
          as: 'Members'
        }
      ]
    });
  }

  async getOverlayById(id) {
    return db.Overlay.findByPk(id);
  }

  async getOverlaysByGroupId(groupId) {
    return db.Overlay.findAll({ where: { groupId } });
  }

  async createOverlay(data) {
    // Ensure we only pass valid fields to the database
    const validFields = ['name', 'type', 'content', 'active', 'x', 'y', 'width', 'height', 'fit', 'groupId'];
    const cleanData = {};

    for (const field of validFields) {
      if (data.hasOwnProperty(field)) {
        cleanData[field] = data[field];
      }
    }

    const overlay = await db.Overlay.create(cleanData);
    return overlay.toJSON();
  }

  async updateOverlay(id, data) {
    const overlay = await db.Overlay.findByPk(id);
    if (overlay) {
      // Ensure we only pass valid fields to the database
      const validFields = ['name', 'type', 'content', 'active', 'x', 'y', 'width', 'height', 'fit', 'groupId'];
      const cleanData = {};

      for (const field of validFields) {
        if (data.hasOwnProperty(field)) {
          cleanData[field] = data[field];
        }
      }

      const updatedOverlay = await overlay.update(cleanData);
      return updatedOverlay.toJSON();
    }
    throw new Error('Overlay not found');
  }

  async deleteOverlay(id) {
    const overlay = await db.Overlay.findByPk(id);
    if (overlay) {
      // Ungroup any members before deleting
      await db.Overlay.update({ groupId: null }, { where: { groupId: id } });
      return overlay.destroy();
    }
    throw new Error('Overlay not found');
  }

  async groupOverlays(overlayIds, groupName) {
    try {
      // Validate inputs
      if (!Array.isArray(overlayIds) || overlayIds.length === 0) {
        throw new Error('No overlay IDs provided for grouping');
      }
      if (!groupName || typeof groupName !== 'string' || !groupName.trim()) {
        throw new Error('Invalid group name provided');
      }

      // Validate that all overlay IDs exist
      const existingOverlays = await db.Overlay.findAll({
        where: {
          id: {
            [Op.in]: overlayIds
          }
        }
      });

      if (existingOverlays.length !== overlayIds.length) {
        throw new Error('One or more overlay IDs do not exist');
      }

      // Create a new 'group' overlay
      const groupOverlay = await db.Overlay.create({
        name: groupName.trim(),
        type: 'group',
        // Other fields can be null or have default values for a group
      });

      await db.Overlay.update(
        { groupId: groupOverlay.id },
        { where: { id: { [Op.in]: overlayIds } } }
      );
      return groupOverlay;
    } catch (error) {
      console.error('Error in groupOverlays:', error);
      throw error;
    }
  }

  async ungroupOverlays(groupId) {
    // Remove the group association from members
    await db.Overlay.update({ groupId: null }, { where: { groupId } });
    // Delete the group overlay itself
    const groupOverlay = await db.Overlay.findByPk(groupId);
    if (groupOverlay) {
      await groupOverlay.destroy();
    }
  }

  async removeOverlayFromGroup(overlayId) {
    return db.Overlay.update({ groupId: null }, { where: { id: overlayId } });
  }
}

module.exports = new OverlayService();