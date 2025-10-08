<template>
  <div class="timeline-controls btn-toolbar" role="toolbar">
    <div class="btn-group me-2" role="group">
      <button
        class="btn btn-sm"
        :class="isPublished ? 'btn-warning' : 'btn-success'"
        @click="$emit('toggle-publish')"
        :disabled="!canOperate"
      >
        {{ isPublished ? 'Unpublish' : 'Publish' }}
      </button>
    </div>
    <div class="btn-group me-2" role="group">
        <button class="btn btn-sm btn-outline-secondary" @click="$emit('duplicate')" :disabled="!canOperate">Duplicate</button>
        <button class="btn btn-sm btn-outline-secondary" @click="$emit('repeat')" :disabled="!canOperate">Repeat</button>
    </div>
    <div class="btn-group" role="group">
      <button
        class="btn btn-sm btn-outline-secondary dropdown-toggle"
        type="button"
        data-bs-toggle="dropdown"
        aria-expanded="false"
      >
        More
      </button>
      <ul class="dropdown-menu">
        <li><a class="dropdown-item" href="#" @click.prevent="$emit('export')" :class="{ disabled: !canOperate }">Export to File</a></li>
        <li><a class="dropdown-item" href="#" @click.prevent="$emit('import')">Import from File</a></li>
        <li><hr class="dropdown-divider" /></li>
        <li><a class="dropdown-item text-danger" href="#" @click.prevent="$emit('clear')" :class="{ disabled: !canOperate }">Clear Schedule</a></li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TimelineControls',
  props: {
    isPublished: {
      type: Boolean,
      default: false,
    },
    canOperate: {
        type: Boolean,
        default: false,
    }
  },
  emits: ['toggle-publish', 'duplicate', 'repeat', 'export', 'import', 'clear'],
};
</script>

<style scoped>
.dropdown-item.disabled {
    pointer-events: none;
    opacity: 0.5;
}
</style>