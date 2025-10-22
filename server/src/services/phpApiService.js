import axios from 'axios';
import crypto from 'crypto';
import dotenv from 'dotenv';
import https from 'https';

dotenv.config();

const agent = new https.Agent({
    rejectUnauthorized: false
});

const phpApi = axios.create({
    baseURL: process.env.PHP_API_URL || 'https://sandbox.tarva.co.in',
    httpsAgent: agent
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

    try {
        const response = await phpApi.post(endpoint, body, {
            headers: {
                'Content-Type': 'application/json',
                'X-Signature': signature,
            },
        });
        return response.data;
    } catch (error) {
        console.error('Error calling PHP API:', error.response ? error.response.data : error.message);
        throw new Error(error.response ? error.response.data.message : 'Failed to communicate with PHP backend');
    }
};
