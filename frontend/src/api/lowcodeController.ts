import request from '@/request'

/** 保存 Schema */
export async function saveSchema(body: { appId: number; schemaContent: string }) {
  return request<API.BaseResponseInt>('/lowcode/schema/save', {
    method: 'POST',
    data: body,
  })
}

/** 获取最新 Schema */
export async function getLatestSchema(params: { appId: number; codeGenType?: string }) {
  return request<API.BaseResponsePageSchemaVO>('/lowcode/schema/get', {
    method: 'GET',
    params,
  })
}

/** 获取 Schema 版本历史 */
export async function getSchemaHistory(params: { appId: number }) {
  return request<API.BaseResponsePageSchemaVOList>('/lowcode/schema/history', {
    method: 'GET',
    params,
  })
}

/** 回滚 Schema */
export async function rollbackSchema(params: { appId: number; version: number }) {
  return request<API.BaseResponseInt>('/lowcode/schema/rollback', {
    method: 'POST',
    params,
  })
}

/** 导出 Schema 为代码 */
export async function exportSchema(body: { appId: number }) {
  return request<API.BaseResponseString>('/lowcode/schema/export', {
    method: 'POST',
    data: body,
  })
}

/** 保存低代码编辑后的 HTML */
export async function saveLowcodeHtml(body: { appId: number; htmlContent: string }) {
  return request<API.BaseResponseBoolean>('/lowcode/html/save', {
    method: 'POST',
    data: body,
  })
}

/** 获取组件模板列表 */
export async function listComponentTemplates(params?: { category?: string; name?: string }) {
  return request<API.BaseResponseComponentTemplateVOList>('/lowcode/template/list', {
    method: 'GET',
    params,
  })
}

/** 获取单个组件模板 */
export async function getComponentTemplate(params: { componentKey: string }) {
  return request<API.BaseResponseComponentTemplateVO>('/lowcode/template/get', {
    method: 'GET',
    params,
  })
}
