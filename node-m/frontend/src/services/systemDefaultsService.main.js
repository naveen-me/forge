const db = require('../db');

class SystemDefaultsService {
  async getAllDefaults() {
    const defaults = await db.SystemDefaults.findAll();
    // Convert array to a key-value object
    return defaults.reduce((acc, item) => {
      acc[item.key] = item.value;
      return acc;
    }, {});
  }

  async getDefault(key) {
    const setting = await db.SystemDefaults.findByPk(key);
    return setting ? setting.value : null;
  }

  async setDefault(key, value) {
    const [setting, created] = await db.SystemDefaults.findOrCreate({
      where: { key },
      defaults: { value },
    });

    if (!created) {
      await setting.update({ value });
    }

    return setting;
  }
}

module.exports = new SystemDefaultsService();
