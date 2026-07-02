// /frontend/src/api/task.ts
// 职责描述：任务 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { TaskVO, TaskDetail, TaskSubmitReq } from '../types/task'

export function submitTask(data: TaskSubmitReq) {
  return request.post<any, ApiResponse<{ id: number; status: string }>>('/api/tasks', data)
}

export function getTaskList(page = 1, size = 10) {
  return request.get<any, ApiResponse<{
    records: TaskVO[]; total: number; page: number; size: number
  }>>('/api/tasks', { params: { page, size } })
}

export function getTaskDetail(id: number) {
  return request.get<any, ApiResponse<TaskDetail>>(`/api/tasks/${id}`)
}

export function getTaskStats() {
  return request.get<any, ApiResponse<{
    total: number; completed: number; running: number; failed: number; pending: number;
    recent: TaskVO[]
  }>>('/api/tasks/stats')
}
