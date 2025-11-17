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
// Organized by resource operation type:

// Get operations (GET)
router.get('/my-subscription', getUserSubscription);

// Post operations (POST/PUT)
router.post('/subscribe', subscribe);
router.post('/purchase-feature', purchaseFeature);
router.post('/verify-and-activate', verifyAndActivateSubscription);

// Delete operations (DELETE)
router.delete('/cancel', cancelSubscription);

export default router;
