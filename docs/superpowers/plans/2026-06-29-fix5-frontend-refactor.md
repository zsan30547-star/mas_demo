# FIX-5: 前端深度重构与体验升级 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 对 Vue3 前端进行全方位重构——建立严格 TypeScript 类型体系、Pinia 状态管理下沉、核心页面组件化拆分、UI/UX 视觉升级。

**Architecture:** 自底向上四阶段推进：类型基建 → 状态管理 → 组件拆解 → 视觉升级。每阶段产出独立可验证的增量，后续阶段构建在前一阶段之上。

**Tech Stack:** Vue 3.4+ TypeScript + Vite 5 + Element Plus 2.14 + Pinia 3 + vuedraggable 4 + Axios 1.18

## Global Constraints

- TypeScript strict mode 已启用（`tsconfig.app.json`），所有新代码必须通过 `vue-tsc` 类型检查
- 编译验证命令：`npm run build`（前端目录）
- API 响应统一格式：`{ code: number; data: T; message: string }`
- 现有页面路由和组件结构保持不变，重构为内部拆分，不改变 URL
- Element Plus 组件库保持不变，仅增强样式
- 禁止引入新的第三方依赖（已有依赖：vue, vue-router, pinia, element-plus, axios, vuedraggable）

---

### Task 1: 新建集中化 TypeScript 类型声明

**Files:**
- Create: `frontend/src/types/api.ts`
- Create: `frontend/src/types/agent.ts`
- Create: `frontend/src/types/workflow.ts`
- Create: `frontend/src/types/task.ts`
- Create: `frontend/src/types/model.ts`
- Create: `frontend/src/types/user.ts`

**Interfaces:**
- Produces: `ApiResponse<T>`, `PageResult<T>`, `AgentVO`, `AgentCreateReq`, `WorkflowVO`, `WorkflowStep`, `WorkflowCreateReq`, `TaskVO`, `StepLogVO`, `TaskDetail`, `TaskSubmitReq`, `ModelConfigVO`, `ModelConfigCreateReq`, `LoginReq`, `RegisterReq`, `LoginRes`

- [ ] **Step 1: 创建统一 API 响应类型 `api.ts`**

```typescript
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
```

- [ ] **Step 2: 创建 Agent 类型 `agent.ts`**

```typescript
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
```

- [ ] **Step 3: 创建工作流类型 `workflow.ts`**

```typescript
// /frontend/src/types/workflow.ts
// 职责描述：工作流模板相关类型定义

export interface WorkflowStep {
  agentId: number
  order: number
  inputTemplate: string
}

export interface WorkflowVO {
  id: number
  name: string
  description: string
  steps: string
  isPreset: number
  createdAt: string
}

export interface WorkflowCreateReq {
  name: string
  description?: string
  steps: string
}
```

- [ ] **Step 4: 创建任务类型 `task.ts`**

```typescript
// /frontend/src/types/task.ts
// 职责描述：任务相关类型定义

export interface TaskVO {
  id: number
  title: string
  status: string
  inputData: string
  finalOutput: string
  errorMessage: string
  createdAt: string
  finishedAt: string
}

export interface StepLogVO {
  stepIndex: number
  agentName: string
  agentType: string
  input: string
  output: string
  status: string
  durationMs: number
  error: string
}

export interface TaskDetail {
  task: TaskVO
  stepLogs: StepLogVO[]
}

export interface TaskSubmitReq {
  workflowId: number
  title: string
  inputData: Record<string, string>
  fileIds?: number[]
}
```

- [ ] **Step 5: 创建模型类型和用户类型**

```typescript
// /frontend/src/types/model.ts
// 职责描述：模型配置相关类型定义

export interface ModelConfigVO {
  id: number
  name: string
  model: string
  endpoint: string
  isPreset: number
  hasApiKey: boolean
}

export interface ModelConfigCreateReq {
  name: string
  model: string
  endpoint: string
  apiKey?: string
}
```

```typescript
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
```

- [ ] **Step 6: 编译验证**

```bash
cd frontend
npx vue-tsc --noEmit
```

---


### Task 2: API 层类型治理——消除 `any`，统一引用 `src/types/`

**Files:**
- Modify: `frontend/src/api/request.ts` → 增强拦截器
- Modify: `frontend/src/api/auth.ts` → 移除内联类型，引用 `types/user.ts`
- Modify: `frontend/src/api/agent.ts` → 移除内联类型，引用 `types/agent.ts`
- Modify: `frontend/src/api/task.ts` → 移除内联类型，引用 `types/task.ts`
- Modify: `frontend/src/api/workflow.ts` → 移除内联类型，引用 `types/workflow.ts`
- Modify: `frontend/src/api/model.ts` → 移除内联类型，引用 `types/model.ts`

**Interfaces:**
- Consumes: `ApiResponse<T>` from `types/api.ts`, all entity types from `types/`
- Produces: 所有 API 函数返回值从 `{ code: number; data: T }` 改为 `ApiResponse<T>`

- [ ] **Step 1: 增强 `request.ts` 拦截器**

保持现有 Token 注入和 401/403 处理，增加全局 Loading 和类型化响应：

