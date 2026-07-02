// /frontend/src/api/model.ts
// 职责描述：模型配置 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { ModelConfigVO, ModelConfigCreateReq } from '../types/model'

export function getModelList() {
  return request.get<any, ApiResponse<ModelConfigVO[]>>('/api/models')
}

export function createModel(data: ModelConfigCreateReq) {
  return request.post<any, ApiResponse<number>>('/api/models', data)
}

export function updateModel(id: number, data: Partial<ModelConfigCreateReq>) {
  return request.put<any, ApiResponse<null>>(`/api/models/${id}`, data)
}

export function deleteModel(id: number) {
  return request.delete<any, ApiResponse<null>>(`/api/models/${id}`)
}
