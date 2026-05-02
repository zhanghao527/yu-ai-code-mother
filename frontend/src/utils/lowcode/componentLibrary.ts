/**
 * 低代码组件库 - 企业级完整组件定义
 * 每个组件包含：名称、图标、分类、配置字段、HTML 生成函数
 */

export interface ComponentField {
  key: string
  label: string
  type: 'input' | 'textarea' | 'number' | 'select' | 'switch' | 'color' | 'array'
  placeholder?: string
  defaultValue?: any
  options?: { label: string; value: string }[]
  required?: boolean
  min?: number
  max?: number
  /** array 类型的子字段 */
  itemFields?: ComponentField[]
}

export interface ComponentDef {
  key: string
  name: string
  icon: string
  category: 'layout' | 'basic' | 'form' | 'data' | 'business' | 'media'
  description: string
  fields: ComponentField[]
  generateHtml: (config: Record<string, any>) => string
}

// ========== 布局组件 ==========

const layoutComponents: ComponentDef[] = [
  {
    key: 'section-container',
    name: '区块容器',
    icon: '📦',
    category: 'layout',
    description: '通用内容区块',
    fields: [
      { key: 'maxWidth', label: '最大宽度', type: 'input', defaultValue: '1200px', placeholder: '1200px' },
      { key: 'padding', label: '内边距', type: 'input', defaultValue: '48px 24px', placeholder: '48px 24px' },
      { key: 'bgColor', label: '背景色', type: 'color', defaultValue: '#ffffff' },
    ],
    generateHtml: (c) => `<section style="max-width:${c.maxWidth || '1200px'};margin:0 auto;padding:${c.padding || '48px 24px'};background:${c.bgColor || '#ffffff'}"><p>在此添加内容</p></section>`,
  },
  {
    key: 'two-columns',
    name: '两栏布局',
    icon: '▦',
    category: 'layout',
    description: '左右两栏',
    fields: [
      { key: 'ratio', label: '比例', type: 'select', defaultValue: '1:1', options: [{ label: '1:1', value: '1:1' }, { label: '1:2', value: '1:2' }, { label: '2:1', value: '2:1' }, { label: '1:3', value: '1:3' }, { label: '3:1', value: '3:1' }] },
      { key: 'gap', label: '间距', type: 'input', defaultValue: '24px', placeholder: '24px' },
    ],
    generateHtml: (c) => {
      const ratioMap: Record<string, string> = { '1:1': '1fr 1fr', '1:2': '1fr 2fr', '2:1': '2fr 1fr', '1:3': '1fr 3fr', '3:1': '3fr 1fr' }
      const cols = ratioMap[c.ratio] || '1fr 1fr'
      return `<div style="display:grid;grid-template-columns:${cols};gap:${c.gap || '24px'};padding:24px;max-width:1200px;margin:0 auto"><div style="padding:24px;background:#f9f9f9;border-radius:8px;min-height:100px">左栏内容</div><div style="padding:24px;background:#f9f9f9;border-radius:8px;min-height:100px">右栏内容</div></div>`
    },
  },
  {
    key: 'three-columns',
    name: '三栏布局',
    icon: '▤',
    category: 'layout',
    description: '三列等分',
    fields: [
      { key: 'gap', label: '间距', type: 'input', defaultValue: '24px' },
    ],
    generateHtml: (c) => `<div style="display:grid;grid-template-columns:repeat(3,1fr);gap:${c.gap || '24px'};padding:24px;max-width:1200px;margin:0 auto"><div style="padding:24px;background:#f9f9f9;border-radius:8px">第一栏</div><div style="padding:24px;background:#f9f9f9;border-radius:8px">第二栏</div><div style="padding:24px;background:#f9f9f9;border-radius:8px">第三栏</div></div>`,
  },
  {
    key: 'four-columns',
    name: '四栏布局',
    icon: '⊞',
    category: 'layout',
    description: '四列等分',
    fields: [
      { key: 'gap', label: '间距', type: 'input', defaultValue: '20px' },
    ],
    generateHtml: (c) => `<div style="display:grid;grid-template-columns:repeat(4,1fr);gap:${c.gap || '20px'};padding:24px;max-width:1200px;margin:0 auto"><div style="padding:20px;background:#f9f9f9;border-radius:8px">第一栏</div><div style="padding:20px;background:#f9f9f9;border-radius:8px">第二栏</div><div style="padding:20px;background:#f9f9f9;border-radius:8px">第三栏</div><div style="padding:20px;background:#f9f9f9;border-radius:8px">第四栏</div></div>`,
  },
  {
    key: 'divider',
    name: '分割线',
    icon: '—',
    category: 'layout',
    description: '水平分割线',
    fields: [
      { key: 'color', label: '颜色', type: 'color', defaultValue: '#e8e8e8' },
      { key: 'thickness', label: '粗细', type: 'input', defaultValue: '1px' },
      { key: 'margin', label: '上下间距', type: 'input', defaultValue: '32px' },
    ],
    generateHtml: (c) => `<hr style="border:none;border-top:${c.thickness || '1px'} solid ${c.color || '#e8e8e8'};margin:${c.margin || '32px'} 0">`,
  },
  {
    key: 'spacer',
    name: '间距块',
    icon: '↕',
    category: 'layout',
    description: '纯间距占位',
    fields: [
      { key: 'height', label: '高度', type: 'input', defaultValue: '48px' },
    ],
    generateHtml: (c) => `<div style="height:${c.height || '48px'}"></div>`,
  },
]

// ========== 基础组件 ==========

