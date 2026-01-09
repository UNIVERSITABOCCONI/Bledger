<template>
  <Modal
    v-model:open="isModalOpen"
    variant="simple"
    modal-title="Confirm invitations"
    cta-close-label="Close"
    cta-confirm-label="Send Invitation"
    @confirm="onConfirm"
    @close="onClose">
    <TableCustom
      fixedHeight
      :showImage="false"
      :showPagination="false"
      :headers="[
        { key: 'name', name: 'Companies', width: '70%', type: 'name' },
        { key: 'level', name: 'Level', width: '30%', type: 'level' },
      ]"
      :rows="networkMembersRows"
      :total="networkMembersRows.length"
    />
  </Modal>


</template>

<script setup lang="ts">
import { computed } from 'vue'
import Modal from '@/components/modal/Modal.vue'
import TableCustom from '@/components/table/TableCustom.vue'
import { useNetworksStore } from '@/stores/networks'
import { useCompanyStore } from '@/stores/company'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'confirm': []
}>()

const networksStore = useNetworksStore()
const companyStore = useCompanyStore()

const isModalOpen = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const flattenTreeWithLevels = (root) => {
  const result = []
  if (!root) return result

  const queue = [{ node: root, level: 1 }]

  while (queue.length > 0) {
    const { node, level } = queue.shift()

    if (node.companyId !== companyStore.company?.id) {
      result.push({
        name: node.companyName,
        level: level
      })
    }

    if (node.children) {
      for (const child of node.children) {
        queue.push({ node: child, level: level + 1 })
      }
    }
  }

  return result
}

const networkMembersRows = computed(() => {
  const tree = networksStore.currentNetworkTree
  if (!tree) return []
  return flattenTreeWithLevels(tree)
})

const onConfirm = () => {
  emit('confirm')
}

const onClose = () => {
  isModalOpen.value = false
}
</script>