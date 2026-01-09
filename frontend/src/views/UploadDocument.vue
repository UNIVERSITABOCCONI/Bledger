<template>
  <section class="container page-wrapper">
    <div class="grid gap-4">
      <div class="col-span-12 col-start-3">
        <Panel class="page-panel">
          <div class="panel-head">
            <div class="title-heading-4">Upload documents</div>
          </div>
          <VSpacer size="lg" />
          <div class="panel-content">
            <div class="grid grid-3 gap-4">
              <div class="col-span section-column">
                <div class="text-container">
                  <p><span class="label">1. Scope 1 & 2 documents</span><br>
                  This document must contain the data regarding Scope 1 & 2.</p>
                  </div>

                <div class="guidelines-text"><a href="/templates/scope_1_2_data_facts.csv" download>Download template</a></div>
              </div>
              <div class="col-span section-column">
                <div class="text-container">
                <p><span class="label">2. Emission details (SCOPE 1)</span><br>
                  This document must contain the explanation of data contained in Scope 1 & 2 document</p>
                </div>
                <div class="guidelines-text"><a href="/templates/granular_scope_1_data_facts.csv" download>Download template</a></div>
              </div>
              <div class="col-span section-column">
                <div class="text-container">
                <p><span class="label">3. Emission details (SCOPE 2)</span><br>
                  This document must contain the explanation of data contained in Scope 1 & 2 document</p>
                </div>
                <div class="guidelines-text"><a href="/templates/granular_scope_2_data_facts.csv" download>Download template</a></div>
              </div>
            </div>
            <div class="grid grid-3 gap-4">
              <div class="col-span section-column">
                <UploadDocumentCard fileName="report.csv" other-file-cta-label="Choose another file"
                  choose-file-label="Choose a file" @file-selected="handleScopeFileSelected" />
              </div>
              <div class="col-span section-column">
                <UploadDocumentCard fileName="report.csv" other-file-cta-label="Choose another file"
                  choose-file-label="Choose a file" @file-selected="handleGranularScope1FileSelected" />
              </div>
              <div class="col-span section-column">
                <UploadDocumentCard fileName="report.csv" other-file-cta-label="Choose another file"
                  choose-file-label="Choose a file" @file-selected="handleGranularScope2FileSelected" />
              </div>
            </div>
          </div>
          <VSpacer size="lg" />
          <div class="panel-footer">
            <Button @click="handleCancel" variant="secondary">Cancel</Button>

            <Button variant="primary" @click="handleNextStep" :disabled="!allFilesSelected">Next Step</Button>
          </div>
        </Panel>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import UploadDocumentCard from "@/components/cards/UploadDocumentCard.vue";
import Panel from "@/components/panel/Panel.vue";
import Button from "@/components/button/Button.vue";
import { useFileStore } from '@/stores/fileStore'
import VSpacer from "@/components/spacer/VSpacer.vue";
import { ref, computed } from "vue";
import { useRoute, useRouter } from 'vue-router'

const fileStore = useFileStore()
const emit = defineEmits(['next'])
const route = useRoute()
const router = useRouter()

// File storage
const scopeFile = ref<File | null>(null)
const granularScope1File = ref<File | null>(null)
const granularScope2File = ref<File | null>(null)

const allFilesSelected = computed(() => scopeFile.value && granularScope1File.value && granularScope2File.value)

function handleNextStep() {
  // Store the uploaded files in the store
  fileStore.setUploadedFiles({
    globalScopeFile: scopeFile.value,
    granularScope1File: granularScope1File.value,
    granularScope2File: granularScope2File.value,
  })

  // Notify parent (UploadDocumentSuper) to move to status step
  emit('next')
}


function handleCancel() {
  router.push('/network-details/' + route.params.networkId)
}


function handleScopeFileSelected(file: File | null) {
  scopeFile.value = file
}

function handleGranularScope1FileSelected(file: File | null) {
  granularScope1File.value = file
}

function handleGranularScope2FileSelected(file: File | null) {
  granularScope2File.value = file
}
</script>

<style scoped>
@import "@/styles/uploadDocumentCard.css";

.text-container {
  min-height: 120px;
}

.guidelines-text {
  text-align: center;
  margin-bottom: 10px;
}
</style>