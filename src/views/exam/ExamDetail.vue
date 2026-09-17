<!-- 模考整卷页(任务书 10.12):整卷答题 → 交卷 → 按 status 进入成绩报告 / 部分批改 / 待批改
     status 分支(10.12 表格):graded=完整报告;partially_graded=客观题先出+作文待批改;pending=占位页
     沉浸模式与练习页一致:默认不沉浸,头部按钮手动开启 -->
<template>
  <div class="exam-detail">
    <!-- 头部:← 退出 | 卷名 | 计时 | 沉浸切换 | 交卷 -->
    <div class="exam-detail__header">
      <n-button quaternary circle aria-label="退出" @click="onBack">
        <template #icon><n-icon :component="ArrowBackOutline" /></template>
      </n-button>
      <span class="exam-detail__title">{{ paper?.title ?? '真题模考' }}</span>
      <div class="exam-detail__right">
        <CountdownTimer v-if="state === 'answering'" :seconds="elapsed" />
        <n-button
          v-if="isImmersivePage"
          quaternary
          circle
          :title="isImmersive ? '退出沉浸' : '进入沉浸'"
          @click="toggleImmersive"
        >
          <template #icon>
            <n-icon :component="isImmersive ? ExpandOutline : ContractOutline" />
          </template>
        </n-button>
        <n-button
          v-if="state === 'answering'"
          type="primary"
          size="small"
          :loading="submitting"
          @click="onSubmit"
        >
          交卷
        </n-button>
      </div>
    </div>

    <!-- 加载态 -->
    <div v-if="state === 'loading'" class="exam-detail__loading">
      <n-spin size="large" />
    </div>

    <!-- 答题态:整卷答题组件 -->
    <ExamPaper v-else-if="state === 'answering' && paper" ref="paperRef" :paper="paper" />

    <!-- 待批改态(人工批改成整卷待批改) -->
    <PendingView
      v-else-if="state === 'pending'"
      :estimated-time="estimatedTime"
      :refreshing="pendingRefreshing"
      @back="onBack"
      @refresh="refreshPending"
    />

    <!-- 成绩报告态(graded / partially_graded) -->
    <div v-else-if="state === 'report' && report" class="exam-detail__report">
      <ExamReport :report="report" />
      <div class="exam-detail__report-actions">
        <n-button @click="router.push('/exam')">返回列表</n-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline, ContractOutline, ExpandOutline } from '@vicons/ionicons5'
import { NButton, NIcon, NSpin, useDialog, useMessage } from 'naive-ui'
import ExamPaper from '@/components/exam/ExamPaper.vue'
import ExamReport from '@/components/exam/ExamReport.vue'
import PendingView from '@/components/common/PendingView.vue'
import CountdownTimer from '@/components/common/CountdownTimer.vue'
import { getExamPaper, getExamReport, submitExam } from '@/api/exam'
import { useCountdown } from '@/composables/useCountdown'
import { useImmersive } from '@/composables/useImmersive'
import type { ExamPaper as ExamPaperData, ExamReport as ExamReportData } from '@/types/exam'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const { isImmersive, isImmersivePage, toggleImmersive } = useImmersive()

/** 计时:正向计时展示;10.12 未规定超时自动交卷,不自动提交 */
const { elapsed, start } = useCountdown(0)

const state = ref<'loading' | 'answering' | 'pending' | 'report'>('loading')
const paper = ref<ExamPaperData | null>(null)
const report = ref<ExamReportData | null>(null)
const paperRef = ref<InstanceType<typeof ExamPaper> | null>(null)
const submitting = ref(false)
const pendingRefreshing = ref(false)
const estimatedTime = ref('')
const pendingSubmissionId = ref('')

/** 试卷 id */
const examId = String(route.params.id ?? '')

onMounted(async () => {
  paper.value = await getExamPaper(examId)
  state.value = 'answering'
  start()
})

/** 交卷:未答提醒后确认(10.12) */
function onSubmit(): void {
  const unanswered = paperRef.value?.unansweredCount() ?? 0
  dialog.warning({
    title: '确认交卷',
    content: unanswered > 0 ? `还有 ${unanswered} 道客观题未作答,确认交卷吗?` : '确认交卷吗?',
    positiveText: '交卷',
    negativeText: '再检查一下',
    onPositiveClick: () => {
      doSubmit()
    },
  })
}

/** 执行交卷:按响应 status 分流 */
async function doSubmit(): Promise<void> {
  const payload = paperRef.value?.getPayload()
  if (!payload) return
  submitting.value = true
  try {
    const result = await submitExam(examId, {
      objective: payload.objective,
      writing: payload.writing,
      duration: elapsed.value,
    })
    if (result.status === 'pending') {
      // 整卷待批改(人工模式)→ 占位页
      pendingSubmissionId.value = result.submissionId
      estimatedTime.value = result.estimatedTime ?? '预计 24 小时内出结果'
      state.value = 'pending'
      return
    }
    // graded / partially_graded → 成绩报告(部分批改时作文区显示待批改,验收 29)
    report.value = await getExamReport(result.submissionId)
    state.value = 'report'
  } catch {
    // 错误提示由 request 层统一弹出
  } finally {
    submitting.value = false
  }
}

/** 刷新待批改状态:客观题先出分后转报告 */
async function refreshPending(): Promise<void> {
  if (!pendingSubmissionId.value) return
  pendingRefreshing.value = true
  try {
    const latest = await getExamReport(pendingSubmissionId.value)
    if (latest.objectiveGraded || latest.writingGraded) {
      report.value = latest
      state.value = 'report'
      message.success('已出结果')
    } else {
      message.info('仍在批改中,请稍后再查')
    }
  } finally {
    pendingRefreshing.value = false
  }
}

/** 退出:答题中先确认(作答不保存) */
function onBack(): void {
  if (state.value === 'answering') {
    dialog.warning({
      title: '退出模考',
      content: '退出后本次作答不会保存,确认退出吗?',
      positiveText: '退出',
      negativeText: '继续答题',
      onPositiveClick: () => {
        router.back()
      },
    })
    return
  }
  router.back()
}
</script>

<style scoped>
/* 页面自然流式布局:由布局层内容区统一滚动 */
.exam-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.exam-detail__header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
}

.exam-detail__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-1);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.exam-detail__right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.exam-detail__loading {
  display: flex;
  justify-content: center;
  padding: 64px 0;
}

.exam-detail__report {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 720px;
  min-width: 0;
}

.exam-detail__report-actions {
  display: flex;
  gap: 12px;
}
</style>