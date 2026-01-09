<template>
  <div class="network-controller" :class="{ 'is-disabled': disabled }" :style="{ position: 'fixed', left: position.x + 'px', top: position.y + 'px', zIndex: 1000, cursor: isDragging ? 'grabbing' : 'grab' }" @mousedown="onMouseDown">
    <Button
        class="controller-title"
        variant="link"
        type="button"
        aria-label="Edit network"
        @click="$emit('edit-network')"
    >
      Edit network
      <IconEdit aria-hidden="true" />
    </Button>

    <!-- Navigation pad -->
    <div class="controller-card" aria-labelledby="controller-nav-title">
      <h3 class="controller-card-title" id="controller-nav-title">Navigation</h3>

      <div
          class="pad-grid"
          role="group"
          aria-label="Navigation pad"
          tabindex="0"
          @focus="onPadGridFocus"
      >
        <button
            type="button"
            :ref="setPadRef('up')"
            :class="padClass('up')"
            :aria-pressed="activeComputed === 'up'"
            :tabindex="tabIndexFor('up')"
            aria-label="Move up"
            :disabled="disabled"
            @click="onMove('up')"
            @keydown="onPadKeydown($event, 'up')"
        >
          <IconArrowDown aria-hidden="true" />
          <span class="sr-only">Move up</span>
        </button>

        <button
            type="button"
            :ref="setPadRef('left')"
            :class="padClass('left')"
            :aria-pressed="activeComputed === 'left'"
            :tabindex="tabIndexFor('left')"
            aria-label="Move left"
            :disabled="disabled"
            @click="onMove('left')"
            @keydown="onPadKeydown($event, 'left')"
        >
          <IconArrowDown aria-hidden="true" />
          <span class="sr-only">Move left</span>
        </button>

        <button
            type="button"
            :ref="setPadRef('right')"
            :class="padClass('right')"
            :aria-pressed="activeComputed === 'right'"
            :tabindex="tabIndexFor('right')"
            aria-label="Move right"
            :disabled="disabled"
            @click="onMove('right')"
            @keydown="onPadKeydown($event, 'right')"
        >
          <IconArrowDown aria-hidden="true" />
          <span class="sr-only">Move right</span>
        </button>

        <button
            type="button"
            :ref="setPadRef('down')"
            :class="padClass('down')"
            :aria-pressed="activeComputed === 'down'"
            :tabindex="tabIndexFor('down')"
            aria-label="Move down"
            :disabled="disabled"
            @click="onMove('down')"
            @keydown="onPadKeydown($event, 'down')"
        >
          <IconArrowDown aria-hidden="true" />
          <span class="sr-only">Move down</span>
        </button>
      </div>

      <div class="direction-labels" aria-hidden="true">
        <div><IconArrowUpDown aria-hidden="true" /> Levels</div>
        <div><IconArrowLeftRight aria-hidden="true" /> Companies</div>
      </div>
    </div>

    <!-- Bottom actions -->
    <div class="controller-card" aria-labelledby="controller-actions-title">
      <h3 class="controller-card-title" id="controller-actions-title">Actions</h3>
      <div class="controller-actions">
        <button
            type="button"
            class="btn blue"
            :disabled="disabled"
            @click="$emit('add-supplier')"
            aria-label="Add supplier"
        >
          <IconArrowDown aria-hidden="true" /> Add supplier
        </button>
        <button
            type="button"
            class="btn red"
            :disabled="disabled"
            @click="$emit('delete')"
            aria-label="Delete"
        >
          <IconTrash aria-hidden="true" /> Delete
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick, onMounted, watch, reactive } from 'vue'
import IconArrowDown from "@/components/icons/IconArrowDown.vue";
import IconTrash from "@/components/icons/IconTrash.vue";
import IconArrowUpDown from "@/components/icons/IconArrowUpDown.vue";
import IconArrowLeftRight from "@/components/icons/IconArrowLeftRight.vue";
import Button from "@/components/button/Button.vue";
import IconEdit from "@/components/icons/IconEdit.vue";
import { useNetworksStore } from '@/stores/networks';

