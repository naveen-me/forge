# PHP API Endpoints for Subscription and Feature Management

This document outlines the required PHP API endpoints that need to be implemented to support the subscription and feature purchase flow with UPI payments.

## API Structure
All endpoints follow the pattern: `POST /api/v1/action`

The request body should be a JSON with the following structure:
```json
{
  "action": "subscription|feature|auth",
  "task": "specific-task",
  "...": "additional parameters based on task"
}
```

## Required Endpoints

### 1. Get Available Subscription Plans
**Action:** `subscription`
**Task:** `get-plans`

**Request:**
```json
{
  "action": "subscription",
  "task": "get-plans"
}
```

**Response (Success):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Basic",
      "price": 9.99,
      "duration_days": 30,
      "features": ["Ad-Free Experience"]
    }
  ]
}
```

**Response (Error):**
```json
{
  "success": false,
  "error": "Error message"
}
```

### 2. Update User Subscription (after payment verification)
**Action:** `subscription`
**Task:** `update`

**Request:**
```json
{
  "action": "subscription",
  "task": "update",
  "userId": 123,
  "email": "user@example.com",
  "planId": 1,
  "planName": "Premium",
  "planPrice": 29.99,
  "planDuration": 30,
  "endDate": "2025-12-31",
  "status": "active"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Subscription updated successfully"
}
```

### 3. Get User's Current Subscription
**Action:** `subscription`
**Task:** `get-user-subscription`

**Request:**
```json
{
  "action": "subscription",
  "task": "get-user-subscription",
  "userId": 123
}
```

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "subscription": {
      "id": 456,
      "plan_id": 1,
      "plan_name": "Premium",
      "price": 29.99,
      "duration_days": 30,
      "end_date": "2025-12-31",
      "status": "active"
    },
    "purchased_features": [
      {
        "id": 1,
        "name": "Ad-Free Experience",
        "description": "Enjoy the app without any advertisements",
        "price": 4.99,
        "purchase_date": "2025-10-20"
      }
    ],
    "available_features": [
      {
        "id": 1,
        "name": "Ad-Free Experience",
        "description": "Enjoy the app without any advertisements",
        "price": 4.99
      }
    ]
  }
}
```

### 4. Cancel User's Subscription
**Action:** `subscription`
**Task:** `cancel`

**Request:**
```json
{
  "action": "subscription",
  "task": "cancel",
  "userId": 123,
  "subscriptionId": 456
}
```

**Response:**
```json
{
  "success": true,
  "message": "Subscription cancelled successfully"
}
```

### 5. Record Feature Purchase
**Action:** `feature`
**Task:** `purchase`

**Request:**
```json
{
  "action": "feature",
  "task": "purchase",
  "userId": 123,
  "email": "user@example.com",
  "featureId": 1,
  "featureName": "Ad-Free Experience",
  "featurePrice": 4.99,
  "purchaseDate": "2025-10-20"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Feature purchase recorded successfully"
}
```

## Security Considerations

1. **Signature Verification**: All requests must include a proper signature in the `X-Signature` header, created using HMAC-SHA256 with the shared secret.

2. **User Authentication**: The PHP server should validate that the userId corresponds to the authenticated user where appropriate.

3. **Data Validation**: All input data should be validated before storage, including:
   - Valid email format
   - Positive numeric values for prices and durations
   - Valid date formats
   - Valid status values (active, cancelled, expired, etc.)

4. **Rate Limiting**: Consider implementing rate limiting to prevent abuse.

## Database Schema for PHP Server

The PHP server should maintain the following tables:

```
users
- id (primary key)
- email (unique)
- name
- created_at

plans
- id (primary key)
- name
- price
- duration_days
- is_active
- created_at

subscriptions
- id (primary key)
- user_id (foreign key to users)
- plan_id (foreign key to plans)
- start_date
- end_date
- status (active, cancelled, expired)
- created_at
- updated_at

features
- id (primary key)
- name
- description
- price
- is_active
- created_at

user_features
- id (primary key)
- user_id (foreign key to users)
- feature_id (foreign key to features)
- purchase_date
- created_at

plan_features (junction table)
- id (primary key)
- plan_id (foreign key to plans)
- feature_id (foreign key to features)
```

## Implementation Notes

1. The Node.js server acts as a middleman between the frontend and the PHP server
2. Subscription and feature purchase data should be stored and validated on the PHP server as the authoritative source
3. The Node.js server can maintain a local cache of plans for performance but should always defer to the PHP server for user-specific data
4. When PHP server is unavailable, the Node.js server should fall back to local data where appropriate
```