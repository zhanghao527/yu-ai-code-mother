<template>
  <div class="material-container" :style="containerStyle">
    <slot />
    <div v-if="!hasChildren" class="empty-placeholder">
      拖拽组件到此处
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
  hasChildren?: boolean
}>()

const containerStyle = computed(() => {
  const layout = props.componentProps.layout || 'flex'
  const columns = props.componentProps.columns || 1
  const gap = props.componentProps.gap || '16px'

  const base: Record<string, string> = { gap }
  if (layout === 'grid') {
    base.display = 'grid'
    base.gridTemplateColumns = `repeat(${columns}, 1fr)`
  } else {
    base.display = 'flex'
    base.flexDirection = 'column'
  }

  return { ...base, ...props.componentStyle }
})
</script>

<style scoped>
.material-container {
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
}
</style>
