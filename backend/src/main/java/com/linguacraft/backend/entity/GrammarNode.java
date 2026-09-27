package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语法目录节点实体:对应表 grammar_node(自引用;顶层 parent_id 为 NULL)。
 */
@Data
@TableName("grammar_node")
public class GrammarNode {

    /** 节点 ID(如 tenses / tenses-overview;非自增) */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 父节点 ID(顶层为 NULL) */
    private String parentId;

    /** 节点名称 */
    private String name;

    /** 同级排序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}