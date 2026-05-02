<template>
  <component :is="'h' + level" class="material-heading" :style="props.componentStyle">
    {{ props.componentProps.text || '标题文本' }}
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()

const level = computed(() => {
  const l = props.componentProps.level || 2
  return Math.max(1, Math.min(6, l))
})
</script>

<style scoped>
.material-heading { padding: 8px 0; color: #1a1a1a; }
</style>
