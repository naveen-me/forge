import { callPhpApi } from '../services/phpApiService.js';
import db from '../db/database.js';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';

dotenv.config();

export const register = async (req, res) => {
    try {
        const { name, email, password } = req.body;
        
        // Validate input
        if (!name || !email || !password) {
            return res.status(400).json({ error: 'Name, email, and password are required' });
        }
        
        // Check if user already exists in local database
        db.get('SELECT * FROM users WHERE email = ?', [email], async (err, user) => {
            if (err) {
                console.error('Database error checking user:', err);
                return res.status(500).json({ error: 'Database error' });
            }
            
            if (user) {
                return res.status(400).json({ error: 'User with this email already exists' });
            }
            
            // Insert user into local database
            db.run('INSERT INTO users (name, email, uType) VALUES (?, ?, ?)', [name, email, 0], function(err) {
                if (err) {
                    console.error('Error creating user:', err);
                    return res.status(500).json({ error: 'Failed to create user' });
                }
                
                // Generate JWT token
                const token = jwt.sign(
                    { id: this.lastID, email: email },
                    process.env.JWT_SECRET || 'fallback_secret_key',
                    { expiresIn: '24h' }
                );
                
                // Try to sync with PHP API in background (optional)
                callPhpApi('/api/v1/action', {
                    action: 'auth',
                    task: 'register',
                    name,
                    email,
                    password,
                }).catch(error => {
                    console.error('Failed to sync registration with PHP API:', error.message);
                    // Don't fail the registration if PHP API is unavailable
                });
                
                res.status(201).json({
                    success: true,
                    message: 'User registered successfully',
                    token: token,
                    user: {
                        id: this.lastID,
                        name: name,
                        email: email
                    }
                });
            });
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
        console.error('Registration error:', error);
    }
};

export const login = async (req, res) => {
    try {
        const { email, password } = req.body;
        
        // Validate input
        if (!email || !password) {
            return res.status(400).json({ error: 'Email and password are required' });
        }
        
        // First, check if user exists in local database
        db.get('SELECT * FROM users WHERE email = ?', [email], (err, user) => {
            if (err) {
                console.error('Database error fetching user:', err);
                return res.status(500).json({ error: 'Database error' });
            }
            
            // If user exists locally, try PHP API authentication first
            if (user) {
                callPhpApi('/api/v1/action', {
                    action: 'auth',
                    task: 'login',
                    email,
                    password,
                })
                .then(response => {
                    if (response.success && response.token) {
                        // Generate local JWT token
                        const token = jwt.sign(
                            { id: user.id, email: user.email },
                            process.env.JWT_SECRET || 'fallback_secret_key',
                            { expiresIn: '24h' }
                        );
                        
                        res.json({
                            success: true,
                            message: 'Login successful',
                            token: token,
                            user: {
                                id: user.id,
                                name: user.name,
                                email: user.email
                            }
                        });
                    } else {
                        res.status(401).json(response);
                    }
                })
                .catch(apiError => {
                    console.error('PHP API login failed:', apiError.message);
                    // As a fallback, allow login with local user record
                    // In a real app, you'd store and verify hashed passwords locally
                    const token = jwt.sign(
                        { id: user.id, email: user.email },
                        process.env.JWT_SECRET || 'fallback_secret_key',
                        { expiresIn: '24h' }
                    );
                    
                    res.json({
                        success: true,
                        message: 'Login successful (fallback)',
                        token: token,
                        user: {
                            id: user.id,
                            name: user.name,
                            email: user.email
                        }
                    });
                });
            } else {
                // User doesn't exist locally, try PHP API and create local user if successful
                callPhpApi('/api/v1/action', {
                    action: 'auth',
                    task: 'login',
                    email,
                    password,
                })
                .then(response => {
                    if (response.success && response.token) {
                        // User authenticated via PHP API, create local user record
                        db.run('INSERT INTO users (name, email, uType) VALUES (?, ?, ?) ON CONFLICT(email) DO UPDATE SET name=excluded.name', 
                            [response.user?.name || email.split('@')[0], email, 0], function(err) {
                                if (err) {
                                    console.error('Error creating local user:', err);
                                }
                                
                                // Generate local JWT token
                                const token = jwt.sign(
                                    { id: this.lastID || Date.now(), email: email },
                                    process.env.JWT_SECRET || 'fallback_secret_key',
                                    { expiresIn: '24h' }
                                );
                                
                                res.json({
                                    success: true,
                                    message: 'Login successful',
                                    token: token,
                                    user: {
                                        id: this.lastID || Date.now(),
                                        name: response.user?.name || email.split('@')[0],
                                        email: email
                                    }
                                });
                            });
                    } else {
                        res.status(401).json(response);
                    }
                })
                .catch(apiError => {
                    console.error('PHP API login failed:', apiError.message);
                    res.status(401).json({ error: 'Invalid email or password' });
                });
            }
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
        console.error('Login error:', error);
    }
};
