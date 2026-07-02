# /ai_engine/app/services/task_runner.py
# 职责描述：任务执行协调器，调用 Langgraph 工作流并处理结果回调，支持基于 Redis 的流式事件推送

import json
import asyncio
from typing import Dict, Any, List
import redis.asyncio as redis

from app.workflows.supervisor import build_workflow
from app.workflows.state import TaskState
from app.services.memory_service import MemoryService
from app.core.config import settings

class TaskRunner:
    """任务执行协调器"""

    def __init__(self):
        self.memory_service = MemoryService(
            host=settings.CHROMA_HOST,
            port=settings.CHROMA_PORT,
        )
        self.redis_client = redis.Redis(
            host=settings.REDIS_HOST, 
            port=settings.REDIS_PORT, 
            decode_responses=True
        )

    async def execute(self, task_id: str, steps: List[Dict],
                      agents_config: Dict[int, Dict],
                      user_input: str,
                      files: List[Dict] = None) -> Dict[str, Any]:
        
        channel_name = f"task_stream:{task_id}"
        try:
            memory_results = self.memory_service.search_memory(user_input)
            memory_context = "\n".join(memory_results) if memory_results else ""

            initial_state: TaskState = {
                "task_id": task_id,
                "user_input": user_input,
                "steps_config": steps,
                "agents_config": agents_config,
                "files": files or [],
                "current_step": 0,
                "step_outputs": {},
                "step_logs": [],
                "final_output": "",
                "errors": [],
                "memory_context": memory_context,
            }

            app = build_workflow()
            
            # 记录当前执行到了哪一步，以便知道流式字符属于哪个步骤
            current_executing_step = 0
            
            # 使用 astream_events 来捕获底层 LangChain 模型的实时生成事件
            async for event in app.astream_events(initial_state, version="v2"):
                kind = event["event"]
                
                # 捕获节点开始执行的信号 (LangGraph Node)
                if kind == "on_chat_model_start":
                    # 每当有新的大模型被调用，说明进入了一个新的步骤
                    current_executing_step += 1
                    # 推送步骤开始信号
                    await self.redis_client.publish(channel_name, json.dumps({
                        "type": "step_start",
                        "stepIndex": current_executing_step
                    }))
                
                # 捕获大模型生成的每一个 Token 字符块
                elif kind == "on_chat_model_stream":
                    chunk = event["data"]["chunk"]
                    if hasattr(chunk, 'content') and chunk.content:
                        # 极速推送到 Redis
                        await self.redis_client.publish(channel_name, json.dumps({
                            "type": "chunk",
                            "stepIndex": current_executing_step,
                            "content": chunk.content
                        }))

            # 流式执行完毕后，执行最终确认。
            # 直接使用刚才跑到最后的最终状态构建结果
            # 因为 astream_events 没有像 ainvoke 那样直接返回最终大字典，
            # 为保证向后兼容 MQ 逻辑，调用一次 ainvoke 获取最终确定性结果
            result = await app.ainvoke(initial_state)

            final_output = ""
            step_logs = result.get("step_logs", [])
            if step_logs:
                last_log = step_logs[-1]
                final_output = last_log.get("output", "")

            status = "failed" if result.get("errors") else "completed"

            if status == "completed" and final_output:
                try:
                    self.memory_service.save_memory(
                        task_id, user_input[:200], final_output[:1000]
                    )
                except Exception:
                    pass

            return {
                "taskId": task_id,
                "status": status,
                "output": final_output,
                "stepLogs": step_logs,
                "error": result.get("errors", [None])[0] if result.get("errors") else None,
            }

        except Exception as e:
            return {
                "taskId": task_id,
                "status": "failed",
                "output": "",
                "stepLogs": [],
                "error": str(e),
            }
        finally:
            await self.redis_client.close()