<template>
  <div class="material-carousel" :style="{ height: props.componentProps.height || '400px', ...props.componentStyle }">
    <div class="carousel-track" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
      <div v-for="(img, i) in images" :key="i" class="carousel-slide">
        <img :src="img" :alt="'轮播图 ' + (i + 1)" />
      </div>
    </div>
    <button class="carousel-btn carousel-prev" @click="move(-1)">&#10094;</button>
    <button class="carousel-btn carousel-next" @click="move(1)">&#10095;</button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  componentProps: Record<string, any>
  componentStyle?: Record<string, any>
}>()

const currentSlide = ref(0)

const images = computed(() => {
  return props.componentProps.images || [
    'https://picsum.photos/1200/400?random=1',
    'https://picsum.photos/1200/400?random=2',
    'https://picsum.photos/1200/400?random=3',
  ]
})

function move(direction: number) {
  const total = images.value.length
  currentSlide.value = (currentSlide.value + direction + total) % total
}
</script>

<style scoped>
.material-carousel { position: relative; overflow: hidden; }
.carousel-track { display: flex; transition: transform 0.5s ease; height: 100%; }
.carousel-slide { min-width: 100%; height: 100%; }
.carousel-slide img { width: 100%; height: 100%; object-fit: cover; }
.carousel-btn {
  position: absolute; top: 50%; transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.8); border: none;
  width: 40px; height: 40px; border-radius: 50%;
  cursor: pointer; font-size: 18px; z-index: 10;
}
.carousel-prev { left: 16px; }
.carousel-next { right: 16px; }
</style>
