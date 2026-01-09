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
  { key: 'name', name: 'Other Members', width: '75%', type: 'name' },
  { key: 'level', name: 'Level', width: '25%', type: 'level' },
]

const slice = computed(() => networksStore.otherMembers)

const rows = computed(() =>
  (slice.value.content ?? []).map((item: any) => ({
    id: item.id,
    name: item.companyName,
    level: item.nodeDepth,
  }))
)

async function fetchPage({ page, size }: { page: number; size?: number }) {
  await networksStore.fetchOtherMembers(props.networkId, {
    page,
    size: size ?? slice.value.size,
    sort: 'updated_at,desc',
  })
}
</script>