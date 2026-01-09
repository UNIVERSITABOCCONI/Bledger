<template>
  <Modal
    v-model:open="isModalOpen"
    variant="simple"
    modal-title="Add auditor"
    cta-confirm-label="Confirm"
    cta-close-label="Cancel"
    cta-open-label="Open"
    button-variant="primary"
    @confirm="onConfirm"
    @close="onClose"
  >
    <p>Select the Auditor for this network</p>

    <div>
      <Dropdown
        ref="companyDropdown"
        required
        v-model="dropdownCompany"
        :options="optionsCompany"
        aria-required="true"
        placeholder="Select company"
        @change="onAuditorSelect"
      />
      <small class="small-note"><em>The auditor will be responsible for validating Scope 1 & 2 data.</em></small>
    </div>
    <VSpacer size="sm" />
    <div class="flex gap-2 justify-start flex-wrap">
      <Badge
        v-for="auditor in selectedAuditors"
        :key="auditor.companyId"
        :label="auditor.companyName"
        remove-label="Remove"
        @remove="handleBadgeRemove(auditor.companyId)"
      />
    </div>
  </Modal>

  <!-- Confirmation Modal -->
  <Modal
    v-model:open="showConfirmationModal"
    variant="confirm"
    modal-title="Data confirmed!"
    cta-close-label="Close"
    @close="handleModalClose"
  >
    <p>Auditors have been successfully added to the network.</p>
  </Modal>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useNetworksStore } from '@/stores/networks'
import { useCompanyStore } from '@/stores/company'
import Modal from '@/components/modal/Modal.vue'
import Dropdown from '@/components/forms/Dropdown.vue'
import Badge from '@/components/badge/Badge.vue'
import VSpacer from '@/components/spacer/VSpacer.vue'

const props = defineProps<{
  networkId: string
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const networksStore = useNetworksStore()
const companyStore = useCompanyStore()

const isModalOpen = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const showConfirmationModal = ref(false)
const dropdownCompany = ref('')
const selectedAuditors = ref([])
const availableAuditors = ref([])
const existingAuditors = ref([])

const optionsCompany = computed(() => {
  return availableAuditors.value
    .filter(auditor => !selectedAuditors.value.find(selected => selected.companyId === auditor.companyId))
    .map(auditor => ({
      label: auditor.companyName,
      value: auditor.companyId
    }))
})

onMounted(async () => {
  // Fetch current auditors
  await fetchCurrentAuditors()
  // Fetch available TPA companies
  await fetchAvailableAuditors()
})

const fetchCurrentAuditors = async () => {
  try {
    const auditors = await networksStore.fetchAuditors(props.networkId)
    existingAuditors.value = auditors.content || []
    selectedAuditors.value = [...existingAuditors.value]
  } catch (error) {
    console.error('Error fetching current auditors:', error)
  }
}

const fetchAvailableAuditors = async () => {
  try {
    const auditors = await companyStore.fetchCompanyNamesByType('TPA')
    availableAuditors.value = auditors || []
  } catch (error) {
    console.error('Error fetching available auditors:', error)
  }
}

const onAuditorSelect = () => {
  if (dropdownCompany.value) {
    const selected = availableAuditors.value.find(a => a.companyId === dropdownCompany.value)
    if (selected && !selectedAuditors.value.find(a => a.companyId === selected.companyId)) {
      selectedAuditors.value.push(selected)
    }
    dropdownCompany.value = ''
  }
}

const handleBadgeRemove = (companyId: string) => {
  // Only allow removing newly added auditors, not existing ones
  const isExisting = existingAuditors.value.find(a => a.companyId === companyId)
  if (!isExisting) {
    selectedAuditors.value = selectedAuditors.value.filter(a => a.companyId !== companyId)
  }
}

const onConfirm = async () => {
  try {
    // Only send newly added auditors, not existing ones
    const newAuditors = selectedAuditors.value.filter(auditor =>
      !existingAuditors.value.find(existing => existing.companyId === auditor.companyId)
    )
    const auditorIds = newAuditors.map(a => a.companyId)

    if (auditorIds.length > 0) {
      await networksStore.addAuditors(props.networkId, { auditorIds })
      showConfirmationModal.value = true
    } else {
      // No new auditors to add
      isModalOpen.value = false
    }
  } catch (error) {
    console.error('Error adding auditors:', error)
  }
}

const onClose = () => {
  isModalOpen.value = false
  dropdownCompany.value = ''
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