```typescript
// /frontend/src/api/request.ts
// 职责描述：Axios 实例封装，自动带 Token、统一错误提示、401 跳转登录

import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '../types/api'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 30000,
})

// 请求拦截器：自动带 Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理 + 提取 data
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    const msg = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

export default request
```

- [ ] **Step 2: 重构 `auth.ts`**

```typescript
// /frontend/src/api/auth.ts
// 职责描述：登录/注册 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { LoginReq, RegisterReq, LoginRes } from '../types/user'

export function login(data: LoginReq) {
  return request.post<ApiResponse<LoginRes>>('/api/auth/login', data)
}

export function register(data: RegisterReq) {
  return request.post<ApiResponse<number>>('/api/auth/register', data)
}

export function refreshToken(refreshToken: string) {
  return request.post<ApiResponse<LoginRes>>('/api/auth/refresh', { refreshToken })
}
```

- [ ] **Step 3: 重构 `agent.ts`**

```typescript
// /frontend/src/api/agent.ts
// 职责描述：Agent 配置 API

import request from './request'
import axios from 'axios'
import type { ApiResponse } from '../types/api'
import type { AgentVO, AgentCreateReq } from '../types/agent'

const aiEngine = axios.create({ baseURL: 'http://localhost:8000', timeout: 10000 })

export function getAgentList() {
  return request.get<ApiResponse<AgentVO[]>>('/api/agents')
}

export function getAgentPresets() {
  return request.get<ApiResponse<AgentVO[]>>('/api/agents/presets')
}

export function getAgent(id: number) {
  return request.get<ApiResponse<AgentVO>>(`/api/agents/${id}`)
}

export function createAgent(data: AgentCreateReq) {
  return request.post<ApiResponse<number>>('/api/agents', data)
}

export function updateAgent(id: number, data: Partial<AgentCreateReq>) {
  return request.put<ApiResponse<null>>(`/api/agents/${id}`, data)
}

export function deleteAgent(id: number) {
  return request.delete<ApiResponse<null>>(`/api/agents/${id}`)
}

export async function testApiKey(endpoint: string, apiKey: string, model: string) {
  const res = await aiEngine.post<{ valid: boolean; message: string }>(
    '/api/v1/agent/test-key',
    { endpoint, api_key: apiKey, model },
  )
  return res.data as { valid: boolean; message: string }
}
```

- [ ] **Step 4: 重构 `task.ts`**

```typescript
// /frontend/src/api/task.ts
// 职责描述：任务 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { TaskVO, TaskDetail, TaskSubmitReq } from '../types/task'

export function submitTask(data: TaskSubmitReq) {
  return request.post<ApiResponse<{ id: number; status: string }>>('/api/tasks', data)
}

export function getTaskList() {
  return request.get<ApiResponse<TaskVO[]>>('/api/tasks')
}

export function getTaskDetail(id: number) {
  return request.get<ApiResponse<TaskDetail>>(`/api/tasks/${id}`)
}
```

- [ ] **Step 5: 重构 `workflow.ts`**

```typescript
// /frontend/src/api/workflow.ts
// 职责描述：工作流模板 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { WorkflowVO, WorkflowCreateReq } from '../types/workflow'

export function getWorkflowList() {
  return request.get<ApiResponse<WorkflowVO[]>>('/api/workflows')
}

export function getWorkflow(id: number) {
  return request.get<ApiResponse<WorkflowVO>>(`/api/workflows/${id}`)
}

export function createWorkflow(data: WorkflowCreateReq) {
  return request.post<ApiResponse<number>>('/api/workflows', data)
}

export function updateWorkflow(id: number, data: Partial<WorkflowCreateReq>) {
  return request.put<ApiResponse<null>>(`/api/workflows/${id}`, data)
}

export function deleteWorkflow(id: number) {
  return request.delete<ApiResponse<null>>(`/api/workflows/${id}`)
}
```

- [ ] **Step 6: 重构 `model.ts`**

```typescript
// /frontend/src/api/model.ts
// 职责描述：模型配置 API

import request from './request'
import type { ApiResponse } from '../types/api'
import type { ModelConfigVO, ModelConfigCreateReq } from '../types/model'

export function getModelList() {
  return request.get<ApiResponse<ModelConfigVO[]>>('/api/models')
}

export function createModel(data: ModelConfigCreateReq) {
  return request.post<ApiResponse<number>>('/api/models', data)
}

export function updateModel(id: number, data: Partial<ModelConfigCreateReq>) {
  return request.put<ApiResponse<null>>(`/api/models/${id}`, data)
}

export function deleteModel(id: number) {
  return request.delete<ApiResponse<null>>(`/api/models/${id}`)
}
```

- [ ] **Step 7: 更新 View 层 import 路径**

检查所有 `.vue` 文件中直接从 `../../api/xxx` 导入类型的语句，因为类型声明已从 API 文件移到 `types/`。需要更新的文件：

- `views/agent/AgentForm.vue`：`import { createAgent, updateAgent, getAgent } from '../../api/agent'` + `import type { AgentCreateReq } from '../../types/agent'`
- `views/workflow/WorkflowEditor.vue`：`import type { AgentVO } from '../../types/agent'` + `import type { WorkflowStep } from '../../types/workflow'`
- `views/task/TaskDetail.vue`：`import type { TaskVO, StepLogVO } from '../../types/task'`
- `views/task/TaskSubmit.vue`：检查 import 路径
- `views/task/TaskHistory.vue`：检查 import 路径

