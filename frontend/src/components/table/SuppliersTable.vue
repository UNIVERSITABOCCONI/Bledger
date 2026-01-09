<template>
  <TableCustom
    :headers="headers"
    :rows="rows"
    :total="slice.total"
    :page="slice.page"
    :size="slice.size"
    :fetchFn="fetchPage"
  />

  <!-- Update Values Modal -->
  <UpdateSupplierValuesModal
    :network-id="networkId"
    :supplier="selectedSupplier"
    v-model:open="showUpdateModal"
    @updated="handleSupplierUpdated"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import TableCustom from '@/components/table/TableCustom.vue'
import UpdateSupplierValuesModal from '@/components/UpdateSupplierValuesModal.vue'
import { useNetworksStore } from '@/stores/networks'

const props = defineProps<{
  networkId: string
}>()

const networksStore = useNetworksStore()

const headers = [
  { key: 'name', name: 'Suppliers', width: '35%', type: 'name' },
  { key: 'level', name: 'Level', width: '15%', type: 'level' },
  { key: 'values', name: 'q / t', width: '25%', type: 'values' },
  { key: 'status', name: 'Scope 1 e 2', width: '25%', type: 'status' },
]

const slice = computed(() => networksStore.suppliers)

const showUpdateModal = ref(false)
const selectedSupplier = ref(null)

const rows = computed(() =>
  (slice.value.content ?? []).map((item: any) => ({
    id: item.id,
    name: item.companyName,
    level: item.nodeDepth,
    quantity: item.quantity,
    transportationEmission: item.transportationEmission ? `${item.transportationEmission} tCO₂e` : item.transportationEmission,
    transactionId: item.auditFileTransactionId || item.uploadFileTransactionId || null,
    verify: !!item.auditFileTransactionId,
    verified: !!item.auditFileTransactionId,
    verifiedDate: item.auditedAt ? new Date(item.auditedAt).toLocaleDateString() : '',
    onClick: () => openUpdateModal(item),
  }))
)

function openUpdateModal(supplier: any) {
  selectedSupplier.value = supplier
  showUpdateModal.value = true
}

async function handleSupplierUpdated() {
  // Refresh the suppliers list
  await fetchPage({ page: slice.value.page, size: slice.value.size })
}

async function fetchPage({ page, size }: { page: number; size?: number }) {
  await networksStore.fetchSuppliers(props.networkId, {
    page,
    size: size ?? slice.value.size,
    sort: 'updated_at,desc',
  })
}
</script>