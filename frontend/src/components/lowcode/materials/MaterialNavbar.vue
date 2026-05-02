<template>
  <nav class="material-navbar" :style="props.componentStyle">
    <div class="nav-title">{{ props.componentProps.title || '网站标题' }}</div>
    <div class="nav-links">
      <a v-for="(link, i) in (props.componentProps.links || [])" :key="i" :href="link.href || '#'">
        {{ link.text || '链接' }}
      </a>
    </div>
  </nav>
</template>

<script setup lang="ts">
const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()
</script>

<style scoped>
.material-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.nav-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color, #1890ff);
}
.nav-links {
  display: flex;
  gap: 24px;
}
.nav-links a {
  color: #555;
  font-size: 15px;
  text-decoration: none;
  transition: color 0.2s;
}
.nav-links a:hover {
  color: var(--primary-color, #1890ff);
}
</style>
