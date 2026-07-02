// /backend/src/main/java/edu/nslk/imylm/entity/FileRecord.java
// 职责描述：文件记录实体，映射 file_record 表

package edu.nslk.imylm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_record")
public class FileRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long taskId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
