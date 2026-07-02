// /backend/src/main/java/edu/nslk/imylm/dto/response/LoginResponse.java
// 职责描述：登录响应 DTO

package edu.nslk.imylm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
