/**
 * 平台设计风格数据
 * 参考来源：https://github.com/VoltAgent/awesome-design-md
 */

export interface DesignStyle {
  /** 唯一标识 */
  key: string
  /** 英文名称 */
  name: string
  /** 中文名称 */
  label: string
  /** 分类 */
  category: string
  /** 风格简述（悬浮提示） */
  description: string
}

/** 设计风格分类 */
export const designStyleCategories = [
  { key: 'ai', label: 'AI & LLM 平台' },
  { key: 'devtools', label: '开发者工具' },
  { key: 'backend', label: '后端 & 数据库' },
  { key: 'saas', label: '效率 & SaaS' },
  { key: 'design', label: '设计 & 创意工具' },
  { key: 'fintech', label: '金融科技' },
  { key: 'ecommerce', label: '电商 & 零售' },
  { key: 'media', label: '媒体 & 消费科技' },
  { key: 'automotive', label: '汽车' },
]

/** 设计风格列表 */
export const designStyles: DesignStyle[] = [
  // AI & LLM 平台
  {
    key: 'claude',
    name: 'Claude',
    label: 'Claude（克劳德）',
    category: 'ai',
    description: '温暖赤陶色调点缀，干净的编辑式排版布局，给人以专业且亲和的感觉',
  },
  {
    key: 'cohere',
    name: 'Cohere',
    label: 'Cohere（凝聚）',
    category: 'ai',
    description: '鲜艳渐变色彩，数据密集的仪表盘风格，适合企业级 AI 产品',
  },
  {
    key: 'elevenlabs',
    name: 'ElevenLabs',
    label: 'ElevenLabs（十一实验室）',
    category: 'ai',
    description: '深色电影感界面，音频波形美学元素，科技感十足',
  },
  {
    key: 'minimax',
    name: 'Minimax',
    label: 'Minimax（极小极大）',
    category: 'ai',
    description: '大胆的深色界面搭配霓虹色点缀，视觉冲击力强',
  },
  {
    key: 'mistral',
    name: 'Mistral AI',
    label: 'Mistral AI（西北风）',
    category: 'ai',
    description: '法式工程极简主义，紫色调为主，优雅而克制',
  },
  {
    key: 'ollama',
    name: 'Ollama',
    label: 'Ollama',
    category: 'ai',
    description: '终端优先风格，单色极简设计，开发者友好',
  },
  {
    key: 'replicate',
    name: 'Replicate',
    label: 'Replicate（复现）',
    category: 'ai',
    description: '干净白色画布，代码优先的展示方式，简洁明了',
  },
  {
    key: 'runwayml',
    name: 'RunwayML',
    label: 'RunwayML（跑道）',
    category: 'ai',
    description: '电影感深色界面，富媒体布局，适合创意类 AI 产品',
  },
  {
    key: 'together-ai',
    name: 'Together AI',
    label: 'Together AI（协同）',
    category: 'ai',
    description: '技术蓝图风格设计，开源基础设施感，专业严谨',
  },
  {
    key: 'xai',
    name: 'xAI',
    label: 'xAI',
    category: 'ai',
    description: '极致黑白对比，未来主义极简风格，科幻感强烈',
  },

  // 开发者工具
  {
    key: 'cursor',
    name: 'Cursor',
    label: 'Cursor（光标）',
    category: 'devtools',
    description: '流畅深色界面搭配渐变色点缀，现代代码编辑器美学',
  },
  {
    key: 'expo',
    name: 'Expo',
    label: 'Expo（博览）',
    category: 'devtools',
    description: '深色主题，紧凑字间距，以代码为中心的展示风格',
  },
  {
    key: 'lovable',
    name: 'Lovable',
    label: 'Lovable（可爱）',
    category: 'devtools',
    description: '活泼渐变色彩，友好的开发者美学，轻松愉快',
  },
  {
    key: 'raycast',
    name: 'Raycast',
    label: 'Raycast（射线）',
    category: 'devtools',
    description: '精致深色外观，鲜艳渐变色点缀，效率工具美学',
  },
  {
    key: 'superhuman',
    name: 'Superhuman',
    label: 'Superhuman（超人）',
    category: 'devtools',
    description: '高端深色界面，键盘优先操作，紫色光晕效果',
  },
  {
    key: 'vercel',
    name: 'Vercel',
    label: 'Vercel',
    category: 'devtools',
    description: '黑白精确设计，Geist 字体，极致简约的前端部署平台风格',
  },
  {
    key: 'warp',
    name: 'Warp',
    label: 'Warp（曲速）',
    category: 'devtools',
    description: '深色 IDE 风格界面，块状命令交互，现代终端美学',
  },

  // 后端 & 数据库
  {
    key: 'clickhouse',
    name: 'ClickHouse',
    label: 'ClickHouse',
    category: 'backend',
    description: '黄色点缀的技术文档风格，数据库产品的专业感',
  },
  {
    key: 'hashicorp',
    name: 'HashiCorp',
    label: 'HashiCorp',
    category: 'backend',
    description: '企业级干净设计，黑白为主，基础设施自动化的严肃感',
  },
  {
    key: 'mongodb',
    name: 'MongoDB',
    label: 'MongoDB',
    category: 'backend',
    description: '绿叶品牌标识，开发者文档聚焦，清新自然',
  },
  {
    key: 'posthog',
    name: 'PostHog',
    label: 'PostHog（邮递刺猬）',
    category: 'backend',
    description: '活泼的刺猬品牌形象，开发者友好的深色界面，粉紫色点缀',
  },
  {
    key: 'supabase',
    name: 'Supabase',
    label: 'Supabase',
    category: 'backend',
    description: '深色翡翠绿主题，代码优先展示，开源 Firebase 替代品风格',
  },
  {
    key: 'sentry',
    name: 'Sentry',
    label: 'Sentry（哨兵）',
    category: 'backend',
    description: '深色仪表盘，数据密集布局，粉紫色点缀，错误监控专业感',
  },

  // 效率 & SaaS
  {
    key: 'linear',
    name: 'Linear',
    label: 'Linear（线性）',
    category: 'saas',
    description: '超级极简设计，精确到像素，紫色点缀，工程师项目管理工具的标杆',
  },
  {
    key: 'notion',
    name: 'Notion',
    label: 'Notion（概念）',
    category: 'saas',
    description: '温暖极简主义，衬线标题，柔和表面，全能工作空间的舒适感',
  },
  {
    key: 'figma',
    name: 'Figma',
    label: 'Figma',
    category: 'saas',
    description: '鲜艳多彩配色，活泼又专业，协作设计工具的代表风格',
  },
  {
    key: 'cal',
    name: 'Cal.com',
    label: 'Cal.com（日历）',
    category: 'saas',
    description: '干净中性界面，开发者导向的简约风格，开源日程工具',
  },
  {
    key: 'zapier',
    name: 'Zapier',
    label: 'Zapier（连接器）',
    category: 'saas',
    description: '温暖橙色调，友好的插画驱动设计，自动化平台的亲和力',
  },
  {
    key: 'mintlify',
    name: 'Mintlify',
    label: 'Mintlify（薄荷）',
    category: 'saas',
    description: '干净绿色点缀，阅读优化排版，文档平台的清爽感',
  },
  {
    key: 'resend',
    name: 'Resend',
    label: 'Resend（重发）',
    category: 'saas',
    description: '极简深色主题，等宽字体点缀，开发者邮件 API 的极客风',
  },

  // 设计 & 创意工具
  {
    key: 'framer',
    name: 'Framer',
    label: 'Framer（构建者）',
    category: 'design',
    description: '大胆黑蓝配色，动效优先，设计感前卫的网站构建器风格',
  },
  {
    key: 'miro',
    name: 'Miro',
    label: 'Miro（画板）',
    category: 'design',
    description: '明亮黄色点缀，无限画布美学，视觉协作的开放感',
  },
  {
    key: 'webflow',
    name: 'Webflow',
    label: 'Webflow（网流）',
    category: 'design',
    description: '蓝色点缀，精致的营销网站美学，可视化建站的专业感',
  },
  {
    key: 'clay',
    name: 'Clay',
    label: 'Clay（黏土）',
    category: 'design',
    description: '有机形状，柔和渐变，艺术指导式布局，创意机构的高级感',
  },

  // 金融科技
  {
    key: 'stripe',
    name: 'Stripe',
    label: 'Stripe（条纹）',
    category: 'fintech',
    description: '标志性紫色渐变，字重 300 的优雅排版，支付基础设施的高端感',
  },
  {
    key: 'coinbase',
    name: 'Coinbase',
    label: 'Coinbase（币库）',
    category: 'fintech',
    description: '干净蓝色标识，信任感导向，机构级加密货币平台风格',
  },
  {
    key: 'revolut',
    name: 'Revolut',
    label: 'Revolut（革新）',
    category: 'fintech',
    description: '流畅深色界面，渐变卡片效果，金融科技的精密感',
  },
  {
    key: 'wise',
    name: 'Wise',
    label: 'Wise（智汇）',
    category: 'fintech',
    description: '明亮绿色点缀，友好清晰的设计，国际汇款的透明感',
  },

  // 电商 & 零售
  {
    key: 'airbnb',
    name: 'Airbnb',
    label: 'Airbnb（爱彼迎）',
    category: 'ecommerce',
    description: '温暖珊瑚色点缀，摄影驱动，圆角界面，旅行平台的温馨感',
  },
  {
    key: 'nike',
    name: 'Nike',
    label: 'Nike（耐克）',
    category: 'ecommerce',
    description: '黑白单色界面，巨大的 Futura 大写字体，全幅摄影，运动品牌的力量感',
  },
  {
    key: 'shopify',
    name: 'Shopify',
    label: 'Shopify',
    category: 'ecommerce',
    description: '深色电影感优先，霓虹绿点缀，超轻展示字体，电商平台的现代感',
  },
  {
    key: 'starbucks',
    name: 'Starbucks',
    label: 'Starbucks（星巴克）',
    category: 'ecommerce',
    description: '四层大地绿色系统，温暖奶油色画布，咖啡品牌的自然亲和力',
  },

  // 媒体 & 消费科技
  {
    key: 'apple',
    name: 'Apple',
    label: 'Apple（苹果）',
    category: 'media',
    description: '高端留白设计，SF Pro 字体，电影级图像展示，消费电子的极致精致',
  },
  {
    key: 'spotify',
    name: 'Spotify',
    label: 'Spotify',
    category: 'media',
    description: '鲜艳绿色搭配深色背景，大胆字体，专辑封面驱动的音乐平台风格',
  },
  {
    key: 'spacex',
    name: 'SpaceX',
    label: 'SpaceX（太空探索）',
    category: 'media',
    description: '极致黑白对比，全幅太空图像，未来主义科技感',
  },
  {
    key: 'uber',
    name: 'Uber',
    label: 'Uber（优步）',
    category: 'media',
    description: '大胆黑白设计，紧凑字体排版，都市出行的力量感',
  },
  {
    key: 'pinterest',
    name: 'Pinterest',
    label: 'Pinterest（拼趣）',
    category: 'media',
    description: '红色点缀，瀑布流网格布局，图片优先的视觉发现平台风格',
  },

  // 汽车
  {
    key: 'tesla',
    name: 'Tesla',
    label: 'Tesla（特斯拉）',
    category: 'automotive',
    description: '极致减法设计，电影级全视口摄影，Universal Sans 字体，电动车的未来感',
  },
  {
    key: 'bmw',
    name: 'BMW',
    label: 'BMW（宝马）',
    category: 'automotive',
    description: '深色高端表面，精确的德国工程美学，豪华汽车的品质感',
  },
  {
    key: 'ferrari',
    name: 'Ferrari',
    label: 'Ferrari（法拉利）',
    category: 'automotive',
    description: '明暗对比的黑白编辑风格，法拉利红极度克制使用，超跑的稀缺感',
  },
  {
    key: 'lamborghini',
    name: 'Lamborghini',
    label: 'Lamborghini（兰博基尼）',
    category: 'automotive',
    description: '纯黑教堂式背景，金色点缀，定制新怪诞字体，超跑的奢华感',
  },
]

