<!-- 语法内容(任务书 10.14):markdown-it 渲染章节内容,代码高亮交给 highlight.js
     双主题:两套 hljs CSS 在 index.html 中随主题切换(任务书 5.4,useHighlightTheme) -->
<template>
  <article class="grammar-content">
    <h2 class="grammar-content__title">{{ section.title }}</h2>
    <!-- 内容为受信 markdown(mock/后端契约提供)渲染结果 -->
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div class="grammar-content__body" v-html="html" />
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
// 使用 highlight.js 官方 common 子集(常用语言),避免全量语言包使分包体积膨胀
import hljs from 'highlight.js/lib/common'
import type { GrammarSection } from '@/types/grammar'

const props = defineProps<{
  /** 章节(GET /grammar/section/:id) */
  section: GrammarSection
}>()

/** HTML 转义(无语言代码块的纯文本兜底) */
function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

/** markdown-it 实例:代码块经 highlight.js 高亮(hljs 主题随全局双主题切换) */
const md = new MarkdownIt({
  html: false,
  linkify: false,
  highlight(code: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      return `<pre class="hljs"><code>${hljs.highlight(code, { language: lang }).value}</code></pre>`
    }
    return `<pre class="hljs"><code>${escapeHtml(code)}</code></pre>`
  },
})

/** 渲染后的 HTML */
const html = computed(() => md.render(props.section.markdown))
</script>

<style scoped>
.grammar-content {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  overflow-wrap: break-word;
  overflow-x: clip;
}

.grammar-content__title {
  font-size: 18px;
  color: var(--lc-text-1);
  margin-bottom: 12px;
}

/* markdown 正文排版 */
.grammar-content__body {
  font-size: 14px;
  line-height: 1.9;
  color: var(--lc-text-2);
}

.grammar-content__body :deep(h2) {
  font-size: 15px;
  color: var(--lc-text-1);
  margin: 16px 0 8px;
}

.grammar-content__body :deep(h3) {
  font-size: 14px;
  color: var(--lc-text-1);
  margin: 12px 0 6px;
}

.grammar-content__body :deep(p) {
  margin: 6px 0;
}

.grammar-content__body :deep(ul),
.grammar-content__body :deep(ol) {
  margin: 6px 0;
  padding-left: 20px;
}

.grammar-content__body :deep(ul) {
  list-style: disc;
}

.grammar-content__body :deep(ol) {
  list-style: decimal;
}

.grammar-content__body :deep(li) {
  margin: 2px 0;
}

.grammar-content__body :deep(strong) {
  color: var(--lc-text-1);
}

.grammar-content__body :deep(code) {
  padding: 1px 6px;
  border-radius: 6px;
  font-size: 13px;
  background-color: var(--lc-bg-hover);
}

.grammar-content__body :deep(pre) {
  margin: 10px 0;
  border-radius: var(--lc-radius);
  overflow-x: auto;
}

.grammar-content__body :deep(pre code) {
  display: block;
  padding: 12px;
  background-color: transparent;
}

.grammar-content__body :deep(hr) {
  border: none;
  border-top: 1px dashed var(--lc-border);
  margin: 14px 0;
}
</style>