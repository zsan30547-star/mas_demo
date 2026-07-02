// /backend/src/main/java/edu/nslk/imylm/mq/TaskResultConsumer.java
// 职责描述：接收 FastAPI 执行结果，更新任务状态并保存步骤日志

package edu.nslk.imylm.mq;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import edu.nslk.imylm.entity.Task;
import edu.nslk.imylm.entity.TaskStepLog;
import edu.nslk.imylm.mapper.TaskStepLogMapper;
import edu.nslk.imylm.service.TaskService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class TaskResultConsumer {

    private final TaskService taskService;
    private final TaskStepLogMapper taskStepLogMapper;

    public TaskResultConsumer(TaskService taskService,
                              TaskStepLogMapper taskStepLogMapper) {
        this.taskService = taskService;
        this.taskStepLogMapper = taskStepLogMapper;
    }

    // 消费结果消息
    // @param result { taskId, status, output, stepLogs, error }
    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "task.result.queue")
    public void handleResult(Map<String, Object> result) {
        Long taskId = Long.valueOf(result.get("taskId").toString());
        String status = (String) result.get("status");

        // 1. 更新任务状态
        LambdaUpdateWrapper<Task> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Task::getId, taskId);

        if ("completed".equals(status)) {
            wrapper.set(Task::getStatus, "completed");
            wrapper.set(Task::getFinalOutput, (String) result.get("output"));
            wrapper.set(Task::getFinishedAt, LocalDateTime.now());
        } else {
            wrapper.set(Task::getStatus, "failed");
            Object err = result.get("error");
            wrapper.set(Task::getErrorMessage, err != null ? err.toString() : "");
            wrapper.set(Task::getFinishedAt, LocalDateTime.now());
        }
        taskService.update(wrapper);

        // 2. 保存步骤日志
        Object stepLogsObj = result.get("stepLogs");
        if (stepLogsObj instanceof List) {
            List<Map<String, Object>> stepLogs = (List<Map<String, Object>>) stepLogsObj;
            for (Map<String, Object> log : stepLogs) {
                TaskStepLog stepLog = new TaskStepLog();
                stepLog.setTaskId(taskId);
                stepLog.setStepIndex(getInt(log, "stepIndex"));
                stepLog.setAgentName(getStr(log, "agentName"));
                stepLog.setInputData(getStr(log, "input"));
                stepLog.setOutputData(getStr(log, "output"));
                stepLog.setStatus(getStr(log, "status"));
                stepLog.setDurationMs(getInt(log, "durationMs"));
                stepLog.setErrorMessage(getStr(log, "error"));
                stepLog.setCreatedAt(LocalDateTime.now());
                taskStepLogMapper.insert(stepLog);
            }
        }
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return 0;
    }
}