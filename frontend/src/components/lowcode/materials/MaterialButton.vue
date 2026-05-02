<template>
  <a class="material-button" :class="'variant-' + (props.componentProps.variant || 'primary')"
     :href="props.componentProps.href || '#'" :style="props.componentStyle">
    {{ props.componentProps.text || '按钮' }}
  </a>
</template>

<script setup lang="ts">
const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()
</script>

<style scoped>
.material-button {
  display: inline-block;
  padding: 12px 28px;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: opacity 0.2s;
}
.material-button:hover { opacity: 0.85; }
.variant-primary { background: var(--primary-color, #1890ff); color: #fff; border: none; }
.variant-secondary { background: transparent; color: var(--primary-color, #1890ff); border: 2px solid var(--primary-color, #1890ff); }
</style>
