<template>
  <div id="appChatPage">
    <!-- 顶部栏 -->
    <div class="header-bar">
      <div class="header-left">
        <h1 class="app-name">{{ appInfo?.appName || '网站生成器' }}</h1>
        <a-tag v-if="appInfo?.codeGenType" color="blue" class="code-gen-type-tag">
          {{ formatCodeGenType(appInfo.codeGenType) }}
        </a-tag>
      </div>
      <div class="header-right">
        <a-button type="default" @click="showAppDetail">
          <template #icon>
            <InfoCircleOutlined />
          </template>
          应用详情
        </a-button>
        <a-button
            type="primary"
            ghost
            @click="downloadCode"
            :loading="downloading"
            :disabled="!isOwner"
        >
          <template #icon>
            <DownloadOutlined />
          </template>
          下载代码
        </a-button>
        <a-button type="primary" @click="deployApp" :loading="deploying">
          <template #icon>
            <CloudUploadOutlined />
          </template>
          部署
        </a-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧对话区域 -->
      <div class="chat-section" v-show="rightPanelMode !== 'lowcode'">
        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainer">
          <!-- 加载更多按钮 -->
          <div v-if="hasMoreHistory" class="load-more-container">
            <a-button type="link" @click="loadMoreHistory" :loading="loadingHistory" size="small">
              加载更多历史消息
            </a-button>
          </div>
          <div v-for="(message, index) in messages" :key="index" class="message-item">
            <div v-if="message.type === 'user'" class="user-message">
              <div class="message-content">{{ message.content }}</div>
              <div class="message-avatar">
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
              </div>
            </div>
            <div v-else class="ai-message">
              <div class="message-avatar">
                <a-avatar :src="aiAvatar" />
              </div>
              <div class="message-content">
                <MarkdownRenderer v-if="message.content" :content="message.content" />
                <div v-if="message.loading" class="loading-indicator">
                  <a-spin size="small" />
                  <span>AI 正在思考...</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 选中元素信息展示 -->
        <a-alert
            v-if="selectedElementInfo"
            class="selected-element-alert"
            type="info"
            closable
            @close="clearSelectedElement"
        >
          <template #message>
            <div class="selected-element-info">
              <div class="element-header">
                <span class="element-tag">
                  选中元素：{{ selectedElementInfo.tagName.toLowerCase() }}
                </span>
                <span v-if="selectedElementInfo.id" class="element-id">
                  #{{ selectedElementInfo.id }}
                </span>
                <span v-if="selectedElementInfo.className" class="element-class">
                  .{{ selectedElementInfo.className.split(' ').join('.') }}
                </span>
              </div>
              <div class="element-details">
                <div v-if="selectedElementInfo.textContent" class="element-item">
                  内容: {{ selectedElementInfo.textContent.substring(0, 50) }}
                  {{ selectedElementInfo.textContent.length > 50 ? '...' : '' }}
                </div>
                <div v-if="selectedElementInfo.pagePath" class="element-item">
                  页面路径: {{ selectedElementInfo.pagePath }}
                </div>
                <div class="element-item">
                  选择器:
                  <code class="element-selector-code">{{ selectedElementInfo.selector }}</code>
                </div>
              </div>
            </div>
          </template>
        </a-alert>

        <!-- 用户消息输入框 -->
        <div class="input-container">
          <div class="input-wrapper">
            <a-tooltip v-if="!isOwner" title="无法在别人的作品下对话哦~" placement="top">
              <a-textarea
                  v-model:value="userInput"
                  :placeholder="getInputPlaceholder()"
                  :rows="4"
                  :maxlength="1000"
                  @keydown.enter.prevent="sendMessage"
                  :disabled="isGenerating || !isOwner"
              />
            </a-tooltip>
            <a-textarea
                v-else
                v-model:value="userInput"
                :placeholder="getInputPlaceholder()"
                :rows="4"
                :maxlength="1000"
                @keydown.enter.prevent="sendMessage"
                :disabled="isGenerating"
            />
            <div class="input-actions">
              <a-button
                  type="primary"
                  @click="sendMessage"
                  :loading="isGenerating"
                  :disabled="!isOwner"
              >
                <template #icon>
                  <SendOutlined />
                </template>
              </a-button>
            </div>
          </div>
        </div>
      </div>
      <!-- 右侧网页展示区域 -->
      <div class="preview-section" :class="{ 'preview-section-full': rightPanelMode === 'lowcode' }">
        <div class="preview-header">
          <div class="preview-header-left">
            <a-segmented
                v-model:value="rightPanelMode"
                :options="[
                  { label: '👁 预览', value: 'preview' },
                  { label: '✏️ 编辑模式', value: 'edit' },
                  { label: '🧩 低代码编辑', value: 'lowcode' },
                ]"
                size="small"
                @change="onRightPanelModeChange"
            />
          </div>
          <div class="preview-actions">
            <a-button v-if="previewUrl" type="link" @click="openInNewTab">
              <template #icon>
                <ExportOutlined />
              </template>
              新窗口打开
            </a-button>
            <a-button
                v-if="rightPanelMode === 'lowcode' && previewUrl"
                type="primary"
                size="small"
                @click="saveLowcodeChanges"
                :loading="lowcodeSaving"
                style="margin-left: 8px"
            >
              💾 保存修改
            </a-button>
          </div>
        </div>

        <!-- iframe 预览区（两种模式共用同一个 iframe） -->
        <div class="preview-content" :class="{ 'lowcode-active': rightPanelMode === 'lowcode' }">
          <div v-if="!previewUrl && !isGenerating" class="preview-placeholder">
            <div class="placeholder-icon">🌐</div>
            <p>网站文件生成完成后将在这里展示</p>
          </div>
          <div v-else-if="isGenerating && !previewTimedOut" class="preview-loading">
            <a-spin size="large" />
            <p>正在生成网站...</p>
          </div>
          <div v-else-if="previewTimedOut" class="preview-timeout">
            <div class="timeout-icon">⏱️</div>
            <p>生成超时，请检查左侧对话中的错误信息</p>
            <a-button type="primary" @click="previewTimedOut = false; isGenerating = false">
              知道了
            </a-button>
          </div>
          <template v-else>
            <div class="lowcode-wrapper">
              <!-- 拖拽放置遮罩层（拖拽时显示在 iframe 上方接收 drop 事件） -->
              <div
                  v-show="isDraggingComponent"
                  class="iframe-drop-overlay"
                  @dragover.prevent.stop="onOverlayDragOver"
                  @dragenter.prevent.stop
                  @dragleave.prevent.stop
                  @drop.prevent.stop="onOverlayDrop"
              >
                <div class="drop-overlay-hint">
                  <span>📥</span> 释放以插入组件到页面
                </div>
              </div>
              <iframe
                  :src="previewUrl"
                  class="preview-iframe"
                  frameborder="0"
                  @load="onIframeLoad"
              ></iframe>
              <!-- 低代码模式下的右侧面板 -->
              <div v-if="rightPanelMode === 'lowcode'" class="lowcode-side-panel">
                <!-- 顶部 Tab 切换：组件库 / 属性编辑 -->
                <div class="lowcode-panel-tabs">
                  <div
                      class="lowcode-tab"
                      :class="{ active: lowcodePanelTab === 'components' }"
                      @click="lowcodePanelTab = 'components'"
                  >
                    🧩 组件库
                  </div>
                  <div
                      class="lowcode-tab"
                      :class="{ active: lowcodePanelTab === 'properties' }"
                      @click="lowcodePanelTab = 'properties'"
                  >
                    ⚙️ 属性
                    <span v-if="lowcodeSelectedElement" class="lowcode-tab-dot"></span>
                  </div>
                  <div
                      class="lowcode-tab"
                      :class="{ active: lowcodePanelTab === 'tree' }"
                      @click="lowcodePanelTab = 'tree'"
                  >
                    🌳 结构
                  </div>
                </div>

                <!-- 组件库面板 -->
                <div v-show="lowcodePanelTab === 'components'" class="lowcode-panel-body">
                  <div v-for="cat in componentCategories" :key="cat.key" class="lowcode-comp-category">
                    <div class="lowcode-comp-category-title">{{ cat.icon }} {{ cat.label }}</div>
                    <div class="lowcode-comp-grid">
                      <div
                          v-for="comp in getComponentsByCategory(cat.key)"
                          :key="comp.key"
                          class="lowcode-comp-item"
                          :title="comp.description"
                          draggable="true"
                          @click="openComponentConfig(comp)"
                          @dragstart="onCompDragStart($event, comp)"
                      >
                        <span class="lowcode-comp-icon">{{ comp.icon }}</span>
                        <span>{{ comp.name }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 属性编辑面板 -->
                <div v-show="lowcodePanelTab === 'properties'" class="lowcode-panel-body">
                  <div v-if="!lowcodeSelectedElement" class="lowcode-props-empty">
                    <div style="font-size: 36px; margin-bottom: 12px">👆</div>
                    <p>点击页面中的元素</p>
                    <p class="lowcode-hint">选中后可编辑属性</p>
                  </div>
                  <div v-else class="lowcode-props-form">
                    <div class="lowcode-props-header">
                      <span class="lowcode-el-tag">{{ lowcodeSelectedElement.tagName.toLowerCase() }}</span>
                      <a-button type="link" size="small" danger @click="lowcodeClearSelection">取消</a-button>
                    </div>

                    <!-- 文本内容 -->
                    <div class="lowcode-prop-section">
                      <div class="lowcode-prop-section-title">内容</div>
                      <div v-if="lowcodeSelectedElement.textContent" class="lowcode-prop-group">
                        <label>文本</label>
                        <a-textarea v-model:value="lowcodeEditText" :rows="3" size="small" @change="applyTextChange" />
                      </div>
                      <div v-if="lowcodeSelectedElement.tagName === 'IMG'" class="lowcode-prop-group">
                        <label>图片地址</label>
                        <a-input v-model:value="lowcodeEditSrc" size="small" @change="applyImageChange" />
                      </div>
                      <div v-if="lowcodeSelectedElement.tagName === 'A'" class="lowcode-prop-group">
                        <label>链接地址</label>
                        <a-input v-model:value="lowcodeEditHref" size="small" @change="applyHrefChange" />
                      </div>
                    </div>

                    <!-- 排版 -->
                    <div class="lowcode-prop-section">
                      <div class="lowcode-prop-section-title">排版</div>
                      <div class="lowcode-prop-row">
                        <div class="lowcode-prop-group lowcode-prop-half">
                          <label>字号</label>
                          <a-input v-model:value="lowcodeEditFontSize" size="small" placeholder="16px" @change="applyStyleChange('fontSize', lowcodeEditFontSize)" />
                        </div>
                        <div class="lowcode-prop-group lowcode-prop-half">
                          <label>字重</label>
                          <a-select v-model:value="lowcodeEditFontWeight" size="small" style="width:100%" @change="applyStyleChange('fontWeight', lowcodeEditFontWeight)">
                            <a-select-option value="">默认</a-select-option>
                            <a-select-option value="400">常规 400</a-select-option>
                            <a-select-option value="500">中等 500</a-select-option>
                            <a-select-option value="600">半粗 600</a-select-option>
                            <a-select-option value="700">粗体 700</a-select-option>
                          </a-select>
                        </div>
                      </div>
                      <div class="lowcode-prop-group">
                        <label>文字对齐</label>
                        <a-radio-group v-model:value="lowcodeEditTextAlign" size="small" @change="applyStyleChange('textAlign', lowcodeEditTextAlign)">
                          <a-radio-button value="left">左</a-radio-button>
                          <a-radio-button value="center">中</a-radio-button>
                          <a-radio-button value="right">右</a-radio-button>
                        </a-radio-group>
                      </div>
                    </div>

                    <!-- 颜色 -->
                    <div class="lowcode-prop-section">
                      <div class="lowcode-prop-section-title">颜色</div>
                      <div class="lowcode-prop-row">
                        <div class="lowcode-prop-group lowcode-prop-half">
                          <label>文字</label>
                          <div class="lowcode-color-input">
                            <input type="color" :value="lowcodeEditColor || '#333333'" @input="(e: any) => { lowcodeEditColor = e.target.value; applyStyleChange('color', lowcodeEditColor) }" class="lowcode-color-picker" />
                            <a-input v-model:value="lowcodeEditColor" size="small" placeholder="#333" @change="applyStyleChange('color', lowcodeEditColor)" />
                          </div>
                        </div>
                        <div class="lowcode-prop-group lowcode-prop-half">
                          <label>背景</label>
                          <div class="lowcode-color-input">
                            <input type="color" :value="lowcodeEditBgColor || '#ffffff'" @input="(e: any) => { lowcodeEditBgColor = e.target.value; applyStyleChange('backgroundColor', lowcodeEditBgColor) }" class="lowcode-color-picker" />
                            <a-input v-model:value="lowcodeEditBgColor" size="small" placeholder="#fff" @change="applyStyleChange('backgroundColor', lowcodeEditBgColor)" />
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- 间距 -->
                    <div class="lowcode-prop-section">
                      <div class="lowcode-prop-section-title">间距</div>
                      <div class="lowcode-prop-row">
                        <div class="lowcode-prop-group lowcode-prop-half">
                          <label>内边距</label>
                          <a-input v-model:value="lowcodeEditPadding" size="small" placeholder="16px" @change="applyStyleChange('padding', lowcodeEditPadding)" />
                        </div>
                        <div class="lowcode-prop-group lowcode-prop-half">
                          <label>外边距</label>
                          <a-input v-model:value="lowcodeEditMargin" size="small" placeholder="0 auto" @change="applyStyleChange('margin', lowcodeEditMargin)" />
                        </div>
                      </div>
                      <div class="lowcode-prop-group">
                        <label>圆角</label>
                        <a-input v-model:value="lowcodeEditBorderRadius" size="small" placeholder="8px" @change="applyStyleChange('borderRadius', lowcodeEditBorderRadius)" />
                      </div>
                    </div>

                    <!-- 尺寸 -->
                    <div class="lowcode-prop-section">
                      <div class="lowcode-prop-section-title">尺寸</div>
                      <div class="lowcode-prop-row">
                        <div class="lowcode-prop-group lowcode-prop-half">
                          <label>宽度</label>
                          <a-input v-model:value="lowcodeEditWidth" size="small" placeholder="auto" @change="applyStyleChange('width', lowcodeEditWidth)" />
                        </div>
                        <div class="lowcode-prop-group lowcode-prop-half">
                          <label>高度</label>
                          <a-input v-model:value="lowcodeEditHeight" size="small" placeholder="auto" @change="applyStyleChange('height', lowcodeEditHeight)" />
                        </div>
                      </div>
                    </div>

                    <!-- 操作 -->
                    <div class="lowcode-prop-section">
                      <div class="lowcode-prop-section-title">操作</div>
                      <div class="lowcode-prop-row">
                        <a-button size="small" block @click="duplicateElement">复制元素</a-button>
                      </div>
                      <div class="lowcode-prop-row" style="margin-top: 8px">
                        <a-button danger size="small" block @click="lowcodeDeleteElement">删除元素</a-button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 页面结构树 -->
                <div v-show="lowcodePanelTab === 'tree'" class="lowcode-panel-body">
                  <div class="lowcode-tree-hint">
                    <p>页面 DOM 结构概览</p>
                    <a-button size="small" type="primary" ghost @click="refreshPageTree">刷新</a-button>
                  </div>
                  <div class="lowcode-tree-content">
                    <pre style="font-size: 12px; color: #555; white-space: pre-wrap; word-break: break-all">{{ pageTreeText }}</pre>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 应用详情弹窗 -->
    <AppDetailModal
        v-model:open="appDetailVisible"
        :app="appInfo"
        :show-actions="isOwner || isAdmin"
        @edit="editApp"
        @delete="deleteApp"
    />

    <!-- 部署成功弹窗 -->
    <DeploySuccessModal
        v-model:open="deployModalVisible"
        :deploy-url="deployUrl"
        @open-site="openDeployedSite"
    />

    <!-- 组件配置弹窗 -->
    <ComponentConfigModal
        v-model:open="configModalOpen"
        :componentDef="configModalComponent"
        @confirm="onComponentConfigConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  getAppVoById,
  deployApp as deployAppApi,
  deleteApp as deleteAppApi,
} from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { CodeGenTypeEnum, formatCodeGenType } from '@/utils/codeGenTypes'
import request from '@/request'

