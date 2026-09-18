<!-- 真题模考列表(任务书 10.12):按年份倒序展示真题套卷;筛选栏 [2026][2025][2024][2023][更早] -->
<template>
  <div class="exam-list">
    <h2 class="exam-list__title">真题模考</h2>

    <!-- 年份筛选(本地过滤,一次拉全量) -->
    <div class="exam-list__filters">
      <n-button
        v-for="year in yearChips"
        :key="year"
        size="small"
        :type="selectedYear === year ? 'primary' : 'default'"
        ghost
        @click="selectedYear = year"
      >
        {{ year }}
      </n-button>
    </div>

    <!-- 套卷卡片 -->
    <n-card v-for="item in displayList" :key="item.id" class="exam-card" :bordered="false">
      <div class="exam-card__row">
        <div class="exam-card__main">
          <p class="exam-card__title">{{ item.title }}</p>
          <p class="exam-card__meta">
            总分 {{ item.totalScore }} 分 · {{ item.duration }} 分钟 · 无听力
          </p>
          <p v-if="item.lastScore !== undefined" class="exam-card__last">
            上次得分 {{ item.lastScore }} 分
            <span v-if="item.lastSubmittedAt">({{ formatDateTime(item.lastSubmittedAt) }})</span>
          </p>
        </div>
        <n-button type="primary" @click="startExam(item.id)">开始模考</n-button>
      </div>
    </n-card>

    <n-empty v-if="displayList.length === 0" description="该年份暂无可用的真题套卷" />
  </div>
</template>

<script setup lang="ts">
// 数据:GET /exam/list(一次取全量后按年份 chips 本地过滤,含"更早"聚合)
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NEmpty } from 'naive-ui'
import { getExamList } from '@/api/exam'
import { formatDateTime } from '@/utils/format'
import type { ExamListItem } from '@/types/exam'

const router = useRouter()

const list = ref<ExamListItem[]>([])
/** 年份筛选:全部 / 具体年份 / 更早(<2023) */
const selectedYear = ref('全部')
const yearChips = ['全部', '2026', '2025', '2024', '2023', '更早']

/** 本地过滤 */
const displayList = computed(() => {
  if (selectedYear.value === '全部') return list.value
  if (selectedYear.value === '更早') return list.value.filter((i) => i.year < 2023)
  return list.value.filter((i) => i.year === Number(selectedYear.value))
})

/** 进入整卷答题 */
function startExam(id: string): void {
  router.push(`/exam/${id}`)
}

onMounted(async () => {
  const res = await getExamList({ size: 50 })
  list.value = res.list
})
</script>

<style scoped>
.exam-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.exam-list__title {
  font-size: 18px;
  color: var(--lc-text-1);
}

.exam-list__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.exam-card {
  background-color: var(--lc-bg-card);
}

.exam-card__row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.exam-card__main {
  flex: 1;
  min-width: 0;
}

.exam-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 6px;
  overflow-wrap: break-word;
}

.exam-card__meta {
  font-size: 13px;
  color: var(--lc-text-3);
  margin-bottom: 4px;
}

.exam-card__last {
  font-size: 13px;
  color: var(--lc-text-2);
}
</style>