# /ai_engine/app/agents/base.py
# 职责描述：Agent 基类，定义 Agent 统一接口

from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Optional


@dataclass
class AgentResult:
    """Agent 执行结果"""
    success: bool
    output: str
    error: Optional[str] = None
    duration_ms: int = 0


class BaseAgent(ABC):
    """Agent 基类，所有 Agent 必须实现 run 方法"""

    def __init__(self, config: dict):
        self.config = config

    @abstractmethod
    async def run(self, input_text: str, context: dict = None, config: dict = None) -> AgentResult:
        """
        执行 Agent 任务 
        @param input_text 输入文本
        @param context 上下文（含内存等）
        @param config LangChain 的运行配置 (用于传递事件监听器)
        @return AgentResult
        """
        pass
