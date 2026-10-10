package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.AdminUserDetailDto;
import com.linguacraft.backend.dto.admin.AdminUserListItemDto;
import com.linguacraft.backend.entity.ExamSubmission;
import com.linguacraft.backend.entity.Mistake;
import com.linguacraft.backend.entity.PracticeSubmission;
import com.linguacraft.backend.entity.User;
import com.linguacraft.backend.entity.UserSetting;
import com.linguacraft.backend.entity.UserWordProgress;
import com.linguacraft.backend.mapper.ExamSubmissionMapper;
import com.linguacraft.backend.mapper.MistakeMapper;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.UserMapper;
import com.linguacraft.backend.mapper.UserSettingMapper;
import com.linguacraft.backend.mapper.UserWordProgressMapper;
import com.linguacraft.backend.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理端用户服务(阶段 A5):用户查询、启用停用、角色调整。
 *
 * <p>V08 加了 {@code role}/{@code enabled} 两列、A1 也已经让后端按角色放行,但这两列此前<b>只能改数据库</b> ——
 * 本服务补上入口。</p>
 *
 * <p>三条护栏,顺序即优先级:</p>
 * <ol>
 *   <li>不能停用自己、不能把自己降权 —— 否则管理员会把自己关在门外且无法自救(后台入口本身要求 admin);</li>
 *   <li>系统必须保留至少一个管理员 —— 停用或降权"最后一个 admin"同样拒绝;</li>
 *   <li>角色只接受 student/admin。</li>
 * </ol>
 *
 * <p>已知的生效延迟(用户已确认保持现状):{@code JwtAuthFilter} 只解析令牌声明、不回查数据库,
 * 因此停用/降权<b>不影响已签发令牌</b>,最长到令牌过期(2 小时)才真正生效;新登录立即被拒
 * (登录环节比对 {@code enabled},返回 {@code code 1001 账号已停用,请联系管理员})。</p>
 */
