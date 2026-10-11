package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.AdminVocabBookDto;
import com.linguacraft.backend.dto.admin.AdminWordDto;
import com.linguacraft.backend.dto.admin.VocabBookUpsertRequest;
import com.linguacraft.backend.dto.admin.WordImportResultDto;
import com.linguacraft.backend.dto.admin.WordUpsertRequest;
import com.linguacraft.backend.service.AdminVocabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 管理端词书与单词接口(阶段 A6-①)。
 *
 * <p>注意词书与单词之间<b>没有中间表</b>:词书靠 {@code filter_level} 过滤单词,
 * 因此这里没有"把单词加进某本书"的接口 —— 改单词的 {@code level} 就是改它的归属。</p>
 */
@Tag(name = "管理端-词书单词", description = "词书维护与单词录入、等级调整、引用保护删除(仅管理员)")
@RestController
@RequestMapping("/api/admin/vocab")
public class AdminVocabController {

    private final AdminVocabService adminVocabService;

    public AdminVocabController(AdminVocabService adminVocabService) {
        this.adminVocabService = adminVocabService;
    }

    /**
     * 词书列表。
     *
     * @return 统一响应:data 为词书列表(含声明词数与真实词数)
     */
    @Operation(summary = "词书列表", description = "同时下发 declaredTotal(展示值)与 actualWordCount(真实值),两者不一致属正常(词库只导了演示词量)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @GetMapping("/books")
    public R<List<AdminVocabBookDto>> books() {
        return R.ok(adminVocabService.books());
    }

    /**
     * 新建词书。
     *
     * @param request 请求
     * @return 统一响应:data 为新建词书
     */
    @Operation(summary = "新建词书", description = "id 留空由服务端生成;filterLevel 为空表示该书收录全部等级单词")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 filterLevel 非法或声明词数为负")
    @PostMapping("/books")
    public R<AdminVocabBookDto> createBook(@Valid @RequestBody VocabBookUpsertRequest request) {
        return R.ok(adminVocabService.saveBook(null, request));
    }

    /**
     * 更新词书。
     *
     * @param id      词书 ID
     * @param request 请求
     * @return 统一响应:data 为更新后词书
     */
    @Operation(summary = "更新词书", description = "不会自动改写声明总词数;需要一致请调 sync-total")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 词书不存在")
    @PutMapping("/books/{id}")
    public R<AdminVocabBookDto> updateBook(
            @Parameter(description = "词书 ID", required = true) @PathVariable("id") String id,
            @Valid @RequestBody VocabBookUpsertRequest request) {
        return R.ok(adminVocabService.saveBook(id, request));
    }

    /**
     * 对齐词书声明词数。
     *
     * @param id 词书 ID
     * @return 统一响应:data 为更新后词书
     */
    @Operation(summary = "对齐声明词数", description = "把 vocab_book.total 改写为按 filter_level 统计出的真实词数(显式操作,不自动发生)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 词书不存在")
    @PatchMapping("/books/{id}/sync-total")
    public R<AdminVocabBookDto> syncBookTotal(
            @Parameter(description = "词书 ID", required = true) @PathVariable("id") String id) {
        return R.ok(adminVocabService.syncBookTotal(id));
    }

    /**
     * 删除词书。
     *
     * @param id 词书 ID
     * @return 统一响应(空数据)
     */
    @Operation(summary = "删除词书", description = "词书只是过滤器,删除不动单词;学生端默认词书 book-syllabus 拒绝删除")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 默认词书不可删,code=404 词书不存在")
    @DeleteMapping("/books/{id}")
    public R<Void> deleteBook(
            @Parameter(description = "词书 ID", required = true) @PathVariable("id") String id) {
        adminVocabService.deleteBook(id);
        return R.ok();
    }

    /**
     * 单词分页列表。
     *
     * @param level   等级筛选(可空)
     * @param keyword 拼写或释义关键词(可空)
     * @param page    页码
     * @param size    每页条数
     * @return 统一响应:data 为分页结果
     */
    @Operation(summary = "单词列表", description = "按拼写字典序;每项附带在学用户数与被收藏次数(>0 即不可删除)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 level 非法")
    @GetMapping("/words")
    public R<PageResult<AdminWordDto>> words(
            @Parameter(description = "等级筛选:high/core/cognitive") @RequestParam(value = "level", required = false) String level,
            @Parameter(description = "拼写或释义关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "20") long size) {
        return R.ok(adminVocabService.words(level, keyword, page, size));
    }

    /**
     * 单词详情。
     *
     * @param id 单词 ID
     * @return 统一响应:data 为单词
     */
    @Operation(summary = "单词详情", description = "含引用计数,供删除前判断")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 单词不存在")
    @GetMapping("/words/{id}")
    public R<AdminWordDto> word(
            @Parameter(description = "单词 ID", required = true, example = "w-001") @PathVariable("id") String id) {
        return R.ok(adminVocabService.word(id));
    }

    /**
     * 新建单词。
     *
     * @param request 请求
     * @return 统一响应:data 为新建单词
     */
    @Operation(summary = "新建单词", description = "ID 由服务端按现有最大数字后缀 +1 生成并补零(学生端按 id 排序取词,不能插到中间);拼写重复会被拒绝")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 校验失败")
    @PostMapping("/words")
    public R<AdminWordDto> createWord(@Valid @RequestBody WordUpsertRequest request) {
        return R.ok(adminVocabService.saveWord(null, request));
    }

    /**
     * 更新单词。
     *
     * @param id      单词 ID
     * @param request 请求
     * @return 统一响应:data 为更新后单词
     */
    @Operation(summary = "更新单词", description = "改 level 即改变它属于哪些词书(词书按 filter_level 过滤,无中间表)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 校验失败,code=404 单词不存在")
    @PutMapping("/words/{id}")
    public R<AdminWordDto> updateWord(
            @Parameter(description = "单词 ID", required = true) @PathVariable("id") String id,
            @Valid @RequestBody WordUpsertRequest request) {
        return R.ok(adminVocabService.saveWord(id, request));
    }

    /**
     * CSV 批量导入单词。
     *
     * @param file 上传的 CSV 文件(UTF-8)
     * @return 统一响应:data 为导入结果
     */
    @Operation(summary = "CSV 批量导入单词",
            description = "表头须含 spelling,phonetic,part_of_speech,meanings,level(缺列整单 400);"
                    + "行级问题跳过并回报,重复拼写不覆盖已有词条;上限 2000 行,超出即截断")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 文件为空或读取失败")
    @PostMapping("/words/import")
    public R<WordImportResultDto> importWords(
            @Parameter(description = "CSV 文件(UTF-8)", required = true) @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "请选择要导入的 CSV 文件");
        }
        try {
            return R.ok(adminVocabService.importWords(new String(file.getBytes(), StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "文件读取失败:" + e.getMessage());
        }
    }

    /**
     * 删除单词。
     *
     * @param id 单词 ID
     * @return 统一响应(空数据)
     */
    @Operation(summary = "删除单词", description = "有学习记录或收藏时拒绝:word_id 无外键,删了会留下 user_word_progress 孤儿行,而今日待复习数不 join 词表,会算出幽灵词")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 存在引用,code=404 单词不存在")
    @DeleteMapping("/words/{id}")
    public R<Void> deleteWord(
            @Parameter(description = "单词 ID", required = true) @PathVariable("id") String id) {
        adminVocabService.deleteWord(id);
        return R.ok();
    }
}
