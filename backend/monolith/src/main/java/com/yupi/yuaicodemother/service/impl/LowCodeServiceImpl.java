package com.yupi.yuaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.yupi.yuaicodemother.core.lowcode.SchemaExporter;
import com.yupi.yuaicodemother.core.lowcode.HtmlToSchemaParser;
import com.yupi.yuaicodemother.constant.AppConstant;
import com.yupi.yuaicodemother.exception.BusinessException;
import com.yupi.yuaicodemother.exception.ErrorCode;
import com.yupi.yuaicodemother.exception.ThrowUtils;
import com.yupi.yuaicodemother.mapper.ComponentTemplateMapper;
import com.yupi.yuaicodemother.mapper.PageSchemaMapper;
import com.yupi.yuaicodemother.model.dto.lowcode.ComponentTemplateQueryRequest;
import com.yupi.yuaicodemother.model.dto.lowcode.SchemaSaveRequest;
import com.yupi.yuaicodemother.model.entity.App;
import com.yupi.yuaicodemother.model.entity.ComponentTemplate;
import com.yupi.yuaicodemother.model.entity.PageSchema;
import com.yupi.yuaicodemother.model.entity.User;
import com.yupi.yuaicodemother.model.vo.ComponentTemplateVO;
import com.yupi.yuaicodemother.model.vo.PageSchemaVO;
import com.yupi.yuaicodemother.service.AppService;
import com.yupi.yuaicodemother.service.LowCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 低代码服务实现
 */
@Service
@Slf4j
public class LowCodeServiceImpl implements LowCodeService {

    @Resource
    private PageSchemaMapper pageSchemaMapper;

    @Resource
    private ComponentTemplateMapper componentTemplateMapper;

    @Resource
    private AppService appService;

    @Resource
    private SchemaExporter schemaExporter;

    @Resource
    private HtmlToSchemaParser htmlToSchemaParser;

    /**
     * 最大保留版本数
     */
    private static final int MAX_VERSIONS = 50;

    @Override
    public Integer saveSchema(SchemaSaveRequest schemaSaveRequest, User loginUser) {
        Long appId = schemaSaveRequest.getAppId();
        String schemaContent = schemaSaveRequest.getSchemaContent();

        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(schemaContent), ErrorCode.PARAMS_ERROR, "Schema内容不能为空");
        ThrowUtils.throwIf(!JSONUtil.isTypeJSON(schemaContent), ErrorCode.PARAMS_ERROR, "Schema格式错误，必须是合法JSON");

