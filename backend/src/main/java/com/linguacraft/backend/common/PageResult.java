package com.linguacraft.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 统一分页响应:所有列表接口返回该结构(前端契约 PageResult)。
 *
 * <p>注意:total/page/size 使用原始类型 long,确保序列化为数字;
 * 包装类型 Long 会被全局 Jackson 配置序列化为字符串(用于实体主键 id)。</p>
 *
 * @param <T> 列表元素类型
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> list;

    /** 总条数 */
    private long total;

    /** 当前页码(从 1 开始) */
    private long page;

    /** 每页条数 */
    private long size;
}