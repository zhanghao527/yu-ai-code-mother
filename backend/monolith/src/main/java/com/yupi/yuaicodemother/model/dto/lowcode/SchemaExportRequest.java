package com.yupi.yuaicodemother.model.dto.lowcode;

import lombok.Data;

import java.io.Serializable;

/**
 * Schema 导出请求
 */
@Data
public class SchemaExportRequest implements Serializable {

    /**
     * 应用 ID
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}
