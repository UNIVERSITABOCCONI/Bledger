<template>
  <div class="input-select-wrapper">
    <label v-if="label" :for="id" class="input-label">{{ label }}</label>
    <select
        :id="id"
        :value="modelValue"
        @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value ?? '')"
        class="select-field"
        model-value="{{ modelValue }}"
        options="{{ options }}">
      <option v-if="placeholder" disabled value="">{{ placeholder }}</option>
      <option
          v-for="option in options"
          :key="option.value"
          :value="option.value"
      >
        {{ option.label }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
// Tipo per una singola opzione
interface SelectOption {
  label: string
  value: string
}

const props = withDefaults(defineProps<{
  modelValue: string
  label?: string
  placeholder?: string
  id?: string
  options: SelectOption[]
}>(), {
  placeholder: 'Seleziona un’opzione',
  id: () => `select-${Math.random().toString(36).substr(2, 9)}`,
  modelValue: ''
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<style scoped>
@import "@/styles/select.css";
</style>
