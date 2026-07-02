// /frontend/src/stores/workflow.ts
// 职责描述：工作流编辑器状态管理——步骤增删改查、拖拽排序、选中节点

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { WorkflowStep } from '../types/workflow'
import type { AgentVO } from '../types/agent'

export interface StepItem extends WorkflowStep {
  key: number
}

let keyCounter = 0

export const useWorkflowStore = defineStore('workflow', () => {
  const steps = ref<StepItem[]>([])
  const agents = ref<AgentVO[]>([])
  const selectedStepIndex = ref<number | null>(null)
  const name = ref('')
  const description = ref('')

  const selectedStep = computed(() =>
    selectedStepIndex.value !== null ? steps.value[selectedStepIndex.value] : null,
  )

  function addStep() {
    const newStep: StepItem = {
      agentId: agents.value[0]?.id || 0,
      order: steps.value.length + 1,
      inputTemplate: '$i',
      key: ++keyCounter,
    }
    steps.value.push(newStep)
    selectedStepIndex.value = steps.value.length - 1
  }

  function removeStep(index: number) {
    steps.value.splice(index, 1)
    steps.value.forEach((s, i) => (s.order = i + 1))
    if (selectedStepIndex.value !== null && selectedStepIndex.value >= steps.value.length) {
      selectedStepIndex.value = steps.value.length > 0 ? steps.value.length - 1 : null
    }
  }

  function reorderSteps(newOrder: StepItem[]) {
    steps.value = newOrder.map((s, i) => ({ ...s, order: i + 1 }))
  }

  function selectStep(index: number | null) {
    selectedStepIndex.value = index
  }

  function setAgents(list: AgentVO[]) {
    agents.value = list
  }

  function loadFromSteps(rawSteps: WorkflowStep[]) {
    steps.value = rawSteps.map((s) => ({ ...s, key: ++keyCounter }))
  }

  function toSubmitSteps(): WorkflowStep[] {
    return steps.value.map(({ key, ...rest }) => rest)
  }

  function reset() {
    steps.value = []
    selectedStepIndex.value = null
    name.value = ''
    description.value = ''
  }

  return {
    steps, agents, selectedStepIndex, name, description, selectedStep,
    addStep, removeStep, reorderSteps, selectStep, setAgents, loadFromSteps, toSubmitSteps, reset,
  }
})
