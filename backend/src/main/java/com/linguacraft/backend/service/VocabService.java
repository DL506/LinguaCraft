package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.vocab.VocabBookDto;
import com.linguacraft.backend.dto.vocab.VocabReviewRequest;
import com.linguacraft.backend.dto.vocab.VocabTodayTaskDto;
import com.linguacraft.backend.dto.vocab.WordDto;
import com.linguacraft.backend.entity.UserSetting;
import com.linguacraft.backend.entity.UserWordProgress;
import com.linguacraft.backend.entity.UserWordReview;
import com.linguacraft.backend.entity.VocabBook;
import com.linguacraft.backend.entity.VocabWord;
import com.linguacraft.backend.mapper.UserSettingMapper;
import com.linguacraft.backend.mapper.UserWordProgressMapper;
import com.linguacraft.backend.mapper.UserWordReviewMapper;
import com.linguacraft.backend.mapper.VocabBookMapper;
import com.linguacraft.backend.mapper.VocabWordMapper;
import com.linguacraft.backend.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 背单词服务:词书 / 词表 / 记忆自评(间隔复习)/ 今日任务 / 到期复习。
 *
 * <p>间隔复习口径(已确认):unknown +1 天 / fuzzy +2 天 / known +7 天;
 * 今日任务口径:newCount=今日首次学习数、reviewCount=待复习数(nextReviewAt <= now)、
 * doneCount=今日复习提交数、target=用户设置 dailyWordGoal(默认 50)。</p>
 */
@Service
public class VocabService {

    private static final Logger log = LoggerFactory.getLogger(VocabService.class);

    /** 合法自评集合 */
    private static final Set<String> VALID_RATINGS = Set.of("unknown", "fuzzy", "known");

    /** 默认每日背单词目标(用户设置缺省时) */
    private static final int DEFAULT_DAILY_WORD_GOAL = 50;

    private final VocabBookMapper bookMapper;
    private final VocabWordMapper wordMapper;
    private final UserWordProgressMapper progressMapper;
    private final UserWordReviewMapper reviewMapper;
    private final UserSettingMapper userSettingMapper;
    private final ObjectMapper objectMapper;

    public VocabService(VocabBookMapper bookMapper,
                        VocabWordMapper wordMapper,
                        UserWordProgressMapper progressMapper,
                        UserWordReviewMapper reviewMapper,
                        UserSettingMapper userSettingMapper,
                        ObjectMapper objectMapper) {
        this.bookMapper = bookMapper;
        this.wordMapper = wordMapper;
        this.progressMapper = progressMapper;
        this.reviewMapper = reviewMapper;
        this.userSettingMapper = userSettingMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 词书列表(GET /api/vocab/books):learned 由学习进度表实时派生。
     *
     * @return 词书列表
     */
    public List<VocabBookDto> books() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<VocabBook> books = bookMapper.selectList(
                Wrappers.<VocabBook>lambdaQuery().orderByAsc(VocabBook::getSortOrder));

        // 一次性取全部单词(id -> level)与用户已学词集合,按词书过滤层级计算 learned
        List<VocabWord> allWords = wordMapper.selectList(
                Wrappers.<VocabWord>lambdaQuery().select(VocabWord::getId, VocabWord::getLevel));
        Set<String> learnedIds = new HashSet<>();
        for (UserWordProgress progress : progressMapper.selectList(
                Wrappers.<UserWordProgress>lambdaQuery().eq(UserWordProgress::getUserId, userId))) {
            learnedIds.add(progress.getWordId());
        }

        List<VocabBookDto> result = new ArrayList<>();
        for (VocabBook book : books) {
            int learned = 0;
            for (VocabWord word : allWords) {
                if (!learnedIds.contains(word.getId())) {
                    continue;
                }
                if (book.getFilterLevel() == null || book.getFilterLevel().equals(word.getLevel())) {
                    learned++;
                }
            }
            result.add(VocabBookDto.builder()
                    .id(book.getId())
                    .name(book.getName())
                    .total(book.getTotal())
                    .learned(learned)
                    .build());
        }
        return result;
    }

