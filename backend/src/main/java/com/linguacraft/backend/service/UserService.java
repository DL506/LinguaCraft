package com.linguacraft.backend.service;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.user.UserProfileResponse;
import com.linguacraft.backend.dto.user.UserSettingsResponse;
import com.linguacraft.backend.dto.user.UserSettingsUpdateRequest;
import com.linguacraft.backend.entity.User;
import com.linguacraft.backend.entity.UserSetting;
import com.linguacraft.backend.mapper.UserMapper;
import com.linguacraft.backend.mapper.UserSettingMapper;
import org.springframework.stereotype.Service;

/**
 * 用户服务:用户资料与设置的读取、部分更新。
 */
@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserSettingMapper userSettingMapper;

    public UserService(UserMapper userMapper, UserSettingMapper userSettingMapper) {
        this.userMapper = userMapper;
        this.userSettingMapper = userSettingMapper;
    }

    /**
     * 获取用户资料(GET /api/user/profile)。
     *
     * @param userId 当前登录用户 ID
     * @return 用户资料响应
     * @throws BizException 用户不存在(code 404)
     */
    public UserProfileResponse getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .level(user.getLevel())
                .levelName(user.getLevelName())
                .continuousDays(user.getContinuousDays())
                .totalStudyDays(user.getTotalStudyDays())
                .build();
    }

    /**
     * 获取用户设置(GET /api/user/settings);设置缺失时懒创建默认值。
     *
     * @param userId 当前登录用户 ID
     * @return 用户设置响应
     */
    public UserSettingsResponse getSettings(Long userId) {
        UserSetting setting = ensureSetting(userId);
        return toResponse(setting);
    }

    /**
     * 更新用户设置(PUT /api/user/settings,部分更新:未提供字段保持不变)。
     *
     * @param userId  当前登录用户 ID
     * @param request 更新请求(null 字段忽略)
     * @throws BizException 枚举字段取值非法(code 400)
     */
    public void updateSettings(Long userId, UserSettingsUpdateRequest request) {
        if (request.getAccent() != null && !"us".equals(request.getAccent()) && !"uk".equals(request.getAccent())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "口音仅接受 us 或 uk");
        }
        if (request.getGradingMethod() != null
                && !"ai".equals(request.getGradingMethod()) && !"manual".equals(request.getGradingMethod())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "批改方式仅接受 ai 或 manual");
        }

        // 确保设置行存在(历史用户缺失时懒创建)
        ensureSetting(userId);

        // MyBatis-Plus updateById 默认忽略 null 字段,天然实现部分更新语义
        UserSetting patch = new UserSetting();
        patch.setUserId(userId);
        patch.setDailyWordGoal(request.getDailyWordGoal());
        patch.setDailyReadingGoal(request.getDailyReadingGoal());
        patch.setAccent(request.getAccent());
        patch.setGrammarFillCaseSensitive(request.getGrammarFillCaseSensitive());
        patch.setGradingMethod(request.getGradingMethod());
        patch.setReminderTime(request.getReminderTime());
        userSettingMapper.updateById(patch);
    }

    /**
     * 读取设置行;不存在时创建默认设置并返回。
     *
     * @param userId 用户 ID
     * @return 设置实体
     */
    private UserSetting ensureSetting(Long userId) {
        UserSetting setting = userSettingMapper.selectById(userId);
        if (setting == null) {
            setting = new UserSetting();
            setting.setUserId(userId);
            setting.setDailyWordGoal(50);
            setting.setDailyReadingGoal(1);
            setting.setAccent("us");
            setting.setGrammarFillCaseSensitive(false);
            setting.setGradingMethod("ai");
            setting.setReminderTime("08:00");
            userSettingMapper.insert(setting);
        }
        return setting;
    }

    /**
     * 设置实体 → 响应 DTO。
     *
     * @param setting 设置实体
     * @return 设置响应
     */
    private UserSettingsResponse toResponse(UserSetting setting) {
        return UserSettingsResponse.builder()
                .dailyWordGoal(setting.getDailyWordGoal())
                .dailyReadingGoal(setting.getDailyReadingGoal())
                .accent(setting.getAccent())
                .grammarFillCaseSensitive(setting.getGrammarFillCaseSensitive())
                .gradingMethod(setting.getGradingMethod())
                .reminderTime(setting.getReminderTime())
                .build();
    }
}