<template>
  <div class="canvas-toolbar">
    <div class="toolbar-left">
      <a-tooltip title="撤销 (Ctrl+Z)">
        <a-button size="small" :disabled="!editorStore.canUndo" @click="editorStore.undo()">
          <template #icon><UndoOutlined /></template>
        </a-button>
      </a-tooltip>
      <a-tooltip title="重做 (Ctrl+Y)">
        <a-button size="small" :disabled="!editorStore.canRedo" @click="editorStore.redo()">
          <template #icon><RedoOutlined /></template>
        </a-button>
      </a-tooltip>
      <a-divider type="vertical" />
      <a-radio-group v-model:value="editorStore.deviceMode" size="small">
        <a-radio-button value="desktop">
          <DesktopOutlined />
        </a-radio-button>
        <a-radio-button value="tablet">
          <TabletOutlined />
        </a-radio-button>
        <a-radio-button value="mobile">
          <MobileOutlined />
        </a-radio-button>
      </a-radio-group>
    </div>
    <div class="toolbar-right">
      <a-tag v-if="editorStore.isDirty" color="orange">未保存</a-tag>
      <a-tag v-else color="green">已保存</a-tag>
      <a-button size="small" type="primary" @click="$emit('save')" :loading="saving">
        保存
      </a-button>
      <a-button size="small" @click="$emit('preview')">
        预览
      </a-button>
      <a-button size="small" @click="$emit('export')">
        导出
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLowcodeEditorStore } from '@/stores/lowcodeEditor'
import {
  UndoOutlined,
  RedoOutlined,
  DesktopOutlined,
  TabletOutlined,
  MobileOutlined,
} from '@ant-design/icons-vue'

const editorStore = useLowcodeEditorStore()

defineProps<{
  saving?: boolean
}>()

defineEmits<{
  save: []
  preview: []
  export: []
}>()
</script>

<style scoped>
.canvas-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
