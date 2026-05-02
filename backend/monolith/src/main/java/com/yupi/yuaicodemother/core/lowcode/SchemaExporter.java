package com.yupi.yuaicodemother.core.lowcode;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yuaicodemother.constant.AppConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Schema 导出器
 * 将页面 Schema JSON 转换为可部署的 HTML/CSS 静态文件
 */
@Slf4j
@Component
public class SchemaExporter {

    /**
     * 将 Schema 导出为静态文件
     *
     * @param schemaContent Schema JSON 字符串
     * @param appId         应用 ID
     * @return 导出的目录
     */
    public File exportToFiles(String schemaContent, Long appId) {
        return exportToFiles(schemaContent, appId, null);
    }

    /**
     * 将 Schema 导出为静态文件（指定目录前缀）
     *
     * @param schemaContent Schema JSON 字符串
     * @param appId         应用 ID
     * @param codeGenType   代码生成类型（用于确定目录名前缀）
     * @return 导出的目录
     */
    public File exportToFiles(String schemaContent, Long appId, String codeGenType) {
        JSONObject schema = JSONUtil.parseObj(schemaContent);
        // 使用应用原有的目录名，这样预览 iframe 能直接访问
        String prefix = (codeGenType != null && !codeGenType.isEmpty()) ? codeGenType : "lowcode";
        String dirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + prefix + "_" + appId;
        FileUtil.mkdir(dirPath);

        // 获取全局样式
        JSONObject globalStyle = schema.getJSONObject("globalStyle");
        String fontFamily = globalStyle != null ? globalStyle.getStr("fontFamily", "system-ui, -apple-system, sans-serif") : "system-ui, -apple-system, sans-serif";
        String primaryColor = globalStyle != null ? globalStyle.getStr("primaryColor", "#1890ff") : "#1890ff";

        // 获取页面列表
        JSONArray pages = schema.getJSONArray("pages");
        if (pages == null || pages.isEmpty()) {
            // 生成空白页面
            String html = generateEmptyHtml(fontFamily, primaryColor);
            FileUtil.writeString(html, dirPath + File.separator + "index.html", StandardCharsets.UTF_8);
            return new File(dirPath);
        }

        // 遍历页面生成 HTML
        for (int i = 0; i < pages.size(); i++) {
            JSONObject page = pages.getJSONObject(i);
            String pageName = i == 0 ? "index.html" : page.getStr("name", "page_" + i) + ".html";
            String html = generatePageHtml(page, fontFamily, primaryColor);
            FileUtil.writeString(html, dirPath + File.separator + pageName, StandardCharsets.UTF_8);
        }

        log.info("Schema 导出完成，目录：{}", dirPath);
        return new File(dirPath);
    }

    /**
     * 生成单个页面的完整 HTML
     */
    private String generatePageHtml(JSONObject page, String fontFamily, String primaryColor) {
        String pageName = page.getStr("name", "页面");
        JSONArray components = page.getJSONArray("components");

        StringBuilder bodyBuilder = new StringBuilder();
        StringBuilder styleBuilder = new StringBuilder();

        if (components != null) {
            for (int i = 0; i < components.size(); i++) {
                JSONObject comp = components.getJSONObject(i);
                renderComponent(comp, bodyBuilder, styleBuilder);
            }
        }

        return String.format("""
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        :root {
                            --primary-color: %s;
                            --font-family: %s;
                        }
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: var(--font-family);
                            line-height: 1.6;
                            color: #333;
                        }
                        img {
                            max-width: 100%%;
                            height: auto;
                        }
                        a {
                            color: var(--primary-color);
                            text-decoration: none;
                        }
                        a:hover {
                            opacity: 0.8;
                        }
                        %s
                    </style>
                </head>
                <body>
                %s
                </body>
                </html>
                """, pageName, primaryColor, fontFamily, styleBuilder.toString(), bodyBuilder.toString());
    }