        // 权限校验
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限编辑该应用");
        }

        // 获取当前最大版本号
        Integer maxVersion = getMaxVersion(appId);
        int newVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        // 保存新版本
        PageSchema pageSchema = PageSchema.builder()
                .appId(appId)
                .schemaContent(schemaContent)
                .version(newVersion)
                .build();
        pageSchemaMapper.insert(pageSchema);

        // 清理旧版本
        cleanOldVersions(appId, newVersion);

        log.info("Schema 保存成功，appId={}, version={}", appId, newVersion);
        return newVersion;
    }

    @Override
    public PageSchemaVO getLatestSchema(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        PageSchema pageSchema = pageSchemaMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("appId", appId)
                        .orderBy("version", false)
                        .limit(1)
        );

        if (pageSchema == null) {
            PageSchemaVO vo = new PageSchemaVO();
            vo.setAppId(appId);
            vo.setSchemaContent(getDefaultSchema());
            vo.setVersion(0);
            return vo;
        }

        return toPageSchemaVO(pageSchema);
    }

    @Override
    public List<PageSchemaVO> getSchemaHistory(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用");
        }

        List<PageSchema> schemas = pageSchemaMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("appId", appId)
                        .orderBy("version", false)
                        .limit(MAX_VERSIONS)
        );

        return schemas.stream().map(this::toPageSchemaVO).collect(Collectors.toList());
    }

    @Override
    public Integer rollbackSchema(Long appId, Integer version, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(version == null || version <= 0, ErrorCode.PARAMS_ERROR, "版本号无效");

        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }

        PageSchema targetSchema = pageSchemaMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("appId", appId)
                        .eq("version", version)
        );
        ThrowUtils.throwIf(targetSchema == null, ErrorCode.NOT_FOUND_ERROR, "目标版本不存在");

        SchemaSaveRequest saveRequest = new SchemaSaveRequest();
        saveRequest.setAppId(appId);
        saveRequest.setSchemaContent(targetSchema.getSchemaContent());
        return saveSchema(saveRequest, loginUser);
    }

    @Override
    public String exportSchemaToCode(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }

        PageSchemaVO latestSchema = getLatestSchema(appId, loginUser);
        ThrowUtils.throwIf(StrUtil.isBlank(latestSchema.getSchemaContent()), ErrorCode.OPERATION_ERROR, "Schema为空，请先编辑页面");

        // 导出到应用原有的代码目录（覆盖），这样预览 iframe 能直接看到更新
        String codeGenType = app.getCodeGenType();
        File exportDir = schemaExporter.exportToFiles(latestSchema.getSchemaContent(), appId, codeGenType);
        return exportDir.getAbsolutePath();
    }

    @Override
    @Cacheable(value = "component_templates", key = "#queryRequest?.category + '_' + #queryRequest?.name")
    public List<ComponentTemplateVO> listComponentTemplates(ComponentTemplateQueryRequest queryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();

        if (queryRequest != null) {
            if (StrUtil.isNotBlank(queryRequest.getCategory())) {
                queryWrapper.eq("category", queryRequest.getCategory());
            }
            if (StrUtil.isNotBlank(queryRequest.getName())) {
                queryWrapper.like("name", queryRequest.getName());
            }
        }

        queryWrapper.orderBy("priority", false).orderBy("id", true);

        List<ComponentTemplate> templates = componentTemplateMapper.selectListByQuery(queryWrapper);
        return templates.stream().map(this::toComponentTemplateVO).collect(Collectors.toList());
    }

    @Override
    public ComponentTemplateVO getComponentTemplate(String componentKey) {
        ThrowUtils.throwIf(StrUtil.isBlank(componentKey), ErrorCode.PARAMS_ERROR, "组件标识不能为空");

        ComponentTemplate template = componentTemplateMapper.selectOneByQuery(
                QueryWrapper.create().eq("componentKey", componentKey)
        );
        ThrowUtils.throwIf(template == null, ErrorCode.NOT_FOUND_ERROR, "组件模板不存在");

        return toComponentTemplateVO(template);
    }

    @Override
    public void initBuiltinTemplates() {
        long count = componentTemplateMapper.selectCountByQuery(QueryWrapper.create());
        if (count > 0) {
            log.info("组件模板已存在，跳过初始化");
            return;
        }
        log.info("开始初始化内置组件模板...");
        initLayoutComponents();
        initBasicComponents();
        initBusinessComponents();
        log.info("内置组件模板初始化完成");
    }

    @Override
    public PageSchemaVO parseAndSaveSchemaFromCode(Long appId, String codeGenType, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        // 权限校验
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }

        // 先检查是否已有 Schema
        PageSchemaVO existing = getLatestSchema(appId, loginUser);
        if (existing.getVersion() > 0) {
            // 已有 Schema，直接返回
            return existing;
        }

        // 从代码文件解析 Schema
        String parsedSchema = htmlToSchemaParser.parseToSchema(appId, codeGenType != null ? codeGenType : "html");
        if (parsedSchema == null) {
            // 解析失败，返回默认空 Schema
            return existing;
        }

        // 保存解析出的 Schema
        SchemaSaveRequest saveRequest = new SchemaSaveRequest();
        saveRequest.setAppId(appId);
        saveRequest.setSchemaContent(parsedSchema);
        Integer version = saveSchema(saveRequest, loginUser);

        PageSchemaVO vo = new PageSchemaVO();
        vo.setAppId(appId);
        vo.setSchemaContent(parsedSchema);
        vo.setVersion(version);
        return vo;
    }

    @Override
    public void saveHtmlToCodeFile(Long appId, String htmlContent, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(htmlContent), ErrorCode.PARAMS_ERROR, "HTML内容不能为空");

        // 权限校验
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }

        // 确定代码目录
        String codeGenType = app.getCodeGenType();
        String dirName = codeGenType + "_" + appId;
        String dirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + dirName;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 写入 index.html
        File indexFile = new File(dir, "index.html");
        cn.hutool.core.io.FileUtil.writeString(htmlContent, indexFile, java.nio.charset.StandardCharsets.UTF_8);
        log.info("低代码编辑保存成功，appId={}, path={}", appId, indexFile.getAbsolutePath());
    }

    // ========== 私有方法 ==========

    private Integer getMaxVersion(Long appId) {
        PageSchema latest = pageSchemaMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("appId", appId)
                        .orderBy("version", false)
                        .limit(1)
        );
        return latest != null ? latest.getVersion() : null;
    }

    private void cleanOldVersions(Long appId, int currentVersion) {
        int deleteBeforeVersion = currentVersion - MAX_VERSIONS;
        if (deleteBeforeVersion > 0) {
            pageSchemaMapper.deleteByQuery(
                    QueryWrapper.create()
                            .eq("appId", appId)
                            .le("version", deleteBeforeVersion)
            );
        }
    }

    private String getDefaultSchema() {
        return """
                {
                  "version": "1.0",
                  "globalStyle": {
                    "fontFamily": "system-ui, -apple-system, sans-serif",
                    "primaryColor": "#1890ff"
                  },
                  "pages": [
                    {
                      "id": "page_1",
                      "path": "/",
                      "name": "首页",
                      "components": []
                    }
                  ]
                }
                """;
    }

    private PageSchemaVO toPageSchemaVO(PageSchema pageSchema) {
        PageSchemaVO vo = new PageSchemaVO();
        BeanUtil.copyProperties(pageSchema, vo);
        return vo;
    }

    private ComponentTemplateVO toComponentTemplateVO(ComponentTemplate template) {
        ComponentTemplateVO vo = new ComponentTemplateVO();
        BeanUtil.copyProperties(template, vo);
        return vo;
    }

    // ========== 内置组件初始化 ==========

    private void initLayoutComponents() {
        insertTemplate("容器", "container", "layout", "AppstoreOutlined", 100,
                "{\"type\":\"container\",\"props\":{\"layout\":\"flex\",\"columns\":1,\"gap\":\"16px\"},\"style\":{\"padding\":\"24px\"},\"children\":[]}",
                "[{\"key\":\"layout\",\"label\":\"布局方式\",\"type\":\"select\",\"options\":[{\"label\":\"弹性布局\",\"value\":\"flex\"},{\"label\":\"网格布局\",\"value\":\"grid\"}]},{\"key\":\"columns\",\"label\":\"列数\",\"type\":\"slider\",\"min\":1,\"max\":6},{\"key\":\"gap\",\"label\":\"间距\",\"type\":\"input\"}]");

        insertTemplate("分栏", "columns", "layout", "ColumnWidthOutlined", 90,
                "{\"type\":\"columns\",\"props\":{\"columns\":2,\"gap\":\"24px\"},\"style\":{},\"children\":[]}",
                "[{\"key\":\"columns\",\"label\":\"列数\",\"type\":\"slider\",\"min\":2,\"max\":4},{\"key\":\"gap\",\"label\":\"间距\",\"type\":\"input\"}]");
    }

    private void initBasicComponents() {
        insertTemplate("标题文本", "heading", "basic", "FontSizeOutlined", 100,
                "{\"type\":\"heading\",\"props\":{\"text\":\"标题文本\",\"level\":2},\"style\":{}}",
                "[{\"key\":\"text\",\"label\":\"文本内容\",\"type\":\"input\",\"required\":true},{\"key\":\"level\",\"label\":\"标题级别\",\"type\":\"select\",\"options\":[{\"label\":\"H1\",\"value\":1},{\"label\":\"H2\",\"value\":2},{\"label\":\"H3\",\"value\":3},{\"label\":\"H4\",\"value\":4}]}]");

        insertTemplate("段落文本", "paragraph", "basic", "AlignLeftOutlined", 90,
                "{\"type\":\"paragraph\",\"props\":{\"text\":\"这是一段文本内容，可以在属性面板中修改。\"},\"style\":{}}",
                "[{\"key\":\"text\",\"label\":\"文本内容\",\"type\":\"textarea\",\"required\":true}]");

        insertTemplate("图片", "image", "basic", "PictureOutlined", 80,
                "{\"type\":\"image\",\"props\":{\"src\":\"https://picsum.photos/800/400\",\"alt\":\"图片描述\"},\"style\":{}}",
                "[{\"key\":\"src\",\"label\":\"图片地址\",\"type\":\"input\",\"required\":true},{\"key\":\"alt\",\"label\":\"替代文本\",\"type\":\"input\"}]");

        insertTemplate("按钮", "button", "basic", "BorderOutlined", 70,
                "{\"type\":\"button\",\"props\":{\"text\":\"点击按钮\",\"href\":\"#\",\"variant\":\"primary\"},\"style\":{}}",
                "[{\"key\":\"text\",\"label\":\"按钮文字\",\"type\":\"input\",\"required\":true},{\"key\":\"href\",\"label\":\"链接地址\",\"type\":\"input\"},{\"key\":\"variant\",\"label\":\"样式\",\"type\":\"select\",\"options\":[{\"label\":\"主要按钮\",\"value\":\"primary\"},{\"label\":\"次要按钮\",\"value\":\"secondary\"}]}]");

        insertTemplate("分割线", "divider", "basic", "LineOutlined", 60,
                "{\"type\":\"divider\",\"props\":{},\"style\":{}}",
                "[]");
    }

    private void initBusinessComponents() {
        insertTemplate("导航栏", "navbar", "business", "MenuOutlined", 100,
                "{\"type\":\"navbar\",\"props\":{\"title\":\"网站名称\",\"links\":[{\"text\":\"首页\",\"href\":\"#\"},{\"text\":\"关于\",\"href\":\"#about\"},{\"text\":\"联系\",\"href\":\"#contact\"}],\"sticky\":true},\"style\":{}}",
                "[{\"key\":\"title\",\"label\":\"网站标题\",\"type\":\"input\",\"required\":true},{\"key\":\"sticky\",\"label\":\"固定顶部\",\"type\":\"switch\"}]");

        insertTemplate("主视觉区", "hero", "business", "CrownOutlined", 95,
                "{\"type\":\"hero\",\"props\":{\"title\":\"欢迎来到我的网站\",\"subtitle\":\"用低代码快速搭建精美网页\",\"backgroundImage\":\"https://picsum.photos/1920/600\",\"buttonText\":\"了解更多\",\"buttonLink\":\"#about\"},\"style\":{}}",
                "[{\"key\":\"title\",\"label\":\"主标题\",\"type\":\"input\",\"required\":true},{\"key\":\"subtitle\",\"label\":\"副标题\",\"type\":\"input\"},{\"key\":\"backgroundImage\",\"label\":\"背景图\",\"type\":\"input\"},{\"key\":\"buttonText\",\"label\":\"按钮文字\",\"type\":\"input\"},{\"key\":\"buttonLink\",\"label\":\"按钮链接\",\"type\":\"input\"}]");

        insertTemplate("卡片", "card", "business", "CreditCardOutlined", 85,
                "{\"type\":\"card\",\"props\":{\"title\":\"卡片标题\",\"description\":\"这是卡片的描述内容，可以介绍产品或服务的特点。\",\"image\":\"https://picsum.photos/400/250\"},\"style\":{}}",
                "[{\"key\":\"title\",\"label\":\"标题\",\"type\":\"input\",\"required\":true},{\"key\":\"description\",\"label\":\"描述\",\"type\":\"textarea\"},{\"key\":\"image\",\"label\":\"图片地址\",\"type\":\"input\"}]");

        insertTemplate("特性网格", "feature-grid", "business", "AppstoreAddOutlined", 80,
                "{\"type\":\"feature-grid\",\"props\":{\"columns\":3,\"features\":[{\"icon\":\"🚀\",\"title\":\"快速\",\"description\":\"极速加载体验\"},{\"icon\":\"🎨\",\"title\":\"美观\",\"description\":\"精心设计的界面\"},{\"icon\":\"🔒\",\"title\":\"安全\",\"description\":\"数据安全保障\"}]},\"style\":{}}",
                "[{\"key\":\"columns\",\"label\":\"列数\",\"type\":\"slider\",\"min\":2,\"max\":4}]");

        insertTemplate("轮播图", "carousel", "business", "PlaySquareOutlined", 75,
                "{\"type\":\"carousel\",\"props\":{\"images\":[\"https://picsum.photos/1200/400?random=1\",\"https://picsum.photos/1200/400?random=2\",\"https://picsum.photos/1200/400?random=3\"],\"height\":\"400px\"},\"style\":{}}",
                "[{\"key\":\"height\",\"label\":\"高度\",\"type\":\"input\"}]");

        insertTemplate("页脚", "footer", "business", "LayoutOutlined", 70,
                "{\"type\":\"footer\",\"props\":{\"text\":\"© 2024 All Rights Reserved. Powered by QuickForge\",\"links\":[{\"text\":\"关于我们\",\"href\":\"#\"},{\"text\":\"联系方式\",\"href\":\"#\"},{\"text\":\"隐私政策\",\"href\":\"#\"}]},\"style\":{}}",
                "[{\"key\":\"text\",\"label\":\"版权文字\",\"type\":\"input\"}]");
    }

    private void insertTemplate(String name, String componentKey, String category, String icon, int priority,
                                String defaultSchema, String propsSchema) {
        ComponentTemplate template = ComponentTemplate.builder()
                .name(name)
                .componentKey(componentKey)
                .category(category)
                .icon(icon)
                .defaultSchema(defaultSchema)
                .propsSchema(propsSchema)
                .priority(priority)
                .userId(0L)
                .build();
        componentTemplateMapper.insert(template);
    }
}
