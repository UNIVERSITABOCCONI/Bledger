<template>
  <Table
    :sections="tableSections"
    :total="total"
    :unit="unit"
    :headers="['Categoria', 'Emissioni']"
    :showHeader="false"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Table from './Table.vue'

const props = defineProps<{
  data: Array<{ key: string; value: number }>
  total: number
  unit: string
}>()

const tableSections = computed(() => {
  if (!props.data || props.data.length === 0) return []

  // Group data by scope
  const scope1Data = props.data.filter(item => item.key.includes('scope_1'))
  const scope2Data = props.data.filter(item => item.key.includes('scope_2'))

  const sections = []

  if (scope1Data.length > 0) {
    const scope1Total = scope1Data.find(item => item.key === 'scope_1_total_emission')?.value || NaN
    sections.push({
      title: 'Scope 1',
      total: scope1Total,
      unit: props.unit,
      rows: scope1Data
        .filter(item => !item.key.includes('total'))
        .map(item => ({
          label: item.key
            .replace('scope_1_', '')
            .replace(/_/g, ' ')
            .replace(/^\w/, c => c.toUpperCase()),
          value: item.value,
          unit: props.unit
        }))
    })
  }

  if (scope2Data.length > 0) {
    const scope2Total = scope2Data.find(item => item.key === 'scope_2_total_emission')?.value || NaN
    sections.push({
      title: 'Scope 2',
      total: scope2Total,
      unit: props.unit,
      rows: scope2Data
        .filter(item => !item.key.includes('total'))
        .map(item => ({
          label: item.key
            .replace('scope_1_', '')
            .replace(/_/g, ' ')
            .replace(/^\w/, c => c.toUpperCase()),
          value: item.value,
          unit: props.unit
        }))
    })
  }

  return sections
})
</script>