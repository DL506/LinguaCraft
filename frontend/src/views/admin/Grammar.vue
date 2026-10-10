<!-- 后台语法管理(阶段 A6-②):目录节点(平铺 + 缩进)与正文 markdown 编辑
     两条不变式:叶节点必有同 ID 章节(新建时后端自动补占位);sort_order 决定学生端上一章/下一章 -->
<template>
  <div class="grammar">
    <div class="grammar__head">
      <h2 class="grammar__title">语法目录与正文</h2>
      <n-button size="small" type="primary" @click="openNodeForm(null, null)">新建顶层考点</n-button>
    </div>

    <n-alert type="warning" :show-icon="false" class="grammar__notice">
      这里的<b>排序就是学生端的阅读顺序</b>:目录树按 sort_order 深度优先展开,「上一章 / 下一章」由该顺序推导。
      上移下移会立刻改变学生的浏览路径。
    </n-alert>

    <n-spin :show="loading">
      <div v-for="row in flatRows" :key="row.node.id" class="grammar__row" :style="{ paddingLeft: 8 + row.depth * 24 + 'px' }">
        <n-tag size="small" :bordered="false" :type="row.node.hasChildren ? 'info' : 'default'">
          {{ row.node.hasChildren ? '目录' : '章节' }}
        </n-tag>
        <span class="grammar__name">{{ row.node.name }}</span>
        <span class="grammar__id">{{ row.node.id }}</span>
        <n-tag v-if="!row.node.hasChildren && !row.node.hasSection" size="small" type="warning" :bordered="false">
          无正文
        </n-tag>
        <NSpace :size="4">
          <n-button size="tiny" tertiary :disabled="!row.canUp" @click="move(row, -1)">上移</n-button>
          <n-button size="tiny" tertiary :disabled="!row.canDown" @click="move(row, 1)">下移</n-button>
          <n-button v-if="!row.node.hasChildren" size="tiny" tertiary type="primary" @click="openSection(row.node)">
            编辑正文
          </n-button>
          <n-button size="tiny" tertiary @click="openNodeForm(row.node, row.node.id)">加子节点</n-button>
          <n-button size="tiny" tertiary @click="openNodeForm(row.node, null)">改名</n-button>
          <n-popconfirm v-if="!row.node.hasChildren" @positive-click="onDelete(row.node)">
            <template #trigger>
              <n-button size="tiny" tertiary type="error">删除</n-button>
            </template>
            将同时删除该节点的正文,确认?
          </n-popconfirm>
        </NSpace>
      </div>
      <p v-if="!loading && flatRows.length === 0" class="grammar__empty">目录为空。</p>
    </n-spin>

    <!-- 节点表单 -->
    <n-drawer v-model:show="nodeDrawer" :width="420" placement="right">
      <n-drawer-content :title="nodeForm.id ? '改名 / 移动节点' : '新建节点'" closable>
        <label class="grammar__label">名称</label>
        <n-input v-model:value="nodeForm.name" size="small" />
        <label class="grammar__label">父节点</label>
        <n-select v-model:value="nodeForm.parentId" size="small" :options="parentOptions" clearable />
        <label class="grammar__label">排序值(小在前,留空自动排到本层末尾)</label>
        <n-input-number v-model:value="nodeForm.sortOrder" size="small" :min="0" style="width: 100%" />
        <div class="grammar__actions">
          <n-button size="small" @click="nodeDrawer = false">取消</n-button>
          <n-button size="small" type="primary" :loading="savingNode" @click="onSaveNode">保存</n-button>
        </div>
      </n-drawer-content>
    </n-drawer>

    <!-- 正文表单 -->
    <n-drawer v-model:show="sectionDrawer" :width="720" placement="right">
      <n-drawer-content :title="sectionForm.nodeId ? `正文 · ${sectionForm.nodeId}` : '正文'" closable>
        <n-spin :show="loadingSection">
          <label class="grammar__label">标题</label>
          <n-input v-model:value="sectionForm.title" size="small" />
          <label class="grammar__label">正文(markdown)</label>
          <n-input
            v-model:value="sectionForm.markdown"
            type="textarea"
            :autosize="{ minRows: 16 }"
            class="grammar__md"
          />
          <div class="grammar__actions">
            <n-button size="small" @click="sectionDrawer = false">取消</n-button>
            <n-button size="small" type="primary" :loading="savingSection" @click="onSaveSection">保存正文</n-button>
          </div>
        </n-spin>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /api/admin/grammar/nodes;PUT .../nodes/reorder(整层重排);PUT .../sections/{nodeId}(正文)
import { computed, onMounted, reactive, ref } from 'vue'
import {
  NAlert,
  NButton,
  NDrawer,
  NDrawerContent,
  NInput,
  NInputNumber,
  NPopconfirm,
  NSpace,
  NSpin,
  NSelect,
  NTag,
  useMessage,
} from 'naive-ui'
import {
  createAdminGrammarNode,
  deleteAdminGrammarNode,
  getAdminGrammarNodes,
  getAdminGrammarSection,
  reorderAdminGrammarNodes,
  updateAdminGrammarNode,
  updateAdminGrammarSection,
} from '@/api/admin'
import type { AdminGrammarNode } from '@/types/admin'

const message = useMessage()

const nodes = ref<AdminGrammarNode[]>([])
const loading = ref(false)

const nodeDrawer = ref(false)
const savingNode = ref(false)
const nodeForm = reactive<{ id: string | null; name: string; parentId: string | null; sortOrder: number | null }>({
  id: null,
  name: '',
  parentId: null,
  sortOrder: null,
})

