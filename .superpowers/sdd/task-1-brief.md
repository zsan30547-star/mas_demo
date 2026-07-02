# Task 1: 新建集中化 TypeScript 类型声明

**Goal:** 在 `frontend/src/types/` 目录下创建 6 个类型文件，将分散在各 API 文件中的内联 interface 集中管理。

**Files:**
- Create: `frontend/src/types/api.ts`
- Create: `frontend/src/types/agent.ts`
- Create: `frontend/src/types/workflow.ts`
- Create: `frontend/src/types/task.ts`
- Create: `frontend/src/types/model.ts`
- Create: `frontend/src/types/user.ts`

**Global Constraints:**
- TypeScript strict mode 已启用（`tsconfig.app.json`），所有新代码必须通过 `vue-tsc --noEmit` 类型检查
- 编译验证命令：`npm run build`（在 frontend 目录执行）
- 禁止引入新的第三方依赖

---

### Step 1: 创建统一 API 响应类型 `api.ts`

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

### Step 2: 创建 Agent 类型 `agent.ts`

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

### Step 3: 创建工作流类型 `workflow.ts`

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

### Step 4: 创建任务类型 `task.ts`

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

### Step 5: 创建模型类型和用户类型

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

### Step 6: 编译验证

```bash
cd frontend
npx vue-tsc --noEmit
```

---

**验证标准：**
1. 6 个 `.ts` 文件全部创建在 `frontend/src/types/` 目录下
2. `vue-tsc --noEmit` 零错误
3. 文件头注释遵循项目规范（代码文件首行以 `// 文件路径` 开头 + `// 职责描述`）
