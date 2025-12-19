<template>
  <div class="border rounded-lg p-4 bg-gray-50">
    <div class="flex justify-between items-center mb-4">
      <h3 class="font-medium">Timeline</h3>
      <div class="text-sm text-gray-500">
        Duration: {{ formatTime(totalDuration) }} | Segments: {{ segments.length }}
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
    
    <!-- Timeline grid -->
    <div class="timeline-container relative" :style="{ height: timelineHeight + 'px' }">
      <div 
        v-for="segment in segmentsWithPositions" 
        :key="segment.id"
        class="timeline-segment rounded"
        :style="{
          left: segment.left + 'px',
          width: segment.width + 'px',
          backgroundColor: segmentColor(segment)
        }"
        @click="$emit('segment-clicked', segment)"
      >
        <div class="text-center w-full truncate px-1">
          <div class="font-medium text-xs">{{ segment.name }}</div>
          <div class="text-xs opacity-80">{{ formatTime(segment.startTimeOffset) }}</div>
        </div>
      </div>
    </div>
    
    <!-- Controls -->
    <div class="flex justify-between items-center mt-4">
      <div class="flex space-x-2">
        <button 
          @click="$emit('add-segment')"
          class="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 transition-colors flex items-center"
        >
          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
          </svg>
          Add Segment
        </button>
        <button 
          @click="$emit('reorder-segments')"
          class="px-3 py-1 bg-gray-200 rounded text-sm hover:bg-gray-300 transition-colors"
          :disabled="segments.length < 2"
        >
          Reorder
        </button>
      </div>
      
      <div class="text-sm text-gray-500">
        Scale: 10px per sec
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'ScheduleTimeline',
  props: {
    segments: {
      type: Array,
      required: true
    },
    durationScale: {
      type: Number,
      default: 10 // pixels per second
    }
  },
  
  emits: ['segment-clicked', 'add-segment', 'reorder-segments'],
  
  setup(props) {
    // Calculate total timeline duration
    const totalDuration = computed(() => {
      if (!props.segments || props.segments.length === 0) return 100 // minimum 100 seconds view
      
      const lastSegment = props.segments.reduce((prev, current) => 
        (prev.startTimeOffset + prev.duration > current.startTimeOffset + current.duration) ? prev : current
      )
      
      return Math.max(100, lastSegment.startTimeOffset + lastSegment.duration)
    })
    
    // Calculate timeline height based on number of rows needed
    const timelineHeight = computed(() => {
      // For simplicity, we'll use a single row but in a real app, 
      // this would handle overlapping segments in multiple rows
      return 80
    })
    
    // Generate timeline ticks (every 10 seconds)
    const timelineTicks = computed(() => {
      const ticks = []
      for (let i = 0; i <= totalDuration.value; i += 10) {
        ticks.push(i)
      }
      return ticks
    })
    
    // Calculate positions for each segment
    const segmentsWithPositions = computed(() => {
      return props.segments.map(segment => {
        const left = segment.startTimeOffset * props.durationScale
        const width = segment.duration * props.durationScale
        return {
          ...segment,
          left,
          width
        }
      })
    })
    
    // Format time as MM:SS
    const formatTime = (seconds) => {
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    }
    
    // Get color for segment type
    const segmentColor = (segment) => {
      // Different colors based on segment type or content
      if (segment.name.toLowerCase().includes('ad') || segment.name.toLowerCase().includes('commercial')) {
        return '#F59E0B' // Amber for ads
      } else if (segment.name.toLowerCase().includes('intro') || segment.name.toLowerCase().includes('opening')) {
        return '#3B82F6' // Blue for intros
      } else if (segment.name.toLowerCase().includes('outro') || segment.name.toLowerCase().includes('closing')) {
        return '#8B5CF6' // Purple for outros
      }
      return '#10B981' // Emerald for regular content
    }
    
    return {
      totalDuration,
      timelineHeight,
      timelineTicks,
      segmentsWithPositions,
      formatTime,
      segmentColor
    }
  }
}
</script>