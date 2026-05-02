package com.yupi.yuaicodemother.config;

import com.yupi.yuaicodemother.service.LowCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 低代码模块初始化配置
 * 应用启动时自动初始化内置组件模板
 */
@Slf4j
@Component
public class LowCodeInitConfig implements CommandLineRunner {

    @Resource
    private LowCodeService lowCodeService;

    @Override
    public void run(String... args) {
        try {
            lowCodeService.initBuiltinTemplates();
        } catch (Exception e) {
            log.warn("组件模板初始化失败（可能表还未创建）: {}", e.getMessage());
        }
    }
}
