<template>
<Modal
  v-model:open="isModalOpen"
  variant="alert"
  modalTitle="Delete Company"
  ctaCloseLabel="Cancel"
  ctaConfirmLabel="Delete"
  @confirm="handleDelete"
  @close="handleCancel"
>
  <p>
    Are you sure you want to remove <strong>{{ companyName }}</strong> from the network? 
    <span v-if="numChildren > 0">
      This company has {{ numChildren }} {{ numChildren === 1 ? "child node" : "child nodes" }}. 
      Deleting it will also permanently remove all of its children.
    </span>
  </p>
</Modal>
</template>

<script setup>
import { useNetworksStore } from '@/stores/networks';
import { computed } from 'vue';
import Modal from './Modal.vue';

const networkStore = useNetworksStore();
const companyId = computed(() => networkStore.selectedNode)
const companyName = computed(() => companyId.value ? networkStore.currentNetworkMap[companyId.value].companyName : null)
const numChildren = computed(() => companyId.value ? networkStore.listChildren(companyId.value).length : null)
const isModalOpen = computed(() => networkStore.isDeleteModalOpen)

const handleDelete = () => {
  networkStore.removeNode(companyId.value);
  networkStore.isDeleteModalOpen = false
};

const handleCancel = () => {
  networkStore.isDeleteModalOpen = false
};

</script>

