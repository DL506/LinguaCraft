<!-- 后台运营概览(阶段 A5 接入真实指标;A1 的权限自检卡保留):数字来自 GET /api/admin/dashboard -->
<template>
  <div>
    <div class="admin-overview__head">
      <h2 class="admin-title">运营概览</h2>
      <n-button size="small" :loading="loading" @click="load">刷新</n-button>
    </div>

    <!-- 错误提示:文本与子内容统一放在默认插槽里(不能同时写直接文本与 template #default,否则 Vue 编译器报 Extraneous children) -->
    <n-alert v-if="error" type="error" :show-icon="false" title="管理端接口调用失败" class="admin-alert">
      {{ error }}
      <div class="admin-alert__hint">
        请确认后端已启动、当前登录账号为管理员(admin/123456),且已关闭 MSW
        (frontend/.env.development.local 写 VITE_USE_MOCK=false)。
      </div>
    </n-alert>

    <n-spin :show="loading">
      <!-- 待处理:唯一需要行动的一组,放最上面 -->
      <n-card size="small" title="待处理" class="overview-card">
        <n-grid cols="1 760:3" x-gap="16" y-gap="12">
          <n-gi>
            <div class="metric">
              <p class="metric__value" :class="{ 'is-alert': practicePending > 0 }">{{ practicePending }}</p>
              <p class="metric__label">练习待批改</p>
              <n-button text size="tiny" @click="router.push('/admin/submissions')">去批改工作台</n-button>
            </div>
          </n-gi>
          <n-gi>
            <div class="metric">
              <p class="metric__value" :class="{ 'is-alert': examWritingUngraded > 0 }">{{ examWritingUngraded }}</p>
              <p class="metric__label">模考作文待判</p>
              <n-button text size="tiny" @click="router.push('/admin/submissions')">去模考页签</n-button>
            </div>
          </n-gi>
          <n-gi>
            <div class="metric">
              <p class="metric__value">{{ formatWaiting(oldestPendingMinutes) }}</p>
              <p class="metric__label">最久已等待</p>
              <span class="admin-muted">按最早一条待批改的 submitted_at 算</span>
            </div>
          </n-gi>
        </n-grid>
      </n-card>

      <n-grid cols="1 760:2 1200:4" x-gap="16" y-gap="16" class="overview-grid">
        <n-gi>
          <n-card size="small" title="题库">
            <ul class="metric-list">
              <li><b>{{ dash?.questionOnline ?? '-' }}</b><span>上架(学生可见)</span></li>
              <li><b>{{ dash?.questionTotal ?? '-' }}</b><span>题目总数</span></li>
              <li><b>{{ dash?.questionAiTotal ?? '-' }}</b><span>其中 AI 生成</span></li>
              <li><b>{{ dash?.questionCreatedToday ?? '-' }}</b><span>今日新增</span></li>
            </ul>
            <template #footer>
              <n-button text size="tiny" @click="router.push('/admin/questions')">题库管理</n-button>
            </template>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small" title="用户">
            <ul class="metric-list">
              <li><b>{{ dash?.userTotal ?? '-' }}</b><span>账号总数</span></li>
              <li><b>{{ dash?.userDisabled ?? '-' }}</b><span>已停用</span></li>
              <li><b>{{ dash?.userAdmin ?? '-' }}</b><span>管理员</span></li>
            </ul>
            <template #footer>
              <span class="admin-muted">停用只拦新登录,旧令牌最长 2 小时</span>
            </template>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small" title="今日活动">
            <ul class="metric-list">
              <li><b>{{ dash?.submissionPracticeToday ?? '-' }}</b><span>练习提交</span></li>
              <li><b>{{ dash?.submissionExamToday ?? '-' }}</b><span>模考交卷</span></li>
              <li><b>{{ dash?.wordLearnedToday ?? '-' }}</b><span>新学长词</span></li>
              <li><b>{{ dash?.wordReviewedToday ?? '-' }}</b><span>复习次数</span></li>
            </ul>
            <template #footer>
              <span class="admin-muted">「今日」= created_at 今日零点起,与学生端统计同口径</span>
            </template>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small" title="内容量">
            <ul class="metric-list">
              <li><b>{{ dash?.vocabWordTotal ?? '-' }}</b><span>词库单词({{ dash?.vocabBookTotal ?? 0 }} 本词书)</span></li>
              <li><b>{{ dash?.wordLearnedTotal ?? '-' }}</b><span>全站已学长词</span></li>
              <li><b>{{ dash?.grammarSectionTotal ?? '-' }}</b><span>语法章节</span></li>
              <li><b>{{ dash?.examPaperTotal ?? '-' }}</b><span>真题卷</span></li>
            </ul>
            <template #footer>
              <span class="admin-muted">错题本累计 {{ dash?.mistakeTotal ?? 0 }} 条</span>
            </template>
          </n-card>
        </n-gi>
      </n-grid>

      <n-card size="small" title="权限链路自检(A1)" class="overview-card">
        <n-grid cols="1 760:2 1200:4" x-gap="16" y-gap="12">
          <n-gi>
            <div class="metric">
              <n-tag :type="ping ? 'success' : 'error'" size="small" :bordered="false">
                {{ ping ? '/api/admin/ping 通过' : '未通过' }}
              </n-tag>
              <p class="metric__label">角色由 JWT 声明映射为 ROLE_ADMIN 后放行</p>
            </div>
          </n-gi>
          <n-gi>
            <div class="metric">
              <p class="metric__value">{{ ping?.userId ?? '-' }}</p>
              <p class="metric__label">管理员 ID</p>
              <span class="admin-muted">后端 Long 序列化为字符串</span>
            </div>
          </n-gi>
          <n-gi>
            <div class="metric">
              <p class="metric__value">{{ ping?.role ?? '-' }}</p>
              <p class="metric__label">角色</p>
              <span class="admin-muted">库内 role 列为权威值</span>
            </div>
          </n-gi>
          <n-gi>
            <div class="metric">
              <p class="metric__value">{{ ping?.serverTime ?? '-' }}</p>
              <p class="metric__label">服务器时间</p>
              <span class="admin-muted">证明请求已进入管理端控制器</span>
            </div>
          </n-gi>
        </n-grid>
      </n-card>
    </n-spin>

    <n-card size="small" title="后续阶段(按交付计划推进)" style="margin-top: 16px">
      <n-list hoverable>
        <n-list-item v-for="item in roadmap" :key="item.phase">
          <template #prefix><n-tag size="small" :bordered="false">{{ item.phase }}</n-tag></template>
          <div>{{ item.title }}</div>
          <template #suffix><span class="admin-muted">{{ item.status }}</span></template>
        </n-list-item>
      </n-list>
    </n-card>
  </div>
</template>

<script setup lang="ts">
// 概览数据来自 GET /api/admin/dashboard(纯计数聚合);权限探针 GET /api/admin/ping 保留作为链路自检。
// 学生令牌访问这两个接口都会得到 code 403,此时页面展示错误提示与排查指引。
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NAlert,
  NButton,
  NCard,
  NGi,
  NGrid,
  NList,
  NListItem,
  NSpin,
  NTag,
} from 'naive-ui'
import { getAdminDashboard, getAdminPing } from '@/api/admin'
import type { AdminDashboard, AdminPing } from '@/types/admin'

