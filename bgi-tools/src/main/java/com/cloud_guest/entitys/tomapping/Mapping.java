package com.cloud_guest.entitys.tomapping;

import com.cloud_guest.entitys.domain.BetterGIScriptsListTreeNode;
import com.cloud_guest.utils.GitHubTreeFetcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/4/17 9:43:05
 * @Description
 */
public class Mapping {

    /**
     * 将 TreeNode 转换为 BetterGIScriptsListTreeNode
     * 该方法会递归转换节点及其所有子节点，并保留树形结构
     *
     * @param treeNode 要转换的 TreeNode 对象
     * @return 转换后的 BetterGIScriptsListTreeNode 对象
     */
    public static BetterGIScriptsListTreeNode convertToBetterGITreeNode(GitHubTreeFetcher.TreeNode treeNode) {
        if (treeNode == null) {
            return null;
        }

        BetterGIScriptsListTreeNode betterGITreeNode = new BetterGIScriptsListTreeNode();
        betterGITreeNode.setName(treeNode.getName());
        betterGITreeNode.setDirectory(treeNode.isDirectory());
        betterGITreeNode.setPath(treeNode.getFullPath());

        List<BetterGIScriptsListTreeNode> convertedChildren = new ArrayList<>();
        for (GitHubTreeFetcher.TreeNode child : treeNode.getChildren()) {
            BetterGIScriptsListTreeNode convertedChild = convertToBetterGITreeNode(child);
            if (convertedChild != null) {
                convertedChild.setParent(betterGITreeNode);
                convertedChildren.add(convertedChild);
            }
        }
        betterGITreeNode.setChildren(convertedChildren);

        return betterGITreeNode;
    }

    /**
     * 将 TreeNode 列表转换为 BetterGIScriptsListTreeNode 列表
     * 该方法会遍历所有根节点并递归转换其子节点
     *
     * @param treeNodes 要转换的 TreeNode 列表
     * @return 转换后的 BetterGIScriptsListTreeNode 列表
     */
    public static List<BetterGIScriptsListTreeNode> convertToBetterGITreeNodes(List<GitHubTreeFetcher.TreeNode> treeNodes) {
        if (treeNodes == null || treeNodes.isEmpty()) {
            return new ArrayList<>();
        }

        List<BetterGIScriptsListTreeNode> result = new ArrayList<>();
        for (GitHubTreeFetcher.TreeNode node : treeNodes) {
            BetterGIScriptsListTreeNode convertedNode = convertToBetterGITreeNode(node);
            if (convertedNode != null) {
                result.add(convertedNode);
            }
        }
        return result;
    }
}

