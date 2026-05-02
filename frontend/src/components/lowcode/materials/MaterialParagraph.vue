<template>
  <p class="material-paragraph" :style="props.componentStyle">
    {{ props.componentProps.text || '段落文本内容' }}
  </p>
</template>

<script setup lang="ts">
const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()
</script>

<style scoped>
.material-paragraph { font-size: 15px; line-height: 1.8; color: #555; padding: 4px 0; }
</style>
