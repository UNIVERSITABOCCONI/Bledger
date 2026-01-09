<template>
  <div class="card-diagram" :class="{
    'is-editable': editMode && !editing,
    'is-editing': editing,
    'is-focused': isFocused,
    'is-disabled': disabled,
    'selected': networksStore.selectedNode === props.companyId
  }">
    <template v-if="editing">
      <Dropdown ref="companyDropdown" v-model="internalValue" :options="orgDropdownOptions" :placeholder="placeholder"
        :aria-label="placeholder || 'Select company'" @focus="isFocused = true" @blur="isFocused = false"
        class="card-dropdown" :disabled="disabled" @change="handleChange" />
      <button class="icon-button close" @click="() => editing = !editing" :disabled="disabled" aria-label="Cancel editing">
        <IconClose />
      </button>
    </template>

    <template v-else-if="editMode && !(node.id)">
      <span class="company-name">{{ companyName }} <template v-if="companyId == companyStore.company.id"> (ME)</template></span>
      <Button class="icon-button edit" variant="icon" @click="() => editing = !editing" :disabled="disabled"
        aria-label="Edit company">
        <IconEdit />
      </Button>
    </template>

    <template v-else>
       <span class="company-name">{{ companyName }} <template v-if="companyId == companyStore.company.id"> (ME)</template></span>
     </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import Dropdown from '@/components/forms/Dropdown.vue'
import Button from '@/components/button/Button.vue'
import IconEdit from "@/components/icons/IconEdit.vue";
import IconClose from "@/components/icons/IconClose.vue";
import { useNetworksStore } from '@/stores/networks'
import { useCompanyStore } from '@/stores/company'
 
const networksStore = useNetworksStore()
const companyStore = useCompanyStore()
const orgDropdownOptions = computed(() => networksStore.orgDropdownOptions)

const props = withDefaults(defineProps<{
  companyId: string
  companyName?: string
  disabled?: boolean
}>(), {
  companyName: '',
  disabled: false,
})

const node = computed(() => networksStore.currentNetworkMap[props.companyId])
const companyName = computed(() => node.value?.companyName || '')

const editMode = computed(() => networksStore.editMode)

const internalValue = ref<string>(node.value?.companyId ?? '')
const isFocused = ref(false)

const editing = ref(false)
const placeholder = ref('Select Company')

const getCompanyNameFromValue = (value: string): string => {
  const option = orgDropdownOptions.value.find(opt => opt.value === value)
  return option ? option.label : ''
}

watch(() => node.value?.companyId, (val) => {
  internalValue.value = (val ?? '') as string
})

const handleChange = (e) => {
  const newId = (e.newValue ?? '') as string
  const oldId = (e.oldValue ?? '') as string
  const newCompanyName = getCompanyNameFromValue(newId)
  networksStore.editNode(oldId, newId, newCompanyName)
  editing.value = !editing.value
}


</script>

<style scoped>
@import "@/styles/cardDiagram.css";

.selected {
  border: 2px solid #0046AE;
  box-shadow: 0 0 10px rgba(0, 70, 174, 0.5);
}
</style>
