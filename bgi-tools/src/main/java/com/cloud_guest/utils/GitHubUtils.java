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
import java.util.ArrayList;
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
     * 获取仓库所有workflows列表
     *
     * @param owner 仓库所有者
     * @param repo  仓库名
     * @param token 可选，公开仓库传null/空字符串
     * @return workflows JSON数组
     */
    @SneakyThrows
    public static JSONArray getWorkflowList(String owner, String repo, String token) {
        String url = String.format("%s/%s/%s/actions/workflows", GITHUB_API_BASE, owner, repo);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json");

        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        Request request = requestBuilder.build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API请求失败，响应码：" + response.code());
            }
            String body = response.body().string();
            JSONObject root = JSONUtil.toBean(body, JSONObject.class);
            return root.getJSONArray("workflows");
        }
    }

    /**
     * 获取指定workflow的runs列表
     *
     * @param owner           仓库所有者
     * @param repo            仓库名
     * @param workflowYmlName workflow文件名，如 main.yml
     * @param token           可选，公开仓库传null/空字符串
     * @param page            页码，从1开始
     * @param perPage         每页条数，最大100
     * @return workflow_runs JSON数组
     */
    @SneakyThrows
    public static JSONArray getWorkflowRunList(String owner, String repo, String workflowYmlName, String token, int page, int perPage) {
        String url = String.format("%s/%s/%s/actions/workflows/%s/runs?page=%d&per_page=%d",
                GITHUB_API_BASE, owner, repo, workflowYmlName, page, perPage);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json");

        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        Request request = requestBuilder.build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API请求失败，响应码：" + response.code());
            }
            String body = response.body().string();
            JSONObject root = JSONUtil.toBean(body, JSONObject.class);
            return root.getJSONArray("workflow_runs");
        }
    }

    /**
     * 获取指定run的artifacts列表
     *
     * @param owner 仓库所有者
     * @param repo  仓库名
     * @param runId workflow run的ID
     * @param token 可选，公开仓库传null/空字符串
     * @return artifacts JSON数组
     */
    @SneakyThrows
    public static JSONArray getRunArtifacts(String owner, String repo, long runId, String token) {
        String url = String.format("%s/%s/%s/actions/runs/%d/artifacts", GITHUB_API_BASE, owner, repo, runId);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json");

        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        Request request = requestBuilder.build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API请求失败，响应码：" + response.code());
            }
            String body = response.body().string();
            JSONObject root = JSONUtil.toBean(body, JSONObject.class);
            return root.getJSONArray("artifacts");
        }
    }

    /**
     * 获取artifact的下载链接（302重定向到实际下载地址）
     *
     * @param owner      仓库所有者
     * @param repo       仓库名
     * @param artifactId artifact的ID
     * @param token      可选，公开仓库传null/空字符串
     * @return 实际下载地址
     */
    @SneakyThrows
    public static String getArtifactDownloadUrl(String owner, String repo, long artifactId, String token) {
        String url = String.format("%s/%s/%s/actions/artifacts/%d/zip", GITHUB_API_BASE, owner, repo, artifactId);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json");

        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "token " + token);
        }

        Request request = requestBuilder.build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API请求失败，响应码：" + response.code());
            }
            return response.header("Location");
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
     * 获取 GitHub 仓库所有 Release 信息
     *
     * @param owner    仓库所属用户名/组织名
     * @param repo     仓库名称
     * @param proxyApi API 代理前缀
     * @param headers  请求头
     * @return 所有 Release 的 JSONArray
     */
    @SneakyThrows
    public static JSONArray getAllGitHubReleases(String owner, String repo, String proxyApi, Map<String, String> headers) {
        String apiUrl = String.format("%s%s/%s/%s/releases", proxyApi, GITHUB_API_BASE, owner, repo);
        Request.Builder builder = new Request.Builder()
                .url(apiUrl);
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
            return JSONUtil.parseArray(body);
        }
    }

    public static List<GitHubTag> getGitHubTags(String owner, String repo, String fileProxyApi, String tagProxyApi, Map<String, String> headers) {
        List<GitHubTag> list = CollUtil.newArrayList();
        JSONArray hubReleases = getAllGitHubReleases(owner, repo, tagProxyApi, headers);
        for (Object hubRelease : hubReleases) {
            JSONObject release = (JSONObject) hubRelease;
            GitHubTag gitHubTag = getGitHubFilesFromRelease(release, fileProxyApi, tagProxyApi, StrUtil.EMPTY);
            list.add(gitHubTag);
        }
        return list;
    }

    public static GitHubTag getGitHubFilesFromRelease(JSONObject release, String fileProxyApi, String proxyApi,String dockerImage) {
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
            GitHubFile hubFile = new GitHubFile(fileName, fileProxyApi, String.format("%s", size), downloadUrl,
                    new StringBuffer()
                            .append(fileProxyApi)
                            .append(downloadUrl.replaceFirst("https://", ""))
                            .toString()
            );
            list.add(
                    hubFile
            );
        }
        GitHubTag gitHubTag = new GitHubTag(tagName, dockerImage, proxyApi, list);
        return gitHubTag;
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
        JSONObject release = getLatestGitHubRelease(owner, repo, tagProxyApi, headers);
        // 创建GitHub文件列表
        String dockerImage = String.format("ghcr.io/%s/%s", owner.toLowerCase(), repo);
        GitHubTag gitHubTag = getGitHubFilesFromRelease(release, fileProxyApi,StrUtil.EMPTY,dockerImage);
        return gitHubTag;
    }

    public static GitHubTag getBgiToolsLatestGitHubTagInfo(Map<String, String> headers) {
        String fileProxyApi = "https://cdn.gh-proxy.org/";
        String tagProxyApi = StrUtil.EMPTY;
        String owner = "Kirito520Asuna";
        String repo = "bettergi-scripts-tools";
        return getLatestGitHubTag(owner, repo, fileProxyApi, tagProxyApi, headers);
    }

    public static GitHubTag get1RemoteLatestGitHubTagInfo(Map<String, String> headers) {
        String fileProxyApi = "https://cdn.gh-proxy.org/";
        String tagProxyApi = StrUtil.EMPTY;
        String owner = "1Remote";
        String repo = "1Remote";
        return getLatestGitHubTag(owner, repo, fileProxyApi, tagProxyApi, headers);
    }
    public static List<GitHubTag> get1RemoteAllGitHubTagInfo(Map<String, String> headers) {
        String fileProxyApi = "https://cdn.gh-proxy.org/";
        String tagProxyApi = StrUtil.EMPTY;
        String owner = "1Remote";
        String repo = "1Remote";
        return getGitHubTags(owner, repo, fileProxyApi, tagProxyApi, headers);
    }
    public static void main(String[] args) {
        //test1();
        List<GitHubTag> gitHubTags = get1RemoteAllGitHubTagInfo(null);
        System.out.println(gitHubTags);
    }

    private static void test1() {
        // 查询 Kirito520Asuna/better-genshin-impact 的 publish.yml 的 workflow runs 列表
/*        JSONArray runs = GitHubUtils.getWorkflowList(
                "Kirito520Asuna",
                "bettergi-scripts-tools",
                null
        );*/
        String owner = "Kirito520Asuna";
        String repo = "bettergi-scripts-tools";
        JSONArray runs = GitHubUtils.getWorkflowRunList(
                owner,
                repo,
                "main.yml",
                null,
                1,
                30
        );
        //System.err.println(JSONUtil.toJsonStr(runs));


        JSONObject latestRun = runs.getJSONObject(0);
        long runId = latestRun.getLong("id");
        System.err.println("runId: " + runId);

        // 2. 获取该run的artifacts列表
        JSONArray artifacts = GitHubUtils.getRunArtifacts(
                owner,
                repo,
                runId,
                null
        );
        System.err.println(JSONUtil.toJsonStr(artifacts));
//https://productionresultssa3.blob.core.windows.net/actions-results/f5a65352-5672-4d88-9199-d8f22ab3bcdf/workflow-job-run-0a72f541-7f38-5c27-99f1-0c01a5d672a1/artifacts/4fa1250ac01d4b14155ef8ea9a9f4cffa54d66c5b2ab76638dc975f899f9f70f.zip?rscd=attachment%3B+filename%3D%22windows-standalone.zip%22&rsct=application%2Fzip&se=2026-08-04T05%3A12%3A06Z&sig=vkyW0jfHnuDRO1RrlOf8rU5eE5WW%2Btt7knZXwfsLw1E%3D&ske=2026-08-04T08%3A05%3A27Z&skoid=ca7593d4-ee42-46cd-af88-8b886a2f84eb&sks=b&skt=2026-08-04T04%3A05%3A27Z&sktid=398a6654-997b-47e9-b12b-9515b896b4de&skv=2025-11-05&sp=r&spr=https&sr=b&st=2026-08-04T05%3A02%3A01Z&sv=2025-11-05
        // 3. 获取某个artifact的下载链接
        JSONObject jsonObject = artifacts.getJSONObject(0);
        long artifactId = jsonObject.getLong("id");
        String downloadUrl = GitHubUtils.getArtifactDownloadUrl(
                owner,
                repo,
                artifactId,
                null
        );
        System.err.println("downloadUrl: " + downloadUrl);
    }
}
