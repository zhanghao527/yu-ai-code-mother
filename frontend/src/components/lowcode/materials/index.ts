/**
 * 物料组件注册表
 * 将组件 type 映射到对应的 Vue 组件
 */
import { defineAsyncComponent, type Component } from 'vue'

import MaterialNavbar from './MaterialNavbar.vue'
import MaterialHero from './MaterialHero.vue'
import MaterialHeading from './MaterialHeading.vue'
import MaterialParagraph from './MaterialParagraph.vue'
import MaterialImage from './MaterialImage.vue'
import MaterialButton from './MaterialButton.vue'
import MaterialDivider from './MaterialDivider.vue'
import MaterialContainer from './MaterialContainer.vue'
import MaterialColumns from './MaterialColumns.vue'
import MaterialCard from './MaterialCard.vue'
import MaterialFeatureGrid from './MaterialFeatureGrid.vue'
import MaterialCarousel from './MaterialCarousel.vue'
import MaterialFooter from './MaterialFooter.vue'

/**
 * 组件类型到 Vue 组件的映射
 */
export const materialRegistry: Record<string, Component> = {
  navbar: MaterialNavbar,
  hero: MaterialHero,
  heading: MaterialHeading,
  paragraph: MaterialParagraph,
  image: MaterialImage,
  button: MaterialButton,
  divider: MaterialDivider,
  container: MaterialContainer,
  columns: MaterialColumns,
  card: MaterialCard,
  'feature-grid': MaterialFeatureGrid,
  carousel: MaterialCarousel,
  footer: MaterialFooter,
}

/**
 * 根据组件类型获取对应的 Vue 组件
 */
export function getMaterialComponent(type: string): Component | null {
  return materialRegistry[type] || null
}

/**
 * 检查组件类型是否支持子组件
 */
export function canContainChildren(type: string): boolean {
  return ['container', 'columns'].includes(type)
}
