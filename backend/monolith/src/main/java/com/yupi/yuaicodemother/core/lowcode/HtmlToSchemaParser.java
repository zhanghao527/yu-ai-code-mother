package com.yupi.yuaicodemother.core.lowcode;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.yupi.yuaicodemother.constant.AppConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 文件 → Schema 解析器
 * 从应用的代码目录中读取 HTML 文件，解析为低代码 Schema 格式
 */
@Slf4j
@Component
public class HtmlToSchemaParser {

    /**
     * 根据 appId 和 codeGenType 解析已有代码为 Schema
     *
     * @param appId       应用 ID
     * @param codeGenType 代码生成类型
     * @return Schema JSON 字符串，解析失败返回 null
     */
    public String parseToSchema(Long appId, String codeGenType) {
        try {
            String dirName = codeGenType + "_" + appId;
            String dirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + dirName;
            File dir = new File(dirPath);
            if (!dir.exists() || !dir.isDirectory()) {
                log.info("应用代码目录不存在: {}", dirPath);
                return null;
            }

            // 读取 index.html
            File indexHtml = new File(dir, "index.html");
            if (!indexHtml.exists()) {
                // Vue 项目可能在 dist 目录
                File distIndex = new File(dir, "dist/index.html");
                if (distIndex.exists()) {
                    indexHtml = distIndex;
                } else {
                    log.info("index.html 不存在: {}", dirPath);
                    return null;
                }
            }

            String htmlContent = Files.readString(indexHtml.toPath());
            return convertHtmlToSchema(htmlContent);
        } catch (IOException e) {
            log.error("解析 HTML 为 Schema 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将 HTML 内容转换为 Schema
     * 采用简化策略：提取页面的主要结构元素
     */
    private String convertHtmlToSchema(String html) {
        JSONObject schema = new JSONObject();
        schema.set("version", "1.0");

        JSONObject globalStyle = new JSONObject();
        globalStyle.set("fontFamily", "system-ui, -apple-system, sans-serif");
        globalStyle.set("primaryColor", extractPrimaryColor(html));
        schema.set("globalStyle", globalStyle);

        // 提取 title
        String title = extractTag(html, "title");

        JSONArray pages = new JSONArray();
        JSONObject page = new JSONObject();
        page.set("id", "page_1");
        page.set("path", "/");
        page.set("name", title != null ? title : "首页");

        JSONArray components = new JSONArray();

        // 提取 body 内容
        String body = extractBodyContent(html);
        if (body != null) {
            parseBodyToComponents(body, components);
        }

        page.set("components", components);
        pages.add(page);
        schema.set("pages", pages);

        return schema.toString();
    }

    /**
     * 解析 body 内容为组件列表
     * 采用基于标签的简化解析策略
     */
    private void parseBodyToComponents(String body, JSONArray components) {
        // 移除 script 标签
        body = body.replaceAll("(?s)<script[^>]*>.*?</script>", "");

        int compIndex = 0;

        // 解析 nav 标签 → navbar 组件
        Matcher navMatcher = Pattern.compile("(?s)<nav[^>]*>(.*?)</nav>").matcher(body);
        while (navMatcher.find()) {
            String navContent = navMatcher.group(1);
            JSONObject comp = createComponent("comp_" + (++compIndex), "navbar");
            JSONObject props = comp.getJSONObject("props");

            // 提取标题
            String navTitle = extractFirstText(navContent, "(?s)<[^>]*class=\"[^\"]*(?:title|brand|logo)[^\"]*\"[^>]*>([^<]+)</");
            if (navTitle == null) navTitle = extractFirstText(navContent, "<a[^>]*>([^<]+)</a>");
            if (navTitle != null) props.set("title", navTitle.trim());

            // 提取链接
            JSONArray links = extractLinks(navContent);
            if (!links.isEmpty()) props.set("links", links);

            components.add(comp);
        }

        // 解析 section/header 中的 hero 区域
        Matcher heroMatcher = Pattern.compile("(?s)<(?:section|header)[^>]*(?:class=\"[^\"]*(?:hero|banner|jumbotron)[^\"]*\"|style=\"[^\"]*(?:background|min-height)[^\"]*\")[^>]*>(.*?)</(?:section|header)>").matcher(body);
        if (!heroMatcher.find()) {
            // 尝试匹配第一个有背景图的 section
            heroMatcher = Pattern.compile("(?s)<section[^>]*>(.*?)</section>").matcher(body);
        }
        if (heroMatcher.find()) {
            String heroContent = heroMatcher.group(1);
            if (heroContent.contains("<h1") || heroContent.contains("<h2")) {
                JSONObject comp = createComponent("comp_" + (++compIndex), "hero");
                JSONObject props = comp.getJSONObject("props");

                String h1 = extractFirstText(heroContent, "<h1[^>]*>([^<]+)</h1>");
                String h2 = extractFirstText(heroContent, "<h2[^>]*>([^<]+)</h2>");
                String p = extractFirstText(heroContent, "<p[^>]*>([^<]+)</p>");

                if (h1 != null) props.set("title", h1.trim());
                else if (h2 != null) props.set("title", h2.trim());
                if (p != null) props.set("subtitle", p.trim());

                // 提取按钮
                String btnText = extractFirstText(heroContent, "<a[^>]*class=\"[^\"]*(?:btn|button)[^\"]*\"[^>]*>([^<]+)</a>");
                if (btnText == null) btnText = extractFirstText(heroContent, "<button[^>]*>([^<]+)</button>");
                if (btnText != null) props.set("buttonText", btnText.trim());

                components.add(comp);
            }
        }

        // 解析独立的 h1-h4 标签
        Matcher headingMatcher = Pattern.compile("<(h[1-4])[^>]*>([^<]+)</\\1>").matcher(body);
        // 只取不在 nav/section 内的（简化处理：跳过前面已解析的）

        // 解析 footer
        Matcher footerMatcher = Pattern.compile("(?s)<footer[^>]*>(.*?)</footer>").matcher(body);
        if (footerMatcher.find()) {
            String footerContent = footerMatcher.group(1);
            JSONObject comp = createComponent("comp_" + (++compIndex), "footer");
            JSONObject props = comp.getJSONObject("props");

            // 提取版权文字
            String copyright = extractFirstText(footerContent, "<p[^>]*>([^<]+)</p>");
            if (copyright == null) copyright = extractFirstText(footerContent, "<(?:div|span)[^>]*>([^<]*©[^<]*)</(?:div|span)>");
            if (copyright != null) props.set("text", copyright.trim());

            JSONArray links = extractLinks(footerContent);
            if (!links.isEmpty()) props.set("links", links);

            components.add(comp);
        }

        // 如果没有解析出任何组件，创建一个提示
        if (components.isEmpty()) {
            JSONObject comp = createComponent("comp_1", "paragraph");
            comp.getJSONObject("props").set("text", "当前页面结构较复杂，已加载为基础视图。你可以在此基础上添加和编辑组件。");
            components.add(comp);
        }
    }

    private JSONObject createComponent(String id, String type) {
        JSONObject comp = new JSONObject();
        comp.set("id", id);
        comp.set("type", type);
        comp.set("props", new JSONObject());
        comp.set("style", new JSONObject());
        return comp;
    }

    private JSONArray extractLinks(String content) {
        JSONArray links = new JSONArray();
        Matcher linkMatcher = Pattern.compile("<a[^>]*href=\"([^\"]+)\"[^>]*>([^<]+)</a>").matcher(content);
        while (linkMatcher.find()) {
            JSONObject link = new JSONObject();
            link.set("href", linkMatcher.group(1));
            link.set("text", linkMatcher.group(2).trim());
            links.add(link);
        }
        return links;
    }

    private String extractFirstText(String content, String regex) {
        Matcher m = Pattern.compile(regex).matcher(content);
        return m.find() ? m.group(1) : null;
    }

    private String extractTag(String html, String tag) {
        Matcher m = Pattern.compile("<" + tag + "[^>]*>([^<]+)</" + tag + ">").matcher(html);
        return m.find() ? m.group(1).trim() : null;
    }

    private String extractBodyContent(String html) {
        Matcher m = Pattern.compile("(?s)<body[^>]*>(.*)</body>").matcher(html);
        return m.find() ? m.group(1) : null;
    }

    private String extractPrimaryColor(String html) {
        Matcher m = Pattern.compile("--primary-color:\\s*([^;]+);").matcher(html);
        return m.find() ? m.group(1).trim() : "#1890ff";
    }
}
