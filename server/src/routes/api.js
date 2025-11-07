import { Router } from 'express';
import subscriptionRoutes from './subscription.js';
import paymentRoutes from './payment.js';
import upiRoutes from './upiManagement.js';
import mediaRoutes from './media.js';
import { authenticateToken } from '../middleware/auth.js';
import { getPlans } from '../controllers/subscriptionController.js';
import {
    getStats,
    testExternalApi,
    connectObs,
} from '../controllers/mainController.js';

const router = Router();

// Public Routes
router.get('/subscription/plans', getPlans);

// Protected Routes
router.use('/subscription', authenticateToken, subscriptionRoutes);
router.use('/payment', authenticateToken, paymentRoutes);
router.use('/upi', authenticateToken, upiRoutes);
router.use('/media', authenticateToken, mediaRoutes);

router.get('/stats', getStats);
router.post('/test-api', testExternalApi);
router.post('/connect-obs', connectObs);

export default router;
