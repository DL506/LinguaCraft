package com.linguacraft.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模考作答进度实体:对应表 exam_progress(断点续答,扩展契约 E2;交卷成功后删除)。
 */
@Data
@TableName("exam_progress")
public class ExamProgress {

    /** 主键(自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 试卷 ID */
    private String examId;

    /** 客观题作答进度 JSON(全局编号 -> 答案) */
    private String objective;

    /** 作文作答进度 JSON({content}) */
    private String writing;

    /** 已答题耗时(秒) */
    private Integer duration;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}