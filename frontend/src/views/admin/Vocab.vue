<!-- 后台词书与单词管理(阶段 A6-①):两个页签
     关键事实:词书与单词之间没有中间表 —— 词书按 filterLevel 过滤单词,所以"改等级"就等于"改归属" -->
<template>
  <div class="vocab">
    <div class="vocab__head">
      <h2 class="vocab__title">词书与单词</h2>
      <n-button size="small" type="primary" @click="openWordForm(null)">新建单词</n-button>
    </div>

    <n-tabs v-model:value="tab" type="segment">
      <!-- 词书 -->
      <n-tab-pane name="books" tab="词书">
        <n-alert type="info" :show-icon="false" class="vocab__notice">
          词书是<b>过滤器</b>不是集合:一本书里的词 = 等级命中该书 filterLevel 的单词(filterLevel 为空即全部)。
          「声明词数」是展示值(考纲词量),与「真实词数」不等属正常;要对齐请显式点「对齐词数」。
        </n-alert>
        <n-data-table
          :columns="bookColumns"
          :data="books"
          :loading="booksLoading"
          :bordered="false"
          :scroll-x="920"
          :row-key="(row: AdminVocabBook) => row.id"
        />
        <n-button size="small" dashed block class="vocab__add" @click="openBookForm(null)">+ 新建词书</n-button>
      </n-tab-pane>

      <!-- 单词 -->
      <n-tab-pane name="words" tab="单词">
        <div class="vocab__filters">
          <n-select
            v-model:value="filterLevel"
            size="small"
            :options="levelOptions"
            clearable
            placeholder="全部等级"
            style="width: 132px"
            @update:value="reloadWords"
          />
          <n-input
            v-model:value="keyword"
            size="small"
            placeholder="拼写或释义关键词"
            clearable
            style="width: 220px"
            @keyup.enter="reloadWords"
            @clear="reloadWords"
          />
          <n-button size="small" @click="reloadWords">查询</n-button>
          <n-button size="small" :loading="importing" @click="pickCsv">导入 CSV</n-button>
          <input
            ref="csvInput"
            type="file"
            accept=".csv,text/csv"
            class="vocab__file"
            @change="onCsvPicked"
          />
          <span class="vocab__count">共 {{ wordTotal }} 个单词</span>
        </div>

        <!-- 导入结果:重复与非法都是"跳过并回报",不阻断其余行入库 -->
        <n-alert
          v-if="importResult"
          :type="importResult.skippedInvalid || importResult.skippedDuplicate ? 'warning' : 'success'"
          :show-icon="false"
          class="vocab__notice"
        >
          导入完成:读到 {{ importResult.total }} 行,入库 {{ importResult.inserted }} 条,
          重复跳过 {{ importResult.skippedDuplicate }} 条,非法跳过 {{ importResult.skippedInvalid }} 条。
          <ul v-if="importResult.errors?.length" class="vocab__errors">
            <li v-for="(err, index) in importResult.errors" :key="index">
              {{ err.line ? `第 ${err.line} 行` : '文件' }}{{ err.spelling ? `「${err.spelling}」` : '' }}:{{ err.reason }}
            </li>
          </ul>
          <p v-if="importResult.truncated" class="vocab__hint">错误清单已截断,只列前 50 条。</p>
        </n-alert>
        <n-data-table
          :columns="wordColumns"
          :data="words"
          :loading="wordsLoading"
          :bordered="false"
          :scroll-x="1180"
          :row-key="(row: AdminWord) => row.id"
        />
        <div class="vocab__pager">
          <n-pagination :page="wordPage" :page-size="PAGE_SIZE" :item-count="wordTotal" @update:page="onWordPage" />
        </div>
      </n-tab-pane>
    </n-tabs>

    <!-- 词书表单 -->
    <n-drawer v-model:show="bookDrawer" :width="420" placement="right">
      <n-drawer-content :title="bookForm.id ? `编辑词书 · ${bookForm.id}` : '新建词书'" closable>
        <label class="vocab__label">名称</label>
        <n-input v-model:value="bookForm.name" size="small" placeholder="如:专插本核心词汇" />
        <label class="vocab__label">声明总词数(展示值,可留空)</label>
        <n-input-number v-model:value="bookForm.total" size="small" :min="0" style="width: 100%" />
        <label class="vocab__label">过滤等级(留空=收录全部等级)</label>
        <n-select v-model:value="bookForm.filterLevel" size="small" :options="levelOptions" clearable />
        <label class="vocab__label">排序值(小在前)</label>
        <n-input-number v-model:value="bookForm.sortOrder" size="small" :min="0" style="width: 100%" />
        <div class="vocab__actions">
          <n-button size="small" @click="bookDrawer = false">取消</n-button>
          <n-button size="small" type="primary" :loading="savingBook" @click="onSaveBook">保存</n-button>
        </div>
      </n-drawer-content>
    </n-drawer>

    <!-- 单词表单 -->
    <n-drawer v-model:show="wordDrawer" :width="520" placement="right">
      <n-drawer-content :title="wordForm.id ? `编辑单词 · ${wordForm.id}` : '新建单词'" closable>
        <p v-if="!wordForm.id" class="vocab__hint">
          ID 由服务端按现有最大数字后缀 +1 生成(学生端学习队列按 ID 排序,不能插到中间)。
        </p>
        <div class="vocab__row2">
          <div>
            <label class="vocab__label">拼写</label>
            <n-input v-model:value="wordForm.spelling" size="small" />
          </div>
          <div>
            <label class="vocab__label">词性</label>
            <n-input v-model:value="wordForm.partOfSpeech" size="small" placeholder="v. / n. / adj." />
          </div>
        </div>
        <label class="vocab__label">音标</label>
        <n-input v-model:value="wordForm.phonetic" size="small" placeholder="/əˈbændən/" />
        <label class="vocab__label">释义(每行一条,至少一条)</label>
        <n-input v-model:value="wordForm.meaningsText" type="textarea" :autosize="{ minRows: 2 }" />
        <label class="vocab__label">等级(决定它属于哪些词书)</label>
        <n-select v-model:value="wordForm.level" size="small" :options="levelOptions" />
        <label class="vocab__label">例句</label>
        <n-input v-model:value="wordForm.exampleSentence" size="small" />
        <label class="vocab__label">例句翻译</label>
        <n-input v-model:value="wordForm.exampleTranslation" size="small" />
        <label class="vocab__label">真题句</label>
        <n-input v-model:value="wordForm.realExamSentence" type="textarea" :autosize="{ minRows: 2 }" />
        <label class="vocab__label">真题来源</label>
        <n-input v-model:value="wordForm.realExamSource" size="small" placeholder="2023 年广东真题" />
        <div class="vocab__actions">
          <n-button size="small" @click="wordDrawer = false">取消</n-button>
          <n-button size="small" type="primary" :loading="savingWord" @click="onSaveWord">保存</n-button>
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /api/admin/vocab/books|words;POST/PUT/PATCH sync-total/DELETE
import { h, onMounted, reactive, ref } from 'vue'
import {
  NAlert,
  NButton,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NInput,
  NInputNumber,
  NPagination,
  NPopconfirm,
  NSpace,
  NSelect,
  NTabPane,
  NTabs,
  NTag,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import {
  createAdminVocabBook,
  createAdminWord,
  deleteAdminVocabBook,
  deleteAdminWord,
  getAdminVocabBooks,
  getAdminWords,
  importAdminWords,
  syncAdminVocabBookTotal,
  updateAdminVocabBook,
  updateAdminWord,
} from '@/api/admin'
import type { AdminVocabBook, AdminWord, WordImportResult } from '@/types/admin'

const message = useMessage()
const PAGE_SIZE = 20

const tab = ref<'books' | 'words'>('books')

const books = ref<AdminVocabBook[]>([])
const booksLoading = ref(false)

const words = ref<AdminWord[]>([])
const wordTotal = ref(0)
const wordPage = ref(1)
const wordsLoading = ref(false)
const filterLevel = ref<string | null>(null)
const keyword = ref('')

/** 等级选项:与后端白名单和前端 WordLevel 联合一致 */
const levelOptions = [
  { label: '高频 high', value: 'high' },
  { label: '核心 core', value: 'core' },
  { label: '认知 cognitive', value: 'cognitive' },
]

const LEVEL_LABELS: Record<string, string> = { high: '高频', core: '核心', cognitive: '认知' }

const bookDrawer = ref(false)
const savingBook = ref(false)
const bookForm = reactive<{ id: string | null; name: string; total: number | null; filterLevel: string | null; sortOrder: number | null }>({
  id: null,
  name: '',
  total: null,
  filterLevel: null,
  sortOrder: null,
})

const wordDrawer = ref(false)
const savingWord = ref(false)
const wordForm = reactive<{
  id: string | null
  spelling: string
  phonetic: string
  partOfSpeech: string
  meaningsText: string
  level: string
  exampleSentence: string
  exampleTranslation: string
  realExamSentence: string
  realExamSource: string
}>({
  id: null,
  spelling: '',
  phonetic: '',
  partOfSpeech: '',
  meaningsText: '',
  level: 'core',
  exampleSentence: '',
  exampleTranslation: '',
  realExamSentence: '',
  realExamSource: '',
})

function levelLabel(level?: string | null): string {
  return level ? LEVEL_LABELS[level] ?? level : '不限'
}

async function fetchBooks(): Promise<void> {
  booksLoading.value = true
  try {
    books.value = await getAdminVocabBooks()
  } finally {
    booksLoading.value = false
  }
}

async function fetchWords(): Promise<void> {
  wordsLoading.value = true
  try {
    const res = await getAdminWords({
      level: filterLevel.value || undefined,
      keyword: keyword.value.trim() || undefined,
      page: wordPage.value,
      size: PAGE_SIZE,
    })
    words.value = res.list
    wordTotal.value = res.total
  } finally {
    wordsLoading.value = false
  }
}

function reloadWords(): void {
  wordPage.value = 1
  void fetchWords()
}

function onWordPage(next: number): void {
  wordPage.value = next
  void fetchWords()
}

const bookColumns: DataTableColumns<AdminVocabBook> = [
  { title: 'ID', key: 'id', width: 160, ellipsis: { tooltip: true } },
  { title: '名称', key: 'name', minWidth: 160 },
  { title: '声明词数', key: 'declaredTotal', width: 104, render: (row) => row.declaredTotal ?? '-' },
  {
    title: '真实词数',
    key: 'actualWordCount',
    width: 104,
    render: (row) =>
      h(
        NTag,
        {
          size: 'small',
          bordered: false,
          // 声明与真实不一致是常态(词库只导了演示量),用颜色提示而非报错
          type: row.declaredTotal === row.actualWordCount ? 'success' : 'warning',
        },
        { default: () => String(row.actualWordCount) }
      ),
  },
  { title: '过滤等级', key: 'filterLevel', width: 100, render: (row) => levelLabel(row.filterLevel) },
  { title: '排序', key: 'sortOrder', width: 76 },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 6 }, {
        default: () => [
          h(
            NButton,
            { size: 'tiny', tertiary: true, onClick: () => openBookForm(row) },
            { default: () => '编辑' }
          ),
          h(
            NButton,
            { size: 'tiny', tertiary: true, onClick: () => onSyncTotal(row) },
            { default: () => '对齐词数' }
          ),
          h(
            NPopconfirm,
            { onPositiveClick: () => onDeleteBook(row) },
            {
              trigger: () =>
                h(
                  NButton,
                  { size: 'tiny', tertiary: true, type: 'error', disabled: row.id === 'book-syllabus' },
                  { default: () => '删除' }
                ),
              default: () =>
                row.id === 'book-syllabus'
                  ? '学生端默认词书,不可删除'
                  : '删除词书只移除这个过滤器,不会删除单词本身。确认删除?',
            }
          ),
        ],
      }),
  },
]

