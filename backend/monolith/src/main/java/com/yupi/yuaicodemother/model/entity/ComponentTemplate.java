package com.yupi.yuaicodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组件模板 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("component_template")
public class ComponentTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件唯一标识
     */
    @Column("componentKey")
    private String componentKey;

    /**
     * 分类：layout/basic/business
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
    @Column("defaultSchema")
    private String defaultSchema;

    /**
     * 属性配置描述JSON
     */
    @Column("propsSchema")
    private String propsSchema;

    /**
     * 排序优先级
     */
    private Integer priority;

    /**
     * 创建者（0=系统内置）
     */
    @Column("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @Column(value = "createTime", onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(value = "updateTime", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;
}
