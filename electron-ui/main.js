// This main.js file is kept for compatibility with direct electron execution
// The actual electron build process uses background.js
const { app } = require('electron');

app.quit();
console.log('Please use npm run electron:serve for development or npm run electron:build for production builds.');