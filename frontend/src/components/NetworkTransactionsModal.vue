<template>
  <Modal size="xl" variant="simple" v-model:open="isOpen"
    cta-close-label="Close">
    <div class="transactions-modal-content">

      <div class="token-info">
        <span class="note">Token Id: {{ networkStore.networkDetails.tokenId }}</span>
        <a class="explorer-link" target="_blank" :href="'https://amoy.polygonscan.com/token/'+networkStore.networkDetails.smartContractAddress+'?a='+networkStore.networkDetails.tokenId+'#readContract'">
          <IconPolygon class="icon-polygon" /> See in Explorer
        </a>
      </div>
      <div v-if="!isTpa" class="node-info">
        <span class="note">Your node id: </span>
        <span class="node-id-address" :title="networkStore.networkDetails?.myNode" role="button" tabindex="0" aria-label="Copy node id"
          @click="copyToClipboard" @keydown.enter.prevent="copyToClipboard" @keydown.space.prevent="copyToClipboard">
          <span class="addr-clip">{{ networkStore.networkDetails?.myNode?.id }}</span>
      </span>
        <span class="copied-badge" v-if="copied">Copied!</span>
      </div>
      <div class="node-info">
        <span class="note">Smart Contract address: </span>
        <span class="node-id-address" :title="networkStore.networkDetails?.smartContractAddress" role="button" tabindex="0" aria-label="Copy smart contract address"
          @click="copyContractToClipboard" @keydown.enter.prevent="copyContractToClipboard" @keydown.space.prevent="copyContractToClipboard">
          <span class="addr-clip">{{ networkStore.networkDetails?.smartContractAddress }}</span>
      </span>
        <span class="copied-badge" v-if="copiedContract">Copied!</span>
      </div>
      <NetworkTransactionsTable class="table"
        :items="transactionStore.networkTransactions.content"
        :total="transactionStore.networkTransactions.total"
        :page="transactionStore.networkTransactions.page"
        :size="transactionStore.networkTransactions.size"
        :fetchFn="(opts) => transactionStore.fetchNetworkTransactions(props.networkId, opts)"
      />
    </div>
  </Modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Modal from "@/components/modal/Modal.vue";
import IconPolygon from "@/components/icons/IconPolygon.vue";
import { useNetworksStore } from '@/stores/networks'
import NetworkTransactionsTable from "@/components/table/NetworkTransactionsTable.vue";
import { useCompanyStore } from '@/stores/company';
import { useTransactionStore } from '@/stores/transaction';

const props = defineProps<{
  networkId: string
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const networkStore = useNetworksStore();
const companyStore = useCompanyStore()
const transactionStore = useTransactionStore()

// Reactive data
const copied = ref(false)
const copiedContract = ref(false)

// Computed properties
const isOpen = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const isTpa = computed(() => companyStore.company?.companyType === 'TPA')


// Watch for modal opening to fetch data
watch(() => props.open, async (newOpen) => {
  if (newOpen && props.networkId) {
    await transactionStore.fetchNetworkTransactions(props.networkId)
  }
})


async function copyToClipboard() {
  try {
    await navigator.clipboard.writeText(networkStore.networkDetails?.myNode.id || '')
    copied.value = true
    setTimeout(() => (copied.value = false), 1500)
  } catch (err) {
    console.error('Copy failed:', err)
  }
}

async function copyContractToClipboard() {
  try {
    await navigator.clipboard.writeText(networkStore.networkDetails?.smartContractAddress || '')
    copiedContract.value = true
    setTimeout(() => (copiedContract.value = false), 1500)
  } catch (err) {
    console.error('Copy failed:', err)
  }
}
</script>

<style scoped>
@import "@/styles/xbrlCard.css";

.transactions-modal-content {
  max-height: 70vh;
  overflow-y: scroll;
  width: 100%;
}

.token-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.explorer-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-link);
  text-decoration: none;
  transition: 0.4s;
}

.explorer-link:hover {
  background-color: hsla(160, 100%, 37%, 0.2);
}

.node-info {
  font-size: 0.9rem;
}

.addr-clip {
  overflow: scroll;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copied-badge {
  margin-left: 8px;
  font-size: 0.75rem;
  opacity: 0.8;
}

.node-id-address {
  cursor: pointer;
  outline: none;
}

.node-id-address:focus {
  box-shadow: 0 0 0 2px rgba(0, 102, 255, 0.3);
  border-radius: 6px;
  padding: 2px 4px;
}

.contract-info {
  margin-top: 1rem;
}

.contract-address {
  cursor: pointer;
  outline: none;
}

.contract-address:focus {
  box-shadow: 0 0 0 2px rgba(0, 102, 255, 0.3);
  border-radius: 6px;
  padding: 2px 4px;
}

.table {
  margin-top: 15px;
}
</style>