const basicComponents: ComponentDef[] = [
  {
    key: 'heading',
    name: '标题',
    icon: 'T',
    category: 'basic',
    description: '标题文本 h1-h6',
    fields: [
      { key: 'text', label: '标题文字', type: 'input', defaultValue: '标题文本', required: true },
      { key: 'level', label: '级别', type: 'select', defaultValue: '2', options: [{ label: 'H1 大标题', value: '1' }, { label: 'H2 标题', value: '2' }, { label: 'H3 小标题', value: '3' }, { label: 'H4', value: '4' }] },
      { key: 'align', label: '对齐', type: 'select', defaultValue: 'left', options: [{ label: '左对齐', value: 'left' }, { label: '居中', value: 'center' }, { label: '右对齐', value: 'right' }] },
      { key: 'color', label: '颜色', type: 'color', defaultValue: '#1a1a1a' },
    ],
    generateHtml: (c) => {
      const sizes: Record<string, string> = { '1': '42px', '2': '32px', '3': '24px', '4': '20px' }
      return `<h${c.level || 2} style="font-size:${sizes[c.level] || '32px'};font-weight:700;text-align:${c.align || 'left'};color:${c.color || '#1a1a1a'};padding:16px 24px;margin:0">${c.text || '标题文本'}</h${c.level || 2}>`
    },
  },
  {
    key: 'paragraph',
    name: '段落',
    icon: '¶',
    category: 'basic',
    description: '正文段落',
    fields: [
      { key: 'text', label: '文本内容', type: 'textarea', defaultValue: '这是一段文本内容，可以在属性面板中修改。', required: true },
      { key: 'align', label: '对齐', type: 'select', defaultValue: 'left', options: [{ label: '左对齐', value: 'left' }, { label: '居中', value: 'center' }, { label: '右对齐', value: 'right' }] },
      { key: 'color', label: '颜色', type: 'color', defaultValue: '#555555' },
    ],
    generateHtml: (c) => `<p style="font-size:16px;line-height:1.8;color:${c.color || '#555'};text-align:${c.align || 'left'};padding:8px 24px;max-width:800px;margin:0 auto">${c.text || '段落文本'}</p>`,
  },
  {
    key: 'image',
    name: '图片',
    icon: '🖼',
    category: 'basic',
    description: '单张图片',
    fields: [
      { key: 'src', label: '图片地址', type: 'input', defaultValue: 'https://picsum.photos/800/400', required: true },
      { key: 'alt', label: '替代文本', type: 'input', defaultValue: '图片' },
      { key: 'borderRadius', label: '圆角', type: 'input', defaultValue: '12px' },
      { key: 'shadow', label: '阴影', type: 'switch', defaultValue: false },
    ],
    generateHtml: (c) => `<img src="${c.src || 'https://picsum.photos/800/400'}" alt="${c.alt || '图片'}" style="width:100%;max-width:800px;display:block;margin:24px auto;border-radius:${c.borderRadius || '12px'};${c.shadow ? 'box-shadow:0 8px 24px rgba(0,0,0,0.12);' : ''}">`,
  },
  {
    key: 'button',
    name: '按钮',
    icon: '▢',
    category: 'basic',
    description: '操作按钮',
    fields: [
      { key: 'text', label: '按钮文字', type: 'input', defaultValue: '点击按钮', required: true },
      { key: 'href', label: '链接地址', type: 'input', defaultValue: '#' },
      { key: 'variant', label: '样式', type: 'select', defaultValue: 'primary', options: [{ label: '主要按钮', value: 'primary' }, { label: '次要按钮', value: 'secondary' }, { label: '幽灵按钮', value: 'ghost' }, { label: '危险按钮', value: 'danger' }] },
      { key: 'size', label: '大小', type: 'select', defaultValue: 'medium', options: [{ label: '小', value: 'small' }, { label: '中', value: 'medium' }, { label: '大', value: 'large' }] },
    ],
    generateHtml: (c) => {
      const sizeMap: Record<string, string> = { small: '8px 20px;font-size:13px', medium: '12px 32px;font-size:15px', large: '16px 40px;font-size:17px' }
      const variantMap: Record<string, string> = {
        primary: 'background:#1890ff;color:#fff;border:none',
        secondary: 'background:transparent;color:#1890ff;border:2px solid #1890ff',
        ghost: 'background:transparent;color:#333;border:1px solid #d9d9d9',
        danger: 'background:#ff4d4f;color:#fff;border:none',
      }
      return `<a href="${c.href || '#'}" style="display:inline-block;padding:${sizeMap[c.size] || sizeMap.medium};${variantMap[c.variant] || variantMap.primary};border-radius:6px;text-decoration:none;font-weight:500;margin:16px 24px;cursor:pointer;transition:opacity 0.2s">${c.text || '按钮'}</a>`
    },
  },
  {
    key: 'icon-text',
    name: '图标文字',
    icon: '⭐',
    category: 'basic',
    description: 'Emoji + 文字组合',
    fields: [
      { key: 'icon', label: '图标(emoji)', type: 'input', defaultValue: '🚀' },
      { key: 'text', label: '文字', type: 'input', defaultValue: '功能特点', required: true },
      { key: 'direction', label: '排列', type: 'select', defaultValue: 'horizontal', options: [{ label: '水平', value: 'horizontal' }, { label: '垂直', value: 'vertical' }] },
    ],
    generateHtml: (c) => {
      const dir = c.direction === 'vertical' ? 'column' : 'row'
      return `<div style="display:flex;flex-direction:${dir};align-items:center;gap:8px;padding:12px 24px"><span style="font-size:28px">${c.icon || '🚀'}</span><span style="font-size:16px;color:#333">${c.text || '功能特点'}</span></div>`
    },
  },
  {
    key: 'code-block',
    name: '代码块',
    icon: '</>',
    category: 'basic',
    description: '代码展示',
    fields: [
      { key: 'code', label: '代码内容', type: 'textarea', defaultValue: 'console.log("Hello World");' },
      { key: 'language', label: '语言', type: 'input', defaultValue: 'javascript' },
    ],
    generateHtml: (c) => `<pre style="background:#1e1e1e;color:#d4d4d4;padding:20px;border-radius:8px;overflow-x:auto;margin:16px 24px;font-family:Monaco,Menlo,monospace;font-size:14px;line-height:1.6"><code>${(c.code || '').replace(/</g, '&lt;').replace(/>/g, '&gt;')}</code></pre>`,
  },
  {
    key: 'rich-text',
    name: '富文本',
    icon: '📝',
    category: 'basic',
    description: '自定义 HTML 内容',
    fields: [
      { key: 'html', label: 'HTML 内容', type: 'textarea', defaultValue: '<div style="padding:24px"><h3>自定义内容</h3><p>在这里输入任意 HTML</p></div>' },
    ],
    generateHtml: (c) => c.html || '<div style="padding:24px"><p>富文本内容</p></div>',
  },
]

// ========== 表单组件 ==========

