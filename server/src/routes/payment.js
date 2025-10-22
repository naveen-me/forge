import express from 'express';
import { PaymentService } from '../services/paymentService.js';
import { authenticateToken } from '../middleware/auth.js';

const router = express.Router();

// Get payment details by ID (public endpoint for status checks)
router.get('/payment/:paymentId', async (req, res) => {
    try {
        const paymentId = parseInt(req.params.paymentId);
        const paymentDetails = await PaymentService.getPaymentDetails(paymentId);
        
        if (!paymentDetails) {
            return res.status(404).json({ error: 'Payment not found' });
        }
        
        res.json({ success: true, data: paymentDetails });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Verify payment and update status (manual verification endpoint)
router.post('/payment/:paymentId/verify', authenticateToken, async (req, res) => {
    try {
        const paymentId = parseInt(req.params.paymentId);
        const verificationData = req.body || {};
        
        const result = await PaymentService.verifyPayment(paymentId, verificationData);
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Cancel a pending payment
router.post('/payment/:paymentId/cancel', authenticateToken, async (req, res) => {
    try {
        const paymentId = parseInt(req.params.paymentId);
        
        const result = await PaymentService.cancelPayment(paymentId);
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get user's pending payments
router.get('/user/payments/pending', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id; // From auth middleware
        
        const pendingPayments = await PaymentService.getUserPendingPayments(userId);
        res.json({ success: true, data: pendingPayments });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Check payment status
router.get('/payment/:paymentId/status', authenticateToken, async (req, res) => {
    try {
        const paymentId = parseInt(req.params.paymentId);
        
        const status = await PaymentService.checkPaymentStatus(paymentId);
        res.json({ success: true, data: status });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Complete payment flow with manual verification (for testing)
router.post('/payment/complete', authenticateToken, async (req, res) => {
    try {
        const { paymentId } = req.body;
        
        if (!paymentId) {
            return res.status(400).json({ error: 'Payment ID is required' });
        }
        
        const result = await PaymentService.verifyPayment(paymentId);
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get user's payment history
router.get('/user/payments', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id; // From auth middleware
        
        const payments = await PaymentService.getUserPaymentHistory(userId);
        res.json({ success: true, data: payments });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

export default router;