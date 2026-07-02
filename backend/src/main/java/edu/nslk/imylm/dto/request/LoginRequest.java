// /backend/src/main/java/edu/nslk/imylm/dto/request/LoginRequest.java
// 职责描述：登录请求 DTO

package edu.nslk.imylm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
