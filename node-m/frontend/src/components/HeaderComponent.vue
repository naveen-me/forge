<template>
  <header class="app-header">
    <div class="container-fluid">
      <div class="row align-items-center">
        <div class="col">
          <h1 class="display-4 mb-0">Tarva Engine Playout System</h1>
        </div>
        <div class="col-auto">
          <button
            :class="connected ? 'btn btn-success' : 'btn btn-danger'"
            class="btn rounded-pill"
          >
            {{ connected ? 'Connected' : 'Disconnected' }}
          </button>
        </div>
      </div>

      <div class="row mt-3">
        <div class="col">
          <div class="card shadow-sm">
            <div class="card-body">
              <div class="d-flex align-items-center">
                <i :class="connected ? 'fas fa-circle text-success me-2' : 'fas fa-circle text-danger me-2'"></i>
                <span class="fw-medium">{{ connected ? 'Connected to Private Tarva Engine Session' : 'Disconnected' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import { inject } from 'vue'

export default {
  name: 'HeaderComponent',
  setup() {
    const connected = inject('connected')
    return {
      connected
    }
  }
}
</script>

<style scoped>
.app-header {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  background-color: #f8f9fa;
}

/* Dark theme support */
@media (prefers-color-scheme: dark) {
  .app-header {
    background-color: #333;
    border-bottom: 1px solid #555;
  }

  .card {
    background-color: #2d2d2d;
    border: 1px solid #555;
  }
}
</style>