package com.yupi.yuaicodemother.model.dto.lowcode;

import lombok.Data;

import java.io.Serializable;

/**
 * 低代码编辑后保存 HTML 请求
 */
@Data
public class HtmlSaveRequest implements Serializable {

    /**
     * 应用 ID
     */
    private Long appId;

    /**
     * 完整的 HTML 内容
     */
    private String htmlContent;

    private static final long serialVersionUID = 1L;
}
