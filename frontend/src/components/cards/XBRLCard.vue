<template>
  <div class="file-card" :class="cardStatusClass">
    <!-- Icona file -->
    <div class="file-card-icon">
      <slot name="icon">
        <IconFile class="icon-file" v-if="iconType === 'blue'" />
        <IconFileOutline v-else />
      </slot>
    </div>

    <!-- Contenuto principale -->
    <div class="file-card-body">
      <p class="file-card-meta">{{ metaLabel }}</p>
      <h4 class="file-card-title">{{ title }}</h4>


      <ul class="file-card-tags">
        <li class="file-card-tag">
          <template v-if="uploadFileTransactionId || auditFileTransactionId">
              <TransactionStatus v-if="uploadFileTransactionId" :transactionId="uploadFileTransactionId" text="Uploaded" />
              <TransactionStatus v-if="auditFileTransactionId" :transactionId="auditFileTransactionId" text="Verified"/>
 
          </template>
          <template v-else>
            <IconPolygon class="icon-polygon" />
          </template>
        </li>
      </ul>
    </div>

    <!-- Azioni / Stato -->
    <div class="file-card-status">
      <template v-if="status === 'pending'">
        <IconRefresh class="icon icon--spin" />
        <p class="status-label" v-if="status === 'pending'" v-html="pendingLabel" />
      </template>

      <template v-else-if="status === 'verified'">
        <IconCheck class="icon icon--success" />
        <div>
          <div class="status-label" v-html="verifiedLabel" />
          <div class="status-date" v-html="verifiedDate" />
        </div>
      </template>
    </div>

    <div class="file-card-actions" v-if="status === 'download' || (status === 'request-verification' && !auditorId)">
      <template v-if="status === 'download'">
        <Button class="cta" variant="primary" icon icon-position="left" @click="$emit('download')">
          <template #icon><IconDownload class="icon-download" /></template>
          {{ downloadLabel }}
        </Button>
      </template>

      <template v-else-if="status === 'request-verification' && !auditorId">
        <Button class="cta" variant="secondary" @click="$emit('verify')">
          {{ requestVerificationLabel }}
        </Button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import Button from '@/components/button/Button.vue'
import IconFileOutline from '@/components/icons/IconFileOutline.vue'
import IconRefresh from '@/components/icons/IconInProgress.vue'
import IconCheck from '@/components/icons/IconChecked.vue'
import IconDownload from '@/components/icons/IconDownload.vue'
import IconFile from "@/components/icons/IconFile.vue";
import { computed } from "vue";
import IconPolygon from "@/components/icons/IconPolygon.vue";
import TransactionStatus from "@/components/TransactionStatus.vue";

const props = defineProps<{
  metaLabel: string
  title: string
  tags?: string[]
  status: 'pending' | 'verified' | 'download' | 'request-verification' | 'basic'
  pendingLabel?: string
  verifiedLabel?: string
  verifiedDate?: string
  downloadLabel?: string
  requestVerificationLabel?: string
  iconType?: 'blue' | 'outline'
  uploadFileTransactionId?: string | null
  auditFileTransactionId?: string | null
  auditorId?: string | null
}>()

const emit = defineEmits<{
  (e: 'download'): void
  (e: 'verify'): void
}>()

const cardStatusClass = computed(() => {
  return `status--${props.status}`
})
</script>


<style scoped>
@import "@/styles/xbrlCard.css";

.transaction-status-container {
  display: flex;
  gap: 8px;
  align-items: center;
}

.transaction-status-container .transaction-status {
  margin: 0;
}
</style>
