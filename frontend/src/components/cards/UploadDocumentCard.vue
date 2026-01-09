<template>
  <!-- Upload panel -->
  <div
      class="panel file-upload-card-panel"
      :class="{ 'hide-panel': fileSelected }"
      @dragenter.prevent
      @dragover.prevent
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
  >
    <IconUploadPremium class="file-upload-card-icon" />
    <div class="choose-text">{{ chooseFileLabel }}</div>
    <div class="drop-text">Or drop it here</div>

    <div class="file-upload-card-panel-footer">
      <div class="info-text">
        Supported file: <strong>.CSV</strong><br />
        Max. dimension: <strong>5mb</strong>
      </div>
      <input
          type="file"
          accept=".csv"
          class="hidden-input"
          ref="fileInput"
          @change="handleFile"
      />
    </div>
  </div>

  <!-- file selected panel -->
  <Panel class="file-selected-card-panel" :class="{ 'hide-panel': !fileSelected }">
    <div class="file-selected-card-panel-head">
      <IconFile class="file-selected-card-icon" />
      <p>File name: <strong>{{ fileName }}</strong></p>
      <p class="semibold">{{ fileSize }}</p>
    </div>

    <div class="file-selected-card-panel-footer">
      <RouterLink @click="triggerFileInput" class="guidelines-text" to="">
        {{ otherFileCtaLabel }}
      </RouterLink>
    </div>
  </Panel>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Panel from '@/components/panel/Panel.vue';
import IconFile from '@/components/icons/IconFile.vue';
import IconUploadPremium from '@/components/icons/IconUploadPremium.vue';

interface Step {
  label: string;
  status: 'pending' | 'in-progress' | 'done';
}

const props = defineProps<{
  fileName: string;
  handleGuidelinesClick?: () => void;
  guidelinesLabel?: string;
  otherFileCtaLabel: string;
  chooseFileLabel: string;
  initialSteps?: Step[];
}>();

const emit = defineEmits<{
  fileSelected: [file: File | null]
}>()

const fileInput = ref<HTMLInputElement | null>(null);
const fileSelected = ref(false);
const fileName = ref('');
const fileSize = ref('');

function triggerFileInput() {
  fileInput.value?.click();
}

function formatFileSize(size: number): string {
  const mb = size / (1024 * 1024);
  if (mb < 1) {
    const kb = size / 1024;
    return `${kb.toFixed(1)}kb`;
  }
  return `${mb.toFixed(1)}mb`;
}

function handleFile(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (file && file.type === 'text/csv' && file.size <= 5 * 1024 * 1024) {
    console.log('File selezionato:', file);
    fileName.value = file.name;
    fileSize.value = formatFileSize(file.size);
    fileSelected.value = true;
    emit('fileSelected', file);
  }
}

function handleDrop(event: DragEvent) {
  const file = event.dataTransfer?.files[0];
  if (file && file.type === 'text/csv' && file.size <= 5 * 1024 * 1024) {
    console.log('File droppato:', file);
    fileName.value = file.name;
    fileSize.value = formatFileSize(file.size);
    fileSelected.value = true;
    emit('fileSelected', file);
  }
}
</script>

<style scoped>
@import "@/styles/uploadDocumentCard.css";
</style>