const sectionDrawer = ref(false)
const loadingSection = ref(false)
const savingSection = ref(false)
const sectionForm = reactive({ nodeId: '', title: '', markdown: '' })

/** 按 parentId 分组并按 sortOrder 排好的层 */
const levels = computed(() => {
  const map = new Map<string, AdminGrammarNode[]>()
  for (const node of [...nodes.value].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))) {
    const key = node.parentId || ''
    if (!map.has(key)) {
      map.set(key, [])
    }
    map.get(key)!.push(node)
  }
  return map
})

/** 平铺成带层级的行(深度优先),并标出同层内能否上下移动 */
const flatRows = computed(() => {
  const rows: {
    node: AdminGrammarNode
    depth: number
    canUp: boolean
    canDown: boolean
  }[] = []
  const walk = (parentKey: string, depth: number) => {
    const siblings = levels.value.get(parentKey) ?? []
    siblings.forEach((node, index) => {
      rows.push({ node, depth, canUp: index > 0, canDown: index < siblings.length - 1 })
      walk(node.id, depth + 1)
    })
  }
  walk('', 0)
  return rows
})

const parentOptions = computed(() =>
  nodes.value.filter((node) => node.id !== nodeForm.id).map((node) => ({ label: node.name, value: node.id }))
)

async function fetchNodes(): Promise<void> {
  loading.value = true
  try {
    nodes.value = await getAdminGrammarNodes()
  } finally {
    loading.value = false
  }
}

function openNodeForm(base: AdminGrammarNode | null, parentId: string | null): void {
  nodeForm.id = base?.id ?? null
  nodeForm.name = base?.name ?? ''
  // 改名沿用原父节点;加子节点用点击行的 id;新建顶层为 null
  nodeForm.parentId = parentId ?? base?.parentId ?? null
  nodeForm.sortOrder = base?.sortOrder ?? null
  nodeDrawer.value = true
}

async function onSaveNode(): Promise<void> {
  if (!nodeForm.name.trim()) {
    message.warning('名称不能为空')
    return
  }
  const payload = {
    name: nodeForm.name.trim(),
    parentId: nodeForm.parentId || null,
    sortOrder: nodeForm.sortOrder ?? undefined,
  }
  savingNode.value = true
  try {
    if (nodeForm.id) {
      await updateAdminGrammarNode(nodeForm.id, payload)
      message.success('节点已更新')
    } else {
      const created = await createAdminGrammarNode(payload)
      message.success(`节点已创建:${created.id}(已自动补一份占位正文)`)
    }
    nodeDrawer.value = false
    void fetchNodes()
  } catch {
    /* 成环、父节点缺失等由 request 层弹出 */
  } finally {
    savingNode.value = false
  }
}

/** 同层上移/下移:交换顺序后整层提交 reorder */
async function move(row: { node: AdminGrammarNode }, delta: number): Promise<void> {
  const key = row.node.parentId || ''
  const siblings = [...(levels.value.get(key) ?? [])]
  const index = siblings.findIndex((node) => node.id === row.node.id)
  const target = index + delta
  if (index < 0 || target < 0 || target >= siblings.length) {
    return
  }
  const swapped = [...siblings]
  swapped.splice(index, 1)
  swapped.splice(target, 0, row.node)
  try {
    nodes.value = await reorderAdminGrammarNodes(key || undefined, swapped.map((node) => node.id))
    message.success('已调整顺序(学生端上一章/下一章随之改变)')
  } catch {
    /* 已提示 */
  }
}

async function onDelete(node: AdminGrammarNode): Promise<void> {
  try {
    await deleteAdminGrammarNode(node.id)
    message.success('节点与正文已删除')
    void fetchNodes()
  } catch {
    /* 已提示 */
  }
}

async function openSection(node: AdminGrammarNode): Promise<void> {
  sectionDrawer.value = true
  loadingSection.value = true
  sectionForm.nodeId = node.id
  sectionForm.title = node.sectionTitle ?? node.name
  sectionForm.markdown = ''
  try {
    const section = await getAdminGrammarSection(node.id)
    sectionForm.title = section.title
    sectionForm.markdown = section.markdown ?? ''
  } catch {
    /* 无正文时保留节点名作标题,正文留空由管理员填写 */
  } finally {
    loadingSection.value = false
  }
}

async function onSaveSection(): Promise<void> {
  if (!sectionForm.title.trim() || !sectionForm.markdown.trim()) {
    message.warning('标题与正文都不能为空')
    return
  }
  savingSection.value = true
  try {
    await updateAdminGrammarSection(sectionForm.nodeId, {
      title: sectionForm.title.trim(),
      markdown: sectionForm.markdown,
    })
    message.success('正文已保存')
    sectionDrawer.value = false
    void fetchNodes()
  } catch {
    /* 已提示 */
  } finally {
    savingSection.value = false
  }
}

onMounted(fetchNodes)
</script>

<style scoped>
.grammar__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.grammar__title {
  margin: 0;
  font-size: 20px;
}

.grammar__notice {
  margin-bottom: 12px;
}

.grammar__row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px;
  border-bottom: 1px solid var(--lc-border, rgba(128, 128, 128, 0.16));
}

.grammar__name {
  font-size: 14px;
}

.grammar__id {
  font-size: 12px;
  opacity: 0.6;
}

.grammar__empty {
  font-size: 13px;
  opacity: 0.7;
}

.grammar__label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin: 12px 0 4px;
}

.grammar__md :deep(textarea) {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 13px;
  line-height: 1.7;
}

.grammar__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
