// /frontend/src/api/file.ts
// 职责描述：文件上传相关 API

import request from './request'
import type { ApiResponse } from '../types/api'

export interface FileRecordVO {
  id: number
  fileName: string
  fileUrl: string
  fileType: string
  fileSize: number
}

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, ApiResponse<FileRecordVO>>('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
