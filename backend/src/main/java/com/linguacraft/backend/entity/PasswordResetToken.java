package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 密码重置令牌实体:对应表 password_reset_token(找回密码的一次性令牌)。
 */
@Data
@TableName("password_reset_token")
public class PasswordResetToken {

    /** 主键(自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 一次性重置令牌(UUID 去连字符) */
    private String token;

    /** 过期时间(创建后 30 分钟) */
    private LocalDateTime expiresAt;

    /** 是否已使用:false 未使用,true 已使用 */
    private Boolean used;

    /** 创建时间(数据库默认值维护) */
    private LocalDateTime createdAt;
}