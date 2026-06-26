import { spawn } from 'child_process';

export function runFfmpeg(args, { logPrefix = 'ffmpeg' } = {}) {
  return new Promise((resolve, reject) => {
    const proc = spawn('ffmpeg', ['-y', ...args], { stdio: ['ignore', 'pipe', 'pipe'] });

    let stderr = '';
    proc.stdout.on('data', (d) => {
      const s = d.toString();
      // keep output small; ffmpeg usually logs to stderr
      if (s.trim()) console.log(`[${logPrefix}]`, s.trim());
    });
    proc.stderr.on('data', (d) => {
      const s = d.toString();
      stderr += s;
      // Avoid extremely verbose spam, but keep useful progress/errors.
      const lines = s.split(/\r?\n/).map(l => l.trim()).filter(Boolean);
      for (const line of lines.slice(-6)) {
        if (line) console.log(`[${logPrefix}]`, line);
      }
    });

    proc.on('error', (err) => reject(err));
    proc.on('close', (code) => {
      if (code === 0) return resolve({ code, stderr });
      const err = new Error(`ffmpeg exited with code ${code}`);
      err.stderr = stderr;
      return reject(err);
    });
  });
}