    /**
     * 递归渲染组件
     */
    private void renderComponent(JSONObject comp, StringBuilder bodyBuilder, StringBuilder styleBuilder) {
        String type = comp.getStr("type");
        String id = comp.getStr("id", "comp_" + System.nanoTime());
        JSONObject props = comp.getJSONObject("props");
        JSONObject style = comp.getJSONObject("style");
        JSONArray children = comp.getJSONArray("children");

        if (props == null) props = new JSONObject();

        // 生成组件样式
        if (style != null && !style.isEmpty()) {
            styleBuilder.append(String.format("#%s { %s }\n", id, jsonStyleToCss(style)));
        }

        // 根据组件类型生成 HTML
        String html = switch (type) {
            case "navbar" -> renderNavbar(id, props, styleBuilder);
            case "hero" -> renderHero(id, props, styleBuilder);
            case "heading" -> renderHeading(id, props);
            case "paragraph" -> renderParagraph(id, props);
            case "image" -> renderImage(id, props);
            case "button" -> renderButton(id, props, styleBuilder);
            case "divider" -> renderDivider(id);
            case "container" -> renderContainer(id, props, children, styleBuilder);
            case "columns" -> renderColumns(id, props, children, styleBuilder);
            case "card" -> renderCard(id, props, styleBuilder);
            case "feature-grid" -> renderFeatureGrid(id, props, styleBuilder);
            case "carousel" -> renderCarousel(id, props, styleBuilder);
            case "footer" -> renderFooter(id, props, styleBuilder);
            default -> String.format("<div id=\"%s\"><!-- 未知组件: %s --></div>", id, type);
        };

        bodyBuilder.append(html).append("\n");

        // 渲染子组件（container 和 columns 内部已处理）
    }

    private String renderNavbar(String id, JSONObject props, StringBuilder styleBuilder) {
        String title = props.getStr("title", "网站标题");
        JSONArray links = props.getJSONArray("links");
        boolean sticky = props.getBool("sticky", false);

        StringBuilder linksHtml = new StringBuilder();
        if (links != null) {
            for (int i = 0; i < links.size(); i++) {
                JSONObject link = links.getJSONObject(i);
                linksHtml.append(String.format("<a href=\"%s\">%s</a>",
                        link.getStr("href", "#"), link.getStr("text", "链接")));
            }
        }

        styleBuilder.append(String.format("""
                #%s { display: flex; align-items: center; justify-content: space-between; padding: 0 24px; height: 64px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.06); %s }
                #%s .nav-title { font-size: 20px; font-weight: 700; color: var(--primary-color); }
                #%s .nav-links { display: flex; gap: 24px; }
                #%s .nav-links a { color: #555; font-size: 15px; transition: color 0.2s; }
                #%s .nav-links a:hover { color: var(--primary-color); }
                """, id, sticky ? "position: sticky; top: 0; z-index: 1000;" : "", id, id, id, id));

        return String.format("""
                <nav id="%s">
                    <div class="nav-title">%s</div>
                    <div class="nav-links">%s</div>
                </nav>
                """, id, title, linksHtml.toString());
    }

