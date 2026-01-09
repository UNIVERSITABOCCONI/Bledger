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
import { useRouter } from 'vue-router'
import TableCustom from '@/components/table/TableCustom.vue'
import { useNetworksStore } from '@/stores/networks'

const networksStore = useNetworksStore()
const router = useRouter()

const headers = [
  { key: 'name', name: 'You’re an PARTICIPANT in the following networks:', width: '55%', type: 'name' },
  { key: 'level', name: 'Members', width: '20%', type: 'level' },
  { key: 'status', name: 'Scope 1 & 2', width: '25%', type: 'status' },
]

const slice = computed(() => networksStore.participant || { content: [], total: 0, page: 0, size: 10 })

function mapNetworkToRow(n) {
  const id = n.id ?? n.networkId ?? n.uuid ?? n.name
  const name = n.name ?? n.networkName ?? 'Network'
  const level = n.membersCount ?? n.members ?? n.level ?? 0

  const transactionId = n.auditFileTransactionId || n.uploadFileTransactionId || null
  const verify = !!n.auditFileTransactionId
  const verifiedDate = n.auditedAt || ''
  const verified = verify

  const handleClick = () => {
    const routeName = 'network-details'
    router.push({ name: routeName, params: { networkId: id } })
  }

  return {
    id,
    name,
    level,
    verified,
    verifiedDate,
    transactionId,
    verify,
    onClick: handleClick,
  }
}

const rows = computed(() => (slice.value.content ?? []).map((n) => mapNetworkToRow(n)))

async function fetchPage({ page, size }: { page: number; size?: number }) {
  await networksStore.fetchParticipantNetworks({
    page,
    size: size ?? slice.value.size,
    sort: 'updated_at,desc',
  })
}
</script>