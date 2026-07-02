# /ai_engine/app/mq/consumer.py
# 职责描述：RabbitMQ 消费者，从 task.execute.queue 取消息并执行

import json
import asyncio
import time
import pika
from typing import Dict, Any

from app.core.config import settings
from app.services.task_runner import TaskRunner


class MQConsumer:
    """RabbitMQ message consumer"""

    def __init__(self):
        self.connection = None
        self.channel = None
        self.task_runner = TaskRunner()
        self._running = False

    def start(self):
        self._running = True
        while self._running:
            try:
                self._connect()
                self.channel.basic_consume(
                    queue="task.execute.queue",
                    on_message_callback=self._on_message,
                    auto_ack=False,
                )
                self.channel.start_consuming()
            except Exception as e:
                print(f"MQ connection error, retry in 10s: {e}")
                time.sleep(10)

    def stop(self):
        self._running = False
        if self.channel:
            self.channel.stop_consuming()
        if self.connection:
            self.connection.close()

    def _connect(self):
        credentials = pika.PlainCredentials(
            settings.RABBITMQ_USER, settings.RABBITMQ_PASS
        )
        params = pika.ConnectionParameters(
            host=settings.RABBITMQ_HOST,
            port=settings.RABBITMQ_PORT,
            credentials=credentials,
            heartbeat=600,
        )
        self.connection = pika.BlockingConnection(params)
        self.channel = self.connection.channel()
        self.channel.basic_qos(prefetch_count=1)

    def _on_message(self, ch, method, properties, body):
        task_id = ""
        try:
            message = json.loads(body)
            task_id = str(message.get("taskId", ""))
            steps = message.get("steps", [])
            user_input = message.get("input", "")
            agents_config = message.get("agentsConfig", {})

            print(f"--- [MQ] Received task: {task_id}")
            print(f"    Steps count: {len(steps)}, input: {user_input[:100] if user_input else '(empty)'}")

            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            result = loop.run_until_complete(
                self.task_runner.execute(
                    task_id=task_id,
                    steps=steps,
                    agents_config=agents_config,
                    user_input=user_input,
                    files=message.get("files"),
                )
            )
            loop.close()

            status = result.get("status")
            print(f"--- [MQ] Task {task_id} completed: {status}")
            if status == "failed":
                print(f"    Error: {result.get('error')}")

            self._send_result(result)
            ch.basic_ack(delivery_tag=method.delivery_tag)

        except Exception as e:
            print(f"--- [MQ] Task {task_id} failed with exception: {e}")
            import traceback
            traceback.print_exc()
            error_result = {
                "taskId": task_id,
                "status": "failed",
                "output": "",
                "stepLogs": [],
                "error": str(e),
            }
            self._send_result(error_result)
            ch.basic_ack(delivery_tag=method.delivery_tag)

    def _send_result(self, result: Dict[str, Any]):
        try:
            self.channel.basic_publish(
                exchange="task.exchange",
                routing_key="task.result",
                body=json.dumps(result, ensure_ascii=False),
                properties=pika.BasicProperties(delivery_mode=2),
            )
        except Exception as e:
            print(f"Failed to send result: {e}")
