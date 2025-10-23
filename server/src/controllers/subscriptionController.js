import { callPhpApi } from '../services/phpApiService.js';
import db from '../db/database.js';
import { v4 as uuidv4 } from 'uuid';
import { PaymentService } from '../services/paymentService.js';

// Helper function to format dates
const formatDate = (date) => {
    return date.toISOString().split('T')[0];
};

export const getPlans = async (req, res) => {
    try {
        // Always fetch plans from PHP server as source of truth - NO LOCAL DB FALLBACK
        const response = await callPhpApi('/api/v1/action', {
            action: 'subscription',
            task: 'get-plans',
        });

        if (response.success) {
            res.json({ ...response, source: 'api' });
        } else {
            res.status(500).json(response);
        }
    } catch (error) {
        console.error('Error fetching plans from PHP server:', error);
        
        // Do not fall back to local DB - this is a security concern
        res.status(500).json({ 
            success: false, 
            error: 'Failed to retrieve plans - unable to connect to secure service' 
        });
    }
};

// Subscribe to a plan - Create payment request for UPI
export const subscribe = async (req, res) => {
    try {
        const { planId } = req.body;
        const userId = req.user.id; // Get from authentication middleware

        if (!planId) {
            return res.status(400).json({ error: 'Plan ID is required' });
        }

        try {
            // Check if user already has an active subscription in PHP DB only - NO LOCAL DB
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'subscription',
                task: 'get-status',
                userId: userId,
                token: req.headers.authorization?.split(' ')[1] // Include user token for PHP validation
            });

            if (phpResponse.success && phpResponse.data?.subscription) {
                const phpSubscription = phpResponse.data.subscription;
                if (phpSubscription && phpSubscription.status === 'active') {
                    const endDate = new Date(phpSubscription.end_date);
                    const now = new Date();
                    
                    if (now <= endDate) {
                        return res.status(400).json({ 
                            error: 'You already have an active subscription',
                            currentSubscription: phpSubscription
                        });
                    } else {
                        // Subscription is expired, so it's okay to create a new one
                        // The PHP DB should handle the expiration automatically, but we can update it if needed
                    }
                }
            }

            // Create payment record and generate UPI QR - but store payment in PHP DB instead of local
            const paymentDetails = await PaymentService.createSubscriptionPayment(userId, planId);
            
            res.json({ 
                success: true, 
                message: 'Payment request created successfully',
                payment: {
                    id: paymentDetails.paymentId,
                    transactionId: paymentDetails.transactionId,
                    amount: paymentDetails.amount,
                    upiQRData: paymentDetails.upiQRData,
                    purpose: paymentDetails.purpose,
                    expiresAt: paymentDetails.expiresAt
                }
            });
        } catch (error) {
            console.error('Error in subscription process:', error);
            res.status(500).json({ error: error.message });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
        console.error('Subscription error:', error);
    }
};

// Purchase a one-time feature - Create payment request for UPI
export const purchaseFeature = async (req, res) => {
    try {
        const { featureId } = req.body;
        const userId = req.user.id; // Get from authentication middleware

        if (!featureId) {
            return res.status(400).json({ error: 'Feature ID is required' });
        }

        // Check if user already purchased this feature in PHP DB only - NO LOCAL DB
        try {
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'subscription',
                task: 'get-user-subscription',
                userId: userId,
                token: req.headers.authorization?.split(' ')[1] // Include user token for PHP validation
            });

            if (phpResponse.success && phpResponse.data?.purchased_features) {
                const purchasedFeature = phpResponse.data.purchased_features.find(f => f.id === featureId);
                if (purchasedFeature) {
                    return res.status(400).json({ error: 'Feature already purchased' });
                }
            }
        } catch (phpErr) {
            console.error('Error checking PHP DB for feature purchase:', phpErr.message);
            // Do not continue if PHP API is unavailable - security concern
            return res.status(500).json({ 
                success: false, 
                error: 'Feature availability check failed - unable to connect to secure service' 
            });
        }

        // Create payment record and generate UPI QR
        const paymentDetails = await PaymentService.createFeaturePayment(userId, featureId);
        
        res.json({ 
            success: true, 
            message: 'Feature payment request created successfully',
            payment: {
                id: paymentDetails.paymentId,
                transactionId: paymentDetails.transactionId,
                amount: paymentDetails.amount,
                upiQRData: paymentDetails.upiQRData,
                purpose: paymentDetails.purpose,
                expiresAt: paymentDetails.expiresAt
            }
        });
    } catch (error) {
        console.error('Error in feature purchase:', error);
        res.status(500).json({ error: error.message });
    }
};