const router = useRouter()

/** 加载态与结果 */
const loading = ref(false)
const ping = ref<AdminPing | null>(null)
const dash = ref<AdminDashboard | null>(null)
const error = ref('')

const practicePending = computed(() => dash.value?.practicePending ?? 0)
const examWritingUngraded = computed(() => dash.value?.examWritingUngraded ?? 0)
const oldestPendingMinutes = computed(() => dash.value?.practiceOldestPendingMinutes ?? 0)

/** 等待时长人话化 */
function formatWaiting(minutes: number): string {
  if (!minutes) return '无积压'
  if (minutes < 60) return `${minutes} 分钟`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时`
  return `${Math.floor(hours / 24)} 天 ${hours % 24} 小时`
}

/** 阶段路线图(与 docs/verification 的 A 系列文档一一对应) */
const roadmap = [
  { phase: 'A1', title: '权限与账号基座(role/enabled、403 统一响应、后台布局与守卫)', status: '已交付' },
  { phase: 'A2', title: '题库管理(增删改查、上下架、答案防呆校验)', status: '已交付' },
  { phase: 'A3', title: 'AI 链路加固(出题共用五道校验、判分口径、配置注入自检)', status: '已交付' },
  { phase: 'A4', title: '人工批改工作台(练习 A4 + 模考作文 A4-②)', status: '已交付' },
  { phase: 'A5', title: '运营看板与用户管理(停用、角色调整)', status: '已交付' },
  { phase: 'A6', title: '内容全家桶(词书 / 单词含批量导入 / 语法 / 真题卷管理)', status: '本阶段' },
]

async function load(): Promise<void> {
  loading.value = true
  error.value = ''
  try {
    const [pingResult, dashResult] = await Promise.all([getAdminPing(), getAdminDashboard()])
    ping.value = pingResult
    dash.value = dashResult
  } catch (e) {
    error.value = e instanceof Error ? e.message : '未知错误'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-overview__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.admin-title {
  margin: 0;
  font-size: 20px;
}

.admin-alert {
  margin-bottom: 16px;
}

.admin-alert__hint {
  margin-top: 4px;
  font-size: 12px;
}

.admin-muted {
  font-size: 12px;
  opacity: 0.7;
}

.overview-card {
  margin-bottom: 16px;
}

.overview-grid {
  margin-bottom: 16px;
}

.metric__value {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.metric__value.is-alert {
  color: var(--lc-error, #d03050);
}

.metric__label {
  margin: 2px 0 4px;
  font-size: 13px;
  opacity: 0.85;
}

.metric-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.metric-list li {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  padding: 4px 0;
  font-size: 13px;
}

.metric-list b {
  font-size: 18px;
  font-variant-numeric: tabular-nums;
}
</style>
