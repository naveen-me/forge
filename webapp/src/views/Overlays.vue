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
        <button @click="createGroupFromSelection" :disabled="selectedOverlays.length === 0" class="flex h-10 items-center justify-center rounded-lg bg-gray-200 px-4 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-400 focus-visible:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed">
          <span class="material-symbols-outlined mr-2">create_new_folder</span>
          Create Group from Selection
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
          <div class="space-y-1 p-2">
            <div v-for="overlay in topLevelOverlays" :key="overlay.id" :data-id="overlay.id">
              <div @click="handleOverlayClick(overlay)" class="group flex items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 cursor-pointer" :class="{ 'bg-gray-100': selectedOverlay === overlay }">
                <div class="flex items-center gap-3">
                  <input v-if="overlay.type !== 'group'" type="checkbox" class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary" :value="overlay.id" v-model="selectedOverlays" @click.stop/>
                  <span v-if="overlay.type === 'group'" class="material-symbols-outlined text-gray-400 group-hover:text-gray-600">
                    {{ expandedGroups.includes(overlay.id) ? 'expand_more' : 'chevron_right' }}
                  </span>
                  <div class="flex flex-col">
                    <span class="text-gray-900">{{ overlay.name }}</span>
                    <span class="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800">{{ overlay.type }}</span>
                  </div>
                </div>
                <!-- No drag handle for top-level items -->
              </div>
              <draggable v-if="overlay.type === 'group' && expandedGroups.includes(overlay.id)" :list="getChildren(overlay.id)" tag="div" class="pl-6 space-y-1" :group="`group-${overlay.id}`" item-key="id" handle=".handle" @end="onChildDragEnd($event, overlay.id)">
                <template #item="{ element: child }">
                  <div @click="handleOverlayClick(child)" class="group flex items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100" :class="{ 'bg-gray-100': selectedOverlay === child || selectedGroup === child }" :data-id="child.id">
                    <div class="flex items-center gap-3">
                      <div class="flex flex-col">
                        <span class="text-gray-900">{{ child.name }}</span>
                        <span class="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800">{{ child.type }}</span>
                      </div>
                    </div>
                    <div class="flex items-center gap-1 handle">
                      <span class="material-symbols-outlined text-gray-400">drag_indicator</span>
                    </div>
                  </div>
                </template>
              </draggable>
            </div>
          </div>
        </div>
      </aside>
      <section class="flex-1 flex flex-col p-6 overflow-hidden">
        <GroupPreview v-if="selectedGroup" :group="selectedGroup" :children="getChildren(selectedGroup.id)" />
        <OverlayForm v-else-if="selectedOverlay" :selectedOverlay="selectedOverlay" :allOverlays="overlays" @update="updateOverlay" @delete="deleteOverlay" />
        <div v-else class="flex items-center justify-center h-full">
          <div class="text-center">
            <h2 class="text-xl font-semibold text-gray-700">Select an overlay or group</h2>
            <p class="text-gray-500">Choose an item from the left panel to see its details or preview.</p>
          </div>
        </div>
      </section>
    </main>
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
import draggable from 'vuedraggable';
import OverlayForm from '../components/OverlayForm.vue';
import GroupPreview from '../components/GroupPreview.vue';
import { api } from '../services/api'; // Use consistent API service

const overlays = ref([]);
const selectedOverlay = ref(null);
const selectedGroup = ref(null);
const selectedOverlays = ref([]);
const showAddOverlayModal = ref(false);
const searchQuery = ref('');
const expandedGroups = ref([]);

const fetchOverlays = async () => {
  try {
    const response = await api.get('/overlays');
    overlays.value = response.data;
  } catch (error) {
    console.error('Error fetching overlays:', error);
  }
};

const createOverlay = async (type) => {
  try {
    const response = await api.post('/overlays', { name: 'New Overlay', type });
    overlays.value.push(response.data);
    selectedOverlay.value = response.data;
    selectedGroup.value = null;
  } catch (error) {
    console.error('Error creating overlay:', error);
  }
};

const createGroupFromSelection = async () => {
  if (selectedOverlays.value.length === 0) return;
  try {
    await api.post('/overlays/group', {
      overlayIds: selectedOverlays.value,
    });
    selectedOverlays.value = [];
    fetchOverlays();
  } catch (error) {
    console.error('Error creating group from selection:', error);
  }
};

const updateOverlay = async (updatedOverlay) => {
  try {
    const response = await api.put(`/overlays/${updatedOverlay.id}`, updatedOverlay);
    const index = overlays.value.findIndex(o => o.id === updatedOverlay.id);
    if (index !== -1) {
      overlays.value[index] = response.data;
    }
    // Also update the selected overlay if it's the one being edited
    if (selectedOverlay.value && selectedOverlay.value.id === updatedOverlay.id) {
      selectedOverlay.value = response.data;
    }
  } catch (error) {
    console.error('Error updating overlay:', error);
  }
};

const deleteOverlay = async (overlayId) => {
  try {
    await api.delete(`/overlays/${overlayId}`);
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

const getChildren = (parentId) => {
  return overlays.value.filter(o => o.parentId === parentId);
};

const topLevelOverlays = computed(() => {
  const topLevel = filteredOverlays.value.filter(o => !o.parentId);
  return topLevel.map(overlay => {
    if (overlay.type === 'group') {
      return {
        ...overlay,
        children: getChildren(overlay.id)
      };
    }
    return overlay;
  });
});

const handleOverlayClick = (overlay) => {
  if (overlay.type === 'group') {
    selectedGroup.value = overlay;
    selectedOverlay.value = null;
    const index = expandedGroups.value.indexOf(overlay.id);
    if (index > -1) {
      expandedGroups.value.splice(index, 1);
    } else {
      expandedGroups.value.push(overlay.id);
    }
  } else {
    selectedOverlay.value = overlay;
    selectedGroup.value = null;
  }
};

const onChildDragEnd = (event, parentId) => {
  const { to } = event;
  const orderedIds = Array.from(to.children).map(el => el.dataset.id);
  updateOverlayOrder(orderedIds, parentId);
};

const updateOverlayOrder = async (orderedIds, parentId) => {
  try {
    await api.post('/overlays/order', { orderedIds, parentId });
  } catch (error) {
    console.error('Error updating overlay order:', error);
  }
};

onMounted(fetchOverlays);
</script>