    private String renderHero(String id, JSONObject props, StringBuilder styleBuilder) {
        String title = props.getStr("title", "欢迎");
        String subtitle = props.getStr("subtitle", "");
        String bgImage = props.getStr("backgroundImage", "https://picsum.photos/1920/600");
        String buttonText = props.getStr("buttonText", "");
        String buttonLink = props.getStr("buttonLink", "#");

        styleBuilder.append(String.format("""
                #%s { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 500px; background: linear-gradient(rgba(0,0,0,0.4), rgba(0,0,0,0.4)), url('%s') center/cover no-repeat; color: #fff; text-align: center; padding: 48px 24px; }
                #%s h1 { font-size: 48px; font-weight: 700; margin-bottom: 16px; }
                #%s p { font-size: 20px; opacity: 0.9; margin-bottom: 32px; max-width: 600px; }
                #%s .hero-btn { display: inline-block; padding: 14px 36px; background: var(--primary-color); color: #fff; border-radius: 6px; font-size: 16px; font-weight: 500; transition: transform 0.2s; }
                #%s .hero-btn:hover { transform: translateY(-2px); }
                """, id, bgImage, id, id, id, id));

        String buttonHtml = buttonText.isEmpty() ? "" :
                String.format("<a href=\"%s\" class=\"hero-btn\">%s</a>", buttonLink, buttonText);

        return String.format("""
                <section id="%s">
                    <h1>%s</h1>
                    <p>%s</p>
                    %s
                </section>
                """, id, title, subtitle, buttonHtml);
    }

    private String renderHeading(String id, JSONObject props) {
        String text = props.getStr("text", "标题");
        int level = props.getInt("level", 2);
        level = Math.max(1, Math.min(6, level));
        return String.format("<h%d id=\"%s\">%s</h%d>", level, id, text, level);
    }

    private String renderParagraph(String id, JSONObject props) {
        String text = props.getStr("text", "段落文本内容");
        return String.format("<p id=\"%s\">%s</p>", id, text);
    }

    private String renderImage(String id, JSONObject props) {
        String src = props.getStr("src", "https://picsum.photos/800/400");
        String alt = props.getStr("alt", "图片");
        return String.format("<img id=\"%s\" src=\"%s\" alt=\"%s\">", id, src, alt);
    }

    private String renderButton(String id, JSONObject props, StringBuilder styleBuilder) {
        String text = props.getStr("text", "按钮");
        String href = props.getStr("href", "#");
        String variant = props.getStr("variant", "primary");

        String bgColor = "primary".equals(variant) ? "var(--primary-color)" : "transparent";
        String textColor = "primary".equals(variant) ? "#fff" : "var(--primary-color)";
        String border = "primary".equals(variant) ? "none" : "2px solid var(--primary-color)";

        styleBuilder.append(String.format("""
                #%s { display: inline-block; padding: 12px 28px; background: %s; color: %s; border: %s; border-radius: 6px; font-size: 15px; font-weight: 500; cursor: pointer; transition: opacity 0.2s; }
                #%s:hover { opacity: 0.85; }
                """, id, bgColor, textColor, border, id));

        return String.format("<a id=\"%s\" href=\"%s\">%s</a>", id, href, text);
    }

    private String renderDivider(String id) {
        return String.format("<hr id=\"%s\" style=\"border: none; border-top: 1px solid #e8e8e8; margin: 24px 0;\">", id);
    }