- [ ] **Step 8: 编译验证**

```bash
cd frontend
npx vue-tsc --noEmit; if ($?) { npm run build }
```

---


### Task 3: 路由守卫增强

**Files:**
- Modify: `frontend/src/router/index.ts:96-103`

**Interfaces:**
- Consumes: `useUserStore` from `stores/user.ts`

- [ ] **Step 1: 改为使用 Pinia userStore + `meta.requireAuth` 白名单**

```typescript
// /frontend/src/router/index.ts (只展示守卫修改部分)

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('../views/login/Login.vue'), meta: { guest: true } },
    { path: '/register', name: 'Register', component: () => import('../views/register/Register.vue'), meta: { guest: true } },
    {
      path: '/',
      component: () => import('../components/layout/AppLayout.vue'),
      redirect: '/dashboard',
      meta: { requireAuth: true },
      children: [
        { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/Dashboard.vue'), meta: { title: '仪表盘', requireAuth: true } },
        { path: 'models', name: 'ModelList', component: () => import('../views/model/ModelConfigList.vue'), meta: { title: '模型管理', requireAuth: true } },
        { path: 'agents', name: 'AgentList', component: () => import('../views/agent/AgentList.vue'), meta: { title: 'Agent 管理', requireAuth: true } },
        { path: 'agents/new', name: 'AgentCreate', component: () => import('../views/agent/AgentForm.vue'), meta: { title: '新建 Agent', requireAuth: true } },
        { path: 'agents/:id/edit', name: 'AgentEdit', component: () => import('../views/agent/AgentForm.vue'), meta: { title: '编辑 Agent', requireAuth: true } },
        { path: 'workflows', name: 'WorkflowList', component: () => import('../views/workflow/WorkflowList.vue'), meta: { title: '工作流模板', requireAuth: true } },
        { path: 'workflows/new', name: 'WorkflowCreate', component: () => import('../views/workflow/WorkflowEditor.vue'), meta: { title: '新建工作流', requireAuth: true } },
        { path: 'workflows/:id/edit', name: 'WorkflowEdit', component: () => import('../views/workflow/WorkflowEditor.vue'), meta: { title: '编辑工作流', requireAuth: true } },
        { path: 'tasks/new', name: 'TaskSubmit', component: () => import('../views/task/TaskSubmit.vue'), meta: { title: '提交任务', requireAuth: true } },
        { path: 'tasks', name: 'TaskHistory', component: () => import('../views/task/TaskHistory.vue'), meta: { title: '任务历史', requireAuth: true } },
        { path: 'tasks/:id', name: 'TaskDetail', component: () => import('../views/task/TaskDetail.vue'), meta: { title: '任务详情', requireAuth: true } },
      ],
    },
  ],
})

// 导航守卫：使用 Pinia store 判断登录状态
router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requireAuth && !userStore.isLoggedIn()) {
    return '/login'
  }
})

export default router
```

- [ ] **Step 2: 编译验证**

```bash
cd frontend
npx vue-tsc --noEmit
```

---


### Task 4: 新建工作流编辑器状态管理 `stores/workflow.ts`

**Files:**
- Create: `frontend/src/stores/workflow.ts`

**Interfaces:**
- Consumes: `WorkflowStep` from `types/workflow.ts`, `AgentVO` from `types/agent.ts`
- Produces: `useWorkflowStore` — `steps`, `agents`, `selectedStepIndex`, `addStep()`, `removeStep()`, `reorderSteps()`, `selectStep()`, `setAgents()`, `loadFromSteps()`, `toSubmitSteps()`

- [ ] **Step 1: 创建 `stores/workflow.ts`**

```typescript
// /frontend/src/stores/workflow.ts
// 职责描述：工作流编辑器状态管理——步骤增删改查、拖拽排序、选中节点

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { WorkflowStep } from '../types/workflow'
import type { AgentVO } from '../types/agent'

export interface StepItem extends WorkflowStep {
  key: number
}

let keyCounter = 0

export const useWorkflowStore = defineStore('workflow', () => {
  const steps = ref<StepItem[]>([])
  const agents = ref<AgentVO[]>([])
  const selectedStepIndex = ref<number | null>(null)
  const name = ref('')
  const description = ref('')

  const selectedStep = computed(() =>
    selectedStepIndex.value !== null ? steps.value[selectedStepIndex.value] : null,
  )

  function addStep() {
    const newStep: StepItem = {
      agentId: agents.value[0]?.id || 0,
      order: steps.value.length + 1,
      inputTemplate: '{{input}}',
      key: ++keyCounter,
    }
    steps.value.push(newStep)
    selectedStepIndex.value = steps.value.length - 1
  }

  function removeStep(index: number) {
    steps.value.splice(index, 1)
    steps.value.forEach((s, i) => (s.order = i + 1))
    if (selectedStepIndex.value !== null && selectedStepIndex.value >= steps.value.length) {
      selectedStepIndex.value = steps.value.length > 0 ? steps.value.length - 1 : null
    }
  }

  function reorderSteps(newOrder: StepItem[]) {
    steps.value = newOrder.map((s, i) => ({ ...s, order: i + 1 }))
  }

  function selectStep(index: number | null) {
    selectedStepIndex.value = index
  }

  function setAgents(list: AgentVO[]) {
    agents.value = list
  }

  function loadFromSteps(rawSteps: WorkflowStep[]) {
    steps.value = rawSteps.map((s) => ({ ...s, key: ++keyCounter }))
  }

  function toSubmitSteps(): WorkflowStep[] {
    return steps.value.map(({ key, ...rest }) => rest)
  }

  function reset() {
    steps.value = []
    selectedStepIndex.value = null
    name.value = ''
    description.value = ''
  }

  return {
    steps, agents, selectedStepIndex, name, description, selectedStep,
    addStep, removeStep, reorderSteps, selectStep, setAgents, loadFromSteps, toSubmitSteps, reset,
  }
})
```

