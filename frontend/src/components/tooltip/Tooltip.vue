<template>
  <div class="tooltip-wrapper" @mouseenter="show" @mouseleave="hide" @focus="show" @blur="hide" tabindex="0">
    <slot />
    <div v-if="isVisible" :class="['tooltip', position]" role="tooltip">
      <slot name="content">{{ content }}</slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
  content?: string;
  position?: 'top' | 'bottom' | 'left' | 'right';
}>();

const isVisible = ref(false);

const show = () => {
  isVisible.value = true;
};

const hide = () => {
  isVisible.value = false;
};
</script>

<style scoped>
@import "@/styles/tooltip.css";
</style>