import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'
import aiAvatar from '@/assets/aiAvatar.png'
import { API_BASE_URL, getStaticPreviewUrl } from '@/config/env'
import { VisualEditor, type ElementInfo } from '@/utils/visualEditor'
import { componentCategories, getComponentsByCategory, type ComponentDef } from '@/utils/lowcode/componentLibrary'
import ComponentConfigModal from '@/components/lowcode/ComponentConfigModal.vue'
import { saveLowcodeHtml } from '@/api/lowcodeController'

import {
  CloudUploadOutlined,
  SendOutlined,
  ExportOutlined,
  InfoCircleOutlined,
  DownloadOutlined,
  EditOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const appId = ref<any>()

// 对话相关
interface Message {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  createTime?: string
}

const messages = ref<Message[]>([])
const userInput = ref('')
const isGenerating = ref(false)
const messagesContainer = ref<HTMLElement>()

// 对话历史相关
const loadingHistory = ref(false)
const hasMoreHistory = ref(false)
const lastCreateTime = ref<string>()
const historyLoaded = ref(false)

// 预览相关
const previewUrl = ref('')
const previewReady = ref(false)
const previewTimeout = ref<ReturnType<typeof setTimeout>>()
const previewTimedOut = ref(false)

// 部署相关
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// 下载相关
const downloading = ref(false)

// 右侧面板模式切换
const rightPanelMode = ref<'preview' | 'edit' | 'lowcode'>('preview')

// 低代码编辑相关（基于 iframe 的可视化编辑）
const lowcodePanelTab = ref<'components' | 'properties' | 'tree'>('components')
const lowcodeSelectedElement = ref<ElementInfo | null>(null)
const lowcodeEditText = ref('')
const lowcodeEditSrc = ref('')
const lowcodeEditHref = ref('')
const lowcodeEditBgColor = ref('')
const lowcodeEditColor = ref('')
const lowcodeEditFontSize = ref('')
const lowcodeEditFontWeight = ref('')
const lowcodeEditTextAlign = ref('')
const lowcodeEditPadding = ref('')
const lowcodeEditMargin = ref('')
const lowcodeEditBorderRadius = ref('')
const lowcodeEditWidth = ref('')
const lowcodeEditHeight = ref('')
const pageTreeText = ref('点击"刷新"查看页面结构')

// 组件配置弹窗
const configModalOpen = ref(false)
const configModalComponent = ref<ComponentDef | null>(null)

// 保存状态
const lowcodeSaving = ref(false)

// 拖拽状态
const isDraggingComponent = ref(false)
const draggingHtml = ref('')

// 可视化编辑相关
const isEditMode = ref(false)
const selectedElementInfo = ref<ElementInfo | null>(null)
const visualEditor = new VisualEditor({
  onElementSelected: (elementInfo: ElementInfo) => {
    selectedElementInfo.value = elementInfo
  },
})

// 权限相关
const isOwner = computed(() => {
  return appInfo.value?.userId === loginUserStore.loginUser.id
})

const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 应用详情相关
const appDetailVisible = ref(false)

// 显示应用详情
const showAppDetail = () => {
  appDetailVisible.value = true
}

// 加载对话历史
const loadChatHistory = async (isLoadMore = false) => {
  if (!appId.value || loadingHistory.value) return
  loadingHistory.value = true
  try {
    const params: API.listAppChatHistoryParams = {
      appId: appId.value,
      pageSize: 10,
    }
    // 如果是加载更多，传递最后一条消息的创建时间作为游标
    if (isLoadMore && lastCreateTime.value) {
      params.lastCreateTime = lastCreateTime.value
    }
    const res = await listAppChatHistory(params)
    if (res.data.code === 0 && res.data.data) {
      const chatHistories = res.data.data.records || []
      if (chatHistories.length > 0) {
        // 将对话历史转换为消息格式，并按时间正序排列（老消息在前）
        const historyMessages: Message[] = chatHistories
            .map((chat) => ({
              type: (chat.messageType === 'user' ? 'user' : 'ai') as 'user' | 'ai',
              content: chat.message || '',
              createTime: chat.createTime,
            }))
            .reverse() // 反转数组，让老消息在前
        if (isLoadMore) {
          // 加载更多时，将历史消息添加到开头
          messages.value.unshift(...historyMessages)
        } else {
          // 初始加载，直接设置消息列表
          messages.value = historyMessages
        }
        // 更新游标
        lastCreateTime.value = chatHistories[chatHistories.length - 1]?.createTime
        // 检查是否还有更多历史
        hasMoreHistory.value = chatHistories.length === 10
      } else {
        hasMoreHistory.value = false
      }
      historyLoaded.value = true
    }
  } catch (error) {
    console.error('加载对话历史失败：', error)
    message.error('加载对话历史失败')
  } finally {
    loadingHistory.value = false
  }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
  await loadChatHistory(true)
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  appId.value = id

  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data

      // 先加载对话历史
      await loadChatHistory()
      // 如果有至少2条对话记录，展示对应的网站
      if (messages.value.length >= 2) {
        updatePreview()
      }
      // 检查是否需要自动发送初始提示词
      // 只有在是自己的应用且没有对话历史时才自动发送
      if (
          appInfo.value.initPrompt &&
          isOwner.value &&
          messages.value.length === 0 &&
          historyLoaded.value
      ) {
        await sendInitialMessage(appInfo.value.initPrompt)
      }
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
    router.push('/')
  }
}

