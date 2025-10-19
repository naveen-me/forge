import express from 'express';
import http from 'http';
import path from 'path';
import { fileURLToPath } from 'url';
import dotenv from 'dotenv';
import cors from 'cors';
import { WebSocketServer } from 'ws';
import apiRoutes from './src/routes/api.js';
import { initDb } from './src/db/database.js';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const server = http.createServer(app);
const port = process.env.PORT || 4000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Logger Middleware
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
    next();
});

// API Routes
app.use('/api', apiRoutes);

// Serve static files from Vue app
const webappPath = path.join(__dirname, '..', 'webapp', 'dist');
app.use(express.static(webappPath));

// Handle all other routes by serving the Vue app
app.get('*', (req, res) => {
    res.sendFile(path.join(webappPath, 'index.html'));
});

// Initialize Database
initDb().then(() => {
    console.log('Database initialized.');
});

// WebSocket Server
const wss = new WebSocketServer({ server });

wss.on('connection', (ws) => {
    console.log('Client connected to WebSocket');
    ws.on('message', (message) => {
        console.log(`Received message: ${message}`);
        // Broadcast to all clients
        wss.clients.forEach(client => {
            if (client.readyState === ws.OPEN) {
                client.send(`Echo: ${message}`);
            }
        });
    });
    ws.on('close', () => {
        console.log('Client disconnected');
    });
});

// Graceful Shutdown
const shutdown = () => {
    console.log('Shutting down gracefully...');
    server.close(() => {
        console.log('Server closed.');
        process.exit(0);
    });
};

process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);

server.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});
