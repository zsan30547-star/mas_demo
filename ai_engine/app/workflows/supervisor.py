# /ai_engine/app/workflows/supervisor.py
# 职责描述：Langgraph Supervisor 工作流，根据用户编排的步骤顺序执行 Agent

from langgraph.graph import StateGraph, START, END
from langchain_core.runnables.config import RunnableConfig
from app.workflows.state import TaskState
from app.agents.agent_factory import AgentFactory
from app.agents.base import BaseAgent
from app.services.memory_service import MemoryService
import time
import re


# 渲染输入模板
# @param template 模板字符串，支持 $i 和 $oN
# @param user_input 用户原始输入
# @param step_outputs 各步骤输出
# @return 渲染后的文本
def render_template(template: str, user_input: str, step_outputs: dict) -> str:
    # 替换 $i 为用户原始输入
    result = template.replace("$i", user_input)
    
    # 替换 $oN 为对应步骤的输出 (N 为数字)
    pattern = r"\$o(\d+)"
    def replace_step(match):
        step_key = match.group(1)
        return step_outputs.get(step_key, "")
    result = re.sub(pattern, replace_step, result)
    
    # 兼容处理：为了向后兼容，保留对旧版 {{input}} 和 {{stepN.output}} 的支持
    result = result.replace("{{input}}", user_input)
    old_pattern = r"\{\{step(\d+)\.output\}\}"
    result = re.sub(old_pattern, replace_step, result)
    
    # 清理未被识别的 {{...}} 占位符（如 typo），避免透传给 LLM
    result = re.sub(r"\{\{.*?\}\}", "", result)
    
    return result


# 执行单个步骤的节点
# @param state 当前状态
# @param config LangGraph 自动注入的追踪上下文，用于冒泡事件
# @return 更新后的状态字典
async def execute_step(state: TaskState, config: RunnableConfig) -> dict:
    step_idx = state["current_step"]
    steps_config = state["steps_config"]

    if step_idx >= len(steps_config):
        return {"final_output": state.get("final_output", ""), "current_step": step_idx}

    step = steps_config[step_idx]
    agent_id = step.get("agentId", 0)
    input_template = step.get("inputTemplate", "$i") # 默认模板改为新的语法
    agents_config = state.get("agents_config", {})

    # 渲染输入
    rendered_input = render_template(
        input_template,
        state["user_input"],
        state.get("step_outputs", {}),
    )

    # 创建 Agent
    agent_config = agents_config.get(str(agent_id), {})
    agent_type = agent_config.get("agentType", "")
    agent = AgentFactory.create(agent_type, agent_config)

    if agent is None:
        error_msg = f"不支持的 Agent 类型: {agent_type}"
        return {
            "errors": [*state.get("errors", []), error_msg],
            "current_step": step_idx + 1,
        }

    # 构建上下文
    context = {"files": state.get("files", [])}
    # 可选：带 Chroma 检索记录
    if state.get("memory_context"):
        context["memory"] = state["memory_context"]

    # 执行 Agent
    start = time.time()
    # 【关键修改】：将外层工作流的 config 上下文透传给底层的 Agent
    result = await agent.run(rendered_input, context=context, config=config)

    step_log = {
        "stepIndex": step_idx + 1,
        "agentName": agent_config.get("name", f"Agent_{agent_id}"),
        "agentType": agent_type,
        "input": rendered_input,
        "output": result.output if result.success else "",
        "status": "success" if result.success else "failed",
        "durationMs": result.duration_ms,
        "error": result.error if not result.success else "",
    }

    step_key = str(step_idx + 1)
    new_outputs = {**state.get("step_outputs", {}), step_key: result.output}
    new_logs = [*state.get("step_logs", []), step_log]
    new_errors = state.get("errors", [])
    if not result.success:
        new_errors = [*new_errors, result.error]

    return {
        "current_step": step_idx + 1,
        "step_outputs": new_outputs,
        "step_logs": new_logs,
        "errors": new_errors,
    }


# 路由函数：决定下一步执行还是结束
# @param state 当前状态
# @return 下一步节点名称或 END
def route_next(state: TaskState) -> str:
    if state["errors"]:
        return END
    if state["current_step"] >= len(state["steps_config"]):
        return END
    return "execute_step"


# 构建工作流图
# @return 编译后的 StateGraph
def build_workflow() -> StateGraph:
    workflow = StateGraph(TaskState)

    workflow.add_node("execute_step", execute_step)
    workflow.add_edge(START, "execute_step")
    workflow.add_conditional_edges("execute_step", route_next, {
        "execute_step": "execute_step",
        END: END,
    })

    return workflow.compile()