// 发送初始消息
const sendInitialMessage = async (prompt: string) => {
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: prompt,
  })

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(prompt, aiMessageIndex)
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isGenerating.value) {
    return
  }

  let message = userInput.value.trim()
  // 如果有选中的元素，将元素信息添加到提示词中
  if (selectedElementInfo.value) {
    let elementContext = `\n\n选中元素信息：`
    if (selectedElementInfo.value.pagePath) {
      elementContext += `\n- 页面路径: ${selectedElementInfo.value.pagePath}`
    }
    elementContext += `\n- 标签: ${selectedElementInfo.value.tagName.toLowerCase()}\n- 选择器: ${selectedElementInfo.value.selector}`
    if (selectedElementInfo.value.textContent) {
      elementContext += `\n- 当前内容: ${selectedElementInfo.value.textContent.substring(0, 100)}`
    }
    message += elementContext
  }
  userInput.value = ''
  // 添加用户消息（包含元素信息）
  messages.value.push({
    type: 'user',
    content: message,
  })

  // 发送消息后，清除选中元素并退出编辑模式
  if (selectedElementInfo.value) {
    clearSelectedElement()
    if (isEditMode.value) {
      toggleEditMode()
    }
  }

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(message, aiMessageIndex)
}

// 生成代码 - 使用 EventSource 处理流式响应
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  let eventSource: EventSource | null = null
  let streamCompleted = false

  try {
    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL || API_BASE_URL

    // 构建URL参数
    const params = new URLSearchParams({
      appId: appId.value || '',
      message: userMessage,
    })

    const url = `${baseURL}/app/chat/gen/code?${params}`

    // 创建 EventSource 连接
    eventSource = new EventSource(url, {
      withCredentials: true,
    })

    let fullContent = ''

    // 处理接收到的消息
    eventSource.onmessage = function (event) {
      if (streamCompleted) return

      try {
        // 解析JSON包装的数据
        const parsed = JSON.parse(event.data)
        const content = parsed.d

        // 拼接内容
        if (content !== undefined && content !== null) {
          fullContent += content
          messages.value[aiMessageIndex].content = fullContent
          messages.value[aiMessageIndex].loading = false
          scrollToBottom()
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        handleError(error, aiMessageIndex)
      }
    }

    // 处理done事件
    eventSource.addEventListener('done', function () {
      if (streamCompleted) return

      streamCompleted = true
      isGenerating.value = false
      eventSource?.close()

      // 延迟更新预览，确保后端已完成处理
      setTimeout(async () => {
        await fetchAppInfo()
        updatePreview()
      }, 1000)
    })

    // 处理business-error事件（后端限流等错误）
    eventSource.addEventListener('business-error', function (event: MessageEvent) {
      if (streamCompleted) return

      try {
        const errorData = JSON.parse(event.data)
        console.error('SSE业务错误事件:', errorData)

        // 显示具体的错误信息
        const errorMessage = errorData.message || '生成过程中出现错误'
        messages.value[aiMessageIndex].content = `❌ ${errorMessage}`
        messages.value[aiMessageIndex].loading = false
        message.error(errorMessage)

        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()
      } catch (parseError) {
        console.error('解析错误事件失败:', parseError, '原始数据:', event.data)
        handleError(new Error('服务器返回错误'), aiMessageIndex)
      }
    })

    // 处理错误
    eventSource.onerror = function () {
      if (streamCompleted || !isGenerating.value) return
      // 检查是否是正常的连接关闭
      if (eventSource?.readyState === EventSource.CONNECTING) {
        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()

        setTimeout(async () => {
          await fetchAppInfo()
          updatePreview()
        }, 1000)
      } else {
        handleError(new Error('SSE连接错误'), aiMessageIndex)
      }
    }
  } catch (error) {
    console.error('创建 EventSource 失败：', error)
    handleError(error, aiMessageIndex)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  message.error('生成失败，请重试')
  isGenerating.value = false
}

// 更新预览
const updatePreview = () => {
  if (appId.value) {
    const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML
    const newPreviewUrl = getStaticPreviewUrl(codeGenType, appId.value)
    previewUrl.value = newPreviewUrl
    previewReady.value = true
    previewTimedOut.value = false

    // 设置预览加载超时检测（120 秒）
    if (previewTimeout.value) {
      clearTimeout(previewTimeout.value)
    }
    previewTimeout.value = setTimeout(() => {
      if (!previewReady.value || isGenerating.value) {
        previewTimedOut.value = true
        isGenerating.value = false
      }
    }, 120000)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 下载代码
const downloadCode = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }
  downloading.value = true
  try {
    const API_BASE_URL = request.defaults.baseURL || ''
    const url = `${API_BASE_URL}/app/download/${appId.value}`
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
    })
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status}`)
    }
    // 获取文件名
    const contentDisposition = response.headers.get('Content-Disposition')
    const fileName = contentDisposition?.match(/filename="(.+)"/)?.[1] || `app-${appId.value}.zip`
    // 下载文件
    const blob = await response.blob()
    const downloadUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName
    link.click()
    // 清理
    URL.revokeObjectURL(downloadUrl)
    message.success('代码下载成功')
  } catch (error) {
    console.error('下载失败：', error)
    message.error('下载失败，请重试')
  } finally {
    downloading.value = false
  }
}

// 部署应用
const deployApp = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  deploying.value = true
  try {
    const res = await deployAppApi({
      appId: appId.value as unknown as number,
    })

    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
      message.success('部署成功')
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    console.error('部署失败：', error)
    message.error('部署失败，请重试')
  } finally {
    deploying.value = false
  }
}

// 在新窗口打开预览
const openInNewTab = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 打开部署的网站
const openDeployedSite = () => {
  if (deployUrl.value) {
    window.open(deployUrl.value, '_blank')
  }
}

// iframe加载完成
const onIframeLoad = () => {
  previewReady.value = true
  previewTimedOut.value = false
  if (previewTimeout.value) {
    clearTimeout(previewTimeout.value)
  }
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (iframe) {
    visualEditor.init(iframe)
    visualEditor.onIframeLoad()
  }
}

// 编辑应用
const editApp = () => {
  if (appInfo.value?.id) {
    router.push(`/app/edit/${appInfo.value.id}`)
  }
}

// 删除应用
const deleteApp = async () => {
  if (!appInfo.value?.id) return

  try {
    const res = await deleteAppApi({ id: appInfo.value.id })
    if (res.data.code === 0) {
      message.success('删除成功')
      appDetailVisible.value = false
      router.push('/')
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

// 可视化编辑相关函数
const toggleEditMode = () => {
  // 检查 iframe 是否已经加载
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe) {
    message.warning('请等待页面加载完成')
    return
  }
  // 确保 visualEditor 已初始化
  if (!previewReady.value) {
    message.warning('请等待页面加载完成')
    return
  }
  const newEditMode = visualEditor.toggleEditMode()
  isEditMode.value = newEditMode
}

const clearSelectedElement = () => {
  selectedElementInfo.value = null
  visualEditor.clearSelection()
}

const getInputPlaceholder = () => {
  if (selectedElementInfo.value) {
    return `正在编辑 ${selectedElementInfo.value.tagName.toLowerCase()} 元素，描述您想要的修改...`
  }
  return '请描述你想生成的网站，越详细效果越好哦'
}

// ========== 低代码编辑相关方法 ==========

// 切换右侧面板模式
const onRightPanelModeChange = (mode: string | number) => {
  if (mode === 'edit' || mode === 'lowcode') {
    // 编辑模式和低代码模式都需要开启 iframe 编辑
    const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
    if (iframe && previewUrl.value) {
      visualEditor.init(iframe)
      if (!isEditMode.value) {
        visualEditor.enableEditMode()
        isEditMode.value = true
      }
    }

    if (mode === 'lowcode') {
      // 低代码模式：选中元素后切到属性面板
      visualEditor.options.onElementSelected = (elementInfo: ElementInfo) => {
        lowcodeSelectedElement.value = elementInfo
        lowcodePanelTab.value = 'properties'
        lowcodeEditText.value = elementInfo.textContent || ''
        lowcodeEditBgColor.value = ''
        lowcodeEditColor.value = ''
        lowcodeEditFontSize.value = ''
        lowcodeEditFontWeight.value = ''
        lowcodeEditTextAlign.value = ''
        lowcodeEditPadding.value = ''
        lowcodeEditMargin.value = ''
        lowcodeEditBorderRadius.value = ''
        lowcodeEditWidth.value = ''
        lowcodeEditHeight.value = ''
        lowcodeEditSrc.value = ''
        lowcodeEditHref.value = ''
        loadElementStyles(elementInfo.selector)
      }
    } else {
      // 编辑模式：选中元素后显示在左侧对话区的提示中
      visualEditor.options.onElementSelected = (elementInfo: ElementInfo) => {
        selectedElementInfo.value = elementInfo
      }
    }
  } else {
    // 预览模式：退出编辑
    if (isEditMode.value) {
      visualEditor.disableEditMode()
      isEditMode.value = false
    }
    lowcodeSelectedElement.value = null
    selectedElementInfo.value = null
    visualEditor.options.onElementSelected = (elementInfo: ElementInfo) => {
      selectedElementInfo.value = elementInfo
    }
  }
}

// 从 iframe 获取选中元素的当前样式
const loadElementStyles = (selector: string) => {
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe?.contentDocument) return
  try {
    const el = iframe.contentDocument.querySelector(selector) as HTMLElement
    if (!el) return
    const cs = iframe.contentWindow!.getComputedStyle(el)
    lowcodeEditBgColor.value = el.style.backgroundColor || rgbToHex(cs.backgroundColor) || ''
    lowcodeEditColor.value = el.style.color || rgbToHex(cs.color) || ''
    lowcodeEditFontSize.value = el.style.fontSize || cs.fontSize || ''
    lowcodeEditFontWeight.value = el.style.fontWeight || ''
    lowcodeEditTextAlign.value = el.style.textAlign || ''
    lowcodeEditPadding.value = el.style.padding || ''
    lowcodeEditMargin.value = el.style.margin || ''
    lowcodeEditBorderRadius.value = el.style.borderRadius || ''
    lowcodeEditWidth.value = el.style.width || ''
    lowcodeEditHeight.value = el.style.height || ''
    if (el.tagName === 'IMG') {
      lowcodeEditSrc.value = (el as HTMLImageElement).src || ''
    }
    if (el.tagName === 'A') {
      lowcodeEditHref.value = (el as HTMLAnchorElement).href || ''
    }
  } catch (e) {
    // 跨域或其他错误，静默处理
  }
}

// RGB 转 Hex
const rgbToHex = (rgb: string): string => {
  if (!rgb || rgb === 'rgba(0, 0, 0, 0)' || rgb === 'transparent') return ''
  const match = rgb.match(/\d+/g)
  if (!match || match.length < 3) return ''
  const r = parseInt(match[0]).toString(16).padStart(2, '0')
  const g = parseInt(match[1]).toString(16).padStart(2, '0')
  const b = parseInt(match[2]).toString(16).padStart(2, '0')
  return `#${r}${g}${b}`
}