const wordColumns: DataTableColumns<AdminWord> = [
  { title: 'ID', key: 'id', width: 96 },
  { title: '拼写', key: 'spelling', width: 150 },
  { title: '音标', key: 'phonetic', width: 140, ellipsis: { tooltip: true } },
  { title: '词性', key: 'partOfSpeech', width: 76 },
  { title: '释义', key: 'meanings', minWidth: 220, render: (row) => row.meanings?.join('; ') || '-' },
  {
    title: '等级',
    key: 'level',
    width: 92,
    render: (row) => h(NTag, { size: 'small', bordered: false }, { default: () => levelLabel(row.level) }),
  },
  {
    title: '在学',
    key: 'learnedUserCount',
    width: 76,
    render: (row) =>
      h(
        NTag,
        { size: 'small', bordered: false, type: row.learnedUserCount > 0 ? 'warning' : 'default' },
        { default: () => String(row.learnedUserCount) }
      ),
  },
  { title: '收藏', key: 'favoriteCount', width: 76 },
  {
    title: '操作',
    key: 'actions',
    width: 168,
    fixed: 'right',
    render: (row) => {
      const guarded = row.learnedUserCount > 0 || row.favoriteCount > 0
      const edit = h(
        NButton,
        { size: 'tiny', tertiary: true, onClick: () => openWordForm(row) },
        { default: () => '编辑' }
      )
      if (guarded) {
        // 有引用时直接不给删除入口:后端会拒,置灰比点了报错更清楚
        return h(NSpace, { size: 6 }, {
          default: () => [
            edit,
            h(NButton, { size: 'tiny', tertiary: true, disabled: true }, { default: () => '删除' }),
          ],
        })
      }
      return h(NSpace, { size: 6 }, {
        default: () => [
          edit,
          h(
            NPopconfirm,
            { onPositiveClick: () => onDeleteWord(row) },
            {
              trigger: () =>
                h(NButton, { size: 'tiny', tertiary: true, type: 'error' }, { default: () => '删除' }),
              default: () => `确认删除「${row.spelling}」?`,
            }
          ),
        ],
      })
    },
  },
]

