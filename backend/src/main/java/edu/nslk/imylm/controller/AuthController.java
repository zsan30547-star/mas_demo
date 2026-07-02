// /backend/src/main/java/edu/nslk/imylm/controller/AuthController.java
// 职责描述：认证相关接口（注册、登录、刷新Token）

package edu.nslk.imylm.controller;

import edu.nslk.imylm.dto.request.LoginRequest;
import edu.nslk.imylm.dto.request.RegisterRequest;
import edu.nslk.imylm.dto.response.ApiResponse;
import edu.nslk.imylm.dto.response.LoginResponse;
import edu.nslk.imylm.entity.SysUser;
import edu.nslk.imylm.redis.TokenStore;
import edu.nslk.imylm.security.JwtTokenProvider;
import edu.nslk.imylm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TokenStore tokenStore;

    public AuthController(UserService userService,
                          JwtTokenProvider jwtTokenProvider,
                          PasswordEncoder passwordEncoder,
                          TokenStore tokenStore) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.tokenStore = tokenStore;
    }

    // 用户注册
    // @param request 注册请求（用户名、密码、邮箱）
    // @return 用户ID
    @PostMapping("/register")
    public ApiResponse<Long> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.getByUsername(request.getUsername()) != null) {
            return ApiResponse.error(400, "用户名已存在");
        }
        Long userId = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail());
        return ApiResponse.success(userId);
    }

    // 用户登录
    // @param request 登录请求（用户名、密码）
    // @return Token 信息
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        SysUser user = userService.getByUsername(request.getUsername());
        if (user == null) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error(401, "用户名或密码错误");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // refresh_token 存入 Redis
        tokenStore.storeRefreshToken(user.getId(), refreshToken, 604800000L);

        return ApiResponse.success(new LoginResponse(accessToken, refreshToken, 7200));
    }

    // 刷新 Token
    // @param body { refreshToken }
    // @return 新的 access_token
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ApiResponse.error(401, "refresh_token 无效或已过期");
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 校验 Redis 中的 refresh_token
        if (!tokenStore.validateRefreshToken(userId, refreshToken)) {
            return ApiResponse.error(401, "refresh_token 已失效，请重新登录");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, "");
        return ApiResponse.success(new LoginResponse(newAccessToken, refreshToken, 7200));
    }

    // 登出——将当前 token 加入黑名单，清除 refresh_token
    // @param authHeader Authorization: Bearer xxx
    // @return 成功消息
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                String jti = jwtTokenProvider.getJtiFromToken(token);
                long ttl = jwtTokenProvider.getRemainingTtl(token);
                tokenStore.blacklist(jti, ttl);

                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                tokenStore.removeRefreshToken(userId);
            }
        }
        return ApiResponse.success(null);
    }
}
