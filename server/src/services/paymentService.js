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
    static async createSubscriptionPayment(userId, planId, userToken = null) {
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
        // Include user token as the UPI service requires authentication
        const upiRequestData = {
            action: 'upi',
            task: 'get-primary'
        };
        
        // Add user token if provided
        if (userToken) {
            upiRequestData.token = userToken;
        }
        
        const upiResponse = await callPhpApi('/api/v1/action', upiRequestData);

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
        const createPaymentRequestData = {
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
        };
        
        // Include user token since payment API requires authentication
        if (userToken) {
            createPaymentRequestData.token = userToken;
        }
        
        const createPaymentResponse = await callPhpApi('/api/v1/action', createPaymentRequestData);

        if (!createPaymentResponse.success) {
            throw new Error(createPaymentResponse.message || 'Failed to create payment record in PHP DB');
        }

        // Return payment information, using the actual payment ID from PHP
        const actualPaymentId = createPaymentResponse.paymentId || transactionId;
        
        return {
            paymentId: actualPaymentId,
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
    static async createFeaturePayment(userId, featureId, userToken = null) {
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
        // Include user token as the UPI service requires authentication
        const upiRequestData = {
            action: 'upi',
            task: 'get-primary'
        };
        
        // Add user token if provided
        if (userToken) {
            upiRequestData.token = userToken;
        }
        
        const upiResponse = await callPhpApi('/api/v1/action', upiRequestData);

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
        const createPaymentRequestData = {
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
        };
        
        // Include user token since payment API requires authentication
        if (userToken) {
            createPaymentRequestData.token = userToken;
        }
        
        const createPaymentResponse = await callPhpApi('/api/v1/action', createPaymentRequestData);

        if (!createPaymentResponse.success) {
            throw new Error(createPaymentResponse.message || 'Failed to create payment record in PHP DB');
        }

        // Return payment information, using the actual payment ID from PHP
        const actualPaymentId = createPaymentResponse.paymentId || transactionId;
        
        return {
            paymentId: actualPaymentId,
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
    static async verifyPayment(paymentId, verificationData = {}, userToken = null, status = 'paid') {
        try {
            // First, get the existing payment details to ensure we have all required fields
            let existingPaymentDetails = null;
            try {
                const paymentDetailsResponse = await callPhpApi('/api/v1/action', {
                    action: 'payment',
                    task: 'get-details',
                    paymentId: paymentId,
                    token: userToken
                });
                
                if (paymentDetailsResponse.success && paymentDetailsResponse.data) {
                    existingPaymentDetails = paymentDetailsResponse.data;
                }
            } catch (detailsError) {
                console.log('Could not fetch existing payment details:', detailsError.message);
            }

            // Update payment status in PHP DB via API
            const requestData = {
                action: 'payment',
                task: 'update-status',
                paymentId: paymentId,
                status: status,
                paymentDate: new Date().toISOString(),
                verificationData: verificationData
            };
            
            // Include user token since payment API requires authentication
            if (userToken) {
                requestData.token = userToken;
            }
            
            // When creating a new record (fallback), include required fields if we have them
            if (existingPaymentDetails) {
                requestData.userId = existingPaymentDetails.user_id;
                requestData.paymentType = existingPaymentDetails.payment_type;
                requestData.itemId = existingPaymentDetails.item_id;
                requestData.amount = existingPaymentDetails.amount;
            }
            
            const response = await callPhpApi('/api/v1/action', requestData);

            if (!response.success) {
                throw new Error(response.message || 'Failed to verify payment in PHP DB');
            }

            // If the status is 'paid', activate the subscription/feature
            if (status === 'paid') {
                try {
                    const payment = existingPaymentDetails;
                    
                    if (payment && payment.payment_type === 'subscription') {
                        // Update subscription status in PHP
                        await callPhpApi('/api/v1/action', {
                            action: 'subscription',
                            task: 'update',
                            userId: payment.user_id,
                            planId: payment.item_id,
                            endDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString(), // 1 year from now
                            status: 'active'
                        });
                    } else if (payment && payment.payment_type === 'feature') {
                        // Update feature purchase in PHP
                        await callPhpApi('/api/v1/action', {
                            action: 'feature',
                            task: 'purchase',
                            userId: payment.user_id,
                            featureId: payment.item_id,
                            purchaseDate: new Date().toISOString()
                        });
                    }
                } catch (activationError) {
                    console.error('Error in post-payment activation:', activationError);
                    // Don't throw error as the payment status was updated successfully
                    // The activation is best-effort
                }
            }

            // Return success result
            return {
                success: true,
                paymentId,
                status: status
            };
        } catch (error) {
            console.error('Error in payment verification:', error);
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
    static async syncPaymentStatusToPhp(paymentId, status, userToken = null) {
        try {
            // Call PHP API to update payment status with minimal required info
            // We don't need to fetch payment details from local DB anymore
            const requestData = {
                action: 'payment',
                task: 'update-status',
                paymentId: paymentId,
                status: status,
                // Other fields are optional for PHP to fill in from its own records
            };
            
            // Include user token since payment API requires authentication
            if (userToken) {
                requestData.token = userToken;
            }
            
            const response = await callPhpApi('/api/v1/action', requestData);
            
            return response;
        } catch (error) {
            console.error('Error syncing payment status to PHP server:', error);
            throw error;
        }
    }

    /**
     * Get payment details by ID from PHP DB
     */
    static async getPaymentDetails(paymentId, userToken = null) {
        try {
            const requestData = {
                action: 'payment',
                task: 'get-details',
                paymentId: paymentId
            };
            
            // Include user token since payment API requires authentication
            if (userToken) {
                requestData.token = userToken;
            }
            
            const response = await callPhpApi('/api/v1/action', requestData);
            
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
    static async checkPaymentStatus(paymentId, userToken = null) {
        try {
            const requestData = {
                action: 'payment',
                task: 'get-details',
                paymentId: paymentId
            };
            
            // Include user token since payment API requires authentication
            if (userToken) {
                requestData.token = userToken;
            }
            
            const response = await callPhpApi('/api/v1/action', requestData);
            
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
    static async getPrimaryUPIXDetail(userToken = null) {
        try {
            // Build the request data
            const requestData = {
                action: 'upi',
                task: 'get-primary'
            };
            
            // Include user token for authentication if provided
            if (userToken) {
                requestData.token = userToken;
            }
            
            // Try to get from PHP server only
            const phpResponse = await callPhpApi('/api/v1/action', requestData);
            
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
    static async getAllUPIXDetails(userToken = null) {
        try {
            // Build the request data
            const requestData = {
                action: 'upi',
                task: 'get-all'
            };
            
            // Include user token for authentication if provided
            if (userToken) {
                requestData.token = userToken;
            }
            
            // Try to get from PHP server only
            const phpResponse = await callPhpApi('/api/v1/action', requestData);
            
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
    static async cancelPayment(paymentId, userToken = null) {
        // First verify payment is pending by calling PHP API
        // In a full implementation, we'd have a check-status endpoint
        // For now, we'll just make the cancel call and let PHP handle validation
        
        const requestData = {
            action: 'payment',
            task: 'update-status',
            paymentId: paymentId,
            status: 'cancelled'
        };
        
        // Include user token since payment API requires authentication
        if (userToken) {
            requestData.token = userToken;
        }
        
        const response = await callPhpApi('/api/v1/action', requestData);

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