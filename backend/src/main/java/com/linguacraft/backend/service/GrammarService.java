package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.grammar.GrammarNodeDto;
import com.linguacraft.backend.dto.grammar.GrammarSectionDto;
import com.linguacraft.backend.entity.GrammarNode;
import com.linguacraft.backend.entity.GrammarSection;
import com.linguacraft.backend.mapper.GrammarNodeMapper;
import com.linguacraft.backend.mapper.GrammarSectionMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学语法服务:目录树与章节内容(章节 ID 与叶节点 node id 一致)。
 */
@Service
public class GrammarService {

    private final GrammarNodeMapper nodeMapper;
    private final GrammarSectionMapper sectionMapper;

    public GrammarService(GrammarNodeMapper nodeMapper, GrammarSectionMapper sectionMapper) {
        this.nodeMapper = nodeMapper;
        this.sectionMapper = sectionMapper;
    }

    /**
     * 语法目录树(GET /api/grammar/tree):按 sort_order 组织为自引用树。
     *
     * @return 顶层节点列表(children 递归)
     */
    public List<GrammarNodeDto> tree() {
        List<GrammarNode> nodes = nodeMapper.selectList(
                Wrappers.<GrammarNode>lambdaQuery().orderByAsc(GrammarNode::getSortOrder));
        Map<String, List<GrammarNode>> byParent = new HashMap<>();
        for (GrammarNode node : nodes) {
            byParent.computeIfAbsent(node.getParentId(), key -> new ArrayList<>()).add(node);
        }
        return buildChildren(null, byParent);
    }

    /**
     * 语法章节详情(GET /api/grammar/section/{id})。
     *
     * @param id 章节 ID
     * @return 章节(id/title/markdown)
     * @throws BizException 章节不存在(code 404)
     */
    public GrammarSectionDto section(String id) {
        GrammarSection section = sectionMapper.selectById(id);
        if (section == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "章节不存在");
        }
        return GrammarSectionDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .markdown(section.getMarkdown())
                .build();
    }

    /**
     * 递归构建节点子树。
     *
     * @param parentId 父节点 ID(null 为顶层)
     * @param byParent 按父节点分组的节点表
     * @return 子树列表
     */
    private List<GrammarNodeDto> buildChildren(String parentId, Map<String, List<GrammarNode>> byParent) {
        List<GrammarNodeDto> result = new ArrayList<>();
        for (GrammarNode node : byParent.getOrDefault(parentId, List.of())) {
            List<GrammarNodeDto> children = buildChildren(node.getId(), byParent);
            result.add(GrammarNodeDto.builder()
                    .id(node.getId())
                    .name(node.getName())
                    .children(children.isEmpty() ? null : children)
                    .build());
        }
        return result;
    }
}