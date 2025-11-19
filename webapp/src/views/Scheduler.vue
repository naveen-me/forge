<template>
  <div class="flex h-screen bg-background-light font-display text-gray-800">
    <main class="flex-1 flex flex-col h-screen overflow-hidden">
      <header class="p-6 border-b border-gray-200">
        <div class="flex flex-wrap justify-between items-center gap-3">
          <div class="flex flex-col gap-1">
            <h1 class="text-gray-900 text-3xl font-bold leading-tight tracking-tight">Scheduler</h1>
          </div>
        </div>
      </header>
      <div class="flex-1 grid grid-cols-1 lg:grid-cols-3 gap-6 p-6 overflow-hidden">
        <div class="lg:col-span-2 flex flex-col bg-white rounded-xl overflow-hidden shadow-sm border border-gray-200">
          <div class="p-4 border-b border-gray-200 flex justify-between items-center">
            <h2 class="text-gray-900 text-lg font-bold">Timeline</h2>
            <div class="flex items-center gap-2">
              <button @click="previousDay" class="p-2 rounded-md hover:bg-gray-100 transition-colors">
                <span class="material-symbols-outlined text-gray-500">chevron_left</span>
              </button>
              <div class="relative">
                <input type="date" v-model="currentDateISO" @change="handleDateChange" class="form-control" />
              </div>
              <button @click="nextDay" class="p-2 rounded-md hover:bg-gray-100 transition-colors">
                <span class="material-symbols-outlined text-gray-500">chevron_right</span>
              </button>
              <div class="relative group">
                <button class="p-2 rounded-md hover:bg-gray-100 text-gray-500">
                  <span class="material-symbols-outlined">more_vert</span>
                </button>
                <div class="absolute right-0 top-full mt-2 w-48 bg-white rounded-lg shadow-xl border border-gray-200 py-1 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none group-hover:pointer-events-auto z-10">
                  <a class="flex items-center gap-2 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100" href="#">
                    <span class="material-symbols-outlined text-base">content_copy</span>
                    Duplicate Timeline
                  </a>
                  <a class="flex items-center gap-2 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100" href="#">
                    <span class="material-symbols-outlined text-base">repeat</span>
                    Repeat Timeline
                  </a>
                </div>
              </div>
            </div>
          </div>
          <div class="flex-1 p-4 space-y-4 overflow-y-auto">
            <draggable
              v-model="schedule"
              group="schedule"
              @end="onDragEnd"
              item-key="id"
              class="space-y-4"
              @add="onAddToSchedule"
            >
              <template #item="{ element }">
                <div
                  class="flex flex-col p-4 rounded-lg border bg-white shadow-sm"
                  :class="{
                    'border-green-500 shadow-md': isNowPlaying(element),
                    'border-gray-200': !isNowPlaying(element)
                  }"
                  :style="isNowPlaying(element) ? getProgressBgStyle(element) : {}"
                >
                  <div class="flex items-start gap-4">
                    <div class="text-white flex items-center justify-center rounded-lg bg-primary/20 text-primary shrink-0 size-12">
                      <span class="material-symbols-outlined">{{ getItemIcon(element.item_type) }}</span>
                    </div>
                    <div class="flex-1 grid grid-cols-12 items-center gap-4">
                      <div class="flex flex-col col-span-5">
                        <p class="text-gray-900 text-base font-medium leading-normal line-clamp-1">
                          <span v-if="isNowPlaying(element)">Now Playing: </span>
                          {{ element.item?.name || element.name || element.item?.title || element.title || element.item_type }}
                        </p>
                        <p v-if="isNowPlaying(element)" class="text-green-500 text-sm font-medium leading-normal">
                          Active ({{ getCurrentProgressTime(element) }} / {{ formatDuration(getDuration(element)) }})
                        </p>
                      </div>
                      <p class="text-gray-500 text-sm font-mono col-span-2 text-right">{{ formatTime(element.start_time) }}</p>
                      <p class="text-gray-500 text-sm font-mono col-span-2 text-right">{{ formatTime(element.end_time) }}</p>
                      <p class="text-gray-500 text-sm font-mono col-span-2 text-right">{{ formatDuration(getDuration(element)) }}</p>
                    </div>
                  </div>
                  <div class="flex items-center justify-end gap-2 mt-2">
                    <button :disabled="isNowPlaying(element)" class="px-3 py-1.5 text-xs font-medium rounded-md flex items-center gap-1" :class="isNowPlaying(element) ? 'text-gray-400 bg-gray-100 cursor-not-allowed' : 'text-gray-600 bg-gray-100 hover:bg-gray-200 transition-colors'">
                      <span class="material-symbols-outlined text-sm">layers</span>
                      Overlays <span class="rounded-full px-1.5 py-0.5 text-xs" :class="isNowPlaying(element) ? 'text-gray-400 bg-gray-200' : 'text-gray-500 bg-gray-200'">0</span>
                    </button>
                    <button :disabled="isNowPlaying(element)" class="px-3 py-1.5 text-xs font-medium rounded-md flex items-center gap-1" :class="isNowPlaying(element) ? 'text-gray-400 bg-gray-100 cursor-not-allowed' : 'text-gray-600 bg-gray-100 hover:bg-gray-200 transition-colors'">
                      <span class="material-symbols-outlined text-sm">monetization_on</span>
                      Ads <span class="rounded-full px-1.5 py-0.5 text-xs" :class="isNowPlaying(element) ? 'text-gray-400 bg-gray-200' : 'text-gray-500 bg-gray-200'">0</span>
                    </button>
                    <button :disabled="isNowPlaying(element)" @click="editItem(element)" class="px-3 py-1.5 text-xs font-medium rounded-md flex items-center gap-1" :class="isNowPlaying(element) ? 'text-gray-400 bg-gray-100 cursor-not-allowed' : 'text-gray-600 bg-gray-100 hover:bg-gray-200 transition-colors'">
                      <span class="material-symbols-outlined text-sm">edit</span>
                      Edit
                    </button>
                    <button :disabled="isNowPlaying(element)" @click="deleteItem(element.id)" class="px-3 py-1.5 text-xs font-medium rounded-md flex items-center gap-1" :class="isNowPlaying(element) ? 'text-red-300 bg-red-50 cursor-not-allowed' : 'text-red-600 bg-red-100 hover:bg-red-200 transition-colors'">
                      <span class="material-symbols-outlined text-sm">delete</span>
                      Delete
                    </button>
                    <div class="relative group">
                      <button :disabled="isNowPlaying(element)" class="p-2 rounded-full text-gray-500" :class="isNowPlaying(element) ? 'text-gray-400 cursor-not-allowed' : 'hover:bg-gray-100'">
                        <span class="material-symbols-outlined text-base">more_vert</span>
                      </button>
                      <div v-if="!isNowPlaying(element)" class="absolute right-0 top-full mt-2 w-40 bg-white rounded-lg shadow-xl border border-gray-200 py-1 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none group-hover:pointer-events-auto z-10">
                        <a class="flex items-center gap-2 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100" href="#">
                          <span class="material-symbols-outlined text-base">content_copy</span>
                          Duplicate
                        </a>
                        <a class="flex items-center gap-2 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100" href="#">
                          <span class="material-symbols-outlined text-base">repeat</span>
                          Repeat
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </draggable>
          </div>
        </div>
        <div class="lg:col-span-1 flex flex-col bg-white rounded-xl overflow-hidden shadow-sm border border-gray-200">
          <div class="p-4 border-b border-gray-200">
            <div class="flex items-center">
              <button @click="activeTab = 'media'" :class="{'border-primary text-primary': activeTab === 'media', 'border-transparent text-gray-500 hover:text-gray-700': activeTab !== 'media'}" class="px-4 py-2 text-sm font-medium border-b-2">Media</button>
              <button @click="activeTab = 'ads'" :class="{'border-primary text-primary': activeTab === 'ads', 'border-transparent text-gray-500 hover:text-gray-700': activeTab !== 'ads'}" class="px-4 py-2 text-sm font-medium border-b-2">Ads</button>
              <button @click="activeTab = 'links'" :class="{'border-primary text-primary': activeTab === 'links', 'border-transparent text-gray-500 hover:text-gray-700': activeTab !== 'links'}" class="px-4 py-2 text-sm font-medium border-b-2">Links</button>
            </div>
          </div>
          <div class="flex-1 flex flex-col overflow-y-auto">
            <div class="flex flex-col h-full">
              <div class="p-4 border-b border-gray-200 flex items-center gap-2">
                <div class="relative w-full">
                  <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-xl">search</span>
                  <input v-model="searchQuery" class="w-full pl-10 pr-4 py-2 bg-gray-100 border border-transparent focus:ring-2 focus:ring-primary focus:border-primary rounded-lg text-sm text-gray-900 placeholder-gray-400" :placeholder="`Search ${activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}...`" type="text"/>
                </div>
                <div class="flex items-center bg-gray-100 rounded-lg p-0.5">
                  <button v-if="activeTab !== 'links'" @click="viewMode = 'grid'" class="p-1.5 rounded-md" :class="viewMode === 'grid' ? 'text-primary bg-white shadow-sm' : 'text-gray-400 hover:bg-white hover:text-gray-700 transition-colors'">
                    <span class="material-symbols-outlined text-xl">grid_view</span>
                  </button>
                  <button v-if="activeTab !== 'links'" @click="viewMode = 'list'" class="p-1.5 rounded-md" :class="viewMode === 'list' ? 'text-primary bg-white shadow-sm' : 'text-gray-400 hover:bg-white hover:text-gray-700 transition-colors'">
                    <span class="material-symbols-outlined text-xl">view_list</span>
                  </button>
                  <button v-if="activeTab === 'links'" @click="showLinkModal = true" class="px-3 py-1 rounded-md bg-primary text-white text-sm">Add Link</button>
                </div>
              </div>
              <div class="p-4 space-y-3 flex-1 overflow-y-auto">
                <draggable
                  :list="filteredContent"
                  :group="{ name: 'schedule', pull: 'clone', put: false }"
                  item-key="id"
                  class="space-y-2"
                  :class="{'grid grid-cols-2 gap-2': viewMode === 'grid' && activeTab !== 'links'}"
                >
                  <template #item="{ element }">
                    <div 
                      class="p-2 rounded-lg hover:bg-gray-100 cursor-grab"
                      :class="viewMode === 'list' || activeTab === 'links' ? 'flex items-center gap-4' : 'flex flex-col items-center gap-2'"
                    >
                      <div v-if="element.thumbnail" 
                        class="relative group bg-center bg-no-repeat aspect-video bg-cover rounded-md"
                        :class="viewMode === 'list' || activeTab === 'links' ? 'w-20 h-12' : 'w-full h-24'"
                        :style="{backgroundImage: `url(${element.thumbnail})`}">
                        <div v-if="element.item_type === 'media'" class="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity">
                          <span class="material-symbols-outlined text-white" style="font-variation-settings: 'FILL' 1;">play_circle</span>
                        </div>
                      </div>
                      <div v-else 
                        class="flex items-center justify-center bg-gray-100 rounded-md shrink-0"
                        :class="viewMode === 'list' || activeTab === 'links' ? 'w-20 h-12' : 'w-full h-24'"
                        >
                        <span class="material-symbols-outlined text-gray-400" :class="viewMode === 'grid' && activeTab !== 'links' ? 'text-4xl' : ''">{{ getItemIcon(element.item_type || element.type) }}</span>
                      </div>
                      <div class="flex-1" :class="viewMode === 'grid' && activeTab !== 'links' ? 'text-center' : ''">
                        <p class="text-gray-900 text-sm font-medium">{{ element.title || element.name }}</p>
                        <p class="text-gray-500 text-xs">{{ element.item_type || element.type }} - {{ element.duration ? formatDuration(element.duration) : (element.url || '') }}</p>
                      </div>
                    </div>
                  </template>
                </draggable>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    <schedule-item-modal
      v-if="showEditModal"
      :item="editingItem"
      @close="closeEditModal"
      @save="saveItem"
    />
    <!-- Link Modal -->
    <div v-if="showLinkModal" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold">{{ editingLink ? 'Edit Link' : 'Add Link' }}</h3>
          <button @click="closeLinkModal" class="text-gray-400 hover:text-gray-600">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <form @submit.prevent="saveLink" class="space-y-4">
          <input type="text" placeholder="Name" v-model="linkForm.name" required class="w-full px-3 py-2 rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm" />
          <input type="url" placeholder="URL" v-model="linkForm.url" required class="w-full px-3 py-2 rounded-md border border-gray-300 shadow-sm focus:border-primary focus:ring-primary sm:text-sm" />
          <button type="submit" class="w-full px-4 py-2 rounded-md bg-primary text-white">Save</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable';
