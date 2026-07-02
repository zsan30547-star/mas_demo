// /backend/src/main/java/edu/nslk/imylm/entity/AgentConfig.java
// 职责描述：Agent 配置实体，映射 agent_config 表

package edu.nslk.imylm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("agent_config")
public class AgentConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long modelConfigId;

    private String name;

    private String agentType;

    private String systemPrompt;

    private BigDecimal temperature;

    private Integer maxTokens;

    private String icon;

    private Integer sortOrder;

    private Integer enabled;

    private Integer isPreset;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
