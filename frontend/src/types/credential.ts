// /frontend/src/types/credential.ts
// 职责描述：API 凭证配置相关类型定义

export interface ApiCredentialVO {
  id: number
  name: string
  endpoint: string
  hasApiKey: boolean
  createdAt: string
}

export interface ApiCredentialCreateReq {
  name: string
  endpoint: string
  apiKey?: string
}
