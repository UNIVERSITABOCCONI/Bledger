<template>
  <div class="hamburger-menu">
    <button @click="isOpen = !isOpen" class="hamburger-btn" :class="{ open: isOpen }">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
        <title>menu</title>
        <path d="M3,6H21V8H3V6M3,11H21V13H3V11M3,16H21V18H3V16Z" />
      </svg>
    </button>
    <div v-if="isOpen" class="mobile-menu">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const isOpen = ref(false)
</script>

<style scoped>
.hamburger-menu {
  position: relative;
}

.hamburger-btn {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.hamburger-btn svg {
  fill: #666;
}

.hamburger-btn:hover svg {
  fill: var(--Blue-2, #0046AE);
}

.mobile-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 0.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 1rem;
  z-index: 1000;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .hamburger-btn {
    display: block;
  }
}
</style>