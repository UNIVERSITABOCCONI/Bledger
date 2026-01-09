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
import { computed, h } from 'vue'
import TableCustom from '@/components/table/TableCustom.vue'
import Button from '@/components/button/Button.vue'
import { useNetworksStore } from '@/stores/networks'

const props = defineProps<{
  networkId: string
}>()

const emit = defineEmits<{
  review: [id: string | number]
}>()

const networksStore = useNetworksStore()

const headers = [
  { key: 'name', name: 'Request from', width: '55%', type: 'name' },
  { key: 'level', name: 'Level', width: '20%', type: 'level' },
  { key: 'status', name: 'Verification Request', width: '25%', type: 'status' },
]

const slice = computed(() => networksStore.tpaRequests)

const rows = computed(() =>
  (slice.value.content ?? []).map((item: any) => ({
    id: item.id,
    name: item.companyName,
    level: item.nodeDepth,
    transactionId: item.auditFileTransactionId || null,
    verify: !!item.auditFileTransactionId,
    custom: !item.auditFileTransactionId
      ? h(
          Button as any,
          {
            variant: 'secondary',
            onClick: () => emit('review', item.id),
          },
          { default: () => 'Review files' }
        )
      : undefined,
  }))
)

async function fetchPage({ page, size }: { page: number; size?: number }) {
  await networksStore.fetchTpaRequests(props.networkId, {
    page,
    size: size ?? slice.value.size,
    sort: 'updated_at,desc',
  })
}
</script>