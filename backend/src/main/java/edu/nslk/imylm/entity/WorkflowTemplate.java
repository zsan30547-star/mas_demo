// /backend/src/main/java/edu/nslk/imylm/entity/WorkflowTemplate.java
// 职责描述：工作流模板实体，映射 workflow_template 表

package edu.nslk.imylm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workflow_template")
public class WorkflowTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String description;

    private String steps;

    private Integer status;

    private Integer isPreset;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
