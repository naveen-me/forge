<template>
  <div class="border rounded-lg p-4 bg-gray-50">
    <div class="flex justify-between items-center mb-4">
      <h3 class="font-medium">Timeline V2</h3>
      <div class="text-sm text-gray-500">
        Duration: {{ formatTime(totalDuration) }} | Tracks: {{ tracks.length }}
      </div>
    </div>
    
    <!-- Timeline ruler -->
    <div class="timeline-ruler mb-2">
      <div 
        v-for="tick in timelineTicks" 
        :key="tick" 
        class="timeline-ruler-tick"
      >
        {{ formatTime(tick) }}
      </div>
    </div>
    
    <!-- Tracks container -->
    <div class="space-y-3 mb-4">
      <div 
        v-for="(track, trackIndex) in tracksWithPositions" 
        :key="track.id"
        class="track-container bg-white p-3 rounded border"
      >
        <div class="flex items-center mb-2">
          <h4 class="font-medium text-sm">{{ track.name }} ({{ track.type }})</h4>
          <span class="ml-2 text-xs px-2 py-1 bg-gray-200 rounded">
            Z: {{ track.zIndex }}
          </span>
        </div>
        
        <!-- Timeline items for this track -->
        <div class="relative h-12 bg-gray-100 rounded">
          <div 
            v-for="item in track.itemsWithPositions"
            :key="item.id"
            class="absolute h-10 rounded flex items-center justify-center text-xs cursor-pointer"
            :style="{
              left: item.left + 'px',
              width: item.width + 'px',
              backgroundColor: itemColor(item.type)
            }"
            @click="selectItem(item)"
          >
            <div class="truncate w-full text-center px-1">
              {{ item.name }}
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Selected item details -->
    <div v-if="selectedItem" class="bg-blue-50 p-4 rounded border">
      <h4 class="font-medium mb-2">Selected: {{ selectedItem.name }}</h4>
      
      <div class="grid grid-cols-2 gap-4 text-sm">
        <div>
          <label class="block text-gray-700">Type</label>
          <div>{{ selectedItem.type }}</div>
        </div>
        
        <div>
          <label class="block text-gray-700">Start Time</label>
          <div>{{ formatTime(selectedItem.timeline.start) }}s</div>
        </div>
        
        <div>
          <label class="block text-gray-700">End Time</label>
          <div>{{ formatTime(selectedItem.timeline.end) }}s</div>
        </div>
        
        <div>
          <label class="block text-gray-700">Duration</label>
          <div>{{ formatTime(selectedItem.timeline.end - selectedItem.timeline.start) }}s</div>
        </div>
        
        <!-- Transform properties for program and overlay items -->
        <div v-if="selectedItem.transform">
          <label class="block text-gray-700">X Position</label>
          <input 
            v-model.number="selectedItem.transform.x"
            type="number"
            class="w-full p-1 border rounded text-xs"
            @change="updateItemTransform"
          />
        </div>
        
        <div v-if="selectedItem.transform">
          <label class="block text-gray-700">Y Position</label>
          <input 
            v-model.number="selectedItem.transform.y"
            type="number"
            class="w-full p-1 border rounded text-xs"
            @change="updateItemTransform"
          />
        </div>
        
        <!-- Ad-specific properties -->
        <div v-if="selectedItem.type === 'ad'">
          <label class="block text-gray-700">Resume After</label>
          <input 
            v-model="selectedItem.resumeMainMedia"
            type="checkbox"
            @change="updateItemProperty"
          />
        </div>
        
        <!-- Overlay-specific properties -->
        <div v-if="selectedItem.type === 'overlay'">
          <label class="block text-gray-700">Scope</label>
          <select
            v-model="selectedItem.scope"
            class="w-full p-1 border rounded text-xs"
            @change="updateItemProperty"
          >
            <option value="GLOBAL">Global</option>
            <option value="PROGRAM">Program-Specific</option>
          </select>
        </div>
      </div>
    </div>
    
    <!-- Controls -->
    <div class="flex justify-between items-center mt-4">
      <div class="flex space-x-2">
        <button 
          @click="$emit('add-item')"
          class="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 transition-colors flex items-center"
        >
          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
          </svg>
          Add Item
        </button>
        <button 
          @click="addTrack"
          class="px-3 py-1 bg-green-600 text-white rounded text-sm hover:bg-green-700 transition-colors"
        >
          Add Track
        </button>
      </div>
      
      <div class="text-sm text-gray-500">
        Scale: 10px per sec
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue';

