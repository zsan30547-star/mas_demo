// /frontend/src/api/auth.ts
// 职责描述：登录/注册 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { LoginReq, RegisterReq, LoginRes } from '../types/user'

export function login(data: LoginReq) {
  return request.post<any, ApiResponse<LoginRes>>('/api/auth/login', data)
}

export function register(data: RegisterReq) {
  return request.post<any, ApiResponse<number>>('/api/auth/register', data)
}

export function refreshToken(refreshToken: string) {
  return request.post<any, ApiResponse<LoginRes>>('/api/auth/refresh', { refreshToken })
}

export function logout() {
  return request.post<any, ApiResponse<void>>('/api/auth/logout')
}
