<template>
  <div class="emission-table-wrapper">
    <table class="emission-table"
           aria-label="Emission breakdown by scope">
      <!-- Header -->
      <thead v-if="showHeader">
      <tr>
        <th scope="col">{{ headers[0] }}</th>
        <th scope="col" class="text-right">{{ headers[1] }}</th>
      </tr>
      </thead>

      <!-- Corpo -->
      <tbody>
      <template v-for="(section, sIndex) in sections" :key="`section-${sIndex}`">
        <tr class="scope-row">
          <th scope="row" class="scope-label" colspan="2">
            <div class="scope-container">
              <span class="scope-title">{{ section.title }}</span>
              <span class="scope-value">{{ formatTons(section.total) }}<small>{{ section.unit }}</small></span>
            </div>
          </th>
        </tr>
        <tr
            v-for="(row, rIndex) in section.rows"
            :key="`section-${sIndex}-row-${rIndex}`"
        >
          <th scope="row" class="row-label">{{ row.label }}</th>
          <td class="row-value text-right">{{ formatTons(row.value) }}<small>{{ row.unit }}</small></td>
        </tr>
      </template>
      </tbody>

      <tfoot>
      <tr class="total-row">
        <th scope="row" class="footer-label">Tot.</th>
        <td class="footer-value text-right">{{ formatTons(total) }}<small>{{ unit }}</small></td>
      </tr>
      </tfoot>
    </table>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  sections?: {
    title: string
    total: number
    unit: string
    rows: { label: string; value: number, unit: string }[]
  }[]
  total?: number
  unit?: string
  headers?: string[]
  showHeader?: boolean
  fixedHeight?: boolean
}>()

function formatTons(value: number): string {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<style scoped>
@import "@/styles/table.css";
</style>
