<template>
  <footer class="app-footer mt-auto">
    <p>Tarva Engine Playout System &copy; {{ new Date().getFullYear() }} - Industrial Grade Solution</p>
  </footer>
</template>

<script>
export default {
  name: 'FooterComponent'
}
</script>

<style scoped>
.app-footer {
  text-align: center;
  padding: 20px;
  border-top: 1px solid #eee;
  color: #7f8c8d;
  background-color: #f8f9fa;
}

/* Dark theme support */
@media (prefers-color-scheme: dark) {
  .app-footer {
    background-color: #333;
    border-top: 1px solid #555;
    color: #aaa;
  }
}
</style>