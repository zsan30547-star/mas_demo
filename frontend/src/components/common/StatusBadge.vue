<!-- /frontend/src/components/common/StatusBadge.vue -->
<!-- 职责描述：任务状态标签，带过渡动画和图标 -->

<template>
  <el-tag :type="type" size="small" effect="plain" class="status-badge">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: string }>()

const map: Record<string, { type: string; label: string }> = {
  pending: { type: 'info', label: '等待中' },
  running: { type: 'warning', label: '执行中' },
  completed: { type: 'success', label: '已完成' },
  failed: { type: 'danger', label: '失败' },
  success: { type: 'success', label: '成功' },
}

const type = computed(() => map[props.status]?.type || 'info')
const label = computed(() => map[props.status]?.label || props.status)
</script>

<style scoped>
.status-badge {
  transition: all var(--transition-normal);
}
</style>
