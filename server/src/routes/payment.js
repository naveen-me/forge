import express from 'express';
import { PaymentService } from '../services/paymentService.js';
import { authenticateToken } from '../middleware/auth.js';

const router = express.Router();

// Get payment details by ID
router.get('/:paymentId', authenticateToken, async (req, res) => {
    try {
        const paymentId = req.params.paymentId; // Keep as string since payment IDs may not be numeric
        const token = req.headers.authorization?.split(' ')[1]; // Extract token from header
        
        const paymentDetails = await PaymentService.getPaymentDetails(paymentId, token);
        
        if (!paymentDetails) {
            return res.status(404).json({ error: 'Payment not found' });
        }
        
        res.json({ success: true, data: paymentDetails });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Verify payment and update status (manual verification endpoint)
router.post('/:paymentId/verify', authenticateToken, async (req, res) => {
    try {
        const paymentId = req.params.paymentId; // Keep as string since payment IDs may not be numeric
        const verificationData = req.body || {};
        const token = req.headers.authorization?.split(' ')[1]; // Extract token from header
        
        const result = await PaymentService.verifyPayment(paymentId, verificationData, token);
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Cancel a pending payment
router.post('/:paymentId/cancel', authenticateToken, async (req, res) => {
    try {
        const paymentId = req.params.paymentId; // Keep as string since payment IDs may not be numeric
        const token = req.headers.authorization?.split(' ')[1]; // Extract token from header
        
        const result = await PaymentService.cancelPayment(paymentId, token);
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get user's pending payments
router.get('/user/pending', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id; // From auth middleware
        const token = req.headers.authorization?.split(' ')[1]; // Extract token from header
        
        const pendingPayments = await PaymentService.getUserPendingPayments(userId, token);
        res.json({ success: true, data: pendingPayments });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Check payment status
router.get('/:paymentId/status', authenticateToken, async (req, res) => {
    try {
        const paymentId = req.params.paymentId; // Keep as string since payment IDs may not be numeric
        const token = req.headers.authorization?.split(' ')[1]; // Extract token from header
        
        const status = await PaymentService.checkPaymentStatus(paymentId, token);
        res.json({ success: true, data: status });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Request payment verification - user indicates they have paid, but needs manual verification
router.post('/complete', authenticateToken, async (req, res) => {
    try {
        const { paymentId } = req.body;
        const token = req.headers.authorization?.split(' ')[1]; // Extract token from header
        
        if (!paymentId) {
            return res.status(400).json({ error: 'Payment ID is required' });
        }
        
        // Update payment status to awaiting verification
        const result = await PaymentService.verifyPayment(paymentId, {}, token, 'awaiting_verification');
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Admin endpoint to finalize verified payment (admin confirms payment received and activates subscription)
router.post('/:paymentId/finalize', authenticateToken, async (req, res) => {
    try {
        const paymentId = req.params.paymentId; // Keep as string since payment IDs may not be numeric
        const token = req.headers.authorization?.split(' ')[1]; // Extract token from header
        
        // Update payment status to paid
        const result = await PaymentService.verifyPayment(paymentId, {}, token, 'paid');
        
        // TODO: Add subscription/feature activation logic here
        
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get user's payment history
router.get('/user', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id; // From auth middleware
        const token = req.headers.authorization?.split(' ')[1]; // Extract token from header
        
        const payments = await PaymentService.getUserPaymentHistory(userId, token);
        res.json({ success: true, data: payments });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

export default router;