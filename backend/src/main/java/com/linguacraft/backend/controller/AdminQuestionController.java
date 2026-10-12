package com.linguacraft.backend.controller;

import com.linguacraft.backend.ai.AiGenerateService;
import com.linguacraft.backend.ai.AiReviewService;
import com.linguacraft.backend.common.PageResult;
import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.admin.AiDraftDto;
import com.linguacraft.backend.dto.admin.AiDraftRequest;
import com.linguacraft.backend.dto.admin.AiReviewDto;
import com.linguacraft.backend.dto.admin.AiReviewRequest;
import com.linguacraft.backend.dto.admin.QuestionAdminDetailDto;
import com.linguacraft.backend.dto.admin.QuestionAdminListItemDto;
import com.linguacraft.backend.dto.admin.QuestionUpsertRequest;
import com.linguacraft.backend.service.AdminQuestionService;
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

/**
 * 管理端题库接口(阶段 A2):题目录入、编辑、上下架与删除。
 *
 * <p>整个命名空间由 SecurityConfig 要求 {@code hasRole("ADMIN")}:未登录 401、
 * 学生令牌 403,均由 config 包下的两个处理器返回统一响应体。</p>
 *
 * <p>与学生端的关键差异:本组接口的详情响应<b>包含 answer_key</b>(答案与解析),
 * 因此绝不能被学生端复用;学生端题目详情永远只读 content 列。</p>
 */
