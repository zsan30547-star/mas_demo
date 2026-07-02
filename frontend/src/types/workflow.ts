// /frontend/src/types/workflow.ts
// 职责描述：工作流模板相关类型定义

export interface WorkflowStep {
  agentId: number
  order: number
  inputTemplate: string
}

export interface WorkflowVO {
  id: number
  name: string
  description: string
  steps: string
  isPreset: number
  createdAt: string
}

export interface WorkflowCreateReq {
  name: string
  description?: string
  steps: string
}
