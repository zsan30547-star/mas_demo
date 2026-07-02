# Task 2: API 层类型治理 — 执行报告

## 状态：DONE

## 修改文件列表

### API 文件（6 个，完全覆盖）
| 文件 | 操作 | 说明 |
|------|------|------|
| `frontend/src/api/request.ts` | 覆盖 | 移除未使用的 ApiResponse 导入，保持拦截器逻辑 |
| `frontend/src/api/auth.ts` | 覆盖 | 移除内联 LoginReq/RegisterReq/LoginRes，从 types/user.ts 导入 |
| `frontend/src/api/agent.ts` | 覆盖 | 移除内联 AgentVO/AgentCreateReq，从 types/agent.ts 导入 |
| `frontend/src/api/task.ts` | 覆盖 | 移除内联 TaskVO/StepLogVO/TaskDetail/TaskSubmitReq，从 types/task.ts 导入 |
| `frontend/src/api/workflow.ts` | 覆盖 | 移除内联 WorkflowStep/WorkflowVO/WorkflowCreateReq，从 types/workflow.ts 导入 |
| `frontend/src/api/model.ts` | 覆盖 | 移除内联 ModelConfigVO/ModelConfigCreateReq，从 types/model.ts 导入 |

### .vue 文件（9 个，修改 import 语句）
| 文件 | 说明 |
|------|------|
| `views/agent/AgentForm.vue` | AgentCreateReq → types/agent, ModelConfigVO → types/model |
| `views/workflow/WorkflowEditor.vue` | AgentVO → types/agent, WorkflowStep → types/workflow |
| `views/task/TaskDetail.vue` | TaskVO, StepLogVO → types/task |
| `views/task/TaskSubmit.vue` | WorkflowVO → types/workflow |
| `views/task/TaskHistory.vue` | TaskVO → types/task |
| `views/dashboard/Dashboard.vue` | TaskVO → types/task（额外发现） |
| `views/model/ModelConfigList.vue` | ModelConfigVO → types/model（额外发现） |
| `views/agent/AgentList.vue` | AgentVO → types/agent（额外发现） |
| `views/workflow/WorkflowList.vue` | WorkflowVO → types/workflow（额外发现） |

## 类型检查和构建结果

### `npx vue-tsc --noEmit`
✅ 零错误通过

### `npm run build`（vue-tsc -b && vite build）
✅ 零 TypeScript 错误，Vite 构建成功，输出 dist 目录

## 遇到的问题

### 1. `request.get<ApiResponse<T>>` 单泛型参数不满足类型推导

Brief 中的 API 函数使用单泛型参数 `request.get<ApiResponse<AgentVO[]>>('/api/agents')`，但 axios 的 `get<T, R>` 有两个泛型参数：
- T：AxiosResponse 的 data 类型（拦截器输入）
- R：最终返回类型（拦截器输出）

由于拦截器 `response => response.data` 在运行时已解包 AxiosResponse，但类型系统无法自动推导此变换，必须显式指定第二个泛型参数：
```typescript
// 修改前（类型错误）
request.get<ApiResponse<AgentVO[]>>('/api/agents')

// 修改后（类型正确）
request.get<any, ApiResponse<AgentVO[]>>('/api/agents')
```

同理，`post`、`put`、`delete` 方法也需要两个泛型参数。

### 2. `noUnusedLocals` 导致 request.ts 中未使用的导入报错

tsconfig.app.json 启用了 `noUnusedLocals: true`，request.ts 中导入 `ApiResponse` 但未使用，导致编译失败。已移除该导入。

### 3. `vue-tsc --noEmit` 与 `vue-tsc -b` 的行为差异

`vue-tsc --noEmit` 零错误通过但 `vue-tsc -b`（build 模式）报错，原因是 build 模式使用项目引用（tsconfig.json → tsconfig.app.json），启用了 `noUnusedLocals: true` 等更严格的检查。后续均应使用 `npm run build`（即 `vue-tsc -b && vite build`）作为标准验证方式。

## 验证标准检查清单

- [x] 6 个 API 文件全部消除内联 interface 声明，改为从 `types/` 导入
- [x] 所有 API 函数返回值使用 `ApiResponse<T>` 泛型
- [x] `.vue` 文件中的类型导入从 `api/` 改为 `types/`
- [x] `npm run build`（vue-tsc -b + vite build）成功
