<template>
  <div class="modal fade" ref="modalElement" tabindex="-1" aria-labelledby="infoModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="infoModalLabel">{{ title }}</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <p>{{ content }}</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click="$emit('close')">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { onMounted, ref } from 'vue'
import { Modal } from 'bootstrap'

export default {
  name: 'InfoModal',
  props: {
    title: {
      type: String,
      required: true
    },
    content: {
      type: String,
      required: true
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    let modalInstance = null
    const modalElement = ref(null)

    onMounted(() => {
      modalInstance = new Modal(modalElement.value)
      modalInstance.show()

      // Listen for the hidden event to emit close
      modalElement.value.addEventListener('hidden.bs.modal', () => {
        emit('close')
      })
    })

    return {
      modalElement
    }
  }
}
</script>
