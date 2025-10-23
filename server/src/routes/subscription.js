import { Router } from 'express';
import { 
    subscribe, 
    purchaseFeature, 
    getUserSubscription, 
    cancelSubscription,
    verifyAndActivateSubscription
} from '../controllers/subscriptionController.js';
import { authenticateToken } from '../middleware/auth.js';

const router = Router();

// Protected routes - require authentication
router.post('/subscribe', subscribe);
router.post('/purchase-feature', purchaseFeature);
router.get('/my-subscription', getUserSubscription);
router.delete('/cancel', cancelSubscription);
router.post('/verify-and-activate', verifyAndActivateSubscription);

export default router;
