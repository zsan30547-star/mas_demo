<template>
  <el-card header="执行进度">
    <el-steps :active="store.activeStep" direction="vertical">
      
      <!-- 预加载的工作流执行步骤 (不论是否执行到，都会显示) -->
      <el-step
        v-for="(node, idx) in store.workflowNodes"
        :key="idx"
        :title="getNodeTitle(node)"
        :status="getStepStatus(idx)"
      >
        <template #description>
          <!-- 如果当前步骤有日志记录，则展示详细信息 -->
          <div v-if="getStepLog(idx)" class="step-detail">
            
            <div class="time-meta">
              <span v-if="getStepLog(idx)?.durationMs">⏱ {{ getStepLog(idx)?.durationMs }}ms</span>
            </div>
            
            <div v-if="getStepLog(idx)?.error" class="error-text">❌ {{ getStepLog(idx)?.error }}</div>

            <!-- 无论输入还是输出，只要存在就展示在对应的步骤中 -->
            <el-collapse v-if="getStepLog(idx)?.input || getStepLog(idx)?.output" class="content-collapse">
              <el-collapse-item title="查看执行详情" :name="idx">
                <div v-if="getStepLog(idx)?.input" class="code-block-wrapper">
                  <div class="block-title">输入：</div>
                  <pre>{{ getStepLog(idx)?.input }}</pre>
                </div>
                <div v-if="getStepLog(idx)?.output" class="code-block-wrapper">
                  <div class="block-title">输出：</div>
                  <pre>{{ getStepLog(idx)?.output }}</pre>
                </div>
              </el-collapse-item>
            </el-collapse>

          </div>
          
          <!-- 状态提示文字 -->
          <div v-if="getStepStatus(idx) === 'process'" class="running-text">
            <span>🚀 执行中...</span>
          </div>
          <div v-else-if="getStepStatus(idx) === 'wait'" class="wait-text">
            等待执行...
          </div>
        </template>
      </el-step>

      <!-- 最终结束与汇总节点 -->
      <el-step 
        title="结束 (任务汇总)" 
        :status="store.task?.status === 'completed' ? 'success' : (store.task?.status === 'failed' ? 'error' : 'wait')"
      >
        <template #description>
          <div v-if="store.task?.status === 'completed'" class="step-detail">
            <el-collapse v-if="store.task?.finalOutput" class="content-collapse">
              <el-collapse-item title="查看最终输出结果" name="final">
                <div class="code-block-wrapper">
                  <pre>{{ store.task.finalOutput }}</pre>
                </div>
              </el-collapse-item>
            </el-collapse>
            <div v-else class="success-text">🎉 任务已成功结束，无文本输出。</div>
          </div>
          <div v-else-if="store.task?.status === 'failed'" class="error-text">
            任务执行失败，已非正常结束。
          </div>
        </template>
      </el-step>

    </el-steps>
  </el-card>
</template>

<script setup lang="ts">
import { useTaskStore } from '../../stores/task'
import type { StepLogVO } from '../../types/task'

const store = useTaskStore()

// 获取节点标题 (从配置中获取 Agent 的信息或使用默认值)
function getNodeTitle(node: any): string {
  // 如果后端能够返回 agentName，可以在这里解析，否则显示第几步
  return `Agent 节点 ${node.agentId || '...'}` 
}

// 尝试从实时日志中匹配当前节点的执行记录
function getStepLog(index: number): StepLogVO | undefined {
  // 假设 stepLogs 中的 stepIndex 与 workflowNodes 中的索引一致 (从 0 开始或顺序一致)
  // 如果后端的 stepIndex 从 1 开始，可能需要用 index + 1
  return store.stepLogs.find(log => (log.stepIndex === index + 1) || (log.stepIndex === index))
}

// 动态计算每个步骤的 UI 状态
function getStepStatus(index: number): 'wait' | 'process' | 'success' | 'error' {
  const log = getStepLog(index)
  if (log) {
    if (log.status === 'success') return 'success'
    if (log.status === 'failed') return 'error'
    if (log.status === 'running' || log.status === 'pending') return 'process'
  }
  return 'wait'
}
</script>

<style scoped>
/* 覆盖 Element Plus 步骤条颜色 */
:deep(.el-step__head.is-process) {
  color: var(--color-primary);
  border-color: var(--color-primary);
}
:deep(.el-step__title.is-process) {
  color: var(--color-primary);
  font-weight: bold;
}
:deep(.el-step__head.is-success) {
  color: var(--color-success);
  border-color: var(--color-success);
}
:deep(.el-step__title.is-success) {
  color: var(--color-success);
}
:deep(.el-step__head.is-wait),
:deep(.el-step__title.is-wait) {
  color: var(--color-text-placeholder);
  border-color: var(--color-text-placeholder);
}

.step-detail { margin-top: 8px; }
.time-meta { color: var(--color-text-secondary); font-size: 13px; margin-bottom: 8px; }
.running-text { color: var(--color-primary); margin-top: 8px; font-weight: 500; }
.wait-text { color: var(--color-text-placeholder); margin-top: 8px; }
.success-text { color: var(--color-success); margin-top: 8px; }
.error-text { color: var(--color-danger); margin-top: 8px; }

/* 折叠面板样式优化 */
.content-collapse {
  margin-top: 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border-light);
}
:deep(.el-collapse-item__header) {
  height: 40px;
  line-height: 40px;
  padding: 0 12px;
  background-color: var(--color-bg);
  border-radius: var(--radius-sm);
  color: var(--color-text-regular);
  font-size: 13px;
  border-bottom: none;
}
:deep(.el-collapse-item__wrap) {
  border-bottom: none;
}
:deep(.el-collapse-item__content) {
  padding: 12px;
  background-color: #fff;
}

.code-block-wrapper { margin-bottom: 12px; }
.code-block-wrapper:last-child { margin-bottom: 0; }
.block-title {
  font-weight: 600;
  margin-bottom: 6px;
  color: var(--color-text-primary);
  font-size: 13px;
}
.code-block-wrapper pre {
  background: var(--color-bg);
  padding: 12px;
  border-radius: 6px;
  font-size: 13px;
  white-space: pre-wrap;
  max-height: 350px;
  overflow-y: auto;
  border: 1px solid var(--color-border-light);
  margin: 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}
</style>