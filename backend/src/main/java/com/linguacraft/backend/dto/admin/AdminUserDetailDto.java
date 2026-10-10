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
 * 管理端用户详情(阶段 A5):资料 + 学习量小结。
 *
 * <p>永不返回密码字段。头像为空时前端按昵称首字渲染(与学生端一致)。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "管理端用户详情")
public class AdminUserDetailDto {

    /** 用户 ID */
    @Schema(description = "用户 ID")
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

    /** 头像 */
    @Schema(description = "头像 URL(可空)")
    private String avatar;

    /** 角色 */
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

    /** 练习提交数 */
    @Schema(description = "练习提交数")
    private int submissionCount;

    /** 其中待批改数 */
    @Schema(description = "待批改提交数")
    private int pendingSubmissionCount;

    /** 模考交卷数 */
    @Schema(description = "模考交卷数")
    private int examSubmissionCount;

    /** 错题数 */
    @Schema(description = "错题数")
    private int mistakeCount;

    /** 已学长词数 */
    @Schema(description = "已学长词数")
    private int learnedWordCount;

    /** 生效的批改方式偏好(来自 user_setting) */
    @Schema(description = "批改方式偏好:ai/manual")
    private String gradingPreference;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "注册时间")
    private LocalDateTime createdAt;

    /** 资料更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "资料更新时间")
    private LocalDateTime updatedAt;
}
