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
        
        try {
            // Register with PHP API as the main source of truth
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'auth',
                task: 'register',
                name,
                email,
                password,
            });
            
            if (!phpResponse.success) {
                return res.status(400).json(phpResponse);
            }
            
            // Return the response from PHP API which includes the token
            // Since PHP is the source of truth, we should use PHP's response
            res.status(201).json({
                success: true,
                message: 'User registered successfully',
                ...phpResponse // Include any data from PHP API
            });
        } catch (apiError) {
            console.error('PHP API registration failed:', apiError.message);
            // Do not fall back to local DB - this is a security concern
            res.status(500).json({ 
                success: false, 
                error: 'Registration failed - unable to connect to secure authentication service' 
            });
        }
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
        
        try {
            // Authenticate with PHP API as the main source of truth
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'auth',
                task: 'login',
                email,
                password,
            });
            
            if (phpResponse.success && phpResponse.token) {
                // Use PHP API response directly
                res.json({
                    success: true,
                    message: phpResponse.message || 'Login successful',
                    token: phpResponse.token, // Use PHP token directly
                    user: {
                        id: phpResponse.data?.id || phpResponse.user?.id || phpResponse.id || null,
                        name: phpResponse.data?.name || phpResponse.user?.name || phpResponse.name || email.split('@')[0],
                        email: email
                    }
                });
            } else {
                res.status(401).json(phpResponse || { success: false, message: 'Login failed' });
            }
        } catch (apiError) {
            console.error('PHP API login failed:', apiError.message);
            // Pass the actual error message from the PHP API to the client
            res.status(401).json({ 
                success: false, 
                message: apiError.message || 'Invalid credentials' 
            });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
        console.error('Login error:', error);
    }
};
