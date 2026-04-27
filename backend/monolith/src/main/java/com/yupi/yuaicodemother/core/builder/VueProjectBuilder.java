package com.yupi.yuaicodemother.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * 构建 Vue 项目
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 构建结果，包含是否成功和错误信息
     */
    @Data
    public static class BuildResult {
        private boolean success;
        private String errorLog;

        public static BuildResult success() {
            BuildResult result = new BuildResult();
            result.setSuccess(true);
            return result;
        }

        public static BuildResult failure(String errorLog) {
            BuildResult result = new BuildResult();
            result.setSuccess(false);
            result.setErrorLog(errorLog);
            return result;
        }
    }

    /**
     * 异步构建 Vue 项目
     *
     * @param projectPath
     */
    public void buildProjectAsync(String projectPath) {
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis())
                .start(() -> {
                    try {
                        buildProject(projectPath);
                    } catch (Exception e) {
                        log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
                    }
                });
    }

    /**
     * 构建 Vue 项目
     *
     * @param projectPath 项目根目录路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        return buildProjectWithResult(projectPath).isSuccess();
    }

    /**
     * 构建 Vue 项目，返回详细结果（包含错误日志）
     *
     * @param projectPath 项目根目录路径
     * @return 构建结果
     */
    public BuildResult buildProjectWithResult(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在：{}", projectPath);
            return BuildResult.failure("项目目录不存在：" + projectPath);
        }
        // 检查是否有 package.json 文件
        File packageJsonFile = new File(projectDir, "package.json");
        if (!packageJsonFile.exists()) {
            log.error("项目目录中没有 package.json 文件：{}", projectPath);
            return BuildResult.failure("项目目录中没有 package.json 文件");
        }
        log.info("开始构建 Vue 项目：{}", projectPath);
        // 执行 npm install
        CommandResult installResult = executeCommandWithOutput(projectDir,
                String.format("%s install", buildCommand("npm")), 300);
        if (!installResult.isSuccess()) {
            log.error("npm install 执行失败：{}", projectPath);
            return BuildResult.failure("npm install 失败:\n" + installResult.getErrorOutput());
        }
        // 执行 npm run build
        CommandResult buildResult = executeCommandWithOutput(projectDir,
                String.format("%s run build", buildCommand("npm")), 180);
        if (!buildResult.isSuccess()) {
            log.error("npm run build 执行失败：{}", projectPath);
            return BuildResult.failure("npm run build 失败:\n" + buildResult.getErrorOutput());
        }
        // 验证 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成但 dist 目录未生成：{}", projectPath);
            return BuildResult.failure("构建完成但 dist 目录未生成");
        }
        log.info("Vue 项目构建成功，dist 目录：{}", projectPath);
        return BuildResult.success();
    }

    /**
     * 根据操作系统构造命令
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 操作系统检测
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 命令执行结果
     */
    @Data
    private static class CommandResult {
        private boolean success;
        private String standardOutput;
        private String errorOutput;
    }

    /**
     * 执行命令并捕获输出
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 命令执行结果（包含标准输出和错误输出）
     */
    private CommandResult executeCommandWithOutput(File workingDir, String command, int timeoutSeconds) {
        CommandResult result = new CommandResult();
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+")
            );

            // 异步读取标准输出和错误输出，避免缓冲区满导致阻塞
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            Thread stdoutThread = Thread.ofVirtual().start(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdout.append(line).append("\n");
                    }
                } catch (IOException ignored) {
                }
            });

            Thread stderrThread = Thread.ofVirtual().start(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderr.append(line).append("\n");
                    }
                } catch (IOException ignored) {
                }
            });

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                result.setSuccess(false);
                result.setErrorOutput("命令执行超时（" + timeoutSeconds + "秒）");
                return result;
            }

            // 等待输出读取完成
            stdoutThread.join(5000);
            stderrThread.join(5000);

            int exitCode = process.exitValue();
            result.setStandardOutput(stdout.toString());
            result.setErrorOutput(stderr.toString());

            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                result.setSuccess(true);
            } else {
                log.error("命令执行失败，退出码: {}，错误输出:\n{}", exitCode, stderr);
                result.setSuccess(false);
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            result.setSuccess(false);
            result.setErrorOutput(e.getMessage());
        }
        return result;
    }
}
