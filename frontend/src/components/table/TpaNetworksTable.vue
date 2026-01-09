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
  { key: 'name', name: 'You are an auditor of the following networks:', width: '45%', type: 'name' },
  { key: 'membersCount', name: 'Members', width: '15%', type: 'level' },
  { key: 'requestCount', name: 'Verification requests', width: '20%', type: 'level' },
  { key: 'auditedCount', name: 'Completed verifications', width: '20%', type: 'level' },
]

const slice = computed(() => networksStore.tpaNetworks || { content: [], total: 0, page: 0, size: 10 })

const rows = computed(() =>
  (slice.value.content ?? []).map((item: any) => ({
    id: item.id,
    name: item.networkName,
    membersCount: item.membersCount,
    requestCount: item.requestCount,
    auditedCount: item.auditedCount,
    onClick: () => {
      const routeName = 'network-details'
      router.push({ name: routeName, params: { networkId: item.id } })
    },
  }))
)

async function fetchPage({ page, size }: { page: number; size?: number }) {
  await networksStore.fetchTpaNetworks({
    page,
    size: size ?? slice.value.size,
    sort: 'updated_at,desc',
  })
}
</script>