// Get user's current subscription and features
export const getUserSubscription = async (req, res) => {
    try {
        const userId = req.user.id; // Get from authentication middleware
        
        try {
            // Get subscription data from PHP server only - NO LOCAL DB FALLBACK
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'subscription',
                task: 'get-user-subscription',
                userId: userId,
                token: req.headers.authorization?.split(' ')[1] // Include user token for PHP validation
            });
            
            if (phpResponse.success) {
                // Use data from PHP server
                res.json({
                    success: true,
                    subscription: phpResponse.data.subscription || null,
                    purchased_features: phpResponse.data.purchased_features || [],
                    available_features: phpResponse.data.available_features || []
                });
                return;
            } else {
                res.status(500).json(phpResponse);
            }
        } catch (phpErr) {
            console.error('Error fetching subscription from PHP server:', phpErr.message);
            res.status(500).json({ 
                success: false, 
                error: phpErr.message || 'Failed to retrieve subscription data.' 
            });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
        console.error('Get user subscription error:', error);
    }
};

// Verify and activate subscription after payment
export const verifyAndActivateSubscription = async (req, res) => {
    try {
        const { paymentId } = req.body;
        const userId = req.user.id; // Get from authentication middleware

        if (!paymentId) {
            return res.status(400).json({ error: 'Payment ID is required' });
        }

        // Verify the payment and activate the subscription
        try {
            const result = await PaymentService.verifyPayment(paymentId);
            
            res.json({ 
                success: true, 
                message: 'Payment verified and subscription activated',
                result: result
            });
        } catch (error) {
            console.error('Error verifying and activating subscription:', error);
            res.status(500).json({ error: error.message });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

// Cancel user's subscription
export const cancelSubscription = async (req, res) => {
    try {
        const userId = req.user.id; // Get from authentication middleware

        try {
            // Get current subscription from PHP server only - NO LOCAL DB
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'subscription',
                task: 'get-status',
                userId: userId,
                token: req.headers.authorization?.split(' ')[1] // Include user token for PHP validation
            });
            
            if (!phpResponse.success || !phpResponse.data?.subscription) {
                return res.status(404).json({ error: 'No active subscription found' });
            }
            
            const phpSubscription = phpResponse.data.subscription;
            if (phpSubscription.status !== 'active') {
                return res.status(400).json({ error: 'No active subscription to cancel' });
            }

            // Cancel subscription in PHP server only - NO LOCAL DB UPDATE
            const cancelResponse = await callPhpApi('/api/v1/action', {
                action: 'subscription',
                task: 'cancel',
                userId: userId,
                subscriptionId: phpSubscription.id,
                token: req.headers.authorization?.split(' ')[1] // Include user token for PHP validation
            });
            
            if (cancelResponse.success) {
                res.json({ 
                    success: true, 
                    message: 'Subscription cancelled successfully',
                    subscription_id: phpSubscription.id
                });
            } else {
                res.status(500).json(cancelResponse);
            }
        } catch (phpErr) {
            console.error('Error cancelling subscription from PHP server:', phpErr.message);
            // Do not fall back to local DB - this is a security concern
            res.status(500).json({ 
                success: false, 
                error: 'Failed to cancel subscription - unable to connect to secure service' 
            });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
        console.error('Cancel subscription error:', error);
    }
};
