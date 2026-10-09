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
 * <p>令牌负载:subject = 用户 ID,附加 username 与 role 声明(角色用于后台接口的权限判定)。
 * 签名算法由密钥长度决定(HS256 起)。密钥与有效期来自配置 lc.jwt.secret / lc.jwt.expire-seconds。</p>
 *
 * <p>安全性说明:role 从令牌声明读取而非每次查库,因此**修改角色或停用账号后,已签发的旧令牌在有效期内仍可用**。
 * MVP 阶段可接受(令牌有效期 2 小时);如需立即失效,可改为每请求回查 user 表或引入 token 版本号。</p>
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
     * @param role     角色:student/admin(写入声明,供权限判定)
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000))
                .signWith(key)
                .compact();
    }

    /**
     * 令牌主体:用户 ID 与角色。
     *
     * @param userId 用户 ID
     * @param role   角色(student/admin);旧令牌未携带该声明时为 null,由过滤器按 student 兜底
     */
    public record JwtPrincipal(Long userId, String role) {
    }

    /**
     * 解析令牌中的主体信息(用户 ID + 角色)。
     *
     * @param token JWT 字符串
     * @return 主体;令牌过期、伪造或格式非法时返回 null(由安全层统一处理为 401)
     */
    public JwtPrincipal parsePrincipal(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new JwtPrincipal(Long.valueOf(claims.getSubject()), claims.get("role", String.class));
        } catch (Exception e) {
            // 过期/签名不符/格式错误统一视为无效令牌
            return null;
        }
    }

    /**
     * 解析令牌中的用户 ID(仅需要身份时使用,委托 {@link #parsePrincipal(String)})。
     *
     * @param token JWT 字符串
     * @return 用户 ID;令牌无效时返回 null
     */
    public Long parseUserId(String token) {
        JwtPrincipal principal = parsePrincipal(token);
        return principal == null ? null : principal.userId();
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