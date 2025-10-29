
import { PaymentService } from '../services/paymentService.js';
import db from '../db/database.js';

import { callPhpApi } from '../services/phpApiService.js';

export const logPaymentAction = async (req, res) => {
    try {
        const { paymentId, action, details } = req.body;
        const userId = req.user.id;

        if (!paymentId || !action) {
            return res.status(400).json({ error: 'Payment ID and action are required' });
        }

        const response = await callPhpApi('/api/v1/action', {
            action: 'payment',
            task: 'log-action',
            paymentId: paymentId,
            action: action,
            details: details,
            token: req.headers.authorization?.split(' ')[1]
        });

        if (response.success) {
            res.json({ success: true, message: 'Payment action logged successfully' });
        } else {
            res.status(500).json({ error: 'Failed to log payment action' });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

export const initiateVerification = async (req, res) => {
    try {
        const { paymentId } = req.body;
        const userId = req.user.id;

        if (!paymentId) {
            return res.status(400).json({ error: 'Payment ID is required' });
        }

        const result = await PaymentService.verifyPayment(paymentId, {}, req.headers.authorization?.split(' ')[1], 'verifying');

        res.json({
            success: true,
            message: 'Payment verification initiated',
            result: result
        });
    } catch (error) {
        console.error('Error initiating payment verification:', error);
        res.status(500).json({ error: error.message });
    }
};

export const getPaymentStatus = async (req, res) => {
    try {
        const { paymentId } = req.params;
        const userId = req.user.id;

        if (!paymentId) {
            return res.status(400).json({ error: 'Payment ID is required' });
        }

        const status = await PaymentService.checkPaymentStatus(paymentId, req.headers.authorization?.split(' ')[1]);

        if (status) {
            res.json({ success: true, data: status });
        } else {
            res.status(404).json({ success: false, error: 'Payment not found' });
        }
    } catch (error) {
        console.error('Error getting payment status:', error);
        res.status(500).json({ error: error.message });
    }
};

export const cancelPayment = async (req, res) => {
    try {
        const { paymentId } = req.params;
        const userId = req.user.id;

        if (!paymentId) {
            return res.status(400).json({ error: 'Payment ID is required' });
        }

        const result = await PaymentService.cancelPayment(paymentId, req.headers.authorization?.split(' ')[1]);

        res.json({
            success: true,
            message: 'Payment cancelled',
            result: result
        });
    } catch (error) {
        console.error('Error cancelling payment:', error);
        res.status(500).json({ error: error.message });
    }
}

export const completePayment = async (req, res) => {
    try {
        const { paymentId } = req.body;
        const userId = req.user.id;

        if (!paymentId) {
            return res.status(400).json({ error: 'Payment ID is required' });
        }

        const result = await PaymentService.verifyPayment(paymentId, {}, req.headers.authorization?.split(' ')[1], 'paid');

        res.json({
            success: true,
            message: 'Payment completed',
            result: result
        });
    } catch (error) {
        console.error('Error completing payment:', error);
        res.status(500).json({ error: error.message });
    }
}

export const getUserPayments = async (req, res) => {
    try {
        const userId = req.user.id;
        const payments = await PaymentService.getUserPaymentHistory(userId, req.headers.authorization?.split(' ')[1]);
        res.json({ success: true, data: payments });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};
