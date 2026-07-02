// /backend/src/main/java/edu/nslk/imylm/redis/RateLimiter.java
// 职责描述：基于 Redis 的滑动窗口限流工具

package edu.nslk.imylm.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {

    private static final String PREFIX = "ratelimit:";
    private final StringRedisTemplate redisTemplate;

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 检查是否超过限流阈值（滑动窗口）
    // @param key 限流 key
    // @param maxRequests 窗口内最大请求数
    // @param windowMs 窗口大小（毫秒）
    // @return true 表示允许通过，false 表示被限流
    public boolean tryAcquire(String key, int maxRequests, long windowMs) {
        String redisKey = PREFIX + key;
        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;

        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
        long count = redisTemplate.opsForZSet().size(redisKey) != null
                ? redisTemplate.opsForZSet().size(redisKey) : 0L;

        if (count >= maxRequests) {
            return false;
        }

        redisTemplate.opsForZSet().add(redisKey, String.valueOf(now), now);
        redisTemplate.expire(redisKey, windowMs, TimeUnit.MILLISECONDS);
        return true;
    }
}
