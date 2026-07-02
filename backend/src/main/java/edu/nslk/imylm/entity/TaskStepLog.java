// /backend/src/main/java/edu/nslk/imylm/entity/TaskStepLog.java
// 职责描述：任务步骤日志实体，映射 task_step_log 表

package edu.nslk.imylm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_step_log")
public class TaskStepLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long agentConfigId;

    private Integer stepIndex;

    private String agentName;

    private String inputData;

    private String outputData;

    private String status;

    private Integer durationMs;

    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime createdAt;
}
