package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.entity.User;
import com.linguacraft.backend.mapper.ExamSubmissionMapper;
import com.linguacraft.backend.mapper.MistakeMapper;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.UserMapper;
import com.linguacraft.backend.mapper.UserSettingMapper;
import com.linguacraft.backend.mapper.UserWordProgressMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理端用户服务测试(阶段 A5):停用与调角色的三条护栏。
 *
 * <p>用真实的 SecurityContext 注入"当前登录用户",而不是静态 mock —— 与被测代码读
 * {@code SecurityUtils.getCurrentUserId()} 的方式完全一致。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminUserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserSettingMapper userSettingMapper;

    @Mock
    private PracticeSubmissionMapper practiceSubmissionMapper;

    @Mock
    private ExamSubmissionMapper examSubmissionMapper;

    @Mock
    private MistakeMapper mistakeMapper;

    @Mock
    private UserWordProgressMapper wordProgressMapper;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    /** 把当前登录用户设成指定 ID(与 JwtAuthFilter 写入的 principal 形状一致) */
    private void loginAs(long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of()));
    }

    private User user(long id, String role, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setUsername(id == 2L ? "admin" : "demo");
        user.setNickname(id == 2L ? "管理员" : "小明");
        user.setEmail(id + "@example.com");
        user.setRole(role);
        user.setEnabled(enabled);
        return user;
    }

    private AdminUserService newService() {
        return new AdminUserService(userMapper, userSettingMapper, practiceSubmissionMapper,
                examSubmissionMapper, mistakeMapper, wordProgressMapper);
    }

    @Test
    @DisplayName("不能停用当前登录账号")
    void rejectsSelfDisable() {
        loginAs(2L);
        when(userMapper.selectById(2L)).thenReturn(user(2L, User.ROLE_ADMIN, true));

        assertThatThrownBy(() -> newService().setEnabled(2L, false))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能停用当前登录账号");
    }

    @Test
    @DisplayName("不能停用最后一个可用管理员")
    void rejectsDisablingLastAdmin() {
        loginAs(2L);
        when(userMapper.selectById(5L)).thenReturn(user(5L, User.ROLE_ADMIN, true));
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> newService().setEnabled(5L, false))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("系统必须保留至少一个可用的管理员账号");
    }

    @Test
    @DisplayName("停用普通学生账号生效并落库")
    void disablesOrdinaryStudent() {
        loginAs(2L);
        User target = user(1L, User.ROLE_STUDENT, true);
        when(userMapper.selectById(1L)).thenReturn(target);

        newService().setEnabled(1L, false);

        assertThat(target.getEnabled()).isFalse();
        verify(userMapper).updateById(target);
    }

    @Test
    @DisplayName("目标状态与当前一致时不动库(重复点击不报错也不写)")
    void skipsNoopStatusChange() {
        loginAs(2L);
        when(userMapper.selectById(1L)).thenReturn(user(1L, User.ROLE_STUDENT, false));

        assertThatCode(() -> newService().setEnabled(1L, false)).doesNotThrowAnyException();

        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("enabled 缺省即拒绝,避免误改状态")
    void requiresEnabledParam() {
        assertThatThrownBy(() -> newService().setEnabled(1L, null))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("enabled 不能为空");
    }

    @Test
    @DisplayName("不能把自己降权(会失去后台入口)")
    void rejectsSelfDemotion() {
        loginAs(2L);
        when(userMapper.selectById(2L)).thenReturn(user(2L, User.ROLE_ADMIN, true));

        assertThatThrownBy(() -> newService().setRole(2L, User.ROLE_STUDENT))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能降权当前登录账号");
    }

    @Test
    @DisplayName("不能降权最后一个管理员")
    void rejectsDemotingLastAdmin() {
        loginAs(2L);
        when(userMapper.selectById(5L)).thenReturn(user(5L, User.ROLE_ADMIN, true));
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> newService().setRole(5L, User.ROLE_STUDENT))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("系统必须保留至少一个管理员账号");
    }

    @Test
    @DisplayName("升权为学生为管理员不受最后一名限制")
    void promotesStudentToAdmin() {
        loginAs(2L);
        User target = user(1L, User.ROLE_STUDENT, true);
        when(userMapper.selectById(1L)).thenReturn(target);
        when(userMapper.selectCount(any())).thenReturn(1L);

        newService().setRole(1L, User.ROLE_ADMIN);

        assertThat(target.getRole()).isEqualTo(User.ROLE_ADMIN);
        verify(userMapper).updateById(target);
    }

    @Test
    @DisplayName("角色白名单外的值直接拒绝")
    void rejectsUnknownRole() {
        assertThatThrownBy(() -> newService().setRole(1L, "superuser"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("role 仅接受 student/admin");

        assertThatThrownBy(() -> newService().list(null, "root", null, 1, 20))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("role 仅接受 student/admin");
    }

    @Test
    @DisplayName("用户不存在时 404")
    void throwsNotFoundForMissingUser() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> newService().detail(99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    @DisplayName("详情不返回密码,并汇总学习量")
    void detailOmitsPasswordAndSummarizes() {
        User target = user(1L, User.ROLE_STUDENT, true);
        target.setPassword("should-never-be-serialized");
        when(userMapper.selectById(1L)).thenReturn(target);
        when(practiceSubmissionMapper.selectCount(any(Wrapper.class))).thenReturn(4L, 1L);
        when(examSubmissionMapper.selectCount(any())).thenReturn(2L);
        when(mistakeMapper.selectCount(any())).thenReturn(3L);
        when(wordProgressMapper.selectCount(any())).thenReturn(12L);

        var detail = newService().detail(1L);

        assertThat(detail.getSubmissionCount()).isEqualTo(4);
        assertThat(detail.getPendingSubmissionCount()).isEqualTo(1);
        assertThat(detail.getExamSubmissionCount()).isEqualTo(2);
        assertThat(detail.getMistakeCount()).isEqualTo(3);
        assertThat(detail.getLearnedWordCount()).isEqualTo(12);
    }
}
