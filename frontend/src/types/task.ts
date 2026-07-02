// /frontend/src/types/task.ts
// 职责描述：任务相关类型定义

export interface TaskVO {
  id: number
  workflowId: number
  title: string
  status: string
  inputData: string
  finalOutput: string
  errorMessage: string
  createdAt: string
  finishedAt: string
}

export interface StepLogVO {
  stepIndex: number
  agentName: string
  agentType: string
  input: string
  output: string
  status: string
  durationMs: number
  error: string
}

export interface TaskDetail {
  task: TaskVO
  stepLogs: StepLogVO[]
}

export interface TaskSubmitReq {
  workflowId: number
  title: string
  inputData: Record<string, string>
  fileIds?: number[]
}
