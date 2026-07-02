# /ai_engine/app/agents/planner_agent.py
# 职责描述：规划 Agent，将任务拆解为可执行的子步骤（LangChain 模型）

from .base import BaseAgent, AgentResult
from app.core.model_gateway import ModelGateway
from langchain_core.messages import SystemMessage, HumanMessage
import time

class PlannerAgent(BaseAgent):
    """规划 Agent - 将复杂任务拆解为子步骤"""

    DEFAULT_PROMPT = "你是一个任务规划专家。请将以下任务拆解为3-5个具体的、可执行的子步骤。每个子步骤应该独立且有明确目标。"

    async def run(self, input_text: str, context: dict = None, config: dict = None) -> AgentResult:
        system_prompt = self.config.get("systemPrompt", self.DEFAULT_PROMPT)
        
        # 获取支持流式的 LangChain 模型
        gateway = ModelGateway(
            api_key=self.config.get("api_key", ""),
            endpoint=self.config.get("endpoint", ""),
            model=self.config.get("model", "deepseek-chat"),
        )
        llm = gateway.get_chat_model(
            temperature=self.config.get("temperature", 0.3),
            max_tokens=self.config.get("max_tokens", 4096),
        )

        messages = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=f"请规划以下任务：\n{input_text}")
        ]

        start = time.time()
        try:
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