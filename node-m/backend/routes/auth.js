
const express = require('express');
const router = express.Router();
const axios = require('axios');
const https = require('https');
const jwt = require('jsonwebtoken');
const { generateToken } = require('../middleware/auth'); // Use the local JWT token generation

// JWT Secret - should match the one in middleware/auth.js
const JWT_SECRET = process.env.JWT_SECRET || 'your-super-secret-jwt-key-change-in-production';

// New auth module that first calls PHP endpoint and then returns a local Node token for subsequent calls
// This will act as an intermediate service between frontend and PHP auth

// Store for PHP tokens associated with users (in a real app, this would be in a database)
// Maps user ID to PHP token
const phpTokenStore = new Map();

// Helper function to get PHP token from Node JWT
function getPhpTokenFromNodeToken(nodeToken) {
  try {
    const decoded = jwt.verify(nodeToken, JWT_SECRET);
    const userId = decoded.id;
    return phpTokenStore.get(userId); // return the stored PHP token
  } catch (error) {
    console.error('Error decoding Node token:', error.message);
    return null;
  }
}

router.post('/login', async (req, res) => {
    const { email, password } = req.body;

    try {
        const agent = new https.Agent({
            rejectUnauthorized: false
        });

        // Call the PHP endpoint for authentication
        // According to the PHP auth.php code, even for public tasks like login, the data should be wrapped in 'data'
        const phpResponse = await axios.post('https://matrixapi.io/api/v1/action', {
            action: 'auth',
            task: 'login',
            data: { email, password }
        }, {
            headers: {
                'Content-Type': 'application/json'
            },
            httpsAgent: agent,
            timeout: 10000 // Add timeout
        });

        console.log('PHP login response:', phpResponse.data); // Debug logging
        if (phpResponse.data.success) {
            // PHP authentication successful, extract user data
            const phpUser = phpResponse.data.data?.user || { email: email, name: email.split('@')[0] };
            const phpToken = phpResponse.data.data?.token;
            
            console.log('Extracted PHP user:', phpUser, 'PHP token:', phpToken); // Debug logging
            
            console.log('Generating Node token for user:', phpUser || { id: null, email: email, name: email.split('@')[0] }); // Debug logging
            // Generate local Node token for subsequent API calls to Node endpoints
            let nodeToken;
            try {
                nodeToken = generateToken(phpUser || { id: null, email: email, name: email.split('@')[0] });
                console.log('Generated Node token:', nodeToken); // Debug logging
            } catch (tokenError) {
                console.error('Error generating Node token:', tokenError);
                return res.status(500).json({ 
                    success: false, 
                    message: 'Token generation failed: ' + tokenError.message
                });
            }

            // Store the mapping between user ID and PHP token
            // Use the user ID from the PHP response as the key
            if (phpUser && phpUser.id) {
                phpTokenStore.set(phpUser.id, phpToken);
            }
            
            // Return the Node token and user data to the frontend
            // Match the structure used by electron-main proxy: { success: true, data: {...} }
            res.json({ 
                success: true, 
                data: {
                    token: nodeToken, // This is the local Node token, not the PHP token
                    user: phpUser,
                    phpToken: phpToken // Include PHP token if needed for other operations
                }
            });
        } else {
            console.error('PHP login failed:', phpResponse.data.message || phpResponse.data.error); // Debug logging
            res.status(401).json({ 
                success: false, 
                message: phpResponse.data.message || phpResponse.data.error || 'Invalid credentials' 
            });
        }
    } catch (error) {
        console.error('Authentication failed:', error.message);
        console.error('Error details:', error.code, error.errno, error.syscall); // Additional error details
        if (error.response) {
            console.error('Error response from PHP:', error.response.data);
            console.error('Error response status:', error.response.status);
        }
        res.status(500).json({ 
            success: false, 
            message: error.response?.data?.message || error.message || error.code || 'Authentication failed. Please try again later.' 
        });
    }
});

router.post('/register', async (req, res) => {
    const { name, email, password } = req.body;

    try {
        const agent = new https.Agent({
            rejectUnauthorized: false
        });

        // Call the PHP endpoint for registration
        // According to the PHP auth.php code, even for public tasks like register, the data should be wrapped in 'data'
        const phpResponse = await axios.post('https://matrixapi.io/api/v1/action', {
            action: 'auth',
            task: 'register',
            data: { name, email, password }
        }, {
            headers: {
                'Content-Type': 'application/json'
            },
            httpsAgent: agent,
            timeout: 10000 // Add timeout
        });

        console.log('PHP register response:', phpResponse.data); // Debug logging
        if (phpResponse.data.success) {
            // Registration successful
            const phpUser = phpResponse.data.data?.user || { email: email, name: name };
            const phpToken = phpResponse.data.data?.token;
            
            console.log('Extracted PHP user:', phpUser, 'PHP token:', phpToken); // Debug logging
            
            console.log('Generating Node token for registration user:', phpUser || { id: null, email: email, name: name }); // Debug logging
            // Generate local Node token for subsequent API calls to Node endpoints
            let nodeToken;
            try {
                nodeToken = generateToken(phpUser || { id: null, email: email, name: name });
                console.log('Generated Node token for registration:', nodeToken); // Debug logging
            } catch (tokenError) {
                console.error('Error generating Node token for registration:', tokenError);
                return res.status(500).json({ 
                    success: false, 
                    message: 'Token generation failed: ' + tokenError.message
                });
            }

            // Store the mapping between user ID and PHP token
            if (phpUser && phpUser.id) {
                phpTokenStore.set(phpUser.id, phpToken);
            }
            
            console.log('Sending registration response to frontend:', { 
                success: true, 
                data: {
                    token: nodeToken, // This is the local Node token
                    user: phpUser,
                    phpToken: phpToken // Include PHP token if needed for other operations
                }
            }); // Debug logging
            
            // Return the Node token and user data to the frontend
            // Match the structure used by electron-main proxy: { success: true, data: {...} }
            res.json({ 
                success: true, 
                data: {
                    token: nodeToken, // This is the local Node token
                    user: phpUser,
                    phpToken: phpToken // Include PHP token if needed for other operations
                }
            });
        } else {
            console.error('PHP register failed:', phpResponse.data.message || phpResponse.data.error); // Debug logging
            res.status(400).json({ 
                success: false, 
                message: phpResponse.data.message || phpResponse.data.error || 'Registration failed' 
            });
        }
    } catch (error) {
        console.error('Registration failed:', error.message);
        console.error('Registration error details:', error.code, error.errno, error.syscall); // Additional error details
        if (error.response) {
            console.error('Error response from PHP on register:', error.response.data);
            console.error('Error response status on register:', error.response.status);
        }
        res.status(500).json({ 
            success: false, 
            message: error.response?.data?.message || error.message || error.code || 'Registration failed. Please try again later.' 
        });
    }
});

