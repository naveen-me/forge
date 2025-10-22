import express from 'express';
import { PaymentService } from '../services/paymentService.js';
import { authenticateToken } from '../middleware/auth.js';
import { callPhpApi } from '../services/phpApiService.js';

const router = express.Router();

// Get all system UPI details
router.get('/upi-details', authenticateToken, async (req, res) => {
    try {
        // Get from PHP server
        const phpResponse = await callPhpApi('/api/v1/action', {
            action: 'upi',
            task: 'get-all'
        });
        
        res.json(phpResponse);
    } catch (error) {
        console.error('Error fetching UPI details from PHP server:', error);
        res.status(500).json({ error: error.message });
    }
});

// Add a new UPI detail
router.post('/upi-details', authenticateToken, async (req, res) => {
    try {
        const { upiId, upiVpa, displayName } = req.body;
        
        if (!upiId || !upiVpa) {
            return res.status(400).json({ error: 'UPI ID and UPI VPA are required' });
        }
        
        // Add to PHP server
        const phpResponse = await callPhpApi('/api/v1/action', {
            action: 'upi',
            task: 'add',
            upiId,
            upiVpa,
            displayName: displayName || upiId
        });
        
        res.json(phpResponse);
    } catch (error) {
        console.error('Error adding UPI detail to PHP server:', error);
        res.status(500).json({ error: error.message });
    }
});

// Update UPI detail
router.put('/upi-details/:id', authenticateToken, async (req, res) => {
    try {
        const id = parseInt(req.params.id);
        const { upiId, upiVpa, displayName, isActive, isPrimary } = req.body;
        
        // Update in PHP server
        const phpResponse = await callPhpApi('/api/v1/action', {
            action: 'upi',
            task: 'update',
            id,
            upiId,
            upiVpa,
            displayName,
            isActive,
            isPrimary
        });
        
        res.json(phpResponse);
    } catch (error) {
        console.error('Error updating UPI detail in PHP server:', error);
        res.status(500).json({ error: error.message });
    }
});

// Set primary UPI
router.put('/upi-details/:id/set-primary', authenticateToken, async (req, res) => {
    try {
        const id = parseInt(req.params.id);
        
        // Set primary in PHP server
        const phpResponse = await callPhpApi('/api/v1/action', {
            action: 'upi',
            task: 'set-primary',
            id
        });
        
        res.json(phpResponse);
    } catch (error) {
        console.error('Error setting primary UPI in PHP server:', error);
        res.status(500).json({ error: error.message });
    }
});

// Delete UPI detail
router.delete('/upi-details/:id', authenticateToken, async (req, res) => {
    try {
        const id = parseInt(req.params.id);
        
        // Delete from PHP server
        const phpResponse = await callPhpApi('/api/v1/action', {
            action: 'upi',
            task: 'delete',
            id
        });
        
        res.json(phpResponse);
    } catch (error) {
        console.error('Error deleting UPI detail from PHP server:', error);
        res.status(500).json({ error: error.message });
    }
});

export default router;