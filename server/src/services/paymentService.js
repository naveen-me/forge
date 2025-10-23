import { v4 as uuidv4 } from 'uuid';
import { callPhpApi } from './phpApiService.js';

/**
 * Payment Service for UPI-based transactions
 */
export class PaymentService {
    
    /**
     * Generate a UPI payment link/QR data for subscription
     */
    static generateUPIPaymentLink({ upiId, amount, transactionId, purpose = 'Subscription' }) {
        // Format: upi://pay?pa=[UPI_ID]&pn=[PAYEE_NAME]&am=[AMOUNT]&tn=[TRANSACTION_NOTE]&tr=[TRANSACTION_ID]
        const upiUrl = `upi://pay?pa=${encodeURIComponent(upiId)}&pn=MatrixAPI&am=${amount}&tn=${encodeURIComponent(purpose)}&tr=${transactionId}`;
        return upiUrl;
    }

    /**
     * Generate UPI QR code data for subscription
     */
    static generateUPIQRData({ upiId, amount, transactionId, purpose = 'Subscription' }) {
        // UPI string format: [UPI_ID]:[AMOUNT]:[TRANSACTION_ID]:[PURPOSE]
        return `upi://pay?pa=${upiId}&pn=MatrixAPI&am=${amount}&tn=${encodeURIComponent(purpose)}&tr=${transactionId}`;
    }

    /**
     * Create a subscription payment record using PHP DB as source of truth
     */
    static async createSubscriptionPayment(userId, planId) {
        // Get plan details from PHP server - no local DB dependency
        const plansResponse = await callPhpApi('/api/v1/action', {
            action: 'subscription',
            task: 'get-plans'
        });

        if (!plansResponse.success) {
            throw new Error('Unable to fetch plan details from server');
        }

        const plan = plansResponse.data.find(p => p.id == planId);
        if (!plan) {
            throw new Error('Plan not found');
        }

        // Get UPI details from PHP server - no local DB dependency
        const upiResponse = await callPhpApi('/api/v1/action', {
            action: 'upi',
            task: 'get-primary'
        });

        if (!upiResponse.success || !upiResponse.data) {
            throw new Error('Unable to get UPI details from server');
        }

        const upiDetails = upiResponse.data;
        const transactionId = `TXN_${Date.now()}_${userId}_${planId}`;
        
        // Generate UPI QR data
        const upiQRData = this.generateUPIQRData({
            upiId: upiDetails.upi_vpa,
            amount: plan.price,
            transactionId: transactionId,
            purpose: `Subscription: ${plan.name}`
        });

        // Create payment record in PHP DB via API
        const createPaymentResponse = await callPhpApi('/api/v1/action', {
            action: 'payment',
            task: 'create',
            userId: userId,
            payment_type: 'subscription',
            item_id: planId,
            amount: plan.price,
            payment_method: 'upi',
            transaction_id: transactionId,
            upi_qr_data: upiQRData,
            status: 'pending',
            expires_at: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24 hours from now
        });

        if (!createPaymentResponse.success) {
            throw new Error(createPaymentResponse.message || 'Failed to create payment record in PHP DB');
        }

        // Return payment information
        return {
            paymentId: createPaymentResponse.paymentId || transactionId,
            transactionId,
            amount: plan.price,
            upiQRData,
            purpose: `Subscription: ${plan.name}`,
            expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
        };
    }
                    
    
    /**
     * Create a feature purchase payment record using PHP DB as source of truth
     */
    static async createFeaturePayment(userId, featureId) {
        // Check if user already purchased this feature using PHP DB
        const userSubscriptionResponse = await callPhpApi('/api/v1/action', {
            action: 'subscription',
            task: 'get-user-subscription',
            userId: userId
        });
        
        if (userSubscriptionResponse.success && userSubscriptionResponse.data?.purchased_features) {
            const purchasedFeature = userSubscriptionResponse.data.purchased_features.find(f => f.id == featureId);
            if (purchasedFeature) {
                throw new Error('Feature already purchased');
            }
        }

        // Get feature details from PHP - we need to get available features somehow
        // This would require an API change to get feature details by ID
        // For now, get all plans and find features that way
        const plansResponse = await callPhpApi('/api/v1/action', {
            action: 'subscription',
            task: 'get-plans'
        });
        
        // Extract features from all plans to find the specific feature
        let feature = null;
        if (plansResponse.success) {
            // We need a better way to get individual feature data
            // This requires a specific feature-by-id endpoint on the PHP side
            // For now, we'll need to work with what's available or assume features can be retrieved another way
            // As a fallback, we'll create a temporary solution
        }

        // Get UPI details from PHP server
        const upiResponse = await callPhpApi('/api/v1/action', {
            action: 'upi',
            task: 'get-primary'
        });

        if (!upiResponse.success || !upiResponse.data) {
            throw new Error('Unable to get UPI details from server');
        }

        const upiDetails = upiResponse.data;
        const transactionId = `TXN_${Date.now()}_${userId}_${featureId}`;
        
        // For now, assume we have a generic price for the feature
        // In a real system, we'd have a separate endpoint to get feature details
        const featurePrice = 4.99; // Default price - this should come from PHP
        
        // Generate UPI QR data
        const upiQRData = this.generateUPIQRData({
            upiId: upiDetails.upi_vpa,
            amount: featurePrice,
            transactionId: transactionId,
            purpose: `Feature: Feature ${featureId}`
        });

        // Create payment record in PHP DB via API
        const createPaymentResponse = await callPhpApi('/api/v1/action', {
            action: 'payment',
            task: 'create',
            userId: userId,
            payment_type: 'feature',
            item_id: featureId,
            amount: featurePrice,
            payment_method: 'upi',
            transaction_id: transactionId,
            upi_qr_data: upiQRData,
            status: 'pending',
            expires_at: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24 hours from now
        });

        if (!createPaymentResponse.success) {
            throw new Error(createPaymentResponse.message || 'Failed to create payment record in PHP DB');
        }

        // Return payment information
        return {
            paymentId: createPaymentResponse.paymentId || transactionId,
            transactionId,
            amount: featurePrice,
            upiQRData,
            purpose: `Feature: Feature ${featureId}`,
            expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
        };
    }
                    
