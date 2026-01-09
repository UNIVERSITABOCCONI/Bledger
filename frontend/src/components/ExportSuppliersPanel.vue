<template>
     <Panel title="Export your emission data">
          <p>In this section, you can download your emissions data, as well as your suppliers’ documents once all suppliers’ required files have been uploaded.</p>
          <p class="note">Note: Please note that the data may be incomplete if your emissions data has not been calculated.</p>

          <VSpacer size="xl" />

          <ProgressBar :progress="progressPercentage" :copy="getProgressMessage()" />

          <div class="space-between">
               <div  class="label">
                    <div v-if="lastVersionDate && progressPercentage == 100">Last version {{ lastVersionDate }}</div>
                    <div v-if="lastExportDate && progressPercentage == 100">Last export {{ lastExportDate }}</div>
                    <div class="label" v-if="lastVersionTimestamp > lastExportTimestamp && progressPercentage == 100">New Version Available!</div>
               </div>
               <div>
               <Button variant="primary" @click="handleExport" icon iconPosition="left">
                    Export
                    <template #icon>
                         <IconExport />
                    </template>
               </Button>
               </div>
               
          </div>
     </Panel>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Panel from "@/components/panel/Panel.vue";
import VSpacer from "@/components/spacer/VSpacer.vue";
import ProgressBar from "@/components/progressBar/ProgressBar.vue";
import Button from "@/components/button/Button.vue";
import IconExport from "@/components/icons/IconExport.vue";
import { useNetworksStore } from '@/stores/networks'

const props = defineProps<{
      networkId: string;
      progressPercentage?: number;
}>();

const networksStore = useNetworksStore()

const nodeDetails = computed(() => networksStore.networkDetails)

const lastVersionTimestamp = computed(() => {
       return nodeDetails.value?.lastVersionExport
    ? new Date(nodeDetails.value.lastVersionExport).getTime()
    : null
});

const lastVersionDate = computed(() => {
     if(!lastVersionTimestamp.value) return null;
     

     const date = new Date(lastVersionTimestamp.value)
     return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()}`
})

const lastExportTimestamp = computed(() => {
     return new Date(networksStore.networkDetails?.lastExport).getTime()
})

const lastExportDate = computed(() => {
  if (!lastExportTimestamp.value) return null
  const date = new Date(lastExportTimestamp.value)
  return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()}`
})

const progressPercentage = computed(() => props.progressPercentage ?? 0);

function getProgressMessage() {
     const percentage = progressPercentage.value;
     if (percentage === 100) {
          return "All suppliers have uploaded their Scope 1 and 2 documentation.";
     } else if (percentage > 0) {
          return `Some suppliers have uploaded their Scope 1 and 2 documentation. ${percentage}% complete.`;
     } else {
          return "Some suppliers of the Network have not yet uploaded their Scope 1 and 2 documentation.";
     }
}

async function handleExport() {
     try {
          await networksStore.exportSuppliers(props.networkId)
          networksStore.fetchNetworkDetails(props.networkId)
     } catch (error) {
          console.error('Error exporting suppliers:', error)
     }
}
</script>

<style scoped>
.note {
     font-style: italic;
     color: #666;
     margin-top: 0.5rem;
}

.space-between {
     display: flex;
     justify-content: space-between;
     align-items: center;
     margin-top: 1rem;
}

.label {
     font-size: 0.90rem;
     font-weight: 500;
     color: #666;
}
</style>