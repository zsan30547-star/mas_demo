# /ai_engine/app/agents/executor_agent.py
# 职责描述：执行 Agent，使用 Gemini 按规划执行具体任务

from .base import BaseAgent, AgentResult
from app.core.model_gateway import ModelGateway
import time


class ExecutorAgent(BaseAgent):
    """执行 Agent - 按规划和资料执行具体任务"""

    DEFAULT_PROMPT = "你是一个内容执行专家。请根据任务规划和参考资料，认真执行任务并输出高质量的结果。注意完整性、准确性和可读性。"

    async def run(self, input_text: str, context: dict = None, config: dict = None) -> AgentResult:
        system_prompt = self.config.get("systemPrompt", self.DEFAULT_PROMPT)
        gateway = ModelGateway(
            api_key=self.config.get("api_key", ""),
            endpoint=self.config.get("endpoint", ""),
            model=self.config.get("model", "gemini-2.0-flash"),
        )

        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": input_text}
        ]

        start = time.time()
        try:
            output = await gateway.chat(
                messages,
                temperature=self.config.get("temperature", 0.7),
                max_tokens=self.config.get("max_tokens", 8192),
            )
            return AgentResult(
                success=True,
                output=output,
                duration_ms=int((time.time() - start) * 1000),
            )
        except Exception as e:
            return AgentResult(
                success=False,
                output="",
                error=str(e),
                duration_ms=int((time.time() - start) * 1000),
            )
