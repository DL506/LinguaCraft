package com.linguacraft.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具:签发与解析访问令牌。
 *
 * <p>令牌负载:subject = 用户 ID,附加 username 声明;签名算法由密钥长度决定(HS256 起)。
 * 密钥与有效期来自配置 lc.jwt.secret / lc.jwt.expire-seconds。</p>
 */
@Component
public class JwtUtil {

    /** 签名密钥(HMAC-SHA) */
    private final SecretKey key;

    /** 令牌有效期(秒) */
    private final long expireSeconds;

    public JwtUtil(@Value("${lc.jwt.secret}") String secret,
                   @Value("${lc.jwt.expire-seconds}") long expireSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
    }

    /**
     * 签发访问令牌。
     *
     * @param userId   用户 ID
     * @param username 用户名(写入声明,便于排查)
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000))
                .signWith(key)
                .compact();
    }

    /**
     * 解析令牌中的用户 ID。
     *
     * @param token JWT 字符串
     * @return 用户 ID;令牌过期、伪造或格式非法时返回 null(由安全层统一处理为 401)
     */
    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            // 过期/签名不符/格式错误统一视为无效令牌
            return null;
        }
    }

    /**
     * 令牌有效期(秒),用于登录响应中的 expiresIn。
     *
     * @return 有效期秒数
     */
    public int getExpireSeconds() {
        return (int) expireSeconds;
    }
}