<template>
  <div v-if="show" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-lg max-w-md w-full p-6">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-xl font-bold text-gray-800">Complete Payment</h3>
        <button @click="closeModal" class="text-gray-500 hover:text-gray-700">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div v-if="paymentData" class="text-center">
        <h4 class="text-lg font-semibold mb-2">{{ paymentData.purpose }}</h4>
        <p class="text-2xl font-bold text-green-600 mb-4">₹{{ paymentData.amount }}</p>
        
        <div v-if="showQrCode" class="mb-4">
          <p class="text-sm text-gray-600 mb-2">Scan QR Code to Pay</p>
          <div class="flex justify-center">
            <vue-qrcode 
              :value="paymentData.upiQRData" 
              :options="{ width: 200 }"
              class="border p-2 bg-white"
            />
          </div>
        </div>

        <div class="bg-gray-100 p-3 rounded mb-4 text-left">
          <p class="text-sm font-semibold">UPI Payment Details:</p>
          <p class="text-xs mt-1">Amount: ₹{{ paymentData.amount }}</p>
          <p class="text-xs">Transaction ID: {{ paymentData.transactionId }}</p>
          <p class="text-xs">Expires: {{ formatDate(paymentData.expiresAt) }}</p>
        </div>

        <div v-if="showQrCode" class="flex space-x-2">
          <button 
            @click="initiateVerification" 
            :disabled="verifying"
            class="flex-1 bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
          >
            {{ verifying ? 'Verifying...' : 'I have paid' }}
          </button>
          <button 
            @click="checkPaymentStatus"
            :disabled="checkingStatus"
            class="flex-1 bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
          >
            {{ checkingStatus ? 'Checking...' : 'Check Status' }}
          </button>
          <button 
            @click="cancelPayment" 
            :disabled="verifying || checkingStatus"
            class="flex-1 bg-gray-500 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
          >
            Cancel
          </button>
        </div>

        <div v-if="statusMessage" class="mt-4 text-center">
          <p class="text-gray-600">{{ statusMessage }}</p>
        </div>

        <div v-if="verifying" class="mt-4 text-center">
          <p class="text-gray-600">Verifying payment status...</p>
        </div>
        
        <div v-if="checkingStatus" class="mt-2 text-center">
          <p class="text-gray-600">Checking payment status...</p>
        </div>
      </div>

      <div v-else class="text-center">
        <p class="text-gray-600">Processing payment request...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import VueQrcode from 'vue-qrcode';

const props = defineProps({
  show: Boolean,
  paymentData: Object
});

const emit = defineEmits(['close', 'payment-verified']);

const verifying = ref(false);
const checkingStatus = ref(false);
const statusMessage = ref('');
const showQrCode = ref(true);
let pollingInterval = null;
let pollingCounter = 0;

const closeModal = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval);
  }
  emit('close');
};

const initiateVerification = async () => {
  if (!props.paymentData) return;
  
  try {
    verifying.value = true;
    statusMessage.value = 'Initiating payment verification...';
    
    const response = await fetch(`/api/payment/initiate-verification`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        paymentId: props.paymentData.id
      })
    });

    if (response.ok) {
      // Start polling for payment status
      startPollingPaymentStatus();
    } else {
      const error = await response.json();
      statusMessage.value = `Payment verification failed: ${error.error || 'Unknown error'}`;
      verifying.value = false;
    }
  } catch (error) {
    console.error('Error initiating payment verification:', error);
    statusMessage.value = 'Error initiating payment verification. Please try again later.';
    verifying.value = false;
  }
};

const startPollingPaymentStatus = () => {
  pollingCounter = 0;
  pollingInterval = setInterval(async () => {
    pollingCounter++;
    if (pollingCounter > 3) {
      clearInterval(pollingInterval);
      verifying.value = false;
      showQrCode.value = false;
      statusMessage.value = 'It will take some time to verify the payment. Please check the payment history later.';
      return;
    }
    await checkPaymentStatus();
  }, 5000); // Poll every 5 seconds
};

const checkPaymentStatus = async () => {
  if (!props.paymentData) return;
  
  try {
    checkingStatus.value = true;
    statusMessage.value = 'Checking payment status...';
    
    const response = await fetch(`/api/payment/${props.paymentData.id}/status`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    if (response.ok) {
      const result = await response.json();
      if (result.data.status === 'paid') {
        statusMessage.value = 'Payment has been verified successfully!';
        setTimeout(() => {
          emit('payment-verified');
          closeModal();
        }, 2000);
      } else {
        statusMessage.value = `Payment status: ${result.data.status}.`;
      }
    } else {
      const error = await response.json();
      statusMessage.value = `Failed to check payment status: ${error.error || 'Unknown error'}`;
    }
  } catch (error) {
    console.error('Error checking payment status:', error);
    statusMessage.value = 'Error checking payment status. Please try again later.';
  } finally {
    checkingStatus.value = false;
  }
};

const cancelPayment = async () => {
  if (!props.paymentData) return;
  
  try {
    const response = await fetch(`/api/payment/${props.paymentData.id}/cancel`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    if (response.ok) {
      closeModal();
    } else {
      const error = await response.json();
      alert(`Failed to cancel payment: ${error.error || 'Unknown error'}`);
    }
  } catch (error) {
    console.error('Error cancelling payment:', error);
    alert('Error cancelling payment. Please try again later.');
  }
};

const formatDate = (dateString) => {
  const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
  return new Date(dateString).toLocaleDateString(undefined, options);
};

// Close modal if payment expires
onMounted(() => {
  if (props.paymentData?.expiresAt) {
    const expirationTime = new Date(props.paymentData.expiresAt).getTime();
    const currentTime = Date.now();
    
    if (currentTime > expirationTime) {
      alert('Payment request has expired');
      closeModal();
    }
  }
});
</script>