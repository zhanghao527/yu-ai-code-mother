<template>
  <div class="component-panel">
    <h3 class="panel-title">组件</h3>

    <div v-for="category in categories" :key="category.key" class="category-section">
      <div class="category-title">{{ category.label }}</div>
      <div class="component-grid">
        <div
          v-for="template in getTemplatesByCategory(category.key)"
          :key="template.componentKey"
          class="component-item"
          draggable="true"
          @dragstart="onDragStart($event, template)"
        >
          <div class="component-icon">
            <component :is="getIconComponent(template.icon)" v-if="getIconComponent(template.icon)" />
            <span v-else>📦</span>
          </div>
          <div class="component-name">{{ template.name }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listComponentTemplates } from '@/api/lowcodeController'
import {
  AppstoreOutlined,
  ColumnWidthOutlined,
  FontSizeOutlined,
  AlignLeftOutlined,
  PictureOutlined,
  BorderOutlined,
  LineOutlined,
  MenuOutlined,
  CrownOutlined,
  CreditCardOutlined,
  AppstoreAddOutlined,
  PlaySquareOutlined,
  LayoutOutlined,
} from '@ant-design/icons-vue'

interface TemplateItem {
  name: string
  componentKey: string
  category: string
  icon: string
  defaultSchema: string
  propsSchema: string
}

const templates = ref<TemplateItem[]>([])

const categories = [
  { key: 'layout', label: '布局组件' },
  { key: 'basic', label: '基础组件' },
  { key: 'business', label: '业务组件' },
]

const iconMap: Record<string, any> = {
  AppstoreOutlined,
  ColumnWidthOutlined,
  FontSizeOutlined,
  AlignLeftOutlined,
  PictureOutlined,
  BorderOutlined,
  LineOutlined,
  MenuOutlined,
  CrownOutlined,
  CreditCardOutlined,
  AppstoreAddOutlined,
  PlaySquareOutlined,
  LayoutOutlined,
}

function getIconComponent(iconName: string) {
  return iconMap[iconName] || null
}

function getTemplatesByCategory(category: string) {
  return templates.value.filter((t) => t.category === category)
}

function onDragStart(event: DragEvent, template: TemplateItem) {
  event.dataTransfer?.setData('application/json', JSON.stringify(template))
  event.dataTransfer!.effectAllowed = 'copy'
}

onMounted(async () => {
  try {
    const res = await listComponentTemplates()
    if (res.data.code === 0 && res.data.data) {
      templates.value = res.data.data as TemplateItem[]
    }
  } catch (e) {
    console.error('加载组件模板失败', e)
  }
})
</script>

<style scoped>
.component-panel {
  padding: 16px;
  height: 100%;
  overflow-y: auto;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1a1a1a;
}

.category-section {
  margin-bottom: 20px;
}

.category-title {
  font-size: 13px;
  color: #888;
  margin-bottom: 8px;
  font-weight: 500;
}

.component-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.component-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: grab;
  transition: all 0.2s;
  background: #fafafa;
}

.component-item:hover {
  border-color: #1890ff;
  background: #e6f7ff;
}

.component-item:active {
  cursor: grabbing;
}

.component-icon {
  font-size: 20px;
  color: #1890ff;
}

.component-name {
  font-size: 12px;
  color: #555;
  text-align: center;
}
</style>
