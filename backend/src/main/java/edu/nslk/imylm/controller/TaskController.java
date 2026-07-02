// /backend/src/main/java/edu/nslk/imylm/controller/TaskController.java
// 职责描述：任务提交、查询接口

package edu.nslk.imylm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.nslk.imylm.dto.request.TaskSubmitRequest;
import edu.nslk.imylm.dto.response.ApiResponse;
import edu.nslk.imylm.entity.AgentConfig;
import edu.nslk.imylm.entity.ApiCredential;
import edu.nslk.imylm.entity.FileRecord;
import edu.nslk.imylm.entity.ModelConfig;
import edu.nslk.imylm.entity.Task;
import edu.nslk.imylm.entity.TaskStepLog;
import edu.nslk.imylm.entity.WorkflowTemplate;
import edu.nslk.imylm.mapper.AgentConfigMapper;
import edu.nslk.imylm.mapper.FileRecordMapper;
import edu.nslk.imylm.mapper.ModelConfigMapper;
import edu.nslk.imylm.mapper.TaskStepLogMapper;
import edu.nslk.imylm.mq.TaskMessage;
import edu.nslk.imylm.mq.TaskProducer;
import edu.nslk.imylm.service.ApiCredentialService;
import edu.nslk.imylm.service.TaskService;
import edu.nslk.imylm.service.WorkflowService;
import edu.nslk.imylm.util.AesEncryptUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.nslk.imylm.service.SseService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final WorkflowService workflowService;
    private final TaskProducer taskProducer;
    private final TaskStepLogMapper taskStepLogMapper;
    private final AgentConfigMapper agentConfigMapper;
    private final ModelConfigMapper modelConfigMapper;
    private final ApiCredentialService apiCredentialService;
    private final SseService sseService;
    private final FileRecordMapper fileRecordMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${file.upload-dir}")
    private String uploadDir;

    public TaskController(TaskService taskService,
                          WorkflowService workflowService,
                          TaskProducer taskProducer,
                          TaskStepLogMapper taskStepLogMapper,
                          AgentConfigMapper agentConfigMapper,
                          ModelConfigMapper modelConfigMapper,
                          ApiCredentialService apiCredentialService,
                          SseService sseService,
                          FileRecordMapper fileRecordMapper) {
        this.taskService = taskService;
        this.workflowService = workflowService;
        this.taskProducer = taskProducer;
        this.taskStepLogMapper = taskStepLogMapper;
        this.agentConfigMapper = agentConfigMapper;
        this.modelConfigMapper = modelConfigMapper;
        this.apiCredentialService = apiCredentialService;
        this.sseService = sseService;
        this.fileRecordMapper = fileRecordMapper;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> submit(@Valid @RequestBody TaskSubmitRequest request,
                                                    Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        WorkflowTemplate template = workflowService.getById(request.getWorkflowId());
        if (template == null) return ApiResponse.error(404, "工作流模板不存在");

        Task task = new Task();
        task.setUserId(userId);
        task.setWorkflowId(request.getWorkflowId());
        task.setTitle(request.getTitle());
        try {
            task.setInputData(request.getInputData() != null
                    ? objectMapper.writeValueAsString(request.getInputData()) : "{}");
        } catch (Exception e) { task.setInputData("{}"); }
        task.setStatus("pending");
        taskService.save(task);

        List<Map<String, Object>> steps = parseSteps(template.getSteps());
        Map<String, Object> agentsConfig = buildAgentsConfig(steps);

        // 最终防线：校验所有 Agent 的凭证是否可用，不可用则拒绝提交
        for (Map<String, Object> step : steps) {
            Object agentIdObj = step.get("agentId");
            if (agentIdObj == null) continue;
            Long agentId = ((Number) agentIdObj).longValue();
            String configKey = String.valueOf(agentId);
            @SuppressWarnings("unchecked")
            Map<String, Object> cfg = (Map<String, Object>) agentsConfig.get(configKey);
            if (cfg == null) return ApiResponse.error(400, "步骤中的 Agent 配置未找到");
            String apiKey = (String) cfg.getOrDefault("api_key", "");
            if (apiKey.isEmpty()) {
                String agentName = (String) cfg.getOrDefault("name", "未知");
                return ApiResponse.error(400, "Agent「" + agentName + "」未配置有效的 API Key，无法执行任务，请先在模型管理中绑定凭证");
            }
            String endpoint = (String) cfg.getOrDefault("endpoint", "");
            if (endpoint.isEmpty()) {
                String agentName = (String) cfg.getOrDefault("name", "未知");
                return ApiResponse.error(400, "Agent「" + agentName + "」未配置 API 端点，无法执行任务");
            }
        }

        TaskMessage msg = new TaskMessage();
        msg.setTaskId(task.getId());
        msg.setInput(request.getInputData() != null ? request.getInputData().get("text") : "");
        msg.setSteps(steps);
        msg.setAgentsConfig(agentsConfig);

        // 查文件记录，构建 [{name, type, localPath}] 传给 AI 引擎
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            List<FileRecord> fileRecords = fileRecordMapper.selectBatchIds(request.getFileIds());
            List<Map<String, String>> files = new java.util.ArrayList<>();
            for (FileRecord fr : fileRecords) {
                // fileUrl 格式 "/uploads/uuid.ext" → 提取 "uuid.ext"
                String storedName = fr.getFileUrl().substring(fr.getFileUrl().lastIndexOf("/") + 1);
                Map<String, String> f = new HashMap<>();
                f.put("name", fr.getFileName());
                f.put("type", fr.getFileType() != null ? fr.getFileType() : "application/octet-stream");
                // localPath 是磁盘上的绝对路径
                f.put("localPath", uploadDir.endsWith("/") || uploadDir.endsWith("\\")
                        ? uploadDir + storedName : uploadDir + "/" + storedName);
                files.add(f);
            }
            msg.setFiles(files);
        }

        taskProducer.sendTask(msg);

        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("status", "pending");
        return ApiResponse.success(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildAgentsConfig(List<Map<String, Object>> steps) {
        Map<String, Object> configMap = new HashMap<>();
        for (Map<String, Object> step : steps) {
            Object agentIdObj = step.get("agentId");
            if (agentIdObj == null) continue;
            Long agentId = ((Number) agentIdObj).longValue();
            if (configMap.containsKey(String.valueOf(agentId))) continue;

            AgentConfig agent = agentConfigMapper.selectById(agentId);
            if (agent == null) continue;

            ModelConfig mc = modelConfigMapper.selectById(agent.getModelConfigId());

            Map<String, Object> cfg = new HashMap<>();
            cfg.put("name", agent.getName());
            cfg.put("agentType", agent.getAgentType());
            cfg.put("temperature", agent.getTemperature());
            cfg.put("maxTokens", agent.getMaxTokens());
            cfg.put("systemPrompt", agent.getSystemPrompt());

            if (mc != null) {
                cfg.put("model", mc.getModel());
                if (mc.getApiCredentialId() != null) {
                    ApiCredential cred = apiCredentialService.getById(mc.getApiCredentialId());
                    if (cred != null) {
                        cfg.put("endpoint", cred.getEndpoint());
                        String keyEnc = cred.getApiKeyEnc();
                        if (keyEnc != null && !keyEnc.isEmpty()) {
                            try { cfg.put("api_key", AesEncryptUtil.decrypt(keyEnc)); }
                            catch (Exception e) { cfg.put("api_key", ""); }
                        } else {
                            cfg.put("api_key", "");
                        }
                    } else {
                        cfg.put("endpoint", "");
                        cfg.put("api_key", "");
                    }
                } else {
                    cfg.put("endpoint", "");
                    cfg.put("api_key", "");
                }
            } else {
                cfg.put("model", "");
                cfg.put("endpoint", "");
                cfg.put("api_key", "");
            }
            configMap.put(String.valueOf(agentId), cfg);
        }
        return configMap;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(Authentication authentication,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Task> p =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId).orderByDesc(Task::getCreatedAt);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Task> result = taskService.page(p, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", result.getCurrent());
        map.put("size", result.getSize());
        return ApiResponse.success(map);
    }

    // 仪表盘统计
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId);
        List<Task> allTasks = taskService.list(wrapper);
        Map<String, Long> statusCounts = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        Map<String, Object> result = new HashMap<>();
        result.put("total", (long) allTasks.size());
        result.put("completed", statusCounts.getOrDefault("completed", 0L));
        result.put("running", statusCounts.getOrDefault("running", 0L));
        result.put("failed", statusCounts.getOrDefault("failed", 0L));
        result.put("pending", statusCounts.getOrDefault("pending", 0L));
        // 最近 5 个任务
        result.put("recent", allTasks.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList()));
        return ApiResponse.success(result);
    }

    @GetMapping("/{id:\\d+}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        Task task = taskService.getById(id);
        if (task == null) return ApiResponse.error(404, "任务不存在");
        LambdaQueryWrapper<TaskStepLog> lw = new LambdaQueryWrapper<>();
        lw.eq(TaskStepLog::getTaskId, id).orderByAsc(TaskStepLog::getStepIndex);
        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        result.put("stepLogs", taskStepLogMapper.selectList(lw));
        return ApiResponse.success(result);
    }
    
    // SSE 订阅接口
    @GetMapping(value = "/{id:\\d+}/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter subscribeTaskStream(@PathVariable Long id) {
        return sseService.subscribe(id);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseSteps(String stepsJson) {
        try { return new ObjectMapper().readValue(stepsJson, List.class); }
        catch (Exception e) { return List.of(); }
    }
}
