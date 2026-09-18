<!-- 语法目录(任务书 10.14):按考点组织,两级列表;桌面左侧栏 / 平板抽屉复用 -->
<template>
  <nav class="grammar-tree">
    <div v-for="node in nodes" :key="node.id" class="grammar-tree__group">
      <p class="grammar-tree__group-title">{{ node.name }}</p>
      <ul v-if="node.children?.length" class="grammar-tree__list">
        <li v-for="child in node.children" :key="child.id">
          <button
            type="button"
            class="grammar-tree__item"
            :class="{ 'is-active': child.id === currentId }"
            @click="$emit('select', child.id)"
          >
            {{ child.name }}
          </button>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script setup lang="ts">
import type { GrammarNode } from '@/types/grammar'

defineProps<{
  /** 目录树 */
  nodes: GrammarNode[]
  /** 当前选中的章节 id */
  currentId?: string
}>()

defineEmits<{ select: [id: string] }>()
</script>

<style scoped>
.grammar-tree {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 12px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  overflow-wrap: break-word;
}

.grammar-tree__group-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--lc-text-1);
  margin-bottom: 6px;
}

.grammar-tree__list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.grammar-tree__item {
  display: block;
  width: 100%;
  padding: 8px 10px;
  border: none;
  border-radius: 8px;
  background: none;
  text-align: left;
  font-size: 13px;
  color: var(--lc-text-2);
  cursor: pointer;
  min-height: 36px;
}

.grammar-tree__item:hover {
  background-color: var(--lc-bg-hover);
}

.grammar-tree__item.is-active {
  background-color: var(--lc-primary-soft);
  color: var(--lc-primary);
  font-weight: 600;
}
</style>