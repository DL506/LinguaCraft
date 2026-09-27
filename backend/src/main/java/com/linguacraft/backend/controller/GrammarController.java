package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.grammar.GrammarNodeDto;
import com.linguacraft.backend.dto.grammar.GrammarSectionDto;
import com.linguacraft.backend.service.GrammarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学语法接口:目录树与章节内容(前端契约 12.6)。
 */
@Tag(name = "学语法", description = "语法目录树与章节内容(markdown)")
@RestController
@RequestMapping("/api/grammar")
public class GrammarController {

    private final GrammarService grammarService;

    public GrammarController(GrammarService grammarService) {
        this.grammarService = grammarService;
    }

    /**
     * 语法目录树。
     *
     * @return 统一响应:data 为 GrammarNode[](children 递归)
     */
    @Operation(summary = "语法目录树", description = "按考点组织(动词时态/定语从句/名词性从句/非谓语动词/虚拟语气/主谓一致等);children 递归")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @GetMapping("/tree")
    public R<List<GrammarNodeDto>> tree() {
        return R.ok(grammarService.tree());
    }

    /**
     * 语法章节详情(markdown)。
     *
     * @param id 章节 ID
     * @return 统一响应:data 为 {id,title,markdown}
     */
    @Operation(summary = "语法章节详情", description = "返回 markdown 内容(考点精讲/真题示例/易错点辨析/专项练习);章节不存在 code 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 章节不存在")
    @GetMapping("/section/{id}")
    public R<GrammarSectionDto> section(
            @Parameter(description = "章节 ID", required = true, example = "tenses-overview") @PathVariable("id") String id) {
        return R.ok(grammarService.section(id));
    }
}