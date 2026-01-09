<template>
  <div class="badge">
    <span class="badge-label">{{ label }}</span>
    <button
        v-if="hasRemoveListener"
        class="remove-btn"
        @click="emit('remove')"
        aria-label="Remove"
    >
      &times;
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance } from 'vue'

const emit = defineEmits<{
  (e: 'remove'): void
}>()

const props = defineProps<{
  label: string
}>()

// Controlla se il parent ha collegato un listener "remove"
const instance = getCurrentInstance()
const hasRemoveListener = computed(() => {
  return !!instance?.vnode.props?.onRemove
})
</script>

<style scoped>
@import "@/styles/badge.css";
</style>
