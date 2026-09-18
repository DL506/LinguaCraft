<!-- 单词卡(任务书 10.13):拼写 + 音标 + 发音按钮;释义/例句/真题原句
     桌面直接展示释义;手机默认折叠,上滑或点击展开(由查看态 revealed 控制) -->
<template>
  <div class="word-card" @click="!revealed && $emit('reveal')">
    <h2 class="word-card__spelling">{{ word.spelling }}</h2>
    <div class="word-card__phonetic-row">
      <span class="word-card__phonetic">{{ word.phonetic }}</span>
      <n-button quaternary circle size="small" aria-label="播放发音" @click.stop="speak">
        <template #icon><n-icon :component="VolumeHighOutline" /></template>
      </n-button>
    </div>

    <!-- 释义区:桌面可见;手机上滑/点击展开(10.13) -->
    <div v-if="revealed" class="word-card__detail">
      <p class="word-card__pos">{{ word.partOfSpeech }}</p>
      <p class="word-card__meaning">{{ word.meanings.join(';') }}</p>

      <template v-if="word.exampleSentence">
        <p class="word-card__example">{{ word.exampleSentence }}</p>
        <p v-if="word.exampleTranslation" class="word-card__translation">{{ word.exampleTranslation }}</p>
      </template>

      <!-- 真题例句(10.13:展示该词在历年真题中的原句) -->
      <div v-if="word.realExamSentence" class="word-card__real">
        <p class="word-card__real-label">真题例句<span v-if="word.realExamSource">({{ word.realExamSource }})</span></p>
        <p class="word-card__real-text">{{ word.realExamSentence }}</p>
      </div>
    </div>
    <p v-else class="word-card__hint">上滑或点击卡片查看释义</p>
  </div>
</template>

<script setup lang="ts">
import { NButton, NIcon } from 'naive-ui'
import { VolumeHighOutline } from '@vicons/ionicons5'
import type { Word } from '@/types/vocab'

const props = defineProps<{
  /** 当前单词 */
  word: Word
  /** 释义是否已展开(桌面恒 true;手机由手势/点击切换) */
  revealed: boolean
}>()

defineEmits<{ reveal: [] }>()

/** 播放发音:优先 audioUrl(后端提供);缺失时用浏览器语音合成兜底朗读 */
function speak(): void {
  if (props.word.audioUrl) {
    void new Audio(props.word.audioUrl).play()
    return
  }
  if (typeof speechSynthesis !== 'undefined') {
    const utterance = new SpeechSynthesisUtterance(props.word.spelling)
    utterance.lang = 'en-US'
    speechSynthesis.speak(utterance)
  }
}
</script>

<style scoped>
.word-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 32px 24px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  text-align: center;
  overflow-wrap: break-word;
}

.word-card__spelling {
  font-size: 32px;
  font-weight: 700;
  color: var(--lc-text-1);
  line-height: 1.3;
}

.word-card__phonetic-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.word-card__phonetic {
  font-size: 15px;
  color: var(--lc-text-3);
}

.word-card__detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 520px;
}

.word-card__pos {
  font-size: 14px;
  color: var(--lc-primary);
}

.word-card__meaning {
  font-size: 16px;
  color: var(--lc-text-1);
}

.word-card__example {
  font-size: 14px;
  color: var(--lc-text-2);
  margin-top: 8px;
}

.word-card__translation {
  font-size: 13px;
  color: var(--lc-text-3);
}

.word-card__real {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: var(--lc-radius);
  background-color: var(--lc-bg-hover);
}

.word-card__real-label {
  font-size: 12px;
  color: var(--lc-text-3);
  margin-bottom: 4px;
}

.word-card__real-text {
  font-size: 13px;
  color: var(--lc-text-2);
}

.word-card__hint {
  font-size: 13px;
  color: var(--lc-text-3);
}

/* 手机全屏形态(10.13):由父级 .is-mobile 尺寸控制 */
</style>