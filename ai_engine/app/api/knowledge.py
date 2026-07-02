# /ai_engine/app/api/knowledge.py
# 职责描述：知识库 API——文档入库、检索

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from app.services.memory_service import MemoryService
from app.core.config import settings

router = APIRouter()
memory_service = MemoryService(
    host=settings.CHROMA_HOST,
    port=settings.CHROMA_PORT,
)


class IngestRequest(BaseModel):
    docId: str
    content: str
    title: Optional[str] = ""


class SearchRequest(BaseModel):
    query: str
    topK: int = 3


@router.post("/knowledge/ingest")
async def ingest_document(request: IngestRequest):
    """
    将文档内容入库到 Chroma 知识库
    """
    try:
        content = request.content
        if request.title:
            content = f"# {request.title}\n\n{request.content}"
        memory_service.save_knowledge(request.docId, content)
        return {"code": 200, "message": "ok"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/knowledge/search")
async def search_knowledge(request: SearchRequest):
    """
    从 Chroma 知识库检索相关内容
    """
    try:
        results = memory_service.search_knowledge(request.query, request.topK)
        return {"code": 200, "data": results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
