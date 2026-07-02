<!-- /frontend/src/components/common/MarkdownRenderer.vue -->
<!-- 职责描述：Markdown 内容渲染组件，格式化展示 AI 输出 -->

<template>
  <div class="markdown-body" v-html="rendered" />
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ content: string }>()

const rendered = computed(() => {
  return props.content
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/\n{2,}/g, '<br/><br/>')
    .replace(/\n/g, '<br/>')
})
</script>

<style scoped>
.markdown-body {
  line-height: 1.8;
  color: var(--color-text-primary);
  font-size: 14px;
}
.markdown-body :deep(h1) { font-size: 20px; margin: 16px 0 8px; }
.markdown-body :deep(h2) { font-size: 18px; margin: 14px 0 6px; }
.markdown-body :deep(h3) { font-size: 16px; margin: 12px 0 4px; }
.markdown-body :deep(code) {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
}
.markdown-body :deep(strong) { font-weight: 600; }
.markdown-body :deep(li) { margin-left: 20px; }
</style>
