import fs from 'fs';
import path from 'path';
import { execSync } from 'child_process';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const outputDir = path.join(__dirname, 'output');
const executableName = 'playout.exe';
const nodePath = process.execPath;

// 1. Create output directory if it doesn't exist
if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
}

// 2. Create the blob for SEA
console.log('Creating SEA blob...');
const seaBlobPath = path.join(outputDir, 'playout.blob');
execSync(`node --experimental-sea-config sea.config.json`, {
    cwd: path.join(__dirname, '..', 'build'),
    stdio: 'inherit' 
});

// 3. Copy the Node.js executable
console.log(`Copying Node.js executable to ${executableName}...`);
const exePath = path.join(outputDir, executableName);
fs.copyFileSync(nodePath, exePath);

// 4. Remove the signature from the copied executable
console.log('Removing signature from executable...');
try {
    execSync(`signtool remove /s "${exePath}"`, { stdio: 'inherit' });
} catch (error) {
    console.warn('Could not remove signature. This is expected if signtool is not installed or the file is not signed.');
}

// 5. Inject the blob into the executable
console.log('Injecting blob into executable...');
execSync(`postject "${exePath}" NODE_SEA_BLOB "${seaBlobPath}" --sentinel-fuse NODE_SEA_FUSE_fce680ab2cc467b6e072b8b5df1996b2`, { stdio: 'inherit' });

console.log(`
✅ Success! Single executable created at:
${path.resolve(exePath)}
`);