function openBookForm(row: AdminVocabBook | null): void {
  bookForm.id = row?.id ?? null
  bookForm.name = row?.name ?? ''
  bookForm.total = row?.declaredTotal ?? null
  bookForm.filterLevel = row?.filterLevel ?? null
  bookForm.sortOrder = row?.sortOrder ?? null
  bookDrawer.value = true
}

function openWordForm(row: AdminWord | null): void {
  wordForm.id = row?.id ?? null
  wordForm.spelling = row?.spelling ?? ''
  wordForm.phonetic = row?.phonetic ?? ''
  wordForm.partOfSpeech = row?.partOfSpeech ?? ''
  wordForm.meaningsText = (row?.meanings ?? []).join('\n')
  wordForm.level = row?.level ?? 'core'
  wordForm.exampleSentence = row?.exampleSentence ?? ''
  wordForm.exampleTranslation = row?.exampleTranslation ?? ''
  wordForm.realExamSentence = row?.realExamSentence ?? ''
  wordForm.realExamSource = row?.realExamSource ?? ''
  wordDrawer.value = true
}

async function onSaveBook(): Promise<void> {
  if (!bookForm.name.trim()) {
    message.warning('词书名称不能为空')
    return
  }
  const payload = {
    name: bookForm.name.trim(),
    total: bookForm.total ?? undefined,
    filterLevel: bookForm.filterLevel || null,
    sortOrder: bookForm.sortOrder ?? undefined,
  }
  savingBook.value = true
  try {
    if (bookForm.id) {
      await updateAdminVocabBook(bookForm.id, payload)
      message.success('词书已更新')
    } else {
      const created = await createAdminVocabBook(payload)
      message.success(`词书已创建:${created.id}`)
    }
    bookDrawer.value = false
    void fetchBooks()
  } catch {
    /* 校验原因由 request 层弹出 */
  } finally {
    savingBook.value = false
  }
}

