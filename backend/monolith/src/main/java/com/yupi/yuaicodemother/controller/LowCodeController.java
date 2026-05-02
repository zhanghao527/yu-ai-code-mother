package com.yupi.yuaicodemother.controller;

import com.yupi.yuaicodemother.common.BaseResponse;
import com.yupi.yuaicodemother.common.ResultUtils;
import com.yupi.yuaicodemother.exception.ErrorCode;
import com.yupi.yuaicodemother.exception.ThrowUtils;
import com.yupi.yuaicodemother.model.dto.lowcode.ComponentTemplateQueryRequest;
import com.yupi.yuaicodemother.model.dto.lowcode.SchemaExportRequest;
import com.yupi.yuaicodemother.model.dto.lowcode.SchemaSaveRequest;
import com.yupi.yuaicodemother.model.entity.User;
import com.yupi.yuaicodemother.model.vo.ComponentTemplateVO;
import com.yupi.yuaicodemother.model.vo.PageSchemaVO;
import com.yupi.yuaicodemother.service.LowCodeService;
import com.yupi.yuaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 低代码 控制层
 */
@RestController
@RequestMapping("/lowcode")
public class LowCodeController {

    @Resource
    private LowCodeService lowCodeService;

    @Resource
    private UserService userService;

    // ========== Schema 相关接口 ==========

    /**
     * 保存 Schema
     */
    @PostMapping("/schema/save")
    public BaseResponse<Integer> saveSchema(@RequestBody SchemaSaveRequest schemaSaveRequest,
                                            HttpServletRequest request) {
        ThrowUtils.throwIf(schemaSaveRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer version = lowCodeService.saveSchema(schemaSaveRequest, loginUser);
        return ResultUtils.success(version);
    }

    /**
     * 获取应用的最新 Schema（如果没有则从代码解析生成）
     */
    @GetMapping("/schema/get")
    public BaseResponse<PageSchemaVO> getLatestSchema(@RequestParam Long appId,
                                                      @RequestParam(required = false) String codeGenType,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        User loginUser = userService.getLoginUser(request);
        // 如果传了 codeGenType，尝试从代码解析
        if (codeGenType != null && !codeGenType.isEmpty()) {
            PageSchemaVO schemaVO = lowCodeService.parseAndSaveSchemaFromCode(appId, codeGenType, loginUser);
            return ResultUtils.success(schemaVO);
        }
        PageSchemaVO schemaVO = lowCodeService.getLatestSchema(appId, loginUser);
        return ResultUtils.success(schemaVO);
    }

    /**
     * 获取 Schema 版本历史
     */
    @GetMapping("/schema/history")
    public BaseResponse<List<PageSchemaVO>> getSchemaHistory(@RequestParam Long appId,
                                                             HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        User loginUser = userService.getLoginUser(request);
        List<PageSchemaVO> history = lowCodeService.getSchemaHistory(appId, loginUser);
        return ResultUtils.success(history);
    }

    /**
     * 回滚到指定版本
     */
    @PostMapping("/schema/rollback")
    public BaseResponse<Integer> rollbackSchema(@RequestParam Long appId,
                                                @RequestParam Integer version,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(version == null || version <= 0, ErrorCode.PARAMS_ERROR, "版本号无效");
        User loginUser = userService.getLoginUser(request);
        Integer newVersion = lowCodeService.rollbackSchema(appId, version, loginUser);
        return ResultUtils.success(newVersion);
    }

    /**
     * 导出 Schema 为代码
     */
    @PostMapping("/schema/export")
    public BaseResponse<String> exportSchema(@RequestBody SchemaExportRequest exportRequest,
                                             HttpServletRequest request) {
        ThrowUtils.throwIf(exportRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String path = lowCodeService.exportSchemaToCode(exportRequest.getAppId(), loginUser);
        return ResultUtils.success(path);
    }

    // ========== 组件模板相关接口 ==========

    /**
     * 保存低代码编辑后的 HTML（持久化到代码文件）
     */
    @PostMapping("/html/save")
    public BaseResponse<Boolean> saveHtml(@RequestBody com.yupi.yuaicodemother.model.dto.lowcode.HtmlSaveRequest htmlSaveRequest,
                                          HttpServletRequest request) {
        ThrowUtils.throwIf(htmlSaveRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        lowCodeService.saveHtmlToCodeFile(htmlSaveRequest.getAppId(), htmlSaveRequest.getHtmlContent(), loginUser);
        return ResultUtils.success(true);
    }

    // ========== 组件模板相关接口（原有） ==========

    /**
     * 获取组件模板列表
     */
    @GetMapping("/template/list")
    public BaseResponse<List<ComponentTemplateVO>> listTemplates(ComponentTemplateQueryRequest queryRequest) {
        List<ComponentTemplateVO> templates = lowCodeService.listComponentTemplates(queryRequest);
        return ResultUtils.success(templates);
    }

    /**
     * 获取单个组件模板详情
     */
    @GetMapping("/template/get")
    public BaseResponse<ComponentTemplateVO> getTemplate(@RequestParam String componentKey) {
        ComponentTemplateVO template = lowCodeService.getComponentTemplate(componentKey);
        return ResultUtils.success(template);
    }
}
