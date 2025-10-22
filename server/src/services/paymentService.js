import db from '../db/database.js';
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
     * Create a subscription payment record
     */
    static createSubscriptionPayment(userId, planId) {
        return new Promise((resolve, reject) => {
            // Get plan details
            db.get('SELECT * FROM plans WHERE id = ?', [planId], (err, plan) => {
                if (err) {
                    return reject(err);
                }
                if (!plan) {
                    return reject(new Error('Plan not found'));
                }

                // First try to get from PHP server
                callPhpApi('/api/v1/action', {
                    action: 'upi',
                    task: 'get-primary'
                })
                .then(phpResponse => {
                    let upiDetails;
                    if (phpResponse.success && phpResponse.data) {
                        upiDetails = phpResponse.data;
                    } else {
                        // If PHP server is unavailable, try to use local fallback
                        db.get('SELECT * FROM system_upi_details WHERE is_primary = 1 AND is_active = 1 LIMIT 1', (err, primaryUPIXDetails) => {
                            if (err) {
                                return reject(err);
                            }
                            
                            if (!primaryUPIXDetails) {
                                // Fallback to any active UPI if no primary is set
                                db.get('SELECT * FROM system_upi_details WHERE is_active = 1 LIMIT 1', (err, fallbackUPIXDetails) => {
                                    if (err) {
                                        return reject(err);
                                    }
                                    if (!fallbackUPIXDetails) {
                                        return reject(new Error('No active UPI payment method configured'));
                                    }
                                    processPaymentWithUPI(fallbackUPIXDetails);
                                });
                                return;
                            }
                            
                            processPaymentWithUPI(primaryUPIXDetails);
                        });
                        
                        function processPaymentWithUPI(upiToUse) {
                            // Generate unique transaction ID using fallback UPI details
                            const transactionId = `TXN_${Date.now()}_${userId}_${planId}`;
                            
                            // Generate UPI QR data
                            const upiQRData = this.generateUPIQRData({
                                upiId: upiToUse.upi_vpa,
                                amount: plan.price,
                                transactionId: transactionId,
                                purpose: `Subscription: ${plan.name}`
                            });

                            // Create payment record
                            db.run(
                                `INSERT INTO payments (user_id, payment_type, item_id, amount, payment_method, transaction_id, upi_qr_data, status, expires_at) 
                                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                                [
                                    userId,
                                    'subscription',
                                    planId,
                                    plan.price,
                                    'upi',
                                    transactionId,
                                    upiQRData,
                                    'pending',
                                    new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24 hours from now
                                ],
                                function(err) {
                                    if (err) {
                                        return reject(err);
                                    }

                                    // Log the payment creation
                                    db.run(
                                        `INSERT INTO payment_logs (payment_id, action, details) 
                                         VALUES (?, ?, ?)`,
                                        [this.lastID, 'created', `Subscription payment created for plan ${planId}`],
                                        (err) => {
                                            if (err) {
                                                console.error('Error logging payment creation:', err);
                                            }
                                        }
                                    );

                                    resolve({
                                        paymentId: this.lastID,
                                        transactionId,
                                        amount: plan.price,
                                        upiQRData,
                                        purpose: `Subscription: ${plan.name}`,
                                        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
                                    });
                                }
                            );
                        }
                        
                        return; // Exit the main function after local processing
                    }
                    
                    // Process with PHP UPI details
                    const transactionId = `TXN_${Date.now()}_${userId}_${planId}`;
                    
                    // Generate UPI QR data
                    const upiQRData = this.generateUPIQRData({
                        upiId: upiDetails.upi_vpa,
                        amount: plan.price,
                        transactionId: transactionId,
                        purpose: `Subscription: ${plan.name}`
                    });

                    // Create payment record
                    db.run(
                        `INSERT INTO payments (user_id, payment_type, item_id, amount, payment_method, transaction_id, upi_qr_data, status, expires_at) 
                         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                        [
                            userId,
                            'subscription',
                            planId,
                            plan.price,
                            'upi',
                            transactionId,
                            upiQRData,
                            'pending',
                            new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24 hours from now
                        ],
                        function(err) {
                            if (err) {
                                return reject(err);
                            }

                            // Log the payment creation
                            db.run(
                                `INSERT INTO payment_logs (payment_id, action, details) 
                                 VALUES (?, ?, ?)`,
                                [this.lastID, 'created', `Subscription payment created for plan ${planId}`],
                                (err) => {
                                    if (err) {
                                        console.error('Error logging payment creation:', err);
                                    }
                                }
                            );

                            resolve({
                                paymentId: this.lastID,
                                transactionId,
                                amount: plan.price,
                                upiQRData,
                                purpose: `Subscription: ${plan.name}`,
                                expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
                            });
                        }
                    );
                })
                .catch(phpErr => {
                    console.error('Error getting UPI details from PHP server:', phpErr);
                    // Fall back to local UPI details
                    db.get('SELECT * FROM system_upi_details WHERE is_primary = 1 AND is_active = 1 LIMIT 1', (err, primaryUPIXDetails) => {
                        if (err) {
                            return reject(err);
                        }
                        
                        if (!primaryUPIXDetails) {
                            // Fallback to any active UPI if no primary is set
                            db.get('SELECT * FROM system_upi_details WHERE is_active = 1 LIMIT 1', (err, fallbackUPIXDetails) => {
                                if (err) {
                                    return reject(err);
                                }
                                if (!fallbackUPIXDetails) {
                                    return reject(new Error('No active UPI payment method configured'));
                                }
                                processPaymentWithUPI(fallbackUPIXDetails);
                            });
                            return;
                        }
                        
                        processPaymentWithUPI(primaryUPIXDetails);
                        
                        function processPaymentWithUPI(upiToUse) {
                            // Generate unique transaction ID using fallback UPI details
                            const transactionId = `TXN_${Date.now()}_${userId}_${planId}`;
                            
                            // Generate UPI QR data
                            const upiQRData = this.generateUPIQRData({
                                upiId: upiToUse.upi_vpa,
                                amount: plan.price,
                                transactionId: transactionId,
                                purpose: `Subscription: ${plan.name}`
                            });

                            // Create payment record
                            db.run(
                                `INSERT INTO payments (user_id, payment_type, item_id, amount, payment_method, transaction_id, upi_qr_data, status, expires_at) 
                                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                                [
                                    userId,
                                    'subscription',
                                    planId,
                                    plan.price,
                                    'upi',
                                    transactionId,
                                    upiQRData,
                                    'pending',
                                    new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24 hours from now
                                ],
                                function(err) {
                                    if (err) {
                                        return reject(err);
                                    }

                                    // Log the payment creation
                                    db.run(
                                        `INSERT INTO payment_logs (payment_id, action, details) 
                                         VALUES (?, ?, ?)`,
                                        [this.lastID, 'created', `Subscription payment created for plan ${planId}`],
                                        (err) => {
                                            if (err) {
                                                console.error('Error logging payment creation:', err);
                                            }
                                        }
                                    );

                                    resolve({
                                        paymentId: this.lastID,
                                        transactionId,
                                        amount: plan.price,
                                        upiQRData,
                                        purpose: `Subscription: ${plan.name}`,
                                        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
                                    });
                                }
                            );
                        }
                    });
               });
            });
        });
    }
                    
    
    /**
     * Create a feature purchase payment record
     */
    static createFeaturePayment(userId, featureId) {
        return new Promise((resolve, reject) => {
            // Get feature details
            db.get('SELECT * FROM features WHERE id = ?', [featureId], (err, feature) => {
                if (err) {
                    return reject(err);
                }
                if (!feature) {
                    return reject(new Error('Feature not found'));
                }

                // First try to get from PHP server
                callPhpApi('/api/v1/action', {
                    action: 'upi',
                    task: 'get-primary'
                })
                .then(phpResponse => {
                    let upiDetails;
                    if (phpResponse.success && phpResponse.data) {
                        upiDetails = phpResponse.data;
                    } else {
                        // If PHP server is unavailable, try to use local fallback
                        db.get('SELECT * FROM system_upi_details WHERE is_primary = 1 AND is_active = 1 LIMIT 1', (err, primaryUPIXDetails) => {
                            if (err) {
                                return reject(err);
                            }
                            
                            if (!primaryUPIXDetails) {
                                // Fallback to any active UPI if no primary is set
                                db.get('SELECT * FROM system_upi_details WHERE is_active = 1 LIMIT 1', (err, fallbackUPIXDetails) => {
                                    if (err) {
                                        return reject(err);
                                    }
                                    if (!fallbackUPIXDetails) {
                                        return reject(new Error('No active UPI payment method configured'));
                                    }
                                    processFeaturePaymentWithUPI(fallbackUPIXDetails);
                                });
                                return;
                            }
                            
                            processFeaturePaymentWithUPI(primaryUPIXDetails);
                        });
                        
                        function processFeaturePaymentWithUPI(upiToUse) {
                            // Generate unique transaction ID using fallback UPI details
                            const transactionId = `TXN_${Date.now()}_${userId}_${featureId}`;
                            
                            // Generate UPI QR data
                            const upiQRData = this.generateUPIQRData({
                                upiId: upiToUse.upi_vpa,
                                amount: feature.price,
                                transactionId: transactionId,
                                purpose: `Feature: ${feature.name}`
                            });

                            // Create payment record
                            db.run(
                                `INSERT INTO payments (user_id, payment_type, item_id, amount, payment_method, transaction_id, upi_qr_data, status, expires_at) 
                                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                                [
                                    userId,
                                    'feature',
                                    featureId,
                                    feature.price,
                                    'upi',
                                    transactionId,
                                    upiQRData,
                                    'pending',
                                    new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24 hours from now
                                ],
                                function(err) {
                                    if (err) {
                                        return reject(err);
                                    }

                                    // Log the payment creation
                                    db.run(
                                        `INSERT INTO payment_logs (payment_id, action, details) 
                                         VALUES (?, ?, ?)`,
                                        [this.lastID, 'created', `Feature payment created for feature ${featureId}`],
                                        (err) => {
                                            if (err) {
                                                console.error('Error logging payment creation:', err);
                                            }
                                        }
                                    );

                                    resolve({
                                        paymentId: this.lastID,
                                        transactionId,
                                        amount: feature.price,
                                        upiQRData,
                                        purpose: `Feature: ${feature.name}`,
                                        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
                                    });
                                }
                            );
                        }
                        
                        return; // Exit the main function after local processing
                    }
                    
                    // Process with PHP UPI details
                    const transactionId = `TXN_${Date.now()}_${userId}_${featureId}`;
                    
                    // Generate UPI QR data
                    const upiQRData = this.generateUPIQRData({
                        upiId: upiDetails.upi_vpa,
                        amount: feature.price,
                        transactionId: transactionId,
                        purpose: `Feature: ${feature.name}`
                    });

                    // Create payment record
                    db.run(
                        `INSERT INTO payments (user_id, payment_type, item_id, amount, payment_method, transaction_id, upi_qr_data, status, expires_at) 
                         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                        [
                            userId,
                            'feature',
                            featureId,
                            feature.price,
                            'upi',
                            transactionId,
                            upiQRData,
                            'pending',
                            new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24 hours from now
                        ],
                        function(err) {
                            if (err) {
                                return reject(err);
                            }

                            // Log the payment creation
                            db.run(
                                `INSERT INTO payment_logs (payment_id, action, details) 
                                 VALUES (?, ?, ?)`,
                                [this.lastID, 'created', `Feature payment created for feature ${featureId}`],
                                (err) => {
                                    if (err) {
                                        console.error('Error logging payment creation:', err);
                                    }
                                }
                            );

                            resolve({
                                paymentId: this.lastID,
                                transactionId,
                                amount: feature.price,
                                upiQRData,
                                purpose: `Feature: ${feature.name}`,
                                expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
                            });
                        }
                    );
                })
                .catch(phpErr => {
                    console.error('Error getting UPI details from PHP server:', phpErr);
                    // Fall back to local UPI details
                    db.get('SELECT * FROM system_upi_details WHERE is_primary = 1 AND is_active = 1 LIMIT 1', (err, primaryUPIXDetails) => {
                        if (err) {
                            return reject(err);
                        }
                        
                        if (!primaryUPIXDetails) {
                            // Fallback to any active UPI if no primary is set
                            db.get('SELECT * FROM system_upi_details WHERE is_active = 1 LIMIT 1', (err, fallbackUPIXDetails) => {
                                if (err) {
                                    return reject(err);
                                }
                                if (!fallbackUPIXDetails) {
                                    return reject(new Error('No active UPI payment method configured'));
                                }
                                processFeaturePaymentWithUPI(fallbackUPIXDetails);
                            });
                            return;
                        }
                        
                        processFeaturePaymentWithUPI(primaryUPIXDetails);
                        
                        function processFeaturePaymentWithUPI(upiToUse) {
                            // Generate unique transaction ID using fallback UPI details
                            const transactionId = `TXN_${Date.now()}_${userId}_${featureId}`;
                            
                            // Generate UPI QR data
                            const upiQRData = this.generateUPIQRData({
                                upiId: upiToUse.upi_vpa,
                                amount: feature.price,
                                transactionId: transactionId,
                                purpose: `Feature: ${feature.name}`
                            });

                            // Create payment record
                            db.run(
                                `INSERT INTO payments (user_id, payment_type, item_id, amount, payment_method, transaction_id, upi_qr_data, status, expires_at) 
                                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                                [
                                    userId,
                                    'feature',
                                    featureId,
                                    feature.price,
                                    'upi',
                                    transactionId,
                                    upiQRData,
                                    'pending',
                                    new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24 hours from now
                                ],
                                function(err) {
                                    if (err) {
                                        return reject(err);
                                    }

                                    // Log the payment creation
                                    db.run(
                                        `INSERT INTO payment_logs (payment_id, action, details) 
                                         VALUES (?, ?, ?)`,
                                        [this.lastID, 'created', `Feature payment created for feature ${featureId}`],
                                        (err) => {
                                            if (err) {
                                                console.error('Error logging payment creation:', err);
                                            }
                                        }
                                    );

                                    resolve({
                                        paymentId: this.lastID,
                                        transactionId,
                                        amount: feature.price,
                                        upiQRData,
                                        purpose: `Feature: ${feature.name}`,
                                        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
                                    });
                                }
                            );
                        }
                    });
               });
            });
        });
    }
                    
    /** 
     * Helper function to close properly
     */
    // End of processFeaturePaymentWithUPI function

    /**
     * Verify and update payment status
     */
    static async verifyPayment(paymentId, verificationData = {}) {
        return new Promise(async (resolve, reject) => {
            db.get('SELECT * FROM payments WHERE id = ?', [paymentId], (err, payment) => {
                if (err) {
                    return reject(err);
                }
                if (!payment) {
                    return reject(new Error('Payment not found'));
                }

                // Update payment status to 'paid'
                db.run(
                    `UPDATE payments SET status = 'paid', payment_date = ? WHERE id = ?`,
                    [new Date().toISOString(), paymentId],
                    async (err) => {
                        if (err) {
                            return reject(err);
                        }

                        // Record verification in payment_verification table
                        db.run(
                            `INSERT INTO payment_verification (payment_id, verification_status, verification_response, verified_by, verified_at, notes) 
                             VALUES (?, ?, ?, ?, ?, ?)`,
                            [
                                paymentId,
                                'verified',
                                JSON.stringify(verificationData),
                                'system',
                                new Date().toISOString(),
                                'Payment verified and status updated'
                            ],
                            (err) => {
                                if (err) {
                                    console.error('Error recording payment verification:', err);
                                }
                            }
                        );

                        // Log the verification
                        db.run(
                            `INSERT INTO payment_logs (payment_id, action, details) 
                             VALUES (?, ?, ?)`,
                            [paymentId, 'verified', 'Payment verified and status updated to paid'],
                            async (err) => {
                                if (err) {
                                    console.error('Error logging payment verification:', err);
                                }
                                
                                // Sync payment status to PHP server
                                try {
                                    await this.syncPaymentStatusToPhp(paymentId, 'paid');
                                } catch (phpErr) {
                                    console.error('Error syncing payment verification to PHP server:', phpErr);
                                    // Don't fail the operation if PHP sync fails, but log it
                                }
                            }
                        );

                        // Process the payment completion based on type
                        if (payment.payment_type === 'subscription') {
                            this.processSubscriptionPayment(payment);
                        }else if (payment.payment_type === 'feature') {
                            this.processFeaturePayment(payment)
                                .then(() => {
                                    // Sync payment status to PHP server after feature processing
                                    this.syncPaymentStatusToPhp(paymentId, 'paid')
                                        .catch(phpErr => {
                                            console.error('Error syncing feature payment to PHP server:', phpErr);
                                        });
                                })
                                .catch(err => {
                                    console.error('Error processing feature payment:', err);
                                });
                        }

                        resolve({ success: true, paymentId, status: 'paid' });
                    }
                );
            });
        });
    }

    /**
     * Process a completed subscription payment
     */
    static async processSubscriptionPayment(payment) {
        return new Promise(async (resolve, reject) => {
            // Get plan duration
            db.get('SELECT duration_days FROM plans WHERE id = ?', [payment.item_id], (err, plan) => {
                if (err) {
                    return reject(err);
                }
                if (!plan) {
                    return reject(new Error('Plan not found for payment'));
                }

                const startDate = new Date();
                const endDate = new Date();
                endDate.setDate(startDate.getDate() + plan.duration_days);

                // Check if user already has an active subscription
                db.get(
                    'SELECT * FROM subscriptions WHERE user_id = ? AND status = ?',
                    [payment.user_id, 'active'],
                    async (err, existingSubscription) => {
                        if (err) {
                            return reject(err);
                        }

                        if (existingSubscription) {
                            // Update existing subscription locally
                            db.run(
                                'UPDATE subscriptions SET plan_id = ?, end_date = ?, payment_id = ? WHERE id = ?',
                                [payment.item_id, endDate.toISOString().split('T')[0], payment.id, existingSubscription.id],
                                async (err) => {
                                    if (err) {
                                        return reject(err);
                                    }
                                    
                                    // Sync with PHP server
                                    try {
                                        await this.syncSubscriptionWithPhp(payment.user_id, payment.item_id, endDate.toISOString().split('T')[0], 'active');
                                        // Sync payment status to PHP server as well
                                        await this.syncPaymentStatusToPhp(payment.id, 'paid');
                                    } catch (phpErr) {
                                        console.error('Error syncing subscription/payment with PHP server:', phpErr);
                                        // Don't fail the operation if PHP sync fails, but log it
                                    }
                                    
                                    resolve();
                                }
                            );
                        } else {
                            // Create new subscription locally
                            db.run(
                                'INSERT INTO subscriptions (user_id, plan_id, end_date, status, payment_id) VALUES (?, ?, ?, ?, ?)',
                                [payment.user_id, payment.item_id, endDate.toISOString().split('T')[0], 'active', payment.id],
                                async (err) => {
                                    if (err) {
                                        return reject(err);
                                    }
                                    
                                    // Sync with PHP server
                                    try {
                                        await this.syncSubscriptionWithPhp(payment.user_id, payment.item_id, endDate.toISOString().split('T')[0], 'active');
                                        // Sync payment status to PHP server as well
                                        await this.syncPaymentStatusToPhp(payment.id, 'paid');
                                    } catch (phpErr) {
                                        console.error('Error syncing subscription/payment with PHP server:', phpErr);
                                        // Don't fail the operation if PHP sync fails, but log it
                                    }
                                    
                                    resolve();
                                }
                            );
                        }
                    }
                );
            });
        });
    }
    
    /**
     * Sync subscription data with PHP server
     */
    static async syncSubscriptionWithPhp(userId, planId, endDate, status) {
        try {
            // Get user and plan details for the API call
            const user = await new Promise((resolve, reject) => {
                db.get('SELECT email, name FROM users WHERE id = ?', [userId], (err, user) => {
                    if (err) reject(err);
                    else resolve(user);
                });
            });
            
            const plan = await new Promise((resolve, reject) => {
                db.get('SELECT name, price, duration_days FROM plans WHERE id = ?', [planId], (err, plan) => {
                    if (err) reject(err);
                    else resolve(plan);
                });
            });
            
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
            // Check if user already purchased this feature
            db.get(
                'SELECT * FROM user_features WHERE user_id = ? AND feature_id = ?',
                [payment.user_id, payment.item_id],
                async (err, existingFeature) => {
                    if (err) {
                        return reject(err);
                    }

                    if (existingFeature) {
                        // Feature already purchased, just return
                        return resolve();
                    }

                    // Record the feature purchase
                    db.run(
                        'INSERT INTO user_features (user_id, feature_id, purchase_date, payment_id) VALUES (?, ?, ?, ?)',
                        [payment.user_id, payment.item_id, new Date().toISOString().split('T')[0], payment.id],
                        async (err) => {
                            if (err) {
                                return reject(err);
                            }
                            
                            // Sync feature purchase with PHP server
                            try {
                                await this.syncFeaturePurchaseWithPhp(payment.user_id, payment.item_id, new Date().toISOString().split('T')[0]);
                                // Also sync payment status to PHP server
                                await this.syncPaymentStatusToPhp(payment.id, 'paid');
                            } catch (phpErr) {
                                console.error('Error syncing feature purchase/payment with PHP server:', phpErr);
                                // Don't fail the operation if PHP sync fails, but log it
                            }
                            
                            resolve();
                        }
                    );
                }
            );
        });
    }
    
    /**
     * Sync feature purchase data with PHP server
     */
    static async syncFeaturePurchaseWithPhp(userId, featureId, purchaseDate) {
        try {
            // Get user and feature details for the API call
            const user = await new Promise((resolve, reject) => {
                db.get('SELECT email, name FROM users WHERE id = ?', [userId], (err, user) => {
                    if (err) reject(err);
                    else resolve(user);
                });
            });
            
            const feature = await new Promise((resolve, reject) => {
                db.get('SELECT name, price FROM features WHERE id = ?', [featureId], (err, feature) => {
                    if (err) reject(err);
                    else resolve(feature);
                });
            });
            
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
            // Get payment details for the API call
            const payment = await this.getPaymentDetails(paymentId);
            
            if (!payment) {
                throw new Error(`Payment with ID ${paymentId} not found`);
            }
            
            // Call PHP API to update payment status
            const response = await callPhpApi('/api/v1/action', {
                action: 'payment',
                task: 'update-status',
                paymentId: paymentId,
                status: status,
                userId: payment.user_id,
                paymentType: payment.payment_type,
                itemId: payment.item_id,
                amount: payment.amount,
                paymentDate: payment.payment_date || new Date().toISOString()
            });
            
            return response;
        } catch (error) {
            console.error('Error syncing payment status to PHP server:', error);
            throw error;
        }
    }

    /**
     * Get payment details by ID
     */
    static async getPaymentDetails(paymentId) {
        return new Promise((resolve, reject) => {
            db.get(
                `SELECT p.*, pl.name as plan_name, f.name as feature_name 
                 FROM payments p
                 LEFT JOIN plans pl ON (p.payment_type = 'subscription' AND p.item_id = pl.id)
                 LEFT JOIN features f ON (p.payment_type = 'feature' AND p.item_id = f.id)
                 WHERE p.id = ?`,
                [paymentId],
                (err, payment) => {
                    if (err) {
                        return reject(err);
                    }
                    resolve(payment);
                }
            );
        });
    }

    /**
     * Check payment status by ID
     */
    static async checkPaymentStatus(paymentId) {
        return new Promise((resolve, reject) => {
            db.get(
                `SELECT p.status, p.payment_date, pl.name as plan_name, f.name as feature_name,
                 pl.duration_days, f.price as feature_price
                 FROM payments p
                 LEFT JOIN plans pl ON (p.payment_type = 'subscription' AND p.item_id = pl.id)
                 LEFT JOIN features f ON (p.payment_type = 'feature' AND p.item_id = f.id)
                 WHERE p.id = ?`,
                [paymentId],
                (err, payment) => {
                    if (err) {
                        return reject(err);
                    }
                    
                    if (!payment) {
                        return reject(new Error('Payment not found'));
                    }
                    
                    resolve({
                        status: payment.status,
                        paymentDate: payment.payment_date,
                        planName: payment.plan_name,
                        featureName: payment.feature_name,
                        durationDays: payment.duration_days,
                        featurePrice: payment.feature_price
                    });
                }
            );
        });
    }

    /**
     * Get primary UPI detail from PHP server
     */
    static async getPrimaryUPIXDetail() {
        return new Promise(async (resolve, reject) => {
            try {
                // Try to get from PHP server first
                const phpResponse = await callPhpApi('/api/v1/action', {
                    action: 'upi',
                    task: 'get-primary'
                });
                
                if (phpResponse.success && phpResponse.data) {
                    resolve(phpResponse.data);
                } else {
                    // Fall back to local database
                    db.get('SELECT * FROM system_upi_details WHERE is_primary = 1 AND is_active = 1 LIMIT 1', (err, upiDetails) => {
                        if (err) {
                            return reject(err);
                        }
                        resolve(upiDetails || null);
                    });
                }
            } catch (error) {
                console.error('Error getting primary UPI detail:', error);
                // Fall back to local database if PHP server fails
                db.get('SELECT * FROM system_upi_details WHERE is_primary = 1 AND is_active = 1 LIMIT 1', (err, upiDetails) => {
                    if (err) {
                        return reject(err);
                    }
                    resolve(upiDetails || null);
                });
            }
        });
    }

    /**
     * Get all UPI details from PHP server
     */
    static async getAllUPIXDetails() {
        return new Promise(async (resolve, reject) => {
            try {
                // Try to get from PHP server first
                const phpResponse = await callPhpApi('/api/v1/action', {
                    action: 'upi',
                    task: 'get-all'
                });
                
                if (phpResponse.success && phpResponse.data) {
                    resolve(phpResponse.data);
                } else {
                    // Fall back to local database
                    db.all('SELECT * FROM system_upi_details ORDER BY is_primary DESC, created_at DESC', [], (err, upiDetails) => {
                        if (err) {
                            return reject(err);
                        }
                        resolve(upiDetails || []);
                    });
                }
            } catch (error) {
                console.error('Error getting all UPI details:', error);
                // Fall back to local database if PHP server fails
                db.all('SELECT * FROM system_upi_details ORDER BY is_primary DESC, created_at DESC', [], (err, upiDetails) => {
                    if (err) {
                        return reject(err);
                    }
                    resolve(upiDetails || []);
                });
            }
        });
    }

    /**
     * Get user's payment history
     */
    static async getUserPaymentHistory(userId) {
        return new Promise((resolve, reject) => {
            db.all(
                `SELECT p.*, pl.name as plan_name, f.name as feature_name,
                 pv.verification_status, pv.verified_at
                 FROM payments p
                 LEFT JOIN plans pl ON (p.payment_type = 'subscription' AND p.item_id = pl.id)
                 LEFT JOIN features f ON (p.payment_type = 'feature' AND p.item_id = f.id)
                 LEFT JOIN payment_verification pv ON p.id = pv.payment_id
                 WHERE p.user_id = ?
                 ORDER BY p.created_at DESC`,
                [userId],
                (err, payments) => {
                    if (err) {
                        return reject(err);
                    }
                    resolve(payments);
                }
            );
        });
    }

    /**
     * Get user's pending payments
     */
    static async getUserPendingPayments(userId) {
        return new Promise((resolve, reject) => {
            db.all(
                `SELECT p.*, pl.name as plan_name, f.name as feature_name 
                 FROM payments p
                 LEFT JOIN plans pl ON (p.payment_type = 'subscription' AND p.item_id = pl.id)
                 LEFT JOIN features f ON (p.payment_type = 'feature' AND p.item_id = f.id)
                 WHERE p.user_id = ? AND p.status = 'pending'`,
                [userId],
                (err, payments) => {
                    if (err) {
                        return reject(err);
                    }
                    resolve(payments);
                }
            );
        });
    }

    /**
     * Cancel a pending payment
     */
    static async cancelPayment(paymentId) {
        return new Promise((resolve, reject) => {
            db.get('SELECT status FROM payments WHERE id = ?', [paymentId], (err, payment) => {
                if (err) {
                    return reject(err);
                }
                if (!payment) {
                    return reject(new Error('Payment not found'));
                }
                if (payment.status !== 'pending') {
                    return reject(new Error('Cannot cancel payment that is not in pending status'));
                }

                db.run('UPDATE payments SET status = ? WHERE id = ?', ['cancelled', paymentId], (err) => {
                    if (err) {
                        return reject(err);
                    }

                    // Log the cancellation
                    db.run(
                        `INSERT INTO payment_logs (payment_id, action, details) 
                         VALUES (?, ?, ?)`,
                        [paymentId, 'cancelled', 'Payment cancelled by user'],
                        (err) => {
                            if (err) {
                                console.error('Error logging payment cancellation:', err);
                            }
                        }
                    );

                    resolve({ success: true, paymentId, status: 'cancelled' });
                });
            });
        });
    }
}