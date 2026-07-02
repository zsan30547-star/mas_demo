# Task 5: 新建任务状态管理 `stores/task.ts`

**Goal:** 创建 `useTaskStore`，接管任务详情页的状态——任务数据、步骤日志、轮询逻辑、进度计算。

**Files:**
- Create: `frontend/src/stores/task.ts`

**Interfaces:**
- Produces: `useTaskStore` — `task`, `stepLogs`, `loading`, `activeStep`, `isRunning`, `currentStepIndex`, `fetchTaskDetail()`, `startPolling()`, `stopPolling()`

**Global Constraints:**
- TypeScript strict mode 已启用
- 编译验证命令：`npm run build`（在 frontend 目录执行）
- 禁止引入新的第三方依赖

---

### 完整代码

创建 `E:\Code\jobs\project_1\frontend\src\stores\task.ts`：

```typescript
// /frontend/src/stores/task.ts
// 职责描述：任务详情状态管理——状态轮询、步骤日志、进度计算

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getTaskDetail } from '../api/task'
import type { TaskVO, StepLogVO } from '../types/task'

export const useTaskStore = defineStore('task', () => {
  const task = ref<TaskVO | null>(null)
  const stepLogs = ref<StepLogVO[]>([])
  const loading = ref(false)
  let pollTimer: ReturnType<typeof setInterval> | null = null

  const activeStep = computed(() =>
    stepLogs.value.filter((s) => s.status === 'success').length,
  )

  const isRunning = computed(() =>
    task.value?.status === 'pending' || task.value?.status === 'running',
  )

  const currentStepIndex = computed(() =>
    stepLogs.value.findIndex((s) => s.status === 'pending' || s.status === 'running'),
  )

  async function fetchTaskDetail(taskId: number) {
    loading.value = true
    try {
      const res = await getTaskDetail(taskId)
      task.value = res.data.task
      stepLogs.value = res.data.stepLogs || []
    } finally {
      loading.value = false
    }
  }

  function startPolling(taskId: number, intervalMs = 3000) {
    stopPolling()
    fetchTaskDetail(taskId)
    pollTimer = setInterval(() => {
      fetchTaskDetail(taskId).then(() => {
        if (!isRunning.value) stopPolling()
      })
    }, intervalMs)
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  return {
    task, stepLogs, loading, activeStep, isRunning, currentStepIndex,
    fetchTaskDetail, startPolling, stopPolling,
  }
})
```

### 编译验证

在 `E:\Code\jobs\project_1\frontend` 目录执行：
```bash
npx vue-tsc --noEmit
npm run build
```

---

**验证标准：**
1. 文件创建在正确路径
2. `vue-tsc --noEmit` 零错误
3. `npm run build` 成功