// 应用文本修改到 iframe
const applyTextChange = () => {
  if (!lowcodeSelectedElement.value) return
  sendMessageToIframe({
    type: 'LOWCODE_SET_TEXT',
    selector: lowcodeSelectedElement.value.selector,
    value: lowcodeEditText.value,
  })
}

// 应用图片修改到 iframe
const applyImageChange = () => {
  if (!lowcodeSelectedElement.value) return
  sendMessageToIframe({
    type: 'LOWCODE_SET_ATTR',
    selector: lowcodeSelectedElement.value.selector,
    attr: 'src',
    value: lowcodeEditSrc.value,
  })
}

// 应用链接修改到 iframe
const applyHrefChange = () => {
  if (!lowcodeSelectedElement.value) return
  sendMessageToIframe({
    type: 'LOWCODE_SET_ATTR',
    selector: lowcodeSelectedElement.value.selector,
    attr: 'href',
    value: lowcodeEditHref.value,
  })
}

// 应用样式修改到 iframe
const applyStyleChange = (prop: string, value: string) => {
  if (!lowcodeSelectedElement.value) return
  sendMessageToIframe({
    type: 'LOWCODE_SET_STYLE',
    selector: lowcodeSelectedElement.value.selector,
    prop,
    value,
  })
}

// 删除选中元素
const lowcodeDeleteElement = () => {
  if (!lowcodeSelectedElement.value) return
  sendMessageToIframe({
    type: 'LOWCODE_DELETE',
    selector: lowcodeSelectedElement.value.selector,
  })
  lowcodeSelectedElement.value = null
}

