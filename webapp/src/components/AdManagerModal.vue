<template>
  <div class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-xl font-bold">Manage Ads for {{ scheduleItem?.item?.name || 'Schedule Item' }}</h3>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      
      <div class="mb-4 p-4 bg-blue-50 rounded-lg">
        <p class="text-sm text-blue-800">
          <span class="font-semibold">Scheduled Duration:</span> {{ formatDuration(scheduleItem?.duration) }} 
          ({{ scheduleItem?.duration }} seconds)
          <br>
          <span class="font-semibold">Available Time:</span> {{ availableTime }} seconds
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <h4 class="font-medium mb-2">Available Ads</h4>
          <div class="space-y-2 max-h-60 overflow-y-auto">
            <div 
              v-for="ad in availableAds" 
              :key="ad.id"
              class="p-3 border rounded-lg hover:bg-gray-50 cursor-pointer"
              :class="{ 'bg-blue-50 border-blue-300': selectedAd?.id === ad.id }"
              @click="selectAd(ad)"
            >
              <div class="flex justify-between items-start">
                <div>
                  <p class="font-medium">{{ ad.name }}</p>
                  <p class="text-sm text-gray-600">{{ formatDuration(ad.duration) }} ({{ ad.duration }}s)</p>
                </div>
                <button 
                  @click.stop="addAdToSchedule(ad)"
                  :disabled="!canAddAd(ad) || isAdAlreadyAdded(ad.id)"
                  class="px-2 py-1 text-xs rounded bg-gray-100 hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {{ isAdAlreadyAdded(ad.id) ? 'Added' : 'Add' }}
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div>
          <div class="flex justify-between items-center mb-2">
            <h4 class="font-medium">Scheduled Ads</h4>
            <span class="text-sm text-gray-600">{{ scheduledAds.length }} ads</span>
          </div>
          
          <div class="space-y-2 max-h-60 overflow-y-auto">
            <div 
              v-for="(scheduledAd, index) in scheduledAds" 
              :key="scheduledAd.id"
              class="p-3 border rounded-lg bg-yellow-50"
            >
              <div class="flex justify-between items-start">
                <div>
                  <p class="font-medium">{{ scheduledAd.item.name }}</p>
                  <p class="text-sm text-gray-600">
                    {{ formatDuration(scheduledAd.duration) }} ({{ scheduledAd.duration }}s)
                    <span v-if="scheduledAd.duration !== scheduledAd.item.duration">
                      of {{ formatDuration(scheduledAd.item.duration) }}
                    </span>
                  </p>
                </div>
                <div class="flex gap-1">
                  <button 
                    @click="adjustAdDuration(scheduledAd, 'reduce')"
                    :disabled="scheduledAd.duration <= 1"
                    class="px-2 py-1 text-xs rounded bg-red-100 hover:bg-red-200 disabled:opacity-50"
                  >
                    -
                  </button>
                  <button 
                    @click="adjustAdDuration(scheduledAd, 'increase')"
                    :disabled="!canIncreaseAd(scheduledAd)"
                    class="px-2 py-1 text-xs rounded bg-green-100 hover:bg-green-200 disabled:opacity-50"
                  >
                    +
                  </button>
                  <button 
                    @click="removeAdFromSchedule(index)"
                    class="px-2 py-1 text-xs rounded bg-red-500 text-white hover:bg-red-600"
                  >
                    Remove
                  </button>
                </div>
              </div>
            </div>
            
            <div v-if="scheduledAds.length === 0" class="text-center text-gray-500 py-4">
              No ads scheduled yet
            </div>
          </div>
        </div>
      </div>
      
      <div class="flex justify-end gap-2 mt-6">
        <button @click="$emit('close')" class="px-4 py-2 rounded-md text-gray-600 bg-gray-100 hover:bg-gray-200">
          Cancel
        </button>
        <button 
          @click="saveAds" 
          class="px-4 py-2 rounded-md bg-primary text-white"
        >
          Save
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { api } from '../services/api';

export default {
  name: 'AdManagerModal',
  props: {
    scheduleItem: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      availableAds: [],
      selectedAd: null,
      scheduledAds: [],
    };
  },
  computed: {
    availableTime() {
      const totalAdTime = this.scheduledAds.reduce((sum, ad) => sum + ad.duration, 0);
      return this.scheduleItem.duration - totalAdTime;
    }
  },
  async mounted() {
    await this.loadAvailableAds();
    this.loadScheduledAds();
  },
  methods: {
    async loadAvailableAds() {
      try {
        const response = await api.get('/ads');
        this.availableAds = response.data.success ? response.data.data : response.data;
      } catch (error) {
        console.error('Error loading ads:', error);
      }
    },
    loadScheduledAds() {
      // For now, we'll just initialize with an empty array
      // In a full implementation, we would load scheduled ads for this specific item
      this.scheduledAds = [];
    },
    selectAd(ad) {
      this.selectedAd = ad;
    },
    canAddAd(ad) {
      return this.availableTime >= ad.duration;
    },
    isAdAlreadyAdded(adId) {
      return this.scheduledAds.some(scheduled => scheduled.itemId === adId);
    },
    addAdToSchedule(ad) {
      if (!this.canAddAd(ad) || this.isAdAlreadyAdded(ad.id)) {
        return;
      }
      
      this.scheduledAds.push({
        id: `temp-${Date.now()}-${ad.id}`,
        itemId: ad.id,
        item: ad,
        duration: ad.duration // Start with full ad duration
      });
    },
    canIncreaseAd(scheduledAd) {
      return this.availableTime > 0 && 
             scheduledAd.duration < scheduledAd.item.duration;
    },
    adjustAdDuration(scheduledAd, action) {
      if (action === 'reduce' && scheduledAd.duration > 1) {
        scheduledAd.duration -= 1;
      } else if (action === 'increase' && 
                 this.availableTime > 0 && 
                 scheduledAd.duration < scheduledAd.item.duration) {
        scheduledAd.duration += 1;
      }
    },
    removeAdFromSchedule(index) {
      this.scheduledAds.splice(index, 1);
    },
    formatDuration(totalSeconds) {
      if (totalSeconds < 60) {
        return `${totalSeconds}s`;
      }
      const hours = Math.floor(totalSeconds / 3600);
      const minutes = Math.floor((totalSeconds % 3600) / 60);
      const seconds = totalSeconds % 60;
      
      if (hours > 0) {
        return `${hours}h ${minutes}m ${seconds}s`;
      } else if (seconds === 0) {
        return `${minutes}m`;
      } else {
        return `${minutes}m ${seconds}s`;
      }
    },
    async saveAds() {
      // In a full implementation, this would save to the backend
      // For now, just emit an event to indicate ads have been updated
      this.$emit('ads-updated', this.scheduledAds);
    }
  }
};
</script>