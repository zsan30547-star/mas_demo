// /frontend/src/api/agent.ts
// 职责描述：Agent 配置 API

import request from './request'
import axios from 'axios'
import type { ApiResponse } from '../types/api'
import type { AgentVO, AgentCreateReq } from '../types/agent'

const aiEngine = axios.create({ baseURL: 'http://localhost:8000', timeout: 10000 })

export function getAgentList() {
  return request.get<any, ApiResponse<AgentVO[]>>('/api/agents')
}

export function getAgentPresets() {
  return request.get<any, ApiResponse<AgentVO[]>>('/api/agents/presets')
}

export function getAgent(id: number) {
  return request.get<any, ApiResponse<AgentVO>>(`/api/agents/${id}`)
}

export function createAgent(data: AgentCreateReq) {
  return request.post<any, ApiResponse<number>>('/api/agents', data)
}

export function updateAgent(id: number, data: Partial<AgentCreateReq>) {
  return request.put<any, ApiResponse<null>>(`/api/agents/${id}`, data)
}

export function deleteAgent(id: number) {
  return request.delete<any, ApiResponse<null>>(`/api/agents/${id}`)
}

export async function testApiKey(endpoint: string, apiKey: string, model: string) {
  const res = await aiEngine.post<{ valid: boolean; message: string }>(
    '/api/v1/agent/test-key',
    { endpoint, api_key: apiKey, model },
  )
  return res.data as { valid: boolean; message: string }
}
