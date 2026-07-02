# /ai_engine/app/agents/search_agent.py
# 职责描述：搜索 Agent，调用 SerpAPI 搜索并整理结果 (LangChain 模型)
from .base import BaseAgent, AgentResult
from app.core.model_gateway import ModelGateway
from langchain_core.messages import SystemMessage, HumanMessage
import httpx
import time
import os

class SearchAgent(BaseAgent):
    """搜索 Agent - 联网搜索并整理结果"""

    DEFAULT_PROMPT = "你是一个搜索专家。请根据以下搜索结果，整理成结构化的信息摘要，包含关键数据、来源和趋势分析。"

    async def run(self, input_text: str, context: dict = None, config: dict = None) -> AgentResult:
        start = time.time()
        try:
            # 1. 调用 SerpAPI 搜索
            search_results = await self._search(input_text)

            if not search_results or search_results.startswith("未配置") or search_results.startswith("搜索请求失败"):
                return AgentResult(
                    success=True,
                    output=search_results,
                    duration_ms=int((time.time() - start) * 1000),
                )

            # 2. LLM 整理搜索结果
            system_prompt = self.config.get("systemPrompt", self.DEFAULT_PROMPT)
            gateway = ModelGateway(
                api_key=self.config.get("api_key", ""),
                endpoint=self.config.get("endpoint", ""),
                model=self.config.get("model", "deepseek-chat"),
            )
            llm = gateway.get_chat_model(
                temperature=self.config.get("temperature", 0.5),
                max_tokens=self.config.get("max_tokens", 4096),
            )

            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"搜索结果：\n{search_results}\n\n请整理以上信息。")
            ]

            # 关键：将外层传入的 config 原封不动传给 LLM
            response = await llm.ainvoke(messages, config=config)

            return AgentResult(
                success=True,
                output=response.content,
                duration_ms=int((time.time() - start) * 1000),
            )
        except Exception as e:
            return AgentResult(
                success=False,
                output="",
                error=str(e),
                duration_ms=int((time.time() - start) * 1000),
            )

    # 调用 SerpAPI 搜索
    async def _search(self, query: str) -> str:
        api_key = os.getenv("SERPAPI_API_KEY", "")
        if not api_key:
            return "未配置 SerpAPI Key，无法搜索。"

        try:
            async with httpx.AsyncClient() as client:
                resp = await client.get(
                    "https://serpapi.com/search",
                    params={"q": query, "api_key": api_key, "hl": "zh-cn"},
                    timeout=15,
                )
                data = resp.json()

            results = []
            for item in data.get("organic_results", [])[:5]:
                results.append(f"- {item.get('title', '')}\n  {item.get('snippet', '')}\n  来源: {item.get('link', '')}")

            return "\n".join(results) if results else "无搜索结果"
        except Exception as e:
            return f"搜索请求失败: {str(e)}"