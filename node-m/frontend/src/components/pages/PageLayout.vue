<template>
  <div class="page-layout">
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: 'PageLayout'
}
</script>

<style scoped>
.page-layout {
  border-left: 1px solid var(--menu-bar-bg-color);
  border-top: 1px solid var(--menu-bar-bg-color);
  border-top-left-radius: 10px;
  padding: 20px;
  height: 100vh;
  overflow-y: auto;
}

:root {
  --menu-bar-bg-color: #f0f0f0;
}

@media (prefers-color-scheme: dark) {
  :root {
    --menu-bar-bg-color: #2d2d2d;
  }
}
</style>
