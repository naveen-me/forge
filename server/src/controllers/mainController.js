import db from '../db/database.js';
import { callExternalApi } from '../services/apiService.js';
import { connectToObs } from '../services/obsService.js';

export const getStats = (req, res) => {
    db.all("SELECT * FROM stats", [], (err, rows) => {
        if (err) {
            res.status(500).json({ error: err.message });
            return;
        }
        res.json({ data: rows });
    });
};

export const testExternalApi = async (req, res) => {
    try {
        const data = await callExternalApi('test-endpoint');
        res.json(data);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

export const connectObs = async (req, res) => {
    try {
        const { address, password } = req.body;
        if (!address || !password) {
            return res.status(400).json({ error: 'Address and password are required.' });
        }
        const message = await connectToObs({ address, password });
        res.json({ message });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};
