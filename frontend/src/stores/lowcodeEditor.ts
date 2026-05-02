import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * Schema 中的组件节点
 */
export interface SchemaComponent {
  id: string
  type: string
  props: Record<string, any>
  style?: Record<string, any>
  children?: SchemaComponent[]
}

/**
 * Schema 中的页面
 */
export interface SchemaPage {
  id: string
  path: string
  name: string
  components: SchemaComponent[]
}

/**
 * 完整的页面 Schema
 */
export interface PageSchema {
  version: string
  globalStyle: {
    fontFamily: string
    primaryColor: string
  }
  pages: SchemaPage[]
}

/**
 * 低代码编辑器状态管理
 */
export const useLowcodeEditorStore = defineStore('lowcodeEditor', () => {
  // 当前 Schema
  const schema = ref<PageSchema>({
    version: '1.0',
    globalStyle: {
      fontFamily: 'system-ui, -apple-system, sans-serif',
      primaryColor: '#1890ff',
    },
    pages: [
      {
        id: 'page_1',
        path: '/',
        name: '首页',
        components: [],
      },
    ],
  })

  // 当前选中的组件 ID
  const selectedComponentId = ref<string | null>(null)

  // 当前悬浮的组件 ID
  const hoveredComponentId = ref<string | null>(null)

  // 撤销/重做栈
  const historyStack = ref<string[]>([])
  const futureStack = ref<string[]>([])

  // 是否有未保存的修改
  const isDirty = ref(false)

  // 当前编辑的页面索引
  const currentPageIndex = ref(0)

  // 预览设备模式
  const deviceMode = ref<'desktop' | 'tablet' | 'mobile'>('desktop')

  // 当前页面
  const currentPage = computed(() => {
    return schema.value.pages[currentPageIndex.value] || schema.value.pages[0]
  })

  // 当前选中的组件
  const selectedComponent = computed(() => {
    if (!selectedComponentId.value) return null
    return findComponentById(currentPage.value.components, selectedComponentId.value)
  })

  // 是否可以撤销
  const canUndo = computed(() => historyStack.value.length > 0)

  // 是否可以重做
  const canRedo = computed(() => futureStack.value.length > 0)

  /**
   * 设置 Schema（从后端加载时使用）
   */
  function setSchema(newSchema: PageSchema) {
    schema.value = newSchema
    isDirty.value = false
    historyStack.value = []
    futureStack.value = []
  }

  /**
   * 记录历史快照（在每次修改前调用）
   */
  function pushHistory() {
    historyStack.value.push(JSON.stringify(schema.value))
    // 限制历史栈大小
    if (historyStack.value.length > 50) {
      historyStack.value.shift()
    }
    // 清空重做栈
    futureStack.value = []
  }

  /**
   * 撤销
   */
  function undo() {
    if (historyStack.value.length === 0) return
    futureStack.value.push(JSON.stringify(schema.value))
    const previous = historyStack.value.pop()!
    schema.value = JSON.parse(previous)
    isDirty.value = true
  }

  /**
   * 重做
   */
  function redo() {
    if (futureStack.value.length === 0) return
    historyStack.value.push(JSON.stringify(schema.value))
    const next = futureStack.value.pop()!
    schema.value = JSON.parse(next)
    isDirty.value = true
  }

  /**
   * 添加组件到当前页面
   */
  function addComponent(component: SchemaComponent, index?: number) {
    pushHistory()
    const components = currentPage.value.components
    if (index !== undefined && index >= 0 && index <= components.length) {
      components.splice(index, 0, component)
    } else {
      components.push(component)
    }
    isDirty.value = true
  }

  /**
   * 删除组件
   */
  function deleteComponent(componentId: string) {
    pushHistory()
    const removed = removeFromArray(currentPage.value.components, componentId)
    if (removed && selectedComponentId.value === componentId) {
      selectedComponentId.value = null
    }
    isDirty.value = true
  }

  /**
   * 更新组件属性
   */
  function updateComponentProps(componentId: string, props: Record<string, any>) {
    pushHistory()
    const comp = findComponentById(currentPage.value.components, componentId)
    if (comp) {
      comp.props = { ...comp.props, ...props }
      isDirty.value = true
    }
  }

  /**
   * 更新组件样式
   */
  function updateComponentStyle(componentId: string, style: Record<string, any>) {
    pushHistory()
    const comp = findComponentById(currentPage.value.components, componentId)
    if (comp) {
      comp.style = { ...comp.style, ...style }
      isDirty.value = true
    }
  }

  /**
   * 移动组件位置
   */
  function moveComponent(fromIndex: number, toIndex: number) {
    pushHistory()
    const components = currentPage.value.components
    const [moved] = components.splice(fromIndex, 1)
    components.splice(toIndex, 0, moved)
    isDirty.value = true
  }

  /**
   * 选中组件
   */
  function selectComponent(componentId: string | null) {
    selectedComponentId.value = componentId
  }

  /**
   * 悬浮组件
   */
  function hoverComponent(componentId: string | null) {
    hoveredComponentId.value = componentId
  }

  /**
   * 生成唯一组件 ID
   */
  function generateComponentId(): string {
    return 'comp_' + Date.now().toString(36) + '_' + Math.random().toString(36).substring(2, 6)
  }

  // ========== 工具函数 ==========

  function findComponentById(
    components: SchemaComponent[],
    id: string,
  ): SchemaComponent | null {
    for (const comp of components) {
      if (comp.id === id) return comp
      if (comp.children) {
        const found = findComponentById(comp.children, id)
        if (found) return found
      }
    }
    return null
  }

  function removeFromArray(components: SchemaComponent[], id: string): boolean {
    for (let i = 0; i < components.length; i++) {
      if (components[i].id === id) {
        components.splice(i, 1)
        return true
      }
      if (components[i].children) {
        if (removeFromArray(components[i].children!, id)) return true
      }
    }
    return false
  }

  return {
    schema,
    selectedComponentId,
    hoveredComponentId,
    historyStack,
    futureStack,
    isDirty,
    currentPageIndex,
    deviceMode,
    currentPage,
    selectedComponent,
    canUndo,
    canRedo,
    setSchema,
    addComponent,
    deleteComponent,
    updateComponentProps,
    updateComponentStyle,
    moveComponent,
    selectComponent,
    hoverComponent,
    undo,
    redo,
    generateComponentId,
  }
})
