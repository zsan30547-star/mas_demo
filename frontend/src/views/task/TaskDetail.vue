<!-- /frontend/src/views/task/TaskDetail.vue -->
<!-- 职责描述：任务详情页，组合 TaskStatusHeader + StepTimeline + TerminalLogViewer -->

<template>
  <div>
    <el-button text @click="$router.back()">← 返回</el-button>
    <h2>{{ store.task?.title || '任务详情' }}</h2>

    <div v-loading="store.loading">
      <TaskStatusHeader style="margin-top: 16px" />

      <div style="margin-top: 16px">
        <StepTimeline />
      </div>

      <TerminalLogViewer />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useTaskStore } from '../../stores/task'
import TaskStatusHeader from '../../components/task/TaskStatusHeader.vue'
import StepTimeline from '../../components/task/StepTimeline.vue'
import TerminalLogViewer from '../../components/task/TerminalLogViewer.vue'

const route = useRoute()
const store = useTaskStore()

onMounted(() => {
  store.startPolling(Number(route.params.id))
})

onUnmounted(() => {
  store.stopPolling()
})
</script>

<style scoped>
</style>
