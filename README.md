# 多模型智能体协作平台

Multi-Model Agent Collaboration Platform

一个支持多模型混合编排的智能体协作平台——通过表单配置 Agent、自定义执行步骤，Langgraph 负责编排调度，让不同 AI 模型各司其职、协作完成任务。

## 系统架构

```
Vue3 前端 (5173) ←→ Spring Boot (8080) ←→ RabbitMQ ←→ FastAPI AI Engine (8000)
                         ↕                    ↕
                       Redis ←─────────── Redis Pub/Sub
                       MySQL
                       Chroma (8001)
```

## 技术栈

| 分类 | 技术 |
|------|------|
| 主后端 | Spring Boot 3.5 + JDK 17 |
| ORM | Mybatis-Plus |
| 安全 | Spring Security + JWT |
| 缓存/消息 | Redis 7 + RabbitMQ 3 |
| AI 引擎 | FastAPI + Langgraph |
| 向量库 | Chroma |
| 前端 | Vue3 + Vite + Element Plus + Pinia |

## 快速启动

### 1. 启动基础设施

```bash
docker-compose up -d
```

启动 MySQL (3306)、Redis (6379)、RabbitMQ (5672/15672)、Chroma (8001)

### 2. 启动服务

项目根目录下有 `manage.bat` 一键启停脚本：

| 操作 | 命令 |
|------|------|
| 先停再启 | 双击 `manage.bat` |
| 仅启动 | `manage.bat start` |
| 仅停止 | `manage.bat stop` |

或分别启动：

```bash
# 后端 (新窗口)
cd backend && mvnw spring-boot:run

# 前端 (新窗口)
cd frontend && npm run dev

# AI 引擎 (新窗口)
cd ai_engine && conda activate jobs_1 && uvicorn app.main:app --reload --port 8000
```

### 3. 访问

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 API | http://localhost:8080 |
| AI 引擎 | http://localhost:8000 |
| RabbitMQ 管理 | http://localhost:15672 (admin/admin123) |

**默认管理员账号**：`admin` / `admin123`

## 预置数据

系统启动后自动创建：

- 1 个管理员用户 (admin)
- 4 个预置模型配置 (DeepSeek / Qwen-VL / Gemini / Claude)
- 5 个预置 Agent (规划 / 视觉 / 执行 / 验证 / 搜索)
- 1 个预置工作流模板 (研究报告生成)

## 演示场景：智能研究报告生成

1. 登录后进入「工作流模板」，查看预置的"研究报告生成"模板
2. 进入「提交任务」，选择该模板，输入研究课题（如：2026年AI Agent在企业服务中的应用趋势）
3. 提交后进入任务详情页，实时查看每个 Agent 步骤的执行进度和输出
4. 最终生成的研究报告以 Markdown 格式展示

## 项目结构

```
project_1/
├── backend/        # Spring Boot 3 后端
├── frontend/       # Vue3 前端
├── ai_engine/      # FastAPI AI 引擎
├── docker-compose.yml
├── init.sql        # 数据库初始化脚本
└── manage.bat      # 一键启停脚本
```

## API 概览

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/api/auth/*` | 注册、登录、登出、刷新 Token |
| 用户 | `/api/user/*` | 个人信息、修改密码 |
| Agent | `/api/agents/*` | Agent 配置 CRUD |
| 模型 | `/api/models/*` | 模型配置 CRUD |
| 凭证 | `/api/credentials/*` | API 凭证管理 |
| 工作流 | `/api/workflows/*` | 工作流模板 CRUD |
| 任务 | `/api/tasks/*` | 任务提交、查询、统计、SSE 推送 |
| 文件 | `/api/files/*` | 文件上传、预览 |
