<template>
  <hr class="material-divider" :style="props.componentStyle" />
</template>

<script setup lang="ts">
const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()
</script>

<style scoped>
.material-divider { border: none; border-top: 1px solid #e8e8e8; margin: 24px 0; }
</style>
