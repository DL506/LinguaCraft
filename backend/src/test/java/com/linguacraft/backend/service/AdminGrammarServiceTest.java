package com.linguacraft.backend.service;

import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.dto.admin.GrammarNodeUpsertRequest;
import com.linguacraft.backend.dto.admin.GrammarSectionUpsertRequest;
import com.linguacraft.backend.entity.GrammarNode;
import com.linguacraft.backend.entity.GrammarSection;
import com.linguacraft.backend.mapper.GrammarNodeMapper;
import com.linguacraft.backend.mapper.GrammarSectionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 语法目录与章节服务测试(阶段 A6-②)。
 *
 * <p>锁两条不变式:叶节点必有同 ID 章节(否则学生端点开 404)、父链不可成环;
 * 另覆盖整层重排的归属校验与删除的子节点保护。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminGrammarServiceTest {

    @Mock
    private GrammarNodeMapper nodeMapper;

    @Mock
    private GrammarSectionMapper sectionMapper;

    private AdminGrammarService newService() {
        return new AdminGrammarService(nodeMapper, sectionMapper);
    }

    private GrammarNode node(String id, String parentId, int sortOrder) {
        GrammarNode node = new GrammarNode();
        node.setId(id);
        node.setParentId(parentId);
        node.setName("节点 " + id);
        node.setSortOrder(sortOrder);
        return node;
    }

    @Test
    @DisplayName("新建叶节点同事务补一份占位章节,ID 与节点相同")
    void createsPairedSectionOnNewLeaf() {
        // 新建路径:回显走单节点查询(toRow),不再重扫全表
        when(nodeMapper.selectById(any())).thenReturn(null);
        when(nodeMapper.selectList(any())).thenReturn(List.of());
        when(nodeMapper.selectCount(any())).thenReturn(0L);
        when(sectionMapper.selectById(any())).thenReturn(null);
        when(nodeMapper.insert(any(GrammarNode.class))).thenReturn(1);
        when(sectionMapper.insert(any(GrammarSection.class))).thenReturn(1);

        newService().saveNode(null, GrammarNodeUpsertRequest.builder().name("非谓语动词").build());

        ArgumentCaptor<GrammarSection> captor = ArgumentCaptor.forClass(GrammarSection.class);
        verify(sectionMapper).insert(captor.capture());
        GrammarSection section = captor.getValue();
        ArgumentCaptor<GrammarNode> nodeCaptor = ArgumentCaptor.forClass(GrammarNode.class);
        verify(nodeMapper).insert(nodeCaptor.capture());
        assertThat(section.getId()).isEqualTo(nodeCaptor.getValue().getId());
        assertThat(section.getNodeId()).isEqualTo(nodeCaptor.getValue().getId());
        assertThat(section.getMarkdown()).contains("待补充");
    }

    @Test
    @DisplayName("父节点不存在即 404,且不写库")
    void rejectsMissingParent() {
        when(nodeMapper.selectById("missing")).thenReturn(null);

        assertThatThrownBy(() -> newService().saveNode(null,
                GrammarNodeUpsertRequest.builder().name("子节点").parentId("missing").build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("父节点不存在");

        verify(nodeMapper, never()).insert(any(GrammarNode.class));
        verify(sectionMapper, never()).insert(any(GrammarSection.class));
    }

    @Test
    @DisplayName("把父节点改成自己的子孙即拒绝(环检测)")
    void rejectsCycleOnReparent() {
        // g-a → g-b → g-c;把 g-a 的父节点改成 g-c 会成环
        when(nodeMapper.selectById("g-a")).thenReturn(node("g-a", null, 10));
        when(nodeMapper.selectById("g-c")).thenReturn(node("g-c", "g-b", 30));
        when(nodeMapper.selectById("g-b")).thenReturn(node("g-b", "g-a", 20));
        when(nodeMapper.selectList(any())).thenReturn(List.of(node("g-a", null, 10)));
        when(sectionMapper.selectList(any())).thenReturn(List.of());

        assertThatThrownBy(() -> newService().saveNode("g-a",
                GrammarNodeUpsertRequest.builder().name("动词").parentId("g-c").build()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能把节点移动到自己的子孙下");
    }

    @Test
    @DisplayName("有子节点的目录不允许删除")
    void blocksDeletingNodeWithChildren() {
        when(nodeMapper.selectById("g-a")).thenReturn(node("g-a", null, 10));
        when(nodeMapper.selectCount(any())).thenReturn(3L);

        assertThatThrownBy(() -> newService().deleteNode("g-a"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("请先删除子节点");

        verify(nodeMapper, never()).deleteById(any(String.class));
    }

    @Test
    @DisplayName("删除叶节点时一并删除配对章节")
    void deletesLeafWithItsSection() {
        when(nodeMapper.selectById("g-x")).thenReturn(node("g-x", null, 10));
        when(nodeMapper.selectCount(any())).thenReturn(0L);

        newService().deleteNode("g-x");

        verify(sectionMapper).deleteById("g-x");
        verify(nodeMapper).deleteById("g-x");
    }

    @Test
    @DisplayName("整层重排:节点不属于该层即拒绝")
    void rejectsReorderAcrossLevels() {
        when(nodeMapper.selectById("g-b")).thenReturn(node("g-b", "g-a", 20));

        assertThatThrownBy(() -> newService().reorderNodes(null, List.of("g-b")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不属于");
    }

    @Test
    @DisplayName("整层重排按 10/20 重编 sortOrder,并同步章节排序")
    void reordersSiblings() {
        GrammarNode first = node("g-1", null, 30);
        GrammarNode second = node("g-2", null, 5);
        when(nodeMapper.selectById("g-1")).thenReturn(first);
        when(nodeMapper.selectById("g-2")).thenReturn(second);
        GrammarSection section = new GrammarSection();
        section.setId("g-1");
        section.setNodeId("g-1");
        when(sectionMapper.selectById("g-1")).thenReturn(section);
        when(sectionMapper.selectById("g-2")).thenReturn(null);
        when(nodeMapper.selectList(any())).thenReturn(List.of(first, second));
        when(sectionMapper.selectList(any())).thenReturn(List.of());

        newService().reorderNodes(null, List.of("g-1", "g-2"));

        assertThat(first.getSortOrder()).isEqualTo(10);
        assertThat(second.getSortOrder()).isEqualTo(20);
        assertThat(section.getSortOrder()).isEqualTo(10);
        verify(nodeMapper, times(2)).updateById(any(GrammarNode.class));
    }

    @Test
    @DisplayName("重排清单为空即拒绝")
    void rejectsEmptyReorder() {
        assertThatThrownBy(() -> newService().reorderNodes(null, List.of()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("ids 不能为空");
    }

    @Test
    @DisplayName("节点没有正文时读取章节给出可行动提示")
    void missingSectionMessageIsActionable() {
        when(sectionMapper.selectById("g-y")).thenReturn(null);

        assertThatThrownBy(() -> newService().section("g-y"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("还没有正文");
    }

    @Test
    @DisplayName("保存章节覆盖占位正文")
    void savesSectionMarkdown() {
        GrammarNode existing = node("g-z", null, 10);
        GrammarSection section = new GrammarSection();
        section.setId("g-z");
        section.setNodeId("g-z");
        section.setMarkdown("## 待补充");
        when(nodeMapper.selectById("g-z")).thenReturn(existing);
        when(sectionMapper.selectById("g-z")).thenReturn(section);

        var saved = newService().saveSection("g-z", GrammarSectionUpsertRequest.builder()
                .title("动词不定式").markdown("## 动词不定式\n\n正文").build());

        assertThat(saved.getMarkdown()).contains("动词不定式");
        assertThat(saved.getTitle()).isEqualTo("动词不定式");
        verify(sectionMapper).updateById(section);
    }
}
