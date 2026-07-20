package com.cloud_guest.entitys.records;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Author yan
 * @Date 2026/7/20 13:55:12
 * @Description
 */
@Schema(description = "GitHub文件")
public record GitHubFile(@Schema(description = "名称") String name,
                         @Schema(description = "代理") String proxyApi,
                         @Schema(description = "文件大小") String size,
                         @Schema(description = "原始下载地址") String downloadUrl,
                         @Schema(description = "代理下载地址") String proxyDownloadUrl) {
}