@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    /** 角色白名单 */
    private static final Set<String> VALID_ROLES = Set.of(User.ROLE_STUDENT, User.ROLE_ADMIN);

    private final UserMapper userMapper;
    private final UserSettingMapper userSettingMapper;
    private final PracticeSubmissionMapper practiceSubmissionMapper;
    private final ExamSubmissionMapper examSubmissionMapper;
    private final MistakeMapper mistakeMapper;
    private final UserWordProgressMapper wordProgressMapper;

    public AdminUserService(UserMapper userMapper,
                            UserSettingMapper userSettingMapper,
                            PracticeSubmissionMapper practiceSubmissionMapper,
                            ExamSubmissionMapper examSubmissionMapper,
                            MistakeMapper mistakeMapper,
                            UserWordProgressMapper wordProgressMapper) {
        this.userMapper = userMapper;
        this.userSettingMapper = userSettingMapper;
        this.practiceSubmissionMapper = practiceSubmissionMapper;
        this.examSubmissionMapper = examSubmissionMapper;
        this.mistakeMapper = mistakeMapper;
        this.wordProgressMapper = wordProgressMapper;
    }

    /**
     * 用户分页列表(GET /api/admin/users)。
     *
     * @param keyword 登录名/昵称/邮箱关键词(可空)
     * @param role    角色筛选(可空)
     * @param enabled 启用状态筛选(可空)
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    public PageResult<AdminUserListItemDto> list(String keyword, String role, Boolean enabled, long page, long size) {
        if (role != null && !role.isBlank() && !VALID_ROLES.contains(role)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "role 仅接受 student/admin");
        }
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<User>lambdaQuery();
        if (role != null && !role.isBlank()) {
            wrapper.eq(User::getRole, role);
        }
        if (enabled != null) {
            wrapper.eq(User::getEnabled, enabled);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, kw).or().like(User::getNickname, kw).or().like(User::getEmail, kw));
        }
        wrapper.orderByDesc(User::getCreatedAt).orderByAsc(User::getId);

        Page<User> result = userMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        Map<Long, Integer> submissionCounts = countSubmissions(result.getRecords());

        List<AdminUserListItemDto> items = new ArrayList<>();
        for (User user : result.getRecords()) {
            items.add(AdminUserListItemDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .enabled(user.getEnabled())
                    .level(user.getLevel())
                    .levelName(user.getLevelName())
                    .continuousDays(user.getContinuousDays())
                    .totalStudyDays(user.getTotalStudyDays())
                    .submissionCount(submissionCounts.getOrDefault(user.getId(), 0))
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build());
        }
        return new PageResult<>(items, result.getTotal(), safePage, safeSize);
    }

    /**
     * 用户详情(GET /api/admin/users/{id})。
     *
     * @param id 用户 ID
     * @return 详情(不含密码)
     */
    public AdminUserDetailDto detail(Long id) {
        User user = findUser(id);
        UserSetting setting = userSettingMapper.selectById(id);
        return AdminUserDetailDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .level(user.getLevel())
                .levelName(user.getLevelName())
                .continuousDays(user.getContinuousDays())
                .totalStudyDays(user.getTotalStudyDays())
                .submissionCount(countByUser(practiceSubmissionMapper.selectCount(
                        Wrappers.<PracticeSubmission>lambdaQuery().eq(PracticeSubmission::getUserId, id))))
                .pendingSubmissionCount(countByUser(practiceSubmissionMapper.selectCount(
                        Wrappers.<PracticeSubmission>lambdaQuery()
                                .eq(PracticeSubmission::getUserId, id)
                                .eq(PracticeSubmission::getStatus, "pending"))))
                .examSubmissionCount(countByUser(examSubmissionMapper.selectCount(
                        Wrappers.<ExamSubmission>lambdaQuery().eq(ExamSubmission::getUserId, id))))
                .mistakeCount(countByUser(mistakeMapper.selectCount(
                        Wrappers.<Mistake>lambdaQuery().eq(Mistake::getUserId, id))))
                .learnedWordCount(countByUser(wordProgressMapper.selectCount(
                        Wrappers.<UserWordProgress>lambdaQuery().eq(UserWordProgress::getUserId, id))))
                .gradingPreference(setting == null ? null : setting.getGradingMethod())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 启用/停用(PATCH /api/admin/users/{id}/status)。
     *
     * @param id      用户 ID
     * @param enabled 目标状态
     */
    @Transactional
    public void setEnabled(Long id, Boolean enabled) {
        if (enabled == null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "enabled 不能为空(true 启用 / false 停用)");
        }
        User user = findUser(id);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!enabled && id.equals(currentUserId)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "不能停用当前登录账号");
        }
        if (!enabled && isAdmin(user) && countEnabledAdmins() <= 1) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "系统必须保留至少一个可用的管理员账号");
        }
        if (Boolean.valueOf(enabled).equals(user.getEnabled())) {
            // 状态没变也返回成功,但不动库:前端重复点击不该报错
            return;
        }
        user.setEnabled(enabled);
        userMapper.updateById(user);
        log.info("管理端账号启用状态变更: userId={} enabled={}", id, enabled);
    }

    /**
     * 角色调整(PATCH /api/admin/users/{id}/role)。
     *
     * @param id   用户 ID
     * @param role 目标角色
     */
    @Transactional
    public void setRole(Long id, String role) {
        if (role == null || !VALID_ROLES.contains(role)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "role 仅接受 student/admin");
        }
        User user = findUser(id);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (isAdmin(user) && User.ROLE_STUDENT.equals(role)) {
            if (id.equals(currentUserId)) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "不能降权当前登录账号(会把自己关在后台外)");
            }
            if (countAdmins() <= 1) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "系统必须保留至少一个管理员账号");
            }
        }
        if (role.equals(user.getRole())) {
            return;
        }
        user.setRole(role);
        userMapper.updateById(user);
        log.info("管理端账号角色变更: userId={} role={}", id, role);
    }

    // ==================== 小工具 ====================

    /**
     * 是否管理员角色。
     *
     * @param user 用户
     * @return 是 admin
     */
    private boolean isAdmin(User user) {
        return User.ROLE_ADMIN.equals(user.getRole());
    }

    /**
     * 管理员总数(含已停用的,用于"降权后还剩几个管理员")。
     *
     * @return 数量
     */
    private long countAdmins() {
        Long count = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getRole, User.ROLE_ADMIN));
        return count == null ? 0L : count;
    }

    /**
     * 可用管理员数(enabled=1),用于"停用最后一个能登进去的管理员"。
     *
     * @return 数量
     */
    private long countEnabledAdmins() {
        Long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getRole, User.ROLE_ADMIN)
                .eq(User::getEnabled, true));
        return count == null ? 0L : count;
    }

    /**
     * 批量统计当前页用户的练习提交数(避免逐行查)。
     *
     * @param users 当前页用户
     * @return 用户 ID -> 提交数
     */
    private Map<Long, Integer> countSubmissions(List<User> users) {
        Map<Long, Integer> result = new HashMap<>();
        for (User user : users) {
            Long count = practiceSubmissionMapper.selectCount(
                    Wrappers.<PracticeSubmission>lambdaQuery().eq(PracticeSubmission::getUserId, user.getId()));
            result.put(user.getId(), countByUser(count));
        }
        return result;
    }

    /**
     * selectCount 的 Long 收成 int(null 归零)。
     *
     * @param count 计数
     * @return 计数
     */
    private int countByUser(Long count) {
        return count == null ? 0 : count.intValue();
    }

    /**
     * 按 ID 取用户,不存在即 404。
     *
     * @param id 用户 ID
     * @return 用户实体
     */
    private User findUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return user;
    }
}
