// /backend/src/main/java/edu/nslk/imylm/security/JwtTokenProvider.java
// 职责描述：JWT Token 生成、验证、刷新，从配置读取密钥和过期时间

package edu.nslk.imylm.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // 生成 access_token（含 jti 用于黑名单）
    // @param userId 用户ID
    // @param username 用户名
    // @return JWT token字符串
    public String generateAccessToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // 生成 refresh_token（含 jti 用于校验）
    // @param userId 用户ID
    // @return JWT token字符串
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // 从 token 中提取用户ID
    // @param token JWT token
    // @return 用户ID
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    // 从 token 中提取用户名
    // @param token JWT token
    // @return 用户名
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    // 从 token 中提取 JTI
    // @param token JWT token
    // @return JTI 字符串
    public String getJtiFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    // 获取 access_token 剩余毫秒数
    // @param token JWT token
    // @return 剩余毫秒数
    public long getRemainingTtl(String token) {
        Claims claims = parseToken(token);
        return Math.max(0, claims.getExpiration().getTime() - System.currentTimeMillis());
    }

    // 校验 token 是否有效
    // @param token JWT token
    // @return true/false
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 解析 token
    // @param token JWT token
    // @return Claims
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
