package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体:对应表 user(账号基础信息与学习档案)。
 *
 * <p>对外序列化说明:主键 id 为包装类型 Long,经全局 Jackson 配置序列化为字符串,
 * 满足前端契约中 id 为 string 的约定。</p>
 */
@Data
@TableName("`user`")
public class User {

    /** 主键(自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名(3~20 位字母数字,唯一) */
    private String username;

    /** 密码(BCrypt 密文,禁止明文存储) */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 邮箱(唯一,用于找回密码) */
    private String email;

    /** 头像地址(可空) */
    private String avatar;

    /** 等级数值 */
    private Integer level;

    /** 等级名称 */
    private String levelName;

    /** 连续学习天数 */
    private Integer continuousDays;

    /** 累计学习天数 */
    private Integer totalStudyDays;

    /** 创建时间(数据库默认值维护) */
    private LocalDateTime createdAt;

    /** 更新时间(数据库默认值维护) */
    private LocalDateTime updatedAt;
}