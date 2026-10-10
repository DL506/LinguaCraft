package com.linguacraft.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CSV 批量导入结果(阶段 A6-①)。
 *
 * <p>语义是<b>行级失败跳过、有效行入库</b>:一条坏数据不该回滚前面一千九百条。
 * 重复(库中已有或文件内先前出现过的拼写)计入 {@code skippedDuplicate} 并在错误清单里列出,
 * 不覆盖已有词条。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "CSV 批量导入结果")
public class WordImportResultDto {

    /** 数据行总数(不含表头) */
    @Schema(description = "数据行总数")
    private int total;

    /** 实际入库数 */
    @Schema(description = "入库数")
    private int inserted;

    /** 因拼写已存在(库中或文件内)跳过数 */
    @Schema(description = "重复跳过数")
    private int skippedDuplicate;

    /** 因字段非法跳过数 */
    @Schema(description = "非法跳过数")
    private int skippedInvalid;

    /** 错误明细(最多 50 条) */
    @Schema(description = "错误明细(最多 50 条)")
    private List<RowError> errors;

    /** 错误清单是否被截断 */
    @Schema(description = "错误清单是否被截断")
    private boolean truncated;

    /**
     * 单行错误。
     *
     * @param line     CSV 行号(含表头,数据从 2 起)
     * @param spelling 该行拼写(可能为空)
     * @param reason   拒绝原因
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "行错误")
    public static class RowError {

        /** 行号 */
        @Schema(description = "CSV 行号")
        private int line;

        /** 拼写 */
        @Schema(description = "该行拼写")
        private String spelling;

        /** 原因 */
        @Schema(description = "拒绝原因")
        private String reason;
    }
}
