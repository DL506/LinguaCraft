package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.auth.AuthResult;
import com.linguacraft.backend.dto.auth.AuthUser;
import com.linguacraft.backend.dto.auth.ForgotPasswordRequest;
import com.linguacraft.backend.dto.auth.LoginRequest;
import com.linguacraft.backend.dto.auth.RegisterRequest;
import com.linguacraft.backend.dto.auth.ResetPasswordRequest;
import com.linguacraft.backend.entity.PasswordResetToken;
import com.linguacraft.backend.entity.User;
import com.linguacraft.backend.entity.UserSetting;
import com.linguacraft.backend.mapper.PasswordResetTokenMapper;
import com.linguacraft.backend.mapper.UserMapper;
import com.linguacraft.backend.mapper.UserSettingMapper;
import com.linguacraft.backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 认证服务:登录、注册(注册即登录)、忘记密码、重置密码。
 *
 * <p>契约要点:错误码沿用前端 mock(1001 登录失败 / 1002 用户名已存在);
 * 密码 BCrypt 加密;重置令牌一次性、30 分钟有效。</p>
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** 重置令牌有效期(分钟) */
    private static final long RESET_TOKEN_TTL_MINUTES = 30;

    private final UserMapper userMapper;
    private final UserSettingMapper userSettingMapper;
    private final PasswordResetTokenMapper passwordResetTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MailService mailService;

    /** 重置密码邮件中链接指向的前端页面地址(lc.mail.reset-base-url) */
    private final String resetBaseUrl;

    public AuthService(UserMapper userMapper,
                       UserSettingMapper userSettingMapper,
                       PasswordResetTokenMapper passwordResetTokenMapper,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       MailService mailService,
                       @Value("${lc.mail.reset-base-url}") String resetBaseUrl) {
        this.userMapper = userMapper;
        this.userSettingMapper = userSettingMapper;
        this.passwordResetTokenMapper = passwordResetTokenMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mailService = mailService;
        this.resetBaseUrl = resetBaseUrl;
    }

    /**
     * 登录:校验用户名与密码,成功签发 JWT。
     *
     * @param request 登录请求
     * @return 令牌与用户信息
     * @throws BizException 用户名不存在或密码错误(code 1001)
     */
    public AuthResult login(LoginRequest request) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));
        String password = request.getPassword();
        if (user == null || password == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        return buildAuthResult(user);
    }

    /**
     * 注册(注册即登录):创建用户与默认设置,直接签发 JWT。
     *
     * @param request 注册请求(已通过 Bean Validation)
     * @return 令牌与用户信息
     * @throws BizException 用户名已存在(code 1002)或邮箱已被注册(code 400)
     */
    public AuthResult register(RegisterRequest request) {
        if (userMapper.exists(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()))) {
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }
        if (userMapper.exists(Wrappers.<User>lambdaQuery().eq(User::getEmail, request.getEmail()))) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "该邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册竞态兜底(用户名/邮箱唯一索引)
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "用户名或邮箱已被注册");
        }
        createDefaultSetting(user.getId());
        return buildAuthResult(user);
    }

    /**
     * 忘记密码:邮箱存在时生成一次性重置令牌并发送邮件;邮箱不存在同样返回成功(防账号枚举)。
     *
     * @param request 忘记密码请求
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getEmail, request.getEmail()));
        if (user == null) {
            log.info("忘记密码:邮箱未注册,静默返回(防账号枚举): {}", request.getEmail());
            return;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken entity = new PasswordResetToken();
        entity.setUserId(user.getId());
        entity.setToken(token);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES));
        entity.setUsed(false);
        passwordResetTokenMapper.insert(entity);

        String resetLink = resetBaseUrl + "?token=" + token;
        mailService.sendResetMail(user.getEmail(), resetLink);
    }

    /**
     * 重置密码:校验令牌(存在、未使用、未过期),更新密码并作废令牌。
     *
     * @param request 重置密码请求
     * @throws BizException 令牌无效或已过期(code 400)
     */
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken entity = passwordResetTokenMapper.selectOne(
                Wrappers.<PasswordResetToken>lambdaQuery().eq(PasswordResetToken::getToken, request.getToken()));
        if (entity == null || Boolean.TRUE.equals(entity.getUsed())
                || entity.getExpiresAt() == null
                || entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "重置链接无效或已过期");
        }
        User user = userMapper.selectById(entity.getUserId());
        if (user == null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "重置链接无效或已过期");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        // 一次性:立即作废
        entity.setUsed(true);
        passwordResetTokenMapper.updateById(entity);
    }

    /**
     * 构建认证响应(签发令牌 + 用户信息)。
     *
     * @param user 用户实体
     * @return 认证响应
     */
    private AuthResult buildAuthResult(User user) {
        return AuthResult.builder()
                .token(jwtUtil.generateToken(user.getId(), user.getUsername()))
                .expiresIn(jwtUtil.getExpireSeconds())
                .user(AuthUser.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .build())
                .build();
    }

    /**
     * 为新注册用户创建默认设置(与前端 mock 默认值一致)。
     *
     * @param userId 用户 ID
     */
    private void createDefaultSetting(Long userId) {
        UserSetting setting = new UserSetting();
        setting.setUserId(userId);
        setting.setDailyWordGoal(50);
        setting.setDailyReadingGoal(1);
        setting.setAccent("us");
        setting.setGrammarFillCaseSensitive(false);
        setting.setGradingMethod("ai");
        setting.setReminderTime("08:00");
        userSettingMapper.insert(setting);
    }
}