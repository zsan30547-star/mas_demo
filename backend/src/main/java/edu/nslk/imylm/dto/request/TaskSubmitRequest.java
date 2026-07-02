// /backend/src/main/java/edu/nslk/imylm/dto/request/TaskSubmitRequest.java
// 职责描述：提交任务请求 DTO

package edu.nslk.imylm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TaskSubmitRequest {

    @NotNull(message = "请选择工作流模板")
    private Long workflowId;

    @NotBlank(message = "请输入任务标题")
    private String title;

    private Map<String, String> inputData;

    private List<Long> fileIds;
}
