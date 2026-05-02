<template>
  <div class="property-panel">
    <div v-if="!editorStore.selectedComponent" class="empty-state">
      <p>点击画布中的组件进行编辑</p>
    </div>

    <template v-else>
      <h3 class="panel-title">
        {{ editorStore.selectedComponent.type }}
        <span class="component-id">{{ editorStore.selectedComponent.id }}</span>
      </h3>

      <a-tabs size="small">
        <a-tab-pane key="props" tab="属性">
          <div class="props-form">
            <div v-for="field in propsFields" :key="field.key" class="form-item">
              <label class="form-label">{{ field.label }}</label>

              <!-- 输入框 -->
              <a-input
                v-if="field.type === 'input'"
                :value="getPropsValue(field.key)"
                @change="(e: any) => updateProp(field.key, e.target.value)"
                size="small"
              />

              <!-- 文本域 -->
              <a-textarea
                v-else-if="field.type === 'textarea'"
                :value="getPropsValue(field.key)"
                @change="(e: any) => updateProp(field.key, e.target.value)"
                :rows="3"
                size="small"
              />

              <!-- 选择器 -->
              <a-select
                v-else-if="field.type === 'select'"
                :value="getPropsValue(field.key)"
                @change="(val: any) => updateProp(field.key, val)"
                size="small"
                style="width: 100%"
              >
                <a-select-option v-for="opt in field.options" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </a-select-option>
              </a-select>

              <!-- 滑块 -->
              <a-slider
                v-else-if="field.type === 'slider'"
                :value="getPropsValue(field.key) || field.min || 1"
                @change="(val: any) => updateProp(field.key, val)"
                :min="field.min || 1"
                :max="field.max || 10"
              />

              <!-- 开关 -->
              <a-switch
                v-else-if="field.type === 'switch'"
                :checked="getPropsValue(field.key)"
                @change="(val: any) => updateProp(field.key, val)"
                size="small"
              />

              <!-- 默认输入框 -->
              <a-input
                v-else
                :value="getPropsValue(field.key)"
                @change="(e: any) => updateProp(field.key, e.target.value)"
                size="small"
              />
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="style" tab="样式">
          <div class="props-form">
            <div class="form-item">
              <label class="form-label">背景色</label>
              <a-input
                :value="getStyleValue('backgroundColor')"
                @change="(e: any) => updateStyle('backgroundColor', e.target.value)"
                size="small"
                placeholder="#ffffff"
              />
            </div>
            <div class="form-item">
              <label class="form-label">内边距</label>
              <a-input
                :value="getStyleValue('padding')"
                @change="(e: any) => updateStyle('padding', e.target.value)"
                size="small"
                placeholder="24px"
              />
            </div>
            <div class="form-item">
              <label class="form-label">外边距</label>
              <a-input
                :value="getStyleValue('margin')"
                @change="(e: any) => updateStyle('margin', e.target.value)"
                size="small"
                placeholder="0 auto"
              />
            </div>
            <div class="form-item">
              <label class="form-label">圆角</label>
              <a-input
                :value="getStyleValue('borderRadius')"
                @change="(e: any) => updateStyle('borderRadius', e.target.value)"
                size="small"
                placeholder="8px"
              />
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useLowcodeEditorStore } from '@/stores/lowcodeEditor'
import { listComponentTemplates } from '@/api/lowcodeController'

const editorStore = useLowcodeEditorStore()

interface PropsField {
  key: string
  label: string
  type: string
  options?: { label: string; value: any }[]
  min?: number
  max?: number
}

const allTemplates = ref<Record<string, any>>({})

// 加载组件模板（获取 propsSchema）
async function loadTemplates() {
  try {
    const res = await listComponentTemplates()
    if (res.data.code === 0 && res.data.data) {
      for (const t of res.data.data as any[]) {
        allTemplates.value[t.componentKey] = t
      }
    }
  } catch (e) {
    console.error('加载模板失败', e)
  }
}
loadTemplates()

// 当前选中组件的属性字段定义
const propsFields = computed<PropsField[]>(() => {
  const comp = editorStore.selectedComponent
  if (!comp) return []

  const template = allTemplates.value[comp.type]
  if (!template || !template.propsSchema) return []

  try {
    const fields = JSON.parse(template.propsSchema)
    // 过滤掉 arrayEditor 类型（暂不支持复杂编辑）
    return fields.filter((f: PropsField) => f.type !== 'arrayEditor')
  } catch {
    return []
  }
})

function getPropsValue(key: string) {
  return editorStore.selectedComponent?.props?.[key]
}

function getStyleValue(key: string) {
  return editorStore.selectedComponent?.style?.[key]
}

function updateProp(key: string, value: any) {
  if (!editorStore.selectedComponentId) return
  editorStore.updateComponentProps(editorStore.selectedComponentId, { [key]: value })
}

function updateStyle(key: string, value: any) {
  if (!editorStore.selectedComponentId) return
  editorStore.updateComponentStyle(editorStore.selectedComponentId, { [key]: value })
}
</script>

<style scoped>
.property-panel {
  padding: 16px;
  height: 100%;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
  font-size: 14px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1a1a1a;
}

.component-id {
  font-size: 11px;
  color: #999;
  font-weight: 400;
  margin-left: 8px;
}

.props-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}
</style>
