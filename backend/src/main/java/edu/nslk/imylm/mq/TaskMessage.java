// /backend/src/main/java/edu/nslk/imylm/mq/TaskMessage.java
// 职责描述：MQ 消息体 DTO，用于传输任务执行信息

package edu.nslk.imylm.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskMessage implements Serializable {

    private Long taskId;
    private List<Map<String, Object>> steps;
    private String input;
    private Map<String, Object> agentsConfig;
    private List<Map<String, String>> files;
}