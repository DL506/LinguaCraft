<!-- 后台运营概览(阶段 A1):先提供"权限链路自检"+ 后续阶段路线图;真正的看板指标在 A5 接入 -->
<template>
  <div>
    <h2 class="admin-title">运营概览</h2>

    <!-- 错误提示:文本与子内容统一放在默认插槽里(不能同时写直接文本与 template #default,否则 Vue 编译器报 Extraneous children) -->
    <n-alert v-if="error" type="error" :show-icon="false" title="管理端接口调用失败" class="admin-alert">
      {{ error }}
      <div class="admin-alert__hint">
        请确认后端已启动(8080)、当前登录账号为管理员(admin/123456),且已关闭 MSW
        (frontend/.env.development.local 写 VITE_USE_MOCK=false)。
      </div>
    </n-alert>

    <n-spin :show="loading">
      <n-grid cols="1 760:2 1200:4" x-gap="16" y-gap="16">
        <n-gi>
          <n-card size="small" title="权限探针">
            <n-tag :type="ping ? 'success' : 'error'" size="small" :bordered="false">
              {{ ping ? '/api/admin/ping 通过' : '未通过' }}
            </n-tag>
            <template #footer>
              <span class="admin-muted">角色由 JWT 声明映射为 ROLE_ADMIN 后放行</span>
            </template>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small" title="管理员 ID">
            <strong>{{ ping?.userId ?? '-' }}</strong>
            <template #footer><span class="admin-muted">后端 Long 序列化为字符串</span></template>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small" title="角色">
            <strong>{{ ping?.role ?? '-' }}</strong>
            <template #footer><span class="admin-muted">库内 role 列为权威值</span></template>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card size="small" title="服务器时间">
            <strong>{{ ping?.serverTime ?? '-' }}</strong>
            <template #footer><span class="admin-muted">证明请求已进入管理端控制器</span></template>
          </n-card>
        </n-gi>
      </n-grid>
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
// A1 自检页:调用 GET /api/admin/ping 验证"管理员令牌 → 后端角色判定 → 业务响应"整条链路;
// 学生令牌访问该接口会得到 code 403,此时页面展示错误提示与排查指引。
import { onMounted, ref } from 'vue'
import { NAlert, NCard, NGi, NGrid, NList, NListItem, NSpin, NTag } from 'naive-ui'
import { getAdminPing } from '@/api/admin'
import type { AdminPing } from '@/types/admin'

/** 加载态与结果 */
const loading = ref(false)
const ping = ref<AdminPing | null>(null)
const error = ref('')

/** 阶段路线图(与 docs/verification 的 A 系列文档一一对应) */
const roadmap = [
  { phase: 'A1', title: '权限与账号基座(role/enabled、403 统一响应、后台布局与守卫)', status: '已交付' },
  { phase: 'A2', title: '题库管理(增删改查、上下架、答案防呆校验)', status: '本阶段' },
  { phase: 'A3', title: '词书 / 单词(含批量导入)/ 语法 / 真题卷管理', status: '待交付' },
  { phase: 'A4', title: '人工批改工作台(闭掉"等真人改"的断链)', status: '待交付' },
  { phase: 'A5', title: '运营看板与用户管理(停用、角色调整)', status: '待交付' },
]

onMounted(async () => {
  loading.value = true
  try {
    ping.value = await getAdminPing()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '未知错误'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.admin-title {
  margin: 0 0 16px;
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
</style>
