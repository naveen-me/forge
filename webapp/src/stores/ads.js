import { defineStore } from 'pinia';
import adService from '../services/adService';

export const useAdStore = defineStore('ads', {
  state: () => ({
    items: [],
    currentGroupId: null,
    currentPath: [],
    selectedItems: [],
  }),

  getters: {
    groups: (state) => state.items.filter(item => item.type === 'group'),
    ads: (state) => state.items.filter(item => item.type !== 'group'),
    allItems: (state) => state.items,
  },

  actions: {
    async fetchGroupContents(groupId = null) {
      try {
        const response = await adService.getAds(groupId);
        this.items = response.data;
        this.currentGroupId = groupId;
      } catch (error) {
        console.error('Error fetching group contents:', error);
      }
    },

    async createGroup(name) {
        await adService.createGroup(name, []);
        await this.fetchGroupContents(null);
    },

    async addFiles(files, parentId = null) {
        await adService.addFiles(files, parentId);
        await this.fetchGroupContents(parentId);
    },
    
    async renameItem(id, name) {
        await adService.renameAd(id, name);
        await this.fetchGroupContents(this.currentGroupId);
    },

    async moveItems(itemIds, destinationGroupId) {
        // In Ads, we are moving ads to a group.
        // The backend implementation for this is to update the parentId of the ads.
        await adService.updateOrder(itemIds, destinationGroupId);
        this.selectedItems = [];
        await this.fetchGroupContents(this.currentGroupId);
    },

    async deleteSelectedItems() {
        for (const id of this.selectedItems) {
            await adService.deleteAd(id);
        }
        this.selectedItems = [];
        await this.fetchGroupContents(this.currentGroupId);
    },

    async search(query) {
        if (!query) {
            await this.fetchGroupContents(this.currentGroupId);
            return;
        }
        try {
            // search is not implemented in adService yet
            // const response = await adService.searchItems(query);
            // this.items = response.data;
            console.log('Search not implemented yet');
        } catch (error) {
            console.error('Error searching items:', error);
        }
    },

    async fetchPath(groupId = null) {
      // Since we only have one level of groups, the path is simple.
      if (groupId) {
        const group = this.items.find(item => item.id === groupId);
        if (group) {
          this.currentPath = [group];
        } else {
          this.currentPath = [];
        }
      } else {
        this.currentPath = [];
      }
    },

    setCurrentGroupId(groupId) {
        this.currentGroupId = groupId;
        this.selectedItems = [];
        this.fetchGroupContents(groupId);
        this.fetchPath(groupId);
    },

    toggleItemSelected(itemId) {
        const index = this.selectedItems.indexOf(itemId);
        if (index > -1) {
            this.selectedItems.splice(index, 1);
        } else {
            this.selectedItems.push(itemId);
        }
    },

    clearSelection() {
        this.selectedItems = [];
    },

    selectAll(itemIds) {
        this.selectedItems = [...itemIds];
    },

    sortItems(field, direction) {
      this.items.sort((a, b) => {
        let valA = a[field];
        let valB = b[field];

        if (field === 'name') {
          valA = valA.toLowerCase();
          valB = valB.toLowerCase();
        }

        if (valA < valB) {
          return direction === 'asc' ? -1 : 1;
        }
        if (valA > valB) {
          return direction === 'asc' ? 1 : -1;
        }
        return 0;
      });
    }
  },
});