// 清除低代码选中
const lowcodeClearSelection = () => {
  lowcodeSelectedElement.value = null
  visualEditor.clearSelection()
}

// 向 iframe 发送消息
const sendMessageToIframe = (msg: Record<string, any>) => {
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (iframe?.contentWindow) {
    iframe.contentWindow.postMessage(msg, '*')
  }
}

// 打开组件配置弹窗
const openComponentConfig = (comp: ComponentDef) => {
  configModalComponent.value = comp
  configModalOpen.value = true
}

// 组件拖拽开始
const onCompDragStart = (event: DragEvent, comp: ComponentDef) => {
  const defaultConfig: Record<string, any> = {}
  for (const field of comp.fields) {
    defaultConfig[field.key] = field.defaultValue ?? ''
  }
  const html = comp.generateHtml(defaultConfig)
  draggingHtml.value = html
  isDraggingComponent.value = true
  event.dataTransfer?.setData('text/plain', comp.key)
  event.dataTransfer!.effectAllowed = 'copy'

  // 拖拽结束时清除状态
  const cleanup = () => {
    setTimeout(() => { isDraggingComponent.value = false }, 100)
    document.removeEventListener('dragend', cleanup)
  }
  document.addEventListener('dragend', cleanup)
}

// 遮罩层 dragover
const onOverlayDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy'
  }
}

