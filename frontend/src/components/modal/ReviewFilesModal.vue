<template>
  <Modal
    v-model:open="isOpen"
    size="xl"
    variant="simple"
    modal-title="Verification"
    :cta-close-label="props.showButtons ? (isTpa ? 'Close' : 'Delete Data') : undefined"
    :cta-confirm-label="props.showButtons ? (isTpa ? 'Verify data' : 'Confirm Data') : undefined"
    :disable-right-button="isTpa && !allFilesDownloaded"
    :text-side-button="isTpa ? 'To verify the file, you have to download it first.' : ''"
    @confirm="onConfirm"
    @close="onClose"
  >
    <div class="grid gap-8">
      <div class="col-span-7">
        <XBRLCard
          v-if="nodeDetails?.scopeFile"
          metaLabel="Scope 1 & 2"
          :title="nodeDetails.scopeFile.name"
          :uploadFileTransactionId="nodeDetails.scopeFile.uploadTransactionId"
          status="download"
          downloadLabel="Download"
          iconType="blue"
          @download="downloadFile(nodeDetails.scopeFile.id)"
        />
        <br v-if="nodeDetails?.scopeFile">
        <XBRLCard
          v-if="nodeDetails?.granularScope1File"
          metaLabel="Emission Details (Scope 1)"
          :title="nodeDetails.granularScope1File.name"
          :uploadFileTransactionId="nodeDetails.granularScope1File.uploadTransactionId"
          status="download"
          downloadLabel="Download"
          iconType="blue"
          @download="downloadFile(nodeDetails.granularScope1File.id)"
        />
        <XBRLCard
            v-if="nodeDetails?.granularScope2File"
            metaLabel="Emission Details (Scope 2)"
            :title="nodeDetails.granularScope2File.name"
            :uploadFileTransactionId="nodeDetails.granularScope2File.uploadTransactionId"
            status="download"
            downloadLabel="Download"
            iconType="blue"
            @download="downloadFile(nodeDetails.granularScope2File.id)"
        />
        <br v-if="nodeDetails?.granularScope1File">
      </div>
      <EmissionTable
        class="col-span-9"
        :data="emissionData"
        :total="totalEmission"
        :unit="'tCO₂e'"
      />
    </div>
  </Modal>

      <Modal v-model:open="showSuccessModal" variant="confirm" cta-close-label="Close"
      modal-title="Data verified!" @close="onSuccessClose">
      <p>Data has been successfully verified and saved.</p>
    </Modal>

    <ConfirmOperationModal v-model:open="showConfirmModal" title="Confirm Data Verification" message="Are you sure you want to verify this data?" @confirm="onConfirmModal" />
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Modal from './Modal.vue'
import XBRLCard from '@/components/cards/XBRLCard.vue'
import EmissionTable from '@/components/table/EmissionTable.vue'
import { useNetworksStore } from '@/stores/networks'
import { useCompanyStore } from '@/stores/company'
import { useFileStore } from '@/stores/fileStore'
import api from '@/utils/api'
import { useRoute } from 'vue-router'
import ConfirmOperationModal from '@/components/ConfirmOperationModal.vue'

const props = withDefaults(defineProps<{
  nodeId: string
  open: boolean
  showButtons?: boolean
}>(), {
  showButtons: true
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  'verified': []
}>()
const route = useRoute()
const networkId = computed(() => route.params.networkId as string)

const networksStore = useNetworksStore()
const companyStore = useCompanyStore()
const fileStore = useFileStore()
const isOpen = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const nodeDetails = computed(() => networksStore.nodeDetails)

const isTpa = computed(() => companyStore.company?.companyType === 'TPA')

const emissionData = computed(() => {
  if (!nodeDetails.value?.extractedData) return []
  return nodeDetails.value.extractedData.map(item => ({
    key: item.metadataKey,
    value: parseDotFloat(item.metadataValue)
  }))
})

const totalEmission = computed(() => {
  if (!nodeDetails.value?.extractedData) return 0
  const dataMap = nodeDetails.value.extractedData.reduce((acc, item) => {
    acc[item.metadataKey] = parseDotFloat(item.metadataValue)
    return acc
  }, {})
  return dataMap['total_emission'] || 0
})


function parseDotFloat(str) {
  if (!/^-?\d+(\.\d+)?$/.test(str.trim())) return NaN;
  return parseFloat(str);
}

watch(() => props.open, async (newVal) => {
  if (newVal && props.nodeId) {
    scopeFileDownloaded.value = false;
    granular1FileDownloaded.value = false;
    granular2FileDownloaded.value = false;
    await networksStore.fetchNodeDetails(props.nodeId)
  }
})

async function onConfirm() {
  if (isTpa.value) {
    showConfirmModal.value = true
  } else {
    try {
      await fileStore.confirmFileData(networkId.value)
      networksStore.fetchNetworkDetails(networkId.value)
      showSuccessModal.value = true
      isOpen.value = false
    } catch (error) {
      console.error('Error confirming data:', error)
    }
  }
}

async function onConfirmModal() {
  try {
    await networksStore.verifyTpaData(props.nodeId)
    networksStore.fetchNetworkDetails(networkId.value, true)
    showSuccessModal.value = true
    isOpen.value = false
  } catch (error) {
    console.error('Error verifying data:', error)
  }
}

async function onClose() {
  if (props.showButtons && !isTpa.value) {
    try {
      await fileStore.deleteFileData(networkId.value)
      networksStore.fetchNetworkDetails(networkId.value)
      // Maybe navigate or emit something
    } catch (error) {
      console.error('Error deleting data:', error)
    }
  }
  isOpen.value = false
}

function onSuccessClose() {
  showSuccessModal.value = false
  emit('verified')
}

const scopeFileDownloaded = ref(false);
const granular1FileDownloaded = ref(false);
const granular2FileDownloaded = ref(false);
const showSuccessModal = ref(false);
const showConfirmModal = ref(false);

const allFilesDownloaded = computed(() => scopeFileDownloaded.value && granular1FileDownloaded.value && granular2FileDownloaded.value)

async function downloadFile(fileId: string) {
  try {
    let type = '';

    if(fileId == nodeDetails.value.scopeFile.id){
      type = 'GLOBAL_SCOPE_FILE';
    } else if (fileId == nodeDetails.value.granularScope1File.id){
      type = 'GRANULAR_SCOPE_1'
    } else if (fileId == nodeDetails.value.granularScope2File.id){
      type = 'GRANULAR_SCOPE_2'
    }


    const response = await api.get(`/common/file/download/${props.nodeId}/${type}`, {
      responseType: 'blob'
    })

    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `file-${fileId}.csv`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)

    if(fileId == nodeDetails.value.scopeFile.id){
      scopeFileDownloaded.value = true;
    } else if (fileId == nodeDetails.value.granularScope1File.id){
      granular1FileDownloaded.value = true;
    } else if (fileId == nodeDetails.value.granularScope2File.id){
      granular2FileDownloaded.value = true;
    }
  } catch (error) {
    console.error('Error downloading file:', error)
  }
}
</script>