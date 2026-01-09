<template>
     <Panel :tag="currentYear" title="My Scope 1 e 2 file">
          <p>In this section you can access your uploaded Scope1 and Scope2 documents, review the data, and request verification from an auditor</p>

          <VSpacer size="xl" />

          <!-- Show upload section if no files are present -->
          <div v-if="!myNode?.scopeFileName && !myNode?.granularScope1FileName && !myNode?.granularScope2FileName"
               class="upload-section">
               <p>No documentation uploaded for {{ currentYear }}</p>
               <Button variant="primary" @click="handleUploadClick" icon iconPosition="left">
                    <template #icon>
                         <IconUpload />
                    </template>
                    Upload
               </Button>
          </div>

          <!-- Show existing files if they exist -->
          <XBRLCard v-if="myNode?.scopeFileName" metaLabel="Emission details" :title="myNode.scopeFileName"
               :tags="['Upload']" status="request-verification" :requestVerificationLabel="requestVerificationLabel"
               iconType="blue" :uploadFileTransactionId="transactionIdForCards"
               :auditFileTransactionId="myNode.auditFileTransactionId" :auditorId="myNode.auditorId"
               @verify="handleRequestVerification" />

          <XBRLCard v-if="myNode?.granularScope1FileName" metaLabel="Emission details"
               :title="myNode.granularScope1FileName" :tags="['Upload']" status="basic" iconType="outline"
               :uploadFileTransactionId="transactionIdForCards"
               :auditFileTransactionId="myNode.auditFileTransactionId" />

          <XBRLCard v-if="myNode?.granularScope2FileName" metaLabel="Emission details"
               :title="myNode.granularScope2FileName" :tags="['Upload']" status="basic" iconType="outline"
               :uploadFileTransactionId="transactionIdForCards"
               :auditFileTransactionId="myNode.auditFileTransactionId" />

          <div class="align-right" v-if="myNode?.scopeFileName">
               <Button :class="'download-all'" variant="link" @click="$emit('download-all')" icon iconPosition="left">
                    Download All
                    <template #icon>
                         <IconDownload />
                    </template>
               </Button>
          </div>

          <XBRLTotal v-if="transactionIdForCards" :totals="emissions" button-label="Read More" :onclick="handleClickTotalReadMore" />
     </Panel>

     <!-- Request Audit Modal -->
     <RequestAuditModal :network-id="networkId" v-model:open="showRequestAuditModal" @auditRequested="$emit('auditRequested')" />

     <!-- Review Files Modal -->
     <ReviewFilesModal :node-id="props.myNode?.id" :show-buttons="showButtonsRevieFilesModal" v-model:open="showReviewFilesModal" @verified="handleVerified" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import Panel from "@/components/panel/Panel.vue";
import VSpacer from "@/components/spacer/VSpacer.vue";
import XBRLCard from "@/components/cards/XBRLCard.vue";
import IconDownload from "@/components/icons/IconDownload.vue";
import XBRLTotal from "@/components/cards/XBRLTotal.vue";
import Button from "@/components/button/Button.vue";
import IconUpload from "@/components/icons/IconUpload.vue";
import RequestAuditModal from "@/components/RequestAuditModal.vue";
import ReviewFilesModal from "@/components/modal/ReviewFilesModal.vue";

const router = useRouter()

interface NodeDto {
     id: string;
     parentId: string | null;
     companyId: string | null;
     scopeFileId: string | null;
     scopeFileName: string | null;
     granularScope1FileId: string | null;
     granularScope1FileName: string | null;
     granularScope2FileId: string | null;
     granularScope2FileName: string | null;
     nodeStatus: string;
     audited: boolean;
     auditedAt: string | null;
     uploadFileTransactionId: string | null;
     scope3TransactionId: string | null;
     auditFileTransactionId: string | null;
     scope1: string | null;
     scope2: string | null;
     totalScope1AndScope2: string | null;
     nodeDepth: number;
     auditorId: string | null;
}

const props = defineProps<{
       myNode?: NodeDto | null;
       networkId?: string;
 }>();

const emit = defineEmits<{
  'auditRequested': []
  'download-all': []
}>()

const currentYear = new Date().getFullYear().toString();
const showRequestAuditModal = ref(false);
const showReviewFilesModal = ref(false);
const showButtonsRevieFilesModal = ref(false);

const emissions = computed(() => [
      {
           label: 'Scope 1',
           value: props.myNode?.scope1 ?? 0,
           unit: 'tCO₂e'
      },
      {
           label: 'Scope 2',
           value: props.myNode?.scope2 ?? 0,
           unit: 'tCO₂e'
      },
      {
           label: 'Tot.',
           value: props.myNode?.totalScope1AndScope2 ?? 0,
           unit: 'tCO₂e'
      }
]);

const transactionIdForCards = computed(() => {
      return props.myNode?.uploadFileTransactionId || null;
});

const requestVerificationLabel = computed(() => {
      return props.myNode?.uploadFileTransactionId ? 'Request verification' : 'Confirm data';
});

function handleClickTotalReadMore() {
       showReviewFilesModal.value = true;
       showButtonsRevieFilesModal.value = false;
 }

function handleUploadClick() {
       if (props.networkId) {
             router.push({ name: 'upload-document', params: { networkId: props.networkId } })
       }
}

function handleRequestVerification() {
        if (props.myNode?.uploadFileTransactionId) {
              showRequestAuditModal.value = true
        } else {
              showReviewFilesModal.value = true
              showButtonsRevieFilesModal.value = true
        }
}

function handleVerified() {
        showReviewFilesModal.value = false
}
</script>

<style scoped>
.align-right {
     text-align: right;
}

.upload-section {
     text-align: center;
     padding: 2rem 0;
}

.upload-section p {
     margin-bottom: 1rem;
     color: #666;
}

.download-all {
     margin-bottom: 1rem;
}
</style>