// /backend/src/main/java/edu/nslk/imylm/service/FileService.java
// 职责描述：文件存储业务接口

package edu.nslk.imylm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.nslk.imylm.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends IService<FileRecord> {
    // 上传文件，返回文件记录
    FileRecord upload(MultipartFile file, Long userId);
}
