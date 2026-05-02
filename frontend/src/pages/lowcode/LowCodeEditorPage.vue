<template>
  <div class="lowcode-editor">
    <!-- 顶部工具栏 -->
    <CanvasToolbar :saving="saving" @save="handleSave" @preview="handlePreview" @export="handleExport" />

    <!-- 三栏布局 -->
    <div class="editor-body">
      <!-- 左侧：组件面板 -->
      <div class="left-panel">
        <ComponentPanel />
      </div>

      <!-- 中间：画布区域 -->
      <div class="center-panel">
        <div
          class="canvas-area"
          :class="'device-' + editorStore.deviceMode"
          @drop="onDrop"
          @dragover.prevent="onDragOver"
          @dragleave="onDragLeave"
          @click.self="editorStore.selectComponent(null)"
        >
          <div v-if="editorStore.currentPage.components.length === 0 && !isDragOver" class="canvas-empty">
            <div class="empty-icon">🎨</div>
            <p>从左侧拖拽组件到这里开始搭建页面</p>
          </div>

          <div v-if="isDragOver && editorStore.currentPage.components.length === 0" class="drop-zone active">
            释放以添加组件
          </div>

          <SchemaRenderer :components="editorStore.currentPage.components" />

          <!-- 底部拖放区域 -->
          <div
            v-if="editorStore.currentPage.components.length > 0"
            class="drop-zone-bottom"
            :class="{ active: isDragOver }"
          >
            {{ isDragOver ? '释放以添加到底部' : '' }}
          </div>
        </div>
      </div>

      <!-- 右侧：属性面板 -->
      <div class="right-panel">
        <PropertyPanel />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLowcodeEditorStore, type SchemaComponent } from '@/stores/lowcodeEditor'
import { getLatestSchema, saveSchema, exportSchema } from '@/api/lowcodeController'
import { getAppVoById } from '@/api/appController'
import ComponentPanel from '@/components/lowcode/ComponentPanel.vue'
import SchemaRenderer from '@/components/lowcode/SchemaRenderer.vue'
import PropertyPanel from '@/components/lowcode/PropertyPanel.vue'
import CanvasToolbar from '@/components/lowcode/CanvasToolbar.vue'

const route = useRoute()
const router = useRouter()
const editorStore = useLowcodeEditorStore()

const appId = ref<number>(0)
const saving = ref(false)
const isDragOver = ref(false)

// 自动保存定时器
let autoSaveTimer: ReturnType<typeof setTimeout> | null = null

// 加载应用和 Schema
onMounted(async () => {
  const id = Number(route.params.id)
  if (!id || id <= 0) {
    message.error('应用ID无效')
    router.push('/')
    return
  }
  appId.value = id

  // 验证应用存在
  try {
    const appRes = await getAppVoById({ id })
    if (appRes.data.code !== 0 || !appRes.data.data) {
      message.error('应用不存在')
      router.push('/')
      return
    }
  } catch {
    message.error('获取应用信息失败')
    router.push('/')
    return
  }

  // 加载 Schema
  try {
    const res = await getLatestSchema({ appId: id })
    if (res.data.code === 0 && res.data.data) {
      const schemaData = res.data.data as any
      if (schemaData.schemaContent) {
        editorStore.setSchema(JSON.parse(schemaData.schemaContent))
      }
    }
  } catch {
    message.warning('加载 Schema 失败，使用默认模板')
  }

  // 监听键盘快捷键
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
})

// 监听 Schema 变化，触发自动保存
watch(
  () => editorStore.isDirty,
  (dirty) => {
    if (dirty) {
      if (autoSaveTimer) clearTimeout(autoSaveTimer)
      autoSaveTimer = setTimeout(() => {
        handleSave()
      }, 2000)
    }
  },
)

// 拖放处理
function onDragOver(event: DragEvent) {
  event.preventDefault()
  isDragOver.value = true
}

function onDragLeave() {
  isDragOver.value = false
}

function onDrop(event: DragEvent) {
  event.preventDefault()
  isDragOver.value = false

  const data = event.dataTransfer?.getData('application/json')
  if (!data) return

  try {
    const template = JSON.parse(data)
    const defaultSchema = JSON.parse(template.defaultSchema)

    const newComponent: SchemaComponent = {
      id: editorStore.generateComponentId(),
      type: defaultSchema.type,
      props: defaultSchema.props || {},
      style: defaultSchema.style || {},
      children: defaultSchema.children || undefined,
    }

    editorStore.addComponent(newComponent)
    editorStore.selectComponent(newComponent.id)
  } catch (e) {
    console.error('拖放处理失败', e)
  }
}

