<!-- 首页(任务书 10.5):问候 + 备考倒计时 + 待批改提示 + 今日任务 + 各题型得分率 + 快捷入口
     桌面两列布局,手机单列(顺序:问候→倒计时→待批改→今日任务→得分率→快捷入口) -->
<template>
  <div class="dashboard">
    <!-- 问候行:时段问候 + 昵称;右侧主题切换(10.5 桌面图) -->
    <div class="dashboard__greeting">
      <h1 class="dashboard__hello">{{ greeting }}，{{ nickname }}</h1>
      <ThemeToggle v-if="!isMobile" />
    </div>

    <!-- 备考倒计时 + 进度 -->
    <n-card class="dashboard__countdown" :bordered="false">
      <div class="countdown">
        <div class="countdown__days">
          <span class="countdown__number">{{ countdown.daysLeft }}</span>
          <span class="countdown__unit">天</span>
        </div>
        <div class="countdown__info">
          <p class="countdown__text">距广东专插本考试还有 {{ countdown.daysLeft }} 天</p>
          <n-progress type="line" :percentage="countdown.progress" :show-indicator="false" :height="8" />
          <p class="countdown__sub">已备考 {{ countdown.progress }}%</p>
        </div>
      </div>
    </n-card>

    <!-- 待批改提示:数量 > 0 时显示(任务书 10.5/验收 31) -->
    <n-card v-if="pendingCount > 0" class="dashboard__pending" :bordered="false">
      <div class="pending">
        <n-icon :component="TimeOutline" size="18" class="pending__icon" />
        <span class="pending__text">你有 {{ pendingCount }} 份提交待批改</span>
        <router-link class="pending__link" to="/user/submissions?status=pending">查看</router-link>
      </div>
    </n-card>

    <div class="dashboard__grid" :class="{ 'is-mobile': isMobile }">
      <!-- 今日任务 -->
      <n-card class="dashboard__tasks" :bordered="false" title="今日任务">
        <ul class="tasks">
          <li v-for="item in todayTasks" :key="item.type" class="tasks__item">
            <span class="tasks__icon" :class="{ 'is-done': item.done >= item.target }">
              <n-icon :component="item.done >= item.target ? CheckmarkCircle : EllipseOutline" />
            </span>
            <span class="tasks__label">{{ taskLabel(item.type) }}</span>
            <span class="tasks__progress">{{ item.done }}/{{ item.target }}</span>
          </li>
        </ul>
        <n-button
          type="primary"
          block
          :disabled="!nextTask"
          :loading="smartLoading"
          @click="startToday"
        >
          {{ nextTask ? '开始今日学习 →' : '今日任务已全部完成' }}
        </n-button>
      </n-card>

      <!-- 各题型得分率 -->
      <n-card class="dashboard__accuracy" :bordered="false" title="各题型得分率">
        <ul class="accuracy">
          <li v-for="row in accuracyRows" :key="row.key" class="accuracy__item">
            <span class="accuracy__label">{{ row.label }}</span>
            <n-progress
              class="accuracy__bar"
              type="line"
              :percentage="row.value"
              :show-indicator="false"
              :height="8"
            />
            <span class="accuracy__value">{{ row.value }}%</span>
          </li>
        </ul>
      </n-card>
    </div>

    <!-- 快捷入口(10.5) -->
    <div class="dashboard__shortcuts">
      <n-button v-for="entry in shortcuts" :key="entry.path" :loading="entry.key === 'smart' && smartLoading" @click="onShortcut(entry)">
        {{ entry.label }}
      </n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
// 数据来源:/home/today-tasks + /home/exam-countdown + /user/stats + /user/profile(昵称)
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CheckmarkCircle, EllipseOutline, TimeOutline } from '@vicons/ionicons5'
import { NButton, NCard, NIcon, NProgress } from 'naive-ui'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import { getExamCountdown, getTodayTasks, smartPractice } from '@/api/home'
import { getUserProfile, getUserStats } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { usePracticeStore } from '@/stores/practice'
import type { ExamCountdown, HomeTodayTasks } from '@/types/api'
import type { UserProfile } from '@/types/user'

const router = useRouter()
const { isMobile } = useDevice()
const practiceStore = usePracticeStore()

/** 昵称与问候 */
const nickname = ref('同学')
/** 倒计时数据 */
const countdown = ref<ExamCountdown>({ examDate: '', daysLeft: 0, progress: 0 })
/** 今日任务 */
const todayTasks = ref<HomeTodayTasks['items']>([])
/** 待批改数量 */
const pendingCount = ref(0)
/** 各题型得分率(0-100) */
const typeAccuracy = ref<Record<string, number>>({})
/** 智能练习 loading(快捷入口 + 开始今日学习共用) */
const smartLoading = ref(false)

/** 按当前时段生成问候语 */
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 5) return '凌晨好'
  if (hour < 11) return '上午好'
  if (hour < 13) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

/** 今日任务文案映射 */
function taskLabel(type: string): string {
  const map: Record<string, string> = {
    reading: '阅读理解 2篇',
    cloze: '完形填空 1篇',
    'grammar-fill': '语法填空 1篇',
    writing: '作文 1篇',
    vocab: '背单词',
  }
  return map[type] ?? type
}

