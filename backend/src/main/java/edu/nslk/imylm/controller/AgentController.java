// /backend/src/main/java/edu/nslk/imylm/controller/AgentController.java
// 职责描述：Agent 配置的 HTTP 请求处理

package edu.nslk.imylm.controller;

import edu.nslk.imylm.dto.request.AgentCreateRequest;
import edu.nslk.imylm.dto.response.AgentVO;
import edu.nslk.imylm.dto.response.ApiResponse;
import edu.nslk.imylm.entity.AgentConfig;
import edu.nslk.imylm.entity.ModelConfig;
import edu.nslk.imylm.mapper.ModelConfigMapper;
import edu.nslk.imylm.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;
    private final ModelConfigMapper modelConfigMapper;

    public AgentController(AgentService agentService,
                           ModelConfigMapper modelConfigMapper) {
        this.agentService = agentService;
        this.modelConfigMapper = modelConfigMapper;
    }

    @GetMapping
    public ApiResponse<List<AgentVO>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<AgentConfig> agents = agentService.getByUserId(userId);
        List<AgentVO> vos = agents.stream().map(this::toVO).collect(Collectors.toList());
        return ApiResponse.success(vos);
    }

    @GetMapping("/presets")
    public ApiResponse<List<AgentVO>> presets() {
        List<AgentConfig> agents = agentService.getPresets();
        List<AgentVO> vos = agents.stream().map(this::toVO).collect(Collectors.toList());
        return ApiResponse.success(vos);
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody AgentCreateRequest request,
                                    Authentication authentication) {
        AgentConfig agent = new AgentConfig();
        BeanUtils.copyProperties(request, agent);
        agent.setUserId((Long) authentication.getPrincipal());
        agent.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        agentService.save(agent);
        return ApiResponse.success(agent.getId());
    }

    @GetMapping("/{id}")
    public ApiResponse<AgentVO> getById(@PathVariable Long id) {
        AgentConfig agent = agentService.getById(id);
        if (agent == null) return ApiResponse.error(404, "Agent 不存在");
        return ApiResponse.success(toVO(agent));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @RequestBody AgentCreateRequest request) {
        AgentConfig agent = agentService.getById(id);
        if (agent == null) return ApiResponse.error(404, "Agent 不存在");
        BeanUtils.copyProperties(request, agent);
        agent.setId(id);
        agentService.updateById(agent);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        agentService.removeById(id);
        return ApiResponse.success(null);
    }

    private AgentVO toVO(AgentConfig agent) {
        AgentVO vo = new AgentVO();
        BeanUtils.copyProperties(agent, vo);
        // 关联查询模型信息
        ModelConfig mc = modelConfigMapper.selectById(agent.getModelConfigId());
        if (mc != null) {
            vo.setModelName(mc.getName());
            vo.setModel(mc.getModel());
        }
        return vo;
    }
}
