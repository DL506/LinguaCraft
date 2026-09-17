<!-- 作文题目(任务书 10.11):题目 + 要点要求 + 词数要求 + 评分维度提示 -->
<template>
  <div class="writing-prompt">
    <h3 class="writing-prompt__title">写作题目</h3>
    <p class="writing-prompt__text">{{ prompt }}</p>

    <h4 class="writing-prompt__section">要点要求</h4>
    <ul class="writing-prompt__list">
      <li v-for="(req, index) in requirements" :key="index">{{ index + 1 }}. {{ req }}</li>
    </ul>

    <div class="writing-prompt__meta">
      <n-tag size="small" type="info" :bordered="false">词数要求 {{ wordLimit }} 词</n-tag>
      <n-tag v-for="dim in scoringDimensions" :key="dim" size="small" :bordered="false">{{ dim }}</n-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NTag } from 'naive-ui'

defineProps<{
  /** 写作题目与背景说明 */
  prompt: string
  /** 要点要求列表 */
  requirements: string[]
  /** 词数要求(词汇数) */
  wordLimit: number
  /** 评分维度(提交后批改按此维度出分) */
  scoringDimensions: string[]
}>()
</script>

<style scoped>
.writing-prompt {
  padding: 16px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  /* 断词兜底 + 横向兜底(clip 不创建滚动容器) */
  overflow-wrap: break-word;
  overflow-x: clip;
}

.writing-prompt__title {
  font-size: 15px;
  color: var(--lc-text-1);
  margin-bottom: 10px;
}

.writing-prompt__text {
  font-size: 14px;
  line-height: 1.9;
  color: var(--lc-text-1);
  margin-bottom: 16px;
}

.writing-prompt__section {
  font-size: 13px;
  color: var(--lc-text-2);
  margin-bottom: 8px;
}

.writing-prompt__list {
  margin-bottom: 16px;
}

.writing-prompt__list li {
  font-size: 13px;
  line-height: 1.9;
  color: var(--lc-text-2);
}

.writing-prompt__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>