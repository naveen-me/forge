<template>
  <section class="log-panel" v-if="messages.length > 0">
    <div class="container-fluid">
      <div class="row">
        <div class="col">
          <h2 class="mb-4">System Log</h2>
          <div class="card">
            <div class="card-body">
              <div class="log-container">
                <table class="table table-striped">
                  <thead>
                    <tr>
                      <th scope="col">Time</th>
                      <th scope="col">Type</th>
                      <th scope="col">Message</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="message in messages" :key="message.timestamp">
                      <td>{{ formatTime(message.timestamp) }}</td>
                      <td>
                        <span :class="message.type === 'error' ? 'text-danger fw-bold' : 'text-primary'">
                          {{ message.type.toUpperCase() }}
                        </span>
                      </td>
                      <td>
                        <span :class="message.type === 'error' ? 'text-danger' : ''">
                          {{ message.text }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import { inject } from 'vue'

export default {
  name: 'LogPanel',
  setup() {
    const messages = inject('messages')

    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleTimeString()
    }

    return {
      messages,
      formatTime
    }
  }
}
</script>

<style scoped>
.log-panel {
  margin: 30px 0;
}

.log-container {
  max-height: 400px;
  overflow-y: auto;
}
</style>