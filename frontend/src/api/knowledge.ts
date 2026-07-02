// /frontend/src/api/knowledge.ts
// 职责描述：知识库相关 API

import request from './request'
import type { ApiResponse } from '../types/api'

export function uploadDoc(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, ApiResponse<{ docId: string; fileName: string; chars: number }>>(
    '/api/knowledge/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }
  )
}
