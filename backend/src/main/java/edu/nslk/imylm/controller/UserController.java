// /backend/src/main/java/edu/nslk/imylm/controller/UserController.java
// 职责描述：用户信息管理接口

package edu.nslk.imylm.controller;

import edu.nslk.imylm.dto.response.ApiResponse;
import edu.nslk.imylm.entity.SysUser;
import edu.nslk.imylm.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 获取当前用户信息
    // @param authentication Spring Security 认证信息
    // @return 用户信息（不包含密码）
    @GetMapping("/profile")
    public ApiResponse<SysUser> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        SysUser user = userService.getById(userId);
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    // 更新用户信息
    // @param body { email }
    // @return 成功消息
    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(Authentication authentication,
                                           @RequestBody Map<String, String> body) {
        Long userId = (Long) authentication.getPrincipal();
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ApiResponse.error(400, "邮箱不能为空");
        }
        userService.updateProfile(userId, email);
        return ApiResponse.success(null);
    }

    // 修改密码
    // @param body { oldPassword, newPassword }
    // @return 成功消息
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(Authentication authentication,
                                            @RequestBody Map<String, String> body) {
        Long userId = (Long) authentication.getPrincipal();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isBlank()) {
            return ApiResponse.error(400, "原密码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ApiResponse.error(400, "新密码至少 6 位");
        }

        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        if (!success) {
            return ApiResponse.error(400, "原密码错误");
        }
        return ApiResponse.success(null);
    }
}
