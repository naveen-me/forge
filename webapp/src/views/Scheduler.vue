<template>
  <div class="p-6 bg-background-light dark:bg-background-dark font-sans h-screen flex flex-col">
    <!-- Header -->
    <div class="flex flex-col md:flex-row justify-between md:items-center mb-4 gap-3">
      <h1 class="text-2xl font-bold text-text-light dark:text-text-dark">Scheduler</h1>
    </div>

    <div class="flex flex-1 overflow-hidden gap-6">
      <!-- Timeline Section -->
      <div class="flex-1 flex flex-col">
        <h2 class="text-lg font-semibold text-text-light dark:text-text-dark mb-3">Timeline</h2>
        <div class="flex-1 overflow-y-auto bg-card-light dark:bg-card-dark rounded-lg shadow-sm p-4">
          <draggable
            v-model="schedule"
            group="schedule"
            @end="onDragEnd"
            item-key="id"
            class="space-y-2"
            @add="onAddToSchedule"
          >
            <template #item="{ element }">
              <div class="schedule-item p-3 rounded-lg bg-gray-100 dark:bg-gray-700" :class="{ 'now-playing': isNowPlaying(element) }">
                <p class="font-semibold text-text-light dark:text-text-dark">{{ element.item_type }} - {{ element.item_id }}</p>
                <p class="text-sm text-subtext-light dark:text-subtext-dark">{{ formatTime(element.start_time) }} - {{ formatTime(element.end_time) }}</p>
                <div class="progress-bar-container mt-2">
                  <div class="progress-bar" :style="{ width: getProgress(element) + '%' }"></div>
                </div>
                <div class="item-actions mt-2 flex gap-2">
                  <button @click="editItem(element)" class="text-xs px-2 py-1 rounded bg-gray-200 dark:bg-gray-600">Edit</button>
                  <button @click="deleteItem(element.id)" class="text-xs px-2 py-1 rounded bg-red-500 text-white">Delete</button>
                </div>
              </div>
            </template>
          </draggable>
        </div>
      </div>

      <!-- Content Library Section -->
      <div class="w-1/3 flex flex-col">
        <h2 class="text-lg font-semibold text-text-light dark:text-text-dark mb-3">Content Library</h2>
        <div class="bg-card-light dark:bg-card-dark rounded-lg shadow-sm p-4 flex-1 flex flex-col">
          <div class="tabs flex gap-2 mb-4">
            <button @click="activeTab = 'media'" :class="{ 'bg-primary text-white': activeTab === 'media' }" class="px-3 py-1 rounded-md text-sm">Media</button>
            <button @click="activeTab = 'ads'" :class="{ 'bg-primary text-white': activeTab === 'ads' }" class="px-3 py-1 rounded-md text-sm">Ads</button>
            <button @click="activeTab = 'links'" :class="{ 'bg-primary text-white': activeTab === 'links' }" class="px-3 py-1 rounded-md text-sm">Links</button>
          </div>
          <div class="controls flex gap-2 mb-4">
            <input type="text" placeholder="Search..." v-model="searchQuery" class="flex-1 w-full pl-4 pr-4 py-2 rounded-md border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-light dark:text-text-dark focus:ring-primary focus:border-primary text-sm h-full" />
            <button v-if="activeTab === 'links'" @click="showLinkModal = true" class="px-3 py-1 rounded-md bg-primary text-white text-sm">Add Link</button>
          </div>
          <div class="content-list flex-1 overflow-y-auto">
            <draggable
              :list="filteredContent"
              :group="{ name: 'schedule', pull: 'clone', put: false }"
              item-key="id"
              class="space-y-2"
            >
              <template #item="{ element }">
                <div class="content-item p-3 rounded-lg bg-gray-100 dark:bg-gray-700 cursor-move">
                  <p class="font-semibold text-text-light dark:text-text-dark">{{ element.title || element.name }}</p>
                </div>
              </template>
            </draggable>
          </div>
        </div>
      </div>
    </div>

    <!-- Link Modal -->
    <div v-if="showLinkModal" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div class="bg-card-light dark:bg-card-dark rounded-lg shadow-xl w-full max-w-md p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-text-light dark:text-text-dark">{{ editingLink ? 'Edit Link' : 'Add Link' }}</h3>
          <button @click="closeLinkModal" class="text-gray-400 hover:text-gray-600">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <form @submit.prevent="saveLink" class="space-y-4">
          <input type="text" placeholder="Name" v-model="linkForm.name" required class="w-full px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-background-light dark:bg-background-dark" />
          <input type="url" placeholder="URL" v-model="linkForm.url" required class="w-full px-3 py-2 rounded-md border border-border-light dark:border-border-dark bg-background-light dark:bg-background-dark" />
          <button type="submit" class="w-full px-4 py-2 rounded-md bg-primary text-white">Save</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable';
