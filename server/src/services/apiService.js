import axios from 'axios';
import dotenv from 'dotenv';

dotenv.config();

const api = axios.create({
    baseURL: process.env.API_BASE_URL,
    // You can add headers or other configurations here
});

/**
 * Placeholder function to simulate calling a third-party API.
 * @param {string} endpoint The endpoint to call.
 * @returns {Promise<object>} The data from the API.
 */
export const callExternalApi = async (endpoint) => {
    try {
        // In a real scenario, you would use the 'api' instance:
        // const response = await api.get(endpoint);
        // return response.data;

        // For this boilerplate, we simulate a successful response.
        console.log(`Simulating API call to: ${process.env.API_BASE_URL}/${endpoint}`);
        return Promise.resolve({ 
            status: 'success', 
            message: 'This is a simulated API response.',
            data: {
                timestamp: new Date().toISOString()
            }
        });
    } catch (error) {
        console.error('Error calling external API:', error.message);
        throw new Error('Failed to fetch data from external API.');
    }
};
