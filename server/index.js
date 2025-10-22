import express from 'express';
import http from 'http';
import path from 'path';
import { fileURLToPath } from 'url';
import dotenv from 'dotenv';
import cors from 'cors';
import { WebSocketServer } from 'ws';

// Import routes
import apiRoutes from './src/routes/api.js';
import authRoutes from './src/routes/auth.js';
import subscriptionRoutes from './src/routes/subscription.js';
import paymentRoutes from './src/routes/payment.js';
import upiManagementRoutes from './src/routes/upiManagement.js';

// Import database initializer
import { initDb } from './src/db/database.js';

// Basic setup
dotenv.config();
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// App and server initialization
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
app.use('/api/auth', authRoutes);
app.use('/api/subscription', subscriptionRoutes);
app.use('/api/payment', paymentRoutes);
app.use('/api/upi', upiManagementRoutes);

// Serve static files from Vue app
const webappPath = path.join(__dirname, '..', 'webapp', 'dist');
app.use(express.static(webappPath));

// Handle all other routes by serving the Vue app
app.get('*', (req, res) => {
    res.sendFile(path.join(webappPath, 'index.html'), (err) => {
        if (err) {
            res.status(500).send(err);
        }
    });
});

// Initialize Database
initDb().then(() => {
    console.log('Database initialized.');
}).catch(err => {
    console.error('Database initialization failed:', err);
});

// WebSocket Server
const wss = new WebSocketServer({ server });
wss.on('connection', (ws) => {
    console.log('Client connected to WebSocket');
    ws.on('message', (message) => {
        console.log(`Received message: ${message}`);
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

// Start the server
server.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});