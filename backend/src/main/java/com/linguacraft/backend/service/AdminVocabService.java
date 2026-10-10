package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.ObjectMapper;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.AdminVocabBookDto;
import com.linguacraft.backend.dto.admin.AdminWordDto;
import com.linguacraft.backend.dto.admin.VocabBookUpsertRequest;
import com.linguacraft.backend.dto.admin.WordImportResultDto;
import com.linguacraft.backend.dto.admin.WordUpsertRequest;
import com.linguacraft.backend.entity.Favorite;
import com.linguacraft.backend.entity.UserWordProgress;
import com.linguacraft.backend.entity.UserWordReview;
import com.linguacraft.backend.entity.VocabBook;
import com.linguacraft.backend.entity.VocabWord;
import com.linguacraft.backend.mapper.FavoriteMapper;
import com.linguacraft.backend.mapper.UserWordProgressMapper;
import com.linguacraft.backend.mapper.UserWordReviewMapper;
import com.linguacraft.backend.mapper.VocabBookMapper;
import com.linguacraft.backend.mapper.VocabWordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 管理端词书与单词服务(阶段 A6-①)。
 *
 * <p><b>词书不是集合,而是过滤器</b>:{@code vocab_book} 与 {@code vocab_word} 之间没有中间表,
 * 学生端某本书里的词 = {@code vocab_word.level} 命中该书 {@code filter_level} 的全部词
 * ({@code filter_level} 为空表示不限)。因此单词列表不按书归属维护,后台也按同一口径给出真实词数。</p>
 *
 * <p>两条刻意保留的取舍:</p>
 * <ul>
 *   <li>{@code vocab_book.total} 是<b>声明值</b>(种子写考纲词量 3000/800/1200,库里只导了演示词量),
 *       不自动派生,否则学生端首页词书数字会暴跌;需要一致时由管理员显式调 sync-total;</li>
 *   <li>删除单词带引用保护:单词 ID 无外键约束,删掉一个正在被学的词会留下
 *       {@code user_word_progress} 孤儿行 —— 而"今日待复习数"统计不 join 词表,会算出看不见的幽灵词。</li>
 * </ul>
 */
@Service
public class AdminVocabService {

    private static final Logger log = LoggerFactory.getLogger(AdminVocabService.class);

    /** 单词等级白名单(与前端 types/vocab.d.ts 的 WordLevel 联合一致) */
    private static final Set<String> VALID_LEVELS = Set.of("high", "core", "cognitive");

    /** 学生端默认词书 ID(前端 store 硬编码,删掉会让背单词页无书可选) */
    private static final String DEFAULT_BOOK_ID = "book-syllabus";

    /** 单词 ID 的数字后缀样式(w-001 / w-12) */
    private static final Pattern WORD_ID_SUFFIX = Pattern.compile("^(w-)?(\\d+)$");

    /** 单次导入的数据行上限 */
    private static final int MAX_IMPORT_ROWS = 2000;

    /** 导入结果里最多回报的错误条数 */
    private static final int MAX_IMPORT_ERRORS = 50;

    private final VocabBookMapper bookMapper;
    private final VocabWordMapper wordMapper;
    private final UserWordProgressMapper progressMapper;
    private final UserWordReviewMapper reviewMapper;
    private final FavoriteMapper favoriteMapper;
    private final ObjectMapper objectMapper;

