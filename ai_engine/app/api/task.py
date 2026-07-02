# /ai_engine/app/api/task.py
# 职责描述：任务执行相关 API 路由

from fastapi import APIRouter
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
from app.services.task_runner import TaskRunner

router = APIRouter()
task_runner = TaskRunner()


class StepConfig(BaseModel):
    agentId: int
    order: int
    inputTemplate: str


class ExecuteRequest(BaseModel):
    taskId: str
    steps: List[StepConfig]
    agentsConfig: Dict[str, Any]
    input: str
    files: Optional[List[Dict[str, str]]] = None


@router.post("/task/execute")
async def execute_task(request: ExecuteRequest):
    """
    执行任务
    接收步骤配置和输入，通过 Langgraph 编排执行
    """
    result = await task_runner.execute(
        task_id=request.taskId,
        steps=[s.dict() for s in request.steps],
        agents_config=request.agentsConfig,
        user_input=request.input,
        files=request.files,
    )
    return result
