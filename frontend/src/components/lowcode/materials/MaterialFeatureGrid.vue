<template>
  <div class="material-feature-grid" :style="gridStyle">
    <div v-for="(feature, i) in features" :key="i" class="feature-item">
      <div class="feature-icon">{{ feature.icon || '⭐' }}</div>
      <h3>{{ feature.title || '特性' }}</h3>
      <p>{{ feature.description || '特性描述' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()

const features = computed(() => props.componentProps.features || [])
const gridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${props.componentProps.columns || 3}, 1fr)`,
  ...props.componentStyle,
}))
</script>

<style scoped>
.material-feature-grid {
  display: grid;
  gap: 32px;
  padding: 64px 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.feature-item { text-align: center; padding: 24px; }
.feature-icon { font-size: 48px; margin-bottom: 16px; }
.feature-item h3 { font-size: 20px; margin-bottom: 12px; color: #1a1a1a; }
.feature-item p { font-size: 15px; color: #666; line-height: 1.6; }
</style>