/** 得分率行(顺序与首页图一致;key 对应 UserStats.typeAccuracy 字段) */
const accuracyRows = computed(() => {
  const ordered: { key: string; label: string }[] = [
    { key: 'reading', label: '阅读理解' },
    { key: 'matching', label: '五选五' },
    { key: 'cloze', label: '完形填空' },
    { key: 'grammar', label: '语法填空' },
    { key: 'writing', label: '作文' },
  ]
  return ordered.map((row) => ({ ...row, value: typeAccuracy.value[row.key] ?? 0 }))
})

/** 第一个未完成任务(用于「开始今日学习」,r3 修订 22) */
const nextTask = computed(() => todayTasks.value.find((item) => item.done < item.target))

/** 今日任务 → 页面路由映射(全部完成后按钮置灰,见 r3 修订 22) */
const todayJumpMap: Record<string, string> = {
  reading: '/practice/reading',
  matching: '/practice/matching',
  cloze: '/practice/cloze',
  'grammar-fill': '/practice/grammar-fill',
  writing: '/practice/writing',
  vocab: '/learn/vocab',
}

/** 开始今日学习:按第一个未完成任务跳转对应页面 */
function startToday(): void {
  const task = nextTask.value
  if (!task) return
  const path = todayJumpMap[task.type]
  if (path) router.push(path)
}

/** 快捷入口配置(10.5) */
const shortcuts = computed(() => [
  { key: 'exam', label: '真题模考', path: '/exam' },
  { key: 'smart', label: '智能练习', path: '' },
  { key: 'mistakes', label: '我的错题', path: '/user/mistakes' },
  { key: 'stats', label: '学习统计', path: '/user/stats' },
])

/** 快捷入口点击:智能练习调 POST /practice/smart,经 store 传递题目并进入对应题型答题 */
async function onShortcut(entry: { key: string; path: string }): Promise<void> {
  if (entry.key !== 'smart') {
    router.push(entry.path)
    return
  }
  smartLoading.value = true
  try {
    const item = await smartPractice()
    practiceStore.setIncomingItem(item)
    // 直接进入对应题型答题:type 从 item.type 取(10.5)
    router.push(`/practice/${item.type}`)
  } catch {
    // 错误提示由 request 层统一弹出
  } finally {
    smartLoading.value = false
  }
}

onMounted(async () => {
  // 并行拉取首页数据;昵称缺失时补拉 profile(刷新场景)
  const [tasks, countdownData, stats, profile] = await Promise.all([
    getTodayTasks(),
    getExamCountdown(),
    getUserStats(),
    nickname.value === '同学' ? getUserProfile().catch((): UserProfile | null => null) : Promise.resolve(null),
  ])
  todayTasks.value = tasks.items
  countdown.value = countdownData
  pendingCount.value = stats.pendingGradingCount
  typeAccuracy.value = { ...stats.typeAccuracy }
  if (profile) nickname.value = profile.nickname
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard__greeting {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dashboard__hello {
  font-size: 20px;
  color: var(--lc-text-1);
}

.dashboard__countdown {
  background-color: var(--lc-bg-card);
}

.countdown {
  display: flex;
  align-items: center;
  gap: 20px;
}

.countdown__days {
  display: flex;
  align-items: baseline;
  gap: 4px;
  padding: 0 8px;
}

.countdown__number {
  font-size: 40px;
  font-weight: 700;
  color: var(--lc-primary);
  line-height: 1;
}

.countdown__unit {
  font-size: 16px;
  color: var(--lc-text-2);
}

.countdown__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.countdown__text {
  font-size: 15px;
  color: var(--lc-text-1);
}

.countdown__sub {
  font-size: 12px;
  color: var(--lc-text-3);
}

.pending {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pending__text {
  flex: 1;
  color: var(--lc-text-2);
}

/* 警告色图标(取主题变量,双主题自适应) */
.pending__icon {
  color: var(--lc-warning);
}

.pending__link {
  color: var(--lc-primary);
  min-width: 44px;
  line-height: 44px;
  text-align: center;
}

.dashboard__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.dashboard__grid.is-mobile {
  grid-template-columns: 1fr;
}

.dashboard__tasks,
.dashboard__accuracy {
  background-color: var(--lc-bg-card);
}

.tasks {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.tasks__item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 任务完成状态图标(完成绿勾 / 未完成灰圈) */
.tasks__icon {
  display: inline-flex;
  font-size: 16px;
  color: var(--lc-text-3);
}

.tasks__icon.is-done {
  color: var(--lc-success);
}

.tasks__label {
  flex: 1;
  color: var(--lc-text-1);
}

.tasks__progress {
  color: var(--lc-text-3);
  font-size: 13px;
}

.accuracy {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.accuracy__item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.accuracy__label {
  width: 68px;
  flex-shrink: 0;
  color: var(--lc-text-2);
}

.accuracy__bar {
  flex: 1;
}

.accuracy__value {
  width: 44px;
  text-align: right;
  color: var(--lc-text-2);
  font-size: 13px;
}

.dashboard__shortcuts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

@media (max-width: 767px) {
  .dashboard__shortcuts {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>