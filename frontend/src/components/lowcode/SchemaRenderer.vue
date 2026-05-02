<template>
  <div class="schema-renderer">
    <div
      v-for="(comp, index) in components"
      :key="comp.id"
      class="renderer-wrapper"
      :class="{
        'is-selected': editorStore.selectedComponentId === comp.id,
        'is-hovered': editorStore.hoveredComponentId === comp.id,
      }"
      @click.stop="editorStore.selectComponent(comp.id)"
      @mouseenter="editorStore.hoverComponent(comp.id)"
      @mouseleave="editorStore.hoverComponent(null)"
    >
      <!-- 选中工具栏 -->
      <div v-if="editorStore.selectedComponentId === comp.id" class="component-toolbar">
        <span class="component-type-label">{{ comp.type }}</span>
        <button class="toolbar-btn" title="上移" :disabled="index === 0" @click.stop="moveUp(index)">↑</button>
        <button class="toolbar-btn" title="下移" :disabled="index === components.length - 1" @click.stop="moveDown(index)">↓</button>
        <button class="toolbar-btn delete-btn" title="删除" @click.stop="deleteComp(comp.id)">✕</button>
      </div>

      <!-- 渲染实际组件 -->
      <component
        :is="getMaterialComponent(comp.type)"
        v-if="getMaterialComponent(comp.type)"
        :componentProps="comp.props || {}"
        :componentStyle="comp.style || {}"
        :hasChildren="comp.children && comp.children.length > 0"
      >
        <!-- 递归渲染子组件 -->
        <SchemaRenderer
          v-if="comp.children && comp.children.length > 0"
          :components="comp.children"
        />
      </component>

      <!-- 未知组件 -->
      <div v-else class="unknown-component">
        未知组件: {{ comp.type }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getMaterialComponent } from './materials/index'
import { useLowcodeEditorStore, type SchemaComponent } from '@/stores/lowcodeEditor'

const props = defineProps<{
  components: SchemaComponent[]
}>()

const editorStore = useLowcodeEditorStore()

function moveUp(index: number) {
  if (index > 0) {
    editorStore.moveComponent(index, index - 1)
  }
}

function moveDown(index: number) {
  if (index < props.components.length - 1) {
    editorStore.moveComponent(index, index + 1)
  }
}

function deleteComp(id: string) {
  editorStore.deleteComponent(id)
}
</script>

<style scoped>
.renderer-wrapper {
  position: relative;
  transition: outline 0.15s ease;
  cursor: pointer;
}

.renderer-wrapper.is-hovered {
  outline: 2px dashed #1890ff;
  outline-offset: 2px;
}

.renderer-wrapper.is-selected {
  outline: 2px solid #52c41a;
  outline-offset: 2px;
}

.component-toolbar {
  position: absolute;
  top: -32px;
  left: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  background: #52c41a;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px 4px 0 0;
  font-size: 12px;
  z-index: 100;
  white-space: nowrap;
}

.component-type-label {
  margin-right: 8px;
  font-weight: 500;
}

.toolbar-btn {
  background: none;
  border: none;
  color: #fff;
  cursor: pointer;
  padding: 2px 6px;
  font-size: 12px;
  border-radius: 3px;
  transition: background 0.15s;
}

.toolbar-btn:hover { background: rgba(255, 255, 255, 0.2); }
.toolbar-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.delete-btn:hover { background: #ff4d4f; }

.unknown-component {
  padding: 24px;
  text-align: center;
  color: #999;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
}
</style>
