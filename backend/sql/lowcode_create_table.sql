# 低代码模块数据库初始化
# 在 yu_ai_code_mother 库中执行

USE yu_ai_code_mother;

-- App 表新增编辑模式字段
ALTER TABLE app ADD COLUMN editorMode VARCHAR(32) DEFAULT 'ai_chat' COMMENT '编辑模式：ai_chat / lowcode' AFTER codeGenType;

-- 页面 Schema 表
CREATE TABLE IF NOT EXISTS page_schema
(
    id              BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    appId           BIGINT NOT NULL COMMENT '关联应用ID',
    schemaContent   MEDIUMTEXT NOT NULL COMMENT '页面结构 Schema JSON',
    version         INT DEFAULT 1 COMMENT '版本号',
    createTime      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete        TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX idx_appId (appId),
    INDEX idx_appId_version (appId, version)
) COMMENT '页面Schema' COLLATE = utf8mb4_unicode_ci;

-- 组件模板表
CREATE TABLE IF NOT EXISTS component_template
(
    id              BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    name            VARCHAR(128) NOT NULL COMMENT '组件名称',
    componentKey    VARCHAR(64) NOT NULL COMMENT '组件唯一标识',
    category        VARCHAR(64) NOT NULL COMMENT '分类：layout/basic/business',
    icon            VARCHAR(256) NULL COMMENT '图标名称',
    thumbnail       VARCHAR(512) NULL COMMENT '缩略图URL',
    defaultSchema   TEXT NOT NULL COMMENT '组件默认Schema JSON',
    propsSchema     TEXT NOT NULL COMMENT '属性配置描述JSON',
    priority        INT DEFAULT 0 NOT NULL COMMENT '排序优先级',
    userId          BIGINT DEFAULT 0 NOT NULL COMMENT '创建者（0=系统内置）',
    createTime      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete        TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    UNIQUE KEY uk_componentKey (componentKey),
    INDEX idx_category (category)
) COMMENT '组件模板' COLLATE = utf8mb4_unicode_ci;