    /** 
     * Helper function to close properly
     */
    // End of processFeaturePaymentWithUPI function

    /**
     * Verify and update payment status using PHP DB as source of truth
     */
    static async verifyPayment(paymentId, verificationData = {}) {
        // Update payment status in PHP DB via API
        const response = await callPhpApi('/api/v1/action', {
            action: 'payment',
            task: 'update-status',
            paymentId: paymentId,
            status: 'paid',
            paymentDate: new Date().toISOString(),
            verificationData: verificationData
        });

        if (!response.success) {
            throw new Error(response.message || 'Failed to verify payment in PHP DB');
        }

        // Process the payment completion based on type by syncing with PHP
        // For now, we'll need to determine the payment type by making another call
        try {
            // Since we don't have the payment details locally, we'll just return success
            // The PHP server should handle the rest based on the payment type internally
            return {
                success: true,
                paymentId,
                status: 'paid'
            };
        } catch (error) {
            console.error('Error in post-payment processing:', error);
            throw error;
        }
    }

    /**
     * Process a completed subscription payment
     */
    static async processSubscriptionPayment(payment) {
        return new Promise(async (resolve, reject) => {
            try {
                // Get plan details from local DB for reference, but sync everything to PHP
                const plan = await new Promise((planResolve, planReject) => {
                    db.get('SELECT duration_days, name, price FROM plans WHERE id = ?', [payment.item_id], (err, plan) => {
                        if (err) {
                            planReject(err);
                        } else if (!plan) {
                            planReject(new Error('Plan not found for payment'));
                        } else {
                            planResolve(plan);
                        }
                    });
                });

                const startDate = new Date();
                const endDate = new Date();
                endDate.setDate(startDate.getDate() + plan.duration_days);

                // IMPORTANT: Sync with PHP server ONLY - NO LOCAL DB SUBSCRIPTION RECORDS
                try {
                    await this.syncSubscriptionWithPhp(payment.user_id, payment.item_id, endDate.toISOString().split('T')[0], 'active');
                    // Sync payment status to PHP server as well
                    await this.syncPaymentStatusToPhp(payment.id, 'paid');
                    resolve();
                } catch (phpErr) {
                    console.error('Error syncing subscription/payment with PHP server:', phpErr);
                    reject(phpErr);
                }
            } catch (error) {
                reject(error);
            }
        });
    }
    
