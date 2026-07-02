# /ai_engine/app/core/model_gateway.py
# 职责描述：统一模型调用网关，返回兼容 LangChain 的大模型实例

from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage
from typing import Optional, List, Dict, Any

class ModelGateway:
    """模型调用网关，提供 LangChain 兼容的 ChatModel 实例"""

    DEFAULT_ENDPOINTS = {
        "deepseek": "https://api.deepseek.com/v1",
        "qwen": "https://dashscope.aliyuncs.com/compatible-mode/v1",
        "gemini": "https://generativelanguage.googleapis.com/v1beta/openai",
        "claude": "https://api.anthropic.com/v1",
    }

    def __init__(self, api_key: str, endpoint: str, model: str):
        self.api_key = api_key if api_key else "sk-dummy-key"
        self.endpoint = endpoint or self._guess_endpoint(model)
        self.model = model

    def _guess_endpoint(self, model: str) -> str:
        model_lower = model.lower()
        for key, url in self.DEFAULT_ENDPOINTS.items():
            if key in model_lower:
                return url
        return "https://api.openai.com/v1"

    def get_chat_model(self, temperature: float = 0.7, max_tokens: int = 4096) -> ChatOpenAI:
        """获取配置好的 LangChain 模型实例，开启流式支持"""
        return ChatOpenAI(
            api_key=self.api_key,
            base_url=self.endpoint,
            model=self.model,
            temperature=temperature,
            max_tokens=max_tokens,
            streaming=True # 强制开启底层流式支持
        )

    # 兼容老的调用方式
    async def chat(self, messages: List[Dict[str, str]],
                   temperature: Optional[float] = None,
                   max_tokens: Optional[int] = None) -> str:
        llm = self.get_chat_model(temperature or 0.7, max_tokens or 4096)
        langchain_msgs = []
        for m in messages:
            if m["role"] == "system":
                langchain_msgs.append(SystemMessage(content=m["content"]))
            else:
                langchain_msgs.append(HumanMessage(content=m["content"]))
        response = await llm.ainvoke(langchain_msgs)
        return response.content

