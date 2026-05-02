<template>
  <footer class="material-footer" :style="props.componentStyle">
    <div class="footer-links">
      <a v-for="(link, i) in (props.componentProps.links || [])" :key="i" :href="link.href || '#'">
        {{ link.text || '链接' }}
      </a>
    </div>
    <div class="footer-text">{{ props.componentProps.text || '© 2024 All Rights Reserved' }}</div>
  </footer>
</template>

<script setup lang="ts">
const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()
</script>

<style scoped>
.material-footer { background: #1a1a2e; color: #ccc; padding: 48px 24px; text-align: center; }
.footer-links { display: flex; justify-content: center; gap: 24px; margin-bottom: 16px; }
.footer-links a { color: #aaa; font-size: 14px; text-decoration: none; }
.footer-links a:hover { color: #fff; }
.footer-text { font-size: 13px; color: #888; }
</style>
