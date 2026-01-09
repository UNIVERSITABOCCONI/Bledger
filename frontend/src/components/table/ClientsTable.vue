<template>
  <TableCustom
    :headers="headers"
    :rows="rows"
    :total="slice.total"
    :page="slice.page"
    :size="slice.size"
    :fetchFn="fetchPage"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import TableCustom from '@/components/table/TableCustom.vue'
import { useNetworksStore } from '@/stores/networks'

const props = defineProps<{
  networkId: string
}>()

const networksStore = useNetworksStore()

const headers = [
  { key: 'name', name: 'Clients', width: '55%', type: 'name' },
  { key: 'level', name: 'Level', width: '20%', type: 'level' },
  { key: 'status', name: 'Scope 1 e 2', width: '25%', type: 'status' },
]

const slice = computed(() => networksStore.clients)

const rows = computed(() =>
  (slice.value.content ?? []).map((item) => ({
    id: item.id,
    name: item.companyName,
    level: item.nodeDepth,
    transactionId: item.auditFileTransactionId || item.uploadFileTransactionId || null,
    verify: !!item.auditFileTransactionId,
    verified: !!item.auditFileTransactionId,
    verifiedDate: item.auditedAt ? new Date(item.auditedAt).toLocaleDateString() : '',
  }))
)

async function fetchPage({ page, size }: { page: number; size?: number }) {
  await networksStore.fetchClients(props.networkId, {
    page,
    size: size ?? slice.value.size,
    sort: 'updated_at,desc',
  })
}
</script>