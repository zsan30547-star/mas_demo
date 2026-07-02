# /ai_engine/app/agents/agent_factory.py
# 职责描述：Agent 工厂，根据配置创建对应类型的 Agent 实例

from typing import Optional
from .base import BaseAgent
from .planner_agent import PlannerAgent
from .vision_agent import VisionAgent
from .executor_agent import ExecutorAgent
from .validator_agent import ValidatorAgent
from .search_agent import SearchAgent


class AgentFactory:
    """Agent 工厂，根据 agent_type 创建对应实例"""

    AGENT_MAP = {
        "planner": PlannerAgent,
        "vision": VisionAgent,
        "executor": ExecutorAgent,
        "validator": ValidatorAgent,
        "search": SearchAgent,
    }

    # 创建 Agent 实例
    # @param agent_type Agent 类型 (planner/vision/executor/validator/search)
    # @param config Agent 配置字典
    # @return BaseAgent 实例 的 返回值
    @classmethod
    def create(cls, agent_type: str, config: dict) -> Optional[BaseAgent]:
        agent_class = cls.AGENT_MAP.get(agent_type)
        if agent_class is None:
            return None
        return agent_class(config)

    # 检查 Agent 类型是否支持
    # @param agent_type 类型名
    # @return 是否支持
    @classmethod
    def supports(cls, agent_type: str) -> bool:
        return agent_type in cls.AGENT_MAP
