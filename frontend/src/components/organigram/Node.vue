<script setup>
import { computed } from "vue";
import Button from "@/components/button/Button.vue";
import IconAdd from "@/components/icons/IconAdd.vue";
import CardDiagram from "@/components/organigram/CardDiagram.vue";
import { useNetworksStore } from '@/stores/networks'
import IconTrash from "../icons/IconTrash.vue";
import IconDrag from "@/components/icons/IconDrag.vue";

const networksStore = useNetworksStore()
const orgDropdownOptions = computed(() => networksStore.orgDropdownOptions)

const props = defineProps({
  companyId: {
    type: String,
    required: true
  },
  root: {
    type: Boolean,
    default: false
  }
});

const node = computed(() => networksStore.currentNetworkMap[props.companyId])

const rootChild = computed(() => !node.value?.parentCompanyId)
const lastChild = computed(() => node.value?.children?.length == 0)
const editMode = computed(() => networksStore.editMode)
const children = computed(() => node.value?.children || [])
const loading = computed(() => networksStore.loading)


const addChild = () => {
  try {
    const companyInitialValue = orgDropdownOptions.value[0];
    networksStore.addNode(companyInitialValue.value, companyInitialValue.label, node.value.companyId)
  } catch (e) {
    console.error('Error in addChild:', e);
  }
}

const addSibling = () => {
  try {
    const companyInitialValue = orgDropdownOptions.value[0];
    networksStore.addNode(companyInitialValue.value, companyInitialValue.label, node.value?.parentCompanyId)
  } catch (e) {
    console.error('Error in addSibling:', e);
  }
}

const selectNode = () => {
  networksStore.selectedNode = node.value.companyId;
};

const handleOpenDeleteModal = () => {
  networksStore.selectedNode = props.companyId
  networksStore.isDeleteModalOpen = true
}

const handleDragStart = (e) => {
  networksStore.selectedNode = props.companyId
  e.dataTransfer.setData('text/plain', props.companyId)
  // Set drag image to the entire node container
  const nodeElement = e.target.closest('.organigram__container')
  if (nodeElement) {
    e.dataTransfer.setDragImage(nodeElement, e.offsetX, e.offsetY)
  }
}

const handleDragOver = (e) => {
  e.preventDefault()
}

const handleDrop = (e) => {
  e.preventDefault()
  const draggedCompanyId = e.dataTransfer.getData('text/plain')
  if (draggedCompanyId !== props.companyId) {
    const descendants = networksStore.listChildren(draggedCompanyId)
    if (!descendants.includes(props.companyId)) {
      networksStore.moveNode(draggedCompanyId, props.companyId)
    }
  }
  e.stopPropagation()
}

</script>

<template>
  <div v-if="!loading" class="organigram__container" :class="{ 'organigram__container--root': rootChild}" @dragover="handleDragOver" @drop="handleDrop">
    <div v-if="node" class="organigram__label__container">
      <div class="organigram__label">
        <CardDiagram :companyId="node.companyId" @click="selectNode"/>

        <Button variant="secondary" v-if="editMode && !root" class="organigram__add_sibling" data-action="add_sibling"
          @click="addSibling">
          <span class="sr-only">add sibling</span>
          <IconAdd />
        </Button>

        <Button variant="secondary" v-if="editMode && !root" class="organigram__remove" data-action="add_sibling"
          @click="handleOpenDeleteModal">
          <span class="sr-only">add sibling</span>
          <IconTrash />
        </Button>

        <Button variant="secondary" v-if="editMode && !root" class="organigram__move" data-action="add_sibling"
          :draggable="editMode" @dragstart="handleDragStart">
          <span class="sr-only">drag</span>
          <IconDrag v-if="editMode" />
        </Button>

      </div>
    </div>
    <div v-if="children.length" class="organigram__children__wrapper">
      <div class="organigram__children" :class="{ 'organigram__children--multi': children.length > 1 }">
        <Node v-for="(child, i) in children" :key="child.companyId" :companyId="child.companyId" />
      </div>
    </div>
    <Button variant="secondary" v-if="lastChild && editMode" class="organigram__add_level" data-action="add-child"
      @click="addChild">
      <IconAdd />
    </Button>
  </div>
</template>

<style scoped>
@import "../../styles/organigram.css";
</style>
