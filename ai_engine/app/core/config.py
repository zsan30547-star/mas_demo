# /ai_engine/app/core/config.py
# 职责描述：Pydantic 配置管理，从环境变量读取配置

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """应用配置，从环境变量读取"""

    # Redis
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379

    # RabbitMQ
    RABBITMQ_HOST: str = "localhost"
    RABBITMQ_PORT: int = 5672
    RABBITMQ_USER: str = "admin"
    RABBITMQ_PASS: str = "admin123"

    # Chroma
    CHROMA_HOST: str = "localhost"
    CHROMA_PORT: int = 8001

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