// 遮罩层 drop — 通过 postMessage 插入到 iframe
const onOverlayDrop = (event: DragEvent) => {
  event.preventDefault()
  event.stopPropagation()
  isDraggingComponent.value = false

  if (!draggingHtml.value) return

  const html = draggingHtml.value
  draggingHtml.value = ''

  // 先尝试 postMessage
  sendMessageToIframe({
    type: 'LOWCODE_INSERT_HTML',
    html,
    position: null,
  })

  // 同时直接操作 iframe DOM 作为后备方案
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (iframe?.contentDocument?.body) {
    try {
      // 检查 postMessage 脚本是否已注入
      const hasScript = iframe.contentDocument.getElementById('visual-edit-script')
      if (!hasScript) {
        // 脚本未注入，直接操作 DOM
        const temp = iframe.contentDocument.createElement('div')
        temp.innerHTML = html
        const newEl = temp.firstElementChild
        if (newEl) {
          const footer = iframe.contentDocument.querySelector('footer')
          if (footer && footer.parentElement) {
            footer.parentElement.insertBefore(newEl, footer)
          } else {
            iframe.contentDocument.body.appendChild(newEl)
          }
        }
      }
    } catch (e) {
      // 跨域错误，静默处理
      console.warn('直接操作 iframe DOM 失败，依赖 postMessage', e)
    }
  }
}

