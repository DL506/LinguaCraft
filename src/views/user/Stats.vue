<!-- 学习统计(任务书 10.15):4 统计卡 + 双图并排(得分趋势/来源正确率)+ 各题型得分率 + 待批改数量
     桌面双图并排;手机统计卡 2×2 + 图表纵向(ECharts 按需引入,亮暗色适配见 5.5) -->
<template>
  <div class="stats-page">
    <!-- 4 统计卡 -->
    <div class="stats-page__cards">
      <div class="stat-card">
        <p class="stat-card__label">累计学习时长</p>
        <p class="stat-card__value">{{ formatMinutes(stats?.totalStudyTime ?? 0) }}</p>
      </div>
      <div class="stat-card">
        <p class="stat-card__label">累计背单词</p>
        <p class="stat-card__value">{{ stats?.totalWords ?? 0 }} 词</p>
      </div>
      <div class="stat-card">
        <p class="stat-card__label">累计阅读</p>
        <p class="stat-card__value">{{ stats?.totalReading ?? 0 }} 篇</p>
      </div>
      <div class="stat-card">
        <p class="stat-card__label">总正确率</p>
        <p class="stat-card__value">{{ stats?.accuracy ?? 0 }}%</p>
      </div>
    </div>

    <!-- 待批改提示 -->
    <n-alert v-if="(stats?.pendingGradingCount ?? 0) > 0" type="warning">
      你有 {{ stats?.pendingGradingCount }} 份提交待批改,
      <router-link class="stats-page__link" to="/user/submissions?status=pending">去查看</router-link>
    </n-alert>

    <!-- 双图并排(桌面) / 纵向(手机) -->
    <div class="stats-page__charts" :class="{ 'is-mobile': isMobile }">
      <div class="chart-card">
        <p class="chart-card__title">各题型得分趋势</p>
        <div ref="trendEl" class="chart-card__canvas" />
      </div>
      <div class="chart-card">
        <p class="chart-card__title">各来源正确率</p>
        <div ref="sourceEl" class="chart-card__canvas" />
      </div>
    </div>

    <!-- 各题型得分率 -->
    <div class="stats-page__types">
      <p class="stats-page__section-title">各题型得分率</p>
      <ul class="type-list">
        <li v-for="row in typeRows" :key="row.key" class="type-list__item">
          <span class="type-list__label">{{ row.label }}</span>
          <n-progress
            class="type-list__bar"
            type="line"
            :percentage="row.value"
            :show-indicator="false"
            :height="8"
          />
          <span class="type-list__value">{{ row.value }}%</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /user/stats(12.7);ECharts 按需引入(line/bar),暗色适配按任务书 5.5
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { NAlert, NProgress } from 'naive-ui'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getUserStats } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { useAppStore } from '@/stores/app'
import { formatMinutes } from '@/utils/format'
import type { UserStats } from '@/types/user'

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const { isMobile } = useDevice()
const appStore = useAppStore()

const stats = ref<UserStats | null>(null)
const trendEl = ref<HTMLElement | null>(null)
const sourceEl = ref<HTMLElement | null>(null)

let trendChart: echarts.ECharts | null = null
let sourceChart: echarts.ECharts | null = null

/** 题型得分率行(键与 UserStats.typeAccuracy 对应) */
const typeRows = computed(() => {
  const acc = stats.value?.typeAccuracy
  const ordered: { key: keyof NonNullable<UserStats['typeAccuracy']>; label: string }[] = [
    { key: 'reading', label: '阅读理解' },
    { key: 'matching', label: '五选五' },
    { key: 'cloze', label: '完形填空' },
    { key: 'grammar', label: '语法填空' },
    { key: 'writing', label: '作文' },
  ]
  return ordered.map((row) => ({ ...row, value: acc?.[row.key] ?? 0 }))
})

/** 题型系列配色(亮暗通用) */
const seriesColors = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6']

/** 亮暗主题下的图表基础色(5.5) */
function chartBase(): { text: string; axis: string; split: string } {
  return appStore.isDark
    ? { text: '#CBD5E1', axis: '#1E293B', split: '#1E293B' }
    : { text: '#475569', axis: '#E2E8F0', split: '#F1F5F9' }
}

