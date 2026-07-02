// /backend/src/main/java/edu/nslk/imylm/dto/request/AgentCreateRequest.java
// 职责描述：创建/更新 Agent 请求 DTO

package edu.nslk.imylm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentCreateRequest {

    @NotBlank(message = "Agent名称不能为空")
    private String name;

    @NotBlank(message = "Agent类型不能为空")
    private String agentType;

    @NotNull(message = "请选择模型配置")
    private Long modelConfigId;

    private String systemPrompt;

    private BigDecimal temperature;

    private Integer maxTokens;

    private Integer enabled;
}
