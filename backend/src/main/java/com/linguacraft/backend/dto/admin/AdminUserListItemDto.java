package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理端用户列表项(阶段 A5)。
 *
 * <p>不含密码、不含邮箱验证态等敏感字段;列表用途是"找到人、判断要不要停用/调角色"。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端用户列表项")
public class AdminUserListItemDto {

    /** 用户 ID */
    @Schema(description = "用户 ID", example = "1")
    private Long id;

    /** 登录名 */
    @Schema(description = "登录名")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称")
    private String nickname;

    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;

    /** 角色:student/admin */
    @Schema(description = "角色:student/admin")
    private String role;

    /** 是否启用 */
    @Schema(description = "是否启用")
    private Boolean enabled;

    /** 等级 */
    @Schema(description = "等级")
    private Integer level;

    /** 等级名 */
    @Schema(description = "等级名")
    private String levelName;

    /** 连续学习天数 */
    @Schema(description = "连续学习天数")
    private Integer continuousDays;

    /** 累计学习天数 */
    @Schema(description = "累计学习天数")
    private Integer totalStudyDays;

    /** 练习提交数(判断是否管理员改名的活跃用户) */
    @Schema(description = "练习提交数")
    private int submissionCount;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "注册时间")
    private LocalDateTime createdAt;

    /** 资料更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "资料更新时间")
    private LocalDateTime updatedAt;
}
