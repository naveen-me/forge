'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up (queryInterface, Sequelize) {
    /**
     * Add seed commands here.
     */
    await queryInterface.bulkInsert('MediaLibraries', [
      {
        filename: 'demo-video-1.mp4',
        path: '/path/to/demo-video-1.mp4',
        type: 'video',
        duration: 120,
        metadata: JSON.stringify({ width: 1920, height: 1080 }),
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        filename: 'demo-video-2.mp4',
        path: '/path/to/demo-video-2.mp4',
        type: 'video',
        duration: 90,
        metadata: JSON.stringify({ width: 1280, height: 720 }),
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        filename: 'demo-overlay-1.png',
        path: '/path/to/demo-overlay-1.png',
        type: 'image',
        duration: 0,
        metadata: JSON.stringify({ width: 1920, height: 1080 }),
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ], {});

    await queryInterface.bulkInsert('Overlays', [
      {
        name: 'Logo Overlay',
        type: 'image',
        content: '/path/to/logo.png',
        position: 'top-right',
        size: '200x100',
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        name: 'Lower Third',
        type: 'text',
        content: 'Sample Text Overlay',
        position: 'bottom-left',
        size: '800x150',
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ], {});

    await queryInterface.bulkInsert('Ads', [
      {
        name: 'Commercial Break 1',
        path: '/path/to/commercial-1.mp4',
        duration: 30,
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        name: 'Commercial Break 2',
        path: '/path/to/commercial-2.mp4',
        duration: 45,
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ], {});

    await queryInterface.bulkInsert('SceneTemplates', [
      {
        name: 'Standard Broadcast',
        layout: 'standard',
        defaultSources: JSON.stringify(['Camera', 'Microphone', 'Logo']),
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        name: 'News Studio',
        layout: 'news',
        defaultSources: JSON.stringify(['Camera', 'Microphone', 'Lower Third', 'Ticker']),
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ], {});
  },

  async down (queryInterface, Sequelize) {
    /**
     * Add commands to revert seed here.
     */
    await queryInterface.bulkDelete('MediaLibraries', null, {});
    await queryInterface.bulkDelete('Overlays', null, {});
    await queryInterface.bulkDelete('Ads', null, {});
    await queryInterface.bulkDelete('SceneTemplates', null, {});
  }
};
