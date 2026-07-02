// /backend/src/main/java/edu/nslk/imylm/service/impl/FileServiceImpl.java
// 职责描述：文件存储业务实现——本地文件系统存储

package edu.nslk.imylm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.nslk.imylm.entity.FileRecord;
import edu.nslk.imylm.mapper.FileRecordMapper;
import edu.nslk.imylm.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public FileRecord upload(MultipartFile file, Long userId) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID().toString() + ext;
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Path targetPath = dir.resolve(storedName);
            file.transferTo(targetPath.toFile());

            FileRecord record = new FileRecord();
            record.setUserId(userId);
            record.setFileName(originalName);
            record.setFileUrl("/uploads/" + storedName);
            record.setFileType(file.getContentType());
            record.setFileSize(file.getSize());
            save(record);
            return record;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}
