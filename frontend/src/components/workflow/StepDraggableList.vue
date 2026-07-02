<!-- /frontend/src/components/workflow/StepDraggableList.vue -->
<!-- 职责描述：工作流步骤拖拽排序列表，集成 vuedraggable + TransitionGroup 动画 -->

<template>
  <draggable
    v-model="store.steps"
    :item-key="(step: StepItem) => step.key"
    ghost-class="ghost"
    @end="store.reorderSteps(store.steps)"
    tag="div"
    class="step-list"
  >
    <template #item="{ element, index }">
      <TransitionGroup name="step" tag="div" class="step-wrapper">
        <div
          :key="element.key"
          class="step-item"
          :class="{ active: store.selectedStepIndex === index }"
          @click="store.selectStep(index)"
        >
          <div class="step-header">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <span class="step-index">步骤 {{ index + 1 }}</span>
            <div>
              <el-button text type="primary" size="small" @click.stop="store.selectStep(index)">配置</el-button>
              <el-button text type="danger" size="small" @click.stop="store.removeStep(index)">删除</el-button>
            </div>
          </div>
          <div class="step-body">
            <div class="step-agent">{{ stepAgentLabel(index) }}</div>
            <div class="step-template">{{ element.inputTemplate }}</div>
          </div>
        </div>
      </TransitionGroup>
    </template>
  </draggable>
</template>

<script setup lang="ts">
import draggable from 'vuedraggable'
import { Rank } from '@element-plus/icons-vue'
import { useWorkflowStore } from '../../stores/workflow'
import type { StepItem } from '../../stores/workflow'

const store = useWorkflowStore()

function stepAgentLabel(idx: number): string {
  const agent = store.agents.find((a) => a.id === store.steps[idx]?.agentId)
  return agent?.name || '未选择 Agent'
}
</script>

<style scoped>
.step-list { display: flex; flex-direction: column; gap: 8px; }
.step-wrapper { display: contents; }
.step-item {
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 12px;
  background: var(--color-bg);
  cursor: pointer;
  margin-bottom: 8px;
}
.step-item:hover { border-color: var(--color-primary); }
.step-item.active { border-color: var(--color-primary); background: var(--color-primary-bg); }
.step-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.drag-handle { color: var(--color-text-placeholder); cursor: grab; font-size: 16px; }
.drag-handle:active { cursor: grabbing; }
.step-index { font-weight: 600; color: var(--color-primary); flex: 1; }
.step-agent { font-size: 13px; color: var(--color-text-regular); margin-bottom: 4px; }
.step-template { font-size: 12px; color: var(--color-text-placeholder); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* TransitionGroup 动画 */
.step-enter-active,
.step-leave-active { transition: all 0.3s ease; }
.step-enter-from { opacity: 0; transform: translateX(-20px); }
.step-leave-to { opacity: 0; transform: translateX(20px); }
.step-move { transition: transform 0.3s ease; }

/* 拖拽幽灵占位 */
:deep(.ghost) { opacity: 0.4; background: var(--color-primary-bg); border: 2px dashed var(--color-primary); }
</style>
