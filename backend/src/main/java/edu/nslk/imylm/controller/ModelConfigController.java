// /backend/src/main/java/edu/nslk/imylm/controller/ModelConfigController.java
// 职责描述：模型配置的 HTTP 请求处理

package edu.nslk.imylm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.nslk.imylm.dto.response.ApiResponse;
import edu.nslk.imylm.entity.ModelConfig;
import edu.nslk.imylm.service.ModelConfigService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
public class ModelConfigController {

    private final ModelConfigService modelConfigService;

    public ModelConfigController(ModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    @GetMapping
    public ApiResponse<List<ModelConfig>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getUserId, userId)
               .or()
               .eq(ModelConfig::getIsPreset, 1);
        return ApiResponse.success(modelConfigService.list(wrapper));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody ModelConfig config,
                                    Authentication authentication) {
        config.setUserId((Long) authentication.getPrincipal());
        modelConfigService.save(config);
        return ApiResponse.success(config.getId());
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ModelConfig config) {
        config.setId(id);
        modelConfigService.updateById(config);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        modelConfigService.removeById(id);
        return ApiResponse.success(null);
    }
}
