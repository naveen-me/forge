<template>
  <div class="flex items-center justify-center min-h-full px-4 sm:px-6 lg:px-8">
    <div class="w-full max-w-md space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">Create an account</h2>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
        <div v-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
          <span class="block sm:inline">{{ error }}</span>
        </div>
         <div v-if="success" class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative" role="alert">
          <span class="block sm:inline">{{ success }}</span>
        </div>
        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="name" class="sr-only">Name</label>
            <input id="name" v-model="name" name="name" type="text" required class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" placeholder="Full Name">
          </div>
          <div>
            <label for="email-address" class="sr-only">Email address</label>
            <input id="email-address" v-model="email" name="email" type="email" autocomplete="email" required class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" placeholder="Email address">
          </div>
          <div>
            <label for="password" class="sr-only">Password</label>
            <input id="password" v-model="password" name="password" type="password" autocomplete="new-password" required class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" placeholder="Password">
          </div>
        </div>

        <div class="flex items-center justify-between">
          <div class="text-sm">
            <router-link to="/login" class="font-medium text-indigo-600 hover:text-indigo-500"> Already have an account? Sign in </router-link>
          </div>
        </div>

        <div>
          <button type="submit" class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
            Register
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store';

const router = useRouter();
const name = ref('');
const email = ref('');
const password = ref('');
const error = ref('');
const success = ref('');
const auth = useAuthStore();

const handleRegister = async () => {
  error.value = '';
  success.value = '';
  try {
    const message = await auth.register(name.value, email.value, password.value);
    success.value = message;
    
    // Redirect to login page after successful registration
    setTimeout(() => {
      router.push('/login');
    }, 2000); // 2 seconds delay to show success message
    
  } catch (err) {
    error.value = err.message || 'Failed to register';
  }
};
</script>
