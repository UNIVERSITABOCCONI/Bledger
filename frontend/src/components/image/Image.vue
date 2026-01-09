<template>
  <picture class="image-picture">
    <img class="image-picture-image" :src="currentSrc" :alt="alt" loading="lazy" decoding="async" @load="$emit('load')"
      @error="onError" />
  </picture>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  src: string
  alt?: string
  /** optional custom placeholder path */
  placeholder?: string
}>(), {
  src: '/image-placeholder.png',
  alt: 'Image',
  placeholder: '/image-placeholder.png',
})

const emit = defineEmits<{
  (e: 'load'): void
  (e: 'error', err: Event): void
}>()

const currentSrc = ref(props.src)

watch(() => props.src, (val) => {
  currentSrc.value = val || props.placeholder
})

function onError(e: Event) {
  if (currentSrc.value !== props.placeholder) {
    currentSrc.value = props.placeholder
  }
  emit('error', e)
}
</script>

<style scoped>
@import "@/styles/image.css";
</style>
