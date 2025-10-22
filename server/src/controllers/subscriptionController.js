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
        // Always fetch plans from PHP server as source of truth
        const response = await callPhpApi('/api/v1/action', {
            action: 'subscription',
            task: 'get-plans',
        });

        if (response.success) {
            // Also cache the plans locally for performance
            db.serialize(() => {
                // Clear existing plans to avoid duplicates
                db.run("DELETE FROM plans WHERE id NOT IN (SELECT DISTINCT plan_id FROM plan_features)");
                
                // Insert new plans
                const insert = db.prepare("INSERT OR REPLACE INTO plans (id, name, price, duration_days) VALUES (?, ?, ?, ?)");
                response.data.forEach(plan => {
                    insert.run(plan.id, plan.name, plan.price, plan.duration_days);
                });
                insert.finalize();
            });
            
            res.json({ ...response, source: 'api' });
        } else {
            res.status(500).json(response);
        }
    } catch (error) {
        console.error('Error fetching plans from PHP server:', error);
        
        // Fallback to cached plans if PHP server is unavailable
        db.all(`
            SELECT p.id, p.name, p.price, p.duration_days, 
                   GROUP_CONCAT(f.name) as features 
            FROM plans p 
            LEFT JOIN plan_features pf ON p.id = pf.plan_id 
            LEFT JOIN features f ON pf.feature_id = f.id 
            GROUP BY p.id
        `, [], (err, rows) => {
            if (err) {
                return res.status(500).json({ error: err.message });
            }
            
            if (rows && rows.length > 0) {
                // Format the features for each plan
                const plans = rows.map(row => {
                    return {
                        ...row,
                        features: row.features ? row.features.split(',') : []
                    };
                });
                return res.json({ success: true, data: plans, source: 'cache', message: 'Using cached plans - PHP server unavailable' });
            }
            
            res.status(500).json({ error: 'No plans available and PHP server is unreachable' });
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
            // Create payment record and generate UPI QR
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
            console.error('Error creating subscription payment:', error);
            res.status(500).json({ error: error.message });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
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

        try {
            // Check if user already purchased this feature
            db.get('SELECT * FROM user_features WHERE user_id = ? AND feature_id = ?', [userId, featureId], (err, userFeature) => {
                if (err) {
                    return res.status(500).json({ error: err.message });
                }
                
                if (userFeature) {
                    return res.status(400).json({ error: 'Feature already purchased' });
                }

                // Create payment record and generate UPI QR
                PaymentService.createFeaturePayment(userId, featureId)
                    .then(paymentDetails => {
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
                    })
                    .catch(error => {
                        console.error('Error creating feature payment:', error);
                        res.status(500).json({ error: error.message });
                    });
            });
        } catch (error) {
            console.error('Error in purchaseFeature:', error);
            res.status(500).json({ error: error.message });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

// Get user's current subscription and features
export const getUserSubscription = async (req, res) => {
    try {
        const userId = req.user.id; // Get from authentication middleware
        
        try {
            // Try to get subscription data from PHP server first
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'subscription',
                task: 'get-user-subscription',
                userId: userId
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
            }
        } catch (phpErr) {
            console.log('Could not fetch subscription from PHP server, falling back to local DB:', phpErr.message);
            // Fall back to local database if PHP server is unavailable
        }

        // Get current subscription from local DB as fallback
        db.get(`
            SELECT s.id, s.plan_id, p.name as plan_name, p.price, p.duration_days, s.end_date, s.status 
            FROM subscriptions s
            JOIN plans p ON s.plan_id = p.id
            WHERE s.user_id = ? AND s.status = ?
        `, [userId, 'active'], (err, subscription) => {
            if (err) {
                return res.status(500).json({ error: err.message });
            }

            // Get user's purchased features
            db.all(`
                SELECT f.id, f.name, f.description, f.price, uf.purchase_date
                FROM user_features uf
                JOIN features f ON uf.feature_id = f.id
                WHERE uf.user_id = ?
            `, [userId], (err, features) => {
                if (err) {
                    return res.status(500).json({ error: err.message });
                }

                // Get all available features for reference
                db.all('SELECT * FROM features', [], (err, allFeatures) => {
                    if (err) {
                        return res.status(500).json({ error: err.message });
                    }

                    res.json({ 
                        success: true,
                        subscription: subscription || null,
                        purchased_features: features || [],
                        available_features: allFeatures || []
                    });
                });
            });
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

// Cancel user's subscription
export const cancelSubscription = async (req, res) => {
    try {
        const userId = req.user.id; // Get from authentication middleware

        // Find active subscription
        db.get('SELECT * FROM subscriptions WHERE user_id = ? AND status = ?', [userId, 'active'], async (err, subscription) => {
            if (err) {
                return res.status(500).json({ error: err.message });
            }
            
            if (!subscription) {
                return res.status(404).json({ error: 'No active subscription found' });
            }

            // Update subscription status to 'cancelled'
            db.run('UPDATE subscriptions SET status = ? WHERE id = ?', ['cancelled', subscription.id], async function(err) {
                if (err) {
                    return res.status(500).json({ error: err.message });
                }
                
                try {
                    // Sync cancellation with PHP server
                    await callPhpApi('/api/v1/action', {
                        action: 'subscription',
                        task: 'cancel',
                        userId: userId,
                        subscriptionId: subscription.id
                    });
                } catch (phpErr) {
                    console.error('Error syncing subscription cancellation with PHP server:', phpErr);
                    // Don't fail the operation if PHP sync fails, but log it
                }
                
                res.json({ 
                    success: true, 
                    message: 'Subscription cancelled successfully',
                    subscription_id: subscription.id
                });
            });
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};
