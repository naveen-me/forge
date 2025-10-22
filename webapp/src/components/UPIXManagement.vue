<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">UPI Management</h1>
    
    <div v-if="successMessage" class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4" role="alert">
      <span class="block sm:inline">{{ successMessage }}</span>
    </div>
    
    <div v-if="errorMessage" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4" role="alert">
      <span class="block sm:inline">{{ errorMessage }}</span>
    </div>

    <!-- Add UPI Detail Form -->
    <div class="bg-white p-6 rounded-lg shadow-md mb-8">
      <h2 class="text-xl font-bold text-gray-800 mb-4">Add New UPI Detail</h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">UPI ID</label>
          <input 
            v-model="newUPIXDetail.upiId"
            type="text"
            placeholder="e.g., yourname@upi"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">UPI VPA</label>
          <input 
            v-model="newUPIXDetail.upiVpa"
            type="text"
            placeholder="e.g., yourname@bank"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>
      
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">Display Name (Optional)</label>
        <input 
          v-model="newUPIXDetail.displayName"
          type="text"
          placeholder="e.g., Business Account"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      
      <button 
        @click="addUPIXDetail"
        :disabled="!newUPIXDetail.upiId || !newUPIXDetail.upiVpa || loading"
        class="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
      >
        {{ loading ? 'Adding...' : 'Add UPI Detail' }}
      </button>
    </div>

    <!-- UPI Details List -->
    <div class="bg-white p-6 rounded-lg shadow-md">
      <h2 class="text-xl font-bold text-gray-800 mb-4">Configured UPI Details</h2>
      
      <div v-if="subscription.upiDetails.length" class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Display Name</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">UPI ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">UPI VPA</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="upi in subscription.upiDetails" :key="upi.id">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">{{ upi.display_name || upi.upi_id }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">{{ upi.upi_id }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">{{ upi.upi_vpa }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span 
                  class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                  :class="{
                    'bg-green-100 text-green-800': upi.is_primary,
                    'bg-yellow-100 text-yellow-800': !upi.is_primary && upi.is_active,
                    'bg-red-100 text-red-800': !upi.is_active
                  }"
                >
                  {{ upi.is_primary ? 'Primary' : (upi.is_active ? 'Active' : 'Inactive') }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex space-x-2">
                  <button 
                    v-if="!upi.is_primary"
                    @click="setPrimaryUPIXDetail(upi.id)"
                    class="text-blue-600 hover:text-blue-900"
                    title="Set as Primary"
                  >
                    Set Primary
                  </button>
                  <span v-else class="text-green-600 font-semibold">Primary ✓</span>
                  
                  <button 
                    @click="deleteUPIXDetail(upi.id)"
                    class="text-red-600 hover:text-red-900"
                    title="Delete"
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div v-else class="text-center py-8 text-gray-500">
        <p>No UPI details configured yet.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useSubscriptionStore } from '../store';

const subscription = useSubscriptionStore();
const loading = ref(false);
const successMessage = ref('');
const errorMessage = ref('');
const newUPIXDetail = ref({
  upiId: '',
  upiVpa: '',
  displayName: ''
});

onMounted(() => {
  subscription.fetchUPIXDetails();
});

async function addUPIXDetail() {
  if (!newUPIXDetail.value.upiId || !newUPIXDetail.value.upiVpa) {
    errorMessage.value = 'UPI ID and UPI VPA are required';
    setTimeout(() => { errorMessage.value = ''; }, 3000);
    return;
  }
  
  loading.value = true;
  try {
    await subscription.addUPIXDetail(newUPIXDetail.value);
    
    // Reset form
    newUPIXDetail.value = {
      upiId: '',
      upiVpa: '',
      displayName: ''
    };
    
    successMessage.value = 'UPI detail added successfully!';
    setTimeout(() => { successMessage.value = ''; }, 3000);
  } catch (error) {
    console.error('Failed to add UPI detail:', error);
    errorMessage.value = error.message || 'Failed to add UPI detail';
    setTimeout(() => { errorMessage.value = ''; }, 3000);
  } finally {
    loading.value = false;
  }
}

async function setPrimaryUPIXDetail(id) {
  try {
    await subscription.setPrimaryUPIXDetail(id);
    successMessage.value = 'Primary UPI updated successfully!';
    setTimeout(() => { successMessage.value = ''; }, 3000);
  } catch (error) {
    console.error('Failed to set primary UPI:', error);
    errorMessage.value = error.message || 'Failed to set primary UPI';
    setTimeout(() => { errorMessage.value = ''; }, 3000);
  }
}

async function deleteUPIXDetail(id) {
  if (!confirm('Are you sure you want to delete this UPI detail?')) {
    return;
  }
  
  try {
    await subscription.deleteUPIXDetail(id);
    successMessage.value = 'UPI detail deleted successfully!';
    setTimeout(() => { successMessage.value = ''; }, 3000);
  } catch (error) {
    console.error('Failed to delete UPI detail:', error);
    errorMessage.value = error.message || 'Failed to delete UPI detail';
    setTimeout(() => { errorMessage.value = ''; }, 3000);
  }
}
</script>