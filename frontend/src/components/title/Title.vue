<template>
  <div :class="['title', variantClass, weightClass, colorClass, extraClass, sans && 'title-sans']">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type Variant = 'display' | 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6'
type Weight = 'regular' | 'medium' | 'semi-bold' | 'bold'
type Color = 'black' | 'blue' | 'white' | 'dark-blue'

interface Props {
  variant: Variant,
  weight: Weight,
  color: Color,
  extraClass?: string,
  sans?: boolean
}

const props = defineProps<Props>()

const variantClass = computed(() => `variant-${props.variant}`)
const weightClass = computed(() => `weight-${props.weight}`)
const colorClass = computed(() => `color-${props.color}`)

</script>

<style scoped>
@import "@/styles/title.css";
</style>
