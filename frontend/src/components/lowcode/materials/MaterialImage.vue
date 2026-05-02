<template>
  <img
    class="material-image"
    :src="props.componentProps.src || 'https://picsum.photos/800/400'"
    :alt="props.componentProps.alt || '图片'"
    :style="props.componentStyle"
  />
</template>

<script setup lang="ts">
const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()
</script>

<style scoped>
.material-image { max-width: 100%; height: auto; border-radius: 8px; }
</style>
