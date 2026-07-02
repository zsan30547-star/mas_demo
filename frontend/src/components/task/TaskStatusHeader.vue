<!-- /frontend/src/components/task/TaskStatusHeader.vue -->
<!-- 职责描述：任务详情页顶部状态卡片——任务标题、状态、耗时 -->

<template>
  <el-card v-if="store.task">
    <div class="task-header">
      <h3>{{ store.task.title }}</h3>
      <div class="task-meta">
        <StatusBadge :status="store.task.status" />
        <span>创建时间：{{ store.task.createdAt }}</span>
        <span v-if="store.task.finishedAt">完成时间：{{ store.task.finishedAt }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useTaskStore } from '../../stores/task'
import StatusBadge from '../common/StatusBadge.vue'

const store = useTaskStore()
</script>

<style scoped>
.task-header { display: flex; justify-content: space-between; align-items: center; }
.task-meta { display: flex; gap: 16px; align-items: center; font-size: 13px; color: var(--color-text-placeholder); }
h3 { margin: 0; }
</style>
