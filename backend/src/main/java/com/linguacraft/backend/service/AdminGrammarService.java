package com.linguacraft.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import com.linguacraft.backend.dto.admin.AdminGrammarNodeDto;
import com.linguacraft.backend.dto.admin.AdminGrammarSectionDto;
import com.linguacraft.backend.dto.admin.GrammarNodeUpsertRequest;
import com.linguacraft.backend.dto.admin.GrammarSectionUpsertRequest;
import com.linguacraft.backend.entity.GrammarNode;
import com.linguacraft.backend.entity.GrammarSection;
import com.linguacraft.backend.mapper.GrammarNodeMapper;
import com.linguacraft.backend.mapper.GrammarSectionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理端语法目录与章节服务(阶段 A6-②)。
 *
 * <p>两条必须守住的不变式(与 {@code S05} 种子一致):</p>
 * <ul>
 *   <li><b>叶节点的章节 ID 等于节点 ID</b>:学生端点开后用同一个 id 调 {@code GET /api/grammar/section/{id}},
 *       因此新建叶节点会在<b>同一事务</b>里补一份占位章节 —— 否则目录里出现一个点开就 404 的空 leaf;</li>
 *   <li><b>{@code sort_order} 决定学生端的"上一章/下一章"</b>:{@code stores/grammar.ts} 按树的 DFS 顺序推导上一页下一页,
 *       所以整层重排会改变学生浏览顺序,界面必须提示。</li>
 * </ul>
 *
 * <p>删除只有节点级:单独删章节会留下没有正文的叶子,同样 404。</p>
 */
@Service
public class AdminGrammarService {

    private static final Logger log = LoggerFactory.getLogger(AdminGrammarService.class);

    /** 新建叶节点时的占位正文(管理员必须改写后再给学生看) */
    private static final String PLACEHOLDER_MARKDOWN = "## 待补充\n\n本节内容尚未编写。";

    private final GrammarNodeMapper nodeMapper;
    private final GrammarSectionMapper sectionMapper;

    public AdminGrammarService(GrammarNodeMapper nodeMapper, GrammarSectionMapper sectionMapper) {
        this.nodeMapper = nodeMapper;
        this.sectionMapper = sectionMapper;
    }

    // ==================== 目录 ====================

    /**
     * 节点平铺列表(GET /api/admin/grammar/nodes)。
     *
     * @return 全部节点(含是否有子节点、是否已有正文)
     */
    public List<AdminGrammarNodeDto> nodes() {
        List<GrammarNode> all = nodeMapper.selectList(
                Wrappers.<GrammarNode>lambdaQuery().orderByAsc(GrammarNode::getSortOrder).orderByAsc(GrammarNode::getId));
        Set<String> parents = new HashSet<>();
        for (GrammarNode node : all) {
            if (node.getParentId() != null && !node.getParentId().isBlank()) {
                parents.add(node.getParentId());
            }
        }
        Map<String, String> sectionTitles = new HashMap<>();
        for (GrammarSection section : sectionMapper.selectList(null)) {
            sectionTitles.put(section.getId(), section.getTitle());
        }

        List<AdminGrammarNodeDto> result = new ArrayList<>();
        for (GrammarNode node : all) {
            result.add(AdminGrammarNodeDto.builder()
                    .id(node.getId())
                    .name(node.getName())
                    .parentId(node.getParentId())
                    .sortOrder(node.getSortOrder())
                    .hasChildren(parents.contains(node.getId()))
                    .hasSection(sectionTitles.containsKey(node.getId()))
                    .sectionTitle(sectionTitles.get(node.getId()))
                    .build());
        }
        return result;
    }

    /**
     * 新建或更新节点(POST / PUT /api/admin/grammar/nodes/{id})。
     *
     * <p>新建的叶节点会同步补一份占位章节,保证目录里不存在"点开 404"的叶子。</p>
     *
     * @param id      节点 ID(新建为空)
     * @param request 请求
     * @return 保存后的节点行
     */
    @Transactional
    public AdminGrammarNodeDto saveNode(String id, GrammarNodeUpsertRequest request) {
        String parentId = request.getParentId() == null || request.getParentId().isBlank()
                ? null : request.getParentId().trim();
        if (parentId != null && nodeMapper.selectById(parentId) == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "父节点不存在:" + parentId);
        }

