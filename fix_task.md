# 修复与优化清单

## FIX-1: Model 与 Agent 拆分

**现状问题**：模型信息（名称、端点、API Key）直接写在 `agent_config` 表，导致换个模型就要重新创建 Agent，Agent 和模型强耦合。

**目标**：新增独立的 `model_config` 表，Agent 通过 `model_config_id` 引用模型，实现解耦。

### 改动清单

#### 1.1 数据库

| 文件 | 改动 |
|------|------|
| `init.sql` | 新增 `model_config` 表 |
| `init.sql` | `agent_config` 表删掉 `model`、`endpoint`、`api_key_enc` 列，新增 `model_config_id` 列 |

`model_config` 表结构：
```sql
CREATE TABLE model_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(100) NOT NULL COMMENT '模型名称，如 DeepSeek',
    model           VARCHAR(100) NOT NULL COMMENT '模型标识，如 deepseek-chat',
    endpoint        VARCHAR(500) COMMENT 'API端点',
    api_key_enc     VARCHAR(500) COMMENT '加密的API Key',
    is_preset       TINYINT DEFAULT 0 COMMENT '是否系统预置',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
```

#### 1.2 后端 — 实体

| 文件 | 改动 |
|------|------|
| `entity/ModelConfig.java` | **新建**，映射 `model_config` 表 |
| `entity/AgentConfig.java` | 删掉 `model`、`endpoint`、`apiKeyEnc`，新增 `modelConfigId` |

#### 1.3 后端 — Mapper + Service

| 文件 | 改动 |
|------|------|
| `mapper/ModelConfigMapper.java` | **新建** |
| `service/ModelConfigService.java` | **新建** |
| `service/impl/ModelConfigServiceImpl.java` | **新建** |

#### 1.4 后端 — Controller

| 文件 | 改动 |
|------|------|
| `controller/ModelConfigController.java` | **新建**，模型配置 CRUD |
| `controller/AgentController.java` | `AgentCreateRequest` 里 `model`/`endpoint`/`apiKey` 替换为 `modelConfigId` |
| `controller/AgentController.java` | 查询 Agent 时，关联 ModelConfig 填充完整模型信息 |
| `dto/request/AgentCreateRequest.java` | 调整字段 |
| `dto/response/AgentVO.java` | 增加 `modelName`、`modelEndpoint` 等展示字段 |

#### 1.5 后端 — 任务相关

| 文件 | 改动 |
|------|------|
| `controller/TaskController.java` | `buildAgentsConfig` 里通过 `agent.getModelConfigId()` 查 `ModelConfig`，拿解密后的 `endpoint` 和 `api_key` |
| `config/DataInitializer.java` | 先插入 4 个预置模型（DeepSeek/Gemini/Claude/Qwen-VL），再创建 5 个 Agent 时使用 `modelConfigId` 引用 |

#### 1.6 后端 — 数据初始化

内置模型（4 个）：

| 名称 | 模型标识 | 端点 |
|------|----------|------|
| DeepSeek | deepseek-chat | https://api.deepseek.com/v1 |
| Qwen-VL | qwen-vl-plus | https://dashscope.aliyuncs.com/compatible-mode/v1 |
| Gemini | gemini-2.0-flash | https://generativelanguage.googleapis.com/v1beta/openai |
| Claude | claude-3-5-sonnet | https://api.anthropic.com/v1 |

#### 1.7 前端

| 文件 | 改动 |
|------|------|
| `api/agent.ts` | `AgentVO` 加 `modelConfigId`、`modelName` |
| `views/agent/AgentForm.vue` | 表单里的模型选择改成选 ModelConfig（下拉），去掉 API Key/端点的输入 |
| **新建** `views/model/ModelConfigForm.vue` | 模型配置的独立管理页 |
| **新建** `views/model/ModelConfigList.vue` | 模型列表页 |
| `router/index.ts` | 加 ModelConfig 路由 |
| `components/layout/SideBar.vue` | 侧边栏加「模型管理」菜单 |
| `api/model.ts` | **新建**，模型配置 API |

#### 1.8 AI 引擎

```python
# FastAPI 侧不需要改动 — consumer 从 agentsConfig 里拿 model/endpoint/api_key
# 这些字段仍然由 Spring Boot buildAgentsConfig 组装好发出
```

### 实施顺序

1. 数据库：建 `model_config` 表，改 `agent_config` 表
2. 后端实体 + Mapper + Service
3. 后端 Controller + DTO
4. DataInitializer 调整
5. 编译验证后端
6. 前端页面 + API + 路由
7. 编译验证前端
8. 联调

---

## FIX-2: 工作流编辑器占位符说明

**现状问题**：`{{step1.output}}` 这种占位符晦涩难懂，没有提示。

### 改动

| 文件 | 改动 |
|------|------|
| `views/workflow/WorkflowEditor.vue` | 步骤编辑器底部加一个「占位符说明」折叠面板 |

