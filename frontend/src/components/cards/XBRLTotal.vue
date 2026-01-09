<template>
  <div class="file-card">
    <!-- Lista Totali -->
    <ul class="file-card-total-list">
      <li
          v-for="item in totals"
          :key="item.label"
          class="file-card-total-item"
      >
        {{ item.label }}
        <div class="right">
          {{ formatValue(item.value) }} <small>{{ item.unit || '' }}</small>
        </div>
      </li>
    </ul>

    <!-- Bottone opzionale -->
    <div v-if="buttonLabel" class="align-right">
      <Button
          variant="secondary"
          @click="handleClick"
          :aria-label="buttonLabel"
      >
        {{ buttonLabel }}
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import Button from "@/components/button/Button.vue"

interface TotalItem {
  label: string
  value: number | string
  unit?: string
}

const props = defineProps<{
  totals: TotalItem[]
  buttonLabel?: string
}>()

const emit = defineEmits<{
  (e: 'button-click', event: MouseEvent): void
}>()

function formatValue(value: number | string): string {
  const numValue = typeof value === 'string' ? parseFloat(value) : value
  return isNaN(numValue) ? (typeof value === 'string' ? value : value.toString()) : numValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function handleClick(e: MouseEvent) {
  emit('button-click', e)
}
</script>

<style scoped>
@import "@/styles/xbrlTotal.css";
</style>
