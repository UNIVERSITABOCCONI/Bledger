<template>
  <div class="action-bar">
    <div class="action-bar-start">
      <slot />
    </div>

    <div class="action-bar-end">
      <slot name="right-side" />
    </div>
  </div>
</template>

<style scoped>
@import "@/styles/actionBar.css";
</style>