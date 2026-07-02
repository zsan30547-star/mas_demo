// /backend/src/main/java/edu/nslk/imylm/dto/response/AgentVO.java
// 职责描述：Agent 配置响应 VO

package edu.nslk.imylm.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AgentVO {

    private Long id;
    private Long modelConfigId;
    private String name;
    private String agentType;
    private String modelName;
    private String model;
    private String systemPrompt;
    private BigDecimal temperature;
    private Integer maxTokens;
    private String icon;
    private Integer sortOrder;
    private Integer enabled;
    private Integer isPreset;
    private LocalDateTime createdAt;
}