// 组件配置确认后插入 HTML
const onComponentConfigConfirm = (html: string) => {
  sendMessageToIframe({
    type: 'LOWCODE_INSERT_HTML',
    html,
    position: lowcodeSelectedElement.value?.selector || null,
  })
}

// 插入 HTML 片段到页面（保留兼容）
const insertHtmlSnippet = (tag: string, html: string) => {
  const decoded = html.replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&quot;/g, '"').replace(/&amp;/g, '&')
  sendMessageToIframe({
    type: 'LOWCODE_INSERT_HTML',
    html: decoded,
    position: lowcodeSelectedElement.value?.selector || null,
  })
}

// 保存低代码编辑到文件
const saveLowcodeChanges = async () => {
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe?.contentDocument) {
    message.warning('页面未加载，无法保存')
    return
  }
  lowcodeSaving.value = true
  try {
    // 从 iframe 提取完整 HTML
    const htmlContent = '<!DOCTYPE html>\n' + iframe.contentDocument.documentElement.outerHTML
    const res = await saveLowcodeHtml({
      appId: appId.value,
      htmlContent,
    })
    if (res.data.code === 0) {
      message.success('保存成功')
    } else {
      message.error('保存失败: ' + res.data.message)
    }
  } catch (e) {
    message.error('保存失败')
  } finally {
    lowcodeSaving.value = false
  }
}

// 复制选中元素
const duplicateElement = () => {
  if (!lowcodeSelectedElement.value) return
  sendMessageToIframe({
    type: 'LOWCODE_DUPLICATE',
    selector: lowcodeSelectedElement.value.selector,
  })
}

// 刷新页面结构树
const refreshPageTree = () => {
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe?.contentDocument?.body) {
    pageTreeText.value = '页面未加载'
    return
  }
  try {
    const tree = buildTreeText(iframe.contentDocument.body, 0, 3)
    pageTreeText.value = tree || '空页面'
  } catch (e) {
    pageTreeText.value = '无法读取页面结构'
  }
}

const buildTreeText = (el: Element, depth: number, maxDepth: number): string => {
  if (depth > maxDepth) return ''
  const indent = '  '.repeat(depth)
  const tag = el.tagName.toLowerCase()
  if (['script', 'style', 'link', 'meta'].includes(tag)) return ''
  const id = el.id ? `#${el.id}` : ''
  const cls = el.className && typeof el.className === 'string'
      ? '.' + el.className.split(' ').filter(c => c && !c.startsWith('edit-')).slice(0, 2).join('.')
      : ''
  const text = el.childNodes.length === 1 && el.childNodes[0].nodeType === 3
      ? ` "${(el.textContent || '').trim().substring(0, 30)}"`
      : ''
  let result = `${indent}<${tag}${id}${cls}>${text}\n`
  for (const child of el.children) {
    result += buildTreeText(child, depth + 1, maxDepth)
  }
  return result
}

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()

  // 监听 iframe 消息
  window.addEventListener('message', (event) => {
    visualEditor.handleIframeMessage(event)
  })
})

// 清理资源
onUnmounted(() => {
  if (previewTimeout.value) {
    clearTimeout(previewTimeout.value)
  }
})
</script>

<style scoped>
#appChatPage {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #fdfdfd;
}

/* 顶部栏 */
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.code-gen-type-tag {
  font-size: 12px;
}

.app-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 8px;
  overflow: hidden;
}