- [ ] **Step 2: 编译验证**

```bash
cd frontend
npx vue-tsc --noEmit
```

---


### Task 5: 新建任务状态管理 `stores/task.ts`

**Files:**
- Create: `frontend/src/stores/task.ts`

**Interfaces:**
- Produces: `useTaskStore` — `task`, `stepLogs`, `activeStep`, `isRunning`, `currentStepIndex`, `fetchTaskDetail()`, `startPolling()`, `stopPolling()`

- [ ] **Step 1: 创建 `stores/task.ts`**

```typescript
// /frontend/src/stores/task.ts
// 职责描述：任务详情状态管理——状态轮询、步骤日志、进度计算

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getTaskDetail } from '../api/task'
import type { TaskVO, StepLogVO } from '../types/task'

export const useTaskStore = defineStore('task', () => {
  const task = ref<TaskVO | null>(null)
  const stepLogs = ref<StepLogVO[]>([])
  const loading = ref(false)
  let pollTimer: ReturnType<typeof setInterval> | null = null

  const activeStep = computed(() =>
    stepLogs.value.filter((s) => s.status === 'success').length,
  )

  const isRunning = computed(() =>
    task.value?.status === 'pending' || task.value?.status === 'running',
  )

  const currentStepIndex = computed(() =>
    stepLogs.value.findIndex((s) => s.status === 'pending' || s.status === 'running'),
  )

  async function fetchTaskDetail(taskId: number) {
    loading.value = true
    try {
      const res = await getTaskDetail(taskId)
      task.value = res.data.task
      stepLogs.value = res.data.stepLogs || []
    } finally {
      loading.value = false
    }
  }

  function startPolling(taskId: number, intervalMs = 3000) {
    stopPolling()
    fetchTaskDetail(taskId)
    pollTimer = setInterval(() => {
      fetchTaskDetail(taskId).then(() => {
        if (!isRunning.value) stopPolling()
      })
    }, intervalMs)
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  return {
    task, stepLogs, loading, activeStep, isRunning, currentStepIndex,
    fetchTaskDetail, startPolling, stopPolling,
  }
})
```

- [ ] **Step 2: 编译验证**

```bash
cd frontend
npx vue-tsc --noEmit
```

---


### Task 6: 增强用户 Store 类型约束

**Files:**
- Modify: `frontend/src/stores/user.ts`

- [ ] **Step 1: 增强 `stores/user.ts`**

```typescript
// /frontend/src/stores/user.ts
// 职责描述：用户状态管理——Token、用户名、登录/登出/刷新

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('accessToken') || '')
  const refreshTokenVal = ref(localStorage.getItem('refreshToken') || '')
  const username = ref('')

  const isLoggedIn = computed(() => !!token.value)

  function setToken(accessToken: string, refreshToken: string) {
    token.value = accessToken
    refreshTokenVal.value = refreshToken
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
  }

  function setUsername(name: string) {
    username.value = name
  }

  function logout() {
    token.value = ''
    refreshTokenVal.value = ''
    username.value = ''
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return { token, refreshTokenVal, username, isLoggedIn, setToken, setUsername, logout }
})
```

- [ ] **Step 2: 编译验证**

```bash
cd frontend
npx vue-tsc --noEmit; if ($?) { npm run build }
```

---


### Task 7: 重构工作流编辑器 WorkflowEditor.vue —— 使用 Store + 拆分为子组件

**Files:**
- Create: `frontend/src/components/workflow/StepDraggableList.vue`
- Create: `frontend/src/components/workflow/AgentConfigPanel.vue`
- Modify: `frontend/src/views/workflow/WorkflowEditor.vue`

**Interfaces:**
- Consumes: `useWorkflowStore` from `stores/workflow.ts`
- Produces: 两个新子组件

- [ ] **Step 1: 创建 `StepDraggableList.vue`**

