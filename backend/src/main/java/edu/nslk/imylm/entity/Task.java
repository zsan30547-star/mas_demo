// /backend/src/main/java/edu/nslk/imylm/entity/Task.java
// 职责描述：任务实体，映射 task 表

package edu.nslk.imylm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long workflowId;

    private String title;

    private String inputData;

    private String files;

    private String status;

    private String finalOutput;

    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
