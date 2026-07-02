// /frontend/src/types/agent.ts
// 职责描述：Agent 配置相关类型定义

export interface AgentVO {
  id: number
  modelConfigId: number
  name: string
  agentType: string
  modelName: string
  model: string
  systemPrompt: string
  temperature: number
  maxTokens: number
  icon: string
  enabled: number
  isPreset: number
  createdAt: string
}

export interface AgentCreateReq {
  name: string
  agentType: string
  modelConfigId: number
  systemPrompt?: string
  temperature?: number
  maxTokens?: number
  enabled?: number
}
