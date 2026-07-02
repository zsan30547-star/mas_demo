// /backend/src/main/java/edu/nslk/imylm/entity/SysUser.java
// 职责描述：用户实体，映射 sys_user 表

package edu.nslk.imylm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(condition = SqlCondition.EQUAL)
    private String username;

    private String password;

    private String email;

    private String avatar;

//    @TableLogic
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