import { api } from '../services/api'; // Use correct API service with auth

export default {
  name: 'Scheduler',
  components: {
    draggable,
  },
  data() {
    return {
      schedule: [],
      currentTime: new Date(),
      activeTab: 'media',
      media: [],
      ads: [],
      links: [],
      content: [],
      searchQuery: '',
      showLinkModal: false,
      editingLink: null,
      linkForm: {
        name: '',
        url: '',
      },
    };
  },
  computed: {
    // Filter content based on search
    filteredContent() {
      if (!this.searchQuery) {
        return this.content;
      }
      return this.content.filter(item =>
        (item.name && item.name.toLowerCase().includes(this.searchQuery.toLowerCase())) ||
        (item.title && item.title.toLowerCase().includes(this.searchQuery.toLowerCase()))
      );
    }
  },
  methods: {
    async fetchSchedule() {
      try {
        const today = new Date().toISOString().split('T')[0]; // Format as YYYY-MM-DD
        const response = await api.get(`/schedule/1/${today}`); // Use correct endpoint with channel_id and date
        // Handle the response properly - expecting { success: true, data: [...] } or just the array
        this.schedule = response.data.success ? response.data.data : response.data;
      } catch (error) {
        console.error('Error fetching schedule:', error);
        // Initialize with an empty array if there's an error
        this.schedule = [];
      }
    },
    async fetchMedia() {
      try {
        const response = await api.get('/media');
        this.media = response.data.success ? response.data.data : response.data;
        if (this.activeTab === 'media') {
          this.content = this.media;
        }
      } catch (error) {
        console.error('Error fetching media:', error);
      }
    },
    async fetchAds() {
      try {
        const response = await api.get('/ads');
        this.ads = response.data.success ? response.data.data : response.data;
        if (this.activeTab === 'ads') {
          this.content = this.ads;
        }
      } catch (error) {
        console.error('Error fetching ads:', error);
      }
    },
    async fetchLinks() {
      try {
        const response = await api.get('/links');
        this.links = response.data.success ? response.data.data : response.data;
        if (this.activeTab === 'links') {
          this.content = this.links;
        }
      } catch (error) {
        console.error('Error fetching links:', error);
      }
    },
    async onDragEnd(event) {
      const { newIndex, oldIndex } = event;
      // This is for reordering existing items in the schedule
      if (newIndex !== oldIndex) {
        const movedItem = this.schedule[oldIndex];
        // Update the order via API
        await this.updateScheduleOrder();
      }
    },
    async onDrop(event) {
      // This is handled by vuedraggable's group settings - items will be added via the start event
      // For adding new items to the schedule from content library
    },
    // Handle when an item is moved from content to schedule
    async onAddToSchedule(event) {
      // Get the item that was added from the content list
      const itemIndex = event.item.__draggable_context.index;
      const addedItem = this.content[itemIndex];

      // Add to schedule
      const newItem = {
        item_id: addedItem.id,
        item_type: this.activeTab, // Use the current tab name (media, ads, links)
        duration: addedItem.duration || 300, // Default duration if not provided
        order: this.schedule.length, // Add at the end
      };

      try {
        // Add to schedule via API
        const response = await api.post('/schedule/1', newItem); // Use correct endpoint with channel_id
        // Refresh the schedule
        this.fetchSchedule();
      } catch (error) {
        console.error('Error adding item to schedule:', error);
      }
    },
    async updateScheduleOrder() {
      // Update the order of items in the schedule
      const orderedIds = this.schedule.map((item, index) => ({
        id: item.id,
        order: index
      }));

      try {
        // Call API to update order - scheduler service may need different approach
        // For each item in the schedule, we need to update its order
        for (let i = 0; i < this.schedule.length; i++) {
          const scheduleItem = this.schedule[i];
          await api.put(`/schedule/1/${scheduleItem.id}`, { order: i });
        }
        // Refresh schedule after updating
        this.fetchSchedule();
      } catch (error) {
        console.error('Error updating schedule order:', error);
      }
    },
    formatTime(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    },
    isNowPlaying(item) {
      const now = this.currentTime.getTime();
      const start = new Date(item.start_time).getTime();
      const end = new Date(item.end_time).getTime();
      return now >= start && now < end;
    },
    getProgress(item) {
      if (!this.isNowPlaying(item)) return 0;
      const now = this.currentTime.getTime();
      const start = new Date(item.start_time).getTime();
      const end = new Date(item.end_time).getTime();
      const duration = end - start;
      const elapsed = now - start;
      return Math.min(100, (elapsed / duration) * 100);
    },
    editItem(item) {
      // Placeholder for edit logic - could open a modal
      console.log('Edit item:', item);
    },
    async deleteItem(id) {
      try {
        await api.delete(`/schedule/1/${id}`); // Use correct endpoint with channel_id
        this.fetchSchedule();
      } catch (error) {
        console.error('Error deleting schedule item:', error);
      }
    },
    editLink(link) {
      this.editingLink = link;
      this.linkForm = { ...link };
      this.showLinkModal = true;
    },
    closeLinkModal() {
      this.showLinkModal = false;
      this.editingLink = null;
      this.linkForm = { name: '', url: '' };
    },
    async saveLink() {
      try {
        if (this.editingLink) {
          await api.put(`/links/${this.editingLink.id}`, this.linkForm);
        } else {
          await api.post('/links', this.linkForm);
        }
        this.fetchLinks();
        this.closeLinkModal();
      } catch (error) {
        console.error('Error saving link:', error);
      }
    },
    async deleteLink(id) {
      try {
        await api.delete(`/links/${id}`);
        this.fetchLinks();
      } catch (error) {
        console.error('Error deleting link:', error);
      }
    },
    async fetchContent() {
      switch (this.activeTab) {
        case 'media':
          await this.fetchMedia();
          break;
        case 'ads':
          await this.fetchAds();
          break;
        case 'links':
          await this.fetchLinks();
          break;
        default:
          this.content = [];
      }
    },
  },
  watch: {
    activeTab: {
      immediate: true,
      handler() {
        this.fetchContent();
      },
    },
    searchQuery() {
      // The computed property handles filtering automatically
    }
  },
  mounted() {
    // Fetch all data on mount
    this.fetchSchedule();
    this.fetchMedia();
    this.fetchAds();
    this.fetchLinks();

    // Update current time every second
    setInterval(() => {
      this.currentTime = new Date();
    }, 1000);
  },
};
</script>

<style scoped>
.now-playing {
  border-left: 4px solid #4ade80; /* A green accent for the playing item */
}
.progress-bar-container {
  height: 5px;
  background-color: #e5e7eb; /* A light gray background for the progress bar */
  border-radius: 9999px;
  overflow: hidden;
}
.progress-bar {
  height: 100%;
  background-color: #4ade80; /* A matching green for the progress */
  border-radius: 9999px;
}
</style>
