# Task 2: API 层类型治理——消除 `any`，统一引用 `src/types/`

**Goal:** 重构所有 API 文件，将内联的 interface 替换为从 `types/` 目录导入，并为所有请求函数使用 `ApiResponse<T>` 泛型。

**Files to modify:**
- `frontend/src/api/request.ts` → 增强拦截器（添加 ApiResponse 类型引用）
- `frontend/src/api/auth.ts` → 移除内联 LoginReq/RegisterReq/LoginRes，引用 `types/user.ts`
- `frontend/src/api/agent.ts` → 移除内联 AgentVO/AgentCreateReq，引用 `types/agent.ts`
- `frontend/src/api/task.ts` → 移除内联 TaskVO/StepLogVO/TaskDetail/TaskSubmitReq，引用 `types/task.ts`
- `frontend/src/api/workflow.ts` → 移除内联 WorkflowStep/WorkflowVO/WorkflowCreateReq，引用 `types/workflow.ts`
- `frontend/src/api/model.ts` → 移除内联 ModelConfigVO/ModelConfigCreateReq，引用 `types/model.ts`

**Global Constraints:**
- TypeScript strict mode 已启用
- 编译验证命令：`npm run build`（在 frontend 目录执行）
- API 响应统一格式：`{ code: number; data: T; message: string }`
- 禁止引入新的第三方依赖

---

### Step 1: 增强 `request.ts` 拦截器（添加类型引用）

源文件当前位于 `E:\Code\jobs\project_1\frontend\src\api\request.ts`。

请用以下完整内容覆盖该文件：

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

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

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

### Step 2: 重构 `auth.ts`

源文件当前位于 `E:\Code\jobs\project_1\frontend\src\api\auth.ts`。

请用以下完整内容覆盖该文件：

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

### Step 3: 重构 `agent.ts`

源文件当前位于 `E:\Code\jobs\project_1\frontend\src\api\agent.ts`。

请用以下完整内容覆盖该文件（保留 aiEngine 实例和 testApiKey 函数，但替换类型引用）：

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

### Step 4: 重构 `task.ts`

源文件当前位于 `E:\Code\jobs\project_1\frontend\src\api\task.ts`。

请用以下完整内容覆盖该文件：

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

### Step 5: 重构 `workflow.ts`

源文件当前位于 `E:\Code\jobs\project_1\frontend\src\api\workflow.ts`。

请用以下完整内容覆盖该文件：

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

### Step 6: 重构 `model.ts`

源文件当前位于 `E:\Code\jobs\project_1\frontend\src\api\model.ts`。

请用以下完整内容覆盖该文件：

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

### Step 7: 更新 View 层 import 路径

检查并修改 `.vue` 文件中从 `../../api/xxx` 导入类型的语句，改为从 `../../types/xxx` 导入。需要检查的文件：
- `views/agent/AgentForm.vue`
- `views/workflow/WorkflowEditor.vue`
- `views/task/TaskDetail.vue`
- `views/task/TaskSubmit.vue`
- `views/task/TaskHistory.vue`

对于每个文件，如果原来有 `import { ..., type XxxVO, ... } from '../../api/xxx'` 这样的语句，需要保留函数导入，将类型导入改为从 `../../types/xxx` 导入。

例如 AgentForm.vue 中原来的：
`import { createAgent, updateAgent, getAgent, type AgentCreateReq } from '../../api/agent'`
应改为：
`import { createAgent, updateAgent, getAgent } from '../../api/agent'`
`import type { AgentCreateReq } from '../../types/agent'`

### Step 8: 编译验证

在 `E:\Code\jobs\project_1\frontend` 目录执行：
```bash
npx vue-tsc --noEmit
npm run build
```

---

**验证标准：**
1. 6 个 API 文件全部消除内联 interface 声明，改为从 `types/` 导入
2. 所有 API 函数返回值使用 `ApiResponse<T>` 泛型
3. `.vue` 文件中的类型导入从 `api/` 改为 `types/`
4. `vue-tsc --noEmit` 零错误
5. `npm run build` 成功
