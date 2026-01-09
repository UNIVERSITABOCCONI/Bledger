<template>
  <div class="panel" :class="props.customClass">
    <div v-if="props.tag" class="panel-tag">
      {{ props.tag }}
    </div>
    <Title v-if="props.title" variant="h3" weight="bold" color="black" class="panel-title">{{ props.title }}</Title>
    <slot />
  </div>
</template>

<script setup lang="ts">
import Title from "@/components/title/Title.vue";

const props = defineProps<{
  title?: string
  customClass?: string
  tag?: string
}>()
</script>

<style>
@import "@/styles/panel.css";
</style>
