<template>
  <Modal
    v-model:open="isModalOpen"
    variant="simple"
    :modal-title="title"
    cta-confirm-label="Confirm"
    cta-close-label="Cancel"
    @confirm="onConfirm"
    @close="onClose"
  >
    <p>{{ message }}</p>
  </Modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Modal from '@/components/modal/Modal.vue'

const props = defineProps<{
  open: boolean
  title: string
  message: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'confirm': []
  'close': []
}>()

const isModalOpen = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const onConfirm = () => {
  emit('confirm')
}

const onClose = () => {
  emit('close')
}
</script>

<style scoped>
</style>