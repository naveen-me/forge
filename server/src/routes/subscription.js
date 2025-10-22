import { Router } from 'express';
import { 
    getPlans, 
    subscribe, 
    purchaseFeature, 
    getUserSubscription, 
    cancelSubscription 
} from '../controllers/subscriptionController.js';
import { authenticateToken } from '../middleware/auth.js';

const router = Router();

// Public route to get available plans
router.get('/plans', getPlans);

// Protected routes - require authentication
router.post('/subscribe', authenticateToken, subscribe);
router.post('/purchase-feature', authenticateToken, purchaseFeature);
router.get('/my-subscription', authenticateToken, getUserSubscription);
router.delete('/cancel', authenticateToken, cancelSubscription);

export default router;
