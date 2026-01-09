<template>
  <TableCustom
    :showImage="false"
    :headers="headers"
    :rows="rows"
    :total="total"
    :page="page"
    :size="size"
    :fetchFn="fetchFn"
  />
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import TableCustom from '@/components/table/TableCustom.vue'
import TransactionStatus from '@/components/TransactionStatus.vue'

const props = defineProps<{
  items: any[]
  total: number
  page: number
  size: number
  fetchFn: (opts: { page: number; size?: number }) => Promise<unknown> | unknown
}>()

const headers = [
  { key: 'custom', name: 'Network Transactions', width: '100%', type: 'status' },
]

const rows = computed(() =>
  (props.items ?? []).map((transaction: any) => ({
    createdAt: new Date(transaction.createdAt).toLocaleString(),
    confirmedAt: transaction.confirmedAt ? new Date(transaction.confirmedAt).toLocaleString() : '-',
    id: transaction.id,
    custom: h(TransactionStatus as any, {
      transactionId: transaction.id,
      text: String(transaction.transactionType || '').replace(/_/g, ' ')
    }),
  }))
)

const total = computed(() => props.total ?? 0)
const page = computed(() => props.page ?? 0)
const size = computed(() => props.size ?? 10)
const fetchFn = (opts: { page: number; size?: number }) => props.fetchFn?.(opts)
</script>