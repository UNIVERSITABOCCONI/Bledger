<template>
  <UploadDocument v-if="step === 'upload'" @next="handleNext" />
  <UploadDocumentStatus v-else />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import UploadDocument from '@/views/UploadDocument.vue'
import UploadDocumentStatus from '@/views/UploadDocumentStatus.vue'

const step = ref<'upload' | 'status'>('upload')
const route = useRoute()

onMounted(() => {
  // Open status directly when routed via /upload-document-status/:networkId
  if (route.name === 'upload-document-status') {
    step.value = 'status'
  }
})

function handleNext() {
  step.value = 'status'
}
</script>