面板内容：
```
─ 占位符说明 ─
  {{input}}          — 用户提交任务时的原始输入文本
  {{step1.output}}   — 步骤1 执行后的输出结果
  {{step2.output}}   — 步骤2 执行后的输出结果
  {{stepN.output}}   — 步骤N 执行后的输出结果

  提示：当前步骤只能引用它前面步骤的输出。
```

---

## FIX-3: API Key 测试失败时显示真实错误信息

**已完成** ✅

`AgentForm.vue` 的 `handleTestKey` catch 块已改为显示 `e.response.data.message` 或 `e.message`，不再固定显示"网络错误"。

---

## FIX-4: 执行进度与步骤日志

**已完成** ✅

`TaskResultConsumer.java` 已改为将 FastAPI 返回的 `stepLogs` 逐条写入 `task_step_log` 表。

---

## FIX-5: 前端深度重构与体验升级

**问题**：前端代码在快速迭代中积累了技术债：API 响应缺少严格类型约束（存在 `any`）；核心页面（工作流编辑器、任务详情）代码臃肿、状态传递层级深；UI 交互较为基础，缺乏现代视觉体验。

**目标**：采取自底向上策略，按「基建/类型 → 状态管理 → 组件拆解 → 视觉提升」四个阶段依次推进，打造企业级、高可维护的前端架构。

### 阶段 1：核心基建与类型治理（TypeScript & API 规范）

| 文件 | 改动 |
|------|------|
| **新建** `src/types/api.ts` | 全局 `ApiResponse<T>`、`PageResult<T>` |
| **新建** `src/types/agent.ts` | Agent 相关接口 |
| **新建** `src/types/workflow.ts` | 工作流步骤、模板接口 |
| **新建** `src/types/task.ts` | 任务状态、步骤日志接口 |
| **新建** `src/types/model.ts` | 模型配置接口 |
| **新建** `src/types/user.ts` | 用户相关接口 |
| `api/request.ts` | 重构拦截器：全局 Loading 状态、401/403 统一处理、Element Plus 错误提示 |
| `api/auth.ts` | 响应类型声明，消除 `any` |
| `api/agent.ts` | 响应类型声明，消除 `any` |
| `api/workflow.ts` | 响应类型声明，消除 `any` |
| `api/task.ts` | 响应类型声明，消除 `any` |
| `api/model.ts` | 响应类型声明，消除 `any` |
| `router/index.ts` | 完善 `beforeEach` 守卫，根据 `meta.requireAuth` 阻断未登录用户 |

### 阶段 2：状态管理下沉（Pinia State Management）

| 文件 | 改动 |
|------|------|
| **新建** `stores/workflow.ts` | 工作流编辑器状态：步骤增删改查、拖拽排序、当前选中节点 |
| **新建** `stores/task.ts` | 任务详情状态：任务轮询/SSE 逻辑下沉，暴露响应式 `taskStatus` / `stepLogs` |
| `stores/user.ts` | 增强类型约束，补充 Action（刷新Token、退出清理） |

### 阶段 3：核心页面深度组件化

| 文件 | 改动 |
|------|------|
| `views/workflow/WorkflowEditor.vue` | 重构为布局容器，只保留编排入口 |
| **新建** `components/workflow/WorkflowCanvas.vue` | 左侧主画布，展示步骤连接线 |
| **新建** `components/workflow/StepDraggableList.vue` | 中间拖拽排序区（`vuedraggable`） |
| **新建** `components/workflow/AgentConfigPanel.vue` | 右侧步骤 Agent 参数配置面板 |
| `views/task/TaskDetail.vue` | 重构为布局容器 |
| **新建** `components/task/TaskStatusHeader.vue` | 任务基础信息与当前大状态 |
| **新建** `components/task/StepTimeline.vue` | 左侧步骤流转时间轴 |
| **新建** `components/task/TerminalLogViewer.vue` | 仿终端实时日志打印组件 |
| **新建** `components/task/MarkdownResultViewer.vue` | 最终报告 Markdown 渲染组件 |

### 阶段 4：现代 UI/UX 与视觉升级

| 文件 | 改动 |
|------|------|
| `styles/variables.scss` | CSS 变量色板提取（Primary/Surface/Background/Border），为暗黑模式做准备 |
| `components/layout/SideBar.vue` | 引入磨砂玻璃质感（Glassmorphism） |
| `components/workflow/StepDraggableList.vue` | `vuedraggable` + `<TransitionGroup>` 流畅过渡动画 |
| `components/task/StepTimeline.vue` | 步骤状态切换过渡动画 |
| `components/common/StatusBadge.vue` | 状态胶囊样式升级 |
| `components/common/MarkdownRenderer.vue` | Markdown 排版美化 |

### 实施顺序

1. 阶段 1：类型与基建（新建 types/ → 重构 api/ → 加固 router/）
2. 阶段 2：状态下沉（新建 stores/workflow.ts + stores/task.ts）
3. 阶段 3：组件拆解（重构 WorkflowEditor → 重构 TaskDetail）
4. 阶段 4：视觉升级（CSS 变量 → 毛玻璃 → 过渡动画）
5. 编译验证前端：`npm run build`