import { api } from '../services/api'; // Use correct API service with auth
import ScheduleItemModal from '../components/ScheduleItemModal.vue';

export default {
  name: 'Scheduler',
  components: {
    draggable,
    ScheduleItemModal,
  },
  data() {
    return {
      schedule: [],
      currentTime: new Date(),
      currentDate: new Date(),
      activeTab: 'media',
      media: [],
      ads: [],
      links: [],
      searchQuery: '',
      showLinkModal: false,
      editingLink: null,
      linkForm: {
        name: '',
        url: '',
      },
      showEditModal: false,
      editingItem: null,
      viewMode: 'list',
    };
  },
  computed: {
    currentDateISO: {
      get() {
        return this.currentDate.toISOString().split('T')[0];
      },
      set(isoDate) {
        this.currentDate = new Date(isoDate);
      }
    },
    formattedCurrentDate() {
      return this.currentDate.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    },
    content() {
      switch (this.activeTab) {
        case 'media':
          return this.media;
        case 'ads':
          return this.ads;
        case 'links':
          return this.links;
        default:
          return [];
      }
    },
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
        const date = this.currentDate.toISOString().split('T')[0];
        const response = await api.get(`/schedule/1/${date}`);
        this.schedule = response.data.success ? response.data.data : response.data;
      } catch (error) {
        console.error('Error fetching schedule:', error);
        this.schedule = [];
      }
    },
    previousDay() {
      const newDate = new Date(this.currentDate);
      newDate.setDate(newDate.getDate() - 1);
      this.currentDate = newDate;
      this.fetchSchedule();
    },
    nextDay() {
      const newDate = new Date(this.currentDate);
      newDate.setDate(newDate.getDate() + 1);
      this.currentDate = newDate;
      this.fetchSchedule();
    },
    handleDateChange(event) {
      // The v-model on currentDateISO already updates `currentDate`,
      // but the time might be set to midnight UTC. We need to adjust it to local timezone midnight.
      const [year, month, day] = event.target.value.split('-').map(Number);
      this.currentDate = new Date(year, month - 1, day);
      this.fetchSchedule();
    },
    async fetchMedia() {
      try {
        const response = await api.get('/media');
        this.media = response.data.success ? response.data.data.map(item => ({...item, item_type: 'media'})) : response.data.map(item => ({...item, item_type: 'media'}));
      } catch (error) {
        console.error('Error fetching media:', error);
      }
    },
    async fetchAds() {
      try {
        const response = await api.get('/ads');
        this.ads = response.data.success ? response.data.data.map(item => ({...item, item_type: 'ad'})) : response.data.map(item => ({...item, item_type: 'ad'}));
      } catch (error) {
        console.error('Error fetching ads:', error);
      }
    },
    async fetchLinks() {
      try {
        const response = await api.get('/links');
        this.links = response.data.success ? response.data.data.map(item => ({...item, item_type: 'link'})) : response.data.map(item => ({...item, item_type: 'link'}));
      } catch (error) {
        console.error('Error fetching links:', error);
      }
    },
    async onDragEnd(event) {
      const { newIndex, oldIndex } = event;
      if (newIndex !== oldIndex) {
        await this.updateScheduleOrder();
      }
    },
    async onAddToSchedule(event) {
      const { newIndex } = event;
      // vuedraggable has already added a clone to the schedule array.
      // We need to get the original data from the source item.
      const originalItem = event.item._underlying_vm_;

      // If we can't get the original item, fall back to the cloned item.
      const sourceItem = originalItem || this.schedule[newIndex];

      // Create a new object that matches the structure of the existing schedule items.
      // This ensures the template can find the name/title.
      const tempScheduleItem = {
        id: `temp-${Date.now()}`, // A temporary unique ID for the key
        item_id: sourceItem.id,
        item_type: sourceItem.item_type,
        // Nest the source item's data so the template can access it via `element.item.name` etc.
        item: { ...sourceItem },
        // We don't have start/end time yet, but that's okay for the initial display.
        start_time: null,
        end_time: null,
      };

      // Replace the item vuedraggable added with our properly structured temporary item.
      this.schedule.splice(newIndex, 1, tempScheduleItem);

      const newItemForApi = {
        item_id: sourceItem.id,
        item_type: sourceItem.item_type,
        duration: sourceItem.duration || 300,
        order: newIndex,
      };

      try {
        await api.post('/schedule/1', newItemForApi);
        // Fetching the schedule will replace the temporary item with the real one from the DB.
        await this.fetchSchedule();
      } catch (error) {
        console.error('Error adding item to schedule:', error);
        // If the API call fails, refetch to remove the optimistic update.
        await this.fetchSchedule();
      }
    },
    async updateScheduleOrder() {
      const orderedIds = this.schedule.map((item, index) => ({
        id: item.id,
        order: index
      }));

      try {
        await api.put('/schedule/1/order', { schedule: orderedIds });
        this.fetchSchedule();
      } catch (error) {
        console.error('Error updating schedule order:', error);
      }
    },
    formatTime(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
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
    getDuration(item) {
      if (!item.start_time || !item.end_time) return 0;
      const start = new Date(item.start_time);
      const end = new Date(item.end_time);
      const diff = end.getTime() - start.getTime();
      return Math.round(diff / 1000); // Convert to seconds
    },
    formatDuration(totalSeconds) {
      if (totalSeconds < 60) {
        return `${totalSeconds}s`;
      }
      const minutes = Math.floor(totalSeconds / 60);
      const seconds = totalSeconds % 60;
      return seconds === 0 ? `${minutes}m` : `${minutes}m ${seconds}s`;
    },
    getCurrentProgressTime(item) {
      if (!this.isNowPlaying(item)) return '0:00';
      const now = this.currentTime.getTime();
      const start = new Date(item.start_time).getTime();
      const elapsed = now - start;
      return this.formatDuration(Math.floor(elapsed / 1000));
    },
    getItemIcon(item_type) {
      switch (item_type) {
        case 'media':
          return 'videocam';
        case 'ad':
          return 'monetization_on';
        case 'link':
          return 'link';
        default:
          return 'play_circle';
      }
    },
    getProgressBgStyle(item) {
      const progress = this.getProgress(item);
      return {
        background: `linear-gradient(to right, #eef7ff ${progress}%, white ${progress}%)`
      };
    },
    editItem(item) {
      this.editingItem = item;
      this.showEditModal = true;
    },
    closeEditModal() {
      this.showEditModal = false;
      this.editingItem = null;
    },
    async saveItem(updatedItem) {
      try {
        await api.put(`/schedule/1/${updatedItem.id}`, updatedItem);
        this.fetchSchedule();
        this.closeEditModal();
      } catch (error) {
        console.error('Error updating schedule item:', error);
      }
    },
    async deleteItem(id) {
      try {
        await api.delete(`/schedule/1/${id}`);
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
          //
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
  },
  mounted() {
    this.fetchSchedule();
    this.fetchMedia();
    this.fetchAds();
    this.fetchLinks();

    setInterval(() => {
      this.currentTime = new Date();
    }, 1000);
  },
};
</script>

<style>
.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}
</style>