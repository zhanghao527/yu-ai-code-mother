package com.yupi.yuaicodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 页面 Schema 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("page_schema")
public class PageSchema implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 关联应用ID
     */
    @Column("appId")
    private Long appId;

    /**
     * 页面结构 Schema JSON
     */
    @Column("schemaContent")
    private String schemaContent;

    /**
     * 版本号
     */
    private Integer version;

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
