// /backend/src/main/java/edu/nslk/imylm/controller/KnowledgeController.java
// 职责描述：知识库管理接口——上传文档、解析文本、入库 Chroma

package edu.nslk.imylm.controller;

import edu.nslk.imylm.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai-engine.url:http://localhost:8000}")
    private String aiEngineUrl;

    // 上传文档到知识库
    // @param file 上传的文件（支持 TXT/PDF）
    // @return 入库结果
    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                   Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        try {
            String content;
            String fileName = file.getOriginalFilename();
            String ext = "";
            if (fileName != null && fileName.contains(".")) {
                ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
            }

            if (".txt".equals(ext)) {
                content = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                // 非 TXT 文件暂以内容提示返回
                content = "文件类型 " + ext + " 暂不支持自动解析文本，请上传 TXT 格式。";
                return ApiResponse.error(400, content);
            }

            if (content.isBlank()) {
                return ApiResponse.error(400, "文件内容为空");
            }

            String docId = "kb_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "docId", docId,
                    "content", content,
                    "title", fileName != null ? fileName : "未命名文档"
            );
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            var response = restTemplate.postForEntity(
                    aiEngineUrl + "/api/v1/knowledge/ingest",
                    request,
                    Map.class
            );

            if (response.getBody() != null && Integer.valueOf(200).equals(response.getBody().get("code"))) {
                return ApiResponse.success(Map.of("docId", docId, "fileName", fileName, "chars", content.length()));
            }
            return ApiResponse.error(500, "知识库入库失败");
        } catch (Exception e) {
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }
}
