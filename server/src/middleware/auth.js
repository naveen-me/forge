import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';
import crypto from 'crypto';
import db from '../db/database.js'; // Keep for fallback scenarios where critical
import { callPhpApi } from '../services/phpApiService.js';

dotenv.config();

// In-memory cache for token validation results
const tokenCache = new Map();

// Function to get the cache key based on token
const getTokenCacheKey = (token) => {
    return crypto.createHash('sha256').update(token).digest('hex');
};

// Function to check if cache is still valid (expire after 12 hours)
const isCacheValid = (timestamp) => {
    const twelveHours = 12 * 60 * 60 * 1000; // 12 hours in milliseconds
    return (Date.now() - timestamp) < twelveHours;
};

export const authenticateToken = async (req, res, next) => {
    // Check for authorization header in multiple possible formats
    const authHeader = req.headers['authorization'] || 
                      req.headers['Authorization'] || 
                      req.get('Authorization') || 
                      req.get('authorization');
    
    if (!authHeader) {
        return res.status(401).json({ error: 'Unauthorized - No token provided' });
    }
    
    // Extract token from "Bearer <token>" format
    const tokenMatch = authHeader.match(/^Bearer\s+(.+)$/i);
    const token = tokenMatch ? tokenMatch[1] : null;

    if (!token) {
        return res.status(401).json({ error: 'Unauthorized - Invalid token format' });
    }

    try {
        // First verify the JWT token locally (this is fast)
        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'fallback_secret_key');
        
        // Get cache key for this token
        const cacheKey = getTokenCacheKey(token);

        // Check if we have a valid cached result for this token
        const cachedResult = tokenCache.get(cacheKey);
        if (cachedResult && isCacheValid(cachedResult.timestamp)) {
            // Use cached result - token is valid
            req.user = cachedResult.user;
            next();
            return;
        }

        // Cached result is not valid or doesn't exist, call PHP server
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
                const user = {
                    id: decoded.data.id || null,
                    email: decoded.data.email || null,
                    name: decoded.data.name || 'User'
                };

                // Cache the successful result for 12 hours
                tokenCache.set(cacheKey, {
                    user: user,
                    timestamp: Date.now()
                });

                req.user = user;
                next();
                return;
            } else {
                return res.status(403).json({ error: phpResponse.message || 'Invalid or expired token' });
            }
        } catch (phpErr) {
            return res.status(403).json({ 
                error: phpErr.message || 'Authentication service failed.' 
            });
        }
    } catch (err) {
        // If JWT verification fails, remove from cache if it exists
        const cacheKey = getTokenCacheKey(token);
        tokenCache.delete(cacheKey);
        
        return res.status(403).json({ error: 'Invalid or expired token' });
    }
};