<template>
  <div class="input-text-wrapper">
    <label v-if="label" :for="id" class="input-label">{{ label }}</label>
    <input :id="id" :type="type" :placeholder="placeholder" :value="modelValue"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value ?? '')" class="input-field"
      :disabled="disabled" />
    <div v-if="note" class="note">{{ note }}</div>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  type?: 'text' | 'password' | 'email' | 'password_confirmation'
  modelValue: string
  label?: string
  placeholder?: string
  id?: string
  note?: string
  disabled?: boolean
}>(), {
  id: () => `input-${Math.random().toString(36).substr(2, 9)}`,
  label: '',
  placeholder: '',
  modelValue: '',
  type: 'text'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<style scoped>
@import "@/styles/inputText.css";
</style>