/* 左侧对话区域 */
.chat-section {
  flex: 2;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.messages-container {
  flex: 0.9;
  padding: 16px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.message-item {
  margin-bottom: 12px;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 8px;
}

.ai-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 8px;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
  word-wrap: break-word;
}

.user-message .message-content {
  background: #1890ff;
  color: white;
}

.ai-message .message-content {
  background: #f5f5f5;
  color: #1a1a1a;
  padding: 8px 12px;
}

.message-avatar {
  flex-shrink: 0;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

/* 加载更多按钮 */
.load-more-container {
  text-align: center;
  padding: 8px 0;
  margin-bottom: 16px;
}

/* 输入区域 */
.input-container {
  padding: 16px;
  background: white;
}

.input-wrapper {
  position: relative;
}

.input-wrapper .ant-input {
  padding-right: 50px;
}

.input-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

/* 右侧预览区域 */
.preview-section {
  flex: 3;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 低代码模式下占满全宽 */
.preview-section-full {
  flex: 1 !important;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.preview-content {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.preview-loading p {
  margin-top: 16px;
}

.preview-timeout {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
  gap: 12px;
}

.timeout-icon {
  font-size: 48px;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.selected-element-alert {
  margin: 0 16px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    flex-direction: column;
  }

  .chat-section,
  .preview-section {
    flex: none;
    height: 50vh;
  }
}

@media (max-width: 768px) {
  .header-bar {
    padding: 12px 16px;
  }

  .app-name {
    font-size: 16px;
  }

  .main-content {
    padding: 8px;
    gap: 8px;
  }

  .message-content {
    max-width: 85%;
  }

  /* 选中元素信息样式 */
  .selected-element-alert {
    margin: 0 16px;
  }

  .selected-element-info {
    line-height: 1.4;
  }

  .element-header {
    margin-bottom: 8px;
  }

  .element-details {
    margin-top: 8px;
  }

  .element-item {
    margin-bottom: 4px;
    font-size: 13px;
  }

  .element-item:last-child {
    margin-bottom: 0;
  }

  .element-tag {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 14px;
    font-weight: 600;
    color: #007bff;
  }

  .element-id {
    color: #28a745;
    margin-left: 4px;
  }

  .element-class {
    color: #ffc107;
    margin-left: 4px;
  }

  .element-selector-code {
    font-family: 'Monaco', 'Menlo', monospace;
    background: #f6f8fa;
    padding: 2px 4px;
    border-radius: 3px;
    font-size: 12px;
    color: #d73a49;
    border: 1px solid #e1e4e8;
  }

  /* 编辑模式按钮样式 */
  .edit-mode-active {
    background-color: #52c41a !important;
    border-color: #52c41a !important;
    color: white !important;
  }

  .edit-mode-active:hover {
    background-color: #73d13d !important;
    border-color: #73d13d !important;
  }
}

/* 预览区 header 布局 */
.preview-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 低代码模式下的布局 */
.lowcode-wrapper {
  display: flex;
  height: 100%;
  position: relative;
}

.lowcode-wrapper .preview-iframe {
  flex: 1;
  width: 0;
  height: 100%;
  border: none;
}

/* 拖拽放置遮罩层 */
.iframe-drop-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 280px; /* 留出右侧面板宽度 */
  bottom: 0;
  background: rgba(24, 144, 255, 0.1);
  border: 3px dashed #1890ff;
  border-radius: 8px;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
}

.drop-overlay-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;
  color: #1890ff;
  background: rgba(255, 255, 255, 0.95);
  padding: 16px 32px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.2);
  pointer-events: none;
}

.drop-overlay-hint span {
  font-size: 24px;
  pointer-events: none;
}

/* 低代码右侧面板 */
.lowcode-side-panel {
  width: 280px;
  border-left: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: #fff;
}

.lowcode-panel-tabs {
  display: flex;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.lowcode-tab {
  flex: 1;
  text-align: center;
  padding: 10px 4px;
  font-size: 13px;
  cursor: pointer;
  color: #888;
  transition: all 0.2s;
  position: relative;
  user-select: none;
}

.lowcode-tab:hover { color: #333; background: #fafafa; }

.lowcode-tab.active {
  color: #1890ff;
  font-weight: 500;
  border-bottom: 2px solid #1890ff;
}

.lowcode-tab-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #52c41a;
  border-radius: 50%;
  margin-left: 4px;
  vertical-align: middle;
}

.lowcode-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

/* 组件库 */
.lowcode-comp-category { margin-bottom: 16px; }

.lowcode-comp-category-title {
  font-size: 12px;
  color: #999;
  font-weight: 500;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.lowcode-comp-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
}

.lowcode-comp-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 4px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 12px;
  color: #555;
  background: #fafafa;
  user-select: none;
}

.lowcode-comp-item:hover {
  border-color: #1890ff;
  background: #e6f7ff;
  color: #1890ff;
}

.lowcode-comp-item:active {
  transform: scale(0.96);
}

.lowcode-comp-icon {
  font-size: 18px;
}

/* 属性面板 */
.lowcode-props-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 250px;
  color: #999;
  text-align: center;
}

.lowcode-props-empty p { margin: 4px 0; font-size: 14px; }
.lowcode-hint { font-size: 12px !important; color: #bbb !important; }

.lowcode-props-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.lowcode-props-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 4px;
}

.lowcode-el-tag {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  font-weight: 600;
  color: #1890ff;
  background: #e6f7ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.lowcode-prop-section {
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.lowcode-prop-section:last-child { border-bottom: none; }

.lowcode-prop-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.lowcode-prop-group {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-bottom: 6px;
}

.lowcode-prop-group label {
  font-size: 11px;
  color: #999;
  font-weight: 500;
}

.lowcode-prop-row {
  display: flex;
  gap: 8px;
}

.lowcode-prop-half {
  flex: 1;
  min-width: 0;
}

.lowcode-color-input {
  display: flex;
  align-items: center;
  gap: 6px;
}

.lowcode-color-picker {
  width: 28px;
  height: 28px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  padding: 0;
  flex-shrink: 0;
}

.lowcode-color-picker::-webkit-color-swatch-wrapper { padding: 2px; }
.lowcode-color-picker::-webkit-color-swatch { border: none; border-radius: 2px; }

/* 结构树 */
.lowcode-tree-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.lowcode-tree-hint p { margin: 0; font-size: 13px; color: #888; }

.lowcode-tree-content {
  background: #f9f9f9;
  border-radius: 6px;
  padding: 12px;
  max-height: 500px;
  overflow-y: auto;
}
</style>
