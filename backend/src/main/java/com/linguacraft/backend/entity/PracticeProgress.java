package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作答进度实体:对应表 practice_progress(断点续答持久化,扩展契约 E1)。
 *
 * <p>每条记录对应"某用户某题"的未提交作答;提交成功后删除记录。
 * 口径说明:题目作答进度由后端数据库保存,前端不做本地草稿(项目既定决策)。</p>
 */
@Data
@TableName("practice_progress")
public class PracticeProgress {

    /** 主键(自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 题目 ID */
    private String questionId;

    /** 题型 */
    private String type;

    /** 作答进度原文 JSON(结构同提交 answers) */
    private String answers;

    /** 已作答耗时(秒) */
    private Integer duration;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}