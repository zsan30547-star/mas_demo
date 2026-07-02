# /ai_engine/app/workflows/state.py
# 职责描述：Langgraph 工作流状态定义

from typing import TypedDict, List, Dict, Any, Optional


class TaskState(TypedDict):
    """Langgraph 工作流状态，在节点间传递"""
    task_id: str                              # 任务ID
    user_input: str                           # 用户原始输入
    steps_config: List[Dict[str, Any]]        # 步骤配置列表
    agents_config: Dict[int, Dict[str, Any]]  # Agent 配置 { agentId: config }
    files: List[Dict[str, str]]               # 上传文件
    current_step: int                         # 当前执行到的步骤索引
    step_outputs: Dict[str, str]              # { "1": "step1输出", "2": "step2输出" }
    step_logs: List[Dict[str, Any]]           # 步骤执行日志
    final_output: str                         # 最终结果
    errors: List[str]                         # 错误信息
    memory_context: Optional[str]             # Chroma 历史记忆
