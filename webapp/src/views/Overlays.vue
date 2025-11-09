<template>
  <div class="flex h-screen flex-col">
    <header class="flex h-16 shrink-0 items-center justify-between border-b border-gray-200 px-6">
      <div class="flex items-center gap-4">
        <h1 class="text-lg font-bold text-gray-900">Overlay Manager</h1>
      </div>
      <div class="flex items-center gap-2">
        <button @click="showAddOverlayModal = true" class="flex h-10 items-center justify-center rounded-lg bg-primary px-4 text-sm font-medium text-white transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2">
          <span class="material-symbols-outlined mr-2">add</span>
          Add Overlay
        </button>
        <button @click="showAddGroupModal = true" class="flex h-10 items-center justify-center rounded-lg bg-gray-200 px-4 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-400 focus-visible:ring-offset-2">
          <span class="material-symbols-outlined mr-2">create_new_folder</span>
          Add Group
        </button>
      </div>
    </header>
    <main class="flex flex-1 overflow-hidden">
      <aside class="w-80 border-r border-gray-200 flex flex-col">
        <div class="p-4">
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">search</span>
            <input class="w-full rounded-lg bg-gray-100 pl-10 pr-4 py-2 text-sm text-gray-900 placeholder:text-gray-500 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary" placeholder="Search overlays..." type="text" v-model="searchQuery"/>
          </div>
        </div>
        <div class="px-4 pb-2 pt-2">
          <h2 class="text-xs font-semibold uppercase tracking-wider text-gray-500">Overlays</h2>
        </div>
        <div class="flex-1 overflow-y-auto">
          <nav class="space-y-1 p-2">
            <div v-for="overlay in topLevelOverlays" :key="overlay.id">
              <div @click="toggleGroup(overlay.id)" class="group flex items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 cursor-pointer" :class="{ 'bg-gray-100': selectedOverlay === overlay }">
                <div class="flex items-center gap-3">
                  <input type="checkbox" class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary" @click.stop/>
                  <span v-if="overlay.type === 'group'" class="material-symbols-outlined text-gray-400 group-hover:text-gray-600">
                    {{ expandedGroups.includes(overlay.id) ? 'expand_more' : 'chevron_right' }}
                  </span>
                  <div class="flex flex-col">
                    <span class="text-gray-900">{{ overlay.name }}</span>
                    <span class="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800">{{ overlay.type }}</span>
                  </div>
                </div>
                <div class="flex items-center gap-1">
                  <span class="material-symbols-outlined text-gray-400">drag_indicator</span>
                </div>
              </div>
              <div v-if="overlay.type === 'group' && expandedGroups.includes(overlay.id)" class="pl-6 space-y-1">
                <div v-for="child in getChildren(overlay.id)" :key="child.id" @click="selectedOverlay = child" class="group flex items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100" :class="{ 'bg-gray-100': selectedOverlay === child }">
                  <div class="flex items-center gap-3">
                    <input type="checkbox" class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary" @click.stop/>
                    <div class="flex flex-col">
                      <span class="text-gray-900">{{ child.name }}</span>
                      <span class="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800">{{ child.type }}</span>
                    </div>
                  </div>
                  <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-gray-400">drag_indicator</span>
                  </div>
                </div>
              </div>
            </div>
          </nav>
        </div>
      </aside>
      <section class="flex-1 flex flex-col p-6 overflow-hidden">
        <OverlayForm :selectedOverlay="selectedOverlay" @update="updateOverlay" @delete="deleteOverlay" />
      </section>
    </main>
    <div v-if="showAddGroupModal" class="fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-gray-900">Create New Group</h3>
          <button @click="showAddGroupModal = false" class="text-gray-400 hover:text-gray-600">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700" for="groupName">Group Name</label>
            <input v-model="newGroupName" class="mt-1 block w-full rounded-lg bg-gray-100 border-gray-200 shadow-sm focus:border-primary focus:ring-primary sm:text-sm text-gray-900" id="groupName" placeholder="Enter group name" type="text"/>
          </div>
        </div>
        <div class="flex justify-end mt-6 gap-2">
          <button @click="showAddGroupModal = false" class="flex h-10 items-center justify-center rounded-lg bg-gray-200 px-4 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-400 focus-visible:ring-offset-2">Cancel</button>
          <button @click="createGroup" class="flex h-10 items-center justify-center rounded-lg bg-primary px-4 text-sm font-medium text-white transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2">Create Group</button>
        </div>
      </div>
    </div>
    <div v-if="showAddOverlayModal" class="fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-gray-900">Select Overlay Type</h3>
          <button @click="showAddOverlayModal = false" class="text-gray-400 hover:text-gray-600">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <div @click="createOverlay('image'); showAddOverlayModal = false" class="flex flex-col items-center justify-center p-4 rounded-lg bg-gray-100 hover:bg-gray-200 cursor-pointer">
            <span class="material-symbols-outlined text-4xl">image</span>
            <span class="mt-2 text-sm font-medium">Image</span>
          </div>
          <div @click="createOverlay('video'); showAddOverlayModal = false" class="flex flex-col items-center justify-center p-4 rounded-lg bg-gray-100 hover:bg-gray-200 cursor-pointer">
            <span class="material-symbols-outlined text-4xl">videocam</span>
            <span class="mt-2 text-sm font-medium">Video</span>
          </div>
          <div @click="createOverlay('text'); showAddOverlayModal = false" class="flex flex-col items-center justify-center p-4 rounded-lg bg-gray-100 hover:bg-gray-200 cursor-pointer">
            <span class="material-symbols-outlined text-4xl">title</span>
            <span class="mt-2 text-sm font-medium">Text</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import OverlayForm from '../components/OverlayForm.vue';
