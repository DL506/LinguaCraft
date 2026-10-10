<!-- 后台用户管理(阶段 A5):列表 + 启用停用 + 角色调整 + 详情抽屉
     护栏由后端负责(不许停用自己/不许停用最后一个可用管理员/不许给自己降权),前端只做提示与置灰 -->
<template>
  <div class="users">
    <div class="users__head">
      <h2 class="users__title">用户管理</h2>
      <span class="admin-muted">共 {{ total }} 个账号</span>
    </div>

    <div class="users__filters">
      <n-select
        v-model:value="filterRole"
        size="small"
        :options="roleOptions"
        clearable
        placeholder="全部角色"
        style="width: 132px"
        @update:value="reload"
      />
      <n-select
        v-model:value="filterEnabled"
        size="small"
        :options="enabledOptions"
        clearable
        placeholder="全部状态"
        style="width: 132px"
        @update:value="reload"
      />
      <n-input
        v-model:value="keyword"
        size="small"
        placeholder="登录名 / 昵称 / 邮箱"
        clearable
        style="width: 220px"
        @keyup.enter="reload"
        @clear="reload"
      />
      <n-button size="small" @click="reload">查询</n-button>
    </div>

    <n-alert type="warning" :show-icon="false" class="users__notice">
      后端过滤器只认令牌声明、不回查数据库:<b>停用或降权对已登录会话最长 2 小时后才生效</b>(旧令牌到期为止),
      新登录会立刻被拒(提示「账号已停用,请联系管理员」)。
    </n-alert>

    <n-data-table
      :columns="columns"
      :data="list"
      :loading="loading"
      :bordered="false"
      :scroll-x="1080"
      :row-key="(row: AdminUserListItem) => row.id"
    />

    <div class="users__pager">
      <n-pagination :page="page" :page-size="PAGE_SIZE" :item-count="total" @update:page="onPageChange" />
    </div>

    <n-drawer v-model:show="drawerShow" :width="isMobile ? '100%' : 480" placement="right">
      <n-drawer-content :title="detail ? `用户 · ${detail.username}` : '用户'" closable :native-scrollbar="false">
        <n-spin :show="detailLoading">
          <div v-if="detail" class="user-detail">
            <div class="user-detail__head">
              <n-tag size="small" :bordered="false" :type="detail.role === 'admin' ? 'primary' : 'default'">
                {{ detail.role === 'admin' ? '管理员' : '学生' }}
              </n-tag>
              <n-tag size="small" :bordered="false" :type="detail.enabled ? 'success' : 'error'">
                {{ detail.enabled ? '可用' : '已停用' }}
              </n-tag>
              <span>{{ detail.nickname }}</span>
            </div>
            <ul class="user-detail__list">
              <li><span>邮箱</span><b>{{ detail.email }}</b></li>
              <li><span>等级</span><b>{{ detail.level }} · {{ detail.levelName }}</b></li>
              <li><span>连续学习</span><b>{{ detail.continuousDays }} 天</b></li>
              <li><span>累计学习</span><b>{{ detail.totalStudyDays }} 天</b></li>
              <li><span>练习提交</span><b>{{ detail.submissionCount }}(待批改 {{ detail.pendingSubmissionCount }})</b></li>
              <li><span>模考交卷</span><b>{{ detail.examSubmissionCount }}</b></li>
              <li><span>错题</span><b>{{ detail.mistakeCount }}</b></li>
              <li><span>已学长词</span><b>{{ detail.learnedWordCount }}</b></li>
              <li><span>批改偏好</span><b>{{ detail.gradingPreference || '-' }}</b></li>
              <li><span>注册时间</span><b>{{ detail.createdAt?.replace('T', ' ') || '-' }}</b></li>
            </ul>
          </div>
        </n-spin>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /api/admin/users(列表);PATCH .../status(启用停用);PATCH .../role(角色调整)
