import express from 'express';
import { logPaymentAction, initiateVerification, getPaymentStatus, cancelPayment, completePayment, getUserPayments } from '../controllers/paymentController.js';
import { authenticateToken } from '../middleware/auth.js';

const router = express.Router();

router.use(authenticateToken);

router.post('/log', logPaymentAction);
router.post('/initiate-verification', initiateVerification);
router.get('/:paymentId/status', getPaymentStatus);
router.post('/:paymentId/cancel', cancelPayment);
router.post('/complete', completePayment);
router.get('/user', getUserPayments);

export default router;