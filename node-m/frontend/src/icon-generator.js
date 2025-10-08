const { nativeImage } = require('electron');

function createStatusIcon(color) {
  const size = 16;
  const canvas = document.createElement('canvas');
  canvas.width = size;
  canvas.height = size;
  const context = canvas.getContext('2d');

  context.beginPath();
  context.arc(size / 2, size / 2, size / 2, 0, 2 * Math.PI);
  context.fillStyle = color;
  context.fill();

  return nativeImage.createFromDataURL(canvas.toDataURL());
}

module.exports = {
  createStatusIcon,
};