router.post('/set-pin', async (req, res) => {
    const { token, pin } = req.body;

    try {
        const agent = new https.Agent({
            rejectUnauthorized: false
        });

        // Get the corresponding PHP token from the Node JWT
        const phpToken = getPhpTokenFromNodeToken(token);
        if (!phpToken) {
            return res.status(401).json({ 
                success: false, 
                message: 'Invalid or expired authentication token' 
            });
        }

        // For set-pin (non-public task), data should be wrapped in 'data' key
        const phpResponse = await axios.post('https://matrixapi.io/api/v1/action', {
            action: 'auth',
            task: 'set-pin',
            token: phpToken, // Use the actual PHP token
            data: { pin } // Wrap pin in data object
        }, {
            headers: {
                'Content-Type': 'application/json'
            },
            httpsAgent: agent
        });

        if (phpResponse.data.success) {
            // Pin set successfully
            res.json({ 
                success: true,
                data: phpResponse.data.data || phpResponse.data 
            });
        } else {
            res.status(400).json({ 
                success: false, 
                message: phpResponse.data.message || phpResponse.data.error || 'Failed to set pin' 
            });
        }
    } catch (error) {
        console.error('Set PIN failed:', error.message);
        res.status(500).json({ 
            success: false, 
            message: error.response?.data?.message || error.message || 'Set PIN failed. Please try again later.' 
        });
    }
});

router.post('/validate-pin', async (req, res) => {
    const { token, pin } = req.body;

    try {
        const agent = new https.Agent({
            rejectUnauthorized: false
        });

        // Get the corresponding PHP token from the Node JWT
        const phpToken = getPhpTokenFromNodeToken(token);
        if (!phpToken) {
            return res.status(401).json({ 
                success: false, 
                message: 'Invalid or expired authentication token' 
            });
        }

        // For validate-pin (non-public task), data should be wrapped in 'data' key
        const phpResponse = await axios.post('https://matrixapi.io/api/v1/action', {
            action: 'auth',
            task: 'validate-pin',
            token: phpToken, // Use the actual PHP token
            data: { pin } // Wrap pin in data object
        }, {
            headers: {
                'Content-Type': 'application/json'
            },
            httpsAgent: agent
        });

        if (phpResponse.data.success) {
            // Pin validated successfully
            res.json({ 
                success: true,
                data: phpResponse.data.data || phpResponse.data 
            });
        } else {
            res.status(401).json({ 
                success: false, 
                message: phpResponse.data.message || phpResponse.data.error || 'Invalid PIN' 
            });
        }
    } catch (error) {
        console.error('Validate PIN failed:', error.message);
        res.status(500).json({ 
            success: false, 
            message: error.response?.data?.message || error.message || 'PIN validation failed. Please try again later.' 
        });
    }
});

router.post('/validate-token', async (req, res) => {
    const { token } = req.body;

    try {
        const agent = new https.Agent({
            rejectUnauthorized: false
        });

        // Get the corresponding PHP token from the Node JWT
        const phpToken = getPhpTokenFromNodeToken(token);
        if (!phpToken) {
            return res.status(401).json({ 
                success: false, 
                message: 'Invalid or expired authentication token' 
            });
        }

        // For validate-token (non-public task), data is sent directly (token is the main param)
        const phpResponse = await axios.post('https://matrixapi.io/api/v1/action', {
            action: 'auth',
            task: 'validate-token',
            token: phpToken // Use the actual PHP token
        }, {
            headers: {
                'Content-Type': 'application/json'
            },
            httpsAgent: agent
        });

        if (phpResponse.data.success) {
            // Token is valid
            res.json({ 
                success: true, 
                data: phpResponse.data.data || phpResponse.data 
            });
        } else {
            res.status(401).json({ 
                success: false, 
                message: phpResponse.data.message || phpResponse.data.error || 'Invalid token' 
            });
        }
    } catch (error) {
        console.error('Token validation failed:', error.message);
        res.status(500).json({ 
            success: false, 
            message: error.response?.data?.message || error.message || 'Token validation failed. Please try again later.' 
        });
    }
});

module.exports = router;
