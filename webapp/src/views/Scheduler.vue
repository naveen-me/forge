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
          <draggable v-model="schedule" group="schedule" @end="onDragEnd" item-key="id" class="space-y-2">
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
            <draggable v-model="content" :group="{ name: 'schedule', pull: 'clone', put: false }" item-key="id" class="space-y-2">
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
import api from '../api';

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
    // ...
  },
  methods: {
    async fetchSchedule() {
      const response = await api.getSchedule(1, new Date()); // Assuming channel_id 1
      this.schedule = response.data;
    },
    async onDragEnd(event) {
      const { newIndex } = event;
      const movedItem = this.schedule[newIndex];
      await api.updateScheduleItem(1, movedItem.id, { order: newIndex });
      this.fetchSchedule();
    },
    async onDrop(event) {
      const { newIndex } = event;
      const droppedItem = this.content[event.oldIndex];

      const newItem = {
        item_id: droppedItem.id,
        item_type: this.activeTab.slice(0, -1),
        duration: droppedItem.duration,
        order: newIndex,
      };

      await api.addScheduleItem(1, newItem);
      this.fetchSchedule();
    },
    formatTime(dateString) {
      const date = new Date(dateString);
      return date.toLocaleTimeString();
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
      return (elapsed / duration) * 100;
    },
    editItem(item) {
      // Placeholder for edit logic
    },
    async deleteItem(id) {
      await api.deleteScheduleItem(1, id); // Assuming channel_id 1
      this.fetchSchedule();
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
      if (this.editingLink) {
        await api.updateLink(this.editingLink.id, this.linkForm);
      } else {
        await api.createLink(this.linkForm);
      }
      this.fetchLinks();
      this.closeLinkModal();
    },
    async deleteLink(id) {
      await api.deleteLink(id);
      this.fetchLinks();
    },
    async fetchContent() {
      let response;
      if (this.activeTab === 'media') {
        response = await api.getMedia();
      } else if (this.activeTab === 'ads') {
        response = await api.getAds();
      } else if (this.activeTab === 'links') {
        response = await api.getLinks();
      }
      this.content = response.data;
    },
  },
  watch: {
    activeTab: {
      immediate: true,
      handler() {
        this.fetchContent();
      },
    },
  },
  mounted() {
    this.fetchSchedule();
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
