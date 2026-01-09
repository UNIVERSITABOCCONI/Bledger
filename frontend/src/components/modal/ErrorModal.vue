<template>
  <Modal
    v-model:open="isModalOpen"
    variant="error"
    modalTitle="Error"
    ctaCloseLabel="Close"
    @confirm="onConfirm"
    @close="onClose">
    <p>{{ errorMessage }}</p>
  </Modal>
</template>

<script setup>
import { computed } from 'vue'
import Modal from './Modal.vue'
import { useErrorStore } from '@/stores/errorStore'

const errorStore = useErrorStore()

const isModalOpen = computed({
  get: () => errorStore.isErrorModalOpen,
  set: (value) => errorStore.setErrorModalOpen(value)
})

const errorMessage = computed(() => errorStore.errorMessage)

function onConfirm() {
  // Handle confirm action if needed
  errorStore.setErrorModalOpen(false)
}

function onClose() {
  errorStore.setErrorModalOpen(false)
}
</script>