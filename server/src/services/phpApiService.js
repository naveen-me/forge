import axios from 'axios';
import crypto from 'crypto';
import dotenv from 'dotenv';
import https from 'https';

dotenv.config();

const agent = new https.Agent({
    rejectUnauthorized: false,
    // Add timeout and other options for better error handling
    timeout: 10000
});

const phpApi = axios.create({
    baseURL: process.env.PHP_API_URL || 'https://sandbox.tarva.co.in',
    httpsAgent: agent,
    timeout: 15000, // 15 seconds timeout (increased for remote server)
    // Additional headers that might be needed
    headers: {
        'User-Agent': 'NodeJS-Client/1.0',
        'Content-Type': 'application/json'
    }
});

const sharedSecret = process.env.SHARED_SECRET || 'your-super-secret-shared-key';

/**
 * Sends a request to the PHP backend with a security signature.
 * @param {string} endpoint The endpoint to call (e.g., '/api/v1/action').
 * @param {object} data The data to send.
 * @returns {Promise<object>} The response from the PHP API.
 */
export const callPhpApi = async (endpoint, data) => {
    const body = JSON.stringify(data);
    const signature = crypto.createHmac('sha256', sharedSecret).update(body).digest('hex');
    
    // Debug logging
    console.log('PHP API Request Debug:');
    console.log('Base URL:', phpApi.defaults.baseURL);
    console.log('Full URL:', `${phpApi.defaults.baseURL}${endpoint}`);
    console.log('Endpoint:', endpoint);
    console.log('Data:', data);
    console.log('Signature:', signature);

    try {
        console.log('Making request to PHP API:', `${phpApi.defaults.baseURL}${endpoint}`);
        const response = await phpApi.post(endpoint, body, {
            headers: {
                'Content-Type': 'application/json',
                'X-Signature': signature,
            },
        });
        
        console.log('PHP API Response status:', response.status);
        console.log('PHP API Response:', response.data);
        return response.data;
    } catch (error) {
        console.error('Full Error Object:', error);
        console.error('Request failed to PHP API at:', `${phpApi.defaults.baseURL}${endpoint}`);
        console.error('Error calling PHP API:', error.response ? error.response.data : error.message);
        console.error('Error status:', error.response ? error.response.status : 'No response');
        console.error('Error code:', error.code);
        console.error('Error message:', error.message);
        throw new Error(error.response ? error.response.data?.message || error.message : `Failed to communicate with PHP backend: ${error.message}`);
    }
};
