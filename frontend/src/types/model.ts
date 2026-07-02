// /frontend/src/types/model.ts
// 职责描述：模型配置相关类型定义

export interface ModelConfigVO {
  id: number
  name: string
  model: string
  apiCredentialId: number | null
  isPreset: number
}

export interface ModelConfigCreateReq {
  name: string
  model: string
  apiCredentialId: number | null
}
