<template>
  <div class="max-w-6xl mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Subscription Plans</h1>
    
    <div v-if="successMessage" class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4" role="alert">
      <span class="block sm:inline">{{ successMessage }}</span>
    </div>
    
    <div v-if="subscription.userSubscription && subscription.userSubscription.status === 'active'" class="bg-blue-100 border border-blue-400 text-blue-700 px-4 py-3 rounded mb-4" role="alert">
      <p class="block sm:inline">You currently have an active subscription: {{ subscription.userSubscription.plan_name }} (expires {{ formatDate(subscription.userSubscription.end_date) }})</p>
    </div>
    
    <div v-if="subscription.plans.length && (!subscription.userSubscription || subscription.userSubscription.status !== 'active')" class="grid grid-cols-1 md:grid-cols-3 gap-8">
      <div v-for="plan in subscription.plans" :key="plan.id" class="bg-white p-8 rounded-lg shadow-md text-center flex flex-col">
        <h2 class="text-2xl font-bold text-gray-800">{{ plan.name }}</h2>
        <p class="text-4xl font-extrabold text-gray-900 my-4">${{ plan.price }}</p>
        <p class="text-gray-500">for {{ plan.duration_days }} days</p>
        <ul class="my-4 space-y-2">
          <li v-for="feature in plan.features" :key="feature" class="text-gray-600">
            <span class="mr-2">✓</span>{{ feature }}
          </li>
        </ul>
        <button 
          @click="subscribeToPlan(plan.id)" 
          class="mt-auto bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded-full w-full transition-colors"
          :disabled="loading"
        >
          {{ loading ? 'Processing...' : 'Choose Plan' }}
        </button>
      </div>
    </div>
    
    <div v-else-if="subscription.userSubscription && subscription.userSubscription.status === 'active'">
      <p class="text-center text-gray-600">You already have an active subscription. You can manage it below.</p>
    </div>
    
    <div v-else>
        <p>Loading plans...</p>
    </div>

    <!-- Current Subscription Section -->
    <div class="mt-12 bg-white p-6 rounded-lg shadow-md">
        <h2 class="text-2xl font-bold text-gray-800 mb-4">Current Subscription</h2>
        
        <div v-if="subscription.userSubscription">
          <p class="text-green-600 font-semibold">Status: {{ subscription.userSubscription.status }}</p>
          <p><strong>Plan:</strong> {{ subscription.userSubscription.plan_name }}</p>
          <p><strong>Price:</strong> ${{ subscription.userSubscription.price }}</p>
          <p><strong>End Date:</strong> {{ formatDate(subscription.userSubscription.end_date) }}</p>
          
          <button 
            @click="cancelSubscription" 
            class="mt-4 bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded"
            :disabled="loading"
          >
            {{ loading ? 'Cancelling...' : 'Cancel Subscription' }}
          </button>
        </div>
        <div v-else>
          <p class="text-red-600 font-semibold">No active subscription</p>
        </div>
    </div>

    <!-- Feature Purchase Section -->
    <div class="mt-12 bg-white p-6 rounded-lg shadow-md">
      <h2 class="text-2xl font-bold text-gray-800 mb-4">One-Time Features</h2>
      
      <div v-if="subscription.availableFeatures.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div 
          v-for="feature in subscription.availableFeatures" 
          :key="feature.id"
          class="border rounded-lg p-4 flex flex-col"
        >
          <h3 class="font-bold text-lg">{{ feature.name }}</h3>
          <p class="text-gray-600 text-sm mb-2">{{ feature.description }}</p>
          <p class="text-xl font-bold text-green-600 mb-2">${{ feature.price }}</p>
          
          <!-- Check if user already purchased this feature -->
          <div v-if="hasFeature(feature.id)">
            <span class="text-green-600 font-semibold">✓ Purchased</span>
          </div>
          <button 
            v-else
            @click="purchaseFeature(feature.id)"
            class="mt-auto bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded"
            :disabled="loading"
          >
            {{ loading ? 'Processing...' : 'Purchase Feature' }}
          </button>
        </div>
      </div>
      <p v-else>No features available</p>
    </div>
    
    <!-- User's Purchased Features -->
    <div class="mt-12 bg-white p-6 rounded-lg shadow-md">
      <h2 class="text-2xl font-bold text-gray-800 mb-4">Your Purchased Features</h2>
      
      <div v-if="subscription.purchasedFeatures.length">
        <div v-for="feature in subscription.purchasedFeatures" :key="feature.id" class="border rounded-lg p-4 mb-2">
          <h3 class="font-bold text-lg">{{ feature.name }}</h3>
          <p class="text-gray-600 text-sm">Purchased on: {{ formatDate(feature.purchase_date) }}</p>
        </div>
      </div>
      <p v-else>You haven't purchased any features yet</p>
    </div>

    <!-- Payment History -->
    <div class="mt-12 bg-white p-6 rounded-lg shadow-md">
      <h2 class="text-2xl font-bold text-gray-800 mb-4">Payment History</h2>
      
      <div v-if="subscription.paymentHistory.length" class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Item</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="payment in subscription.paymentHistory" :key="payment.id">
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                  :class="payment.payment_type === 'subscription' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'">
                  {{ payment.payment_type }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ payment.plan_name || payment.feature_name }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                ${{ payment.amount }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                  :class="getStatusClass(payment.status)">
                  {{ payment.status }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ formatDate(payment.created_at) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-else class="text-gray-500">No payment history available.</p>
    </div>
    
    <!-- UPI Payment Modal -->
    <upi-payment-modal 
      :show="showPaymentModal" 
      :payment-data="currentPaymentData"
      @close="showPaymentModal = false"
      @payment-verified="handlePaymentVerified"
    />
    
    <!-- Current UPI Info -->
    <div class="mt-8 bg-blue-50 p-4 rounded-lg border border-blue-200">
      <h3 class="text-lg font-semibold text-blue-800 mb-2">Payment Configuration</h3>
      <p class="text-blue-700">
        <span v-if="subscription.upiDetails && subscription.upiDetails.length">
          <span v-for="upi in subscription.upiDetails" :key="upi.id">
            <span v-if="upi.is_primary" class="font-semibold">Primary UPI: {{ upi.upi_vpa }}</span>
          </span>
          <span v-if="subscription.upiDetails.length > 1">
            ({{ subscription.upiDetails.length - 1 }} other UPI{{ subscription.upiDetails.length > 2 ? 's' : '' }})
          </span>
        </span>
        <span v-else>No UPI details configured</span>
      </p>
      <p class="mt-2 text-sm text-blue-600">
        <router-link to="/upi-management" class="text-blue-800 underline hover:text-blue-600">
          Manage UPI Details
        </router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useSubscriptionStore } from '../store';
import UpiPaymentModal from './UPIPaymentModal.vue';

const subscription = useSubscriptionStore();
const loading = ref(false);
const successMessage = ref('');
const showPaymentModal = ref(false);
const currentPaymentData = ref(null);

onMounted(() => {
  subscription.fetchPlans();
  subscription.fetchUserSubscription();
  subscription.fetchPaymentHistory();
  subscription.fetchUPIXDetails();
});

async function subscribeToPlan(planId) {
  loading.value = true;
  try {
    // This will now return payment details instead of directly subscribing
    const result = await subscription.subscribe(planId);
    
    if (result.payment) {
      currentPaymentData.value = result.payment;
      showPaymentModal.value = true;
    } else {
      successMessage.value = 'Subscription request created successfully!';
      setTimeout(() => {
        successMessage.value = '';
      }, 3000);
    }
  } catch (error) {
    console.error('Subscription failed:', error);
    alert(error.message || 'Failed to create subscription request');
  } finally {
    loading.value = false;
  }
}

async function purchaseFeature(featureId) {
  loading.value = true;
  try {
    const result = await subscription.purchaseFeature(featureId);
    
    if (result.payment) {
      currentPaymentData.value = result.payment;
      showPaymentModal.value = true;
    } else {
      successMessage.value = 'Feature purchase request created successfully!';
      setTimeout(() => {
        successMessage.value = '';
      }, 3000);
    }
  } catch (error) {
    console.error('Feature purchase failed:', error);
    alert(error.message || 'Failed to create feature purchase request');
  } finally {
    loading.value = false;
  }
}

async function cancelSubscription() {
  if (!confirm('Are you sure you want to cancel your subscription? You will lose access when your current subscription expires.')) {
    return;
  }
  
  loading.value = true;
  try {
    await subscription.cancelSubscription();
    // Fetch updated subscription status
    await subscription.fetchUserSubscription();
    
    // Show success message
    successMessage.value = 'Subscription cancelled successfully!';
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (error) {
    console.error('Cancellation failed:', error);
    alert(error.message || 'Failed to cancel subscription');
  } finally {
    loading.value = false;
  }
}

function hasFeature(featureId) {
  return subscription.purchasedFeatures.some(f => f.id === featureId);
}

function formatDate(dateString) {
  const options = { year: 'numeric', month: 'long', day: 'numeric' };
  return new Date(dateString).toLocaleDateString(undefined, options);
}

function getStatusClass(status) {
  switch(status) {
    case 'paid':
      return 'bg-green-100 text-green-800';
    case 'pending':
      return 'bg-yellow-100 text-yellow-800';
    case 'failed':
      return 'bg-red-100 text-red-800';
    case 'cancelled':
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

function handlePaymentVerified() {
  // Refresh subscription data and payment history after payment verification
  subscription.fetchUserSubscription();
  // Add a small delay before fetching payment history to avoid potential race conditions
  setTimeout(() => {
    subscription.fetchPaymentHistory();
  }, 1000);
  successMessage.value = 'Payment verified successfully!';
  setTimeout(() => {
    successMessage.value = '';
  }, 3000);
}
</script>
