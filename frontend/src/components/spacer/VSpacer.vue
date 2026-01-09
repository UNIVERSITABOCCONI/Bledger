<template>
  <div class="vSpacer" :class="`vSpacer--${props.size}`"></div>
</template>


<script setup lang="ts">
const props = defineProps<{
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
}>()
</script>

<style scoped>
@import "@/styles/vSpace.css";
</style>