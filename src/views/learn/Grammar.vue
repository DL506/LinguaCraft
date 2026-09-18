<!-- 学语法页(任务书 10.14):桌面 左目录 240px + 右内容;平板 目录进抽屉;手机 顶部二级 Tab + 单栏内容 + 底部固定操作条
     内容结构由 markdown 承载:考点精讲 → 真题示例 → 易错点辨析 → 专项练习 -->
<template>
  <div class="grammar-page">
    <!-- 桌面/平板:标题行(平板附目录按钮) -->
    <div v-if="!isMobile" class="grammar-page__head">
      <h2 class="grammar-page__title">学语法</h2>
      <n-button v-if="isTablet" size="small" @click="drawerOpen = true">目录</n-button>
    </div>

    <!-- 手机:顶部横向小节 Tab(二级导航) -->
    <div v-if="isMobile" class="grammar-page__mobile-tabs">
      <n-button
        v-for="leaf in store.leaves"
        :key="leaf.id"
        size="tiny"
        :type="leaf.id === store.currentSection?.id ? 'primary' : 'default'"
        ghost
        @click="store.selectSection(leaf.id)"
      >
        {{ leaf.name }}
      </n-button>
    </div>

    <div class="grammar-page__body" :class="{ 'is-desktop': isDesktop }">
      <!-- 桌面:左侧目录常驻 -->
      <aside v-if="isDesktop" class="grammar-page__aside">
        <GrammarTree :nodes="store.tree" :current-id="store.currentSection?.id" @select="store.selectSection" />
      </aside>

      <!-- 平板:目录抽屉 -->
      <n-drawer v-if="isTablet" v-model:show="drawerOpen" placement="left" :width="260" display-directive="show">
        <GrammarTree :nodes="store.tree" :current-id="store.currentSection?.id" @select="onTreeSelect" />
      </n-drawer>

      <!-- 内容区 -->
      <div class="grammar-page__content">
        <div v-if="store.loading" class="grammar-page__loading">
          <n-spin size="large" />
        </div>
        <GrammarContent v-else-if="store.currentSection" :section="store.currentSection" />

        <!-- 桌面/平板:上一节/下一节 -->
        <div v-if="!isMobile" class="grammar-page__nav">
          <n-button :disabled="store.currentIndex <= 0" @click="store.selectPrev()">上一节</n-button>
          <n-button :disabled="store.currentIndex >= store.leaves.length - 1" @click="store.selectNext()">下一节</n-button>
        </div>
      </div>
    </div>

    <!-- 手机:底部固定操作条(上一节/下一节,10.14) -->
    <div v-if="isMobile" class="grammar-page__mobile-bar">
      <n-button block :disabled="store.currentIndex <= 0" @click="store.selectPrev()">上一节</n-button>
      <n-button
        block
        type="primary"
        :disabled="store.currentIndex >= store.leaves.length - 1"
        @click="store.selectNext()"
      >
        下一节
      </n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /grammar/tree(目录)+ GET /grammar/section/:id(markdown 内容,12.6)
import { onMounted, ref } from 'vue'
import { NButton, NDrawer, NSpin } from 'naive-ui'
import GrammarTree from '@/components/grammar/GrammarTree.vue'
import GrammarContent from '@/components/grammar/GrammarContent.vue'
import { useDevice } from '@/composables/useDevice'
import { useGrammarStore } from '@/stores/grammar'

const { isMobile, isTablet, isDesktop } = useDevice()
const store = useGrammarStore()

/** 平板目录抽屉开合 */
const drawerOpen = ref(false)

/** 抽屉内选择章节后自动收起 */
async function onTreeSelect(id: string): Promise<void> {
  await store.selectSection(id)
  drawerOpen.value = false
}

onMounted(() => {
  void store.init()
})
</script>

<style scoped>
/* 页面自然流式布局:由布局层内容区统一滚动 */
.grammar-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.grammar-page__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.grammar-page__title {
  font-size: 18px;
  color: var(--lc-text-1);
}

/* 手机:顶部横向小节 Tab */
.grammar-page__mobile-tabs {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 4px;
}

.grammar-page__mobile-tabs :deep(.n-button) {
  flex-shrink: 0;
}

/* 桌面:左目录 240px + 右内容 */
.grammar-page__body {
  min-width: 0;
}

.grammar-page__body.is-desktop {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.grammar-page__aside {
  min-width: 0;
}

.grammar-page__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.grammar-page__loading {
  display: flex;
  justify-content: center;
  padding: 64px 0;
}

.grammar-page__nav {
  display: flex;
  gap: 12px;
}

/* 手机:底部固定操作条(sticky,与全站滚动结构一致) */
.grammar-page__mobile-bar {
  position: sticky;
  bottom: 8px;
  z-index: 10;
  display: flex;
  gap: 12px;
  padding: 8px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  box-shadow: var(--lc-shadow-sm);
}
</style>