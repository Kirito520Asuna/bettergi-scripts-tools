package com.cloud_guest.controller;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.dto.JsonDto;
import com.cloud_guest.redis.service.RedisService;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.DataBackupRecoveryService;
import com.cloud_guest.utils.ModeUtil;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/3/15 21:26:41
 * @Description
 */
@Slf4j
@Tag(name = "数据备份与恢复")
@RestController
@RequestMapping(value = {"/jwt/data/"})
public class DataBackupRecoveryController {
    @Resource
    private DataBackupRecoveryService dataBackupRecoveryService;

    public static ByteArrayOutputStream downLoadFileMultiThread(HttpServletResponse response, String fileName, byte[] bytes) throws IOException {
        // 获取文件的总大小
        int fileSize = bytes.length;

        // 设置响应的文件类型为二进制流
        response.setContentType("application/octet-stream");
        // 设置响应头，告诉浏览器这是一个附件，提供下载
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
        // 设置文件总大小，方便浏览器展示下载进度
        response.setHeader("Content-Length", String.valueOf(fileSize));

        // 设置每个分片的大小为4MB
        int chunkSize = 4 * 1024 * 1024; // 每个分片4MB
        // 计算文件需要分成多少个块
        int numChunks = (int) Math.ceil((double) fileSize / chunkSize);

        // 创建线程池，最大线程数为8，避免线程过多导致性能问题
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(numChunks, 8));  // 根据需要动态调整线程池大小
        List<Future<byte[]>> futures = new ArrayList<>(numChunks);

        // 将每个文件分块的读取任务提交到线程池
        for (int i = 0; i < numChunks; i++) {
            final int chunkIndex = i;  // 当前块的索引
            futures.add(executor.submit(() -> {
                // 根据分块大小计算读取范围
                int start = chunkIndex * chunkSize;
                int end = Math.min(start + chunkSize, fileSize);
                byte[] buffer = new byte[end - start];
                System.arraycopy(bytes, start, buffer, 0, buffer.length);
                return buffer; // 返回当前块的数据
            }));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 将下载的分块按顺序写入到响应的输出流中
        try (OutputStream os = new BufferedOutputStream(response.getOutputStream())) {
            for (int i = 0; i < futures.size(); i++) {
                // 获取当前块的数据，并写入到响应输出流中
                byte[] chunk = futures.get(i).get(); // 使用future.get()等待当前块数据的完成
                os.write(chunk); // 写入当前分块数据
                out.write(chunk);
            }
            os.flush(); // 确保所有数据写入完成
            out.flush();

        } catch (InterruptedException | ExecutionException e) {
            // 捕获异常，日志记录和适当处理
            log.error("下载过程中出现异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            // 关闭线程池，释放资源
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        return out;
    }

    @SneakyThrows
    @SysLog
    @Operation(summary = "数据备份并下载")
    @GetMapping("backup/download")
    public void backup(HttpServletResponse response) {
        Map<String, Object> map = dataBackupRecoveryService.backup();
        // 将 Map 转换为 JSON 并写入响应
        String json = JSONUtil.toJsonStr(map);
        Environment bean = SpringUtil.getBean(Environment.class);
        String prefix = bean.getProperty("spring.application.name", "bgi-tools");

        // 可选：设置下载文件名
        String fileName = prefix + "_backup_" + System.currentTimeMillis() + ".json";
        downLoadFileMultiThread(response, fileName, json.getBytes(StandardCharsets.UTF_8));
    }

    @SysLog
    @Operation(summary = "数据恢复")
    @PostMapping("recovery/json")
    public Result<?> recoveryJson(@Validated @RequestBody JsonDto json) {
        Map<String, Object> map = JSONUtil.toBean(json.getJson(), Map.class);
        dataBackupRecoveryService.recovery(map);
        return Result.ok();
    }

    @SneakyThrows
    @SysLog
    @Operation(summary = "数据恢复")
    @PostMapping("recovery/file")
    public Result<?> recovery(@RequestPart MultipartFile file) {
        // 读取文件内容
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        Map<String, Object> map = JSONUtil.toBean(content, Map.class);
        dataBackupRecoveryService.recovery(map);
        return Result.ok();
    }
}
