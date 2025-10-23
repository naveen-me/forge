import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';
import db from '../db/database.js'; // Keep for fallback scenarios where critical
import { callPhpApi } from '../services/phpApiService.js';

dotenv.config();

export const authenticateToken = async (req, res, next) => {
    // Check for authorization header in multiple possible formats
    const authHeader = req.headers['authorization'] || 
                      req.headers['Authorization'] || 
                      req.get('Authorization') || 
                      req.get('authorization');
    
    console.log(`Auth check for ${req.method} ${req.path}, auth header:`, authHeader);
    console.log('All headers:', req.headers);
    
    if (!authHeader) {
        console.log(`No authorization header found for ${req.method} ${req.path}`);
        console.log('Raw headers keys:', Object.keys(req.headers));
        return res.status(401).json({ error: 'Unauthorized - No token provided' });
    }
    
    // Extract token from "Bearer <token>" format
    const tokenMatch = authHeader.match(/^Bearer\s+(.+)$/i);
    const token = tokenMatch ? tokenMatch[1] : null;

    if (!token) {
        console.log(`No valid Bearer token found in header: ${authHeader}`);
        return res.status(401).json({ error: 'Unauthorized - Invalid token format' });
    }

    try {
        // First verify the JWT token
        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'fallback_secret_key');
        
        console.log(`Token verified for user:`, decoded);
        
        // Verify user with PHP server as the primary source of truth
        try {
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'auth',
                task: 'verify-token',
                userId: decoded.data.id || null,
                email: decoded.data.email || null,
                token: token
            });
            
            if (phpResponse.success) {
                // PHP verification successful
                req.user = {
                    id: decoded.data.id || null,
                    email: decoded.data.email || null,
                    name: decoded.data.name || 'User'
                };
                
                console.log(`Authentication successful via PHP for user ${req.user.id} (${req.user.email}) on ${req.path}`);
                next();
                return;
            } else {
                console.log(`Token verification failed with PHP server for token:`, decoded);
                return res.status(403).json({ error: phpResponse.message || 'Invalid or expired token' });
            }
        } catch (phpErr) {
            console.error('Token verification with PHP server failed:', phpErr.message);
            return res.status(403).json({ 
                error: phpErr.message || 'Authentication service failed.' 
            });
        }
    } catch (err) {
        console.error('Token verification error for', req.path, ':', err);
        return res.status(403).json({ error: 'Invalid or expired token' });
    }
};