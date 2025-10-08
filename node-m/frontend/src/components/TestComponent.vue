<template>
  <div class="test-component">
    <h2>Test Component</h2>
    <button class="btn btn-primary">Test Button</button>
    <div class="card" style="margin-top: 20px">
      <div class="card-body">
        <h5 class="card-title">Test Card</h5>
        <p class="card-text">This is a test card to check if Bootstrap styles are working.</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TestComponent',
}
</script>

<style scoped>
.test-component {
  padding: 20px;
  border: 1px solid #ccc;
  margin: 20px;
}
</style>