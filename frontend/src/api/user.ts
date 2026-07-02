// /frontend/src/api/user.ts
// 职责描述：用户资料相关 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { UserVO } from '../types/user'

export function getProfile() {
  return request.get<any, ApiResponse<UserVO>>('/api/user/profile')
}

export function updateProfile(email: string) {
  return request.put<any, ApiResponse<void>>('/api/user/profile', { email })
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request.put<any, ApiResponse<void>>('/api/user/password', { oldPassword, newPassword })
}