    /**
     * Sync subscription data with PHP server
     */
    static async syncSubscriptionWithPhp(userId, planId, endDate, status) {
        try {
            // Get user details from PHP server only - no local DB
            let user = { 
                email: `user${userId}@example.com`, // Default if we can't get from PHP
                name: `User${userId}`
            };
            
            try {
                const userResponse = await callPhpApi('/api/v1/action', {
                    action: 'subscription',
                    task: 'get-user-subscription',
                    userId: userId
                });
                
                if (userResponse.success) {
                    user = { 
                        email: userResponse.data.user?.email || `user${userId}@example.com`, 
                        name: userResponse.data.user?.name || `User${userId}`
                    };
                }
            } catch (phpUserErr) {
                console.error('Error getting user from PHP server:', phpUserErr);
                // Continue with default values
            }
            
            // Get plan details from PHP - get all plans and find the specific one
            let plan = {
                name: `Plan ${planId}`,
                price: 0,
                duration_days: 30
            };
            
            try {
                const plansResponse = await callPhpApi('/api/v1/action', {
                    action: 'subscription',
                    task: 'get-plans'
                });
                
                if (plansResponse.success) {
                    const foundPlan = plansResponse.data.find(p => p.id == planId);
                    if (foundPlan) {
                        plan = {
                            name: foundPlan.name,
                            price: foundPlan.price,
                            duration_days: foundPlan.duration_days
                        };
                    }
                }
            } catch (phpPlanErr) {
                console.error('Error getting plan from PHP server:', phpPlanErr);
                // Continue with default values
            }
            
            // Call PHP API to update subscription
            const response = await callPhpApi('/api/v1/action', {
                action: 'subscription',
                task: 'update',
                userId: userId,
                email: user.email,
                planId: planId,
                planName: plan.name,
                planPrice: plan.price,
                planDuration: plan.duration_days,
                endDate: endDate,
                status: status
            });
            
            return response;
        } catch (error) {
            console.error('Error syncing subscription with PHP server:', error);
            throw error;
        }
    }

    /**
     * Process a completed feature payment
     */
    static async processFeaturePayment(payment) {
        return new Promise(async (resolve, reject) => {
            try {
                // IMPORTANT: Sync feature purchase with PHP server ONLY - NO LOCAL DB FEATURE RECORDS
                try {
                    await this.syncFeaturePurchaseWithPhp(payment.user_id, payment.item_id, new Date().toISOString().split('T')[0]);
                    // Also sync payment status to PHP server
                    await this.syncPaymentStatusToPhp(payment.id, 'paid');
                    resolve();
                } catch (phpErr) {
                    console.error('Error syncing feature purchase/payment with PHP server:', phpErr);
                    reject(phpErr);
                }
            } catch (error) {
                reject(error);
            }
        });
    }
    
    /**
     * Sync feature purchase data with PHP server
     */
    static async syncFeaturePurchaseWithPhp(userId, featureId, purchaseDate) {
        try {
            // Get user details from PHP server only - no local DB
            let user = { 
                email: `user${userId}@example.com`, // Default if we can't get from PHP
                name: `User${userId}`
            };
            
            try {
                const userResponse = await callPhpApi('/api/v1/action', {
                    action: 'subscription',
                    task: 'get-user-subscription',
                    userId: userId
                });
                
                if (userResponse.success) {
                    user = { 
                        email: userResponse.data.user?.email || `user${userId}@example.com`, 
                        name: userResponse.data.user?.name || `User${userId}`
                    };
                }
            } catch (phpUserErr) {
                console.error('Error getting user from PHP server:', phpUserErr);
                // Continue with default values
            }
            
            // Get feature details from PHP - we'll need to get this differently
            // Since we don't have a direct feature-by-ID API, use default values
            const feature = {
                name: `Feature ${featureId}`,
                price: 4.99  // Default price
            };
            
            // Call PHP API to update feature purchase
            const response = await callPhpApi('/api/v1/action', {
                action: 'feature',
                task: 'purchase',
                userId: userId,
                email: user.email,
                featureId: featureId,
                featureName: feature.name,
                featurePrice: feature.price,
                purchaseDate: purchaseDate
            });
            
            return response;
        } catch (error) {
            console.error('Error syncing feature purchase with PHP server:', error);
            throw error;
        }
    }
    
