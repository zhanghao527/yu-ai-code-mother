package com.yupi.yuaicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 组件模板视图对象
 */
@Data
public class ComponentTemplateVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件唯一标识
     */
    private String componentKey;

    /**
     * 分类
     */
    private String category;

    /**
     * 图标名称
     */
    private String icon;

    /**
     * 缩略图URL
     */
    private String thumbnail;

    /**
     * 组件默认Schema JSON
     */
    private String defaultSchema;

    /**
     * 属性配置描述JSON
     */
    private String propsSchema;

    /**
     * 排序优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
}