```vue
<!-- /frontend/src/components/workflow/StepDraggableList.vue -->
<!-- 职责描述：工作流步骤拖拽排序列表 -->

<template>
  <div class="step-list">
    <div v-for="(step, idx) in store.steps" :key="step.key" class="step-item" :class="{ active: store.selectedStepIndex === idx }" @click="store.selectStep(idx)">
      <div class="step-header">
        <span class="step-index">步骤 {{ idx + 1 }}</span>
        <div>
          <el-button text type="primary" size="small" @click.stop="store.selectStep(idx)">配置</el-button>
          <el-button text type="danger" size="small" @click.stop="store.removeStep(idx)">删除</el-button>
        </div>
      </div>
      <div class="step-body">
        <div class="step-agent">{{ stepAgentLabel(idx) }}</div>
        <div class="step-template">{{ step.inputTemplate }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useWorkflowStore } from '../../stores/workflow'

const store = useWorkflowStore()

function stepAgentLabel(idx: number): string {
  const agent = store.agents.find((a) => a.id === store.steps[idx]?.agentId)
  return agent?.name || '未选择 Agent'
}
</script>

<style scoped>
.step-list { display: flex; flex-direction: column; gap: 8px; }
.step-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}
.step-item:hover { border-color: #409eff; }
.step-item.active { border-color: #409eff; background: #ecf5ff; }
.step-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.step-index { font-weight: 600; color: #409eff; }
.step-agent { font-size: 13px; color: #606266; margin-bottom: 4px; }
.step-template { font-size: 12px; color: #909399; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
```

- [ ] **Step 2: 创建 `AgentConfigPanel.vue`**

```vue
<!-- /frontend/src/components/workflow/AgentConfigPanel.vue -->
<!-- 职责描述：选中步骤的 Agent 配置面板 -->

<template>
  <el-card v-if="store.selectedStep" header="步骤配置" class="config-panel">
    <el-form label-width="80px">
      <el-form-item label="Agent">
        <el-select v-model="store.selectedStep.agentId" placeholder="选择 Agent" filterable style="width: 100%">
          <el-option v-for="a in store.agents" :key="a.id" :label="a.name" :value="a.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="输入模板">
        <el-input v-model="store.selectedStep.inputTemplate" type="textarea" :rows="3" placeholder="如：将以下课题拆解为子任务：{{input}}" />
      </el-form-item>
    </el-form>
  </el-card>
  <el-empty v-else description="点击左侧步骤进行配置" :image-size="80" style="margin-top: 40px" />
</template>

<script setup lang="ts">
import { useWorkflowStore } from '../../stores/workflow'
const store = useWorkflowStore()
</script>

<style scoped>
.config-panel { min-height: 200px; }
</style>
```

- [ ] **Step 3: 重构 `WorkflowEditor.vue` 为布局容器**

```vue
<!-- /frontend/src/views/workflow/WorkflowEditor.vue -->
<!-- 职责描述：工作流编辑器主页面，组合 StepDraggableList + AgentConfigPanel -->

<template>
  <div class="editor">
    <h2>{{ isEdit ? '编辑工作流' : '新建工作流' }}</h2>

    <el-card style="margin-top: 20px">
      <el-form label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="store.name" placeholder="工作流名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="store.description" placeholder="描述（可选）" />
        </el-form-item>
      </el-form>
    </el-card>

    <div class="editor-body" style="display: flex; gap: 16px; margin-top: 16px">
      <div class="editor-left" style="flex: 1">
        <el-card header="步骤列表">
          <StepDraggableList />
          <el-button type="primary" plain @click="store.addStep" style="margin-top: 12px; width: 100%">+ 添加步骤</el-button>

          <el-collapse style="margin-top: 16px">
            <el-collapse-item title="占位符说明">
              <div class="placeholder-guide">
                <div><code>{{ '{{' }}input}}</code> — 用户提交任务时的原始输入文本</div>
                <div><code>{{ '{{' }}step1.output}}</code> — 步骤1 执行后的输出结果</div>
                <div><code>{{ '{{' }}step2.output}}</code> — 步骤2 执行后的输出结果</div>
                <div><code>{{ '{{' }}stepN.output}}</code> — 步骤N 执行后的输出结果（N 为步骤序号）</div>
                <div style="margin-top: 8px; color: #909399; font-size: 12px">当前步骤只能引用它前面步骤的输出，不能引用后面的步骤或当前步骤。</div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </div>

      <div class="editor-right" style="flex: 1">
        <AgentConfigPanel />
      </div>
    </div>

    <div style="margin-top: 20px">
      <el-button type="primary" :loading="loading" @click="handleSave">保存模板</el-button>
      <el-button @click="$router.back()">取消</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createWorkflow, updateWorkflow, getWorkflow } from '../../api/workflow'
import { getAgentList } from '../../api/agent'
import { useWorkflowStore } from '../../stores/workflow'
import StepDraggableList from '../../components/workflow/StepDraggableList.vue'
import AgentConfigPanel from '../../components/workflow/AgentConfigPanel.vue'

const route = useRoute()
const router = useRouter()
const store = useWorkflowStore()
const isEdit = !!route.params.id
const loading = ref(false)

onMounted(async () => {
  const res = await getAgentList()
  store.setAgents(res.data)

  if (isEdit) {
    const wfRes = await getWorkflow(Number(route.params.id))
    const wf = wfRes.data
    store.name = wf.name
    store.description = wf.description || ''
    const parsed = JSON.parse(wf.steps || '[]')
    store.loadFromSteps(parsed)
  }
})

async function handleSave() {
  if (!store.name) { ElMessage.warning('请输入名称'); return }
  if (!store.steps.length) { ElMessage.warning('请添加至少一个步骤'); return }
  loading.value = true
  try {
    const data = {
      name: store.name,
      description: store.description,
      steps: JSON.stringify(store.toSubmitSteps()),
    }
    if (isEdit) {
      await updateWorkflow(Number(route.params.id), data)
      ElMessage.success('更新成功')
    } else {
      await createWorkflow(data)
      ElMessage.success('创建成功')
    }
    router.push('/workflows')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.placeholder-guide { font-size: 13px; line-height: 2; }
.placeholder-guide code { background: #ecf5ff; color: #409eff; padding: 2px 6px; border-radius: 3px; font-size: 12px; }
</style>
```