    /**
     * 词书单词分页(GET /api/vocab/book/{bookId}/words)。
     *
     * @param bookId 词书 ID
     * @param page   页码
     * @param size   每页条数(1~100)
     * @return 分页单词列表
     * @throws BizException 词书不存在(code 404)
     */
    public PageResult<WordDto> bookWords(String bookId, long page, long size) {
        VocabBook book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "词书不存在");
        }
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<VocabWord>lambdaQuery().orderByAsc(VocabWord::getId);
        if (book.getFilterLevel() != null) {
            wrapper.eq(VocabWord::getLevel, book.getFilterLevel());
        }
        Page<VocabWord> wordPage = wordMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        return new PageResult<>(wordPage.getRecords().stream().map(this::toWordDto).toList(),
                wordPage.getTotal(), safePage, safeSize);
    }

    /**
     * 记忆自评(POST /api/vocab/review):更新进度并写复习日志,下次复习时间按间隔口径顺延。
     *
     * @param request 自评请求(wordId/rating)
     * @throws BizException 单词不存在(404)或自评非法(400)
     */
    public void review(VocabReviewRequest request) {
        if (request == null || request.getWordId() == null || request.getRating() == null
                || !VALID_RATINGS.contains(request.getRating())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "记忆自评仅接受 unknown/fuzzy/known");
        }
        VocabWord word = wordMapper.selectById(request.getWordId());
        if (word == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "单词不存在");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReviewAt = now.plusDays(reviewIntervalDays(request.getRating()));

        UserWordProgress progress = progressMapper.selectOne(
                Wrappers.<UserWordProgress>lambdaQuery()
                        .eq(UserWordProgress::getUserId, userId)
                        .eq(UserWordProgress::getWordId, request.getWordId()));
        if (progress == null) {
            progress = new UserWordProgress();
            progress.setUserId(userId);
            progress.setWordId(request.getWordId());
            progress.setRating(request.getRating());
            progress.setReviewCount(1);
            progress.setFirstLearnedAt(now);
            progress.setLastReviewAt(now);
            progress.setNextReviewAt(nextReviewAt);
            progressMapper.insert(progress);
        } else {
            progress.setRating(request.getRating());
            progress.setReviewCount(progress.getReviewCount() == null ? 1 : progress.getReviewCount() + 1);
            progress.setLastReviewAt(now);
            progress.setNextReviewAt(nextReviewAt);
            progressMapper.updateById(progress);
        }

        // 复习日志(今日完成数与统计的数据源)
        UserWordReview review = new UserWordReview();
        review.setUserId(userId);
        review.setWordId(request.getWordId());
        review.setRating(request.getRating());
        review.setReviewedAt(now);
        reviewMapper.insert(review);
    }

    /**
     * 今日背单词任务(GET /api/vocab/task/today)。
     *
     * @return 今日任务(newCount/reviewCount/doneCount/target)
     */
    public VocabTodayTaskDto todayTask() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        int doneCount = Math.toIntExact(reviewMapper.selectCount(
                Wrappers.<UserWordReview>lambdaQuery()
                        .eq(UserWordReview::getUserId, userId)
                        .ge(UserWordReview::getReviewedAt, startOfDay)).intValue());
        int newCount = Math.toIntExact(progressMapper.selectCount(
                Wrappers.<UserWordProgress>lambdaQuery()
                        .eq(UserWordProgress::getUserId, userId)
                        .ge(UserWordProgress::getFirstLearnedAt, startOfDay)).intValue());
        int reviewCount = Math.toIntExact(progressMapper.selectCount(
                Wrappers.<UserWordProgress>lambdaQuery()
                        .eq(UserWordProgress::getUserId, userId)
                        .le(UserWordProgress::getNextReviewAt, now)).intValue());

        UserSetting setting = userSettingMapper.selectById(userId);
        int target = setting != null && setting.getDailyWordGoal() != null
                ? setting.getDailyWordGoal() : DEFAULT_DAILY_WORD_GOAL;
        return VocabTodayTaskDto.builder()
                .newCount(newCount)
                .reviewCount(reviewCount)
                .doneCount(doneCount)
                .target(target)
                .build();
    }

    /**
     * 到期复习单词分页(GET /api/vocab/task/due):nextReviewAt <= now,按到期时间升序。
     *
     * @param page 页码
     * @param size 每页条数
     * @return 分页单词列表
     */
    public PageResult<WordDto> dueWords(long page, long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        Page<UserWordProgress> progressPage = progressMapper.selectPage(new Page<>(safePage, safeSize),
                Wrappers.<UserWordProgress>lambdaQuery()
                        .eq(UserWordProgress::getUserId, userId)
                        .isNotNull(UserWordProgress::getNextReviewAt)
                        .le(UserWordProgress::getNextReviewAt, LocalDateTime.now())
                        .orderByAsc(UserWordProgress::getNextReviewAt));

        List<String> wordIds = progressPage.getRecords().stream().map(UserWordProgress::getWordId).toList();
        Map<String, VocabWord> wordMap = new HashMap<>();
        if (!wordIds.isEmpty()) {
            for (VocabWord word : wordMapper.selectBatchIds(wordIds)) {
                wordMap.put(word.getId(), word);
            }
        }
        List<WordDto> list = new ArrayList<>();
        for (String wordId : wordIds) {
            VocabWord word = wordMap.get(wordId);
            if (word != null) {
                list.add(toWordDto(word));
            }
        }
        return new PageResult<>(list, progressPage.getTotal(), safePage, safeSize);
    }

    /**
     * 自评到下次复习的间隔天数。
     *
     * @param rating unknown/fuzzy/known
     * @return 天数(1/2/7)
     */
    private int reviewIntervalDays(String rating) {
        return switch (rating) {
            case "unknown" -> 1;
            case "fuzzy" -> 2;
            default -> 7;
        };
    }

    /**
     * 单词实体 -> 响应 DTO(解析 meanings JSON)。
     *
     * @param word 单词实体
     * @return 响应
     */
    private WordDto toWordDto(VocabWord word) {
        List<String> meanings = new ArrayList<>();
        try {
            meanings = objectMapper.readValue(word.getMeanings(), new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("释义 JSON 解析失败: wordId={}", word.getId());
        }
        return WordDto.builder()
                .id(word.getId())
                .spelling(word.getSpelling())
                .phonetic(word.getPhonetic())
                .partOfSpeech(word.getPartOfSpeech())
                .meanings(meanings)
                .exampleSentence(word.getExampleSentence())
                .exampleTranslation(word.getExampleTranslation())
                .realExamSentence(word.getRealExamSentence())
                .realExamSource(word.getRealExamSource())
                .level(word.getLevel())
                .audioUrl(word.getAudioUrl())
                .build();
    }
}