async function onSyncTotal(row: AdminVocabBook): Promise<void> {
  try {
    const updated = await syncAdminVocabBookTotal(row.id)
    message.success(`声明词数已对齐为 ${updated.declaredTotal}`)
    void fetchBooks()
  } catch {
    /* 已提示 */
  }
}

async function onDeleteBook(row: AdminVocabBook): Promise<void> {
  try {
    await deleteAdminVocabBook(row.id)
    message.success('词书已删除(单词本身未动)')
    void fetchBooks()
  } catch {
    /* 已提示 */
  }
}

async function onSaveWord(): Promise<void> {
  const meanings = wordForm.meaningsText
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
  if (!wordForm.spelling.trim() || !wordForm.phonetic.trim() || !wordForm.partOfSpeech.trim()) {
    message.warning('拼写、音标、词性都必填')
    return
  }
  if (meanings.length === 0) {
    message.warning('释义至少一行')
    return
  }
  const payload = {
    spelling: wordForm.spelling.trim(),
    phonetic: wordForm.phonetic.trim(),
    partOfSpeech: wordForm.partOfSpeech.trim(),
    meanings,
    level: wordForm.level,
    exampleSentence: wordForm.exampleSentence.trim() || undefined,
    exampleTranslation: wordForm.exampleTranslation.trim() || undefined,
    realExamSentence: wordForm.realExamSentence.trim() || undefined,
    realExamSource: wordForm.realExamSource.trim() || undefined,
  }
  savingWord.value = true
  try {
    if (wordForm.id) {
      await updateAdminWord(wordForm.id, payload)
      message.success('单词已更新')
    } else {
      const created = await createAdminWord(payload)
      message.success(`单词已创建:${created.id}`)
    }
    wordDrawer.value = false
    void fetchWords()
    void fetchBooks()
  } catch {
    /* 拼写重复、等级非法等由 request 层弹出 */
  } finally {
    savingWord.value = false
  }
}

