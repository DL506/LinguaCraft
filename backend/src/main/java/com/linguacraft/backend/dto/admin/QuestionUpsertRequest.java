package com.linguacraft.backend.dto.admin;

import tools.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 题目新建/更新请求(POST 与 PUT /api/admin/questions[/&#123;id&#125;] 共用)。
 *
 * <p>设计要点:</p>
 * <ul>
 *   <li>{@code content} 与 {@code answerKey} 两列分离提交,服务端落库前做五道校验
 *       (结构、答案防呆、id 集合一致、answer 落在候选 key 内、passage 占位符与空位一致);</li>
 *   <li>{@code content} 内的 {@code id}/{@code type}/{@code source} 三个字段由服务端<b>强制覆写</b>,
 *       以请求的 type/sourceType/最终 id 为准,避免管理员手填错配导致详情接口与列表不一致;</li>
 *   <li>分值不接受请求覆盖,一律按题型默认映射写入(与判分口径同源);</li>
 *   <li>{@code id} 留空时由服务端生成 {@code {type}-{sourceType}-{yyMMddHHmmss}}。</li>
 * </ul>
 */
@Data
@Schema(description = "题目新建/更新请求(content 与 answer_key 分离提交)")
public class QuestionUpsertRequest {

    /** 题目 ID(新建可留空,由服务端生成;更新时以路径变量为准) */
    @Size(max = 64, message = "题目 ID 最长 64 字符")
    @Schema(description = "题目 ID(新建可留空由服务端生成)", example = "cloze-teacher-2")
    private String id;

    /** 题型:reading/matching/cloze/grammar-fill/writing */
    @NotBlank(message = "题型不能为空")
    @Schema(description = "题型:reading/matching/cloze/grammar-fill/writing", requiredMode = Schema.RequiredMode.REQUIRED, example = "cloze")
    private String type;

    /** 来源类型:real/teacher/ai */
    @NotBlank(message = "来源类型不能为空")
    @Schema(description = "来源类型:real/teacher/ai", requiredMode = Schema.RequiredMode.REQUIRED, example = "teacher")
    private String sourceType;

    /** 标题(留空时按题型与时间自动生成) */
    @Size(max = 200, message = "标题最长 200 字符")
    @Schema(description = "标题(留空自动生成)")
    private String title;

    /** 难度 1~5(留空默认 3) */
    @Schema(description = "难度 1~5(留空默认 3)", example = "3")
    private Integer difficulty;

    /** 标签数组 */
    @Schema(description = "标签数组")
    private List<String> tags;

    /** 上架状态:1 上架(默认),0 下架 */
    @Schema(description = "上架状态:1 上架(默认),0 下架", example = "1")
    private Integer status;

    /** 来源附带信息(meta 对象,如 year/region/teacherName;留空则按来源类型生成空 meta) */
    @Schema(description = "来源附带信息 meta(如 year/region/teacherName)")
    private JsonNode sourceMeta;

    /** 下发内容(ForAnswer 结构,禁止包含答案与解析) */
    @NotNull(message = "content 不能为空")
    @Schema(description = "下发内容 content(ForAnswer 结构,禁止含 answer/explanation/sampleAnswer)",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonNode content;

    /** 答案与解析(客观题必填;写作可空) */
    @Schema(description = "答案与解析 answer_key(客观题必填,写作可空)")
    private JsonNode answerKey;
}
