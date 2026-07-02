// /backend/src/main/java/edu/nslk/imylm/controller/WorkflowController.java
// 职责描述：工作流模板的 HTTP 请求处理，含 Agent 凭证有效性校验

package edu.nslk.imylm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.nslk.imylm.dto.response.ApiResponse;
import edu.nslk.imylm.entity.AgentConfig;
import edu.nslk.imylm.entity.ApiCredential;
import edu.nslk.imylm.entity.ModelConfig;
import edu.nslk.imylm.entity.WorkflowTemplate;
import edu.nslk.imylm.mapper.AgentConfigMapper;
import edu.nslk.imylm.mapper.ModelConfigMapper;
import edu.nslk.imylm.service.ApiCredentialService;
import edu.nslk.imylm.service.WorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final AgentConfigMapper agentConfigMapper;
    private final ModelConfigMapper modelConfigMapper;
    private final ApiCredentialService apiCredentialService;

    public WorkflowController(WorkflowService workflowService,
                              AgentConfigMapper agentConfigMapper,
                              ModelConfigMapper modelConfigMapper,
                              ApiCredentialService apiCredentialService) {
        this.workflowService = workflowService;
        this.agentConfigMapper = agentConfigMapper;
        this.modelConfigMapper = modelConfigMapper;
        this.apiCredentialService = apiCredentialService;
    }

    @GetMapping
    public ApiResponse<List<WorkflowTemplate>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LambdaQueryWrapper<WorkflowTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowTemplate::getUserId, userId)
               .or()
               .eq(WorkflowTemplate::getIsPreset, 1);
        return ApiResponse.success(workflowService.list(wrapper));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkflowTemplate> getById(@PathVariable Long id) {
        WorkflowTemplate template = workflowService.getById(id);
        if (template == null) {
            return ApiResponse.error(404, "工作流不存在");
        }
        return ApiResponse.success(template);
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody WorkflowTemplate template,
                                    Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        template.setUserId(userId);
        template.setStatus(1);

        // 校验每个步骤的 Agent 是否绑定了有效的 API 凭证
        String credentialError = validateStepsCredentials(template.getSteps());
        if (credentialError != null) {
            return ApiResponse.error(400, credentialError);
        }

        workflowService.save(template);
        return ApiResponse.success(template.getId());
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @RequestBody WorkflowTemplate template) {
        template.setId(id);

        // 校验每个步骤的 Agent 是否绑定了有效的 API 凭证
        String credentialError = validateStepsCredentials(template.getSteps());
        if (credentialError != null) {
            return ApiResponse.error(400, credentialError);
        }

        workflowService.updateById(template);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workflowService.removeById(id);
        return ApiResponse.success(null);
    }

    // 校验工作流步骤中每个 Agent 的凭证有效性，返回 null 表示全部通过
    private String validateStepsCredentials(String stepsJson) {
        if (stepsJson == null || stepsJson.isEmpty()) return null;
        List<Map<String, Object>> steps;
        try {
            steps = new ObjectMapper().readValue(stepsJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return null;
        }

        List<String> errors = new ArrayList<>();
        for (Map<String, Object> step : steps) {
            Object agentIdObj = step.get("agentId");
            if (agentIdObj == null) continue;
            Long agentId = ((Number) agentIdObj).longValue();

            AgentConfig agent = agentConfigMapper.selectById(agentId);
            if (agent == null) {
                errors.add("步骤中存在无效的 Agent（ID=" + agentId + "）");
                continue;
            }

            ModelConfig mc = modelConfigMapper.selectById(agent.getModelConfigId());
            if (mc == null || mc.getApiCredentialId() == null) {
                errors.add("Agent「" + agent.getName() + "」未绑定任何 API 凭证，请先在模型管理中为其绑定凭证");
                continue;
            }

            ApiCredential cred = apiCredentialService.getById(mc.getApiCredentialId());
            if (cred == null || cred.getApiKeyEnc() == null || cred.getApiKeyEnc().isEmpty()) {
                errors.add("Agent「" + agent.getName() + "」绑定的凭证" + (cred != null ? "「" + cred.getName() + "」" : "") + "未配置 API Key，请在凭证库中完善 Key");
            }
        }

        if (errors.isEmpty()) return null;
        return String.join("；", errors);
    }
}