const formComponents: ComponentDef[] = [
  {
    key: 'form-container',
    name: '表单容器',
    icon: '📋',
    category: 'form',
    description: '包裹表单字段的容器',
    fields: [
      { key: 'title', label: '表单标题', type: 'input', defaultValue: '联系我们' },
      { key: 'submitText', label: '提交按钮文字', type: 'input', defaultValue: '提交' },
      { key: 'action', label: '提交地址(URL)', type: 'input', placeholder: 'https://api.example.com/submit' },
      { key: 'method', label: '提交方法', type: 'select', defaultValue: 'POST', options: [{ label: 'POST', value: 'POST' }, { label: 'GET', value: 'GET' }] },
      { key: 'successMsg', label: '提交成功提示', type: 'input', defaultValue: '提交成功！' },
    ],
    generateHtml: (c) => `<form style="max-width:500px;margin:32px auto;padding:32px;background:#fff;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,0.08)" onsubmit="event.preventDefault();${c.action ? `fetch('${c.action}',{method:'${c.method||'POST'}',headers:{'Content-Type':'application/json'},body:JSON.stringify(Object.fromEntries(new FormData(this)))}).then(()=>alert('${c.successMsg||'提交成功！'}')).catch(()=>alert('提交失败'))` : `alert('${c.successMsg||'提交成功！'}')`}"><h3 style="margin-bottom:24px;text-align:center;font-size:22px">${c.title || '联系我们'}</h3><div class="form-fields" style="display:flex;flex-direction:column;gap:16px"><!-- 在此添加表单字段 --></div><button type="submit" style="width:100%;padding:12px;background:#1890ff;color:#fff;border:none;border-radius:6px;font-size:16px;cursor:pointer;margin-top:20px">${c.submitText || '提交'}</button></form>`,
  },
  {
    key: 'input-text',
    name: '文本输入',
    icon: '⌨',
    category: 'form',
    description: '单行文本输入框',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '姓名', required: true },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'name', required: true },
      { key: 'placeholder', label: '占位符', type: 'input', defaultValue: '请输入姓名' },
      { key: 'required', label: '必填', type: 'switch', defaultValue: false },
    ],
    generateHtml: (c) => `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:6px">${c.label || '标签'}${c.required ? '<span style="color:#ff4d4f"> *</span>' : ''}</label><input type="text" name="${c.name || 'field'}" placeholder="${c.placeholder || ''}" ${c.required ? 'required' : ''} style="width:100%;padding:10px 12px;border:1px solid #d9d9d9;border-radius:6px;font-size:14px;box-sizing:border-box;transition:border-color 0.2s" onfocus="this.style.borderColor='#1890ff'" onblur="this.style.borderColor='#d9d9d9'"></div>`,
  },
  {
    key: 'input-email',
    name: '邮箱输入',
    icon: '✉',
    category: 'form',
    description: '邮箱输入框',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '邮箱' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'email' },
      { key: 'placeholder', label: '占位符', type: 'input', defaultValue: '请输入邮箱地址' },
      { key: 'required', label: '必填', type: 'switch', defaultValue: true },
    ],
    generateHtml: (c) => `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:6px">${c.label || '邮箱'}${c.required ? '<span style="color:#ff4d4f"> *</span>' : ''}</label><input type="email" name="${c.name || 'email'}" placeholder="${c.placeholder || ''}" ${c.required ? 'required' : ''} style="width:100%;padding:10px 12px;border:1px solid #d9d9d9;border-radius:6px;font-size:14px;box-sizing:border-box" onfocus="this.style.borderColor='#1890ff'" onblur="this.style.borderColor='#d9d9d9'"></div>`,
  },
  {
    key: 'input-password',
    name: '密码输入',
    icon: '🔒',
    category: 'form',
    description: '密码输入框',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '密码' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'password' },
      { key: 'placeholder', label: '占位符', type: 'input', defaultValue: '请输入密码' },
      { key: 'required', label: '必填', type: 'switch', defaultValue: true },
    ],
    generateHtml: (c) => `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:6px">${c.label || '密码'}${c.required ? '<span style="color:#ff4d4f"> *</span>' : ''}</label><input type="password" name="${c.name || 'password'}" placeholder="${c.placeholder || ''}" ${c.required ? 'required' : ''} style="width:100%;padding:10px 12px;border:1px solid #d9d9d9;border-radius:6px;font-size:14px;box-sizing:border-box" onfocus="this.style.borderColor='#1890ff'" onblur="this.style.borderColor='#d9d9d9'"></div>`,
  },
  {
    key: 'input-number',
    name: '数字输入',
    icon: '#',
    category: 'form',
    description: '数字输入框',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '数量' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'quantity' },
      { key: 'min', label: '最小值', type: 'number', defaultValue: 0 },
      { key: 'max', label: '最大值', type: 'number', defaultValue: 100 },
      { key: 'placeholder', label: '占位符', type: 'input', defaultValue: '请输入数字' },
    ],
    generateHtml: (c) => `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:6px">${c.label || '数量'}</label><input type="number" name="${c.name || 'number'}" min="${c.min ?? 0}" max="${c.max ?? 100}" placeholder="${c.placeholder || ''}" style="width:100%;padding:10px 12px;border:1px solid #d9d9d9;border-radius:6px;font-size:14px;box-sizing:border-box" onfocus="this.style.borderColor='#1890ff'" onblur="this.style.borderColor='#d9d9d9'"></div>`,
  },
  {
    key: 'textarea',
    name: '文本域',
    icon: '📄',
    category: 'form',
    description: '多行文本输入',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '留言' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'message' },
      { key: 'rows', label: '行数', type: 'number', defaultValue: 4, min: 2, max: 10 },
      { key: 'placeholder', label: '占位符', type: 'input', defaultValue: '请输入内容' },
    ],
    generateHtml: (c) => `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:6px">${c.label || '留言'}</label><textarea name="${c.name || 'message'}" rows="${c.rows || 4}" placeholder="${c.placeholder || ''}" style="width:100%;padding:10px 12px;border:1px solid #d9d9d9;border-radius:6px;font-size:14px;box-sizing:border-box;resize:vertical" onfocus="this.style.borderColor='#1890ff'" onblur="this.style.borderColor='#d9d9d9'"></textarea></div>`,
  },
  {
    key: 'select',
    name: '下拉选择',
    icon: '▼',
    category: 'form',
    description: '下拉选择框',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '选择' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'select' },
      { key: 'optionsText', label: '选项(每行一个)', type: 'textarea', defaultValue: '选项一\n选项二\n选项三' },
      { key: 'required', label: '必填', type: 'switch', defaultValue: false },
    ],
    generateHtml: (c) => {
      const opts = (c.optionsText || '选项一\n选项二\n选项三').split('\n').filter((s: string) => s.trim()).map((s: string) => `<option value="${s.trim()}">${s.trim()}</option>`).join('')
      return `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:6px">${c.label || '选择'}${c.required ? '<span style="color:#ff4d4f"> *</span>' : ''}</label><select name="${c.name || 'select'}" ${c.required ? 'required' : ''} style="width:100%;padding:10px 12px;border:1px solid #d9d9d9;border-radius:6px;font-size:14px;box-sizing:border-box;background:#fff"><option value="">请选择</option>${opts}</select></div>`
    },
  },
  {
    key: 'radio-group',
    name: '单选组',
    icon: '◉',
    category: 'form',
    description: '单选按钮组',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '性别' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'gender' },
      { key: 'optionsText', label: '选项(每行一个)', type: 'textarea', defaultValue: '男\n女\n其他' },
    ],
    generateHtml: (c) => {
      const name = c.name || 'radio'
      const opts = (c.optionsText || '选项一\n选项二').split('\n').filter((s: string) => s.trim()).map((s: string, i: number) => `<label style="display:inline-flex;align-items:center;gap:6px;margin-right:16px;cursor:pointer;font-size:14px"><input type="radio" name="${name}" value="${s.trim()}" ${i === 0 ? 'checked' : ''} style="accent-color:#1890ff">${s.trim()}</label>`).join('')
      return `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:8px">${c.label || '单选'}</label><div>${opts}</div></div>`
    },
  },
  {
    key: 'checkbox-group',
    name: '多选组',
    icon: '☑',
    category: 'form',
    description: '复选框组',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '兴趣爱好' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'hobbies' },
      { key: 'optionsText', label: '选项(每行一个)', type: 'textarea', defaultValue: '阅读\n运动\n音乐\n旅行' },
    ],
    generateHtml: (c) => {
      const name = c.name || 'checkbox'
      const opts = (c.optionsText || '选项一\n选项二').split('\n').filter((s: string) => s.trim()).map((s: string) => `<label style="display:inline-flex;align-items:center;gap:6px;margin-right:16px;cursor:pointer;font-size:14px"><input type="checkbox" name="${name}" value="${s.trim()}" style="accent-color:#1890ff">${s.trim()}</label>`).join('')
      return `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:8px">${c.label || '多选'}</label><div style="display:flex;flex-wrap:wrap;gap:8px">${opts}</div></div>`
    },
  },
  {
    key: 'toggle-switch',
    name: '开关',
    icon: '🔘',
    category: 'form',
    description: '开关切换',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '接收通知' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'notify' },
      { key: 'checked', label: '默认开启', type: 'switch', defaultValue: false },
    ],
    generateHtml: (c) => `<div style="margin-bottom:16px;display:flex;align-items:center;justify-content:space-between"><label style="font-size:14px;font-weight:500;color:#333">${c.label || '开关'}</label><label style="position:relative;display:inline-block;width:44px;height:24px;cursor:pointer"><input type="checkbox" name="${c.name || 'toggle'}" ${c.checked ? 'checked' : ''} style="opacity:0;width:0;height:0" onchange="this.nextElementSibling.style.background=this.checked?'#1890ff':'#ccc';this.nextElementSibling.querySelector('span').style.transform=this.checked?'translateX(20px)':'translateX(0)'"><div style="position:absolute;top:0;left:0;right:0;bottom:0;background:${c.checked ? '#1890ff' : '#ccc'};border-radius:12px;transition:0.3s"><span style="position:absolute;top:2px;left:2px;width:20px;height:20px;background:#fff;border-radius:50%;transition:0.3s;transform:${c.checked ? 'translateX(20px)' : 'translateX(0)'}"></span></div></label></div>`,
  },
  {
    key: 'date-input',
    name: '日期选择',
    icon: '📅',
    category: 'form',
    description: '日期选择器',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '日期' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'date' },
      { key: 'required', label: '必填', type: 'switch', defaultValue: false },
    ],
    generateHtml: (c) => `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:6px">${c.label || '日期'}${c.required ? '<span style="color:#ff4d4f"> *</span>' : ''}</label><input type="date" name="${c.name || 'date'}" ${c.required ? 'required' : ''} style="width:100%;padding:10px 12px;border:1px solid #d9d9d9;border-radius:6px;font-size:14px;box-sizing:border-box"></div>`,
  },
  {
    key: 'file-upload',
    name: '文件上传',
    icon: '📎',
    category: 'form',
    description: '文件上传',
    fields: [
      { key: 'label', label: '标签', type: 'input', defaultValue: '上传文件' },
      { key: 'name', label: '字段名', type: 'input', defaultValue: 'file' },
      { key: 'accept', label: '接受类型', type: 'input', placeholder: '.jpg,.png,.pdf' },
      { key: 'multiple', label: '多选', type: 'switch', defaultValue: false },
    ],
    generateHtml: (c) => `<div style="margin-bottom:16px"><label style="display:block;font-size:14px;font-weight:500;color:#333;margin-bottom:6px">${c.label || '上传文件'}</label><input type="file" name="${c.name || 'file'}" ${c.accept ? `accept="${c.accept}"` : ''} ${c.multiple ? 'multiple' : ''} style="width:100%;padding:10px;border:2px dashed #d9d9d9;border-radius:6px;font-size:14px;box-sizing:border-box;cursor:pointer"></div>`,
  },
]