    private String renderContainer(String id, JSONObject props, JSONArray children, StringBuilder styleBuilder) {
        String layout = props.getStr("layout", "flex");
        int columns = props.getInt("columns", 1);
        String gap = props.getStr("gap", "16px");

        String layoutCss;
        if ("grid".equals(layout)) {
            layoutCss = String.format("display: grid; grid-template-columns: repeat(%d, 1fr); gap: %s;", columns, gap);
        } else {
            layoutCss = String.format("display: flex; flex-direction: column; gap: %s;", gap);
        }

        styleBuilder.append(String.format("#%s { %s padding: 24px; max-width: 1200px; margin: 0 auto; }\n", id, layoutCss));

        StringBuilder childrenHtml = new StringBuilder();
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                JSONObject child = children.getJSONObject(i);
                StringBuilder childBody = new StringBuilder();
                renderComponent(child, childBody, styleBuilder);
                childrenHtml.append(childBody);
            }
        }

        return String.format("<div id=\"%s\">\n%s</div>", id, childrenHtml.toString());
    }

    private String renderColumns(String id, JSONObject props, JSONArray children, StringBuilder styleBuilder) {
        int columns = props.getInt("columns", 2);
        String gap = props.getStr("gap", "24px");

        styleBuilder.append(String.format("""
                #%s { display: grid; grid-template-columns: repeat(%d, 1fr); gap: %s; padding: 24px; max-width: 1200px; margin: 0 auto; }
                @media (max-width: 768px) { #%s { grid-template-columns: 1fr; } }
                """, id, columns, gap, id));

        StringBuilder childrenHtml = new StringBuilder();
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                JSONObject child = children.getJSONObject(i);
                StringBuilder childBody = new StringBuilder();
                renderComponent(child, childBody, styleBuilder);
                childrenHtml.append(childBody);
            }
        }

        return String.format("<div id=\"%s\">\n%s</div>", id, childrenHtml.toString());
    }

    private String renderCard(String id, JSONObject props, StringBuilder styleBuilder) {
        String title = props.getStr("title", "卡片标题");
        String description = props.getStr("description", "卡片描述内容");
        String image = props.getStr("image", "https://picsum.photos/400/250");

        styleBuilder.append(String.format("""
                #%s { border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08); transition: transform 0.2s, box-shadow 0.2s; background: #fff; }
                #%s:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
                #%s img { width: 100%%; height: 200px; object-fit: cover; }
                #%s .card-body { padding: 20px; }
                #%s .card-body h3 { font-size: 18px; margin-bottom: 8px; color: #1a1a1a; }
                #%s .card-body p { font-size: 14px; color: #666; line-height: 1.6; }
                """, id, id, id, id, id, id));

        return String.format("""
                <div id="%s">
                    <img src="%s" alt="%s">
                    <div class="card-body">
                        <h3>%s</h3>
                        <p>%s</p>
                    </div>
                </div>
                """, id, image, title, title, description);
    }

    private String renderFeatureGrid(String id, JSONObject props, StringBuilder styleBuilder) {
        JSONArray features = props.getJSONArray("features");
        int columns = props.getInt("columns", 3);

        styleBuilder.append(String.format("""
                #%s { display: grid; grid-template-columns: repeat(%d, 1fr); gap: 32px; padding: 64px 24px; max-width: 1200px; margin: 0 auto; }
                #%s .feature-item { text-align: center; padding: 24px; }
                #%s .feature-item .feature-icon { font-size: 48px; margin-bottom: 16px; }
                #%s .feature-item h3 { font-size: 20px; margin-bottom: 12px; color: #1a1a1a; }
                #%s .feature-item p { font-size: 15px; color: #666; line-height: 1.6; }
                @media (max-width: 768px) { #%s { grid-template-columns: 1fr; } }
                """, id, columns, id, id, id, id, id));

        StringBuilder featuresHtml = new StringBuilder();
        if (features != null) {
            for (int i = 0; i < features.size(); i++) {
                JSONObject feature = features.getJSONObject(i);
                featuresHtml.append(String.format("""
                        <div class="feature-item">
                            <div class="feature-icon">%s</div>
                            <h3>%s</h3>
                            <p>%s</p>
                        </div>
                        """,
                        feature.getStr("icon", "⭐"),
                        feature.getStr("title", "特性"),
                        feature.getStr("description", "特性描述")));
            }
        }

        return String.format("<div id=\"%s\">\n%s</div>", id, featuresHtml.toString());
    }

    private String renderCarousel(String id, JSONObject props, StringBuilder styleBuilder) {
        JSONArray images = props.getJSONArray("images");
        String height = props.getStr("height", "400px");

        styleBuilder.append(String.format("""
                #%s { position: relative; overflow: hidden; height: %s; }
                #%s .carousel-track { display: flex; transition: transform 0.5s ease; height: 100%%; }
                #%s .carousel-slide { min-width: 100%%; height: 100%%; }
                #%s .carousel-slide img { width: 100%%; height: 100%%; object-fit: cover; }
                #%s .carousel-btn { position: absolute; top: 50%%; transform: translateY(-50%%); background: rgba(255,255,255,0.8); border: none; width: 40px; height: 40px; border-radius: 50%%; cursor: pointer; font-size: 18px; z-index: 10; }
                #%s .carousel-prev { left: 16px; }
                #%s .carousel-next { right: 16px; }
                """, id, height, id, id, id, id, id, id));

        StringBuilder slidesHtml = new StringBuilder();
        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                String src = images.getStr(i);
                slidesHtml.append(String.format("<div class=\"carousel-slide\"><img src=\"%s\" alt=\"轮播图 %d\"></div>\n", src, i + 1));
            }
        } else {
            // 默认 3 张图
            for (int i = 1; i <= 3; i++) {
                slidesHtml.append(String.format("<div class=\"carousel-slide\"><img src=\"https://picsum.photos/1200/400?random=%d\" alt=\"轮播图 %d\"></div>\n", i, i));
            }
        }

        return String.format("""
                <div id="%s">
                    <div class="carousel-track">
                        %s
                    </div>
                    <button class="carousel-btn carousel-prev" onclick="moveCarousel('%s', -1)">&#10094;</button>
                    <button class="carousel-btn carousel-next" onclick="moveCarousel('%s', 1)">&#10095;</button>
                </div>
                <script>
                (function() {
                    let currentSlide = 0;
                    window.moveCarousel = function(id, direction) {
                        const track = document.querySelector('#' + id + ' .carousel-track');
                        const slides = track.children.length;
                        currentSlide = (currentSlide + direction + slides) %% slides;
                        track.style.transform = 'translateX(-' + (currentSlide * 100) + '%%)';
                    };
                })();
                </script>
                """, id, slidesHtml.toString(), id, id);
    }

    private String renderFooter(String id, JSONObject props, StringBuilder styleBuilder) {
        String text = props.getStr("text", "© 2024 All Rights Reserved");
        JSONArray links = props.getJSONArray("links");

        styleBuilder.append(String.format("""
                #%s { background: #1a1a2e; color: #ccc; padding: 48px 24px; text-align: center; }
                #%s .footer-links { display: flex; justify-content: center; gap: 24px; margin-bottom: 16px; }
                #%s .footer-links a { color: #aaa; font-size: 14px; }
                #%s .footer-links a:hover { color: #fff; }
                #%s .footer-text { font-size: 13px; color: #888; }
                """, id, id, id, id, id));

        StringBuilder linksHtml = new StringBuilder();
        if (links != null) {
            for (int i = 0; i < links.size(); i++) {
                JSONObject link = links.getJSONObject(i);
                linksHtml.append(String.format("<a href=\"%s\">%s</a>",
                        link.getStr("href", "#"), link.getStr("text", "链接")));
            }
        }

        return String.format("""
                <footer id="%s">
                    <div class="footer-links">%s</div>
                    <div class="footer-text">%s</div>
                </footer>
                """, id, linksHtml.toString(), text);
    }

    /**
     * 生成空白页面
     */
    private String generateEmptyHtml(String fontFamily, String primaryColor) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>空白页面</title>
                    <style>
                        body { font-family: %s; display: flex; align-items: center; justify-content: center; min-height: 100vh; color: #999; }
                    </style>
                </head>
                <body>
                    <p>请在编辑器中添加组件</p>
                </body>
                </html>
                """, fontFamily);
    }

    /**
     * 将 JSON 样式对象转换为 CSS 字符串
     */
    private String jsonStyleToCss(JSONObject style) {
        StringJoiner joiner = new StringJoiner(" ");
        for (Map.Entry<String, Object> entry : style.entrySet()) {
            String key = camelToKebab(entry.getKey());
            joiner.add(key + ": " + entry.getValue() + ";");
        }
        return joiner.toString();
    }

    /**
     * 驼峰转短横线
     */
    private String camelToKebab(String camel) {
        return camel.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }
}
