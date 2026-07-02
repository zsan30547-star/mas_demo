// /frontend/src/types/api.ts
// 职责描述：全局 API 响应类型定义

/** 统一 API 响应包裹 */
export interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

/** 分页响应 */
export interface PageResult<T> {
  page: number
  size: number
  total: number
  records: T[]
}
