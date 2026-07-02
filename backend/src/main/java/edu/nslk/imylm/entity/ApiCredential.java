package edu.nslk.imylm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("api_credential")
public class ApiCredential {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String endpoint;
    
    @JsonProperty("apiKey")
    private String apiKeyEnc;
    
    @TableField(exist = false)
    private Boolean hasApiKey;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
