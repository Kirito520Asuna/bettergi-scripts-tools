package com.cloud_guest.entitys.records;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/7/20 13:55:12
 * @Description
 */
@Schema(description = "GitHubTag 信息")
public record GitHubTag(@Schema(description = "tag名称") String name,
                        @Schema(description = "Docker镜像名称") String dockerImage,
                        @Schema(description = "代理Api") String proxyApi,
                        @Schema(description = "文件集合") List<GitHubFile> gitHubFileList) {
}