// ========== 数据展示组件 ==========

const dataComponents: ComponentDef[] = [
  {
    key: 'data-table',
    name: '数据表格',
    icon: '📊',
    category: 'data',
    description: '表格数据展示',
    fields: [
      { key: 'columns', label: '列名(逗号分隔)', type: 'input', defaultValue: '姓名,邮箱,角色,状态' },
      { key: 'rows', label: '行数', type: 'number', defaultValue: 3, min: 1, max: 20 },
      { key: 'striped', label: '斑马纹', type: 'switch', defaultValue: true },
    ],
    generateHtml: (c) => {
      const cols = (c.columns || '列1,列2,列3').split(',').map((s: string) => s.trim())
      const ths = cols.map((col: string) => `<th style="padding:12px 16px;text-align:left;font-weight:600;color:#333;border-bottom:2px solid #e8e8e8">${col}</th>`).join('')
      let rows = ''
      for (let i = 0; i < (c.rows || 3); i++) {
        const bg = c.striped && i % 2 === 1 ? 'background:#fafafa;' : ''
        const tds = cols.map((_: string, j: number) => `<td style="padding:12px 16px;border-bottom:1px solid #f0f0f0;color:#555">数据 ${i + 1}-${j + 1}</td>`).join('')
        rows += `<tr style="${bg}">${tds}</tr>`
      }
      return `<div style="overflow-x:auto;margin:24px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.06)"><table style="width:100%;border-collapse:collapse;background:#fff"><thead><tr style="background:#fafafa">${ths}</tr></thead><tbody>${rows}</tbody></table></div>`
    },
  },
  {
    key: 'stat-card',
    name: '统计卡片',
    icon: '📈',
    category: 'data',
    description: '数字统计展示',
    fields: [
      { key: 'value', label: '数值', type: 'input', defaultValue: '12,345', required: true },
      { key: 'label', label: '标签', type: 'input', defaultValue: '总用户数', required: true },
      { key: 'icon', label: '图标(emoji)', type: 'input', defaultValue: '👥' },
      { key: 'trend', label: '趋势', type: 'select', defaultValue: 'up', options: [{ label: '上升', value: 'up' }, { label: '下降', value: 'down' }, { label: '无', value: 'none' }] },
      { key: 'trendValue', label: '趋势值', type: 'input', defaultValue: '+12.5%' },
    ],
    generateHtml: (c) => {
      const trendColor = c.trend === 'up' ? '#52c41a' : c.trend === 'down' ? '#ff4d4f' : '#999'
      const trendHtml = c.trend !== 'none' ? `<span style="font-size:13px;color:${trendColor};margin-left:8px">${c.trendValue || ''}</span>` : ''
      return `<div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,0.06);min-width:200px"><div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px"><span style="font-size:14px;color:#888">${c.label || '统计'}</span><span style="font-size:28px">${c.icon || '📊'}</span></div><div style="display:flex;align-items:baseline"><span style="font-size:32px;font-weight:700;color:#1a1a1a">${c.value || '0'}</span>${trendHtml}</div></div>`
    },
  },
  {
    key: 'progress-bar',
    name: '进度条',
    icon: '▰',
    category: 'data',
    description: '进度展示',
    fields: [
      { key: 'percent', label: '百分比', type: 'number', defaultValue: 75, min: 0, max: 100 },
      { key: 'label', label: '标签', type: 'input', defaultValue: '完成进度' },
      { key: 'color', label: '颜色', type: 'color', defaultValue: '#1890ff' },
      { key: 'height', label: '高度', type: 'input', defaultValue: '8px' },
    ],
    generateHtml: (c) => `<div style="padding:12px 24px"><div style="display:flex;justify-content:space-between;margin-bottom:8px"><span style="font-size:14px;color:#555">${c.label || '进度'}</span><span style="font-size:14px;font-weight:600;color:#333">${c.percent || 0}%</span></div><div style="background:#f0f0f0;border-radius:100px;overflow:hidden;height:${c.height || '8px'}"><div style="width:${c.percent || 0}%;height:100%;background:${c.color || '#1890ff'};border-radius:100px;transition:width 0.6s ease"></div></div></div>`,
  },
  {
    key: 'tag-list',
    name: '标签组',
    icon: '🏷',
    category: 'data',
    description: '标签列表',
    fields: [
      { key: 'tags', label: '标签(逗号分隔)', type: 'input', defaultValue: 'Vue,React,TypeScript,Node.js,Python' },
      { key: 'color', label: '主题色', type: 'color', defaultValue: '#1890ff' },
    ],
    generateHtml: (c) => {
      const tags = (c.tags || 'Tag1,Tag2').split(',').map((s: string) => `<span style="display:inline-block;padding:4px 12px;background:${c.color || '#1890ff'}15;color:${c.color || '#1890ff'};border-radius:4px;font-size:13px;border:1px solid ${c.color || '#1890ff'}30">${s.trim()}</span>`).join('')
      return `<div style="display:flex;flex-wrap:wrap;gap:8px;padding:12px 24px">${tags}</div>`
    },
  },
  {
    key: 'timeline',
    name: '时间线',
    icon: '📅',
    category: 'data',
    description: '时间线展示',
    fields: [
      { key: 'items', label: '事件(每行: 时间|内容)', type: 'textarea', defaultValue: '2024-01|项目启动\n2024-03|完成设计\n2024-06|开发完成\n2024-08|正式上线' },
      { key: 'color', label: '主题色', type: 'color', defaultValue: '#1890ff' },
    ],
    generateHtml: (c) => {
      const items = (c.items || '').split('\n').filter((s: string) => s.trim()).map((s: string) => {
        const [time, content] = s.split('|').map((p: string) => p.trim())
        return `<div style="display:flex;gap:16px;padding-bottom:24px"><div style="display:flex;flex-direction:column;align-items:center"><div style="width:12px;height:12px;border-radius:50%;background:${c.color || '#1890ff'};flex-shrink:0"></div><div style="width:2px;flex:1;background:#e8e8e8"></div></div><div><div style="font-size:13px;color:#999;margin-bottom:4px">${time || ''}</div><div style="font-size:15px;color:#333">${content || ''}</div></div></div>`
      }).join('')
      return `<div style="padding:24px;max-width:600px;margin:0 auto">${items}</div>`
    },
  },
  {
    key: 'rating',
    name: '评分',
    icon: '⭐',
    category: 'data',
    description: '星级评分',
    fields: [
      { key: 'score', label: '分数', type: 'number', defaultValue: 4, min: 0, max: 5 },
      { key: 'total', label: '总分', type: 'number', defaultValue: 5 },
      { key: 'label', label: '标签', type: 'input', defaultValue: '' },
    ],
    generateHtml: (c) => {
      const score = Math.min(c.score || 0, c.total || 5)
      const stars = Array.from({ length: c.total || 5 }, (_, i) => i < score ? '★' : '☆').join('')
      return `<div style="display:flex;align-items:center;gap:8px;padding:8px 24px"><span style="font-size:24px;color:#faad14;letter-spacing:4px">${stars}</span>${c.label ? `<span style="font-size:14px;color:#888">${c.label}</span>` : ''}<span style="font-size:14px;font-weight:600;color:#333">${score}/${c.total || 5}</span></div>`
    },
  },
  {
    key: 'empty-state',
    name: '空状态',
    icon: '📭',
    category: 'data',
    description: '空数据提示',
    fields: [
      { key: 'icon', label: '图标(emoji)', type: 'input', defaultValue: '📭' },
      { key: 'title', label: '标题', type: 'input', defaultValue: '暂无数据' },
      { key: 'description', label: '描述', type: 'input', defaultValue: '当前没有可显示的内容' },
      { key: 'buttonText', label: '按钮文字', type: 'input', defaultValue: '' },
    ],
    generateHtml: (c) => {
      const btn = c.buttonText ? `<a href="#" style="display:inline-block;padding:8px 24px;background:#1890ff;color:#fff;border-radius:6px;text-decoration:none;font-size:14px;margin-top:16px">${c.buttonText}</a>` : ''
      return `<div style="display:flex;flex-direction:column;align-items:center;justify-content:center;padding:64px 24px;color:#999"><div style="font-size:64px;margin-bottom:16px">${c.icon || '📭'}</div><div style="font-size:18px;font-weight:500;color:#333;margin-bottom:8px">${c.title || '暂无数据'}</div><div style="font-size:14px">${c.description || ''}</div>${btn}</div>`
    },
  },
]