async function onDeleteWord(row: AdminWord): Promise<void> {
  try {
    await deleteAdminWord(row.id)
    message.success('已删除')
    void fetchWords()
    void fetchBooks()
  } catch {
    /* 已提示 */
  }
}

/** CSV 导入:用隐藏 file input 而非 n-upload,少一层自定义请求适配 */
const csvInput = ref<HTMLInputElement | null>(null)
const importing = ref(false)
const importResult = ref<WordImportResult | null>(null)

function pickCsv(): void {
  csvInput.value?.click()
}

async function onCsvPicked(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  importing.value = true
  try {
    importResult.value = await importAdminWords(file)
    message.success(`导入完成:入库 ${importResult.value.inserted} 条`)
    void fetchWords()
    void fetchBooks()
  } catch {
    /* 表头缺列、文件为空等由 request 层弹出 */
  } finally {
    importing.value = false
    // 允许连续导入同一文件(否则 change 不再触发)
    input.value = ''
  }
}

onMounted(() => {
  void fetchBooks()
  void fetchWords()
})
</script>

<style scoped>
.vocab__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.vocab__title {
  margin: 0;
  font-size: 20px;
}

.vocab__notice {
  margin-bottom: 12px;
}

.vocab__filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.vocab__count {
  font-size: 12px;
  opacity: 0.7;
}

.vocab__file {
  display: none;
}

.vocab__errors {
  margin: 8px 0 0;
  padding-left: 18px;
  font-size: 12px;
  line-height: 1.7;
}

.vocab__hint {
  margin: 6px 0 0;
  font-size: 12px;
  opacity: 0.75;
}

.vocab__add {
  margin-top: 12px;
}

.vocab__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.vocab__label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin: 12px 0 4px;
}

.vocab__hint {
  margin: 0 0 8px;
  font-size: 12px;
  opacity: 0.75;
}

.vocab__row2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.vocab__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 20px;
}
</style>
