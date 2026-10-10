<!-- 成绩报告(任务书 10.12):总分 + 各题型得分 + 薄弱项 + 历年对比;作文待批改时给出提示
     partially_graded 时客观题得分先出,作文区显示"待批改"(验收 29) -->
<template>
  <div class="exam-report">
    <!-- 总分 -->
    <n-card class="exam-report__score" :bordered="false">
      <div class="score">
        <n-progress
          type="circle"
          :percentage="Math.round((report.score / report.totalScore) * 100)"
          :stroke-width="8"
          :show-indicator="false"
          style="width: 96px"
        />
        <div class="score__detail">
          <p class="score__value">{{ report.score }} <span class="score__total">/ {{ report.totalScore }}</span></p>
          <p class="score__label">总分</p>
          <n-tag v-if="!report.writingGraded" type="warning" size="small" :bordered="false">作文待批改</n-tag>
        </div>
      </div>
    </n-card>

    <!-- 各题型得分 -->
    <n-card :bordered="false" title="各题型得分">
      <ul class="sections">
        <li v-for="section in report.sections" :key="section.part" class="sections__item">
          <span class="sections__name">{{ partName(section.type) }}</span>
          <n-progress
            class="sections__bar"
            type="line"
            :percentage="section.total > 0 ? Math.round((section.score / section.total) * 100) : 0"
            :show-indicator="false"
            :height="8"
          />
          <span class="sections__score">{{ section.score }} / {{ section.total }}</span>
        </li>
      </ul>
    </n-card>

    <!-- 作文批改详情(写作已出分时才有;此前后端不下发,学生只看到一个分数) -->
    <n-card v-if="report.writingGraded" :bordered="false" title="作文批改">
      <p class="writing-score">得分 {{ report.writingScore }}</p>
      <ul v-if="report.writingDimensions?.length" class="writing-dims">
        <li v-for="dim in report.writingDimensions" :key="dim.name" class="writing-dims__item">
          <span class="writing-dims__name">{{ dim.name }}</span>
          <span class="writing-dims__score">{{ dim.score }}</span>
          <p v-if="dim.comment" class="writing-dims__comment">{{ dim.comment }}</p>
        </li>
      </ul>
      <p v-if="report.writingOverallComment" class="writing-comment">
        <b>总评:</b>{{ report.writingOverallComment }}
      </p>
      <details v-if="report.writingSampleAnswer" class="writing-sample">
        <summary>参考范文</summary>
        <p>{{ report.writingSampleAnswer }}</p>
      </details>
    </n-card>

    <!-- 薄弱项 -->
    <n-card :bordered="false" title="薄弱项">
      <ul class="weak-points">
        <li v-for="(point, index) in report.weakPoints" :key="index">
          <n-icon class="weak-points__icon" :component="Warning" /> {{ point }}
        </li>
      </ul>
    </n-card>

    <!-- 历年对比 -->
    <n-card :bordered="false" title="历年成绩对比">
      <ul class="history">
        <li v-for="item in report.compareWithHistory" :key="item.year" class="history__item">
          <span class="history__year">{{ item.year }}</span>
          <n-progress
            class="history__bar"
            type="line"
            :percentage="Math.round((item.score / report.totalScore) * 100)"
            :show-indicator="false"
            :height="8"
          />
          <span class="history__score">{{ item.score }}</span>
        </li>
      </ul>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { NCard, NIcon, NProgress, NTag } from 'naive-ui'
import { Warning } from '@vicons/ionicons5'
import type { ExamReport } from '@/types/exam'
import type { PracticeType } from '@/types/practice'

defineProps<{
  /** 报告数据(GET /exam/submission/:submissionId/report) */
  report: ExamReport
}>()

/** 部分名称 */
function partName(type: PracticeType): string {
  const map: Record<PracticeType, string> = {
    reading: '阅读理解',
    matching: '五选五',
    cloze: '完形填空',
    'grammar-fill': '语法填空',
    writing: '作文',
  }
  return map[type]
}
</script>

<style scoped>
.exam-report {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.exam-report__score :deep(.n-card__content) {
  padding: 16px;
}

.score {
  display: flex;
  align-items: center;
  gap: 20px;
}

.score__value {
  font-size: 32px;
  font-weight: 700;
  color: var(--lc-primary);
  line-height: 1.2;
}

.score__total {
  font-size: 16px;
  color: var(--lc-text-3);
  font-weight: 400;
}

.score__label {
  font-size: 13px;
  color: var(--lc-text-3);
  margin-bottom: 6px;
}

.sections {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sections__item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sections__name {
  width: 68px;
  flex-shrink: 0;
  color: var(--lc-text-2);
  font-size: 13px;
}

.sections__bar {
  flex: 1;
}

.sections__score {
  width: 64px;
  text-align: right;
  font-size: 13px;
  color: var(--lc-text-2);
  font-variant-numeric: tabular-nums;
}

.weak-points {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.weak-points li {
  font-size: 13px;
  color: var(--lc-text-2);
  overflow-wrap: break-word;
}

/* 薄弱项警示图标(主题警告色,亮暗自适应) */
.weak-points__icon {
  font-size: 14px;
  vertical-align: -2px;
  margin-right: 2px;
  color: var(--lc-warning);
}

.history {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history__item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.history__year {
  width: 48px;
  color: var(--lc-text-2);
  font-size: 13px;
}

.history__bar {
  flex: 1;
}

.history__score {
  width: 40px;
  text-align: right;
  font-size: 13px;
  color: var(--lc-text-2);
  font-variant-numeric: tabular-nums;
}

.writing-score {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
}

.writing-dims {
  list-style: none;
  margin: 0;
  padding: 0;
}

.writing-dims__item {
  padding: 8px 0;
  border-bottom: 1px solid var(--lc-border, rgba(128, 128, 128, 0.2));
}

.writing-dims__name {
  font-size: 13px;
  font-weight: 600;
  margin-right: 8px;
}

.writing-dims__score {
  font-size: 13px;
  color: var(--lc-primary, #2080f0);
}

.writing-dims__comment {
  margin: 4px 0 0;
  font-size: 13px;
  line-height: 1.7;
  opacity: 0.86;
}

.writing-comment {
  margin: 12px 0 0;
  font-size: 13px;
  line-height: 1.7;
}

.writing-sample {
  margin-top: 10px;
  font-size: 13px;
}

.writing-sample p {
  margin: 6px 0 0;
  line-height: 1.7;
  white-space: pre-wrap;
}
</style>