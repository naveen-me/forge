<template>
    <div class="p-4">
        <h1 class="text-2xl font-bold mb-4">Ads</h1>
        <div class="flex justify-between mb-4">
            <button @click="selectAndAddFiles" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
                Add Ads
            </button>
            <button @click="createGroup" class="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded">
                Create Group
            </button>
        </div>
        <draggable v-model="ads" @end="onDragEnd" item-key="id" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4" :group="{ name: 'ads' }">
            <template #item="{ element: ad }">
                <div v-if="ad.type === 'group'" class="border rounded-lg p-4 bg-gray-100" :data-id="ad.id" data-testid="ad-group">
                    <div class="flex justify-between items-center mb-2">
                        <input v-if="ad.editing" v-model="ad.newName" @blur="saveName(ad)" @keyup.enter="saveName(ad)" class="border rounded px-2 py-1 w-full" />
                        <span v-else @click="editName(ad)">{{ ad.name }}</span>
                        <button @click="deleteAd(ad.id)" class="text-red-500 hover:text-red-700">
                            <span class="material-symbols-outlined">delete</span>
                        </button>
                    </div>
                    <draggable v-model="ad.children" @end="onDragEnd" item-key="id" class="min-h-[100px] border-dashed border-2 border-gray-400 rounded-md p-2" :group="{ name: 'ads' }" :data-id="ad.id">
                        <template #item="{ element: child }">
                            <div class="border rounded-lg p-2 bg-white" :data-id="child.id">
                                <span>{{ child.name }}</span>
                            </div>
                        </template>
                    </draggable>
                </div>
                <div v-else class="border rounded-lg p-4" :data-id="ad.id" data-testid="ad-item">
                    <div class="flex justify-between items-center mb-2">
                        <input v-if="ad.editing" v-model="ad.newName" @blur="saveName(ad)" @keyup.enter="saveName(ad)" class="border rounded px-2 py-1 w-full" />
                        <span v-else @click="editName(ad)">{{ ad.name }}</span>
                        <button @click="deleteAd(ad.id)" class="text-red-500 hover:text-red-700">
                            <span class="material-symbols-outlined">delete</span>
                        </button>
                    </div>
                    <div class="aspect-w-16 aspect-h-9 bg-gray-200 rounded-md">
                        <img v-if="ad.thumbnailPath" :src="ad.thumbnailPath" class="w-full h-full object-cover" />
                        <span v-else class="material-symbols-outlined text-gray-400 text-4xl">movie</span>
                    </div>
                </div>
            </template>
        </draggable>
    </div>
</template>

<script>
import draggable from 'vuedraggable';
import adService from '../services/adService';

export default {
    name: 'Ads',
    components: {
        draggable,
    },
    data() {
        return {
            ads: [],
        };
    },
    methods: {
        async fetchAds() {
            try {
                const response = await adService.getAds();
                const ads = response.data.map((ad) => ({ ...ad, editing: false, newName: ad.name, children: [] }));
                const adMap = ads.reduce((acc, ad) => {
                    acc[ad.id] = ad;
                    return acc;
                }, {});
                const roots = [];
                ads.forEach(ad => {
                    if (ad.parentId) {
                        if (adMap[ad.parentId]) {
                            adMap[ad.parentId].children.push(ad);
                        }
                    } else {
                        roots.push(ad);
                    }
                });
                this.ads = roots;
            } catch (error) {
                console.error('Error fetching ads:', error);
            }
        },
        async selectAndAddFiles() {
            try {
                const response = await adService.selectFiles();
                const files = response.data.files;
                if (files.length > 0) {
                    const newFiles = await adService.addFiles(files.map((file) => ({ path: file })));
                    newFiles.data.forEach(file => {
                        adService.generateThumbnail(file.id);
                    });
                    this.fetchAds();
                }
            } catch (error) {
                console.error('Error selecting or adding files:', error);
            }
        },
        async createGroup() {
            try {
                await adService.createGroup('New Group', []);
                this.fetchAds();
            } catch (error) {
                console.error('Error creating group:', error);
            }
        },
        editName(ad) {
            ad.editing = true;
        },
        async saveName(ad) {
            ad.editing = false;
            if (ad.name !== ad.newName) {
                try {
                    await adService.renameAd(ad.id, ad.newName);
                    ad.name = ad.newName;
                } catch (error) {
                    console.error('Error renaming ad:', error);
                }
            }
        },
        async deleteAd(id) {
            try {
                await adService.deleteAd(id);
                this.fetchAds();
            } catch (error) {
                console.error('Error deleting ad:', error);
            }
        },
        async onDragEnd(event) {
            const { to, from, item, newIndex } = event;
            const adId = item.dataset.id;
            const toId = to.dataset.id;
            const fromId = from.dataset.id;

            let parentId = toId || null;

            let list = this.ads;
            if (fromId) {
                const group = this.ads.find(ad => ad.id == fromId);
                if (group) {
                    list = group.children;
                }
            }

            const draggedItem = list.find(ad => ad.id == adId);

            if (toId !== fromId) {
                draggedItem.parentId = parentId;
            }

            let orderedIds = [];
            if (toId) {
                const group = this.ads.find(ad => ad.id == toId);
                if (group) {
                    orderedIds = group.children.map(child => child.id);
                }
            } else {
                orderedIds = this.ads.map(ad => ad.id);
            }

            try {
                await adService.updateOrder(orderedIds, parentId);
            } catch (error) {
                console.error('Error updating ad order:', error);
            }
        },
    },
    created() {
        this.fetchAds();
    },
};
</script>
