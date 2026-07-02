# /ai_engine/app/agents/validator_agent.py
# 职责描述：验证 Agent，使用 Claude 检查结果质量和准确性

from .base import BaseAgent, AgentResult
from app.core.model_gateway import ModelGateway
import time


class ValidatorAgent(BaseAgent):
    """验证 Agent - 检查结果质量和准确性"""

    DEFAULT_PROMPT = "你是一个质量验证专家。请仔细检查以下内容的质量，包括：1) 事实准确性 2) 逻辑一致性 3) 结构完整性 4) 语言表达。发现问题请直接修正并输出修正后的版本，最后附上修改说明。"

    async def run(self, input_text: str, context: dict = None, config: dict = None) -> AgentResult:
        system_prompt = self.config.get("systemPrompt", self.DEFAULT_PROMPT)
        gateway = ModelGateway(
            api_key=self.config.get("api_key", ""),
            endpoint=self.config.get("endpoint", ""),
            model=self.config.get("model", "claude-3-5-sonnet"),
        )

        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": f"请验证以下内容：\n\n{input_text}"}
        ]

        start = time.time()
        try:
            output = await gateway.chat(
                messages,
                temperature=self.config.get("temperature", 0.3),
                max_tokens=self.config.get("max_tokens", 4096),
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
