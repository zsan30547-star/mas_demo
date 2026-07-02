// /frontend/src/api/workflow.ts
// 职责描述：工作流模板 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { WorkflowVO, WorkflowCreateReq } from '../types/workflow'

export function getWorkflowList() {
  return request.get<any, ApiResponse<WorkflowVO[]>>('/api/workflows')
}

export function getWorkflow(id: number) {
  return request.get<any, ApiResponse<WorkflowVO>>(`/api/workflows/${id}`)
}

export function createWorkflow(data: WorkflowCreateReq) {
  return request.post<any, ApiResponse<number>>('/api/workflows', data)
}

export function updateWorkflow(id: number, data: Partial<WorkflowCreateReq>) {
  return request.put<any, ApiResponse<null>>(`/api/workflows/${id}`, data)
}

export function deleteWorkflow(id: number) {
  return request.delete<any, ApiResponse<null>>(`/api/workflows/${id}`)
}
