import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';
import db from '../db/database.js';
import { callPhpApi } from '../services/phpApiService.js';

dotenv.config();

export const authenticateToken = async (req, res, next) => {
    const authHeader = req.headers['authorization'];
    console.log(`Auth check for ${req.method} ${req.path}, auth header: ${authHeader ? 'present' : 'missing'}`);
    const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

    if (!token) {
        console.log(`No token provided for ${req.method} ${req.path}`);
        return res.status(401).json({ error: 'Unauthorized - No token provided' });
    }

    jwt.verify(token, process.env.JWT_SECRET || 'fallback_secret_key', async (err, user) => {
        if (err) {
            console.error('Token verification error for', req.path, ':', err);
            return res.status(403).json({ error: 'Invalid or expired token' });
        }

        console.log(`Token verified for user ID: ${user.id}`);
        
        // Verify that user still exists in the database
        db.get('SELECT id, name, email FROM users WHERE id = ?', [user.id], async (err, dbUser) => {
            if (err) {
                console.error('Database error checking user:', err);
                return res.status(500).json({ error: 'Internal server error' });
            }

            if (!dbUser) {
                console.log(`User no longer exists in database: ${user.id}`);
                return res.status(403).json({ error: 'User no longer exists' });
            }

            // In production, you should verify with the PHP server as well
            // For now, we'll attach the user info and continue
            req.user = dbUser;
            
            // Optionally verify with PHP server in the background for added security
            try {
                await callPhpApi('/api/v1/action', {
                    action: 'auth',
                    task: 'verify-token',
                    userId: user.id,
                    email: dbUser.email,
                    token: token
                });
            } catch (phpErr) {
                console.error('Token verification with PHP server failed:', phpErr.message);
                // Don't fail the request if PHP server is unavailable, but log the issue
            }
            
            console.log(`Authentication successful for user ${dbUser.id} (${dbUser.email}) on ${req.path}`);
            next();
        });
    });
};