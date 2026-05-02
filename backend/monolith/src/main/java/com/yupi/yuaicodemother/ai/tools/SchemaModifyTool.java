package com.yupi.yuaicodemother.ai.tools;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.yupi.yuaicodemother.mapper.PageSchemaMapper;
import com.yupi.yuaicodemother.model.entity.PageSchema;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Schema 修改工具
 * 支持 AI 修改组件属性、添加组件、删除组件
 */
@Slf4j
@Component
public class SchemaModifyTool extends BaseTool {

    @Resource
    private PageSchemaMapper pageSchemaMapper;

    @Tool("修改页面中指定组件的属性或样式")
    public String modifyComponent(
            @P("组件 ID") String componentId,
            @P("要修改的属性 JSON，如 {\"title\":\"新标题\"}") String propsJson,
            @P("要修改的样式 JSON，如 {\"backgroundColor\":\"#fff\"}，不修改样式则传空字符串") String styleJson,
            @ToolMemoryId Long appId
    ) {
        try {
            PageSchema pageSchema = getLatestSchema(appId);
            if (pageSchema == null) {
                return "错误：当前应用没有页面 Schema";
            }

            JSONObject schema = JSONUtil.parseObj(pageSchema.getSchemaContent());
            JSONObject targetComponent = findComponentById(schema, componentId);

            if (targetComponent == null) {
                return "错误：未找到 ID 为 " + componentId + " 的组件";
            }

            // 修改属性
            if (propsJson != null && !propsJson.isBlank()) {
                JSONObject newProps = JSONUtil.parseObj(propsJson);
                JSONObject existingProps = targetComponent.getJSONObject("props");
                if (existingProps == null) {
                    existingProps = new JSONObject();
                }
                existingProps.putAll(newProps);
                targetComponent.set("props", existingProps);
            }

            // 修改样式
            if (styleJson != null && !styleJson.isBlank()) {
                JSONObject newStyle = JSONUtil.parseObj(styleJson);
                JSONObject existingStyle = targetComponent.getJSONObject("style");
                if (existingStyle == null) {
                    existingStyle = new JSONObject();
                }
                existingStyle.putAll(newStyle);
                targetComponent.set("style", existingStyle);
            }

            saveNewVersion(appId, schema.toString());
            return "组件修改成功: " + componentId;
        } catch (Exception e) {
            log.error("修改组件失败: {}", e.getMessage(), e);
            return "修改组件失败: " + e.getMessage();
        }
    }

    @Tool("向页面添加一个新组件")
    public String addComponent(
            @P("组件类型，如 navbar、hero、card、heading 等") String componentType,
            @P("组件属性 JSON") String propsJson,
            @P("插入位置索引，-1 表示末尾") int position,
            @ToolMemoryId Long appId
    ) {
        try {
            PageSchema pageSchema = getLatestSchema(appId);
            JSONObject schema;
            if (pageSchema == null) {
                schema = JSONUtil.parseObj("{\"version\":\"1.0\",\"globalStyle\":{\"fontFamily\":\"system-ui, -apple-system, sans-serif\",\"primaryColor\":\"#1890ff\"},\"pages\":[{\"id\":\"page_1\",\"path\":\"/\",\"name\":\"首页\",\"components\":[]}]}");
            } else {
                schema = JSONUtil.parseObj(pageSchema.getSchemaContent());
            }

            JSONObject newComponent = new JSONObject();
            newComponent.set("id", "comp_" + System.currentTimeMillis());
            newComponent.set("type", componentType);
            newComponent.set("props", JSONUtil.parseObj(propsJson != null ? propsJson : "{}"));
            newComponent.set("style", new JSONObject());

            JSONArray pages = schema.getJSONArray("pages");
            JSONObject firstPage = pages.getJSONObject(0);
            JSONArray components = firstPage.getJSONArray("components");
            if (components == null) {
                components = new JSONArray();
                firstPage.set("components", components);
            }

            if (position < 0 || position >= components.size()) {
                components.add(newComponent);
            } else {
                components.add(position, newComponent);
            }

            saveNewVersion(appId, schema.toString());
            return "组件添加成功: " + componentType + " (ID: " + newComponent.getStr("id") + ")";
        } catch (Exception e) {
            log.error("添加组件失败: {}", e.getMessage(), e);
            return "添加组件失败: " + e.getMessage();
        }
    }

    @Tool("删除页面中指定的组件")
    public String deleteComponent(
            @P("要删除的组件 ID") String componentId,
            @ToolMemoryId Long appId
    ) {
        try {
            PageSchema pageSchema = getLatestSchema(appId);
            if (pageSchema == null) {
                return "错误：当前应用没有页面 Schema";
            }

            JSONObject schema = JSONUtil.parseObj(pageSchema.getSchemaContent());
            boolean removed = removeComponentById(schema, componentId);

            if (!removed) {
                return "错误：未找到 ID 为 " + componentId + " 的组件";
            }

            saveNewVersion(appId, schema.toString());
            return "组件删除成功: " + componentId;
        } catch (Exception e) {
            log.error("删除组件失败: {}", e.getMessage(), e);
            return "删除组件失败: " + e.getMessage();
        }
    }

    // ========== 私有方法 ==========

    private PageSchema getLatestSchema(Long appId) {
        return pageSchemaMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("appId", appId)
                        .orderBy("version", false)
                        .limit(1)
        );
    }

    private void saveNewVersion(Long appId, String schemaContent) {
        PageSchema latest = getLatestSchema(appId);
        int newVersion = (latest != null ? latest.getVersion() : 0) + 1;

        PageSchema newSchema = PageSchema.builder()
                .appId(appId)
                .schemaContent(schemaContent)
                .version(newVersion)
                .build();
        pageSchemaMapper.insert(newSchema);
    }

    private JSONObject findComponentById(JSONObject schema, String componentId) {
        JSONArray pages = schema.getJSONArray("pages");
        if (pages == null) return null;

        for (int i = 0; i < pages.size(); i++) {
            JSONObject page = pages.getJSONObject(i);
            JSONArray components = page.getJSONArray("components");
            JSONObject found = findInComponents(components, componentId);
            if (found != null) return found;
        }
        return null;
    }

    private JSONObject findInComponents(JSONArray components, String componentId) {
        if (components == null) return null;

        for (int i = 0; i < components.size(); i++) {
            JSONObject comp = components.getJSONObject(i);
            if (componentId.equals(comp.getStr("id"))) {
                return comp;
            }
            JSONArray children = comp.getJSONArray("children");
            JSONObject found = findInComponents(children, componentId);
            if (found != null) return found;
        }
        return null;
    }

    private boolean removeComponentById(JSONObject schema, String componentId) {
        JSONArray pages = schema.getJSONArray("pages");
        if (pages == null) return false;

        for (int i = 0; i < pages.size(); i++) {
            JSONObject page = pages.getJSONObject(i);
            JSONArray components = page.getJSONArray("components");
            if (removeFromComponents(components, componentId)) {
                return true;
            }
        }
        return false;
    }

    private boolean removeFromComponents(JSONArray components, String componentId) {
        if (components == null) return false;

        for (int i = 0; i < components.size(); i++) {
            JSONObject comp = components.getJSONObject(i);
            if (componentId.equals(comp.getStr("id"))) {
                components.remove(i);
                return true;
            }
            JSONArray children = comp.getJSONArray("children");
            if (removeFromComponents(children, componentId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getToolName() {
        return "modifySchema";
    }

    @Override
    public String getDisplayName() {
        return "修改页面结构";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "[工具调用] 修改页面结构";
    }
}
