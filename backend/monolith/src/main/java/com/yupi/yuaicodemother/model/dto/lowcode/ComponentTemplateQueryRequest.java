package com.yupi.yuaicodemother.model.dto.lowcode;

import lombok.Data;

import java.io.Serializable;

/**
 * 组件模板查询请求
 */
@Data
public class ComponentTemplateQueryRequest implements Serializable {

    /**
     * 分类筛选：layout/basic/business
     */
    private String category;

    /**
     * 名称模糊搜索
     */
    private String name;

    private static final long serialVersionUID = 1L;
}
