package com.yupi.yuaicodemother.service;

import com.yupi.yuaicodemother.model.dto.lowcode.ComponentTemplateQueryRequest;
import com.yupi.yuaicodemother.model.dto.lowcode.SchemaSaveRequest;
import com.yupi.yuaicodemother.model.entity.User;
import com.yupi.yuaicodemother.model.vo.ComponentTemplateVO;
import com.yupi.yuaicodemother.model.vo.PageSchemaVO;

import java.util.List;

/**
 * 低代码服务接口
 */
public interface LowCodeService {

    /**
     * 保存 Schema（自动递增版本号）
     *
     * @param schemaSaveRequest 保存请求
     * @param loginUser         当前登录用户
     * @return 保存后的版本号
     */
    Integer saveSchema(SchemaSaveRequest schemaSaveRequest, User loginUser);

    /**
     * 获取应用的最新 Schema
     *
     * @param appId     应用 ID
     * @param loginUser 当前登录用户
     * @return Schema 视图对象
     */
    PageSchemaVO getLatestSchema(Long appId, User loginUser);

    /**
     * 获取 Schema 版本历史列表
     *
     * @param appId     应用 ID
     * @param loginUser 当前登录用户
     * @return 版本历史列表（不含 schemaContent，只有版本号和时间）
     */
    List<PageSchemaVO> getSchemaHistory(Long appId, User loginUser);

    /**
     * 回滚到指定版本
     *
     * @param appId     应用 ID
     * @param version   目标版本号
     * @param loginUser 当前登录用户
     * @return 回滚后的版本号
     */
    Integer rollbackSchema(Long appId, Integer version, User loginUser);

    /**
     * 导出 Schema 为代码文件
     *
     * @param appId     应用 ID
     * @param loginUser 当前登录用户
     * @return 导出的文件目录路径
     */
    String exportSchemaToCode(Long appId, User loginUser);

    /**
     * 获取组件模板列表
     *
     * @param queryRequest 查询请求
     * @return 组件模板列表
     */
    List<ComponentTemplateVO> listComponentTemplates(ComponentTemplateQueryRequest queryRequest);

    /**
     * 获取单个组件模板详情
     *
     * @param componentKey 组件标识
     * @return 组件模板详情
     */
    ComponentTemplateVO getComponentTemplate(String componentKey);

    /**
     * 初始化内置组件模板（系统启动时调用）
     */
    void initBuiltinTemplates();

    /**
     * 从应用已有代码解析生成 Schema（首次切换到低代码模式时调用）
     *
     * @param appId       应用 ID
     * @param codeGenType 代码生成类型
     * @param loginUser   当前登录用户
     * @return Schema 视图对象
     */
    PageSchemaVO parseAndSaveSchemaFromCode(Long appId, String codeGenType, User loginUser);

    /**
     * 保存低代码编辑后的 HTML 到应用代码文件
     *
     * @param appId       应用 ID
     * @param htmlContent 完整的 HTML 内容
     * @param loginUser   当前登录用户
     */
    void saveHtmlToCodeFile(Long appId, String htmlContent, User loginUser);
}
