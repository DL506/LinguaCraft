package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.practice.SourceDto;
import com.linguacraft.backend.dto.practice.SubmitResultResponse;
import com.linguacraft.backend.dto.user.FavoriteCreateRequest;
import com.linguacraft.backend.dto.user.FavoriteDto;
import com.linguacraft.backend.dto.user.MistakeDto;
import com.linguacraft.backend.dto.user.ScoreTrendDto;
import com.linguacraft.backend.dto.user.SourceAccuracyDto;
import com.linguacraft.backend.dto.user.SubmissionListItemDto;
import com.linguacraft.backend.dto.user.TypeAccuracyDto;
import com.linguacraft.backend.dto.user.UserProfileResponse;
import com.linguacraft.backend.dto.user.UserSettingsResponse;
import com.linguacraft.backend.dto.user.UserSettingsUpdateRequest;
import com.linguacraft.backend.dto.user.UserStatsDto;
import com.linguacraft.backend.entity.Favorite;
import com.linguacraft.backend.entity.Mistake;
import com.linguacraft.backend.entity.PracticeSubmission;
import com.linguacraft.backend.entity.User;
import com.linguacraft.backend.entity.UserSetting;
import com.linguacraft.backend.entity.UserWordProgress;
import com.linguacraft.backend.mapper.FavoriteMapper;
import com.linguacraft.backend.mapper.MistakeMapper;
import com.linguacraft.backend.mapper.PracticeSubmissionMapper;
import com.linguacraft.backend.mapper.UserMapper;
import com.linguacraft.backend.mapper.UserSettingMapper;
import com.linguacraft.backend.mapper.UserWordProgressMapper;
import com.linguacraft.backend.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 用户服务:用户资料与设置、个人中心(提交记录 / 错题本 / 收藏 / 学习统计)。
 *
 * <p>统计口径(已确认):totalStudyTime=Σduration/60;totalWords=有进度的单词数(distinct);
 * totalReading=阅读提交数;accuracy=Σ正确/Σ题;typeAccuracy 键 grammar 映射 grammar-fill,
 * writing 按得分率;scoreTrend 为近 7 天"截至当日累计得分率";sourceAccuracy 无数据返回 -1;
 * pendingGradingCount 仅统计练习 pending(模考不入"我的提交",既定口径)。</p>
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    /** 提交列表合法筛选值 */
    private static final Set<String> SUBMISSION_STATUS_FILTERS =
            Set.of("all", "graded", "partially_graded", "pending");

    private final UserMapper userMapper;
    private final UserSettingMapper userSettingMapper;
    private final PracticeSubmissionMapper submissionMapper;
    private final MistakeMapper mistakeMapper;
    private final FavoriteMapper favoriteMapper;
    private final UserWordProgressMapper progressMapper;
    private final PracticeService practiceService;
    private final ObjectMapper objectMapper;

    public UserService(UserMapper userMapper,
                       UserSettingMapper userSettingMapper,
                       PracticeSubmissionMapper submissionMapper,
                       MistakeMapper mistakeMapper,
                       FavoriteMapper favoriteMapper,
                       UserWordProgressMapper progressMapper,
                       PracticeService practiceService,
                       ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.userSettingMapper = userSettingMapper;
        this.submissionMapper = submissionMapper;
        this.mistakeMapper = mistakeMapper;
        this.favoriteMapper = favoriteMapper;
        this.progressMapper = progressMapper;
        this.practiceService = practiceService;
        this.objectMapper = objectMapper;
    }

    // ==================== 资料与设置 ====================

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
        return toSettingsResponse(setting);
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

    // ==================== 我的提交 ====================

    /**
     * 提交记录列表(GET /api/user/submissions):四态筛选 + 分页(仅练习提交)。
     *
     * @param status 筛选:all/graded/partially_graded/pending(缺省 all)
     * @param page   页码
     * @param size   每页条数(1~100)
     * @return 分页提交列表
     */
    public PageResult<SubmissionListItemDto> submissions(String status, long page, long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        String filter = (status == null || status.isBlank()) ? "all" : status;
        if (!SUBMISSION_STATUS_FILTERS.contains(filter)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "status 仅接受 all/graded/partially_graded/pending");
        }
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<PracticeSubmission>lambdaQuery()
                .eq(PracticeSubmission::getUserId, userId)
                .orderByDesc(PracticeSubmission::getCreatedAt);
        if (!"all".equals(filter)) {
            wrapper.eq(PracticeSubmission::getStatus, filter);
        }
        Page<PracticeSubmission> submissionPage = submissionMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        return new PageResult<>(submissionPage.getRecords().stream().map(this::toSubmissionItem).toList(),
                submissionPage.getTotal(), safePage, safeSize);
    }

    /**
     * 提交详情(GET /api/user/submissions/{id}):复用统一 SubmitResult 结构。
     *
     * @param id 提交记录 ID
     * @return SubmitResult / WritingSubmitResult 结构
     * @throws BizException 不存在或不属于当前用户(code 404)
     */
    public SubmitResultResponse submissionDetail(Long id) {
        PracticeSubmission submission = submissionMapper.selectById(id);
        if (submission == null || !Objects.equals(submission.getUserId(), SecurityUtils.getCurrentUserId())) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }
        return practiceService.buildResult(submission);
    }

    // ==================== 错题本 ====================

    /**
     * 错题列表(GET /api/user/mistakes):来源与题型筛选 + 分页。
     *
     * @param source 来源筛选(可空)
     * @param type   题型筛选(可空)
     * @param page   页码
     * @param size   每页条数
     * @return 分页错题列表
     */
    public PageResult<MistakeDto> mistakes(String source, String type, long page, long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<Mistake>lambdaQuery()
                .eq(Mistake::getUserId, userId)
                .orderByDesc(Mistake::getCreatedAt);
        if (source != null && !source.isBlank()) {
            wrapper.eq(Mistake::getSourceType, source);
        }
        if (type != null && !type.isBlank()) {
            wrapper.eq(Mistake::getType, type);
        }
        Page<Mistake> mistakePage = mistakeMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        return new PageResult<>(mistakePage.getRecords().stream().map(this::toMistakeDto).toList(),
                mistakePage.getTotal(), safePage, safeSize);
    }

    /**
     * 删除错题(DELETE /api/user/mistakes/{id})。
     *
     * @param id 错题记录 ID
     * @throws BizException 不存在或不属于当前用户(code 404)
     */
    public void deleteMistake(Long id) {
        Mistake mistake = mistakeMapper.selectById(id);
        if (mistake == null || !Objects.equals(mistake.getUserId(), SecurityUtils.getCurrentUserId())) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "错题不存在");
        }
        mistakeMapper.deleteById(id);
    }

    // ==================== 收藏 ====================

    /**
     * 收藏列表(GET /api/user/favorites)。
     *
     * @param page 页码
     * @param size 每页条数
     * @return 分页收藏列表
     */
    public PageResult<FavoriteDto> favorites(long page, long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        Page<Favorite> favoritePage = favoriteMapper.selectPage(new Page<>(safePage, safeSize),
                Wrappers.<Favorite>lambdaQuery()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt));
        return new PageResult<>(favoritePage.getRecords().stream().map(this::toFavoriteDto).toList(),
                favoritePage.getTotal(), safePage, safeSize);
    }

    /**
     * 新增收藏(POST /api/user/favorites)。
     *
     * @param request 收藏请求(type/content/refId?)
     * @return 新增的收藏
     * @throws BizException 类型或内容非法(code 400)
     */
    public FavoriteDto addFavorite(FavoriteCreateRequest request) {
        if (request == null || request.getType() == null
                || (!"word".equals(request.getType()) && !"sentence".equals(request.getType()))) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "收藏类型仅接受 word 或 sentence");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "收藏内容不能为空");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(SecurityUtils.getCurrentUserId());
        favorite.setType(request.getType());
        favorite.setContent(request.getContent());
        favorite.setRefId(request.getRefId());
        favoriteMapper.insert(favorite);
        return toFavoriteDto(favorite);
    }

    /**
     * 删除收藏(DELETE /api/user/favorites/{id})。
     *
     * @param id 收藏记录 ID
     * @throws BizException 不存在或不属于当前用户(code 404)
     */
    public void deleteFavorite(Long id) {
        Favorite favorite = favoriteMapper.selectById(id);
        if (favorite == null || !Objects.equals(favorite.getUserId(), SecurityUtils.getCurrentUserId())) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "收藏不存在");
        }
        favoriteMapper.deleteById(id);
    }

    // ==================== 学习统计 ====================

    /**
     * 学习统计(GET /api/user/stats):口径详见类注释。
     *
     * @return 统计响应(含题型得分率/趋势/来源正确率/待批改数)
     */
    public UserStatsDto stats() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<PracticeSubmission> submissions = submissionMapper.selectList(
                Wrappers.<PracticeSubmission>lambdaQuery()
                        .eq(PracticeSubmission::getUserId, userId)
                        .orderByAsc(PracticeSubmission::getCreatedAt));

        StatsAccumulator accumulator = new StatsAccumulator();
        for (PracticeSubmission submission : submissions) {
            accumulator.accumulate(submission);
        }
        int totalWords = Math.toIntExact(progressMapper.selectCount(
                Wrappers.<UserWordProgress>lambdaQuery().eq(UserWordProgress::getUserId, userId)).intValue());

        return UserStatsDto.builder()
                .totalStudyTime(accumulator.durationSeconds / 60)
                .totalWords(totalWords)
                .totalReading(accumulator.totalReadingCount)
                .accuracy(accumulator.overallAccuracy())
                .pendingGradingCount(accumulator.pendingCount)
                .typeAccuracy(TypeAccuracyDto.builder()
                        .reading(accumulator.typeAccuracy("reading"))
                        .matching(accumulator.typeAccuracy("matching"))
                        .cloze(accumulator.typeAccuracy("cloze"))
                        .grammar(accumulator.typeAccuracy("grammar-fill"))
                        .writing(accumulator.writingAccuracy())
                        .build())
                .scoreTrend(buildScoreTrend(submissions))
                .sourceAccuracy(SourceAccuracyDto.builder()
                        .real(accumulator.sourceAccuracy("real"))
                        .teacher(accumulator.sourceAccuracy("teacher"))
                        .ai(accumulator.sourceAccuracy("ai"))
                        .build())
                .build();
    }

    // ==================== 内部工具 ====================

    /**
     * 近 7 天得分趋势(截至当日累计得分率;无数据的日期沿用前一有效累计值)。
     *
     * @param sortedAsc 按创建时间升序的提交列表
     * @return 趋势点列表(最早在前)
     */
    private List<ScoreTrendDto> buildScoreTrend(List<PracticeSubmission> sortedAsc) {
        List<ScoreTrendDto> trend = new ArrayList<>();
        StatsAccumulator running = new StatsAccumulator();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        int pointer = 0;
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime endExclusive = day.plusDays(1).atStartOfDay();
            while (pointer < sortedAsc.size()
                    && sortedAsc.get(pointer).getCreatedAt() != null
                    && sortedAsc.get(pointer).getCreatedAt().isBefore(endExclusive)) {
                running.accumulate(sortedAsc.get(pointer));
                pointer++;
            }
            trend.add(ScoreTrendDto.builder()
                    .date(day.format(formatter))
                    .reading(running.typeAccuracy("reading"))
                    .matching(running.typeAccuracy("matching"))
                    .cloze(running.typeAccuracy("cloze"))
                    .grammar(running.typeAccuracy("grammar-fill"))
                    .writing(running.writingAccuracy())
                    .build());
        }
        return trend;
    }

    /**
     * 提交实体 -> 列表项。
     *
     * @param submission 提交实体
     * @return 列表项
     */
    private SubmissionListItemDto toSubmissionItem(PracticeSubmission submission) {
        return SubmissionListItemDto.builder()
                .id(submission.getId())
                .type(submission.getType())
                .title(submission.getTitle())
                .source(toSource(submission.getSourceMeta(), submission.getSourceType()))
                .status(submission.getStatus())
                .score(submission.getScore())
                .totalScore(submission.getTotalScore())
                .submittedAt(submission.getSubmittedAt())
                .estimatedTime(submission.getEstimatedTime())
                .build();
    }

    /**
     * 错题实体 -> 响应。
     *
     * @param mistake 错题实体
     * @return 响应
     */
    private MistakeDto toMistakeDto(Mistake mistake) {
        return MistakeDto.builder()
                .id(mistake.getId())
                .questionId(mistake.getQuestionId())
                .type(mistake.getType())
                .source(toSource(mistake.getSourceMeta(), mistake.getSourceType()))
                .stem(mistake.getStem())
                .userAnswer(mistake.getUserAnswer())
                .correctAnswer(mistake.getCorrectAnswer())
                .explanation(mistake.getExplanation())
                .createdAt(mistake.getCreatedAt())
                .build();
    }

    /**
     * 收藏实体 -> 响应。
     *
     * @param favorite 收藏实体
     * @return 响应
     */
    private FavoriteDto toFavoriteDto(Favorite favorite) {
        return FavoriteDto.builder()
                .id(favorite.getId())
                .type(favorite.getType())
                .content(favorite.getContent())
                .refId(favorite.getRefId())
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    /**
     * 来源快照 JSON -> SourceDto(取 meta 子节点)。
     *
     * @param sourceMeta   来源快照 JSON(完整 source 对象)
     * @param fallbackType 解析失败时的来源类型
     * @return SourceDto
     */
    private SourceDto toSource(String sourceMeta, String fallbackType) {
        JsonNode node = readTree(sourceMeta);
        return SourceDto.builder()
                .type(node != null && node.hasNonNull("type") ? node.get("type").asText() : fallbackType)
                .meta(node == null ? null : node.get("meta"))
                .build();
    }

    /**
     * JSON 解析(容错:失败返回 null)。
     *
     * @param json JSON 字符串
     * @return JsonNode 或 null
     */
    private JsonNode readTree(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
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
    private UserSettingsResponse toSettingsResponse(UserSetting setting) {
        return UserSettingsResponse.builder()
                .dailyWordGoal(setting.getDailyWordGoal())
                .dailyReadingGoal(setting.getDailyReadingGoal())
                .accent(setting.getAccent())
                .grammarFillCaseSensitive(setting.getGrammarFillCaseSensitive())
                .gradingMethod(setting.getGradingMethod())
                .reminderTime(setting.getReminderTime())
                .build();
    }

    /**
     * 统计累加器:单次遍历聚合总正确率/题型/来源/作文得分率/时长/待批改数,并支持趋势快照。
     */
    private static final class StatsAccumulator {

        /** 累计作答时长(秒) */
        int durationSeconds;

        /** 阅读提交数 */
        int totalReadingCount;

        /** 待批改(pending)数 */
        int pendingCount;

        /** 客观题正确数合计 */
        int correctSum;

        /** 客观题总题数合计 */
        int itemSum;

        /** 题型 -> [正确数, 题数](客观题;grammar-fill 为原始键) */
        final Map<String, int[]> typeAgg = new HashMap<>();

        /** 来源 -> [正确数, 题数] */
        final Map<String, int[]> sourceAgg = new HashMap<>();

        /** 作文得分合计 */
        BigDecimal writingScoreSum = BigDecimal.ZERO;

        /** 作文总分合计 */
        BigDecimal writingTotalSum = BigDecimal.ZERO;

        /**
         * 累加一条提交(pending 仅计入时长/阅读数/待批改数)。
         *
         * @param submission 提交记录
         */
        void accumulate(PracticeSubmission submission) {
            durationSeconds += submission.getDuration() == null ? 0 : submission.getDuration();
            if ("reading".equals(submission.getType())) {
                totalReadingCount++;
            }
            if ("pending".equals(submission.getStatus())) {
                pendingCount++;
            }
            if (!"graded".equals(submission.getStatus())) {
                return;
            }
            if (submission.getCorrectCount() != null && submission.getTotalCount() != null) {
                correctSum += submission.getCorrectCount();
                itemSum += submission.getTotalCount();
                int[] type = typeAgg.computeIfAbsent(submission.getType(), key -> new int[2]);
                type[0] += submission.getCorrectCount();
                type[1] += submission.getTotalCount();
                if (submission.getSourceType() != null) {
                    int[] source = sourceAgg.computeIfAbsent(submission.getSourceType(), key -> new int[2]);
                    source[0] += submission.getCorrectCount();
                    source[1] += submission.getTotalCount();
                }
            }
            if ("writing".equals(submission.getType())
                    && submission.getScore() != null && submission.getTotalScore() != null
                    && submission.getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
                writingScoreSum = writingScoreSum.add(submission.getScore());
                writingTotalSum = writingTotalSum.add(submission.getTotalScore());
            }
        }

        /**
         * 百分比(0-100 取整;无数据 0)。
         */
        int percent(int correct, int total) {
            return total > 0 ? (int) Math.round(correct * 100.0 / total) : 0;
        }

        /** 总正确率(0-100)。 */
        int overallAccuracy() {
            return percent(correctSum, itemSum);
        }

        /** 题型得分率(0-100;键为实践题型,语法填空传 grammar-fill)。 */
        int typeAccuracy(String type) {
            int[] value = typeAgg.get(type);
            return value == null ? 0 : percent(value[0], value[1]);
        }

        /** 作文得分率(0-100)。 */
        int writingAccuracy() {
            return writingTotalSum.compareTo(BigDecimal.ZERO) > 0
                    ? (int) Math.round(writingScoreSum.doubleValue() * 100.0 / writingTotalSum.doubleValue())
                    : 0;
        }

        /** 来源正确率(0-100;无数据 -1)。 */
        int sourceAccuracy(String source) {
            int[] value = sourceAgg.get(source);
            return value == null ? -1 : percent(value[0], value[1]);
        }
    }
}