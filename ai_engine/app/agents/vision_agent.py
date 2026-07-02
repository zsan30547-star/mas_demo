# /ai_engine/app/agents/vision_agent.py
# 职责描述：视觉 Agent，使用 Qwen-VL 进行图片 OCR 和图像理解

from .base import BaseAgent, AgentResult
from app.core.model_gateway import ModelGateway
import time
import base64


class VisionAgent(BaseAgent):
    """视觉 Agent - 图片 OCR 和图像理解"""

    DEFAULT_PROMPT = "你是一个视觉分析专家。请仔细分析图片内容，提取所有文字信息，并描述图片中的关键元素。"

    async def run(self, input_text: str, context: dict = None, config: dict = None) -> AgentResult:
        system_prompt = self.config.get("systemPrompt", self.DEFAULT_PROMPT)
        gateway = ModelGateway(
            api_key=self.config.get("api_key", ""),
            endpoint=self.config.get("endpoint", ""),
            model=self.config.get("model", "qwen-vl-plus"),
        )

        # 从上下文提取图片文件 -> base64 data URI
        image_data_uri = ""
        if context and "files" in context:
            for f in context["files"]:
                ftype = f.get("type", "").lower()
                if "image" in ftype:
                    local_path = f.get("localPath", "")
                    if local_path:
                        try:
                            with open(local_path, "rb") as fp:
                                b64 = base64.b64encode(fp.read()).decode()
                            mime = ftype if ftype else "image/png"
                            image_data_uri = f"data:{mime};base64,{b64}"
                        except Exception:
                            pass
                    break

        messages = [{"role": "system", "content": system_prompt}]

        if image_data_uri:
            # 多模态请求（base64 data URI）
            user_content = [
                {"type": "text", "text": input_text or "请分析这张图片"},
                {"type": "image_url", "image_url": {"url": image_data_uri}}
            ]
        else:
            user_content = input_text or "请分析图片内容"

        messages.append({"role": "user", "content": user_content})

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
