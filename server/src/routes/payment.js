import express from 'express';
import { logPaymentAction, initiateVerification, getPaymentStatus, cancelPayment, completePayment, getUserPayments } from '../controllers/paymentController.js';
import { authenticateToken } from '../middleware/auth.js';

const router = express.Router();

router.use(authenticateToken);

// Organized by resource operation type:

// Get operations (GET)
router.get('/user', getUserPayments);
router.get('/:paymentId/status', getPaymentStatus);

// Post operations (POST/PUT)
router.post('/log', logPaymentAction);
router.post('/initiate-verification', initiateVerification);
router.post('/complete', completePayment);
router.post('/:paymentId/cancel', cancelPayment);

export default router;