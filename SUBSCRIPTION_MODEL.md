# Subscription Model Documentation

This document provides a comprehensive overview of the subscription system implemented in the application, including database schema, API endpoints, and frontend components.

## Database Schema

The subscription system extends the existing database with the following tables:

### features
- `id`: INTEGER PRIMARY KEY - Unique identifier for each feature
- `name`: TEXT NOT NULL - Name of the feature
- `description`: TEXT - Description of the feature
- `price`: REAL NOT NULL - Price of the one-time feature purchase

### plan_features
- `id`: INTEGER PRIMARY KEY - Unique identifier for each relationship
- `plan_id`: INTEGER NOT NULL - Reference to plans table
- `feature_id`: INTEGER NOT NULL - Reference to features table

### user_features
- `id`: INTEGER PRIMARY KEY - Unique identifier for each purchase
- `user_id`: INTEGER NOT NULL - Reference to users table
- `feature_id`: INTEGER NOT NULL - Reference to features table
- `purchase_date`: TEXT NOT NULL - Date when the feature was purchased

### Updated tables
- `plans` table (existing) - Contains subscription plan information
- `subscriptions` table (existing) - Contains user subscription information with fields:
  - `id`: INTEGER PRIMARY KEY
  - `user_id`: INTEGER NOT NULL - Reference to users table
  - `plan_id`: INTEGER NOT NULL - Reference to plans table
  - `end_date`: TEXT NOT NULL - Date when subscription expires
  - `status`: TEXT NOT NULL - Current status (active, cancelled, expired)

## API Endpoints

### GET /api/subscription/plans
- **Description**: Retrieves all available subscription plans with their features
- **Auth Required**: No
- **Response**:
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
    ],
    "source": "cache"
  }
  ```

### POST /api/subscription/subscribe
- **Description**: Subscribe user to a plan
- **Auth Required**: Yes
- **Request Body**:
  ```json
  {
    "planId": 1
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Subscription created successfully",
    "subscription": {
      "id": 1,
      "plan_id": 1,
      "end_date": "2023-12-31",
      "status": "active"
    }
  }
  ```

### POST /api/subscription/purchase-feature
- **Description**: Purchase a one-time feature
- **Auth Required**: Yes
- **Request Body**:
  ```json
  {
    "featureId": 1
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Feature purchased successfully",
    "feature": {
      "id": 1,
      "name": "Ad-Free Experience",
      "purchase_date": "2023-11-30"
    }
  }
  ```

### GET /api/subscription/my-subscription
- **Description**: Retrieve user's current subscription and purchased features
- **Auth Required**: Yes
- **Response**:
  ```json
  {
    "success": true,
    "subscription": {
      "id": 1,
      "plan_id": 1,
      "plan_name": "Basic",
      "price": 9.99,
      "duration_days": 30,
      "end_date": "2023-12-31",
      "status": "active"
    },
    "purchased_features": [
      {
        "id": 1,
        "name": "Ad-Free Experience",
        "description": "Enjoy the app without any advertisements",
        "price": 4.99,
        "purchase_date": "2023-11-30"
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
  ```

### DELETE /api/subscription/cancel
- **Description**: Cancel user's active subscription
- **Auth Required**: Yes
- **Response**:
  ```json
  {
    "success": true,
    "message": "Subscription cancelled successfully",
    "subscription_id": 1
  }
  ```

## Frontend Components

### Subscription.vue
- Displays available subscription plans with their features
- Allows users to subscribe to a plan
- Shows current subscription status
- Provides option to cancel subscription
- Displays available one-time features
- Shows user's purchased features

### Features.vue
- Dedicated component for browsing and purchasing one-time features
- Shows feature details and pricing
- Displays user's purchased features
- Provides purchase buttons for available features

## Vuex Store

### Subscription Store
- `plans`: Array of available subscription plans
- `userSubscription`: Current user's subscription details
- `purchasedFeatures`: Array of features purchased by the user
- `availableFeatures`: Array of all available features

### Actions
- `fetchPlans()`: Fetch available subscription plans
- `subscribe(planId)`: Subscribe to a plan
- `purchaseFeature(featureId)`: Purchase a one-time feature
- `fetchUserSubscription()`: Fetch user's subscription and features
- `cancelSubscription()`: Cancel user's subscription

## Payment Processing

The current implementation includes stubbed payment processing logic. In a production environment, you would need to integrate with a payment processor like Stripe or PayPal.

The main payment integration points are in:
- `server/src/controllers/subscriptionController.js` in the `subscribe` and `purchaseFeature` functions
- The actual payment processing would happen before updating the database

## Navigation Guards

The router includes a navigation guard that:
- Checks authentication status
- Fetches user's subscription status when accessing protected routes
- Can be extended to restrict access based on subscription status

## Seeded Data

The system includes initial seeded data:
- Three subscription plans: Basic ($9.99), Pro ($19.99), Premium ($29.99)
- Three one-time features: Ad-Free Experience ($4.99), Priority Support ($2.99), Advanced Analytics ($9.99)
- Plan-feature relationships where:
  - Basic includes Ad-Free Experience
  - Pro includes Ad-Free Experience and Priority Support
  - Premium includes all features