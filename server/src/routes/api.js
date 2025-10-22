import { Router } from 'express';
import subscriptionRoutes from './subscription.js';
import {
    getStats,
    testExternalApi,
    connectObs,
} from '../controllers/mainController.js';

const router = Router();

router.use('/subscription', subscriptionRoutes);

router.get('/stats', getStats);
router.post('/test-api', testExternalApi);
router.post('/connect-obs', connectObs);

export default router;
