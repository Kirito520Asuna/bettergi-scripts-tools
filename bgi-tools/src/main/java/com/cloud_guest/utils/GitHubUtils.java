package com.cloud_guest.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.entitys.records.GitHubFile;
import com.cloud_guest.entitys.records.GitHubTag;
import lombok.SneakyThrows;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yan
 * @Date 2026/7/20 13:51:13
 * @Description
 */
public class GitHubUtils {
    // 单例OkHttp客户端，复用连接
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final String GITHUB_API_BASE = "https://api.github.com/repos";

    /**
     * 获取指定yml workflow的最新一条run记录
     * @param owner 仓库所有者 Kirito520Asuna
     * @param repo 仓库名 bettergi-scripts-tools
     * @param workflowYmlName workflow文件名 main.yml
     * @param token 可选，公开仓库传null/空字符串即可
     * @return 最新run完整JSON对象，无记录返回null
     */
    @SneakyThrows
    public static JSONArray getLatestWorkflowRun(String owner,
                                                  String repo,
                                                  String workflowYmlName,
                                                  String token) {
        // 拼接接口：只取1条，自动是最新
        String url = String.format("%s/%s/%s/actions/workflows/%s/runs?per_page=1",
                GITHUB_API_BASE, owner, repo, workflowYmlName);

        // 构建请求
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json");

        // 有Token才添加鉴权头，无Token不添加（兼容公开仓库）
        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        Request request = requestBuilder.build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 404：yml不存在/仓库不存在；403：匿名限流耗尽
                throw new IOException("GitHub API请求失败，响应码：" + response.code());
            }

            String body = response.body().string();
            JSONObject root = JSONUtil.toBean(body, JSONObject.class);
            // workflow_runs 数组，无运行记录时为空数组
            JSONArray runList = root.getJSONArray("workflow_runs");
            return runList;
        }
    }
    
    /**
     * 获取 GitHub 仓库最新 Release 信息（使用 OkHttp）
     *
     * @param owner    仓库所属用户名/组织名
     * @param repo     仓库名称
     * @param proxyApi API 代理前缀
     * @param headers
     * @return 最新 Release 的 JSONObject
     */
    @SneakyThrows
    public static JSONObject getLatestGitHubRelease(String owner, String repo, String proxyApi, Map<String, String> headers) {
        String apiUrl = String.format("%s%s/%s/%s/releases/latest", proxyApi, GITHUB_API_BASE, owner, repo);
        Request.Builder builder = new Request.Builder()
                .url(apiUrl);
        //OkHttpClient client = new OkHttpClient();
        if (CollUtil.isEmpty(headers)) {
            headers = new HashMap<>();
        }
        String key = Header.USER_AGENT.getValue();
        String userAgent = headers.getOrDefault(key, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        builder.header(key, userAgent);
        Request request = builder.build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求 Github API 失败，响应码：" + response.code());
            }
            String body = response.body().string();
            return JSONUtil.toBean(body, JSONObject.class);
        }
    }

    /**
     * 获取GitHub仓库的最新标签信息
     *
     * @param owner        GitHub仓库所有者
     * @param repo         GitHub仓库名称
     * @param fileProxyApi 文件代理API地址
     * @param tagProxyApi  标签代理API地址
     * @param headers
     * @return GitHubTag 包含最新标签信息的对象
     * @throws RuntimeException 当owner或repo为空时抛出
     */
    public static GitHubTag getLatestGitHubTag(String owner, String repo, String fileProxyApi, String tagProxyApi, Map<String, String> headers) {
        // 检查owner和repo是否为空
        if (StrUtil.isBlankIfStr(owner) || StrUtil.isBlankIfStr(repo)) {
            throw new RuntimeException("owner or repo is blank");
        }
        // 获取最新的GitHub发布信息
        JSONObject release = getLatestGitHubRelease(owner, repo, tagProxyApi,headers);
        // 创建GitHub文件列表
        List<GitHubFile> list = CollUtil.newArrayList();
        // 获取标签名称
        String tagName = release.getByPath("tag_name", String.class);
        // 遍历所有可下载资产
        JSONArray assets = release.getJSONArray("assets");
        for (int i = 0; i < assets.size(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            // 获取文件名
            String fileName = asset.getByPath("name", String.class);
            // 获取文件大小
            Long size = asset.getByPath("size", Long.class);
            // 获取下载URL
            String downloadUrl = asset.getByPath("browser_download_url", String.class);
            // 添加GitHub文件到列表
            list.add(
                    new GitHubFile(fileName, fileProxyApi, String.format("%s", size), downloadUrl,
                            new StringBuffer()
                                    .append(fileProxyApi)
                                    .append(downloadUrl.replaceFirst("https://", ""))
                                    .toString()
                    )
            );
        }
        String dockerImage = String.format("ghcr.io/%s/%s", owner.toLowerCase(), repo);
        GitHubTag gitHubTag = new GitHubTag(tagName, dockerImage, StrUtil.EMPTY, list);
        return gitHubTag;
    }

    public static GitHubTag getBgiToolsLatestGitHubTagInfo(Map<String, String> headers) {
        String fileProxyApi = "https://cdn.gh-proxy.org/";
        String tagProxyApi = StrUtil.EMPTY;
        String owner = "Kirito520Asuna";
        String repo = "bettergi-scripts-tools";
        return getLatestGitHubTag(owner, repo, fileProxyApi, tagProxyApi,headers);
    }
}