/** 渲染得分趋势折线(5 题型按天) */
function renderTrend(): void {
  if (!trendEl.value || !stats.value) return
  trendChart ??= echarts.init(trendEl.value)
  const base = chartBase()
  const dates = stats.value.scoreTrend.map((t) => t.date)
  trendChart.setOption({
    textStyle: { color: base.text, fontSize: 12 },
    grid: { left: 40, right: 12, top: 32, bottom: 24 },
    tooltip: { trigger: 'axis' },
    legend: { top: 0, textStyle: { color: base.text, fontSize: 11 } },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: base.axis } }, axisLabel: { color: base.text } },
    yAxis: {
      type: 'value',
      max: 100,
      splitLine: { lineStyle: { color: base.split } },
      axisLabel: { color: base.text },
    },
    series: [
      { name: '阅读', type: 'line', smooth: true, data: stats.value.scoreTrend.map((t) => t.reading), itemStyle: { color: seriesColors[0] } },
      { name: '五选五', type: 'line', smooth: true, data: stats.value.scoreTrend.map((t) => t.matching), itemStyle: { color: seriesColors[1] } },
      { name: '完形', type: 'line', smooth: true, data: stats.value.scoreTrend.map((t) => t.cloze), itemStyle: { color: seriesColors[2] } },
      { name: '语法', type: 'line', smooth: true, data: stats.value.scoreTrend.map((t) => t.grammar), itemStyle: { color: seriesColors[3] } },
      { name: '作文', type: 'line', smooth: true, data: stats.value.scoreTrend.map((t) => t.writing), itemStyle: { color: seriesColors[4] } },
    ],
  })
}

/** 渲染来源正确率柱状(ai 为 -1 时视为无数据不展示) */
function renderSource(): void {
  if (!sourceEl.value || !stats.value) return
  sourceChart ??= echarts.init(sourceEl.value)
  const base = chartBase()
  const src = stats.value.sourceAccuracy
  const items = [
    { name: '真题', value: src.real },
    { name: '模拟题', value: src.teacher },
    ...(src.ai >= 0 ? [{ name: 'AI出题', value: src.ai }] : []),
  ]
  sourceChart.setOption({
    textStyle: { color: base.text, fontSize: 12 },
    grid: { left: 40, right: 12, top: 24, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: items.map((i) => i.name), axisLine: { lineStyle: { color: base.axis } }, axisLabel: { color: base.text } },
    yAxis: {
      type: 'value',
      max: 100,
      splitLine: { lineStyle: { color: base.split } },
      axisLabel: { color: base.text },
    },
    series: [
      {
        type: 'bar',
        barWidth: 28,
        data: items.map((i, index) => ({ value: i.value, itemStyle: { color: seriesColors[index % seriesColors.length] } })),
      },
    ],
  })
}

/** 尺寸自适应 */
function onResize(): void {
  trendChart?.resize()
  sourceChart?.resize()
}

/** 数据或主题变化时重渲染 */
watch([stats, () => appStore.isDark], () => {
  renderTrend()
  renderSource()
})

onMounted(async () => {
  stats.value = await getUserStats()
  renderTrend()
  renderSource()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  sourceChart?.dispose()
})
</script>

<style scoped>
.stats-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

/* 4 统计卡:桌面 4 列,手机 2×2 */
.stats-page__cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 767px) {
  .stats-page__cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.stat-card {
  padding: 14px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.stat-card__label {
  font-size: 12px;
  color: var(--lc-text-3);
  margin-bottom: 6px;
}

.stat-card__value {
  font-size: 20px;
  font-weight: 700;
  color: var(--lc-text-1);
  overflow-wrap: break-word;
}

.stats-page__link {
  color: var(--lc-primary);
}

/* 双图:桌面并排;手机纵向 */
.stats-page__charts {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
}

.stats-page__charts.is-mobile {
  grid-template-columns: minmax(0, 1fr);
}

.chart-card {
  padding: 14px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  min-width: 0;
}

.chart-card__title {
  font-size: 13px;
  color: var(--lc-text-2);
  margin-bottom: 8px;
}

.chart-card__canvas {
  width: 100%;
  height: 220px;
}

.stats-page__types {
  padding: 14px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.stats-page__section-title {
  font-size: 13px;
  color: var(--lc-text-2);
  margin-bottom: 10px;
}

.type-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.type-list__item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.type-list__label {
  width: 68px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--lc-text-2);
}

.type-list__bar {
  flex: 1;
}

.type-list__value {
  width: 44px;
  text-align: right;
  font-size: 13px;
  color: var(--lc-text-2);
}
</style>