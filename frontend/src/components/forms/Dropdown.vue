<template>
  <div class="dropdown-container">
    <div class="dropdown-wrapper" ref="dropdownRef">
      <label v-if="label" :for="buttonId" class="input-label" :id="labelId">
        {{ label }}
      </label>

      <div :id="buttonId" class="dropdown-display" role="combobox" :aria-expanded="isOpen" :aria-haspopup="'listbox'"
        :aria-controls="listboxId" :aria-labelledby="label ? labelId : undefined" :aria-describedby="describedByIds"
        :aria-activedescendant="activeOptionId" :aria-invalid="showError"
        :aria-required="props.required || attrs['aria-required']" :tabindex="0" @click="toggleDropdown"
        @keydown="handleKeydown" :class="{
          'dropdown-open': isOpen,
          'dropdown-error': showError
        }">
        <span class="dropdown-value">
          {{ selectedLabel || placeholder }}
        </span>
        <IconDropdown class="icon" :class="{ 'dropdown-open': isOpen }" aria-hidden="true" />
      </div>

      <ul v-if="isOpen" :id="listboxId" class="dropdown-list" role="listbox"
        :aria-labelledby="label ? labelId : buttonId" :class="{ 'dropdown-open': isOpen }">
        <li v-for="(option, index) in options" :key="option.value" :id="getOptionId(index)" role="option"
          :aria-selected="option.value === currentValue" @click="selectOption(option)"
          @mouseenter="setActiveIndex(index)" class="dropdown-item" :class="{
            'is-selected': option.value === currentValue,
            'is-active': index === activeIndex
          }">
          {{ option.label }}
        </li>
      </ul>
    </div>

    <p v-if="showError" :id="errorId" class="error-message" role="alert" aria-live="polite">
      {{ requiredMsg }}
    </p>

    <div v-if="note" :id="noteId" class="small">
      {{ note }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, useAttrs } from 'vue'
import IconDropdown from "@/components/icons/IconDropdown.vue"

// Disabilita l'ereditarietà automatica degli attributi
defineOptions({
  inheritAttrs: false
})

// Tipi
interface Option {
  label: string
  value: string
}

// Props tipizzate
const props = withDefaults(defineProps<{
  modelValue?: string | null
  label?: string
  note?: string
  placeholder?: string
  id?: string
  options: Option[]
  required?: boolean
  requiredMsg?: string
}>(), {
  modelValue: null,
  placeholder: 'Seleziona un\'opzione',
  id: () => `dropdown-${Math.random().toString(36).substr(2, 9)}`,
  required: false,
  requiredMsg: 'This field is required.'
})

// Gestione degli attributi ereditati
const attrs = useAttrs()

// Emit tipizzato
const emit = defineEmits<{
  (e: 'update:modelValue', value: string, oldValue?: string): void
  (e: 'change', payload: { oldValue: string; newValue: string }): void
  (e: 'invalid', id: string): void
}>()

// Stato interno
const isOpen = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)
const showError = ref(false)
const activeIndex = ref(-1)

// IDs unici per accessibilità
const baseId = computed(() => props.id)
const buttonId = computed(() => `${baseId.value}-button`)
const listboxId = computed(() => `${baseId.value}-listbox`)
const labelId = computed(() => `${baseId.value}-label`)
const errorId = computed(() => `${baseId.value}-error`)
const noteId = computed(() => `${baseId.value}-note`)

// Valore corrente gestendo null/undefined
const currentValue = computed(() => props.modelValue ?? '')

// IDs descrittivi per aria-describedby
const describedByIds = computed(() => {
  const ids: string[] = []
  if (showError.value) ids.push(errorId.value)
  if (props.note) ids.push(noteId.value)
  return ids.length > 0 ? ids.join(' ') : undefined
})

// ID opzione attiva
const activeOptionId = computed(() => {
  return activeIndex.value >= 0 ? getOptionId(activeIndex.value) : undefined
})

// Computed per etichetta selezionata
const selectedLabel = computed(() => {
  const found = props.options.find(o => o.value === currentValue.value)
  return found ? found.label : ''
})

// Funzione per generare ID opzione
function getOptionId(index: number): string {
  return `${baseId.value}-option-${index}`
}

// Funzioni
function toggleDropdown() {
  if (isOpen.value) {
    closeDropdown()
  } else {
    openDropdown()
  }
}

function openDropdown() {
  isOpen.value = true
  // Imposta l'indice attivo sull'opzione selezionata o la prima
  const selectedIndex = props.options.findIndex(o => o.value === currentValue.value)
  activeIndex.value = selectedIndex >= 0 ? selectedIndex : 0

  nextTick(() => {
    scrollToActiveOption()
  })
}

function closeDropdown() {
  isOpen.value = false
  activeIndex.value = -1
}

function selectOption(option: Option) {
  const oldValue = currentValue.value
  emit('update:modelValue', option.value, oldValue)      // v-model update (+ old)
  emit('change', { oldValue, newValue: option.value })   // optional separate event
  closeDropdown()
  showError.value = false
  nextTick(() => {
    const button = document.getElementById(buttonId.value)
    button?.focus()
  })
}

function setActiveIndex(index: number) {
  activeIndex.value = index
}

function scrollToActiveOption() {
  if (activeIndex.value >= 0) {
    const option = document.getElementById(getOptionId(activeIndex.value))
    option?.scrollIntoView({ block: 'nearest' })
  }
}

// Navigazione con tastiera
function handleKeydown(event: KeyboardEvent) {
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      if (!isOpen.value) {
        openDropdown()
      } else {
        moveActiveIndex(1)
      }
      break

    case 'ArrowUp':
      event.preventDefault()
      if (!isOpen.value) {
        openDropdown()
      } else {
        moveActiveIndex(-1)
      }
      break

    case 'Enter':
    case ' ':
      event.preventDefault()
      if (!isOpen.value) {
        openDropdown()
      } else if (activeIndex.value >= 0) {
        selectOption(props.options[activeIndex.value])
      }
      break

    case 'Escape':
      if (isOpen.value) {
        event.preventDefault()
        closeDropdown()
      }
      break

    case 'Home':
      if (isOpen.value) {
        event.preventDefault()
        activeIndex.value = 0
        scrollToActiveOption()
      }
      break

    case 'End':
      if (isOpen.value) {
        event.preventDefault()
        activeIndex.value = props.options.length - 1
        scrollToActiveOption()
      }
      break

    case 'Tab':
      if (isOpen.value) {
        closeDropdown()
      }
      break
  }
}

function moveActiveIndex(direction: number) {
  const newIndex = activeIndex.value + direction

  if (newIndex >= 0 && newIndex < props.options.length) {
    activeIndex.value = newIndex
    scrollToActiveOption()
  } else if (direction > 0 && newIndex >= props.options.length) {
    // Wrap to first
    activeIndex.value = 0
    scrollToActiveOption()
  } else if (direction < 0 && newIndex < 0) {
    // Wrap to last
    activeIndex.value = props.options.length - 1
    scrollToActiveOption()
  }
}

// Validazione esposta
function validate(): boolean {
  if (props.required && !currentValue.value) {
    showError.value = true
    emit('invalid', baseId.value)
    return false
  }
  showError.value = false
  return true
}

defineExpose({ validate, open: openDropdown, close: closeDropdown })

// Click esterno
function handleClickOutside(event: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
@import "@/styles/dropdown.css";
</style>