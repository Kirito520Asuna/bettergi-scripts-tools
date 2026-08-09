package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.BatGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;

/**
 * @Author yan
 * @Date 2026/8/9 12:20:24
 * @Description
 */
@Tag(name = "生成模块")
@RestController
@RequestMapping({"/gen/", "/api/gen/", "/jwt/gen/"})
public class GeneratorController {

    /** 浏览文件夹，列出子目录 */
    @SysLog(flag = false,result = false)
    @Operation(summary = "浏览文件夹(列出子目录)")
    @GetMapping("browse")
    public Result<Map<String, Object>> browseDir(@RequestParam(required = false) String path) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> dirs = new ArrayList<>();

        if (path == null || path.isBlank()) {
            File[] roots = File.listRoots();
            for (File root : roots) {
                Map<String, String> m = new HashMap<>();
                m.put("name", root.getAbsolutePath());
                m.put("path", root.getAbsolutePath());
                dirs.add(m);
            }
            result.put("currentPath", "");
        } else {
            File dir = new File(path);
            result.put("currentPath", dir.getAbsolutePath());
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            Map<String, String> m = new HashMap<>();
                            m.put("name", f.getName());
                            m.put("path", f.getAbsolutePath());
                            dirs.add(m);
                        }
                    }
                }
            }
        }
        dirs.sort(Comparator.comparing(a -> a.get("name").toLowerCase()));
        result.put("dirs", dirs);
        return Result.ok(result);
    }


    @SysLog
    @Operation(summary = "预览自启动bat脚本内容")
    @GetMapping("bat/1Remote/preview")
    public Result<String> previewBat(@RequestParam String startDir,
                                     @RequestParam String startUlid,
                                     @RequestParam String title,
                                     @RequestParam String exeName,
                                     @RequestParam int seconds
    ) {
        return Result.ok(BatGenerator.generateStartBatContent(startDir, startUlid, title, exeName, seconds));
    }

}