        GrammarNode node = id == null || id.isBlank() ? new GrammarNode() : findNode(id);
        if (node.getId() == null) {
            node.setId("g-" + System.currentTimeMillis());
        } else if (parentId != null && isDescendant(parentId, node.getId())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "不能把节点移动到自己的子孙下");
        }
        node.setName(request.getName().trim());
        node.setParentId(parentId);
        node.setSortOrder(request.getSortOrder() == null
                ? (node.getSortOrder() == null ? nextNodeSortOrder(parentId) : node.getSortOrder())
                : request.getSortOrder());

        boolean creating = nodeMapper.selectById(node.getId()) == null;
        if (creating) {
            nodeMapper.insert(node);
            // 同事务补建章节:学生端用节点 ID 直接取正文
            GrammarSection section = new GrammarSection();
            section.setId(node.getId());
            section.setNodeId(node.getId());
            section.setTitle(node.getName());
            section.setMarkdown(PLACEHOLDER_MARKDOWN);
            section.setSortOrder(node.getSortOrder());
            sectionMapper.insert(section);
            log.info("管理端新建语法节点(含占位章节): id={} name={}", node.getId(), node.getName());
        } else {
            nodeMapper.updateById(node);
            log.info("管理端更新语法节点: id={}", node.getId());
        }
        return toRow(node);
    }

    /**
     * 整层重排(PUT /api/admin/grammar/nodes/reorder)。
     *
     * @param parentId 所属层(null 表示顶层)
     * @param ids      该层节点 ID 的新顺序
     * @return 重排后的全部节点
     */
    @Transactional
    public List<AdminGrammarNodeDto> reorderNodes(String parentId, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "ids 不能为空");
        }
        String parent = parentId == null || parentId.isBlank() ? null : parentId.trim();
        for (String id : ids) {
            GrammarNode node = findNode(id);
            String actual = node.getParentId() == null || node.getParentId().isBlank() ? null : node.getParentId();
            if (actual != null ? !actual.equals(parent) : parent != null) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                        "节点 " + id + " 不属于" + (parent == null ? "顶层" : "父节点 " + parent) + ",请先改父节点再重排");
            }
        }
        int order = 10;
        for (String id : ids) {
            GrammarNode node = findNode(id);
            node.setSortOrder(order);
            nodeMapper.updateById(node);
            GrammarSection section = sectionMapper.selectById(id);
            if (section != null) {
                section.setSortOrder(order);
                sectionMapper.updateById(section);
            }
            order += 10;
        }
        log.info("管理端语法整层重排: parent={} 共 {} 个", parent, ids.size());
        return nodes();
    }

    /**
     * 删除节点与其配对章节(DELETE /api/admin/grammar/nodes/{id})。
     *
     * @param id 节点 ID
     */
    @Transactional
    public void deleteNode(String id) {
        findNode(id);
        Long children = nodeMapper.selectCount(Wrappers.<GrammarNode>lambdaQuery().eq(GrammarNode::getParentId, id));
        if (children != null && children > 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "该节点下还有 " + children + " 个子节点,请先删除子节点");
        }
        sectionMapper.deleteById(id);
        nodeMapper.deleteById(id);
        log.info("管理端删除语法节点及章节: id={}", id);
    }

    // ==================== 章节 ====================

    /**
     * 章节正文(GET /api/admin/grammar/sections/{nodeId})。
     *
     * @param nodeId 节点 ID
     * @return 章节
     */
    public AdminGrammarSectionDto section(String nodeId) {
        return toSectionDto(findSection(nodeId));
    }

    /**
     * 保存章节正文(PUT /api/admin/grammar/sections/{nodeId})。
     *
     * @param nodeId  节点 ID(章节 ID 与它相同)
     * @param request 请求
     * @return 保存后的章节
     */
    @Transactional
    public AdminGrammarSectionDto saveSection(String nodeId, GrammarSectionUpsertRequest request) {
        findNode(nodeId);
        GrammarSection section = sectionMapper.selectById(nodeId);
        boolean creating = section == null;
        if (creating) {
            section = new GrammarSection();
            section.setId(nodeId);
            section.setNodeId(nodeId);
        }
        section.setTitle(request.getTitle().trim());
        section.setMarkdown(request.getMarkdown());
        if (request.getSortOrder() != null) {
            section.setSortOrder(request.getSortOrder());
        }
        if (creating) {
            sectionMapper.insert(section);
        } else {
            sectionMapper.updateById(section);
        }
        log.info("管理端保存语法章节: nodeId={} title={}", nodeId, section.getTitle());
        return toSectionDto(section);
    }

    // ==================== 小工具 ====================

    /**
     * 判断候选父节点是否为自身的子孙(防止把节点挂到自己下面形成环)。
     *
     * @param candidateParent 候选父节点 ID
     * @param selfId          当前节点 ID
     * @return 是子孙
     */
    private boolean isDescendant(String candidateParent, String selfId) {
        String cursor = candidateParent;
        Set<String> guard = new HashSet<>();
        while (cursor != null && !cursor.isBlank()) {
            if (cursor.equals(selfId) || !guard.add(cursor)) {
                return true;
            }
            GrammarNode parent = nodeMapper.selectById(cursor);
            cursor = parent == null ? null : parent.getParentId();
        }
        return false;
    }

    /**
     * 该层下一个排序值(最大值 +10,留插位空间)。
     *
     * @param parentId 所属层
     * @return 排序值
     */
    private int nextNodeSortOrder(String parentId) {
        List<GrammarNode> siblings = nodeMapper.selectList(parentId == null
                ? Wrappers.<GrammarNode>lambdaQuery().isNull(GrammarNode::getParentId)
                : Wrappers.<GrammarNode>lambdaQuery().eq(GrammarNode::getParentId, parentId));
        int max = 0;
        for (GrammarNode sibling : siblings) {
            max = Math.max(max, sibling.getSortOrder() == null ? 0 : sibling.getSortOrder());
        }
        return max + 10;
    }

    /**
     * 单个节点 -> 行(为保存接口回显用,不重扫全表)。
     *
     * @param node 节点
     * @return 行
     */
    private AdminGrammarNodeDto toRow(GrammarNode node) {
        Long children = nodeMapper.selectCount(
                Wrappers.<GrammarNode>lambdaQuery().eq(GrammarNode::getParentId, node.getId()));
        GrammarSection section = sectionMapper.selectById(node.getId());
        return AdminGrammarNodeDto.builder()
                .id(node.getId())
                .name(node.getName())
                .parentId(node.getParentId())
                .sortOrder(node.getSortOrder())
                .hasChildren(children != null && children > 0)
                .hasSection(section != null)
                .sectionTitle(section == null ? null : section.getTitle())
                .build();
    }

    /**
     * 章节实体 -> DTO。
     *
     * @param section 章节
     * @return DTO
     */
    private AdminGrammarSectionDto toSectionDto(GrammarSection section) {
        return AdminGrammarSectionDto.builder()
                .id(section.getId())
                .nodeId(section.getNodeId())
                .title(section.getTitle())
                .markdown(section.getMarkdown())
                .sortOrder(section.getSortOrder())
                .updatedAt(section.getUpdatedAt())
                .build();
    }

    /**
     * 按 ID 取节点,不存在即 404。
     *
     * @param id 节点 ID
     * @return 节点
     */
    private GrammarNode findNode(String id) {
        GrammarNode node = nodeMapper.selectById(id);
        if (node == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "语法节点不存在");
        }
        return node;
    }

    /**
     * 按节点 ID 取章节,不存在即 404。
     *
     * @param nodeId 节点 ID
     * @return 章节
     */
    private GrammarSection findSection(String nodeId) {
        GrammarSection section = sectionMapper.selectById(nodeId);
        if (section == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(),
                    "该节点还没有正文,请先在节点上新建章节或直接编辑占位内容");
        }
        return section;
    }
}
