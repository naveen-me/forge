<template>
  <div class="timeline">
    <draggable v-model="localItems" item-key="id" @end="onDragEnd" :group="{ name: 'schedule' }" @add="onAdd">
      <template #item="{ element }">
        <schedule-item-card
          :item="element"
          @edit="$emit('edit', element)"
          @add-gap="$emit('add-gap', $event)"
          @duplicate="$emit('duplicate', $event)"
          @repeat="$emit('repeat', $event)"
          @delete="$emit('delete', $event)"
          @play-media="$emit('play-media', $event)"
        />
      </template>
    </draggable>
  </div>
</template>

<script>
import { ref, watch } from 'vue';
import draggable from 'vuedraggable';
import ScheduleItemCard from './ScheduleItemCard.vue';

export default {
  name: 'Timeline',
  components: {
    draggable,
    ScheduleItemCard,
  },
  props: {
    items: {
      type: Array,
      required: true,
    },
  },
  emits: ['reorder', 'edit', 'add', 'add-gap', 'duplicate', 'repeat', 'delete', 'play-media'],
  setup(props, { emit }) {
    const localItems = ref([...props.items]);

    watch(() => props.items, (newItems) => {
      localItems.value = [...newItems];
    });

    const onDragEnd = (event) => {
      // Do not emit reorder if the item was moved to another list
      if (event.from !== event.to) return;
      const orderedIds = localItems.value.map(item => item.id);
      emit('reorder', orderedIds);
    };

    const onAdd = (event) => {
      const { item, newIndex } = event;
      const underlyingItem = item._underlying_vm_;

      if (!underlyingItem) return;

      const newItem = {
        id: underlyingItem.id || Date.now(),
        itemType: underlyingItem.itemType,
        itemId: underlyingItem.itemId,
        displayName: underlyingItem.displayName || underlyingItem.filename,
        duration: underlyingItem.duration,
        ...underlyingItem,
      };

      emit('add', { item: newItem, index: newIndex });
    };

    return {
      localItems,
      onDragEnd,
      onAdd,
    };
  },
};
</script>

<style scoped>
.timeline {
  min-height: 100px; /* Ensure drop zone is available even when empty */
}
</style>