    public AdminVocabService(VocabBookMapper bookMapper,
                             VocabWordMapper wordMapper,
                             UserWordProgressMapper progressMapper,
                             UserWordReviewMapper reviewMapper,
                             FavoriteMapper favoriteMapper,
                             ObjectMapper objectMapper) {
        this.bookMapper = bookMapper;
        this.wordMapper = wordMapper;
        this.progressMapper = progressMapper;
        this.reviewMapper = reviewMapper;
        this.favoriteMapper = favoriteMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== 词书 ====================

    /**
     * 词书列表(GET /api/admin/vocab/books):按 sort_order 升序,附带真实词数。
     *
     * @return 词书列表
     */
    public List<AdminVocabBookDto> books() {
        List<VocabBook> all = bookMapper.selectList(
                Wrappers.<VocabBook>lambdaQuery().orderByAsc(VocabBook::getSortOrder).orderByAsc(VocabBook::getId));
        List<AdminVocabBookDto> result = new ArrayList<>();
        for (VocabBook book : all) {
            result.add(AdminVocabBookDto.builder()
                    .id(book.getId())
                    .name(book.getName())
                    .declaredTotal(book.getTotal())
                    .actualWordCount(countWordsOfBook(book))
                    .filterLevel(book.getFilterLevel())
                    .sortOrder(book.getSortOrder())
                    .build());
        }
        return result;
    }

    /**
     * 新建或更新词书(POST / PUT /api/admin/vocab/books/{id})。
     *
     * @param id      路径 ID(新建时为空)
     * @param request 请求
     * @return 保存后的词书
     */
    @Transactional
    public AdminVocabBookDto saveBook(String id, VocabBookUpsertRequest request) {
        String filterLevel = normalizeFilterLevel(request.getFilterLevel());
        VocabBook book = id == null || id.isBlank() ? new VocabBook() : findBook(id);
        if (book.getId() == null) {
            book.setId("book-" + System.currentTimeMillis());
        }
        book.setName(request.getName().trim());
        if (request.getTotal() != null) {
            if (request.getTotal() < 0) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "声明总词数不能为负");
            }
            book.setTotal(request.getTotal());
        } else if (book.getTotal() == null) {
            book.setTotal(0);
        }
        book.setFilterLevel(filterLevel);
        book.setSortOrder(request.getSortOrder() == null
                ? (book.getSortOrder() == null ? nextSortOrder() : book.getSortOrder())
                : request.getSortOrder());
        if (book.getId() != null && bookMapper.selectById(book.getId()) == null) {
            bookMapper.insert(book);
            log.info("管理端新建词书: id={} name={}", book.getId(), book.getName());
        } else {
            bookMapper.updateById(book);
            log.info("管理端更新词书: id={}", book.getId());
        }
        return toBookDto(book);
    }

    /**
     * 删除词书(DELETE /api/admin/vocab/books/{id})。
     *
     * <p>词书与单词无外键关系,删除只移除这本"过滤器";学生端默认词书不允许删。</p>
     *
     * @param id 词书 ID
     */
    @Transactional
    public void deleteBook(String id) {
        VocabBook book = findBook(id);
        if (DEFAULT_BOOK_ID.equals(book.getId())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "考纲词书(" + DEFAULT_BOOK_ID + ")是学生端默认词书,删除后背单词页无书可选;如需下线请改名称或清空单词");
        }
        bookMapper.deleteById(id);
        log.info("管理端删除词书: id={}", id);
    }

    /**
     * 把声明总词数对齐为真实词数(PATCH /api/admin/vocab/books/{id}/sync-total)。
     *
     * @param id 词书 ID
     * @return 更新后的词书
     */
    @Transactional
    public AdminVocabBookDto syncBookTotal(String id) {
        VocabBook book = findBook(id);
        book.setTotal(countWordsOfBook(book));
        bookMapper.updateById(book);
        log.info("管理端对齐词书声明词数: id={} total={}", id, book.getTotal());
        return toBookDto(book);
    }

    // ==================== 单词 ====================

    /**
     * 单词分页列表(GET /api/admin/vocab/words)。
     *
     * @param level   等级筛选(可空)
     * @param keyword 拼写/释义关键词(可空)
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果(含引用计数)
     */
    public PageResult<AdminWordDto> words(String level, String keyword, long page, long size) {
        if (level != null && !level.isBlank() && !VALID_LEVELS.contains(level)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "level 仅接受 high/core/cognitive");
        }
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        var wrapper = Wrappers.<VocabWord>lambdaQuery();
        if (level != null && !level.isBlank()) {
            wrapper.eq(VocabWord::getLevel, level);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(VocabWord::getSpelling, kw).or().like(VocabWord::getMeanings, kw));
        }
        // 后台按拼写字典序排,方便人眼找词;学生端学习队列按 id 顺序(见 VocabService),两边口径本就不同
        wrapper.orderByAsc(VocabWord::getSpelling).orderByAsc(VocabWord::getId);

        Page<VocabWord> result = wordMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        Map<String, int[]> refs = countReferences(result.getRecords().stream().map(VocabWord::getId).toList());

        List<AdminWordDto> items = new ArrayList<>();
        for (VocabWord word : result.getRecords()) {
            int[] counts = refs.getOrDefault(word.getId(), new int[2]);
            items.add(toWordDto(word, counts[0], counts[1]));
        }
        return new PageResult<>(items, result.getTotal(), safePage, safeSize);
    }

    /**
     * 单词详情(GET /api/admin/vocab/words/{id})。
     *
     * @param id 单词 ID
     * @return 详情
     */
    public AdminWordDto word(String id) {
        VocabWord word = findWord(id);
        int[] counts = countReferences(List.of(id)).getOrDefault(id, new int[2]);
        return toWordDto(word, counts[0], counts[1]);
    }

    /**
     * 新建或更新单词。
     *
     * @param id      路径 ID(新建为空)
     * @param request 请求
     * @return 保存后的单词
     */
    @Transactional
    public AdminWordDto saveWord(String id, WordUpsertRequest request) {
        String level = requireLevel(request.getLevel());
        List<String> meanings = requireMeanings(request.getMeanings());
        String spelling = request.getSpelling().trim();
        assertSpellingFree(id, spelling);

        VocabWord word = id == null || id.isBlank() ? new VocabWord() : findWord(id);
        if (word.getId() == null) {
            word.setId(nextWordId());
        }
        word.setSpelling(spelling);
        word.setPhonetic(request.getPhonetic().trim());
        word.setPartOfSpeech(request.getPartOfSpeech().trim());
        word.setMeanings(writeJson(meanings));
        word.setLevel(level);
        word.setExampleSentence(blankToNull(request.getExampleSentence()));
        word.setExampleTranslation(blankToNull(request.getExampleTranslation()));
        word.setRealExamSentence(blankToNull(request.getRealExamSentence()));
        word.setRealExamSource(blankToNull(request.getRealExamSource()));
        word.setAudioUrl(blankToNull(request.getAudioUrl()));

        if (wordMapper.selectById(word.getId()) == null) {
            wordMapper.insert(word);
            log.info("管理端新建单词: id={} spelling={} level={}", word.getId(), spelling, level);
        } else {
            wordMapper.updateById(word);
            log.info("管理端更新单词: id={}", word.getId());
        }
        int[] counts = countReferences(List.of(word.getId())).getOrDefault(word.getId(), new int[2]);
        return toWordDto(word, counts[0], counts[1]);
    }

    /**
     * 删除单词(带引用保护)。
     *
     * @param id 单词 ID
     */
    @Transactional
    public void deleteWord(String id) {
        findWord(id);
        int[] counts = countReferences(List.of(id)).getOrDefault(id, new int[2]);
        if (counts[0] > 0 || counts[1] > 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "该单词已有 " + counts[0] + " 人在学、被收藏 " + counts[1] + " 次,不能删除;如需停止投放请修改等级或释义");
        }
        wordMapper.deleteById(id);
        log.info("管理端删除单词: id={}", id);
    }

    // ==================== 批量导入 ====================

    /**
     * CSV 批量导入单词(POST /api/admin/vocab/words/import)。
     *
     * <p>失败语义:行级问题跳过并回报,有效行照常入库 —— 一条坏数据不应该回滚前面一千九百条。
     * 重复判定 = 库里已有同拼写(大小写无关)<b>或</b>本文件前面行已出现过;重复一律跳过而非覆盖,
     * 以免误改已有关联学习记录的词条。</p>
     *
     * @param csvText CSV 全文(UTF-8 解码后)
     * @return 导入结果
     */
    @Transactional
    public WordImportResultDto importWords(String csvText) {
        CsvWordParser.Result parsed = CsvWordParser.parse(csvText, MAX_IMPORT_ROWS);
        List<WordImportResultDto.RowError> errors = new ArrayList<>();
        for (String message : parsed.errors()) {
            errors.add(WordImportResultDto.RowError.builder().line(0).spelling(null).reason(message).build());
        }

        // 结构不合规则直接返回(表头错误的行号无意义)
        if (parsed.rows().isEmpty()) {
            return WordImportResultDto.builder()
                    .total(0).inserted(0).skippedDuplicate(0).skippedInvalid(parsed.errors().size())
                    .errors(capErrors(errors)).truncated(parsed.truncated()).build();
        }

        Set<String> existing = lowerSpellings();
        long[] idState = scanWordIdState();
        int inserted = 0;
        int duplicate = 0;
        int invalid = 0;
        for (CsvWordParser.Row row : parsed.rows()) {
            String lower = row.spelling().toLowerCase(Locale.ROOT);
            if (!VALID_LEVELS.contains(row.level())) {
                invalid++;
                errors.add(WordImportResultDto.RowError.builder()
                        .line(row.line()).spelling(row.spelling())
                        .reason("level 仅接受 high/core/cognitive,当前为 " + row.level()).build());
                continue;
            }
            if (existing.contains(lower)) {
                duplicate++;
                errors.add(WordImportResultDto.RowError.builder()
                        .line(row.line()).spelling(row.spelling()).reason("拼写已存在,跳过(不覆盖已有词条)").build());
                continue;
            }
            VocabWord word = new VocabWord();
            word.setId(formatWordId(idState[0] + 1 + inserted, (int) idState[1]));
            word.setSpelling(row.spelling());
            word.setPhonetic(row.phonetic());
            word.setPartOfSpeech(row.partOfSpeech());
            word.setMeanings(writeJson(row.meanings()));
            word.setLevel(row.level());
            wordMapper.insert(word);
            // 文件内自重复也要跳过:把刚入库的拼写加入集合
            existing.add(lower);
            inserted++;
        }

        log.info("管理端 CSV 导入: 入库 {} 条,重复 {} 条,非法 {} 条", inserted, duplicate, invalid);
        return WordImportResultDto.builder()
                .total(parsed.dataRows())
                .inserted(inserted)
                .skippedDuplicate(duplicate)
                .skippedInvalid(invalid)
                .errors(capErrors(errors))
                .truncated(parsed.truncated())
                .build();
    }

    /**
     * 错误清单截断到前 50 条(整份文件可能有几千行坏数据,全量回报会撑爆响应)。
     *
     * @param errors 全部错误
     * @return 截断后的错误
     */
    private List<WordImportResultDto.RowError> capErrors(List<WordImportResultDto.RowError> errors) {
        return errors.size() <= MAX_IMPORT_ERRORS
                ? errors
                : new ArrayList<>(errors.subList(0, MAX_IMPORT_ERRORS));
    }

    /**
     * 库中已有拼写的小写集合。
     *
     * @return 拼写集合
     */
    private Set<String> lowerSpellings() {
        Set<String> result = new HashSet<>();
        for (VocabWord word : wordMapper.selectList(Wrappers.<VocabWord>lambdaQuery().select(VocabWord::getSpelling))) {
            if (word.getSpelling() != null) {
                result.add(word.getSpelling().toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

    // ==================== 校验与工具 ====================

    /**
     * 生成下一个单词 ID:沿用现有最大数字后缀 +1,并按当前最大宽度补零。
     *
     * <p>学生端词表按 id 排序取词,所以新 ID 必须排在后面;若直接用时间戳,
     * {@code w-1700000000} 会字典序排在 {@code w-999} 之前,导致新词插到列表中间。</p>
     *
     * @return 新 ID
     */
    private String nextWordId() {
        long[] state = scanWordIdState();
        return formatWordId(state[0] + 1, (int) state[1]);
    }

    /**
     * 扫一遍现有单词 ID,得到最大数字后缀与当前位宽。
     *
     * @return long[0]=最大值,long[1]=位宽(至少 3)
     */
    private long[] scanWordIdState() {
        long max = 0;
        int width = 3;
        for (VocabWord word : wordMapper.selectList(Wrappers.<VocabWord>lambdaQuery().select(VocabWord::getId))) {
            Matcher matcher = WORD_ID_SUFFIX.matcher(word.getId());
            if (matcher.matches()) {
                long value = Long.parseLong(matcher.group(2));
                if (value > max) {
                    max = value;
                }
                width = Math.max(width, matcher.group(2).length());
            }
        }
        return new long[]{max, width};
    }

    /**
     * 按位宽补零生成单词 ID。
     *
     * @param value 数字部分
     * @param width 位宽
     * @return 形如 w-021 的 ID
     */
    private String formatWordId(long value, int width) {
        return "w-" + String.format("%0" + width + "d", value);
    }

    /**
     * 校验等级合法。
     *
     * @param level 等级
     * @return 等级
     */
    private String requireLevel(String level) {
        if (!VALID_LEVELS.contains(level)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "level 仅接受 high/core/cognitive");
        }
        return level;
    }

    /**
     * 校验释义列表非空且每项非空白。
     *
     * @param meanings 释义
     * @return 去空白后的释义
     */
    private List<String> requireMeanings(List<String> meanings) {
        List<String> cleaned = new ArrayList<>();
        if (meanings != null) {
            for (String meaning : meanings) {
                if (meaning != null && !meaning.isBlank()) {
                    cleaned.add(meaning.trim());
                }
            }
        }
        if (cleaned.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "释义 meanings 至少填写一项非空文本");
        }
        return cleaned;
    }

    /**
     * 词书过滤等级归一(空串视作不限,存 NULL)。
     *
     * @param filterLevel 过滤等级
     * @return 归一后的值
     */
    private String normalizeFilterLevel(String filterLevel) {
        if (filterLevel == null || filterLevel.isBlank()) {
            return null;
        }
        return requireLevel(filterLevel);
    }

    /**
     * 同一拼写只允许一条(vocab_word 上没有唯一键,靠服务层挡)。
     *
     * @param selfId   当前单词 ID(更新时排除自己)
     * @param spelling 拼写
     */
    private void assertSpellingFree(String selfId, String spelling) {
        Long exists = wordMapper.selectCount(Wrappers.<VocabWord>lambdaQuery()
                .eq(VocabWord::getSpelling, spelling)
                .ne(selfId != null && !selfId.isBlank(), VocabWord::getId, selfId));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "拼写已存在:" + spelling + "(词库没有拼写唯一键,请直接编辑那条单词)");
        }
    }

    /**
     * 统计单词被引用情况:在学用户数与收藏次数。
     *
     * @param wordIds 单词 ID 集合
     * @return ID -> [在学用户数, 收藏次数]
     */
    private Map<String, int[]> countReferences(List<String> wordIds) {
        Map<String, int[]> result = new HashMap<>();
        if (wordIds.isEmpty()) {
            return result;
        }
        for (String wordId : wordIds) {
            Long learners = progressMapper.selectCount(
                    Wrappers.<UserWordProgress>lambdaQuery().eq(UserWordProgress::getWordId, wordId));
            Long reviews = reviewMapper.selectCount(
                    Wrappers.<UserWordReview>lambdaQuery().eq(UserWordReview::getWordId, wordId));
            Long favorites = favoriteMapper.selectCount(
                    Wrappers.<Favorite>lambdaQuery()
                            .eq(Favorite::getType, "word")
                            .eq(Favorite::getRefId, wordId));
            result.put(wordId, new int[]{toInt(learners) + toInt(reviews), toInt(favorites)});
        }
        return result;
    }

    /**
     * 词书的真实词数:按 filter_level 过滤单词(filter_level 为空即全部)。
     *
     * @param book 词书
     * @return 词数
     */
    private int countWordsOfBook(VocabBook book) {
        var wrapper = Wrappers.<VocabWord>lambdaQuery();
        if (book.getFilterLevel() != null && !book.getFilterLevel().isBlank()) {
            wrapper.eq(VocabWord::getLevel, book.getFilterLevel());
        }
        return toInt(wordMapper.selectCount(wrapper));
    }

    /**
     * 下一个排序值(当前最大值 +10,留插位空间)。
     *
     * @return 排序值
     */
    private int nextSortOrder() {
        int max = 0;
        for (VocabBook book : bookMapper.selectList(Wrappers.<VocabBook>lambdaQuery().select(VocabBook::getSortOrder))) {
            if (book.getSortOrder() != null) {
                max = Math.max(max, book.getSortOrder());
            }
        }
        return max + 10;
    }

    /**
     * Long 计数收成 int(null 归零)。
     *
     * @param value 计数
     * @return 计数
     */
    private int toInt(Long value) {
        return value == null ? 0 : value.intValue();
    }

    /**
     * 空白转 null(可选列不落空字符串)。
     *
     * @param value 原值
     * @return null 或 trim 后的值
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * 实体 -> 词书 DTO。
     *
     * @param book 词书
     * @return DTO
     */
    private AdminVocabBookDto toBookDto(VocabBook book) {
        return AdminVocabBookDto.builder()
                .id(book.getId())
                .name(book.getName())
                .declaredTotal(book.getTotal())
                .actualWordCount(countWordsOfBook(book))
                .filterLevel(book.getFilterLevel())
                .sortOrder(book.getSortOrder())
                .build();
    }

    /**
     * 实体 -> 单词 DTO。
     *
     * @param word          单词
     * @param learnedUsers  在学用户数
     * @param favorites     收藏次数
     * @return DTO
     */
    private AdminWordDto toWordDto(VocabWord word, int learnedUsers, int favorites) {
        return AdminWordDto.builder()
                .id(word.getId())
                .spelling(word.getSpelling())
                .phonetic(word.getPhonetic())
                .partOfSpeech(word.getPartOfSpeech())
                .meanings(readMeanings(word.getMeanings()))
                .level(word.getLevel())
                .exampleSentence(word.getExampleSentence())
                .exampleTranslation(word.getExampleTranslation())
                .realExamSentence(word.getRealExamSentence())
                .realExamSource(word.getRealExamSource())
                .audioUrl(word.getAudioUrl())
                .learnedUserCount(learnedUsers)
                .favoriteCount(favorites)
                .build();
    }

    /**
     * 解析 meanings JSON 列。
     *
     * @param json JSON 字符串
     * @return 释义列表(解析失败返回空列表)
     */
    private List<String> readMeanings(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            log.warn("释义 JSON 解析失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 序列化为 JSON 字符串。
     *
     * @param value 值
     * @return JSON
     */
    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "JSON 字段序列化失败:" + e.getMessage());
        }
    }

    /**
     * 按 ID 取词书,不存在即 404。
     *
     * @param id 词书 ID
     * @return 词书
     */
    private VocabBook findBook(String id) {
        VocabBook book = bookMapper.selectById(id);
        if (book == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "词书不存在");
        }
        return book;
    }

    /**
     * 按 ID 取单词,不存在即 404。
     *
     * @param id 单词 ID
     * @return 单词
     */
    private VocabWord findWord(String id) {
        VocabWord word = wordMapper.selectById(id);
        if (word == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "单词不存在");
        }
        return word;
    }
}
