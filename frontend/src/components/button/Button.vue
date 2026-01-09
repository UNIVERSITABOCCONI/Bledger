<template>
  <button
      class="button"
      :class="[variantClass]"
      :disabled="disabled"
      :onclick="onclick"
      @click="emit('click', $event)"
  >
    <span v-if="icon && iconPosition === 'left'" class="icon">
      <slot name="icon"/>
    </span>
    <slot />
    <span v-if="icon && iconPosition === 'right'" class="icon">
      <slot name="icon"/>
    </span>
  </button>
</template>

<script setup lang="ts">
import {computed} from 'vue'
const emit = defineEmits(['click'])


const props = defineProps<{
  variant?: 'primary' | 'secondary' | 'alert' | 'negative' | 'icon' | 'secondary-dark' | 'link'
  icon?: boolean
  iconPosition?: 'left' | 'right'
  disabled?: boolean
  onclick?: () => void
}>()

const variantClass = computed(() => {
  switch (props.variant) {
    case 'secondary-dark':
      return 'button--secondary-dark'
    case 'secondary':
      return 'button--secondary'
    case 'alert':
      return 'button--alert'
    case 'negative':
      return 'button--negative'
    case 'icon':
      return 'button--icon'
    case 'link':
      return 'button--link'
    default:
      return 'button--primary'
  }
})

</script>

<style>
@import "@/styles/button.css";
</style>
