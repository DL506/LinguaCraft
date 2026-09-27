package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语法章节实体:对应表 grammar_section(叶节点章节内容,markdown 文本)。
 */
@Data
@TableName("grammar_section")
public class GrammarSection {

    /** 章节 ID(与叶节点 node id 一致;非自增) */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 所属节点 ID */
    private String nodeId;

    /** 章节标题 */
    private String title;

    /** Markdown 内容(考点精讲/真题示例/易错点辨析/专项练习) */
    private String markdown;

    /** 展示排序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}