- [ ] **Step 4: 编译验证**

```bash
cd frontend
npx vue-tsc --noEmit; if ($?) { npm run build }
```

---


### Task 8: 重构任务详情页 TaskDetail.vue —— 使用 Store + 拆分为子组件

**Files:**
- Create: `frontend/src/components/task/TaskStatusHeader.vue`
- Create: `frontend/src/components/task/StepTimeline.vue`
- Create: `frontend/src/components/task/TerminalLogViewer.vue`
- Modify: `frontend/src/views/task/TaskDetail.vue`

- [ ] **Step 1: 创建 `TaskStatusHeader.vue`**

```vue
<!-- /frontend/src/components/task/TaskStatusHeader.vue -->
<!-- 职责描述：任务详情页顶部状态卡片——任务标题、状态、耗时 -->

<template>
  <el-card v-if="store.task">
    <div class="task-header">
      <h3>{{ store.task.title }}</h3>
      <div class="task-meta">
        <StatusBadge :status="store.task.status" />
        <span>创建时间：{{ store.task.createdAt }}</span>
        <span v-if="store.task.finishedAt">完成时间：{{ store.task.finishedAt }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useTaskStore } from '../../stores/task'
import StatusBadge from '../common/StatusBadge.vue'

const store = useTaskStore()
</script>

<style scoped>
.task-header { display: flex; justify-content: space-between; align-items: center; }
.task-meta { display: flex; gap: 16px; align-items: center; font-size: 13px; color: #909399; }
</style>
```

- [ ] **Step 2: 创建 `StepTimeline.vue`**

```vue
<!-- /frontend/src/components/task/StepTimeline.vue -->
<!-- 职责描述：任务步骤时间轴，垂直 Timeline 展示每个步骤的输入/输出/耗时 -->

<template>
  <el-card header="执行进度">
    <el-steps :active="store.activeStep" finish-status="success" direction="vertical">
      <el-step
        v-for="(log, idx) in store.stepLogs"
        :key="idx"
        :title="`${log.agentName} (${log.agentType})`"
        :status="stepStatus(log)"
      >
        <template #description>
          <div v-if="log.status === 'success' || log.status === 'failed'" class="step-detail">
            <div v-if="log.input"><strong>输入：</strong><pre>{{ log.input }}</pre></div>
            <div v-if="log.output"><strong>输出：</strong><pre>{{ log.output }}</pre></div>
            <div v-if="log.durationMs">⏱ {{ log.durationMs }}ms</div>
            <div v-if="log.error" style="color: #f56c6c">❌ {{ log.error }}</div>
          </div>
          <div v-else-if="log.status === 'running'" style="color: #409eff">▶ 执行中...</div>
          <div v-else style="color: #909399">等待执行...</div>
        </template>
      </el-step>
    </el-steps>
    <el-empty v-if="!store.stepLogs.length" description="暂无执行日志" :image-size="60" />
  </el-card>
</template>

<script setup lang="ts">
import { useTaskStore } from '../../stores/task'
import type { StepLogVO } from '../../types/task'

const store = useTaskStore()

function stepStatus(log: StepLogVO): string {
  if (log.status === 'success') return 'success'
  if (log.status === 'failed') return 'error'
  return 'process'
}
</script>

<style scoped>
.step-detail pre {
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 13px;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}
</style>
```

- [ ] **Step 3: 创建 `TerminalLogViewer.vue`**

```vue
<!-- /frontend/src/components/task/TerminalLogViewer.vue -->
<!-- 职责描述：仿终端实时日志查看器，黑底绿字展示当前执行中的步骤日志 -->

<template>
  <el-card header="执行日志" class="terminal-card">
    <div class="terminal" ref="terminalRef">
      <div v-if="store.task" class="terminal-line">
        <span class="prefix">$</span> 任务 {{ store.task.id }} — {{ store.task.title }}
      </div>
      <div v-if="store.task" class="terminal-line">
        <span class="prefix">&gt;</span> 状态：{{ store.task.status }}
      </div>
      <div v-for="(log, idx) in store.stepLogs" :key="idx" class="terminal-line">
        <span :class="log.status === 'failed' ? 'prefix error' : 'prefix success'">
          {{ log.status === 'failed' ? '✗' : log.status === 'success' ? '✓' : '○' }}
        </span>
        [步骤{{ log.stepIndex }}] {{ log.agentName }}
        <span v-if="log.durationMs"> ({{ log.durationMs }}ms)</span>
      </div>
      <div v-if="store.isRunning" class="terminal-line blink">
        <span class="prefix">▶</span> 执行中...
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { watch, ref, nextTick } from 'vue'
import { useTaskStore } from '../../stores/task'

const store = useTaskStore()
const terminalRef = ref<HTMLElement>()

watch(() => store.stepLogs.length, async () => {
  await nextTick()
  if (terminalRef.value) {
    terminalRef.value.scrollTop = terminalRef.value.scrollHeight
  }
}, { deep: false })
</script>

<style scoped>
.terminal-card { margin-top: 16px; }
.terminal {
  background: #1a1a2e;
  color: #e0e0e0;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  padding: 16px;
  border-radius: 6px;
  max-height: 300px;
  overflow-y: auto;
  line-height: 1.8;
}
.terminal-line { white-space: pre-wrap; }
.prefix { margin-right: 8px; color: #4ecca3; }
.prefix.error { color: #e84545; }
.prefix.success { color: #4ecca3; }
.blink { animation: blink-cursor 1s step-end infinite; }
@keyframes blink-cursor {
  50% { opacity: 0.3; }
}
</style>
```

