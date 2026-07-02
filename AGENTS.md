# 多模型智能体协作平台 — AI 行为规范

> 项目级 AGENTS.md，覆盖全局约定 + 本项目特有规则
> 所有开发工作以 `任务书.md` 为准

---

## 一、核心约束

### 1. 遵守任务书

- 所有开发工作必须以 `任务书.md` 为准
- 修改功能前先查阅任务书，确认当前开发的模块和接口定义
- 若发现任务书与实际开发有冲突，先跟用户确认再修改

### 2. 代码注释规范（三端统一）

#### 2.1 文件头注释

每个文件顶部必须有文件头注释，说明文件职责。

**Java**：
```java
// /backend/src/main/java/com/agent/platform/controller/AgentController.java
// 职责描述：Agent 配置的 HTTP 请求处理
```

**Python**：
```python
# /ai-engine/app/agents/planner_agent.py
# 职责描述：规划 Agent，负责任务拆解和计划制定
```

**TypeScript/Vue**：
```typescript
// /frontend/src/views/agent/AgentList.vue
// 职责描述：Agent 列表页面，卡片式展示所有 Agent 配置
```

#### 2.2 函数/方法注释

每条函数/方法前面必须有注释，描述职责、参数、返回值。三端风格统一。

```java
// springboot的单个函数功能
// 根据用户ID查询Agent配置列表，返回值 List<AgentConfig>
// @param userId 用户ID
public List<AgentConfig> getAgentsByUser(Long userId)
{
    // 主要程序
}
```

```python
# 根据用户ID查询Agent配置列表，返回值 List[AgentConfig]
# @param user_id 用户ID
def get_agents_by_user(user_id: int) -> list[AgentConfig]:
    # 主要程序
    return []
```

```typescript
// 根据用户ID查询Agent配置列表，返回值 Promise<AgentConfig[]>
// @param userId 用户ID
async function getAgentsByUser(userId: number): Promise<AgentConfig[]>
{
    // 主要程序
}
```

#### 2.3 行内注释

关键业务逻辑、复杂条件判断处加行内注释说明意图。

#### 2.4 Python 类型注解

所有 Python 函数必须包含类型注解（函数/方法级别）。

### 3. 编译验证规范

每次编写/修改代码后，必须执行对应端的编译验证：

| 端 | 命令 | 说明 |
|----|------|------|
| Spring Boot 后端 | `mvn compile` | 编译 Java 代码 |
| Vue3 前端 | `npm run build` | 构建前端 |
| FastAPI AI 引擎 | `python -m py_compile {file}` | 逐个文件语法检查 |

- **编译器警告**：不影响正常运行的警告可忽略，严重警告需修复
- **例外**：允许残留 `TODO/FIXME`

### 4. 代码架构规范

#### Spring Boot（严格分层）

Controller → Service(接口) → ServiceImpl(实现) → Mapper(Mybatis-Plus)

- Controller 层只做参数校验和路由转发，不写业务逻辑
- Service 层只写接口定义
- ServiceImpl 层写业务逻辑实现
- Entity 统一放在 `entity` 包
- DTO 分 `dto/request/` 和 `dto/response/` 子包
- Mapper 继承 Mybatis-Plus `BaseMapper`

#### FastAPI（按功能模块分包）

api/ → 路由定义 | agents/ → Agent 处理器 | workflows/ → Langgraph 工作流 | services/ → 业务服务 | core/ → 基础设施

#### Vue3（按视图/组件分包）

views/ → 页面组件 | components/ → 可复用组件 | api/ → Axios 封装 | stores/ → Pinia | router/ → 路由

### 5. API 设计规范

| 规则 | 说明 |
|------|------|
| 风格 | RESTful |
| URL 命名 | 资源用复数名词：/api/agents、/api/workflows、/api/tasks |
| HTTP 方法 | GET=查询、POST=创建、PUT=更新、DELETE=删除 |
| 状态码 | 200=成功、201=创建成功、400=参数错误、401=未认证、403=无权限、404=未找到、500=服务端错误 |
| 统一响应格式 | { "code": 200, "data": {}, "message": "ok" } |
| 分页响应格式 | { "page": 1, "size": 10, "total": 100, "records": [] } |
| 异常处理 | 全局 @RestControllerAdvice 统一捕获 |

### 6. 代码严谨性规范

| 规则 | 说明 |
|------|------|
| 注解语义检查 | Mybatis-Plus 注解（@TableLogic、@TableField 等）必须确认默认值与实际字段含义一致后再使用 |
| 字段约束检查 | Entity 字段类型、长度、默认值必须与 init.sql 建表语句对齐 |
| 外键关联检查 | 插入数据前确认关联的记录已存在，尤其是 DataInitializer 等初始化代码 |
| API 响应安全 | API Key、密码等敏感字段在响应体中必须过滤，使用 VO 类而非直接返回 Entity |
| 空指针防护 | 查询结果使用前必须判空，避免 NullPointerException |
| 变更影响检查 | 修改已有代码前，先通过 CodeGraph impact 分析影响范围 |

