<template>
  <div class="material-card" :style="props.componentStyle">
    <img :src="props.componentProps.image || 'https://picsum.photos/400/250'" :alt="props.componentProps.title || '卡片'" />
    <div class="card-body">
      <h3>{{ props.componentProps.title || '卡片标题' }}</h3>
      <p>{{ props.componentProps.description || '卡片描述内容' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()
</script>

<style scoped>
.material-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
  background: #fff;
}
.material-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12); }
.material-card img { width: 100%; height: 200px; object-fit: cover; }
.card-body { padding: 20px; }
.card-body h3 { font-size: 18px; margin-bottom: 8px; color: #1a1a1a; }
.card-body p { font-size: 14px; color: #666; line-height: 1.6; }
</style>
