<template>
  <a-modal
      :open="open"
      :title="'添加' + (componentDef?.name || '组件')"
      @cancel="$emit('update:open', false)"
      @ok="handleConfirm"
      :width="520"
      okText="插入到页面"
      cancelText="取消"
  >
    <div class="config-description" v-if="componentDef?.description">
      {{ componentDef.description }}
    </div>
    <a-form layout="vertical" class="config-form">
      <a-form-item
          v-for="field in componentDef?.fields || []"
          :key="field.key"
          :label="field.label"
          :required="field.required"
      >
        <!-- 文本输入 -->
        <a-input
            v-if="field.type === 'input'"
            v-model:value="formData[field.key]"
            :placeholder="field.placeholder || ''"
            size="small"
        />

        <!-- 文本域 -->
        <a-textarea
            v-else-if="field.type === 'textarea'"
            v-model:value="formData[field.key]"
            :placeholder="field.placeholder || ''"
            :rows="4"
            size="small"
        />

        <!-- 数字 -->
        <a-input-number
            v-else-if="field.type === 'number'"
            v-model:value="formData[field.key]"
            :min="field.min"
            :max="field.max"
            size="small"
            style="width: 100%"
        />

        <!-- 选择器 -->
        <a-select
            v-else-if="field.type === 'select'"
            v-model:value="formData[field.key]"
            size="small"
            style="width: 100%"
        >
          <a-select-option v-for="opt in field.options" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </a-select-option>
        </a-select>

        <!-- 开关 -->
        <a-switch
            v-else-if="field.type === 'switch'"
            v-model:checked="formData[field.key]"
            size="small"
        />

        <!-- 颜色 -->
        <div v-else-if="field.type === 'color'" class="color-field">
          <input
              type="color"
              :value="formData[field.key] || '#000000'"
              @input="(e: any) => formData[field.key] = e.target.value"
              class="color-picker"
          />
          <a-input v-model:value="formData[field.key]" size="small" style="flex:1" />
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { ComponentDef } from '@/utils/lowcode/componentLibrary'

const props = defineProps<{
  open: boolean
  componentDef: ComponentDef | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: [html: string]
}>()

const formData = ref<Record<string, any>>({})

// 当组件定义变化时，初始化表单默认值
watch(
    () => props.componentDef,
    (def) => {
      if (!def) return
      const data: Record<string, any> = {}
      for (const field of def.fields) {
        data[field.key] = field.defaultValue ?? ''
      }
      formData.value = data
    },
    { immediate: true },
)

function handleConfirm() {
  if (!props.componentDef) return
  const html = props.componentDef.generateHtml(formData.value)
  emit('confirm', html)
  emit('update:open', false)
}
</script>

<style scoped>
.config-description {
  font-size: 13px;
  color: #888;
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #f9f9f9;
  border-radius: 6px;
}

.config-form {
  max-height: 400px;
  overflow-y: auto;
}

.color-field {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-picker {
  width: 32px;
  height: 32px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  padding: 0;
  flex-shrink: 0;
}
</style>
