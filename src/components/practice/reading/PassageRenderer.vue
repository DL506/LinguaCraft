<!-- 文章渲染(任务书 10.7):按词渲染,点词查词(桌面浮动卡/手机底部抽屉);字号由 props 控制;
     桌面另支持选中句子收藏(mouseup 后出现「收藏该句」按钮);护眼配色见任务书 5.7 特殊色 -->
<template>
  <div
    ref="rootEl"
    class="passage-renderer"
    :style="{ fontSize: `${fontSize}px` }"
    :class="{ 'is-eye-protect': eyeProtect }"
    @mouseup="onMouseUp"
  >
    <template v-for="(word, index) in words" :key="index">
      <span class="passage-renderer__word" @click="onWordClick(word, $event)">{{ word }}</span><span> </span>
    </template>

    <!-- 桌面点词查词浮动卡 -->
    <div
      v-if="!isMobile && query"
      class="word-pop"
      :style="{ left: `${query.x}px`, top: `${query.y + 8}px` }"
      @click.stop
    >
      <strong>{{ query.word }}</strong>
      <template v-if="query.result">
        <p class="word-pop__phonetic">{{ query.result.phonetic }}</p>
        <p class="word-pop__meaning">{{ query.result.meanings.join(';') }}</p>
      </template>
      <p v-else class="word-pop__missing">演示词库未收录该词</p>
    </div>

    <!-- 桌面选中句子收藏按钮 -->
    <div
      v-if="!isMobile && selectionText"
      class="fav-btn"
      :style="favStyle"
      @mousedown.prevent
      @click="onFavClick"
    >
      收藏该句
    </div>

    <!-- 手机点词查词:底部抽屉(任务书 10.7:手机底部 n-drawer) -->
    <n-drawer v-model:show="mobileDrawerShow" placement="bottom" display-directive="show">
      <div class="word-drawer">
        <strong class="word-drawer__word">{{ query?.word }}</strong>
        <template v-if="query?.result">
          <p>{{ query.result.phonetic }}</p>
          <p>{{ query.result.meanings.join(';') }}</p>
          <p v-if="query.result.realExamSentence" class="word-drawer__real">
            真题:{{ query.result.realExamSentence }}
          </p>
        </template>
        <p v-else>演示词库未收录该词</p>
      </div>
    </n-drawer>

    <!-- 点击空白处关闭浮卡 -->
    <div v-if="!isMobile && query" class="word-pop__mask" @click="query = null" />
  </div>
</template>

<script setup lang="ts">
// 查词数据:mock 阶段直接匹配内置演示词表(真实词典服务由后端契约提供)
import { computed, ref } from 'vue'
import { NDrawer } from 'naive-ui'
import { wordStore } from '@/mocks/data/words'
import { useDevice } from '@/composables/useDevice'
import type { Word } from '@/types/vocab'

const props = defineProps<{
  /** 文章原文 */
  passage: string
  /** 正文字号 px(父级工具栏 [Aa] 控制) */
  fontSize: number
  /** 护眼模式(任务书 5.7 特殊配色) */
  eyeProtect: boolean
}>()

const emit = defineEmits<{
  /** 收藏选中句子(父级调 POST /user/favorites) */
  'favorite-sentence': [text: string]
}>()

const { isMobile } = useDevice()
const rootEl = ref<HTMLElement | null>(null)

/** 英文分词:按空白拆分,标点随词尾(查词时剥离) */
const words = computed(() => props.passage.split(/\s+/).filter(Boolean))

/** 查词状态:word 命中演示词表则带 result */
const query = ref<{ word: string; x: number; y: number; result?: Word } | null>(null)
/** 手机查词抽屉开关 */
const mobileDrawerShow = ref(false)

/** 在演示词表中查找单词(剥离标点、忽略大小写) */
function lookup(raw: string): Word | undefined {
  const clean = raw.replace(/[^a-zA-Z'-]/g, '').toLowerCase()
  if (!clean) return undefined
  return wordStore.find((w) => w.spelling.toLowerCase() === clean)
}

/** 点词查词:桌面浮动卡,手机底部抽屉 */
function onWordClick(raw: string, event: MouseEvent): void {
  const result = lookup(raw)
  if (isMobile.value) {
    query.value = { word: raw.replace(/[^a-zA-Z'-]/g, ''), x: 0, y: 0, result }
    mobileDrawerShow.value = true
  } else {
    query.value = { word: raw, x: event.clientX, y: event.clientY, result }
  }
}

/** 选中句子收藏(桌面):选区位于文章内时显示浮动按钮 */
const selectionText = ref('')
const favStyle = ref<Record<string, string>>({})

function onMouseUp(): void {
  if (isMobile.value) return
  const sel = window.getSelection()
  if (!sel || sel.isCollapsed) {
    selectionText.value = ''
    return
  }
  const text = sel.toString().trim()
  if (!text) {
    selectionText.value = ''
    return
  }
  const range = sel.getRangeAt(0)
  const inside = rootEl.value?.contains(range.commonAncestorContainer) ?? false
  if (!inside) {
    selectionText.value = ''
    return
  }
  const rect = range.getBoundingClientRect()
  selectionText.value = text
  favStyle.value = { left: `${rect.left}px`, top: `${rect.bottom + 6}px` }
}

/** 点击收藏按钮:上抛句子,清空选区 */
function onFavClick(): void {
  emit('favorite-sentence', selectionText.value)
  selectionText.value = ''
  window.getSelection()?.removeAllRanges()
}
</script>

<style scoped>
.passage-renderer {
  position: relative;
  line-height: 2;
  color: var(--lc-text-1);
}

.passage-renderer.is-eye-protect {
  /* 护眼模式:亮色米黄 / 暗色深褐(任务书 5.7 特殊页面配色) */
  background-color: #faf6e9;
  padding: 12px;
  border-radius: var(--lc-radius);
}

.passage-renderer__word {
  cursor: pointer;
  border-radius: 4px;
}

.passage-renderer__word:hover {
  background-color: var(--lc-bg-hover);
}

.word-pop {
  position: fixed;
  z-index: 2000;
  max-width: 260px;
  padding: 10px 12px;
  border-radius: 8px;
  background-color: var(--lc-bg-elevated);
  box-shadow: var(--lc-shadow);
  font-size: 13px;
}

.word-pop__phonetic {
  color: var(--lc-text-3);
}

.word-pop__meaning {
  color: var(--lc-text-1);
  margin-top: 4px;
}

.word-pop__missing {
  color: var(--lc-text-3);
  margin-top: 4px;
}

.fav-btn {
  position: fixed;
  z-index: 2000;
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 6px;
  color: #fff;
  background-color: var(--lc-primary);
  cursor: pointer;
}

.word-drawer {
  padding: 16px;
  color: var(--lc-text-1);
}

.word-drawer__word {
  font-size: 16px;
}

.word-drawer__real {
  margin-top: 8px;
  color: var(--lc-text-3);
  font-size: 12px;
}

.word-pop__mask {
  position: fixed;
  inset: 0;
  z-index: 1999;
}
</style>