export default {
  name: 'TimelineV2',
  props: {
    tracks: {
      type: Array,
      required: true
    },
    durationScale: {
      type: Number,
      default: 10 // pixels per second
    }
  },
  
  emits: ['item-selected', 'item-updated', 'add-item', 'track-added'],
  
  setup(props, { emit }) {
    const selectedItem = ref(null);
    
    // Calculate total timeline duration
    const totalDuration = computed(() => {
      if (!props.tracks || props.tracks.length === 0) return 100; // minimum 100 seconds view
      
      let maxEnd = 100;
      for (const track of props.tracks) {
        for (const item of track.items) {
          maxEnd = Math.max(maxEnd, item.timeline.end);
        }
      }
      return Math.max(100, maxEnd);
    });
    
    // Generate timeline ticks (every 10 seconds)
    const timelineTicks = computed(() => {
      const ticks = [];
      for (let i = 0; i <= totalDuration.value; i += 10) {
        ticks.push(i);
      }
      return ticks;
    });
    
    // Calculate positions for each item in each track
    const tracksWithPositions = computed(() => {
      return props.tracks.map(track => {
        const itemsWithPositions = track.items.map(item => {
          const left = item.timeline.start * props.durationScale;
          const width = (item.timeline.end - item.timeline.start) * props.durationScale;
          return {
            ...item,
            left,
            width
          };
        });
        
        return {
          ...track,
          itemsWithPositions
        };
      });
    });
    
    // Format time as MM:SS
    const formatTime = (seconds) => {
      if (isNaN(seconds)) return '00:00';
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };
    
    // Get color for item type
    const itemColor = (type) => {
      switch (type) {
        case 'program':
          return '#3B82F6'; // Blue for programs
        case 'ad':
          return '#F59E0B'; // Amber for ads
        case 'overlay':
          return '#8B5CF6'; // Purple for overlays
        case 'live':
          return '#10B981'; // Emerald for live
        default:
          return '#9CA3AF'; // Gray for others
      }
    };
    
    const selectItem = (item) => {
      selectedItem.value = { ...item }; // Create a copy to avoid direct mutation
      emit('item-selected', selectedItem.value);
    };
    
    const updateItemTransform = () => {
      emit('item-updated', selectedItem.value);
    };
    
    const updateItemProperty = () => {
      emit('item-updated', selectedItem.value);
    };
    
    const addTrack = () => {
      emit('track-added');
    };
    
    // Watch for changes in selected item
    watch(selectedItem, (newVal) => {
      if (newVal) {
        emit('item-updated', newVal);
      }
    });
    
    return {
      selectedItem,
      totalDuration,
      timelineTicks,
      tracksWithPositions,
      formatTime,
      itemColor,
      selectItem,
      updateItemTransform,
      updateItemProperty,
      addTrack
    };
  }
};
</script>

<style scoped>
.timeline-ruler {
  display: flex;
  height: 20px;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 0.375rem;
  overflow: hidden;
}

.timeline-ruler-tick {
  position: relative;
  min-width: 50px;
  border-right: 1px solid #cbd5e1;
  text-align: center;
  font-size: 0.65rem;
  color: #64748b;
  padding-top: 2px;
}

.track-container {
  transition: all 0.2s ease;
}

.track-container:hover {
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
}
</style>