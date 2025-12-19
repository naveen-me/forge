<template>
  <div id="app" class="h-screen flex flex-col bg-gray-100">
    <!-- Top Navigation Bar -->
    <nav class="bg-gray-800 text-white p-4 shadow-md">
      <div class="container mx-auto flex justify-between items-center">
        <div class="flex items-center space-x-2">
          <div class="bg-blue-500 w-8 h-8 rounded-full flex items-center justify-center">
            <span class="font-bold">BP</span>
          </div>
          <h1 class="text-xl font-bold">Broadcast Playout Editor</h1>
        </div>

        <div class="flex items-center space-x-4">
          <div class="flex items-center space-x-2">
            <div class="w-3 h-3 rounded-full bg-green-500"></div>
            <span class="text-sm">Engine: {{ engineStatus.connected ? 'Connected' : 'Disconnected' }}</span>
          </div>

          <button
            @click="toggleEngineConnection"
            :class="engineStatus.connected ? 'bg-red-600 hover:bg-red-700' : 'bg-green-600 hover:bg-green-700'"
            class="px-4 py-2 rounded text-sm font-medium transition-colors"
          >
            {{ engineStatus.connected ? 'Disconnect' : 'Connect Engine' }}
          </button>
        </div>
      </div>
    </nav>

    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col md:flex-row overflow-hidden">
      <!-- Sidebar -->
      <div class="w-full md:w-64 bg-white shadow-md p-4 flex flex-col">
        <div class="mb-6">
          <h2 class="text-lg font-semibold mb-3">Library</h2>
          <ul class="space-y-1">
            <li>
              <button
                @click="activeTab = 'schedule'"
                :class="{'bg-blue-100 text-blue-700': activeTab === 'schedule'}"
                class="w-full text-left px-3 py-2 rounded hover:bg-gray-100 transition-colors"
              >
                Schedule Editor
              </button>
            </li>
            <li>
              <button
                @click="activeTab = 'timeline'"
                :class="{'bg-blue-100 text-blue-700': activeTab === 'timeline'}"
                class="w-full text-left px-3 py-2 rounded hover:bg-gray-100 transition-colors"
              >
                Timeline V2
              </button>
            </li>
            <li>
              <button
                @click="activeTab = 'media'"
                :class="{'bg-blue-100 text-blue-700': activeTab === 'media'}"
                class="w-full text-left px-3 py-2 rounded hover:bg-gray-100 transition-colors"
              >
                Media Library
              </button>
            </li>
            <li>
              <button
                @click="activeTab = 'content'"
                :class="{'bg-blue-100 text-blue-700': activeTab === 'content'}"
                class="w-full text-left px-3 py-2 rounded hover:bg-gray-100 transition-colors"
              >
                Content Manager
              </button>
            </li>
          </ul>
        </div>

        <div class="mt-auto">
          <div class="text-xs text-gray-500 p-3 bg-gray-50 rounded">
            <p>v2.0.0</p>
            <p>Mode: {{ isEditMode ? 'Edit' : 'Play' }}</p>
            <button
              @click="toggleMode"
              :class="isEditMode ? 'text-green-600' : 'text-red-600'"
              class="mt-2 text-sm font-medium"
            >
              {{ isEditMode ? 'Switch to Play Mode' : 'Switch to Edit Mode' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="flex-1 p-6 overflow-auto">
        <!-- V1 Schedule Editor -->
        <div v-if="activeTab === 'schedule'" class="space-y-6">
          <div class="bg-white rounded-lg shadow p-6">
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-2xl font-bold text-gray-800">Schedule Editor (V1)</h2>
              <div class="flex space-x-2">
                <button
                  @click="loadSchedule"
                  class="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 transition-colors"
                >
                  Load Schedule
                </button>
                <button
                  @click="saveSchedule"
                  class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
                >
                  Save Schedule
                </button>
              </div>
            </div>

            <!-- Schedule Timeline -->
            <div class="border rounded-lg p-4 bg-gray-50">
              <div class="flex items-center mb-4">
                <h3 class="font-medium">Timeline</h3>
                <span class="ml-2 text-sm text-gray-500">(Drag segments to reorder)</span>
              </div>

              <div class="space-y-3">
                <div
                  v-for="(segment, index) in currentSchedule.segments"
                  :key="segment.id"
                  class="p-3 bg-white border rounded flex justify-between items-center"
                >
                  <div>
                    <h4 class="font-medium">{{ segment.name }}</h4>
                    <p class="text-sm text-gray-500">
                      {{ segment.startTimeOffset }}s - {{ segment.startTimeOffset + segment.duration }}s
                      ({{ segment.duration }}s)
                    </p>
                  </div>
                  <div class="flex space-x-2">
                    <button
                      @click="editSegment(segment)"
                      class="px-3 py-1 bg-blue-100 text-blue-700 rounded text-sm hover:bg-blue-200 transition-colors"
                    >
                      Edit
                    </button>
                    <button
                      @click="removeSegment(index)"
                      class="px-3 py-1 bg-red-100 text-red-700 rounded text-sm hover:bg-red-200 transition-colors"
                    >
                      Remove
                    </button>
                  </div>
                </div>

                <button
                  @click="addNewSegment"
                  class="w-full py-3 border-2 border-dashed border-gray-300 rounded flex items-center justify-center text-gray-500 hover:border-blue-400 hover:text-blue-500 transition-colors"
                >
                  <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
                  </svg>
                  Add New Segment
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- V2 Timeline Editor -->
        <div v-if="activeTab === 'timeline'" class="space-y-6">
          <div class="bg-white rounded-lg shadow p-6">
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-2xl font-bold text-gray-800">Timeline Editor (V2)</h2>
              <div class="flex space-x-2">
                <button
                  @click="loadTimeline"
                  class="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 transition-colors"
                >
                  Load Timeline
                </button>
                <button
                  @click="saveTimeline"
                  class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
                >
                  Save Timeline
                </button>
              </div>
            </div>

            <!-- V2 Timeline Component -->
            <TimelineV2
              :tracks="timelineV2.tracks"
              @item-selected="onItemSelected"
              @item-updated="onItemUpdated"
              @track-added="addTrack"
            />
          </div>
        </div>

        <!-- Media Library -->
        <div v-if="activeTab === 'media'" class="space-y-6">
          <div class="bg-white rounded-lg shadow p-6">
            <h2 class="text-2xl font-bold text-gray-800 mb-6">Media Library</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div
                v-for="item in mediaLibrary"
                :key="item.id"
                class="border rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
                @click="selectMedia(item)"
              >
                <div class="bg-gray-200 border-2 border-dashed rounded-xl w-full h-32 flex items-center justify-center">
                  <span class="text-gray-500">Media Preview</span>
                </div>
                <h3 class="font-medium mt-2">{{ item.name }}</h3>
                <p class="text-sm text-gray-500 truncate">{{ item.path }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Content Manager (All Four Content Types) -->
        <div v-if="activeTab === 'content'" class="space-y-6">
          <div class="bg-white rounded-lg shadow p-6">
            <h2 class="text-2xl font-bold text-gray-800 mb-6">Content Manager</h2>

            <!-- Canvas Settings -->
            <div class="mb-6 p-4 bg-gray-50 rounded">
              <h3 class="font-medium mb-3">Canvas Settings</h3>
              <div class="grid grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm text-gray-700">Width</label>
                  <input
                    v-model.number="timelineV2.canvas.width"
                    type="number"
                    class="w-full p-2 border rounded"
                  />
                </div>
                <div>
                  <label class="block text-sm text-gray-700">Height</label>
                  <input
                    v-model.number="timelineV2.canvas.height"
                    type="number"
                    class="w-full p-2 border rounded"
                  />
                </div>
                <div>
                  <label class="block text-sm text-gray-700">FPS</label>
                  <input
                    v-model.number="timelineV2.canvas.fps"
                    type="number"
                    class="w-full p-2 border rounded"
                  />
                </div>
              </div>
            </div>

            <!-- Four Content Types Tabs -->
            <div class="border-b border-gray-200 mb-4">
              <nav class="-mb-px flex space-x-8">
                <button
                  @click="contentTab = 'program'"
                  :class="{'border-blue-500 text-blue-600': contentTab === 'program', 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300': contentTab !== 'program'}"
                  class="whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm"
                >
                  Programs
                </button>
                <button
                  @click="contentTab = 'ad'"
                  :class="{'border-blue-500 text-blue-600': contentTab === 'ad', 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300': contentTab !== 'ad'}"
                  class="whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm"
                >
                  Ads
                </button>
                <button
                  @click="contentTab = 'overlay'"
                  :class="{'border-blue-500 text-blue-600': contentTab === 'overlay', 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300': contentTab !== 'overlay'}"
                  class="whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm"
                >
                  Overlays
                </button>
                <button
                  @click="contentTab = 'live'"
                  :class="{'border-blue-500 text-blue-600': contentTab === 'live', 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300': contentTab !== 'live'}"
                  class="whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm"
                >
                  Live Sources
                </button>
              </nav>
            </div>

            <!-- Program Assets -->
            <div v-if="contentTab === 'program'" class="space-y-4">
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-medium">Program Assets</h3>
                <button @click="addProgram" class="px-4 py-2 bg-blue-600 text-white rounded">Add Program</button>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div
                  v-for="item in timelineV2.assets.media"
                  :key="item.id"
                  class="border rounded-lg p-4"
                >
                  <h4 class="font-medium">{{ item.name }}</h4>
                  <p class="text-sm text-gray-500 truncate">{{ item.path }}</p>
                  <p class="text-xs text-gray-400">Duration: {{ item.duration }}s</p>

                  <div class="mt-2 flex space-x-2">
                    <button @click="editProgram(item)" class="text-blue-600 text-sm">Edit</button>
                    <button @click="removeProgram(item.id)" class="text-red-600 text-sm">Delete</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Ad Assets -->
            <div v-if="contentTab === 'ad'" class="space-y-4">
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-medium">Ad Assets</h3>
                <button @click="addAd" class="px-4 py-2 bg-blue-600 text-white rounded">Add Ad</button>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div
                  v-for="item in timelineV2.assets.ads"
                  :key="item.id"
                  class="border rounded-lg p-4"
                >
                  <h4 class="font-medium">{{ item.name }}</h4>
                  <p class="text-sm text-gray-500 truncate">{{ item.path }}</p>
                  <p class="text-xs text-gray-400">Duration: {{ item.duration }}s</p>

                  <div class="mt-2 flex space-x-2">
                    <button @click="editAd(item)" class="text-blue-600 text-sm">Edit</button>
                    <button @click="removeAd(item.id)" class="text-red-600 text-sm">Delete</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Overlay Assets -->
            <div v-if="contentTab === 'overlay'" class="space-y-4">
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-medium">Overlay Assets</h3>
                <button @click="addOverlay" class="px-4 py-2 bg-blue-600 text-white rounded">Add Overlay</button>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div
                  v-for="item in timelineV2.assets.overlays"
                  :key="item.id"
                  class="border rounded-lg p-4"
                >
                  <h4 class="font-medium">{{ item.name }}</h4>
                  <p class="text-sm text-gray-500 truncate">{{ item.path }}</p>
                  <p class="text-xs text-gray-400">Type: {{ item.overlayType }}</p>

                  <div class="mt-2 flex space-x-2">
                    <button @click="editOverlay(item)" class="text-blue-600 text-sm">Edit</button>
                    <button @click="removeOverlay(item.id)" class="text-red-600 text-sm">Delete</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Live Assets -->
            <div v-if="contentTab === 'live'" class="space-y-4">
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-medium">Live Sources</h3>
                <button @click="addLive" class="px-4 py-2 bg-blue-600 text-white rounded">Add Live Source</button>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div
                  v-for="item in timelineV2.assets.live"
                  :key="item.id"
                  class="border rounded-lg p-4"
                >
                  <h4 class="font-medium">{{ item.name }}</h4>
                  <p class="text-sm text-gray-500 truncate">{{ item.url }}</p>
                  <p class="text-xs text-gray-400">Type: {{ item.sourceType }}</p>

                  <div class="mt-2 flex space-x-2">
                    <button @click="editLive(item)" class="text-blue-600 text-sm">Edit</button>
                    <button @click="removeLive(item.id)" class="text-red-600 text-sm">Delete</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, reactive } from 'vue'
import TimelineV2 from './components/TimelineV2.vue'

export default {
  name: 'App',
  components: {
    TimelineV2
  },
  setup() {
    const activeTab = ref('schedule')
    const contentTab = ref('program')
    const isEditMode = ref(true)
    const engineStatus = ref({ connected: false })

    const currentSchedule = reactive({
      id: 'schedule_1',
      name: 'Default Schedule',
      segments: [
        {
          id: 'seg_1',
          name: 'Opening Sequence',
          startTimeOffset: 0,
          duration: 30,
          media: { path: '/media/opening.mp4' },
          overlays: []
        },
        {
          id: 'seg_2',
          name: 'Main Content',
          startTimeOffset: 30,
          duration: 120,
          media: { path: '/media/content.mp4' },
          overlays: []
        }
      ]
    })

    // V2 Timeline structure
    const timelineV2 = reactive({
      schemaVersion: '2.0',
      canvas: {
        width: 1920,
        height: 1080,
        fps: 30
      },
      timezone: 'UTC',
      schedule: {
        id: 'schedule_v2',
        startTime: new Date(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000) // 2 hours from now
      },
      assets: {
        media: [
          {
            id: 'media_1',
            name: 'Morning News',
            path: '/media/morning_news.mp4',
            duration: 600,
            sourceType: 'MEDIA_SOURCE',
            obsProperties: {
              loop: false,
              restartOnActivate: true
            },
            filters: null
          }
        ],
        ads: [
          {
            id: 'ad_1',
            name: 'Product Ad',
            path: '/media/product_ad.mp4',
            duration: 30,
            sourceType: 'MEDIA_SOURCE',
            filters: null
          }
        ],
        overlays: [
          {
            id: 'overlay_1',
            name: 'Channel Logo',
            overlayType: 'IMAGE',
            path: '/assets/logo.png',
            defaultTransform: {
              x: 20,
              y: 20,
              width: 150,
              height: 80,
              rotation: 0,
              opacity: 1,
              zIndex: 100
            },
            filters: null
          }
        ],
        live: [
          {
            id: 'live_1',
            name: 'Weather Feed',
            url: 'https://weather.example.com/feed',
            sourceType: 'BROWSER_SOURCE',
            browserSettings: {
              width: 1920,
              height: 1080,
              fps: 30,
              shutdownWhenHidden: true
            }
          }
        ]
      },
      timeline: {
        tracks: [
          {
            id: 'track_program',
            type: 'PROGRAM',
            zIndex: 1,
            name: 'Program Content',
            items: [
              {
                id: 'prog_item_1',
                assetRef: 'media_1',
                name: 'Morning News',
                type: 'program',
                timeline: {
                  start: 0,
                  end: 600
                },
                playback: {
                  offsetStart: 0
                },
                transform: {
                  x: 0,
                  y: 0,
                  width: 1920,
                  height: 1080,
                  rotation: 0,
                  opacity: 1,
                  zIndex: 1
                }
              }
            ]
          },
          {
            id: 'track_ads',
            type: 'ADS',
            zIndex: 2,
            name: 'Ads',
            items: [
              {
                id: 'ad_item_1',
                assetRef: 'ad_1',
                name: 'Product Ad',
                type: 'ad',
                insertAt: 300,
                resumeMainMedia: true
              }
            ]
          },
          {
            id: 'track_overlays',
            type: 'OVERLAY',
            zIndex: 3,
            name: 'Overlays',
            items: [
              {
                id: 'overlay_item_1',
                assetRef: 'overlay_1',
                name: 'Channel Logo',
                type: 'overlay',
                scope: 'GLOBAL',
                timeline: {
                  start: 0,
                  end: 7200
                },
                transformOverride: null
              }
            ]
          }
        ]
      },
      engineRules: {
        resumeAfterAd: true,
        onObsDisconnect: 'RETRY',
        onError: 'STOP'
      }
    })

    const mediaLibrary = ref([
      { id: 'm1', name: 'News Intro', path: '/media/news_intro.mp4' },
      { id: 'm2', name: 'Weather Report', path: '/media/weather.mp4' },
      { id: 'm3', name: 'Sports Highlights', path: '/media/sports.mp4' }
    ])

    const availableOverlays = ref([
      { id: 'o1', name: 'Station Logo', type: 'image', source: '/assets/logo.png' },
      { id: 'o2', name: 'Clock', type: 'text', source: 'clock' },
      { id: 'o3', name: 'Breaking News', type: 'text', source: 'breaking_news' }
    ])

    const currentOverlays = ref([])

    // Content management functions
    const addProgram = () => {
      const newId = `media_${Date.now()}`
      timelineV2.assets.media.push({
        id: newId,
        name: `New Program ${timelineV2.assets.media.length + 1}`,
        path: '',
        duration: 30,
        sourceType: 'MEDIA_SOURCE',
        obsProperties: {
          loop: false,
          restartOnActivate: true
        },
        filters: null
      })
    }

    const editProgram = (item) => {
      console.log('Editing program:', item)
      // Would open a modal to edit the program
    }

    const removeProgram = (id) => {
      timelineV2.assets.media = timelineV2.assets.media.filter(item => item.id !== id)
    }

    const addAd = () => {
      const newId = `ad_${Date.now()}`
      timelineV2.assets.ads.push({
        id: newId,
        name: `New Ad ${timelineV2.assets.ads.length + 1}`,
        path: '',
        duration: 15,
        sourceType: 'MEDIA_SOURCE',
        filters: null
      })
    }

    const editAd = (item) => {
      console.log('Editing ad:', item)
    }

    const removeAd = (id) => {
      timelineV2.assets.ads = timelineV2.assets.ads.filter(item => item.id !== id)
    }

    const addOverlay = () => {
      const newId = `overlay_${Date.now()}`
      timelineV2.assets.overlays.push({
        id: newId,
        name: `New Overlay ${timelineV2.assets.overlays.length + 1}`,
        overlayType: 'IMAGE',
        path: '',
        defaultTransform: {
          x: 0,
          y: 0,
          width: 100,
          height: 100,
          rotation: 0,
          opacity: 1,
          zIndex: 1
        },
        filters: null
      })
    }

    const editOverlay = (item) => {
      console.log('Editing overlay:', item)
    }

    const removeOverlay = (id) => {
      timelineV2.assets.overlays = timelineV2.assets.overlays.filter(item => item.id !== id)
    }

    const addLive = () => {
      const newId = `live_${Date.now()}`
      timelineV2.assets.live.push({
        id: newId,
        name: `New Live Source ${timelineV2.assets.live.length + 1}`,
        url: '',
        sourceType: 'BROWSER_SOURCE',
        browserSettings: {
          width: 1920,
          height: 1080,
          fps: 30,
          shutdownWhenHidden: true
        }
      })
    }

    const editLive = (item) => {
      console.log('Editing live source:', item)
    }

    const removeLive = (id) => {
      timelineV2.assets.live = timelineV2.assets.live.filter(item => item.id !== id)
    }

    const addTrack = () => {
      const newId = `track_${Date.now()}`
      const types = ['PROGRAM', 'LIVE', 'ADS', 'OVERLAY']
      const type = types[timelineV2.timeline.tracks.length % types.length]

      timelineV2.timeline.tracks.push({
        id: newId,
        type: type,
        zIndex: timelineV2.timeline.tracks.length + 1,
        name: `${type} Track`,
        items: []
      })
    }

    const onItemSelected = (item) => {
      console.log('Selected item:', item)
    }

    const onItemUpdated = (item) => {
      console.log('Updated item:', item)
    }

    const loadTimeline = async () => {
      console.log('Loading V2 timeline...')
    }

    const saveTimeline = async () => {
      console.log('Saving V2 timeline:', timelineV2)
    }

    const loadSchedule = async () => {
      // In a real app, this would call the electron API
      console.log('Loading schedule...')
    }

    const saveSchedule = async () => {
      // In a real app, this would call the electron API
      console.log('Saving schedule...', currentSchedule)
    }

    const toggleEngineConnection = async () => {
      if (engineStatus.value.connected) {
        // Disconnect from engine
        await window.electronAPI.disconnectEngine()
        engineStatus.value.connected = false
      } else {
        // Connect to engine
        const result = await window.electronAPI.connectEngine()
        if (result.success) {
          engineStatus.value.connected = true
        }
      }
    }

    const toggleMode = () => {
      isEditMode.value = !isEditMode.value
      // In a real app, this would notify the engine of mode change
    }

    const addNewSegment = () => {
      const newId = `seg_${Date.now()}`
      currentSchedule.segments.push({
        id: newId,
        name: `New Segment ${currentSchedule.segments.length + 1}`,
        startTimeOffset: currentSchedule.segments.length > 0
          ? currentSchedule.segments[currentSchedule.segments.length - 1].startTimeOffset +
            currentSchedule.segments[currentSchedule.segments.length - 1].duration
          : 0,
        duration: 30,
        media: { path: '' },
        overlays: []
      })
    }

    const removeSegment = (index) => {
      currentSchedule.segments.splice(index, 1)
    }

    const editSegment = (segment) => {
      console.log('Editing segment:', segment)
      // In a real app, this would open an edit dialog
    }

    const selectMedia = (media) => {
      console.log('Selected media:', media)
      // In a real app, this would associate the media with the current segment
    }

    const addOverlayToSegment = (overlay) => {
      currentOverlays.value.push({ ...overlay })
    }

    onMounted(async () => {
      // Check initial engine status
      engineStatus.value = await window.electronAPI.getEngineStatus()
    })

    return {
      activeTab,
      contentTab,
      isEditMode,
      engineStatus,
      currentSchedule,
      timelineV2,
      mediaLibrary,
      availableOverlays,
      currentOverlays,
      loadSchedule,
      saveSchedule,
      loadTimeline,
      saveTimeline,
      toggleEngineConnection,
      toggleMode,
      addNewSegment,
      removeSegment,
      editSegment,
      selectMedia,
      addOverlayToSegment,
      // V2 content management
      addProgram,
      editProgram,
      removeProgram,
      addAd,
      editAd,
      removeAd,
      addOverlay,
      editOverlay,
      removeOverlay,
      addLive,
      editLive,
      removeLive,
      addTrack,
      onItemSelected,
      onItemUpdated
    }
  }
}
</script>

<style>
@import 'tailwindcss/base';
@import 'tailwindcss/components';
@import 'tailwindcss/utilities';

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