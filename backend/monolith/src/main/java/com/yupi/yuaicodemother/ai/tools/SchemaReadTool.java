package com.yupi.yuaicodemother.ai.tools;

import cn.hutool.json.JSONObject;
import com.mybatisflex.core.query.QueryWrapper;
import com.yupi.yuaicodemother.mapper.PageSchemaMapper;
import com.yupi.yuaicodemother.model.entity.PageSchema;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Schema 读取工具
 * 支持 AI 读取当前应用的页面 Schema
 */
@Slf4j
@Component
public class SchemaReadTool extends BaseTool {

    @Resource
    private PageSchemaMapper pageSchemaMapper;

    @Tool("读取当前应用的页面 Schema 结构")
    public String readSchema(@ToolMemoryId Long appId) {
        PageSchema pageSchema = pageSchemaMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("appId", appId)
                        .orderBy("version", false)
                        .limit(1)
        );

        if (pageSchema == null) {
            return "当前应用还没有页面 Schema，请先通过添加组件工具创建页面内容。";
        }

        return pageSchema.getSchemaContent();
    }

    @Override
    public String getToolName() {
        return "readSchema";
    }

    @Override
    public String getDisplayName() {
        return "读取页面结构";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "[工具调用] 读取页面结构";
    }
}
