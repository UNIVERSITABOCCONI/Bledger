<template>
  <section class="container page-wrapper">
    <div class="grid gap-4">
      <div class="col-span-16">
        <Panel class="page-panel">
          <div class="panel-head">
            <div class="title-heading-4">Upload documents</div>
          </div>
          <VSpacer size="lg" />
          <div class="panel-content">
            <div class="grid grid-3 gap-8">
              <div class="grid grid-3 gap-4 col-span-2">
                <div class="col-span section-column">
                  <UploadDocumentStatusCard :fileName="globalScopeFileName" :fileSize="globalScopeFileSize" :initialSteps="[
                    { label: 'Verifying', status: 'pending' },
                    { label: 'Uploading', status: 'pending' },
                    { label: 'Data extraction', status: 'pending' }
                  ]" :completeNow="allUploadsDone" @step-completed="label => console.log(`${label} completato`)" @all-done="handleUploadDone" />
                </div>
                <div class="col-span section-column">
                  <UploadDocumentStatusCard :fileName="granularScope1FileName" :fileSize="granularScope1FileSize" :initialSteps="[
                    { label: 'Verifying', status: 'pending' },
                    { label: 'Uploading', status: 'pending' },
                    { label: 'Data extraction', status: 'pending' }
                  ]" :completeNow="allUploadsDone" @step-completed="label => console.log(`${label} completato`)" @all-done="handleUploadDone" />
                </div>
                <div class="col-span section-column">
                  <UploadDocumentStatusCard :fileName="granularScope2FileName" :fileSize="granularScope2FileSize" :initialSteps="[
                    { label: 'Verifying', status: 'pending' },
                    { label: 'Uploading', status: 'pending' },
                    { label: 'Data extraction', status: 'pending' }
                  ]" :completeNow="allUploadsDone" @step-completed="label => console.log(`${label} completato`)" @all-done="handleUploadDone" />
                </div>
              </div>
              <div class="progress col-span-1">
                <h3 class="title-heading-6">Extracted data</h3>
                <LoaderBar v-if="!allUploadsDone" />
                <EmissionTable v-else :data="emissionData" :total="totalEmission" :unit="'tCO₂e'" />
              </div>
            </div>
          </div>
          <VSpacer size="lg" />

          <div class="panel-footer">
            <Button @click="isModalOpen = true" variant="secondary">Delete Data</Button>

            <Modal v-model:open="isModalOpen" variant="alert" cta-confirm-label="Delete" cta-close-label="Cancel"
              cta-open-label="Cancel" modal-title="Are you sure you want to delete this upload?"
              @confirm="handleDeleteData" :onClickClose="() => isModalOpen = false">
            </Modal>

            <Button @click="handleConfirmData" variant="primary">Confirm data</Button>

            <Modal v-model:open="isModalOpen2" variant="confirm" v-if="allUploadsDone" cta-close-label="Close"
              modal-title="Data confirmed!" @close="handleModalClose">
              <p>Data has been successfully confirmed and saved.</p>
            </Modal>
          </div>
        </Panel>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import Panel from "@/components/panel/Panel.vue";
import Button from "@/components/button/Button.vue";
import UploadDocumentStatusCard from "@/components/cards/UploadDocumentStatusCard.vue";
import LoaderBar from "@/components/loader/LoaderBar.vue";
import EmissionTable from "@/components/table/EmissionTable.vue";

import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFileStore } from '@/stores/fileStore'
import Modal from "@/components/modal/Modal.vue";
import VSpacer from "@/components/spacer/VSpacer.vue";

const route = useRoute()
const router = useRouter()
const fileStore = useFileStore()

const isModalOpen = ref(false)
const isModalOpen2 = ref(false)

const uploadFinished = ref(false)

const allUploadsDone = computed(() => uploadFinished.value)
const networkId = computed(() => route.params.networkId as string)
const uploadData = computed(() => fileStore.uploadData)

const globalScopeFileName = computed(() => fileStore.uploadedFiles.globalScopeFile?.name || 'No file')
const globalScopeFileSize = computed(() => {
  const file = fileStore.uploadedFiles.globalScopeFile
  if (!file) return '0kb'
  const size = file.size
  return size < 1024 * 1024 ? `${(size / 1024).toFixed(1)}kb` : `${(size / (1024 * 1024)).toFixed(1)}mb`
})

const granularScope1FileName = computed(() => fileStore.uploadedFiles.granularScope1File?.name || 'No file')
const granularScope1FileSize = computed(() => {
  const file = fileStore.uploadedFiles.granularScope1File
  if (!file) return '0kb'
  const size = file.size
  return size < 1024 * 1024 ? `${(size / 1024).toFixed(1)}kb` : `${(size / (1024 * 1024)).toFixed(1)}mb`
})

const granularScope2FileName = computed(() => fileStore.uploadedFiles.granularScope2File?.name || 'No file')
const granularScope2FileSize = computed(() => {
  const file = fileStore.uploadedFiles.granularScope2File
  if (!file) return '0kb'
  const size = file.size
  return size < 1024 * 1024 ? `${(size / 1024).toFixed(1)}kb` : `${(size / (1024 * 1024)).toFixed(1)}mb`
})

const emissionData = computed(() => {
    if (!uploadData.value) return []
    return uploadData.value.map(item => ({
        key: item.scope_1_2_component,
        value: parseDotFloat(item.total_co2e)
    }))
})

const totalEmission = computed(() => {
   const totalItem = uploadData.value?.find(item => item.scope_1_2_component === 'total_emission')
   return totalItem ? parseDotFloat(totalItem.total_co2e) : NaN  
})


function parseDotFloat(str) {
  if (!/^-?\d+(\.\d+)?$/.test(str.trim())) return NaN;
  return parseFloat(str);
}

onMounted(async () => {
    if (networkId.value) {
       try {
          const files = fileStore.uploadedFiles
          await fileStore.uploadScopeFiles(networkId.value, {
             globalScopeFile: files.globalScopeFile,
             granularScope1File: files.granularScope1File,
             granularScope2File: files.granularScope2File
          })
          // Mark upload as finished: cards will instantly complete
          uploadFinished.value = true
       } catch (error) {
          console.error('Error uploading files:', error)
       }
    }
 })

function handleUploadDone() {
   // no-op: completion is driven by the API upload finishing
}

async function handleConfirmData() {
    if (networkId.value) {
       try {
          await fileStore.confirmFileData(networkId.value)
          isModalOpen2.value = true
       } catch (error) {
          console.error('Error confirming file data:', error)
       }
    }
 }

async function handleDeleteData() {
    if (networkId.value) {
       try {
          await fileStore.deleteFileData(networkId.value)
          router.push({ name: 'network-details', params: { networkId: networkId.value } })
       } catch (error) {
          console.error('Error deleting file data:', error)
       }
    }
 }

function handleModalClose() {
   isModalOpen2.value = false
   router.push({ name: 'network-details', params: { networkId: networkId.value } })
}
</script>