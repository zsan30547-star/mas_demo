// /backend/src/main/java/edu/nslk/imylm/redis/TokenStore.java
// 职责描述：Redis Token 存储——JWT 黑名单、Refresh Token 管理

package edu.nslk.imylm.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TokenStore {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String REFRESH_PREFIX = "token:refresh:";

    private final StringRedisTemplate redisTemplate;

    public TokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 将 JTI 加入黑名单，TTL 为 token 剩余有效期
    public void blacklist(String jti, long ttlMs) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "1", ttlMs, TimeUnit.MILLISECONDS);
    }

    // 检查 JTI 是否在黑名单中
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }

    // 存储 refresh_token
    public void storeRefreshToken(Long userId, String refreshToken, long ttlMs) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + userId, refreshToken, ttlMs, TimeUnit.MILLISECONDS);
    }

    // 校验 refresh_token 是否与存储的一致
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String stored = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
        return refreshToken.equals(stored);
    }

    // 移除 refresh_token（登出时清除）
    public void removeRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }
}
