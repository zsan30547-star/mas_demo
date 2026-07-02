# /ai_engine/app/main.py
# 职责描述：FastAPI 应用入口，注册路由和中间件

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import threading

from app.api.health import router as health_router
from app.api.task import router as task_router
from app.api.agent import router as agent_router
from app.api.knowledge import router as knowledge_router
from app.mq.consumer import MQConsumer

mq_consumer = MQConsumer()

# 
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时：在后台线程启动 MQ 消费者
    mq_thread = threading.Thread(target=mq_consumer.start, daemon=True)
    mq_thread.start()
    yield
    # 关闭时：停止 MQ 消费者
    mq_consumer.stop()


app = FastAPI(
    title="多模型智能体协作平台 - AI 引擎",
    description="提供 Langgraph 多 Agent 编排执行能力",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS 配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(health_router, prefix="/api/v1", tags=["health"])
app.include_router(task_router, prefix="/api/v1", tags=["task"])
app.include_router(agent_router, prefix="/api/v1", tags=["agent"])
app.include_router(knowledge_router, prefix="/api/v1", tags=["knowledge"])