/**
 * 根据风格 key 获取风格的英文描述（用于注入 AI prompt）
 */
export function getDesignStylePrompt(styleKey: string): string {
  const style = designStyles.find((s) => s.key === styleKey)
  if (!style) return ''

  const styleDescriptions: Record<string, string> = {
    claude: 'Warm terracotta accent color, clean editorial layout with generous whitespace, professional yet approachable feel',
    cohere: 'Vibrant gradients, data-rich dashboard aesthetic, enterprise AI platform look',
    elevenlabs: 'Dark cinematic UI with audio-waveform visual elements, high-tech atmosphere',
    minimax: 'Bold dark interface with neon accent colors, strong visual impact',
    mistral: 'French-engineered minimalism with purple tones, elegant and restrained',
    ollama: 'Terminal-first monochrome simplicity, developer-friendly aesthetic',
    replicate: 'Clean white canvas with code-forward presentation, minimal and clear',
    runwayml: 'Cinematic dark UI with media-rich layout, creative AI product feel',
    'together-ai': 'Technical blueprint-style design, open-source infrastructure aesthetic',
    xai: 'Stark monochrome with futuristic minimalism, sci-fi inspired',
    cursor: 'Sleek dark interface with gradient accents, modern code editor aesthetic',
    expo: 'Dark theme with tight letter-spacing, code-centric presentation',
    lovable: 'Playful gradients with friendly developer aesthetic, cheerful vibe',
    raycast: 'Sleek dark chrome with vibrant gradient accents, productivity tool aesthetic',
    superhuman: 'Premium dark UI with keyboard-first design, purple glow effects',
    vercel: 'Black and white precision with Geist font, ultimate minimalist deployment platform style',
    warp: 'Dark IDE-like interface with block-based command UI, modern terminal aesthetic',
    clickhouse: 'Yellow-accented technical documentation style, professional database product feel',
    hashicorp: 'Enterprise-clean black and white design, serious infrastructure automation aesthetic',
    mongodb: 'Green leaf branding with developer documentation focus, fresh and natural',
    posthog: 'Playful hedgehog branding with developer-friendly dark UI, pink-purple accents',
    supabase: 'Dark emerald theme with code-first presentation, open-source Firebase alternative style',
    sentry: 'Dark dashboard with data-dense layout, pink-purple accent, error monitoring professional feel',
    linear: 'Ultra-minimal precise design with purple accent, benchmark for engineering project management tools',
    notion: 'Warm minimalism with serif headings and soft surfaces, comfortable all-in-one workspace feel',
    figma: 'Vibrant multi-color palette, playful yet professional, collaborative design tool style',
    cal: 'Clean neutral UI with developer-oriented simplicity, open-source scheduling tool',
    zapier: 'Warm orange palette with friendly illustration-driven design, automation platform approachability',
    mintlify: 'Clean green-accented reading-optimized typography, documentation platform freshness',
    resend: 'Minimal dark theme with monospace accents, developer email API geek style',
    framer: 'Bold black and blue with motion-first design-forward aesthetic, cutting-edge website builder',
    miro: 'Bright yellow accent with infinite canvas aesthetic, visual collaboration openness',
    webflow: 'Blue-accented polished marketing site aesthetic, visual web builder professionalism',
    clay: 'Organic shapes with soft gradients, art-directed layout, creative agency premium feel',
    stripe: 'Signature purple gradients with weight-300 elegant typography, payment infrastructure premium feel',
    coinbase: 'Clean blue identity with trust-focused institutional feel, crypto exchange reliability',
    revolut: 'Sleek dark interface with gradient cards, fintech precision and modernity',
    wise: 'Bright green accent with friendly clear design, international money transfer transparency',
    airbnb: 'Warm coral accent with photography-driven rounded UI, travel platform warmth',
    nike: 'Monochrome UI with massive uppercase Futura font, full-bleed photography, athletic brand power',
    shopify: 'Dark-first cinematic with neon green accent, ultra-light display type, modern e-commerce platform',
    starbucks: 'Four-tier earth-green system with warm cream canvas, coffee brand natural approachability',
    apple: 'Premium whitespace with SF Pro font, cinematic imagery, consumer electronics ultimate refinement',
    spotify: 'Vibrant green on dark background, bold typography, album-art-driven music platform style',
    spacex: 'Stark black and white with full-bleed space imagery, futuristic technology feel',
    uber: 'Bold black and white with tight typography, urban mobility energy',
    pinterest: 'Red accent with masonry grid layout, image-first visual discovery platform style',
    tesla: 'Radical subtraction design with cinematic full-viewport photography, electric vehicle futurism',
    bmw: 'Dark premium surfaces with precise German engineering aesthetic, luxury automotive quality',
    ferrari: 'Chiaroscuro black-white editorial with Ferrari Red used extremely sparingly, supercar exclusivity',
    lamborghini: 'True black cathedral background with gold accent, custom Neo-Grotesk typeface, supercar luxury',
  }

  return styleDescriptions[styleKey] || style.description
}