    /**
     * Sync payment status to PHP server
     */
    static async syncPaymentStatusToPhp(paymentId, status) {
        try {
            // Call PHP API to update payment status with minimal required info
            // We don't need to fetch payment details from local DB anymore
            const response = await callPhpApi('/api/v1/action', {
                action: 'payment',
                task: 'update-status',
                paymentId: paymentId,
                status: status,
                // Other fields are optional for PHP to fill in from its own records
            });
            
            return response;
        } catch (error) {
            console.error('Error syncing payment status to PHP server:', error);
            throw error;
        }
    }

    /**
     * Get payment details by ID from PHP DB
     */
    static async getPaymentDetails(paymentId) {
        try {
            const response = await callPhpApi('/api/v1/action', {
                action: 'payment',
                task: 'get-details',
                paymentId: paymentId
            });
            
            if (response.success) {
                return response.data;
            } else {
                throw new Error(response.message || 'Payment not found');
            }
        } catch (error) {
            console.error('Error getting payment details:', error);
            throw error;
        }
    }

    /**
     * Check payment status by ID from PHP DB
     */
    static async checkPaymentStatus(paymentId) {
        try {
            const response = await callPhpApi('/api/v1/action', {
                action: 'payment',
                task: 'get-details',
                paymentId: paymentId
            });
            
            if (response.success) {
                // Return standardized status information
                const payment = response.data;
                return {
                    status: payment.status,
                    paymentDate: payment.payment_date,
                    transactionId: payment.transaction_id,
                    amount: payment.amount,
                    paymentMethod: payment.payment_method
                };
            } else {
                throw new Error(response.message || 'Payment not found');
            }
        } catch (error) {
            console.error('Error checking payment status:', error);
            throw error;
        }
    }

    /**
     * Get primary UPI detail from PHP server - no local DB fallback for security
     */
    static async getPrimaryUPIXDetail() {
        try {
            // Try to get from PHP server only
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'upi',
                task: 'get-primary'
            });
            
            if (phpResponse.success && phpResponse.data) {
                return phpResponse.data;
            } else {
                throw new Error('No primary UPI details available from server');
            }
        } catch (error) {
            console.error('Error getting primary UPI detail from PHP server:', error);
            // Do not fall back to local DB for security reasons
            throw new Error('Unable to get UPI details - service unavailable');
        }
    }

    /**
     * Get all UPI details from PHP server - no local DB fallback for security
     */
    static async getAllUPIXDetails() {
        try {
            // Try to get from PHP server only
            const phpResponse = await callPhpApi('/api/v1/action', {
                action: 'upi',
                task: 'get-all'
            });
            
            if (phpResponse.success && phpResponse.data) {
                return phpResponse.data;
            } else {
                throw new Error('No UPI details available from server');
            }
        } catch (error) {
            console.error('Error getting all UPI details from PHP server:', error);
            // Do not fall back to local DB for security reasons
            throw new Error('Unable to get UPI details - service unavailable');
        }
    }

    /**
     * Get user's payment history from PHP DB
     */
    static async getUserPaymentHistory(userId, token = null) {
        try {
            const requestData = {
                action: 'payment',
                task: 'get-user-history',
                userId: userId
            };
            
            // Include token if provided for PHP validation
            if (token) {
                requestData.token = token;
            }
            
            const response = await callPhpApi('/api/v1/action', requestData);
            return response.success ? response.data : [];
        } catch (error) {
            console.error('Error fetching payment history:', error);
            return [];
        }
    }

    /**
     * Get user's pending payments from PHP DB
     */
    static async getUserPendingPayments(userId, token = null) {
        try {
            const requestData = {
                action: 'payment',
                task: 'get-user-pending',
                userId: userId
            };
            
            // Include token if provided for PHP validation
            if (token) {
                requestData.token = token;
            }
            
            const response = await callPhpApi('/api/v1/action', requestData);
            return response.success ? response.data : [];
        } catch (error) {
            console.error('Error fetching pending payments:', error);
            return [];
        }
    }

    /**
     * Cancel a pending payment in PHP DB
     */
    static async cancelPayment(paymentId) {
        // First verify payment is pending by calling PHP API
        // In a full implementation, we'd have a check-status endpoint
        // For now, we'll just make the cancel call and let PHP handle validation
        
        const response = await callPhpApi('/api/v1/action', {
            action: 'payment',
            task: 'update-status',
            paymentId: paymentId,
            status: 'cancelled'
        });

        if (!response.success) {
            throw new Error(response.message || 'Failed to cancel payment in PHP DB');
        }

        return {
            success: true,
            paymentId,
            status: 'cancelled'
        };
    }
}