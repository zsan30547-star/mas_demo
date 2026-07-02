# Task 8: 重构任务详情页 TaskDetail.vue —— 使用 Store + 拆分为子组件

**Goal:** 将原本将所有展示逻辑混在单文件里的 `TaskDetail.vue`，重构为布局容器并拆分出：状态头、步骤时间轴、以及具有极客感的终端实时日志器。

**Files:**
- Create: `frontend/src/components/task/TaskStatusHeader.vue`
- Create: `frontend/src/components/task/StepTimeline.vue`
- Create: `frontend/src/components/task/TerminalLogViewer.vue`
- Modify: `frontend/src/views/task/TaskDetail.vue`

---

### Step 1: 创建 `TaskStatusHeader.vue`

```vue
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
.task-meta { display: flex; gap: 16px; align-items: center; font-size: 13px; color: #909399; }
h3 { margin: 0; }
</style>
```

### Step 2: 创建 `StepTimeline.vue`

```vue
<!-- /frontend/src/components/task/StepTimeline.vue -->
<!-- 职责描述：任务步骤时间轴，垂直 Timeline 展示每个步骤的输入/输出/耗时 -->

<template>
  <el-card header="执行进度">
    <el-steps :active="store.activeStep" finish-status="success" direction="vertical">
      <el-step
        v-for="(log, idx) in store.stepLogs"
        :key="idx"
        :title="`${log.agentName} (${log.agentType})`"
        :status="stepStatus(log)"
      >
        <template #description>
          <div v-if="log.status === 'success' || log.status === 'failed'" class="step-detail">
            <div v-if="log.input"><strong>输入：</strong><pre>{{ log.input }}</pre></div>
            <div v-if="log.output"><strong>输出：</strong><pre>{{ log.output }}</pre></div>
            <div v-if="log.durationMs">⏱ {{ log.durationMs }}ms</div>
            <div v-if="log.error" style="color: #f56c6c">❌ {{ log.error }}</div>
          </div>
          <div v-else-if="log.status === 'running'" style="color: #409eff">▶ 执行中...</div>
          <div v-else style="color: #909399">等待执行...</div>
        </template>
      </el-step>
    </el-steps>
    <el-empty v-if="!store.stepLogs.length" description="暂无执行日志" :image-size="60" />
  </el-card>
</template>

<script setup lang="ts">
import { useTaskStore } from '../../stores/task'
import type { StepLogVO } from '../../types/task'

const store = useTaskStore()

function stepStatus(log: StepLogVO): string {
  if (log.status === 'success') return 'success'
  if (log.status === 'failed') return 'error'
  return 'process'
}
</script>

<style scoped>
.step-detail pre {
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 13px;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}
</style>
```

### Step 3: 创建 `TerminalLogViewer.vue`

```vue
<!-- /frontend/src/components/task/TerminalLogViewer.vue -->
<!-- 职责描述：仿终端实时日志查看器，黑底绿字展示当前执行中的步骤日志 -->

<template>
  <el-card header="执行日志" class="terminal-card">
    <div class="terminal" ref="terminalRef">
      <div v-if="store.task" class="terminal-line">
        <span class="prefix">$</span> 任务 {{ store.task.id }} — {{ store.task.title }}
      </div>
      <div v-if="store.task" class="terminal-line">
        <span class="prefix">&gt;</span> 状态：{{ store.task.status }}
      </div>
      <div v-for="(log, idx) in store.stepLogs" :key="idx" class="terminal-line">
        <span :class="log.status === 'failed' ? 'prefix error' : 'prefix success'">
          {{ log.status === 'failed' ? '✗' : log.status === 'success' ? '✓' : '○' }}
        </span>
        [步骤{{ log.stepIndex }}] {{ log.agentName }}
        <span v-if="log.durationMs"> ({{ log.durationMs }}ms)</span>
      </div>
      <div v-if="store.isRunning" class="terminal-line blink">
        <span class="prefix">▶</span> 执行中...
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { watch, ref, nextTick } from 'vue'
import { useTaskStore } from '../../stores/task'

const store = useTaskStore()
const terminalRef = ref<HTMLElement>()

watch(() => store.stepLogs.length, async () => {
  await nextTick()
  if (terminalRef.value) {
    terminalRef.value.scrollTop = terminalRef.value.scrollHeight
  }
}, { deep: false })
</script>

<style scoped>
.terminal-card { margin-top: 16px; }
.terminal {
  background: #1a1a2e;
  color: #e0e0e0;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  padding: 16px;
  border-radius: 6px;
  max-height: 300px;
  overflow-y: auto;
  line-height: 1.8;
}
.terminal-line { white-space: pre-wrap; margin-bottom: 4px; }
.prefix { margin-right: 8px; color: #4ecca3; }
.prefix.error { color: #e84545; }
.prefix.success { color: #4ecca3; }
.blink { animation: blink-cursor 1s step-end infinite; }
@keyframes blink-cursor {
  50% { opacity: 0.3; }
}
</style>
```

### Step 4: 重构 `TaskDetail.vue` 为布局容器

覆盖 `E:\Code\jobs\project_1\frontend\src\views\task\TaskDetail.vue`：

```vue
<!-- /frontend/src/views/task/TaskDetail.vue -->
<!-- 职责描述：任务详情页，组合 TaskStatusHeader + StepTimeline + TerminalLogViewer -->

<template>
  <div>
    <el-button text @click="$router.back()">← 返回</el-button>
    <h2>{{ store.task?.title || '任务详情' }}</h2>

    <div v-loading="store.loading">
      <TaskStatusHeader style="margin-top: 16px" />

      <div class="detail-grid" style="display: flex; gap: 16px; margin-top: 16px">
        <div style="flex: 1">
          <StepTimeline />
        </div>
        <div style="flex: 1">
          <TerminalLogViewer />

          <el-card v-if="store.task?.finalOutput" style="margin-top: 16px" header="最终输出">
            <pre class="output">{{ store.task.finalOutput }}</pre>
          </el-card>
        </div>
      </div>
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
.output {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
  white-space: pre-wrap;
  max-height: 400px;
  overflow-y: auto;
}
</style>
```

### 验证

执行 `vue-tsc --noEmit` 和 `npm run build`
