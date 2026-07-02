// /backend/src/main/java/edu/nslk/imylm/controller/FileController.java
// 职责描述：文件上传/下载接口

package edu.nslk.imylm.controller;

import edu.nslk.imylm.dto.response.ApiResponse;
import edu.nslk.imylm.entity.FileRecord;
import edu.nslk.imylm.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 上传文件
    // @param file 上传的文件
    // @return 文件记录
    @PostMapping("/upload")
    public ApiResponse<FileRecord> upload(@RequestParam("file") MultipartFile file,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        FileRecord record = fileService.upload(file, userId);
        return ApiResponse.success(record);
    }

    // 下载/预览文件
    // @param filename 存储的文件名
    // @return 文件流
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        File file = new File(uploadDir, filename);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedName)
                .body(resource);
    }
}
