<template>
  <section class="material-hero" :style="heroStyle">
    <h1>{{ props.componentProps.title || '欢迎' }}</h1>
    <p>{{ props.componentProps.subtitle || '' }}</p>
    <a v-if="props.componentProps.buttonText" :href="props.componentProps.buttonLink || '#'" class="hero-btn">
      {{ props.componentProps.buttonText }}
    </a>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()

const heroStyle = computed(() => ({
  backgroundImage: `linear-gradient(rgba(0,0,0,0.4), rgba(0,0,0,0.4)), url('${props.componentProps.backgroundImage || 'https://picsum.photos/1920/600'}')`,
  ...props.componentStyle,
}))
</script>

<style scoped>
.material-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  background-size: cover;
  background-position: center;
  color: #fff;
  text-align: center;
  padding: 48px 24px;
}
.material-hero h1 { font-size: 48px; font-weight: 700; margin-bottom: 16px; }
.material-hero p { font-size: 20px; opacity: 0.9; margin-bottom: 32px; max-width: 600px; }
.hero-btn {
  display: inline-block;
  padding: 14px 36px;
  background: var(--primary-color, #1890ff);
  color: #fff;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  text-decoration: none;
  transition: transform 0.2s;
}
.hero-btn:hover { transform: translateY(-2px); }
</style>
