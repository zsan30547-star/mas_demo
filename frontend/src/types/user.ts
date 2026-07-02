// /frontend/src/types/user.ts
// 职责描述：用户认证相关类型定义

export interface LoginReq {
  username: string
  password: string
}

export interface RegisterReq {
  username: string
  password: string
  email?: string
}

export interface LoginRes {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface UserVO {
  id: number
  username: string
  email: string
  avatar?: string
  status: number
  createdAt: string
}
