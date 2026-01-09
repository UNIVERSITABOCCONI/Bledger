<template>
  <div class="progress-bar-container">
    <div v-if="showValue" class="progress-bar-value">{{ clampedProgress }}%</div>

    <div
        class="progress-bar"
        role="progressbar"
        :aria-valuemin="0"
        :aria-valuemax="100"
        :aria-valuenow="clampedProgress"
        :aria-valuetext="clampedProgress + '%'"
        :aria-label="ariaLabel || copy"
    >
      <div
          class="progress-bar-indicator"
          :style="{ width: clampedProgress + '%' }"
      ></div>
    </div>

    <p class="progress-bar-copy">{{ copy }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  progress: number
  copy: string
  ariaLabel?: string
  showValue?: boolean
}>(), {
  showValue: true
})

const clampedProgress = computed(() => {
  const v = Number(props.progress)
  if (isNaN(v)) return 0
  return Math.min(100, Math.max(0, v))
})

</script>

<style scoped>
@import "@/styles/progressBar.css";
</style>