### 7. 索引同步规范

每完成一个 Phase 后，必须执行 CodeGraph 索引同步：

```bash
codegraph init -i
```

### 8. 安全规范

| 规则 | 说明 |
|------|------|
| API Key | 存入数据库时 AES-256-CBC 加密，API 响应中不回传 |
| JWT | access_token 有效期 2h，refresh_token 有效期 7d |
| 密码 | BCrypt 加密存储 |
| 日志 | 禁止输出密钥、Token、密码到日志或控制台 |
| SQL 注入 | 使用 Mybatis-Plus 参数绑定，禁止拼接 SQL |

---

## 二、交互规则

### 需要先确认再执行的操作

| 操作 | 是否需要确认 |
|------|------------|
| 创建新文件 | ? 自主执行 |
| 修改已有代码 | ? 自主执行 |
| 删除文件/代码 | ? 必须先问 |
| 重构已有代码 | ? 必须先问 |
| 修改数据库表结构 | ? 必须先问 |
| 修改工作区外文件 | ? 必须先问 |
| 安装/修改依赖 | ? 自主执行（保证可正常安装） |

---

## 三、Git 工作流规范

| 规则 | 说明 |
|------|------|
| 提交时机 | 按 Phase 分阶段提交（如 P3: Agent CRUD），不零碎提交 |
| Commit 格式 | {Phase}: {描述}，语义化前缀：feat/fix/docs/refactor |
| 提交前检查 | 执行 mvn compile / npm run build 确保编译通过 |
| 禁止操作 | 不得 force push、不得修改已推送的 commit |
| 提交须用户授权 | 提交前必须给用户看 diff，确认后才执行 git commit |

---

## 四、可用工具与 Skill 使用指南

### 4.1 MCP 工具使用策略

| 场景 | 首选工具 | 说明 |
|------|----------|------|
| 理解代码上下文 | **CodeGraph explore** | 问"这是怎么工作的"、"这个架构是什么"时首选，一次调用替代多次 read/grep |
| 定位符号位置 | **CodeGraph search** | 快速知道某个符号定义在哪个文件 |
| 重构前分析影响 | **CodeGraph impact** | 告诉我要改什么，AI 分析波及范围 |
| 查库/框架官方文档 | **Context7** → resolve-library-id → query-docs | 不熟悉的 API 先查文档，不猜。单次任务最多 3 次 |
| 查不到时补充 | **WebFetch** | 获取 GitHub Raw 源码或替代文档 |
| 搜索同类代码实现 | **GitHub Search** | 用字面代码片段搜索，不用自然语言关键词 |
| 数据库操作 | **MySQL** describe_table → query/execute | 仅检测到 SQL 配置时启用。DELETE/UPDATE 需用户确认 |
| 前端验证 | **Playwright** | 仅用户明确要求时使用 |

### 4.2 Skill 使用场景

| Skill | 使用时机 | 说明 |
|-------|----------|------|
| **brainstorming** | 开始任何创意/新功能/改行为前 | 必须先加载讨论设计，用户批准后再编码 |
| **writing-plans** | 有明确 spec 需要多步骤实施时 | 生成实施计划，按步执行 |
| **executing-plans** | 有现成计划需要执行时 | 独立会话执行带检查点 |
| **subagent-driven-development** | 计划中有独立任务可并行时 | 子 Agent 并行处理独立模块 |
| **dispatching-parallel-agents** | 存在 2+ 个无共享状态的独立任务 | 并行调度提高效率 |
| **verification-before-completion** | 要声称工作完成/修复之前 | 先运行验证命令，确认输出后再断言 |
| **systematic-debugging** | 遇到 Bug、测试失败或意外行为时 | 先系统化排查，再修复 |
| **test-driven-development** | 实现新功能或修复前 | 先写测试再写实现 |
| **requesting-code-review** | 完成任务/合并前 | 验证工作是否符合要求 |
| **receiving-code-review** | 收到 Code Review 反馈时 | 技术严谨验证，不盲从 |
| **ui-ux-pro-max** | 构建前端 UI 时 | 样式、配色、字体等设计建议 |
| **ckm:slides** | 需要生成 HTML 演示文稿时 | 使用 Chart.js + 设计 Token |
| **customize-opencode** | 编辑 opencode 自身配置时 | 修改 opencode.json、AGENTS.md 等 |

### 4.3 通用原则

- 能用 CodeGraph 解决的问题，不用 grep/read 重复劳动
- 先查文档再猜 API：不熟悉的库先用 Context7 查官方文档
- 每次写代码自检：字段语义、注解含义、NPE 风险、敏感信息泄露
