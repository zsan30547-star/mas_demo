# /ai_engine/app/api/health.py
# 职责描述：健康检查接口

from fastapi import APIRouter

router = APIRouter()


@router.get("/health")
async def health_check():
    """健康检查，返回值 服务运行状态"""
    return {"status": "ok", "service": "ai-engine"}
