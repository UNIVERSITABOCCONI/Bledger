<template>
  <div>
    <template v-if="status === 'done'">
      <IconChecked class="icon-done" />
    </template>
    <template v-else-if="status === 'in-progress'">
      <IconInProgress class="icon-in-progress" />
    </template>
    <template v-else>
      <IconMinus class="icon-pending" />
    </template>
  </div>
</template>

<script setup lang="ts">
import IconChecked from "@/components/icons/IconChecked.vue";
import IconInProgress from "@/components/icons/IconInProgress.vue";
import IconMinus from "@/components/icons/IconMinus.vue";

type StatusType = 'done' | 'in-progress' | 'pending'

const props = withDefaults(defineProps<{
  status?: StatusType
}>(), {
  status: 'pending'
})
</script>

<style scoped>
@import "@/styles/statusIcon.css";
</style>
