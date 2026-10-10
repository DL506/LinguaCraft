package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.R;
import com.linguacraft.backend.dto.admin.AdminGrammarNodeDto;
import com.linguacraft.backend.dto.admin.AdminGrammarSectionDto;
import com.linguacraft.backend.dto.admin.GrammarNodeUpsertRequest;
import com.linguacraft.backend.dto.admin.GrammarSectionUpsertRequest;
import com.linguacraft.backend.service.AdminGrammarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端语法接口(阶段 A6-②):目录节点与正文章节维护。
 *
 * <p>节点与章节 ID 相同(种子不变式),因此章节接口一律以 {@code nodeId} 寻址。</p>
 */
@Tag(name = "管理端-语法", description = "语法目录节点与正文章节维护(仅管理员)")
@RestController
@RequestMapping("/api/admin/grammar")
public class AdminGrammarController {

    private final AdminGrammarService adminGrammarService;

    public AdminGrammarController(AdminGrammarService adminGrammarService) {
        this.adminGrammarService = adminGrammarService;
    }

    /**
     * 节点平铺列表。
     *
     * @return 统一响应:data 为节点列表
     */
    @Operation(summary = "语法节点列表", description = "平铺返回(前端按 parentId 组装);含是否有子节点与是否已有正文")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功")
    @ApiResponse(responseCode = "403", description = "非管理员令牌:code=403 无权限访问")
    @GetMapping("/nodes")
    public R<List<AdminGrammarNodeDto>> nodes() {
        return R.ok(adminGrammarService.nodes());
    }

    /**
     * 新建节点。
     *
     * @param request 请求
     * @return 统一响应:data 为新节点
     */
    @Operation(summary = "新建语法节点", description = "新建即视为叶节点,同事务补一份占位章节(正文=待补充),避免学生端点开 404")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 环检测拒绝,code=404 父节点不存在")
    @PostMapping("/nodes")
    public R<AdminGrammarNodeDto> createNode(@Valid @RequestBody GrammarNodeUpsertRequest request) {
        return R.ok(adminGrammarService.saveNode(null, request));
    }

    /**
     * 更新节点。
     *
     * @param id      节点 ID
     * @param request 请求
     * @return 统一响应:data 为更新后的节点
     */
    @Operation(summary = "更新语法节点", description = "改父节点时做环检测:不能把节点挂到自己的子孙下")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 成环,code=404 节点或父节点不存在")
    @PutMapping("/nodes/{id}")
    public R<AdminGrammarNodeDto> updateNode(
            @Parameter(description = "节点 ID", required = true) @PathVariable("id") String id,
            @Valid @RequestBody GrammarNodeUpsertRequest request) {
        return R.ok(adminGrammarService.saveNode(id, request));
    }

    /**
     * 整层重排。
     *
     * @param parentId 所属层(留空表示顶层)
     * @param ids      新顺序
     * @return 统一响应:data 为重排后的节点列表
     */
    @Operation(summary = "整层重排", description = "按给定顺序把该层 sortOrder 重编为 10/20/30…;学生端「上一章/下一章」按树的 DFS 顺序推导,重排即改变浏览顺序")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 有空清单或节点不属于该层")
    @PutMapping("/nodes/reorder")
    public R<List<AdminGrammarNodeDto>> reorder(
            @Parameter(description = "父节点 ID(留空为顶层)") @RequestParam(value = "parentId", required = false) String parentId,
            @RequestBody List<String> ids) {
        return R.ok(adminGrammarService.reorderNodes(parentId, ids));
    }

    /**
     * 删除节点。
     *
     * @param id 节点 ID
     * @return 统一响应(空数据)
     */
    @Operation(summary = "删除语法节点", description = "同事务删除配对章节;有子节点时拒绝(不做级联,以免误删整棵子树正文)")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=400 存在子节点,code=404 节点不存在")
    @DeleteMapping("/nodes/{id}")
    public R<Void> deleteNode(
            @Parameter(description = "节点 ID", required = true) @PathVariable("id") String id) {
        adminGrammarService.deleteNode(id);
        return R.ok();
    }

    /**
     * 读取正文。
     *
     * @param nodeId 节点 ID
     * @return 统一响应:data 为章节
     */
    @Operation(summary = "读取章节正文", description = "章节 ID 等于节点 ID")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 章节不存在")
    @GetMapping("/sections/{nodeId}")
    public R<AdminGrammarSectionDto> section(
            @Parameter(description = "节点 ID", required = true) @PathVariable("nodeId") String nodeId) {
        return R.ok(adminGrammarService.section(nodeId));
    }

    /**
     * 保存正文。
     *
     * @param nodeId  节点 ID
     * @param request 请求
     * @return 统一响应:data 为章节
     */
    @Operation(summary = "保存章节正文", description = "markdown 原文落 MEDIUMTEXT 列,学生端按 markdown 渲染;新建节点已带占位正文,这里是覆盖")
    @ApiResponse(responseCode = "200", description = "统一响应;code=0 成功,code=404 节点不存在")
    @PutMapping("/sections/{nodeId}")
    public R<AdminGrammarSectionDto> saveSection(
            @Parameter(description = "节点 ID", required = true) @PathVariable("nodeId") String nodeId,
            @Valid @RequestBody GrammarSectionUpsertRequest request) {
        return R.ok(adminGrammarService.saveSection(nodeId, request));
    }
}
