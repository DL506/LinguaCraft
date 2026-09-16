// 导航配置(任务书 6.3):一级导航 + 二级菜单主体,图标为 ionicons5 组件映射
import type { Component } from 'vue'
import {
  BookOutline,
  CreateOutline,
  DocumentTextOutline,
  ExtensionPuzzleOutline,
  HomeOutline,
  PencilOutline,
  PersonOutline,
  ReaderOutline,
  SchoolOutline,
  TrophyOutline,
} from '@vicons/ionicons5'

/** 图标名 → 组件映射(nav.ts 供各导航组件渲染用) */
export const navIconMap: Record<string, Component> = {
  HomeOutline,
  CreateOutline,
  DocumentTextOutline,
  ExtensionPuzzleOutline,
  ReaderOutline,
  PencilOutline,
  TrophyOutline,
  BookOutline,
  SchoolOutline,
  PersonOutline,
}

/** 二级菜单项 */
export interface NavChild {
  key: string
  label: string
  icon?: string
}

/** 一级菜单项 */
export interface NavItem {
  key: string
  label: string
  icon: string
  children: NavChild[]
}

/** 导航配置:key 与 route.meta.section 对应 */
export const navConfig: NavItem[] = [
  {
    key: 'practice',
    label: '刷题',
    icon: 'CreateOutline',
    children: [
      { key: '/practice/reading', label: '阅读理解', icon: 'DocumentTextOutline' },
      { key: '/practice/matching', label: '五选五', icon: 'ExtensionPuzzleOutline' },
      { key: '/practice/cloze', label: '完形填空', icon: 'ReaderOutline' },
      { key: '/practice/grammar-fill', label: '语法填空', icon: 'CreateOutline' },
      { key: '/practice/writing', label: '作文', icon: 'PencilOutline' },
      { key: '/exam', label: '真题模考', icon: 'TrophyOutline' },
    ],
  },
  {
    key: 'learn',
    label: '学习',
    icon: 'BookOutline',
    children: [
      { key: '/learn/vocab', label: '背单词', icon: 'BookOutline' },
      { key: '/learn/grammar', label: '学语法', icon: 'SchoolOutline' },
    ],
  },
  {
    key: 'user',
    label: '我的',
    icon: 'PersonOutline',
    children: [
      { key: '/user/stats', label: '学习统计' },
      { key: '/user/submissions', label: '我的提交' },
      { key: '/user/books', label: '我的词书' },
      { key: '/user/mistakes', label: '错题本' },
      { key: '/user/favorites', label: '收藏' },
      { key: '/user/settings', label: '设置' },
    ],
  },
]

/** 手机底部 Tab:首页 + 三个一级导航(任务书 6.2:手机底部一级 Tab) */
export const mobileTabs = [
  { key: 'home', label: '首页', path: '/dashboard', icon: 'HomeOutline' },
  { key: 'practice', label: '刷题', path: '/practice/reading', icon: 'CreateOutline' },
  { key: 'learn', label: '学习', path: '/learn/vocab', icon: 'BookOutline' },
  { key: 'user', label: '我的', path: '/user/stats', icon: 'PersonOutline' },
]

/** 根据 section 取对应一级菜单的子项(二级导航复用) */
export function childrenOfSection(section: string | undefined) {
  const parent = navConfig.find((item) => item.key === section)
  return parent?.children ?? []
}