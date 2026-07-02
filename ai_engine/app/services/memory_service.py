# /ai_engine/app/services/memory_service.py
# 职责描述：Chroma 向量存储服务，提供 Agent 记忆和知识库 RAG 能力

from typing import List, Dict, Any, Optional


class MemoryService:
    """Chroma 向量存储服务（容错模式：Chroma 不可用时静默降级）"""

    def __init__(self, host: str = "localhost", port: int = 8001):
        self.client = None
        try:
            import chromadb
            self.client = chromadb.HttpClient(host=host, port=port)
            self._ensure_collections()
        except Exception:
            try:
                import chromadb
                self.client = chromadb.PersistentClient(path="./chroma_data")
                self._ensure_collections()
            except Exception:
                # Chroma 不可用，静默降级
                self.client = None

    # 确保集合存在
    def _ensure_collections(self):
        if self.client is None:
            return
        try:
            self.client.get_collection("agent_memory")
        except Exception:
            try:
                self.client.create_collection("agent_memory")
            except Exception:
                pass

        try:
            self.client.get_collection("knowledge_base")
        except Exception:
            try:
                self.client.create_collection("knowledge_base")
            except Exception:
                pass

    # 保存任务到记忆
    # @param task_id 任务ID
    # @param task_summary 任务摘要
    # @param result 执行结果
    def save_memory(self, task_id: str, task_summary: str, result: str):
        if self.client is None:
            return
        try:
            collection = self.client.get_collection("agent_memory")
            collection.add(
                documents=[f"{task_summary}\n---\n{result}"],
                metadatas=[{"task_id": task_id, "type": "task_result"}],
                ids=[f"memory_{task_id}"],
            )
        except Exception:
            pass

    # 检索相关记忆
    # @param query 查询文本
    # @param top_k 返回数量
    # @return 匹配的记忆文本列表
    def search_memory(self, query: str, top_k: int = 3) -> List[str]:
        if self.client is None:
            return []
        try:
            collection = self.client.get_collection("agent_memory")
            results = collection.query(query_texts=[query], n_results=top_k)
            return results["documents"][0] if results["documents"] else []
        except Exception:
            return []

    # 保存知识库文档
    # @param doc_id 文档ID
    # @param content 文档内容
    def save_knowledge(self, doc_id: str, content: str):
        if self.client is None:
            return
        try:
            collection = self.client.get_collection("knowledge_base")
            collection.add(
                documents=[content],
                metadatas=[{"doc_id": doc_id}],
                ids=[f"kb_{doc_id}"],
            )
        except Exception:
            pass

    # 检索知识库
    # @param query 查询
    # @param top_k 返回数量
    # @return 匹配的文档列表
    def search_knowledge(self, query: str, top_k: int = 3) -> List[str]:
        if self.client is None:
            return []
        try:
            collection = self.client.get_collection("knowledge_base")
            results = collection.query(query_texts=[query], n_results=top_k)
            return results["documents"][0] if results["documents"] else []
        except Exception:
            return []
