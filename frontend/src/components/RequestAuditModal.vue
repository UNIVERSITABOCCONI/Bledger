<template>
  <Modal
    v-model:open="isModalOpen"
    variant="simple"
    modal-title="Request Audit Verification"
    cta-confirm-label="Request Verification"
    cta-close-label="Cancel"
    cta-open-label="Open"
    button-variant="primary"
    :disable-right-button="disableConfirm"
    @confirm="onConfirm"
    @close="onClose"
  >
    <p v-if="auditorOptions.length > 0">Select an auditor to request verification for your Scope 1 & 2 data</p>

    <div v-if="auditorOptions.length > 0">
      <Dropdown
        ref="auditorDropdown"
        required
        v-model="selectedAuditorId"
        :options="auditorOptions"
        aria-required="true"
        placeholder="Select auditor"
      />
      <small class="small-note"><em>The selected auditor will verify your uploaded data.</em></small>
    </div>

    <div v-else>
      <p>No auditors available in the network. You can request an audit once the admin assigns auditors.</p>
    </div>
  </Modal>

  <!-- Confirmation Modal -->
  <Modal
    v-model:open="showConfirmationModal"
    variant="confirm"
    modal-title="Verification Requested!"
    cta-close-label="Close"
    @close="handleModalClose"
  >
    <p>Your audit verification request has been sent successfully.</p>
  </Modal>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useNetworksStore } from '@/stores/networks'
import Modal from '@/components/modal/Modal.vue'
import Dropdown from '@/components/forms/Dropdown.vue'

const props = defineProps<{
  networkId: string
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'auditRequested': []
}>()

const networksStore = useNetworksStore()

const isModalOpen = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const showConfirmationModal = ref(false)
const selectedAuditorId = ref('')
const auditors = ref([])

const auditorOptions = computed(() => {
  return auditors.value.map(auditor => ({
    label: auditor.companyName,
    value: auditor.companyId
  }))
})

const disableConfirm = computed(() => auditorOptions.value.length === 0)

onMounted(async () => {
  await fetchAuditors()
})

watch(() => props.open, async (newVal) => {
  if (newVal) {
    await fetchAuditors()
    selectedAuditorId.value = ''
  }
})

const fetchAuditors = async () => {
  try {
    const auditorsData = await networksStore.fetchAuditors(props.networkId)
    auditors.value = auditorsData.content || []
  } catch (error) {
    console.error('Error fetching auditors:', error)
  }
}

const onConfirm = async () => {
  if (!selectedAuditorId.value) return

  try {
    await networksStore.requestAudit(props.networkId, { auditorId: selectedAuditorId.value })
    emit('auditRequested')
    showConfirmationModal.value = true
  } catch (error) {
    console.error('Error requesting audit:', error)
  }
}

const onClose = () => {
  isModalOpen.value = false
  selectedAuditorId.value = ''
}

const handleModalClose = () => {
  showConfirmationModal.value = false
  isModalOpen.value = false
}
</script>

<style scoped>
.small-note {
  color: #666;
  font-size: 0.875rem;
}
</style>