package com.yupi.yuaicodemother.model.dto.lowcode;

import lombok.Data;

import java.io.Serializable;

/**
 * Schema 保存请求
 */
@Data
public class SchemaSaveRequest implements Serializable {

    /**
     * 应用 ID
     */
    private Long appId;

    /**
     * Schema JSON 内容
     */
    private String schemaContent;

    private static final long serialVersionUID = 1L;
}