- [ ] **Step 4: 重构 `TaskDetail.vue` 为布局容器**

```vue
<!-- /frontend/src/views/task/TaskDetail.vue -->
<!-- 职责描述：任务详情页，组合 TaskStatusHeader + StepTimeline + TerminalLogViewer -->

<template>
  <div>
    <el-button text @click="$router.back()">← 返回</el-button>
    <h2>{{ store.task?.title || '任务详情' }}</h2>

    <div v-loading="store.loading">
      <TaskStatusHeader style="margin-top: 16px" />

      <div class="detail-grid" style="display: flex; gap: 16px; margin-top: 16px">
        <div style="flex: 1">
          <StepTimeline />
        </div>
        <div style="flex: 1">
          <TerminalLogViewer />

          <el-card v-if="store.task?.finalOutput" style="margin-top: 16px" header="最终输出">
            <pre class="output">{{ store.task.finalOutput }}</pre>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useTaskStore } from '../../stores/task'
import TaskStatusHeader from '../../components/task/TaskStatusHeader.vue'
import StepTimeline from '../../components/task/StepTimeline.vue'
import TerminalLogViewer from '../../components/task/TerminalLogViewer.vue'

const route = useRoute()
const store = useTaskStore()

onMounted(() => {
  store.startPolling(Number(route.params.id))
})

onUnmounted(() => {
  store.stopPolling()
})
</script>

<style scoped>
.output {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
  white-space: pre-wrap;
  max-height: 400px;
  overflow-y: auto;
}
</style>
```

- [ ] **Step 5: 编译验证**

```bash
cd frontend
npx vue-tsc --noEmit; if ($?) { npm run build }
```

---


### Task 9: CSS 变量体系 + 暗黑模式基础

**Files:**
- Create: `frontend/src/styles/variables.scss`

- [ ] **Step 1: 创建 CSS 变量文件**

```scss
// /frontend/src/styles/variables.scss
// 职责描述：全局 CSS 变量——色板、间距、暗黑模式基础

:root {
  // Primary
  --color-primary: #409eff;
  --color-primary-light: #79bbff;
  --color-primary-dark: #337ecc;

  // Surface
  --color-bg: #f5f7fa;
  --color-surface: #ffffff;
  --color-surface-hover: #fafafa;

  // Text
  --color-text-primary: #303133;
  --color-text-regular: #606266;
  --color-text-secondary: #909399;
  --color-text-placeholder: #c0c4cc;

  // Border
  --color-border: #e4e7ed;
  --color-border-light: #ebeef5;

  // Status
  --color-success: #67c23a;
  --color-warning: #e6a23c;
  --color-danger: #f56c6c;
  --color-info: #909399;

  // Shadow
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);

  // Glassmorphism
  --glass-bg: rgba(255, 255, 255, 0.72);
  --glass-blur: 12px;
  --glass-border: rgba(255, 255, 255, 0.3);

  // Radius
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;

  // Transitions
  --transition-fast: 0.15s ease;
  --transition-normal: 0.25s ease;
  --transition-slow: 0.4s ease;
}
```

- [ ] **Step 2: 在 `main.ts` 引入全局样式**

在 `main.ts` 顶部添加 `import './styles/variables.scss'`：

```typescript
// /frontend/src/main.ts
// 职责描述：Vue 应用入口，注册插件并挂载

import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './styles/variables.scss'

const app = createApp(App)

app.use(ElementPlus)
app.use(createPinia())
app.use(router)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
```

- [ ] **Step 3: 编译验证**

```bash
cd frontend
npm run build
```

---


### Task 10: 视觉升级——Glassmorphism 侧边栏 + 过渡动画

**Files:**
- Modify: `frontend/src/components/layout/SideBar.vue` → 引入毛玻璃效果
- Modify: `frontend/src/components/task/StepTimeline.vue` → 步骤切换过渡动画
- Modify: `frontend/src/components/common/StatusBadge.vue` → 样式增强
- Create: `frontend/src/components/common/MarkdownRenderer.vue` → Markdown 渲染组件

- [ ] **Step 1: 改造 `SideBar.vue` 毛玻璃效果**

