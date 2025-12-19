import { defineStore } from 'pinia'
import apiService from '../services/api'

export const useScheduleStore = defineStore('schedule', {
  state: () => ({
    currentSchedule: null, // Can be either V1 or V2 format
    currentTimelineV2: {
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
        media: [],
        ads: [],
        overlays: [],
        live: []
      },
      timeline: {
        tracks: []
      },
      engineRules: {
        resumeAfterAd: true,
        onObsDisconnect: 'RETRY',
        onError: 'STOP'
      }
    },
    isLoading: false,
    isModified: false,
    validationErrors: [],
    schedulePath: '',
    isV2Format: false // Tracks whether we're using V2 format
  }),

  getters: {
    scheduleDuration: (state) => {
      if (state.isV2Format && state.currentTimelineV2?.timeline?.tracks) {
        // For V2 format, calculate based on timeline items
        let maxTime = 0;
        for (const track of state.currentTimelineV2.timeline.tracks) {
          for (const item of track.items) {
            if (item.timeline && item.timeline.end > maxTime) {
              maxTime = item.timeline.end;
            }
          }
        }
        return maxTime;
      } else if (state.currentSchedule?.segments) {
        // For V1 format, calculate based on segments
        if (!state.currentSchedule.segments || state.currentSchedule.segments.length === 0) {
          return 0
        }

        const lastSegment = state.currentSchedule.segments.reduce((prev, current) =>
          (prev.startTimeOffset + prev.duration > current.startTimeOffset + current.duration) ? prev : current
        )

        return lastSegment.startTimeOffset + lastSegment.duration
      }
      return 0;
    },

    totalSegments: (state) => {
      return state.isV2Format ?
        (state.currentTimelineV2?.timeline?.tracks?.length || 0) :
        (state.currentSchedule?.segments?.length || 0)
    }
  },

  actions: {
    async loadScheduleFromFile(filePath) {
      this.isLoading = true
      try {
        const result = await apiService.loadSchedule(filePath)
        if (result.success) {
          // Check if the loaded data is V2 format by checking for schemaVersion
          if (result.data.schemaVersion) {
            this.currentTimelineV2 = { ...result.data };
            this.isV2Format = true;
          } else {
            this.currentSchedule = { ...result.data };
            this.isV2Format = false;
          }

          this.schedulePath = filePath
          this.isModified = false
          this.validationErrors = []
          return { success: true }
        } else {
          throw new Error(result.error)
        }
      } catch (error) {
        console.error('Error loading schedule:', error)
        return { success: false, error: error.message }
      } finally {
        this.isLoading = false
      }
    },

    async saveScheduleToFile(filePath = null) {
      this.isLoading = true
      try {
        const pathToUse = filePath || this.schedulePath

        // Determine which schedule format to save
        const scheduleToSave = this.isV2Format ? this.currentTimelineV2 : this.currentSchedule;
        const result = await apiService.saveSchedule(pathToUse, scheduleToSave)

        if (result.success) {
          this.schedulePath = pathToUse
          this.isModified = false
          return { success: true }
        } else {
          throw new Error(result.error)
        }
      } catch (error) {
        console.error('Error saving schedule:', error)
        return { success: false, error: error.message }
      } finally {
        this.isLoading = false
      }
    },

    async validateCurrentSchedule() {
      try {
        // Use the appropriate schedule based on format
        const scheduleToValidate = this.isV2Format ? this.currentTimelineV2 : this.currentSchedule;
        const result = await apiService.validateSchedule(scheduleToValidate)
        this.validationErrors = result.success ? [] : [result.error]
        return result
      } catch (error) {
        this.validationErrors = [error.message]
        return { success: false, error: error.message }
      }
    },

    addSegment(segment) {
      if (!this.isV2Format) {
        if (!this.currentSchedule.segments) {
          this.currentSchedule.segments = [];
        }
        this.currentSchedule.segments.push({
          ...segment,
          id: segment.id || `seg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        })
        this.isModified = true
      }
    },

    addV2Asset(assetType, assetData) {
      if (this.isV2Format) {
        this.currentTimelineV2.assets[assetType].push({
          ...assetData,
          id: assetData.id || `${assetType}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        });
        this.isModified = true;
      }
    },

    addV2Track(trackData) {
      if (this.isV2Format) {
        this.currentTimelineV2.timeline.tracks.push({
          ...trackData,
          id: trackData.id || `track_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        });
        this.isModified = true;
      }
    },

    updateSegment(segmentId, updates) {
      if (!this.isV2Format && this.currentSchedule?.segments) {
        const index = this.currentSchedule.segments.findIndex(s => s.id === segmentId)
        if (index !== -1) {
          this.currentSchedule.segments[index] = {
            ...this.currentSchedule.segments[index],
            ...updates
          }
          this.isModified = true
        }
      }
    },

    removeSegment(segmentId) {
      if (!this.isV2Format && this.currentSchedule?.segments) {
        const index = this.currentSchedule.segments.findIndex(s => s.id === segmentId)
        if (index !== -1) {
          this.currentSchedule.segments.splice(index, 1)
          this.isModified = true
        }
      }
    },

    updateScheduleInfo(updates) {
      if (!this.isV2Format) {
        this.currentSchedule = { ...this.currentSchedule, ...updates }
        this.isModified = true
      }
    },

    updateTimelineV2(updates) {
      if (this.isV2Format) {
        this.currentTimelineV2 = { ...this.currentTimelineV2, ...updates }
        this.isModified = true
      }
    },

    switchToV2Format() {
      this.isV2Format = true;
      this.isModified = true;
    },

    switchToV1Format() {
      this.isV2Format = false;
      this.isModified = true;
    },

    reset() {
      this.currentSchedule = {
        id: '',
        name: '',
        version: '1.0.0',
        startDate: new Date(),
        endDate: new Date(),
        timezone: 'UTC',
        segments: [],
        obsConfiguration: {
          scene: 'Scene 1',
          output: {
            type: 'rtmp_output',
            settings: {}
          }
        }
      };

      this.currentTimelineV2 = {
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
          media: [],
          ads: [],
          overlays: [],
          live: []
        },
        timeline: {
          tracks: []
        },
        engineRules: {
          resumeAfterAd: true,
          onObsDisconnect: 'RETRY',
          onError: 'STOP'
        }
      };

      this.schedulePath = ''
      this.isModified = false
      this.validationErrors = []
      this.isV2Format = false
    }
  }
})