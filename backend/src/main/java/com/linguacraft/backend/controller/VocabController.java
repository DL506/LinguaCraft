package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.vocab.VocabBookDto;
import com.linguacraft.backend.dto.vocab.VocabReviewRequest;
import com.linguacraft.backend.dto.vocab.VocabTodayTaskDto;
import com.linguacraft.backend.dto.vocab.WordDto;
import com.linguacraft.backend.service.VocabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 背单词接口:词书 / 词表 / 记忆自评 / 今日任务 / 到期复习(前端契约 12.5)。
 */
@Tag(name = "背单词", description = "词书、词表、记忆自评(间隔复习)与今日任务")
@RestController
@RequestMapping("/api/vocab")
public class VocabController {

    private final VocabService vocabService;

    public VocabController(VocabService vocabService) {
        this.vocabService = vocabService;
    }

    /**
     * 词书列表。
     *
     * @return 统一响应:data 为词书数组(learned 由进度表派生)
     */
    @Operation(summary = "词书列表", description = "返回 3 本词书;learned(已学词数)由学习进度表实时派生")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/books")
    public R<List<VocabBookDto>> books() {
        return R.ok(vocabService.books());
    }

    /**
     * 词书单词分页。
     *
     * @param bookId 词书 ID
     * @param page   页码
     * @param size   每页条数
     * @return 统一响应:data 为 PageResult<Word>
     */
    @Operation(summary = "词书单词分页", description = "按词书分层过滤(syllabus=全部,high=高频层,core=核心层);词书不存在 code 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 词书不存在")
    @GetMapping("/book/{bookId}/words")
    public R<PageResult<WordDto>> bookWords(
            @Parameter(description = "词书 ID", required = true, example = "book-syllabus") @PathVariable("bookId") String bookId,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "20") long size) {
        return R.ok(vocabService.bookWords(bookId, page, size));
    }

    /**
     * 记忆自评(unknown/fuzzy/known)。
     *
     * @param request 自评请求
     * @return 统一响应(空数据)
     */
    @Operation(summary = "记忆自评", description = "写学习进度并顺延下次复习时间(unknown +1 天 / fuzzy +2 天 / known +7 天);非法自评 code 400,单词不存在 code 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 自评非法,code=404 单词不存在")
    @PostMapping("/review")
    public R<Void> review(@RequestBody VocabReviewRequest request) {
        vocabService.review(request);
        return R.ok();
    }

    /**
     * 今日背单词任务。
     *
     * @return 统一响应:data 为 VocabTodayTask
     */
    @Operation(summary = "今日背单词任务", description = "newCount=今日新学 / reviewCount=待复习 / doneCount=今日已完成 / target=用户设置目标")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/task/today")
    public R<VocabTodayTaskDto> todayTask() {
        return R.ok(vocabService.todayTask());
    }

    /**
     * 到期复习单词分页。
     *
     * @param page 页码
     * @param size 每页条数
     * @return 统一响应:data 为 PageResult<Word>
     */
    @Operation(summary = "到期复习单词", description = "返回 nextReviewAt <= now 的单词,按到期时间升序")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/task/due")
    public R<PageResult<WordDto>> dueWords(
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "20") long size) {
        return R.ok(vocabService.dueWords(page, size));
    }
}