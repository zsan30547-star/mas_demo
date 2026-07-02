// /frontend/src/api/credential.ts
// 职责描述：API 凭证配置 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { ApiCredentialVO, ApiCredentialCreateReq } from '../types/credential'

export function getCredentialList() {
  return request.get<any, ApiResponse<ApiCredentialVO[]>>('/api/credentials')
}

export function createCredential(data: ApiCredentialCreateReq) {
  return request.post<any, ApiResponse<number>>('/api/credentials', data)
}

export function updateCredential(id: number, data: Partial<ApiCredentialCreateReq>) {
  return request.put<any, ApiResponse<null>>('/api/credentials/' + id, data)
}

export function deleteCredential(id: number) {
  return request.delete<any, ApiResponse<null>>('/api/credentials/' + id)
}

export function testCredential(id: number, model: string) {
  return request.post<any, ApiResponse<{ valid: boolean; message: string }>>('/api/credentials/' + id + '/test', { model })
}