@Tag(name = "管理端-题库", description = "题目 CRUD、上下架与引用保护删除(仅管理员)")
@RestController
@RequestMapping("/api/admin/questions")
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    /** AI 草稿入口(阶段 A9):生成与闸门在 AiGenerateService,本控制器只负责鉴权命名空间与响应包装 */
    private final AiGenerateService aiGenerateService;

    /** AI 校核入口(阶段 A10):建议生成与清洗在 AiReviewService,同样不写库 */
    private final AiReviewService aiReviewService;

    public AdminQuestionController(AdminQuestionService adminQuestionService,
                                   AiGenerateService aiGenerateService,
                                   AiReviewService aiReviewService) {
        this.adminQuestionService = adminQuestionService;
        this.aiGenerateService = aiGenerateService;
        this.aiReviewService = aiReviewService;
    }

    /**
     * 题目分页列表。
     *
     * @param type       题型筛选(可空)
     * @param sourceType 来源筛选(可空)
     * @param status     上架状态筛选(可空)
     * @param keyword    标题/ID 关键词(可空)
     * @param page       页码
     * @param size       每页条数
     * @return 统一响应:data 为 PageResult(含引用数)
     */
    @Operation(summary = "题目列表", description = "按题型/来源/上下架状态/关键词分页查询;每项附带被提交引用数(用于判断能否删除)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=1004 题型非法,code=400 来源非法")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @GetMapping
    public R<PageResult<QuestionAdminListItemDto>> list(
            @Parameter(description = "题型筛选", example = "cloze") @RequestParam(value = "type", required = false) String type,
            @Parameter(description = "来源筛选:real/teacher/ai") @RequestParam(value = "sourceType", required = false) String sourceType,
            @Parameter(description = "上架状态:1 上架,0 下架") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "标题或 ID 关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "页码(从 1 开始)") @RequestParam(value = "page", defaultValue = "1") long page,
            @Parameter(description = "每页条数(1~100)") @RequestParam(value = "size", defaultValue = "20") long size) {
        return R.ok(adminQuestionService.list(type, sourceType, status, keyword, page, size));
    }

    /**
     * 题目详情(含答案)。
     *
     * @param id 题目 ID
     * @return 统一响应:data 为管理端详情(含 answerKey)
     */
    @Operation(summary = "题目详情(含答案)", description = "管理端视角返回 content 与 answer_key 两列;仅管理员可访问,学生端详情接口不含答案的约定不受影响")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 题目不存在")
    @GetMapping("/{id}")
    public R<QuestionAdminDetailDto> detail(
            @Parameter(description = "题目 ID", required = true, example = "cloze-real-2023-1") @PathVariable("id") String id) {
        return R.ok(adminQuestionService.detail(id));
    }

    /**
     * 新建题目。
     *
     * @param request 录入请求
     * @return 统一响应:data 为新建后的详情
     */
    @Operation(summary = "新建题目", description = "content 与 answer_key 分离提交;服务端五道校验(结构/答案防呆/id 集合一致/答案合法/占位符一致)后落库,分值按题型默认映射写入")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 校验失败(含定位路径),code=1004 题型非法")
    @PostMapping
    public R<QuestionAdminDetailDto> create(@Valid @RequestBody QuestionUpsertRequest request) {
        return R.ok(adminQuestionService.create(request));
    }

    /**
     * AI 生成题目草稿(不入库)。
     *
     * @param request 只含题型
     * @return 统一响应:data 为过完五道闸门的草稿(content/answerKey/title/difficulty/tags/sourceMeta)
     */
    @Operation(summary = "AI 生成题目草稿(不落库)", description = "阶段 A9:调用模型出题并跑与人工录入同一套五道闸门,通过后只返回草稿;不写 question 表、不上架。管理员在录入表单里改完点「创建题目」才入库。模型产出结构不合格时重试一次,仍失败返回 code=500 且文案以「AI 出题结果不合格:」开头(带定位路径)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功返回草稿,code=1004 题型非法,code=500 AI 不可用/超时/产出反复不过闸门")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @PostMapping("/ai-draft")
    public R<AiDraftDto> aiDraft(@Valid @RequestBody AiDraftRequest request) {
        return R.ok(aiGenerateService.generateDraft(request.getType()));
    }

    /**
     * AI 校核当前题目(只给建议,不入库)。
     *
     * @param request 题型 + 当前 content + 当前 answer_key(表单此刻的内容,允许尚未保存)
     * @return 统一响应:data 为逐编号的建议清单
     */
    @Operation(summary = "AI 校核当前题目(不落库)", description = "阶段 A10:对表单当前的题面与答案给出三类建议 —— 语法填空的可接受写法、解析与答案是否一致、标准答案是否可疑。收整份内容而非题目 id,所以新建与 AI 草稿在保存前就能校核;本接口不读库也不写库,建议由管理员逐项采纳后才写入表单,仍需点保存才落库。答案风险只作提醒,服务端与前端都不会自动改答案")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功返回建议,code=1004 题型非法,code=400 缺答案项(写作题应改用 AI 预评),code=500 AI 不可用/超时")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @PostMapping("/ai-review")
    public R<AiReviewDto> aiReview(@Valid @RequestBody AiReviewRequest request) {
        return R.ok(aiReviewService.review(request.getType(), request.getContent(), request.getAnswerKey()));
    }

    /**
     * 更新题目。
     *
     * @param id      题目 ID
     * @param request 更新请求
     * @return 统一响应:data 为更新后的详情
     */
    @Operation(summary = "更新题目", description = "整条覆盖 content 与 answer_key;题型不可变更(历史提交与错题按原题型快照)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 校验失败或试图改题型,code=404 题目不存在")
    @PutMapping("/{id}")
    public R<QuestionAdminDetailDto> update(
            @Parameter(description = "题目 ID", required = true) @PathVariable("id") String id,
            @Valid @RequestBody QuestionUpsertRequest request) {
        return R.ok(adminQuestionService.update(id, request));
    }

    /**
     * 上架/下架。
     *
     * @param id     题目 ID
     * @param status 目标状态
     * @return 统一响应(空数据)
     */
    @Operation(summary = "上架/下架", description = "status=1 上架、0 下架;下架后学生端列表与智能练习不再出现该题,但历史提交详情与错题重做仍可用")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 status 非法,code=404 题目不存在")
    @PatchMapping("/{id}/status")
    public R<Void> changeStatus(
            @Parameter(description = "题目 ID", required = true) @PathVariable("id") String id,
            @Parameter(description = "目标状态:1 上架,0 下架", required = true, example = "0")
            @RequestParam("status") Integer status) {
        adminQuestionService.changeStatus(id, status);
        return R.ok();
    }

    /**
     * 删除题目(带引用保护)。
     *
     * @param id 题目 ID
     * @return 统一响应(空数据)
     */
    @Operation(summary = "删除题目", description = "被练习提交或错题记录引用时拒绝删除并提示改用下架;通过保护检查后物理删除")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 存在引用,code=404 题目不存在")
    @DeleteMapping("/{id}")
    public R<Void> delete(
            @Parameter(description = "题目 ID", required = true) @PathVariable("id") String id) {
        adminQuestionService.delete(id);
        return R.ok();
    }
}
