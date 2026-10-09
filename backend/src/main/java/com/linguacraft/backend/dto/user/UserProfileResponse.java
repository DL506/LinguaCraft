package com.linguacraft.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户资料响应(GET /api/user/profile,对应前端 UserProfile)。
 *
 * <p>{@code role} 为后台入口判定依据(仅 admin 可进入 /admin);
 * 默认 student,旧前端读到额外字段不会受影响。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    /** 用户 ID(包装类型 Long,对外序列化为字符串) */
    @Schema(description = "用户 ID(字符串)", example = "1")
    private Long id;

    /** 用户名 */
    @Schema(description = "用户名", example = "demo")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称", example = "小明")
    private String nickname;

    /** 头像地址(可空) */
    @Schema(description = "头像地址(可空)")
    private String avatar;

    /** 等级数值 */
    @Schema(description = "等级数值", example = "5")
    private Integer level;

    /** 等级名称 */
    @Schema(description = "等级名称", example = "青铜学习者")
    private String levelName;

    /** 连续学习天数 */
    @Schema(description = "连续学习天数", example = "21")
    private Integer continuousDays;

    /** 累计学习天数 */
    @Schema(description = "累计学习天数", example = "128")
    private Integer totalStudyDays;

    /** 角色:student 学生 / admin 管理员(前端据此显示后台入口并做路由守卫) */
    @Schema(description = "角色:student/admin", example = "admin")
    private String role;
}