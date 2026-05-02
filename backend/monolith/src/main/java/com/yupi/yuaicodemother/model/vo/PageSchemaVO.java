package com.yupi.yuaicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 页面 Schema 视图对象
 */
@Data
public class PageSchemaVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 关联应用ID
     */
    private Long appId;

    /**
     * 页面结构 Schema JSON
     */
    private String schemaContent;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