type Dir = 'up' | 'down' | 'left' | 'right'

const emit = defineEmits<{
  (e: 'move', direction: Dir): void
  (e: 'add-supplier'): void
  (e: 'delete'): void
  (e: 'update:active', direction: Dir | null): void
  (e: 'edit-network'): void
}>()

const props = withDefaults(defineProps<{
  active?: Dir | null
  disabled?: boolean
}>(), {
  active: null,
  disabled: false
})

const localActive = ref<Dir | null>(null)
const activeComputed = computed<Dir | null>(() => props.active ?? localActive.value)
const networkStore = useNetworksStore();

/** --- focus management: keep one tabbable button --- */
const fallbackDir: Dir = 'up'
const focusDir = ref<Dir>(fallbackDir)

watch(activeComputed, (val) => {
  focusDir.value = val ?? fallbackDir
  nextTick(() => padRefs[focusDir.value]?.focus())
})

/** refs pulsanti direzione */
const padRefs: Record<Dir, HTMLButtonElement | null> = {
  up: null,
  down: null,
  left: null,
  right: null
}
const setPadRef = (dir: Dir) => (el: HTMLButtonElement | null) => { padRefs[dir] = el }

/** sposta attivo + focus */
function setActive(dir: Dir | null) {
  localActive.value = dir
  emit('update:active', dir)
  focusDir.value = dir ?? fallbackDir
}

function focusPad(dir: Dir) {
  nextTick(() => { padRefs[dir]?.focus() })
}

function onMove(dir: Dir) {
  networkStore.moveSelected(dir);
  focusPad(dir)
}

/** calcolo tabindex: solo focusDir è 0, tutti gli altri -1 */
function tabIndexFor(dir: Dir) {
  if (props.disabled) return -1
  return focusDir.value === dir ? 0 : -1
}

/** se il contenitore riceve focus, porta focus al bottone tabbabile */
function onPadGridFocus() {
  if (props.disabled) return
  focusPad(focusDir.value)
}

/** class builder per i bottoni direzione */
const padClass = (dir: Dir) => [
  'pad-btn',
  `pad-btn-${dir}`,
  activeComputed.value === dir ? 'active' : ''
]

function nextDirFromKey(key: string): Dir | null {
  switch (key) {
    case 'ArrowUp': return 'up'
    case 'ArrowDown': return 'down'
    case 'ArrowLeft': return 'left'
    case 'ArrowRight': return 'right'
    default: return null
  }
}

function onPadKeydown(e: KeyboardEvent, current: Dir) {
  if (props.disabled) return

  const nd = nextDirFromKey(e.key)
  if (nd) {
    e.preventDefault()
    setActive(nd)
    focusPad(nd)
    return
  }

  if (e.key === 'Enter' || e.key === ' ' || e.key === 'Spacebar') {
    e.preventDefault()
    onMove(current)
  }
}

const position = reactive({ x: 500, y: 500 });
const isDragging = ref(false);
const dragStart = reactive({ x: 0, y: 0 });

const onMouseDown = (e: MouseEvent) => {
  isDragging.value = true;
  dragStart.x = e.clientX - position.x;
  dragStart.y = e.clientY - position.y;
  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}

const onMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return;
  position.x = e.clientX - dragStart.x;
  position.y = e.clientY - dragStart.y;
}

const onMouseUp = () => {
  isDragging.value = false;
  document.removeEventListener('mousemove', onMouseMove);
  document.removeEventListener('mouseup', onMouseUp);
}

onMounted(() => {
  // inizializza focusDir al mount
  focusDir.value = activeComputed.value ?? fallbackDir
})
</script>

<style scoped>
@import "@/styles/networkController.css";
</style>