// ========== 业务组件 ==========

const businessComponents: ComponentDef[] = [
  {
    key: 'navbar',
    name: '导航栏',
    icon: '☰',
    category: 'business',
    description: '顶部导航',
    fields: [
      { key: 'logo', label: 'Logo 文字', type: 'input', defaultValue: 'Logo' },
      { key: 'links', label: '导航链接(每行: 文字|链接)', type: 'textarea', defaultValue: '首页|#\n关于|#about\n服务|#services\n联系|#contact' },
      { key: 'bgColor', label: '背景色', type: 'color', defaultValue: '#ffffff' },
      { key: 'sticky', label: '固定顶部', type: 'switch', defaultValue: true },
    ],
    generateHtml: (c) => {
      const links = (c.links || '').split('\n').filter((s: string) => s.trim()).map((s: string) => { const [text, href] = s.split('|'); return `<a href="${(href||'#').trim()}" style="color:#555;text-decoration:none;font-size:15px;transition:color 0.2s" onmouseover="this.style.color='#1890ff'" onmouseout="this.style.color='#555'">${(text||'').trim()}</a>` }).join('')
      return `<nav style="display:flex;align-items:center;justify-content:space-between;padding:0 24px;height:64px;background:${c.bgColor || '#fff'};box-shadow:0 2px 8px rgba(0,0,0,0.06);${c.sticky ? 'position:sticky;top:0;z-index:1000;' : ''}"><div style="font-size:22px;font-weight:700;color:#1890ff">${c.logo || 'Logo'}</div><div style="display:flex;gap:28px">${links}</div></nav>`
    },
  },
  {
    key: 'hero-banner',
    name: 'Hero 横幅',
    icon: '🎯',
    category: 'business',
    description: '大图标题区',
    fields: [
      { key: 'title', label: '主标题', type: 'input', defaultValue: '欢迎来到我们的平台', required: true },
      { key: 'subtitle', label: '副标题', type: 'input', defaultValue: '用技术改变世界，让创新触手可及' },
      { key: 'buttonText', label: '按钮文字', type: 'input', defaultValue: '立即开始' },
      { key: 'buttonLink', label: '按钮链接', type: 'input', defaultValue: '#' },
      { key: 'bgImage', label: '背景图', type: 'input', defaultValue: 'https://picsum.photos/1920/600' },
      { key: 'height', label: '高度', type: 'input', defaultValue: '500px' },
      { key: 'overlay', label: '遮罩透明度', type: 'number', defaultValue: 40, min: 0, max: 100 },
    ],
    generateHtml: (c) => {
      const overlay = (c.overlay ?? 40) / 100
      const btn = c.buttonText ? `<a href="${c.buttonLink || '#'}" style="display:inline-block;padding:14px 40px;background:#fff;color:#333;border-radius:8px;text-decoration:none;font-weight:600;font-size:16px;transition:transform 0.2s" onmouseover="this.style.transform='translateY(-2px)'" onmouseout="this.style.transform='none'">${c.buttonText}</a>` : ''
      return `<section style="display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:${c.height || '500px'};background:linear-gradient(rgba(0,0,0,${overlay}),rgba(0,0,0,${overlay})),url('${c.bgImage || 'https://picsum.photos/1920/600'}') center/cover no-repeat;color:#fff;text-align:center;padding:48px 24px"><h1 style="font-size:48px;font-weight:700;margin-bottom:16px;text-shadow:0 2px 4px rgba(0,0,0,0.3)">${c.title || '主标题'}</h1><p style="font-size:20px;opacity:0.9;margin-bottom:36px;max-width:600px">${c.subtitle || ''}</p>${btn}</section>`
    },
  },
  {
    key: 'card',
    name: '卡片',
    icon: '🃏',
    category: 'business',
    description: '图文卡片',
    fields: [
      { key: 'image', label: '图片', type: 'input', defaultValue: 'https://picsum.photos/400/250' },
      { key: 'title', label: '标题', type: 'input', defaultValue: '卡片标题', required: true },
      { key: 'description', label: '描述', type: 'textarea', defaultValue: '这是卡片的描述内容，介绍产品或服务的特点。' },
      { key: 'link', label: '链接', type: 'input', defaultValue: '' },
    ],
    generateHtml: (c) => `<div style="border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.08);background:#fff;max-width:360px;transition:transform 0.2s,box-shadow 0.2s" onmouseover="this.style.transform='translateY(-4px)';this.style.boxShadow='0 8px 24px rgba(0,0,0,0.12)'" onmouseout="this.style.transform='none';this.style.boxShadow='0 4px 12px rgba(0,0,0,0.08)'"><img src="${c.image || 'https://picsum.photos/400/250'}" style="width:100%;height:200px;object-fit:cover"><div style="padding:20px"><h3 style="font-size:18px;margin-bottom:8px;color:#1a1a1a">${c.title || '标题'}</h3><p style="font-size:14px;color:#666;line-height:1.6">${c.description || ''}</p>${c.link ? `<a href="${c.link}" style="display:inline-block;margin-top:12px;color:#1890ff;font-size:14px;text-decoration:none">了解更多 →</a>` : ''}</div></div>`,
  },
  {
    key: 'card-list',
    name: '卡片列表',
    icon: '🗂',
    category: 'business',
    description: '多卡片网格',
    fields: [
      { key: 'columns', label: '列数', type: 'number', defaultValue: 3, min: 2, max: 4 },
      { key: 'count', label: '卡片数量', type: 'number', defaultValue: 3, min: 1, max: 12 },
      { key: 'gap', label: '间距', type: 'input', defaultValue: '24px' },
    ],
    generateHtml: (c) => {
      const cards = Array.from({ length: c.count || 3 }, (_, i) => `<div style="border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.08);background:#fff;transition:transform 0.2s" onmouseover="this.style.transform='translateY(-4px)'" onmouseout="this.style.transform='none'"><img src="https://picsum.photos/400/250?random=${i + 1}" style="width:100%;height:180px;object-fit:cover"><div style="padding:20px"><h3 style="font-size:17px;margin-bottom:8px">卡片标题 ${i + 1}</h3><p style="font-size:14px;color:#666;line-height:1.5">卡片描述内容</p></div></div>`).join('')
      return `<div style="display:grid;grid-template-columns:repeat(${c.columns || 3},1fr);gap:${c.gap || '24px'};padding:24px;max-width:1200px;margin:0 auto">${cards}</div>`
    },
  },
  {
    key: 'feature-grid',
    name: '特性网格',
    icon: '✨',
    category: 'business',
    description: '图标+标题+描述',
    fields: [
      { key: 'columns', label: '列数', type: 'number', defaultValue: 3, min: 2, max: 4 },
      { key: 'items', label: '特性(每行: 图标|标题|描述)', type: 'textarea', defaultValue: '🚀|极速体验|毫秒级响应，流畅无卡顿\n🎨|精美设计|专业设计团队打造\n🔒|安全可靠|企业级安全防护\n📱|多端适配|完美支持各种设备' },
    ],
    generateHtml: (c) => {
      const items = (c.items || '').split('\n').filter((s: string) => s.trim()).map((s: string) => {
        const [icon, title, desc] = s.split('|').map((p: string) => p.trim())
        return `<div style="text-align:center;padding:32px 20px"><div style="font-size:48px;margin-bottom:16px">${icon || '⭐'}</div><h3 style="font-size:20px;margin-bottom:12px;color:#1a1a1a">${title || '特性'}</h3><p style="font-size:15px;color:#666;line-height:1.6">${desc || ''}</p></div>`
      }).join('')
      return `<div style="display:grid;grid-template-columns:repeat(${c.columns || 3},1fr);gap:24px;padding:64px 24px;max-width:1200px;margin:0 auto">${items}</div>`
    },
  },
  {
    key: 'pricing-table',
    name: '定价表',
    icon: '💰',
    category: 'business',
    description: '价格方案对比',
    fields: [
      { key: 'plans', label: '方案(每行: 名称|价格|描述|特性1,特性2,...)', type: 'textarea', defaultValue: '基础版|¥99/月|适合个人用户|5GB存储,基础支持,单用户\n专业版|¥299/月|适合小团队|50GB存储,优先支持,10用户,API访问\n企业版|¥999/月|适合大企业|无限存储,专属支持,无限用户,API访问,定制开发' },
      { key: 'recommended', label: '推荐方案(序号,从1开始)', type: 'number', defaultValue: 2 },
    ],
    generateHtml: (c) => {
      const plans = (c.plans || '').split('\n').filter((s: string) => s.trim()).map((s: string, i: number) => {
        const [name, price, desc, featuresStr] = s.split('|').map((p: string) => p.trim())
        const isRec = i + 1 === (c.recommended || 2)
        const features = (featuresStr || '').split(',').map((f: string) => `<li style="padding:8px 0;border-bottom:1px solid #f5f5f5;font-size:14px;color:#555">✓ ${f.trim()}</li>`).join('')
        return `<div style="background:#fff;border-radius:12px;padding:32px 24px;box-shadow:0 4px 12px rgba(0,0,0,${isRec ? '0.12' : '0.06'});text-align:center;${isRec ? 'border:2px solid #1890ff;transform:scale(1.05);position:relative;' : ''}">${isRec ? '<div style="position:absolute;top:-12px;left:50%;transform:translateX(-50%);background:#1890ff;color:#fff;padding:4px 16px;border-radius:12px;font-size:12px;font-weight:500">推荐</div>' : ''}<h3 style="font-size:20px;margin-bottom:8px">${name || '方案'}</h3><p style="font-size:14px;color:#888;margin-bottom:16px">${desc || ''}</p><div style="font-size:36px;font-weight:700;color:#1890ff;margin-bottom:24px">${price || '¥0'}</div><ul style="list-style:none;padding:0;margin:0 0 24px;text-align:left">${features}</ul><a href="#" style="display:block;padding:12px;background:${isRec ? '#1890ff' : '#f5f5f5'};color:${isRec ? '#fff' : '#333'};border-radius:6px;text-decoration:none;font-weight:500">选择方案</a></div>`
      }).join('')
      return `<div style="display:grid;grid-template-columns:repeat(${Math.min((c.plans || '').split('\n').filter((s: string) => s.trim()).length, 4)},1fr);gap:24px;padding:48px 24px;max-width:1200px;margin:0 auto;align-items:start">${plans}</div>`
    },
  },
  {
    key: 'testimonials',
    name: '客户评价',
    icon: '💬',
    category: 'business',
    description: '用户评价展示',
    fields: [
      { key: 'items', label: '评价(每行: 姓名|职位|内容|头像URL)', type: 'textarea', defaultValue: '张三|产品经理|非常好用的平台，大大提升了我们的开发效率！|https://picsum.photos/80/80?random=1\n李四|技术总监|企业级的质量，推荐给所有团队使用。|https://picsum.photos/80/80?random=2\n王五|设计师|界面美观，操作流畅，体验一流。|https://picsum.photos/80/80?random=3' },
    ],
    generateHtml: (c) => {
      const items = (c.items || '').split('\n').filter((s: string) => s.trim()).map((s: string) => {
        const [name, title, content, avatar] = s.split('|').map((p: string) => p.trim())
        return `<div style="background:#fff;border-radius:12px;padding:28px;box-shadow:0 2px 8px rgba(0,0,0,0.06)"><p style="font-size:15px;color:#555;line-height:1.7;margin-bottom:20px;font-style:italic">"${content || ''}"</p><div style="display:flex;align-items:center;gap:12px"><img src="${avatar || 'https://picsum.photos/80/80'}" style="width:44px;height:44px;border-radius:50%;object-fit:cover"><div><div style="font-weight:600;font-size:15px;color:#333">${name || '用户'}</div><div style="font-size:13px;color:#999">${title || ''}</div></div></div></div>`
      }).join('')
      return `<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:24px;padding:48px 24px;max-width:1200px;margin:0 auto">${items}</div>`
    },
  },
  {
    key: 'faq',
    name: 'FAQ 折叠',
    icon: '❓',
    category: 'business',
    description: '常见问题折叠面板',
    fields: [
      { key: 'items', label: '问答(每行: 问题|答案)', type: 'textarea', defaultValue: '这个平台是免费的吗？|我们提供免费基础版和付费专业版，基础版包含核心功能。\n支持哪些浏览器？|支持所有现代浏览器，包括 Chrome、Firefox、Safari、Edge。\n如何联系客服？|您可以通过页面底部的联系方式或发送邮件至 support@example.com。' },
    ],
    generateHtml: (c) => {
      const items = (c.items || '').split('\n').filter((s: string) => s.trim()).map((s: string, i: number) => {
        const [q, a] = s.split('|').map((p: string) => p.trim())
        return `<details style="border:1px solid #e8e8e8;border-radius:8px;margin-bottom:8px;overflow:hidden" ${i === 0 ? 'open' : ''}><summary style="padding:16px 20px;cursor:pointer;font-size:16px;font-weight:500;color:#333;background:#fafafa;list-style:none;display:flex;justify-content:space-between;align-items:center">${q || '问题'}<span style="transition:transform 0.2s">▼</span></summary><div style="padding:16px 20px;font-size:15px;color:#666;line-height:1.7;border-top:1px solid #e8e8e8">${a || '答案'}</div></details>`
      }).join('')
      return `<div style="max-width:800px;margin:0 auto;padding:48px 24px">${items}</div>`
    },
  },
  {
    key: 'footer',
    name: '页脚',
    icon: '⊥',
    category: 'business',
    description: '底部信息栏',
    fields: [
      { key: 'copyright', label: '版权文字', type: 'input', defaultValue: '© 2024 Company. All Rights Reserved.' },
      { key: 'links', label: '链接(每行: 文字|链接)', type: 'textarea', defaultValue: '关于我们|#\n联系方式|#\n隐私政策|#\n服务条款|#' },
      { key: 'bgColor', label: '背景色', type: 'color', defaultValue: '#1a1a2e' },
    ],
    generateHtml: (c) => {
      const links = (c.links || '').split('\n').filter((s: string) => s.trim()).map((s: string) => { const [text, href] = s.split('|'); return `<a href="${(href||'#').trim()}" style="color:#aaa;text-decoration:none;font-size:14px;transition:color 0.2s" onmouseover="this.style.color='#fff'" onmouseout="this.style.color='#aaa'">${(text||'').trim()}</a>` }).join('')
      return `<footer style="background:${c.bgColor || '#1a1a2e'};color:#ccc;padding:48px 24px;text-align:center"><div style="display:flex;justify-content:center;gap:24px;margin-bottom:20px;flex-wrap:wrap">${links}</div><p style="font-size:13px;color:#888">${c.copyright || '© 2024'}</p></footer>`
    },
  },
]

