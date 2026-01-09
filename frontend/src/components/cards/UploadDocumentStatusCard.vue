<template>
  <!-- Status panel -->
  <Panel class="file-status-card-panel">
    <div class="file-status-card-panel-head">
      <IconFile class="file-status-card-icon" />
      <p>File name: <strong>{{ fileName }}</strong></p>
      <p class="semibold">{{ fileSize }}</p>
    </div>

    <ul class="file-status-verify-list">
      <li
          v-for="(step, index) in steps"
          :key="step.label"
          class="file-status-verify-list-item"
      >
        <span>{{ step.label }}</span>
        <StatusIcon class="icon" :status="step.status" />
      </li>
    </ul>
  </Panel>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import StatusIcon from './StatusIcon.vue'
import Panel from '@/components/panel/Panel.vue'
import IconFile from "@/components/icons/IconFile.vue"

type StepStatus = 'pending' | 'in-progress' | 'done'

interface Step {
  label: string
  status: StepStatus
}

// Props
const props = withDefaults(defineProps<{
  fileName?: string
  fileSize?: string
  initialSteps?: Step[]
  completeNow?: boolean
}>(), {
  fileName: 'Unnamed file',
  fileSize: 'Unknown size',
  initialSteps: () => [
    { label: 'Verifying', status: 'pending' },
    { label: 'Uploading', status: 'pending' },
    { label: 'Data extraction', status: 'pending' }
  ],
  completeNow: false,
})

// Eventi
const emit = defineEmits<{
  (e: 'step-completed', stepLabel: string): void
  (e: 'all-done'): void
}>()

const steps = ref<Step[]>(structuredClone(props.initialSteps))
let timer: number | null = null

function runSteps() {
  let i = 0
  timer = window.setInterval(() => {
    if (i > 0) steps.value[i - 1].status = 'done'

    if (i < steps.value.length) {
      steps.value[i].status = 'in-progress'
      emit('step-completed', steps.value[i].label)
    } else {
      if (timer !== null) {
        clearInterval(timer)
        timer = null
      }
      // ensure last step is marked done
      if (steps.value.length > 0) {
        steps.value[steps.value.length - 1].status = 'done'
      }
      emit('all-done')
    }

    i++
  }, 1000)
}

function finishAll() {
  // mark all steps as done immediately
  steps.value.forEach((s) => (s.status = 'done'))
  if (timer !== null) {
    clearInterval(timer)
    timer = null
  }
  emit('all-done')
}

onMounted(() => {
  if (props.completeNow) {
    finishAll()
  } else {
    runSteps()
  }
})

onUnmounted(() => {
  if (timer !== null) {
    clearInterval(timer)
    timer = null
  }
})

watch(
  () => props.completeNow,
  (val) => {
    if (val) finishAll()
  },
)
</script>

<style scoped>
@import "@/styles/uploadDocumentStatusCard.css";
</style>
