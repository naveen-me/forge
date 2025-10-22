<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Additional Features</h1>
    
    <p class="text-gray-600 mb-8">Purchase one-time features to enhance your experience</p>
    
    <div v-if="successMessage" class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4" role="alert">
      <span class="block sm:inline">{{ successMessage }}</span>
    </div>
    
    <div v-if="subscription.availableFeatures.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="feature in subscription.availableFeatures" 
        :key="feature.id"
        class="border rounded-lg p-6 bg-white shadow-md transition-transform hover:scale-105"
      >
        <h2 class="text-xl font-bold text-gray-800 mb-2">{{ feature.name }}</h2>
        <p class="text-gray-600 mb-4">{{ feature.description }}</p>
        <p class="text-2xl font-bold text-green-600 mb-4">${{ feature.price }}</p>
        
        <div v-if="hasFeature(feature.id)" class="mb-4">
          <span class="inline-block bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-semibold">
            ✓ Purchased
          </span>
        </div>
        
        <button 
          @click="purchaseFeature(feature.id)"
          :disabled="hasFeature(feature.id) || loading"
          class="w-full bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded mt-4 transition-colors"
          :class="{ 'opacity-50 cursor-not-allowed': hasFeature(feature.id) || loading }"
        >
          {{ loading ? 'Processing...' : (hasFeature(feature.id) ? 'Purchased' : 'Purchase Now') }}
        </button>
      </div>
    </div>
    <div v-else class="text-center py-12">
      <p class="text-gray-500">No features available at the moment</p>
    </div>
    
    <!-- User's Purchased Features -->
    <div class="mt-12">
      <h2 class="text-2xl font-bold text-gray-800 mb-4">Your Features</h2>
      
      <div v-if="subscription.purchasedFeatures.length">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div 
            v-for="feature in subscription.purchasedFeatures" 
            :key="feature.id"
            class="border rounded-lg p-4 bg-gray-50"
          >
            <h3 class="font-bold text-lg">{{ feature.name }}</h3>
            <p class="text-gray-600 text-sm">Purchased: {{ formatDate(feature.purchase_date) }}</p>
          </div>
        </div>
      </div>
      <p v-else class="text-gray-600">You haven't purchased any features yet</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useSubscriptionStore } from '../store';

const subscription = useSubscriptionStore();
const loading = ref(false);
const successMessage = ref('');

// Fetch features when component mounts
subscription.fetchUserSubscription();

async function purchaseFeature(featureId) {
  if (hasFeature(featureId)) {
    return; // Already purchased
  }
  
  loading.value = true;
  try {
    await subscription.purchaseFeature(featureId);
    // Refresh subscription data to show new purchase
    await subscription.fetchUserSubscription();
    
    // Show success message
    successMessage.value = 'Feature purchased successfully!';
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (error) {
    console.error('Feature purchase failed:', error);
    alert(error.message || 'Failed to purchase feature');
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
</script>