// 保存
async function handleSave() {
  if (!appId.value) return
  saving.value = true
  try {
    const res = await saveSchema({
      appId: appId.value,
      schemaContent: JSON.stringify(editorStore.schema),
    })
    if (res.data.code === 0) {
      editorStore.isDirty = false
      message.success('保存成功 (v' + res.data.data + ')')
    } else {
      message.error('保存失败: ' + res.data.message)
    }
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 预览
function handlePreview() {
  // 在新窗口中打开预览
  const previewWindow = window.open('', '_blank')
  if (!previewWindow) {
    message.error('无法打开预览窗口，请允许弹窗')
    return
  }

  // 生成预览 HTML
  const html = generatePreviewHtml()
  previewWindow.document.write(html)
  previewWindow.document.close()
}

// 导出
async function handleExport() {
  if (!appId.value) return
  // 先保存
  await handleSave()
  try {
    const res = await exportSchema({ appId: appId.value })
    if (res.data.code === 0) {
      message.success('导出成功')
    } else {
      message.error('导出失败: ' + res.data.message)
    }
  } catch {
    message.error('导出失败')
  }
}

// 键盘快捷键
function handleKeydown(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key === 'z') {
    event.preventDefault()
    if (event.shiftKey) {
      editorStore.redo()
    } else {
      editorStore.undo()
    }
  }
  if ((event.ctrlKey || event.metaKey) && event.key === 'y') {
    event.preventDefault()
    editorStore.redo()
  }
  if ((event.ctrlKey || event.metaKey) && event.key === 's') {
    event.preventDefault()
    handleSave()
  }
  if (event.key === 'Delete' || event.key === 'Backspace') {
    if (editorStore.selectedComponentId && !(event.target instanceof HTMLInputElement) && !(event.target instanceof HTMLTextAreaElement)) {
      event.preventDefault()
      editorStore.deleteComponent(editorStore.selectedComponentId)
    }
  }
}

// 生成预览 HTML（简化版，完整版由后端 SchemaExporter 处理）
function generatePreviewHtml(): string {
  const schema = editorStore.schema
  const primaryColor = schema.globalStyle?.primaryColor || '#1890ff'
  const fontFamily = schema.globalStyle?.fontFamily || 'system-ui, -apple-system, sans-serif'

  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>预览</title>
  <style>
    :root { --primary-color: ${primaryColor}; }
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: ${fontFamily}; line-height: 1.6; color: #333; }
    p { text-align: center; padding: 48px; color: #999; }
  </style>
</head>
<body>
  <p>预览功能需要通过后端导出接口生成完整页面。<br>请点击"导出"按钮后访问部署地址查看效果。</p>
</body>
</html>`
}
</script>

<style scoped>
.lowcode-editor {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.editor-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧面板 */
.left-panel {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
}

/* 中间画布 */
.center-panel {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 24px;
  overflow-y: auto;
  background: #f0f2f5;
}

.canvas-area {
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 4px;
  min-height: 600px;
  transition: width 0.3s ease;
  overflow: hidden;
}

.canvas-area.device-desktop { width: 100%; max-width: 1200px; }
.canvas-area.device-tablet { width: 768px; }
.canvas-area.device-mobile { width: 375px; }

.canvas-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #bbb;
}

.empty-icon { font-size: 64px; margin-bottom: 16px; }
.canvas-empty p { font-size: 15px; }

.drop-zone {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  margin: 16px;
  color: #bbb;
  font-size: 14px;
  transition: all 0.2s;
}

.drop-zone.active {
  border-color: #1890ff;
  background: #e6f7ff;
  color: #1890ff;
}

.drop-zone-bottom {
  min-height: 40px;
  border: 2px dashed transparent;
  margin: 8px 16px 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #1890ff;
  transition: all 0.2s;
}

.drop-zone-bottom.active {
  border-color: #1890ff;
  background: #e6f7ff;
  min-height: 60px;
}

/* 右侧面板 */
.right-panel {
  width: 300px;
  background: #fff;
  border-left: 1px solid #e8e8e8;
  overflow-y: auto;
}
</style>
