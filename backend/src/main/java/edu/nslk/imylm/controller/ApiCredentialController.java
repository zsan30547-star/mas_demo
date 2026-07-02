package edu.nslk.imylm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.nslk.imylm.dto.response.ApiResponse;
import edu.nslk.imylm.entity.ApiCredential;
import edu.nslk.imylm.service.ApiCredentialService;
import edu.nslk.imylm.util.AesEncryptUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credentials")
public class ApiCredentialController {

    private final ApiCredentialService apiCredentialService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai-engine.url:http://localhost:8000}")
    private String aiEngineUrl;

    public ApiCredentialController(ApiCredentialService apiCredentialService) {
        this.apiCredentialService = apiCredentialService;
    }

    @GetMapping
    public ApiResponse<List<ApiCredential>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LambdaQueryWrapper<ApiCredential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiCredential::getUserId, userId);
        List<ApiCredential> list = apiCredentialService.list(wrapper);
        for (ApiCredential c : list) {
            c.setHasApiKey(c.getApiKeyEnc() != null && !c.getApiKeyEnc().isEmpty());
            c.setApiKeyEnc(null);
        }
        return ApiResponse.success(list);
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody ApiCredential credential, Authentication authentication) {
        credential.setUserId((Long) authentication.getPrincipal());
        if (credential.getApiKeyEnc() != null && !credential.getApiKeyEnc().isEmpty()) {
            credential.setApiKeyEnc(AesEncryptUtil.encrypt(credential.getApiKeyEnc()));
        }
        apiCredentialService.save(credential);
        return ApiResponse.success(credential.getId());
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ApiCredential credential) {
        if (credential.getApiKeyEnc() != null && !credential.getApiKeyEnc().isEmpty()) {
            credential.setApiKeyEnc(AesEncryptUtil.encrypt(credential.getApiKeyEnc()));
        } else {
            credential.setApiKeyEnc(null);
        }
        credential.setId(id);
        apiCredentialService.updateById(credential);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        apiCredentialService.removeById(id);
        return ApiResponse.success(null);
    }

    // 测试指定凭证能否使用给定的模型标识
    // @param id 凭证 ID
    // @param body { model: "模型标识" }
    // @return { valid: boolean, message: string }
    @PostMapping("/{id}/test")
    public ApiResponse<Map<String, Object>> testCredential(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        String model = body.get("model");
        if (model == null || model.isBlank()) {
            return ApiResponse.error(400, "请指定模型标识");
        }

        ApiCredential cred = apiCredentialService.getById(id);
        if (cred == null) {
            return ApiResponse.error(404, "凭证不存在");
        }
        if (cred.getApiKeyEnc() == null || cred.getApiKeyEnc().isEmpty()) {
            return ApiResponse.error(400, "凭证据未配置 API Key");
        }

        try {
            String decryptedKey = AesEncryptUtil.decrypt(cred.getApiKeyEnc());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestBody = Map.of(
                    "endpoint", cred.getEndpoint(),
                    "api_key", decryptedKey,
                    "model", model
            );
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            var response = restTemplate.postForEntity(
                    aiEngineUrl + "/api/v1/agent/test-key",
                    request,
                    Map.class
            );

            if (response.getBody() != null) {
                return ApiResponse.success(response.getBody());
            }
            return ApiResponse.error(500, "测试服务无响应");
        } catch (Exception e) {
            return ApiResponse.error(500, "测试失败: " + e.getMessage());
        }
    }
}
