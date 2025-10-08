<template>
  <section class="file-list" v-if="addedFiles.length > 0">
    <div class="card">
      <div class="card-header">
        <h2 class="mb-0">Played Scenes</h2>
      </div>
      <div class="card-body">
        <ul class="list-group">
          <li class="list-group-item" v-for="(file, index) in addedFiles" :key="file.filePath">
            <div class="d-flex align-items-center">
              <div class="me-3">
                <i class="bx bx-video"></i>
              </div>
              <div class="flex-grow-1">
                <div class="fw-bold">{{ file.sceneName }}</div>
                <small class="text-muted">{{ file.filePath }}</small>
              </div>
              <span class="badge bg-primary rounded-pill">{{ index + 1 }}</span>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script>
import { inject } from 'vue'

export default {
  name: 'FileList',
  setup() {
    const addedFiles = inject('addedFiles')

    return {
      addedFiles
    }
  }
}
</script>

<style scoped>
.file-list {
  margin: 30px 0;
}
</style>