```vue
<!-- /frontend/src/components/layout/SideBar.vue -->
<!-- 职责描述：侧边导航栏，毛玻璃质感 -->

<template>
  <el-menu
    :collapse="appStore.sidebarCollapsed"
    :default-active="route.path"
    router
    class="sidebar"
  >
    <el-menu-item index="/dashboard">
      <el-icon><DataAnalysis /></el-icon>
      <span>仪表盘</span>
    </el-menu-item>
    <el-menu-item index="/models">
      <el-icon><Monitor /></el-icon>
      <span>模型管理</span>
    </el-menu-item>
    <el-menu-item index="/agents">
      <el-icon><Setting /></el-icon>
      <span>Agent 管理</span>
    </el-menu-item>
    <el-menu-item index="/workflows">
      <el-icon><Share /></el-icon>
      <span>工作流模板</span>
    </el-menu-item>
    <el-menu-item index="/tasks/new">
      <el-icon><Plus /></el-icon>
      <span>提交任务</span>
    </el-menu-item>
    <el-menu-item index="/tasks">
      <el-icon><List /></el-icon>
      <span>任务历史</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAppStore } from '../../stores/app'
import { DataAnalysis, Monitor, Setting, Share, Plus, List } from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()
</script>

<style scoped>
.sidebar {
  height: 100vh;
  overflow-y: auto;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border-right: 1px solid var(--glass-border);
}
</style>
```

- [ ] **Step 2: 增强 `StatusBadge.vue`**

```vue
<!-- /frontend/src/components/common/StatusBadge.vue -->
<!-- 职责描述：任务状态标签，带过渡动画和图标 -->

<template>
  <el-tag :type="type" size="small" effect="plain" class="status-badge">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: string }>()

const map: Record<string, { type: string; label: string }> = {
  pending: { type: 'info', label: '等待中' },
  running: { type: 'warning', label: '执行中' },
  completed: { type: 'success', label: '已完成' },
  failed: { type: 'danger', label: '失败' },
  success: { type: 'success', label: '成功' },
}

const type = computed(() => map[props.status]?.type || 'info')
const label = computed(() => map[props.status]?.label || props.status)
</script>

<style scoped>
.status-badge {
  transition: all var(--transition-normal);
}
</style>
```

- [ ] **Step 3: 创建 `MarkdownRenderer.vue`**

```vue
<!-- /frontend/src/components/common/MarkdownRenderer.vue -->
<!-- 职责描述：Markdown 内容渲染组件，格式化展示 AI 输出 -->

<template>
  <div class="markdown-body" v-html="rendered" />
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ content: string }>()

const rendered = computed(() => {
  return props.content
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/\n{2,}/g, '<br/><br/>')
    .replace(/\n/g, '<br/>')
})
</script>

<style scoped>
.markdown-body {
  line-height: 1.8;
  color: var(--color-text-primary);
  font-size: 14px;
}
.markdown-body :deep(h1) { font-size: 20px; margin: 16px 0 8px; }
.markdown-body :deep(h2) { font-size: 18px; margin: 14px 0 6px; }
.markdown-body :deep(h3) { font-size: 16px; margin: 12px 0 4px; }
.markdown-body :deep(code) {
  background: #ecf5ff;
  color: #409eff;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
}
.markdown-body :deep(strong) { font-weight: 600; }
.markdown-body :deep(li) { margin-left: 20px; }
</style>
```

- [ ] **Step 4: 编译验证**

```bash
cd frontend
npx vue-tsc --noEmit; if ($?) { npm run build }
```

---


### Task 11: 全量编译验证 + 修复

**Files:**
- 无新文件，检查所有 import 路径和类型引用

- [ ] **Step 1: 完整类型检查**

```bash
cd frontend
npx vue-tsc --noEmit
```

如果报错，根据错误信息修复：
- 确保 `src/stores/user.ts` 中 `isLoggedIn` 从函数改为 `computed` 后，`NavBar.vue` 中调用方式已更新（`userStore.isLoggedIn` 而不是 `userStore.isLoggedIn()`）
- 确保所有 `.vue` 文件中的 import 路径正确（类型从 `types/` 导入，函数从 `api/` 导入）

- [ ] **Step 2: 修复 `NavBar.vue` 可能的调用问题**

在 `NavBar.vue` 中，如果原来代码使用了 `userStore.isLoggedIn()`（函数调用），现在改为 `userStore.isLoggedIn`（computed ref）后，模板中会自动解包，但脚本中需要 `.value`。检查并修正。

- [ ] **Step 3: 完整构建**

```bash
cd frontend
npm run build
```

预期输出：`✓ built in xxx ms`，无错误。

---


## 验证清单

- [ ] `npm run build` 零错误零警告
- [ ] 所有 API 函数返回值类型为 `ApiResponse<T>` 而非 `any`
- [ ] `src/types/` 下 6 个类型文件完备
- [ ] `stores/workflow.ts` 接管工作流编辑器状态
- [ ] `stores/task.ts` 接管任务详情状态 + 轮询
- [ ] `WorkflowEditor.vue` 拆分为 `StepDraggableList.vue` + `AgentConfigPanel.vue`
- [ ] `TaskDetail.vue` 拆分为 `TaskStatusHeader.vue` + `StepTimeline.vue` + `TerminalLogViewer.vue`
- [ ] `SideBar.vue` 引入毛玻璃效果
- [ ] CSS 变量文件 `variables.scss` 已创建并在 `main.ts` 中引入
- [ ] 路由守卫使用 Pinia `userStore.isLoggedIn` 判断登录状态
- [ ] `MarkdownRenderer.vue` 可按需在其他页面复用
