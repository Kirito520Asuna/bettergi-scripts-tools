package com.cloud_guest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/4/17 9:40:15
 * @Description
 */
@Data @Schema(description = "BetterGIScriptsListTreeNode")
@NoArgsConstructor
@AllArgsConstructor
public class BetterGIScriptsListTreeNode {
    @Schema(description = "节点名称")
    private String name;
    @Schema(description = "是否为目录")
    private boolean isDirectory;
    @Schema(description = "节点路径")
    private String path;
    @Schema(description = "父节点")
    private BetterGIScriptsListTreeNode parent;
    @Schema(description = "子节点")
    private List<BetterGIScriptsListTreeNode> children = new ArrayList<>();
}