import { computed, h, onMounted, ref } from 'vue'
import {
  NAlert,
  NButton,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NSpin,
  NPagination,
  NPopconfirm,
  NSpace,
  NInput,
  NSelect,
  NTag,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import { getAdminUser, getAdminUsers, setAdminUserEnabled, setAdminUserRole } from '@/api/admin'
import { useUserStore } from '@/stores/user'
import type { AdminUserDetail, AdminUserListItem } from '@/types/admin'

const message = useMessage()
const userStore = useUserStore()

const PAGE_SIZE = 20

const list = ref<AdminUserListItem[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const filterRole = ref<string | null>(null)
const filterEnabled = ref<string | null>(null)
const keyword = ref('')

const drawerShow = ref(false)
const detailLoading = ref(false)
const detail = ref<AdminUserDetail | null>(null)

const isMobile = computed(() => window.innerWidth < 768)

const roleOptions = [
  { label: '学生', value: 'student' },
  { label: '管理员', value: 'admin' },
]

const enabledOptions = [
  { label: '可用', value: '1' },
  { label: '已停用', value: '0' },
]

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const res = await getAdminUsers({
      keyword: keyword.value.trim() || undefined,
      role: filterRole.value || undefined,
      enabled: filterEnabled.value === null ? undefined : filterEnabled.value === '1',
      page: page.value,
      size: PAGE_SIZE,
    })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reload(): void {
  page.value = 1
  void fetchList()
}

function onPageChange(next: number): void {
  page.value = next
  void fetchList()
}

/** 停用/启用 */
async function onToggleEnabled(row: AdminUserListItem): Promise<void> {
  try {
    await setAdminUserEnabled(row.id, !row.enabled)
    message.success(row.enabled ? '已停用(旧令牌最长 2 小时后才失效)' : '已启用')
    void fetchList()
  } catch {
    /* 后端护栏拒绝原因(停用自己/最后一个管理员)由 request 层弹出 */
  }
}

/** 升权/降权 */
async function onToggleRole(row: AdminUserListItem): Promise<void> {
  const next = row.role === 'admin' ? 'student' : 'admin'
  try {
    await setAdminUserRole(row.id, next)
    message.success(next === 'admin' ? '已设为管理员' : '已还原为学生')
    void fetchList()
  } catch {
    /* 已提示 */
  }
}

async function openDetail(row: AdminUserListItem): Promise<void> {
  drawerShow.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getAdminUser(row.id)
  } finally {
    detailLoading.value = false
  }
}

const columns: DataTableColumns<AdminUserListItem> = [
  { title: 'ID', key: 'id', width: 72 },
  { title: '登录名', key: 'username', width: 130 },
  { title: '昵称', key: 'nickname', width: 130, render: (row) => row.nickname || '-' },
  { title: '邮箱', key: 'email', minWidth: 180, ellipsis: { tooltip: true } },
  {
    title: '角色',
    key: 'role',
    width: 96,
    render: (row) =>
      h(
        NTag,
        { size: 'small', bordered: false, type: row.role === 'admin' ? 'primary' : 'default' },
        { default: () => (row.role === 'admin' ? '管理员' : '学生') }
      ),
  },
  {
    title: '状态',
    key: 'enabled',
    width: 88,
    render: (row) =>
      h(
        NTag,
        { size: 'small', bordered: false, type: row.enabled ? 'success' : 'error' },
        { default: () => (row.enabled ? '可用' : '停用') }
      ),
  },
  { title: '练习提交', key: 'submissionCount', width: 96 },
  { title: '累计学习', key: 'totalStudyDays', width: 96, render: (row) => `${row.totalStudyDays ?? 0} 天` },
  {
    title: '注册时间',
    key: 'createdAt',
    width: 160,
    render: (row) => (row.createdAt ? row.createdAt.replace('T', ' ') : '-'),
  },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    fixed: 'right',
    render: (row) => {
      const view = h(
        NButton,
        { size: 'tiny', tertiary: true, onClick: () => openDetail(row) },
        { default: () => '查看' }
      )
      // 自己这一行只留"查看":停用与降权都会被后端拒绝,置灰比点了再报错更清楚
      // (n-popconfirm 没有 disabled 属性,所以自己这行直接不套确认气泡)
      const isSelf = String(row.id) === String(userStore.profile?.id)
      if (isSelf) {
        return h(NSpace, { size: 6 }, {
          default: () => [
            view,
            h(NButton, { size: 'tiny', tertiary: true, disabled: true }, { default: () => '停用' }),
            h(NButton, { size: 'tiny', tertiary: true, disabled: true }, { default: () => '降权' }),
          ],
        })
      }
      return h(NSpace, { size: 6 }, {
        default: () => [
          view,
          h(
            NPopconfirm,
            { onPositiveClick: () => onToggleEnabled(row) },
            {
              trigger: () =>
                h(
                  NButton,
                  { size: 'tiny', tertiary: true, type: row.enabled ? 'error' : 'primary' },
                  { default: () => (row.enabled ? '停用' : '启用') }
                ),
              default: () =>
                row.enabled
                  ? '停用后该账号无法新登录;已登录会话最长 2 小时内仍可用。确认停用?'
                  : '确认恢复该账号登录?',
            }
          ),
          h(
            NPopconfirm,
            { onPositiveClick: () => onToggleRole(row) },
            {
              trigger: () =>
                h(
                  NButton,
                  { size: 'tiny', tertiary: true },
                  { default: () => (row.role === 'admin' ? '降权' : '设为管理员') }
                ),
              default: () =>
                row.role === 'admin'
                  ? '确认把该账号还原为学生?后台入口将对其关闭。'
                  : '确认授予该账号管理员角色?其登录后可进入后台。',
            }
          ),
        ],
      })
    },
  },
]

onMounted(fetchList)
</script>

<style scoped>
.users__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
}

.users__title {
  margin: 0;
  font-size: 20px;
}

.admin-muted {
  font-size: 12px;
  opacity: 0.7;
}

.users__filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.users__notice {
  margin-bottom: 12px;
}

.users__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.user-detail__head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.user-detail__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.user-detail__list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 6px 0;
  border-bottom: 1px solid var(--lc-border, rgba(128, 128, 128, 0.18));
  font-size: 13px;
}

.user-detail__list span {
  opacity: 0.75;
}

.user-detail__list b {
  font-weight: 600;
  text-align: right;
}
</style>