import axios from 'axios';

const overlays = ref([]);
const selectedOverlay = ref(null);
const showAddGroupModal = ref(false);
const showAddOverlayModal = ref(false);
const newGroupName = ref('');
const searchQuery = ref('');
const expandedGroups = ref([]);

const fetchOverlays = async () => {
  try {
    const response = await axios.get('http://localhost:3001/api/overlays');
    overlays.value = response.data;
  } catch (error) {
    console.error('Error fetching overlays:', error);
  }
};

const createOverlay = async (type) => {
  try {
    const response = await axios.post('http://localhost:3001/api/overlays', { name: 'New Overlay', type });
    overlays.value.push(response.data);
    selectedOverlay.value = response.data;
  } catch (error) {
    console.error('Error creating overlay:', error);
  }
};

const createGroup = async () => {
  try {
    const response = await axios.post('http://localhost:3001/api/overlays', { name: newGroupName.value, type: 'group' });
    overlays.value.push(response.data);
    showAddGroupModal.value = false;
    newGroupName.value = '';
  } catch (error) {
    console.error('Error creating group:', error);
  }
};

const updateOverlay = async (updatedOverlay) => {
  try {
    const response = await axios.put(`http://localhost:3001/api/overlays/${updatedOverlay.id}`, updatedOverlay);
    const index = overlays.value.findIndex(o => o.id === updatedOverlay.id);
    if (index !== -1) {
      overlays.value[index] = response.data;
    }
  } catch (error) {
    console.error('Error updating overlay:', error);
  }
};

const deleteOverlay = async (overlayId) => {
  try {
    await axios.delete(`http://localhost:3001/api/overlays/${overlayId}`);
    overlays.value = overlays.value.filter(o => o.id !== overlayId);
    if (selectedOverlay.value?.id === overlayId) {
      selectedOverlay.value = null;
    }
  } catch (error) {
    console.error('Error deleting overlay:', error);
  }
};

const filteredOverlays = computed(() => {
  if (!searchQuery.value) {
    return overlays.value;
  }
  return overlays.value.filter(overlay =>
    overlay.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

const topLevelOverlays = computed(() => {
  return filteredOverlays.value.filter(o => !o.parentId);
});

const getChildren = (parentId) => {
  return filteredOverlays.value.filter(o => o.parentId === parentId);
};

const toggleGroup = (groupId) => {
  const index = expandedGroups.value.indexOf(groupId);
  if (index > -1) {
    expandedGroups.value.splice(index, 1);
  } else {
    expandedGroups.value.push(groupId);
  }
};

onMounted(fetchOverlays);
</script>
