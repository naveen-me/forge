<template>
  <div class="bg-white rounded-lg shadow p-6" v-if="segment">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">Edit Segment</h2>
      <button 
        @click="$emit('close')"
        class="text-gray-500 hover:text-gray-700"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
        </svg>
      </button>
    </div>
    
    <form @submit.prevent="saveSegment" class="space-y-6">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Segment Name</label>
        <input
          v-model="localSegment.name"
          type="text"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          required
        />
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Start Time (seconds)</label>
          <input
            v-model.number="localSegment.startTimeOffset"
            type="number"
            min="0"
            step="0.1"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Duration (seconds)</label>
          <input
            v-model.number="localSegment.duration"
            type="number"
            min="0.1"
            step="0.1"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>
      </div>
      
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Media File</label>
        <div class="flex space-x-2">
          <select
            v-model="localSegment.media.path"
            class="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Select a media file...</option>
            <option v-for="media in mediaOptions" :key="media.path" :value="media.path">
              {{ media.name }}
            </option>
          </select>
          <button
            type="button"
            @click="browseMedia"
            class="px-4 py-2 bg-gray-200 rounded-md hover:bg-gray-300 transition-colors"
          >
            Browse
          </button>
        </div>
      </div>
      
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Loop Media</label>
        <div class="flex items-center">
          <input
            v-model="localSegment.media.loop"
            type="checkbox"
            class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
          />
          <span class="ml-2 text-sm text-gray-600">Repeat this media file</span>
        </div>
      </div>
      
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Ad Insertion</label>
        <div class="flex items-center mb-3">
          <input
            v-model="localSegment.ads.enabled"
            type="checkbox"
            class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
          />
          <span class="ml-2 text-sm text-gray-600">Enable ad insertion for this segment</span>
        </div>
        
        <div v-if="localSegment.ads.enabled" class="pl-6 space-y-3">
          <div>
            <label class="block text-xs text-gray-500 mb-1">Max Ad Duration (seconds)</label>
            <input
              v-model.number="localSegment.ads.maxDuration"
              type="number"
              min="0"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Ad Insertion Points</label>
            <div class="space-y-2">
              <div 
                v-for="(point, index) in localSegment.ads.insertionPoints" 
                :key="index"
                class="flex items-center space-x-2 p-2 bg-gray-50 rounded"
              >
                <input
                  v-model.number="point.offset"
                  type="number"
                  min="0"
                  placeholder="Offset (s)"
                  class="w-24 px-2 py-1 border border-gray-300 rounded text-sm"
                />
                <input
                  v-model.number="point.duration"
                  type="number"
                  min="0"
                  placeholder="Duration (s)"
                  class="w-24 px-2 py-1 border border-gray-300 rounded text-sm"
                />
                <select
                  v-model="point.groupId"
                  class="flex-1 px-2 py-1 border border-gray-300 rounded text-sm"
                >
                  <option value="">Select ad group</option>
                  <option v-for="group in adGroups" :key="group.id" :value="group.id">
                    {{ group.name }}
                  </option>
                </select>
                <button
                  @click="removeAdPoint(index)"
                  class="text-red-500 hover:text-red-700"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                  </svg>
                </button>
              </div>
              <button
                @click="addAdPoint"
                class="text-sm text-blue-600 hover:text-blue-800 flex items-center"
              >
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
                </svg>
                Add Ad Point
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Overlays</label>
        <div class="space-y-2">
          <div 
            v-for="(overlay, index) in localSegment.overlays" 
            :key="overlay.id"
            class="flex items-center justify-between p-2 bg-blue-50 rounded"
          >
            <span class="text-sm">{{ overlay.name }}</span>
            <button
              @click="removeOverlay(index)"
              class="text-red-500 hover:text-red-700"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
              </svg>
            </button>
          </div>
          <button
            @click="addOverlay"
            class="text-sm text-blue-600 hover:text-blue-800 flex items-center"
          >
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
            </svg>
            Add Overlay
          </button>
        </div>
      </div>
      
      <div class="flex justify-end space-x-3 pt-4">
        <button
          type="button"
          @click="$emit('close')"
          class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
        >
          Cancel
        </button>
        <button
          type="submit"
          class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          Save Segment
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, watch } from 'vue'

export default {
  name: 'SegmentEditor',
  props: {
    segment: {
      type: Object,
      default: null
    }
  },
  
  emits: ['close', 'save'],
  
  setup(props, { emit }) {
    const localSegment = ref({
      id: '',
      name: '',
      startTimeOffset: 0,
      duration: 30,
      media: { path: '', loop: false },
      overlays: [],
      ads: { 
        enabled: false, 
        maxDuration: 0, 
        insertionPoints: [] 
      }
    })
    
    // Watch for changes in the props.segment to update our local copy
    watch(
      () => props.segment,
      (newVal) => {
        if (newVal) {
          // Deep clone the segment to avoid direct modification
          localSegment.value = JSON.parse(JSON.stringify(newVal))
        }
      },
      { immediate: true }
    )
    
    const mediaOptions = ref([
      { name: 'News Intro', path: '/media/news_intro.mp4' },
      { name: 'Weather Report', path: '/media/weather.mp4' },
      { name: 'Sports Highlights', path: '/media/sports.mp4' },
      { name: 'Commercial 1', path: '/media/commercial_1.mp4' }
    ])
    
    const adGroups = ref([
      { id: 'pre_roll', name: 'Pre-Roll Commercials' },
      { id: 'mid_roll', name: 'Mid-Roll Commercials' },
      { id: 'post_roll', name: 'Post-Roll Commercials' }
    ])
    
    const saveSegment = () => {
      emit('save', localSegment.value)
    }
    
    const browseMedia = () => {
      // In a real app, this would open a file dialog
      console.log('Browse media files')
    }
    
    const addAdPoint = () => {
      localSegment.value.ads.insertionPoints.push({
        offset: 0,
        duration: 30,
        groupId: ''
      })
    }
    
    const removeAdPoint = (index) => {
      localSegment.value.ads.insertionPoints.splice(index, 1)
    }
    
    const addOverlay = () => {
      // In a real app, this would open overlay selection modal
      console.log('Add overlay')
    }
    
    const removeOverlay = (index) => {
      localSegment.value.overlays.splice(index, 1)
    }
    
    return {
      localSegment,
      mediaOptions,
      adGroups,
      saveSegment,
      browseMedia,
      addAdPoint,
      removeAdPoint,
      addOverlay,
      removeOverlay
    }
  }
}
</script>