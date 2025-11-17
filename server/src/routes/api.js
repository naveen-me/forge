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

// Public Routes (available without authentication)
router.get('/subscription/plans', getPlans);

// Protected Routes (require authentication)
// Organized by business domain:

// Subscription and payment management
router.use('/subscription', authenticateToken, subscriptionRoutes);
router.use('/payment', authenticateToken, paymentRoutes);
router.use('/upi', authenticateToken, upiRoutes);

// Media and content management
router.use('/media', authenticateToken, mediaRoutes);

// System and utility endpoints
router.get('/stats', authenticateToken, getStats);
router.post('/test-api', authenticateToken, testExternalApi);
router.post('/connect-obs', authenticateToken, connectObs);

export default router;
