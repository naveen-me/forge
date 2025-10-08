import { reactive, provide, inject } from 'vue'

// Global state management
export const useAppState = () => {
  const state = reactive({
    connected: false,
    messages: [],
    addedFiles: [],
    previewAvailable: false
  })

  // Provide state to child components
  provide('connected', state.connected)
  provide('messages', state.messages)
  provide('addedFiles', state.addedFiles)
  provide('previewAvailable', state.previewAvailable)

  return state
}

// Composable for handling Tarva Engine connection
export const useTarvaEngineConnection = (state) => {
  const updateConnectionStatus = (connected) => {
    state.connected = connected
    state.messages.unshift({
      type: 'info',
      text: connected ? 'Successfully connected to Tarva Engine' : 'Disconnected from Tarva Engine',
      timestamp: new Date()
    })
  }

  return {
    updateConnectionStatus
  }
}

// Composable for handling messages
export const useMessaging = (state) => {
  const addMessage = (type, text) => {
    state.messages.unshift({
      type,
      text,
      timestamp: new Date()
    })
  }

  const clearMessages = () => {
    state.messages = []
  }

  return {
    addMessage,
    clearMessages
  }
}

// Composable for file handling
export const useFileManagement = (state) => {
  const addFile = (fileData) => {
    state.addedFiles.push(fileData)
  }

  const removeFile = (filePath) => {
    const index = state.addedFiles.findIndex(f => f.filePath === filePath)
    if (index !== -1) {
      state.addedFiles.splice(index, 1)
    }
  }

  return {
    addFile,
    removeFile
  }
}

// Composable for preview handling
export const usePreview = (state) => {
  const setPreviewAvailable = (available) => {
    state.previewAvailable = available
  }

  return {
    setPreviewAvailable
  }
}