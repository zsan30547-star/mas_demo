-- /init.sql
-- 职责描述：多模型智能体协作平台数据库初始化脚本
-- MySQL 8.0+

USE agent_platform;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100),
    avatar      VARCHAR(500),
    status      TINYINT      DEFAULT 1,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. API 凭证表 (解耦后的配置)
CREATE TABLE IF NOT EXISTS api_credential (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(100) NOT NULL COMMENT '凭证名称',
    endpoint        VARCHAR(500) NOT NULL COMMENT 'API端点',
    api_key_enc     VARCHAR(500) COMMENT '加密的 API Key (AES)',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 模型配置表
CREATE TABLE IF NOT EXISTS model_config (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    api_credential_id BIGINT       COMMENT '绑定的凭证ID',
    name              VARCHAR(100) NOT NULL,
    model             VARCHAR(100) NOT NULL,
    is_preset         TINYINT      DEFAULT 0,
    created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (api_credential_id) REFERENCES api_credential(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Agent 配置表（引用 model_config）
CREATE TABLE IF NOT EXISTS agent_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    model_config_id BIGINT       NOT NULL,
    name            VARCHAR(100) NOT NULL,
    agent_type      VARCHAR(50)  NOT NULL,
    system_prompt   TEXT,
    temperature     DECIMAL(2,1) DEFAULT 0.7,
    max_tokens      INT          DEFAULT 4096,
    icon            VARCHAR(50),
    sort_order      INT          DEFAULT 0,
    enabled         TINYINT      DEFAULT 1,
    is_preset       TINYINT      DEFAULT 0,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (model_config_id) REFERENCES model_config(id),
    INDEX idx_user_id (user_id),
    INDEX idx_agent_type (agent_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 工作流模板表
CREATE TABLE IF NOT EXISTS workflow_template (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    steps       JSON         NOT NULL,
    status      TINYINT      DEFAULT 1,
    is_preset   TINYINT      DEFAULT 0,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 任务表
CREATE TABLE IF NOT EXISTS task (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    workflow_id     BIGINT,
    title           VARCHAR(200),
    input_data      JSON,
    files           JSON,
    status          VARCHAR(20)  DEFAULT 'pending',
    final_output    LONGTEXT,
    error_message   TEXT,
    started_at      DATETIME,
    finished_at     DATETIME,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (workflow_id) REFERENCES workflow_template(id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 任务步骤日志表
CREATE TABLE IF NOT EXISTS task_step_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id         BIGINT       NOT NULL,
    agent_config_id BIGINT,
    step_index      INT          NOT NULL,
    agent_name      VARCHAR(100),
    input_data      LONGTEXT,
    output_data     LONGTEXT,
    status          VARCHAR(20)  DEFAULT 'pending',
    duration_ms     INT,
    error_message   TEXT,
    started_at      DATETIME,
    finished_at     DATETIME,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 文件记录表
CREATE TABLE IF NOT EXISTS file_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    task_id     BIGINT,
    file_name   VARCHAR(255) NOT NULL,
    file_url    VARCHAR(500) NOT NULL,
    file_type   VARCHAR(50),
    file_size   BIGINT,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;