<!-- /frontend/src/components/task/TerminalLogViewer.vue -->
<!-- 职责描述：干净的终端实时日志查看器（适配双主题） -->

<template>
  <el-card header="执行日志" class="terminal-card" shadow="never">
    <div class="terminal" ref="terminalRef">
      <div v-if="store.task" class="terminal-line">
        <span class="prefix">→</span> Task #{{ store.task.id }} initialized: {{ store.task.title }}
      </div>
      <div v-if="store.task" class="terminal-line">
        <span class="prefix">→</span> Status: {{ store.task.status }}
      </div>
      <div v-for="(log, idx) in store.stepLogs" :key="idx" class="terminal-line">
        <span :class="log.status === 'failed' ? 'prefix error' : 'prefix success'">
          {{ log.status === 'failed' ? '✖' : log.status === 'success' ? '✓' : '○' }}
        </span>
        [Step {{ log.stepIndex }}] {{ log.agentName }}
        <span v-if="log.durationMs" class="duration">({{ log.durationMs }}ms)</span>
      </div>
      <div v-if="store.isRunning" class="terminal-line blink">
        <span class="prefix">⟳</span> Executing...
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
.terminal-card { margin-top: 16px; border: 1px solid var(--color-border-light); }
.terminal {
  background: var(--terminal-bg); /* 跟随暗黑模式动态切换 */
  color: var(--color-text-primary);
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 13px;
  padding: 16px;
  border-radius: var(--radius-sm);
  max-height: 300px;
  overflow-y: auto;
  line-height: 1.8;
  border: 1px solid var(--terminal-border);
  transition: background-color var(--transition-normal);
}
.terminal-line { white-space: pre-wrap; margin-bottom: 4px; display: flex; align-items: flex-start; }
.prefix { margin-right: 8px; color: var(--color-text-placeholder); font-weight: bold; }
.prefix.error { color: var(--color-danger); }
.prefix.success { color: var(--color-success); }
.duration { color: var(--color-text-secondary); margin-left: 6px; }
.blink { animation: blink-cursor 1.5s step-end infinite; color: var(--color-primary); }
@keyframes blink-cursor {
  50% { opacity: 0.4; }
}
</style>
