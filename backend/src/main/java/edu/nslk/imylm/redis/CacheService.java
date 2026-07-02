// /backend/src/main/java/edu/nslk/imylm/redis/CacheService.java
// 职责描述：基于 Redis 的通用缓存服务

package edu.nslk.imylm.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheService {

    private static final String PREFIX = "cache:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // 存入缓存
    // @param key 缓存 key
    // @param value 缓存值
    // @param ttlMs 过期时间（毫秒）
    public <T> void put(String key, T value, long ttlMs) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(PREFIX + key, json, ttlMs, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) { }
    }

    // 读取缓存
    // @param key 缓存 key
    // @param clazz 类型
    // @return 缓存值
    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + key);
            if (json == null) return null;
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    // 删除缓存
    // @param key 缓存 key
    public void evict(String key) {
        redisTemplate.delete(PREFIX + key);
    }
}
