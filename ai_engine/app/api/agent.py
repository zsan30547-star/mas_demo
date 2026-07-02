# /ai_engine/app/api/agent.py
# 职责描述：Agent 管理相关 API（API Key 验证等）

from fastapi import APIRouter
from pydantic import BaseModel
from app.core.model_gateway import ModelGateway

router = APIRouter()


class TestKeyRequest(BaseModel):
    endpoint: str
    api_key: str
    model: str


@router.post("/agent/test-key")
async def test_key(request: TestKeyRequest):
    """验证 API Key 是否可用，发一条简短测试消息确认连通"""
    try:
        gateway = ModelGateway(
            api_key=request.api_key,
            endpoint=request.endpoint,
            model=request.model,
        )
        await gateway.chat(
            messages=[{"role": "user", "content": "hi"}],
            max_tokens=5,
        )
        return {"valid": True, "message": "连接成功"}
    except Exception as e:
        return {"valid": False, "message": str(e)}