// ========== 媒体组件 ==========

const mediaComponents: ComponentDef[] = [
  {
    key: 'video-embed',
    name: '视频',
    icon: '🎬',
    category: 'media',
    description: '嵌入视频',
    fields: [
      { key: 'src', label: '视频地址', type: 'input', defaultValue: '', placeholder: 'https://example.com/video.mp4 或 iframe 嵌入地址' },
      { key: 'poster', label: '封面图', type: 'input', defaultValue: '' },
      { key: 'autoplay', label: '自动播放', type: 'switch', defaultValue: false },
      { key: 'height', label: '高度', type: 'input', defaultValue: '400px' },
    ],
    generateHtml: (c) => {
      if (c.src && (c.src.includes('iframe') || c.src.includes('bilibili') || c.src.includes('youtube'))) {
        return `<div style="max-width:800px;margin:24px auto;border-radius:12px;overflow:hidden;height:${c.height || '400px'}"><iframe src="${c.src}" style="width:100%;height:100%;border:none" allowfullscreen></iframe></div>`
      }
      return `<div style="max-width:800px;margin:24px auto;border-radius:12px;overflow:hidden"><video ${c.src ? `src="${c.src}"` : ''} ${c.poster ? `poster="${c.poster}"` : ''} ${c.autoplay ? 'autoplay muted' : ''} controls style="width:100%;height:${c.height || '400px'};object-fit:cover;background:#000"></video></div>`
    },
  },
  {
    key: 'map-embed',
    name: '地图',
    icon: '🗺',
    category: 'media',
    description: '嵌入地图',
    fields: [
      { key: 'address', label: '地址', type: 'input', defaultValue: '北京市海淀区' },
      { key: 'height', label: '高度', type: 'input', defaultValue: '350px' },
    ],
    generateHtml: (c) => `<div style="max-width:1000px;margin:24px auto;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08)"><iframe src="https://maps.google.com/maps?q=${encodeURIComponent(c.address || '北京')}&output=embed" style="width:100%;height:${c.height || '350px'};border:none" loading="lazy"></iframe></div>`,
  },
  {
    key: 'image-gallery',
    name: '图片画廊',
    icon: '🖼',
    category: 'media',
    description: '多图网格展示',
    fields: [
      { key: 'columns', label: '列数', type: 'number', defaultValue: 3, min: 2, max: 5 },
      { key: 'count', label: '图片数量', type: 'number', defaultValue: 6, min: 2, max: 20 },
      { key: 'gap', label: '间距', type: 'input', defaultValue: '8px' },
      { key: 'borderRadius', label: '圆角', type: 'input', defaultValue: '8px' },
    ],
    generateHtml: (c) => {
      const imgs = Array.from({ length: c.count || 6 }, (_, i) => `<img src="https://picsum.photos/400/300?random=${i + 10}" style="width:100%;height:200px;object-fit:cover;border-radius:${c.borderRadius || '8px'};cursor:pointer;transition:transform 0.2s" onmouseover="this.style.transform='scale(1.03)'" onmouseout="this.style.transform='none'">`).join('')
      return `<div style="display:grid;grid-template-columns:repeat(${c.columns || 3},1fr);gap:${c.gap || '8px'};padding:24px;max-width:1200px;margin:0 auto">${imgs}</div>`
    },
  },
  {
    key: 'carousel',
    name: '轮播图',
    icon: '🎠',
    category: 'media',
    description: '图片轮播',
    fields: [
      { key: 'count', label: '图片数量', type: 'number', defaultValue: 3, min: 2, max: 8 },
      { key: 'height', label: '高度', type: 'input', defaultValue: '400px' },
      { key: 'autoplay', label: '自动播放', type: 'switch', defaultValue: true },
      { key: 'interval', label: '切换间隔(秒)', type: 'number', defaultValue: 3, min: 1, max: 10 },
    ],
    generateHtml: (c) => {
      const id = 'carousel_' + Date.now()
      const slides = Array.from({ length: c.count || 3 }, (_, i) => `<div style="min-width:100%;height:100%"><img src="https://picsum.photos/1200/400?random=${i + 20}" style="width:100%;height:100%;object-fit:cover"></div>`).join('')
      return `<div id="${id}" style="position:relative;overflow:hidden;height:${c.height || '400px'};border-radius:12px;margin:24px auto;max-width:1200px"><div class="track" style="display:flex;transition:transform 0.5s ease;height:100%">${slides}</div><button onclick="(function(){var t=document.querySelector('#${id} .track'),n=t.children.length,c=parseInt(t.dataset.i||0);c=(c-1+n)%n;t.style.transform='translateX(-'+c*100+'%)';t.dataset.i=c})()" style="position:absolute;top:50%;left:16px;transform:translateY(-50%);background:rgba(255,255,255,0.8);border:none;width:40px;height:40px;border-radius:50%;cursor:pointer;font-size:18px;z-index:10">‹</button><button onclick="(function(){var t=document.querySelector('#${id} .track'),n=t.children.length,c=parseInt(t.dataset.i||0);c=(c+1)%n;t.style.transform='translateX(-'+c*100+'%)';t.dataset.i=c})()" style="position:absolute;top:50%;right:16px;transform:translateY(-50%);background:rgba(255,255,255,0.8);border:none;width:40px;height:40px;border-radius:50%;cursor:pointer;font-size:18px;z-index:10">›</button></div>${c.autoplay ? `<script>(function(){setInterval(function(){var t=document.querySelector('#${id} .track');if(!t)return;var n=t.children.length,c=parseInt(t.dataset.i||0);c=(c+1)%n;t.style.transform='translateX(-'+c*100+'%)';t.dataset.i=c},${(c.interval || 3) * 1000})})()</script>` : ''}`
    },
  },
]

// ========== 导出 ==========

export const componentCategories = [
  { key: 'layout', label: '布局', icon: '📐' },
  { key: 'basic', label: '基础', icon: '🔤' },
  { key: 'form', label: '表单', icon: '📋' },
  { key: 'data', label: '数据', icon: '📊' },
  { key: 'business', label: '业务', icon: '🏢' },
  { key: 'media', label: '媒体', icon: '🎬' },
] as const

export const allComponents: ComponentDef[] = [
  ...layoutComponents,
  ...basicComponents,
  ...formComponents,
  ...dataComponents,
  ...businessComponents,
  ...mediaComponents,
]

export function getComponentsByCategory(category: string): ComponentDef[] {
  return allComponents.filter((c) => c.category === category)
}

export function getComponentByKey(key: string): ComponentDef | undefined {
  return allComponents.find((c) => c.key === key)
}
