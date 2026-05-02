<template>
  <div class="material-columns" :style="columnsStyle">
    <slot />
    <div v-if="!hasChildren" class="empty-placeholder">
      拖拽组件到此处
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
  hasChildren?: boolean
}>()

const columnsStyle = computed(() => {
  const columns = props.componentProps.columns || 2
  const gap = props.componentProps.gap || '24px'
  return {
    display: 'grid',
    gridTemplateColumns: `repeat(${columns}, 1fr)`,
    gap,
    ...props.componentStyle,
  }
})
</script>

<style scoped>
.material-columns {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 80px;
}
.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  color: #bbb;
  font-size: 14px;
  grid-column: 1 / -1;
}
</style>
