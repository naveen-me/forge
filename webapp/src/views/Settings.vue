<template>
  <div class="flex flex-col h-screen bg-background-light font-display text-gray-800">
    <header class="p-6 border-b border-gray-200">
      <div class="flex flex-wrap justify-between items-center gap-3">
        <div class="flex flex-col gap-1">
          <h1 class="text-gray-900 text-3xl font-bold leading-tight tracking-tight">Settings</h1>
          <p class="text-gray-500 text-sm">Configure your preferences and OBS integration</p>
        </div>
      </div>
    </header>

    <div class="flex-1 overflow-y-auto p-6">
      <div class="max-w-4xl mx-auto space-y-8">
        <!-- Timezone Settings -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-bold text-gray-900">Timezone Settings</h2>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Timezone</label>
              <select 
                v-model="settings.timezone"
                class="w-full max-w-xs px-3 py-2 rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
              >
                <option v-for="tz in timezones" :key="tz.value" :value="tz.value">
                  {{ tz.label }}
                </option>
              </select>
              <p class="mt-1 text-sm text-gray-500">Select your local timezone for accurate scheduling</p>
            </div>
          </div>
        </div>

        <!-- OBS Integration -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-bold text-gray-900">OBS Integration</h2>
          </div>
          <div class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">OBS Host</label>
                <input
                  type="text"
                  v-model="settings.obs_host"
                  placeholder="localhost"
                  class="w-full px-3 py-2 rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">OBS Port</label>
                <input
                  type="number"
                  v-model.number="settings.obs_port"
                  placeholder="4455"
                  class="w-full px-3 py-2 rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
                />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">OBS Password</label>
              <input
                type="password"
                v-model="settings.obs_password"
                placeholder="Enter OBS WebSocket password"
                class="w-full max-w-xs px-3 py-2 rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Scene Name</label>
              <input
                type="text"
                v-model="settings.scene_name"
                placeholder="Media Playout"
                class="w-full max-w-xs px-3 py-2 rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm"
              />
            </div>
            <div class="flex items-center">
              <input
                type="checkbox"
                v-model="settings.auto_start_stream"
                class="h-4 w-4 text-primary focus:ring-primary border-gray-300 rounded"
              />
              <label class="ml-2 block text-sm text-gray-700">
                Auto-start stream when schedule begins
              </label>
            </div>
          </div>
        </div>

        <!-- Theme Settings -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-bold text-gray-900">Appearance</h2>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Theme</label>
              <div class="flex space-x-4">
                <label class="inline-flex items-center">
                  <input
                    type="radio"
                    v-model="settings.theme"
                    value="light"
                    class="text-primary focus:ring-primary"
                  />
                  <span class="ml-2">Light</span>
                </label>
                <label class="inline-flex items-center">
                  <input
                    type="radio"
                    v-model="settings.theme"
                    value="dark"
                    class="text-primary focus:ring-primary"
                  />
                  <span class="ml-2">Dark</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Save Button -->
        <div class="flex justify-end pt-4">
          <button
            @click="saveSettings"
            :disabled="isSaving"
            class="px-6 py-2 rounded-md bg-primary text-white font-medium hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ isSaving ? 'Saving...' : 'Save Settings' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { api, settingsService } from '../services/api';

export default {
  name: 'Settings',
  data() {
    return {
      settings: {
        timezone: 'UTC',
        obs_host: 'localhost',
        obs_port: 4455,
        obs_password: '',
        scene_name: 'Media Playout',
        auto_start_stream: false,
        theme: 'light'
      },
      isSaving: false,
      timezones: [
        { value: 'UTC', label: 'UTC (Coordinated Universal Time)' },
        { value: 'GMT', label: 'GMT (Greenwich Mean Time)' },
        { value: 'EST', label: 'EST (Eastern Standard Time)' },
        { value: 'EDT', label: 'EDT (Eastern Daylight Time)' },
        { value: 'CST', label: 'CST (Central Standard Time)' },
        { value: 'CDT', label: 'CDT (Central Daylight Time)' },
        { value: 'MST', label: 'MST (Mountain Standard Time)' },
        { value: 'MDT', label: 'MDT (Mountain Daylight Time)' },
        { value: 'PST', label: 'PST (Pacific Standard Time)' },
        { value: 'PDT', label: 'PDT (Pacific Daylight Time)' },
        { value: 'IST', label: 'IST (Indian Standard Time)' },
        { value: 'JST', label: 'JST (Japan Standard Time)' },
        { value: 'CET', label: 'CET (Central European Time)' },
        { value: 'CEST', label: 'CEST (Central European Summer Time)' },
      ]
    };
  },
  async mounted() {
    await this.loadSettings();
  },
  methods: {
    async loadSettings() {
      try {
        const response = await settingsService.getSettings();
        if (response.data.success) {
          this.settings = {
            ...this.settings,
            ...response.data.data
          };
        }
      } catch (error) {
        console.error('Error loading settings:', error);
        // Initialize with default values or show error to user
      }
    },
    async saveSettings() {
      this.isSaving = true;
      try {
        await settingsService.updateSettings(this.settings);
        // Show success message to user
        alert('Settings saved successfully!');
      } catch (error) {
        console.error('Error saving settings:', error);
        alert('Error saving settings: ' + (error.response?.data?.message || error.message));
      } finally {
        this.isSaving = false;
      }
    }
  }
};
</script>