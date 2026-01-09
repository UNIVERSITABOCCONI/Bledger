<template>

    <!-- Loading state for PENDING or FAIL_PENDING -->
    <template v-if="isLoading">
      <div class="status-container">
        <div><IconInProgress class="icon-in-progress" /></div>
        <div class="status-details">
          <div>{{ text }}</div>
          <div>{{ formattedCreatedDate }}</div>
        </div>
      </div>
    </template>

    <!-- Error state for FAILED -->
    <template v-else-if="isFailed">
      <div class="status-container">
        <div><IconError class="icon icon-error" /></div>
        <div class="status-details">
          <div>{{ text }}</div>
          <div>{{ formattedCreatedDate }}</div>
        </div>
      </div>
    </template>

    <!-- Success state for CONFIRMED -->
    <template v-else-if="isConfirmed">
      <div class="status-container">
        <div><IconPolygon class="icon-polygon" @click="openPolygonScan" /></div>
        <div class="status-details">
          <div @click="openPolygonScan">{{ text }}</div>
          <div>{{ formattedConfirmedDate }}</div>
        </div>
      </div>
    </template>

</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import IconInProgress from '@/components/icons/IconInProgress.vue'
import IconError from '@/components/icons/IconError.vue'
import IconPolygon from '@/components/icons/IconPolygon.vue'
import { useTransactionStore } from '@/stores/transaction'

const props = defineProps<{
  transactionId?: string,
  text: string,
  status?: string,
  txHash?: string,
  verify?: boolean,
}>()

const store = useTransactionStore()
const transaction = ref(null)
const pollingInterval = ref(null)

// Use polled data if available, otherwise use props for initialization
const currentStatus = computed(() => transaction.value?.status || props.status)
const currentTxHash = computed(() => transaction.value?.txHash || props.txHash)

const isLoading = computed(() => {
  const status = currentStatus.value
  return status === 'PENDING' || status === 'FAIL_PENDING'
})

const isFailed = computed(() => currentStatus.value === 'FAILED')

const isConfirmed = computed(() => currentStatus.value === 'CONFIRMED')

const formattedConfirmedDate = computed(() => {
  const v = transaction.value?.confirmedAt
  if (!v) return ''
  const d = new Date(v) // works with ms timestamp or ISO string
  if (isNaN(d as unknown as number)) return ''
  return new Intl.DateTimeFormat('it-IT', {
    timeZone: 'Europe/Rome',
  }).format(d) // -> "29/09/2025"
})

const formattedCreatedDate = computed(() => {
  const v = transaction.value?.createdAt
  if (!v) return ''
  const d = new Date(v)
  if (isNaN(d as unknown as number)) return ''
  return new Intl.DateTimeFormat('it-IT', {
    timeZone: 'Europe/Rome',
  }).format(d)
})

// Poll transaction status
const pollStatus = async () => {
  try {
    const data = await store.pollTransactionStatus(props.transactionId)
    transaction.value = data

    // Stop polling if status is final
    if (data.status === 'CONFIRMED' || data.status === 'FAILED') {
      stopPolling()
    }
  } catch (error) {
    console.error('Error polling transaction status:', error)
    // Stop polling on error
    stopPolling()
  }
}

// Start polling
const startPolling = () => {
  if (pollingInterval.value) return
  pollingInterval.value = setInterval(pollStatus, 5000) // Poll every 10 seconds
}

// Stop polling
const stopPolling = () => {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
}

// Open PolygonScan link
const openPolygonScan = () => {
  if (currentTxHash.value) {
    const url = `https://amoy.polygonscan.com/tx/${currentTxHash.value}`
    window.open(url, '_blank')
  }
}

// Initial setup - only poll if transactionId provided and status not provided or if it's pending
onMounted(async () => {
  if (!props.transactionId) {
    // No transactionId, use status directly if provided, no polling
    return
  }
  // If status is provided from props, use it directly
  if (props.status) {
    // If status is pending, start polling for updates
    if (isLoading.value) {
      startPolling()
    }
    // If status is final (CONFIRMED or FAILED), no need to poll
  } else {
    // No status provided, poll to get it
    await pollStatus()

    // Start polling if status is pending
    if (isLoading.value) {
      startPolling()
    }
  }
})

// Clean up polling on unmount
onBeforeUnmount(() => {
  stopPolling()
})
</script>

<style scoped>

@import "@/styles/xbrlCard.css";
@import "@/styles/statusIcon.css";

.icon-error {
    color: red;
    height: 1rem;
    width: 1rem;
}

.status-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.status-details {
  display: flex;
  flex-direction: column;
  font-size: 0.8rem;
  color: var(--color-text);
  font-weight: 400;
}
</style>
