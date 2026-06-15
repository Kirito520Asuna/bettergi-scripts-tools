package com.cloud_guest.utils; /**
 * @Author yan
 * @Date 2026/4/17 6:10:04
 * @Description
 */

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.entitys.domain.BetterGIScriptsListTreeNode;
import com.cloud_guest.entitys.tomapping.Mapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GitHubTreeFetcher {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    public static Map<String,HashSet<String>> map_paths = new HashMap<>();

    /**
     * 创建带有代理配置的 OkHttpClient
     *
     * @param proxyConfig 代理配置对象，如果为null则返回默认客户端
     * @return 配置好代理的 OkHttpClient 实例
     */
    private static OkHttpClient createClientWithProxy(ProxyConfig proxyConfig) {
        if (proxyConfig == null || !proxyConfig.isEnabled()) {
            return client;
        }

        OkHttpClient.Builder builder = client.newBuilder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort()));
        builder.proxy(proxy);

        if (StrUtil.isNotBlank(proxyConfig.getUsername()) && StrUtil.isNotBlank(proxyConfig.getPassword())) {
            Authenticator proxyAuthenticator = new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(proxyConfig.getUsername(), proxyConfig.getPassword());
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                }
            };
            builder.proxyAuthenticator(proxyAuthenticator);
        }

        return builder.build();
    }

    private static final Map<String, PlatformConfig> PLATFORM_CONFIG_MAP = new HashMap<>();

    static {
        PLATFORM_CONFIG_MAP.put("github.com", new PlatformConfig(
                "https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1",
                "https://raw.githubusercontent.com/%s/%s/%s/%s",
                10, 4000,
                new HashMap<>(Map.of("Accept", "application/vnd.github.v3+json")),
                null
        ));
        // ✅ 修复这里：Gitee 正确配置
        PLATFORM_CONFIG_MAP.put("gitee.com", new PlatformConfig(
                "https://gitee.com/api/v5/repos/%s/%s/git/trees/%s?recursive=1",
                "https://gitee.com/%s/%s/raw/%s/%s",
                10, 4000,
                new HashMap<>(Map.of("Accept", "application/json")),
                null
        ));

        PLATFORM_CONFIG_MAP.put("gitlab.com", new PlatformConfig(
                "https://gitlab.com/api/v4/projects/%s%%2F%s/repository/tree?ref=%s&recursive=true",
                "https://gitlab.com/api/v4/projects/%s%%2F%s/repository/files/%s/raw?ref=%s",
                10, 4000,
                new HashMap<>(Map.of("PRIVATE-TOKEN", "")),
                null
        ));
    }

    private static final Pattern GIT_URL_PATTERN = Pattern.compile(
            """
                    (?:https?://([^/]+)/|git@([^:]+):)([^/]+)/([^/]+?)(?:\\.git)?(?:/tree/([^/#\\?]+))?$""");

    @SneakyThrows
    private static void test1() {
        String url = "https://github.com/babalae/bettergi-scripts-list";

        // 解析Git URL，获取仓库信息
        String[] repoInfo = parseGitUrl(url);
        // 从解析结果中提取平台信息
        String platform = repoInfo[0];
        // 从解析结果中提取仓库所有者信息
        String owner = repoInfo[1];
        // 从解析结果中提取仓库名称信息
        String repo = repoInfo[2];
        // 从解析结果中提取分支信息
        String branch = repoInfo[3];

        String content = fetchFileContent(platform, owner, repo, branch, """
                repo/pathing/地方特产/挪德卡莱/便携轴承/01-便携轴承-叮铃哐啷蛋卷红坊-9个.json
                """.trim());

        log.info(content);
    }

    @SneakyThrows
    private static void test2() {
        //String url = "https://github.com/babalae/bettergi-scripts-list";
        String url = "https://gitee.com/kirito-asuna/bettergi-scripts-list";


        String privateToken = "gitlab.com@PRIVATE-TOKEN=xxx";
        List<TreeNode> treeNodes = getGitTreeNodes(url, privateToken);
        // 打印树形节点，传入空字符串作为初始缩进
        //printTreeNode(treeNodes, "");
        log.info("==============");
        log.info("==============");
        log.info("==============");
        log.info("==============");
        String dir = "repo/pathing/";
        List<TreeNode> repoNode = filterByPath(treeNodes, List.of("icon.ico", "desktop.ini"), dir/*"repo/js", "repo/combat", "repo/pathing", "repo/tcg"*/);
        List<BetterGIScriptsListTreeNode> betterGIScriptsListTreeNodes = Mapping.convertToBetterGITreeNodes(repoNode);
        printTreeNode(repoNode, "");
        HashSet<String> value = new HashSet<>();
        //put url :list path
        collectFilePaths(repoNode, value);
        map_paths.put(url, value);
        log.info("map_paths:{}", map_paths);
    }
    private static void collectFilePaths(List<TreeNode> nodes, HashSet<String> paths) {
        for (TreeNode node : nodes) {
            if (!node.isDirectory) {
                paths.add(node.getFullPath());
            } else if (!node.children.isEmpty()) {
                collectFilePaths(node.children, paths);
            }
        }
    }
/*    public static void main(String[] args) throws IOException {
        //test1();
        test2();
    }*/


    /**
     * 根据给定的URL和可选的头部信息获取Git仓库的树形结构节点列表
     *
     * @param url           Git仓库的URL地址
     * @param headerConfigs 可变长度的头部参数，用于设置认证信息等
     * @return 包含树形结构节点的列表
     * @throws IOException 如果发生I/O错误
     */
    public static List<TreeNode> getGitTreeNodes(String url, String... headerConfigs) throws IOException {
        for (String headerConfig : headerConfigs) {
            if (StrUtil.isNotBlank(headerConfig)) {
                applyHeaderConfig(headerConfig);
            }
        }

        Map<String, List<String>> treeStructure = fetchGitTreeByUrl(url);

        // 构建树形结构的节点列表
        List<TreeNode> treeNodes = buildTree(treeStructure);
        return treeNodes;
    }

    /**
     * 应用头部配置方法
     * 该方法用于解析并应用平台特定的头部配置信息
     *
     * @param headerConfig 配置字符串，格式为"平台@键=值"
     * @throws IllegalArgumentException 当配置格式不正确或平台不支持时抛出
     */
    private static void applyHeaderConfig(String headerConfig) {
        // 使用"@"符号分割字符串，最多分成两部分
        String[] parts = headerConfig.split("@", 2);
        // 检查分割后的数组长度是否为2，否则抛出异常
        if (parts.length != 2) {
            throw new IllegalArgumentException("无效的头部配置格式: " + headerConfig);
        }

        // 第一部分作为平台标识
        String platform = parts[0];
        // 使用"="符号分割第二部分，最多分成两部分
        String[] kv = parts[1].split("=", 2);
        // 检查键值对格式是否正确
        if (kv.length != 2) {
            throw new IllegalArgumentException("无效的键值对格式: " + parts[1]);
        }

        // 从配置映射中获取平台配置
        PlatformConfig config = PLATFORM_CONFIG_MAP.get(platform);
        // 检查平台是否支持
        if (config == null) {
            throw new IllegalArgumentException("不支持的平台: " + platform);
        }

        // 将解析出的键值对添加到平台的头部配置中
        config.headers.put(kv[0], kv[1]);
    }

    /**
     * 根据Git仓库URL获取仓库的树结构
     *
     * @param url Git仓库的URL地址
     * @return 返回一个Map，键为文件路径，值为文件路径对应的字符串列表
     * @throws IOException 当发生I/O错误时抛出异常
     */
    public static Map<String, List<String>> fetchGitTreeByUrl(String url) throws IOException {
        // 解析Git URL，获取仓库信息
        String[] repoInfo = parseGitUrl(url);
        // 从解析结果中提取平台信息
        String platform = repoInfo[0];
        // 从解析结果中提取仓库所有者信息
        String owner = repoInfo[1];
        // 从解析结果中提取仓库名称信息
        String repo = repoInfo[2];
        // 从解析结果中提取分支信息
        String branch = repoInfo[3];
        // 打印正在获取仓库树结构的信息
        log.info("正在获取 " + platform + " 上的 " + owner + "/" + repo + " 的树结构...");
        // 调用fetchGitTree方法获取仓库树结构并返回
        return fetchGitTree(platform, owner, repo, branch);
    }

    /**
     * 根据平台、仓库所有者、仓库名、分支和文件路径获取文件内容
     *
     * @param platform 平台名称（如"github.com"、"gitlab.com"等）
     * @param owner    仓库所有者
     * @param repo     仓库名称
     * @param branch   分支名称
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException 当请求失败或不支持的平台时抛出异常
     */
    public static String fetchFileContent(String platform, String owner, String repo, String branch, String filePath) throws IOException {
        // 从平台配置映射中获取对应平台的配置
        PlatformConfig config = PLATFORM_CONFIG_MAP.get(platform);
        // 如果配置不存在，抛出不支持平台的异常
        if (config == null) {
            throw new IllegalArgumentException("不支持的平台: " + platform);
        }

        String url;
        // 如果是GitLab平台，需要对文件路径进行URL编码
        if ("gitlab.com".equals(platform)) {
            url = String.format(config.fileApiUrl, owner, repo, java.net.URLEncoder.encode(filePath, "UTF-8"), branch);
        } else {
            // 其他平台直接使用文件路径
            url = String.format(config.fileApiUrl, owner, repo, branch, filePath);
        }

        // 构建HTTP请求
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        // 添加请求头
        config.headers.forEach(requestBuilder::addHeader);

        OkHttpClient httpClient = client;
        if (config.getProxy() != null) {
            ProxyConfig proxy = config.getProxy();
            httpClient = createClientWithProxy(proxy);
        }

        // 构建最终的请求对象
        Request request = requestBuilder.build();

        // 执行请求并处理响应
        try (Response response = httpClient.newCall(request).execute()) {
            // 如果请求不成功，抛出异常
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }

            // 返回响应体内容
            return response.body().string();
        }
    }

    /**
     * 解析 Git 仓库链接，提取其中的平台、所有者、仓库名称和分支信息
     *
     * @param url Git 仓库链接，可以是 HTTPS 或 SSH 格式
     * @return 包含平台、所有者、仓库名称和分支信息的字符串数组
     * @throws IllegalArgumentException 当提供的链接格式无效时抛出
     */
    private static String[] parseGitUrl(String url) {
        // 使用预定义的正则表达式模式匹配 Git URL
        Matcher matcher = GIT_URL_PATTERN.matcher(url);
        // 检查是否找到匹配项
        if (matcher.find()) {
            // 提取 HTTPS 主机名（如果存在）
            String httpsHost = matcher.group(1);
            // 提取 SSH 主机名（如果存在）
            String sshHost = matcher.group(2);
            // 确定平台：优先使用 HTTPS 主机，否则使用 SSH 主机
            String platform = StrUtil.isNotBlank(httpsHost) ? httpsHost : sshHost;

            // 提取仓库所有者
            String owner = matcher.group(3);
            // 提取仓库名称
            String repo = matcher.group(4);
            // 提取分支名称，如果为空则使用默认分支
            String branch = StrUtil.isNotBlank(matcher.group(5)) ? matcher.group(5) : getDefaultBranch(platform);

            // 返回包含解析信息的数组
            return new String[]{platform, owner, repo, branch};
        }
        // 如果链接格式无效，抛出异常
        throw new IllegalArgumentException("无效的 Git 仓库链接: " + url);
    }

    /**
     * 根据平台获取默认分支名称
     *
     * @param platform 平台名称，如"gitee.com"等
     * @return 返回该平台的默认分支名称，gitee平台返回"master"，其他平台返回"main"
     */
    private static String getDefaultBranch(String platform) {
        // 判断平台是否为gitee.com
        if ("gitee.com".equals(platform)) {
            // 如果是gitee平台，返回master分支
            return "master";
        }
        // 其他情况返回main分支
        return "main";
    }

    /**
     * 获取Git仓库的目录结构
     *
     * @param platform 平台名称，如"github.com"、"gitlab.com"等
     * @param owner    仓库所有者
     * @param repo     仓库名称
     * @param branch   分支名称
     * @return 返回一个有序的Map，键为目录路径，值为该目录下的文件列表
     * @throws IOException 当请求失败或平台不支持时抛出异常
     */
    public static Map<String, List<String>> fetchGitTree(String platform, String owner, String repo, String branch) throws IOException {
        // 从配置映射中获取平台配置
        PlatformConfig config = PLATFORM_CONFIG_MAP.get(platform);
        // 如果配置不存在，抛出不支持平台的异常
        if (config == null) {
            throw new IllegalArgumentException("不支持的平台: " + platform);
        }

        // 构造API请求URL
        String url = String.format(config.treeApiUrl, owner, repo, branch);

        // 创建HTTP请求构建器
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get(); // 使用GET方法

        // 添加请求头
        config.headers.forEach(requestBuilder::addHeader);

        // 构建请求对象
        Request request = requestBuilder.build();

        OkHttpClient httpClient = client;
        if (config.getProxy() != null) {
            ProxyConfig proxy = config.getProxy();
            httpClient = createClientWithProxy(proxy);
        }

        int maxRetries = config.getMaxRetries();
        int retriesSleep = config.getRetriesSleep();
        int retryCount = 0;
        IOException lastException = null;

        while (retryCount < maxRetries) {
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if ("gitlab.com".equals(platform)) {
                        return parseGitLabTree(responseBody);
                    }

                    JSONObject jsonObject = JSONUtil.parseObj(responseBody);
                    JSONArray treeArray = jsonObject.getJSONArray("tree");

                    Map<String, List<String>> structure = new TreeMap<>();

                    for (int i = 0; i < treeArray.size(); i++) {
                        JSONObject item = treeArray.getJSONObject(i);
                        String path = item.getStr("path");
                        String type = item.getStr("type");

                        if ("blob".equals(type)) {
                            int lastSlashIndex = path.lastIndexOf('/');
                            String dir = lastSlashIndex == -1 ? "." : path.substring(0, lastSlashIndex);

                            structure.computeIfAbsent(dir, k -> new ArrayList<>()).add(path);
                        }
                    }

                    return structure;
                } else {
                    int responseCode = response.code();
                    if (responseCode == 504 || responseCode == 502 || responseCode == 503) {
                        retryCount++;
                        lastException = new IOException("请求失败: " + responseCode + "，第 " + retryCount + " 次重试");
                        log.info("遇到网关错误: " + responseCode + "，进行第 " + retryCount + " 次重试...");

                        if (retryCount < maxRetries) {
                            try {
                                Thread.sleep(retriesSleep * retryCount);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException("重试被中断", e);
                            }
                        }
                    } else {
                        throw new IOException("请求失败: " + responseCode);
                    }
                }
            } catch (IOException e) {
                retryCount++;
                lastException = e;
                if (retryCount >= maxRetries) {
                    throw new IOException("请求失败，已重试 " + maxRetries + " 次: " + e.getMessage(), e);
                }
                log.info("请求异常: " + e.getMessage() + "，进行第 " + retryCount + " 次重试...");

                try {
                    Thread.sleep(retriesSleep * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("重试被中断", ie);
                }
            }
        }

        throw new IOException("请求失败，已达到最大重试次数: " + maxRetries, lastException);


        ///
        // 执行请求并处理响应
        //try (Response response = client.newCall(request).execute()) {
        //    // 检查响应状态
        //    if (!response.isSuccessful()) {
        //        throw new IOException("请求失败: " + response.code());
        //    }
        //
        //    // 获取响应体内容
        //    String responseBody = response.body().string();
        //
        //    // 如果是GitLab平台，使用专门的解析方法
        //    if ("gitlab.com".equals(platform)) {
        //        return parseGitLabTree(responseBody);
        //    }
        //
        //    // 解析JSON响应
        //    JSONObject jsonObject = JSONUtil.parseObj(responseBody);
        //    JSONArray treeArray = jsonObject.getJSONArray("tree");
        //
        //    // 使用TreeMap保持目录有序
        //    Map<String, List<String>> structure = new TreeMap<>();
        //
        //    // 遍历仓库中的每个项目
        //    for (int i = 0; i < treeArray.size(); i++) {
        //        JSONObject item = treeArray.getJSONObject(i);
        //        String path = item.getStr("path");
        //        String type = item.getStr("type");
        //
        //        // 只处理文件类型的项目
        //        if ("blob".equals(type)) {
        //            // 获取文件所在的目录路径
        //            int lastSlashIndex = path.lastIndexOf('/');
        //            String dir = lastSlashIndex == -1 ? "." : path.substring(0, lastSlashIndex);
        //
        //            // 将文件添加到对应目录的列表中
        //            structure.computeIfAbsent(dir, k -> new ArrayList<>()).add(path);
        //        }
        //    }
        //
        //    return structure;
        //}
    }

    /**
     * 解析GitLab的树形结构响应，将其转换为目录和文件的映射关系
     *
     * @param responseBody GitLab API返回的响应体，包含文件和目录的树形结构信息
     * @return 返回一个TreeMap，键是目录路径，值是该目录下的所有文件路径列表
     */
    private static Map<String, List<String>> parseGitLabTree(String responseBody) {
        // 使用JSONUtil解析响应体为JSONArray
        JSONArray treeArray = JSONUtil.parseArray(responseBody);
        // 使用TreeMap来存储结果，会自动按路径排序
        Map<String, List<String>> structure = new TreeMap<>();

        // 遍历树形结构中的每个元素
        for (int i = 0; i < treeArray.size(); i++) {
            // 获取当前元素并解析其路径和类型
            JSONObject item = treeArray.getJSONObject(i);
            String path = item.getStr("path");
            String type = item.getStr("type");

            // 只处理文件类型（blob）的项
            if ("blob".equals(type)) {
                // 获取文件所在目录路径
                int lastSlashIndex = path.lastIndexOf('/');
                String dir = lastSlashIndex == -1 ? "." : path.substring(0, lastSlashIndex);

                // 将文件添加到对应目录的列表中
                structure.computeIfAbsent(dir, k -> new ArrayList<>()).add(path);
            }
        }

        return structure;
    }

    /**
     * 根据给定的结构构建树形结构的节点列表
     *
     * @param structure 包含目录和文件结构的映射表，键为目录路径，值为该目录下的文件列表
     * @return 返回构建好的树形结构的根节点列表
     */
    private static List<TreeNode> buildTree(Map<String, List<String>> structure) {
        Map<String, TreeNode> nodeMap = new TreeMap<>();
        TreeNode rootNode = new TreeNode("root", true);
        nodeMap.put("__root__", rootNode);

        for (Map.Entry<String, List<String>> entry : structure.entrySet()) {
            String dir = entry.getKey();
            List<String> files = entry.getValue();

            TreeNode currentDirNode;

            if (".".equals(dir)) {
                currentDirNode = rootNode;
            } else {
                String[] parts = dir.split("/");
                StringBuilder pathBuilder = new StringBuilder();

                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) pathBuilder.append("/");
                    pathBuilder.append(parts[i]);

                    String currentPath = pathBuilder.toString();
                    if (!nodeMap.containsKey(currentPath)) {
                        TreeNode parentNode = i > 0 ? nodeMap.get(currentPath.substring(0, currentPath.lastIndexOf('/'))) : rootNode;
                        TreeNode newNode = new TreeNode(parts[i], true, parentNode);
                        nodeMap.put(currentPath, newNode);

                        if (parentNode != null) {
                            parentNode.children.add(newNode);
                        }
                    }
                }
                currentDirNode = nodeMap.get(dir);
            }

            for (String filePath : files) {
                String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
                TreeNode fileNode = new TreeNode(fileName, false, currentDirNode);
                currentDirNode.children.add(fileNode);
            }
        }

        return rootNode.children;
    }


    /**
     * 根据路径模式过滤树节点列表
     *
     * @param nodes        树节点列表
     * @param pathPatterns 路径模式，使用"/"作为分隔符
     * @return 匹配路径模式的树节点列表
     */
    //public static List<TreeNode> filterByPath(List<TreeNode> nodes, String pathPattern) {
    //    // 创建结果列表，用于存储匹配的节点
    //    List<TreeNode> result = new ArrayList<>();
    //    // 将路径模式按"/"分割成路径部分数组
    //    String[] pathParts = pathPattern.split("/");
    //
    //    // 遍历所有树节点
    //    for (TreeNode node : nodes) {
    //        // 检查当前节点是否匹配路径模式
    //        if (matchPath(node, pathParts, 0)) {
    //            // 如果匹配，则克隆该节点及其路径，并添加到结果列表
    //            result.add(cloneNodeWithPath(node, pathParts, 0));
    //        }
    //    }
    //    // 返回匹配的节点列表
    //    return result;
    //}
    public static List<TreeNode> filterByPath(List<TreeNode> nodes, String... pathPatterns) {
        return filterByPath(nodes, null, pathPatterns);
    }

    /**
     * 根据路径模式过滤树节点列表，支持排除特定模式
     * 该方法会遍历所有路径模式，匹配对应的节点并克隆其路径结构，
     * 同时可以选择性地排除符合特定模式的节点
     *
     * @param nodes           待过滤的树节点列表
     * @param excludePatterns 需要排除的路径模式列表，如果为null或空则不排除任何节点
     * @param pathPatterns    可变长度的路径模式数组，使用"/"作为路径分隔符
     * @return 匹配路径模式且未被排除的树节点列表
     */
    public static List<TreeNode> filterByPath(List<TreeNode> nodes, List<String> excludePatterns, String... pathPatterns) {
        List<TreeNode> result = new ArrayList<>();
        for (String pathPattern : pathPatterns) {
            String[] pathParts = pathPattern.split("/");

            for (TreeNode node : nodes) {
                if (matchPath(node, pathParts, 0)) {
                    TreeNode clonedNode = cloneNodeWithPath(node, pathParts, 0);
                    if (excludePatterns != null && !excludePatterns.isEmpty()) {
                        clonedNode = excludeNodes(clonedNode, excludePatterns);
                        if (clonedNode != null) {
                            result.add(clonedNode);
                        }
                    } else {
                        result.add(clonedNode);
                    }
                }
            }
        }
        return result;
    }


    /**
     * 递归排除节点及其子节点中匹配排除模式的部分
     * 该方法会检查当前节点是否匹配任何排除模式，如果匹配则返回null；
     * 对于目录节点，会递归处理所有子节点并过滤掉被排除的子节点
     *
     * @param node            待处理的树节点
     * @param excludePatterns 排除模式列表，每个模式使用"/"作为路径分隔符
     * @return 如果节点被排除则返回null，否则返回处理后的节点（可能已移除被排除的子节点）
     */
    private static TreeNode excludeNodes(TreeNode node, List<String> excludePatterns) {
        if (node == null) {
            return null;
        }

        for (String pattern : excludePatterns) {
            if (isExcluded(node, pattern)) {
                return null;
            }
        }

        if (node.isDirectory) {
            List<TreeNode> filteredChildren = new ArrayList<>();
            for (TreeNode child : node.children) {
                TreeNode excludedChild = excludeNodes(child, excludePatterns);
                if (excludedChild != null) {
                    filteredChildren.add(excludedChild);
                }
            }
            node.children = filteredChildren;
        }

        return node;
    }


    /**
     * 判断节点是否应该被排除
     * 该方法将排除模式按"/"分割后，调用matchExcludePath进行路径匹配
     *
     * @param node           待判断的树节点
     * @param excludePattern 排除模式字符串，使用"/"作为路径分隔符
     * @return 如果节点匹配排除模式返回true，否则返回false
     */
    private static boolean isExcluded(TreeNode node, String excludePattern) {
        String[] excludeParts = excludePattern.split("/");
        return matchExcludePath(node, excludeParts, 0);
    }


    /**
     * 递归匹配排除路径的方法，用于检查给定的排除路径是否与当前节点匹配
     * 该方法支持通配符(*)和双星号(**)的路径匹配模式，用于过滤需要排除的文件或目录
     *
     * @param node         当前树节点，表示待匹配的文件或目录节点
     * @param excludeParts 排除路径部分的字符串数组，按"/"分隔后的路径片段
     * @param currentIndex 当前正在匹配的排除路径部分的索引位置
     * @return 如果排除路径匹配成功返回true，否则返回false
     */
    private static boolean matchExcludePath(TreeNode node, String[] excludeParts, int currentIndex) {
        // 边界检查：如果当前索引超出排除路径部分数组的长度，返回false
        if (currentIndex >= excludeParts.length) {
            return false;
        }

        // 获取当前要匹配的排除路径部分
        String currentPart = excludeParts[currentIndex];

        // 处理通配符的情况：单星号(*)匹配单层，双星号(**)匹配多层
        if ("*".equals(currentPart) || "**".equals(currentPart)) {
            // 如果是排除路径的最后一部分，说明可以匹配任意内容，返回true
            if (currentIndex == excludeParts.length - 1) {
                return true;
            }

            // 如果当前节点是目录，则遍历子节点进行递归匹配
            if (node.isDirectory) {
                for (TreeNode child : node.children) {
                    // 尝试用下一个路径部分与子节点进行匹配
                    if (matchExcludePath(child, excludeParts, currentIndex + 1)) {
                        return true;
                    }
                    // 如果是双星号且子节点是目录，则保持在当前索引继续匹配（支持跨级匹配）
                    if ("**".equals(currentPart) && child.isDirectory && matchExcludePath(child, excludeParts, currentIndex)) {
                        return true;
                    }
                }
            } else {
                // 如果当前节点不是目录，只有双星号才能匹配
                return "**".equals(currentPart);
            }
        } else {
            // 处理普通路径部分（非通配符）的精确匹配
            if (node.name.equals(currentPart)) {
                // 如果是排除路径的最后一部分，匹配成功
                if (currentIndex == excludeParts.length - 1) {
                    return true;
                }
                // 如果当前节点是目录且还有更多路径部分需要匹配，则递归处理子节点
                if (node.isDirectory && currentIndex < excludeParts.length - 1) {
                    for (TreeNode child : node.children) {
                        if (matchExcludePath(child, excludeParts, currentIndex + 1)) {
                            return true;
                        }
                    }
                }
            }
        }

        // 所有匹配尝试都失败，返回false
        return false;
    }


    /**
     * 递归匹配路径的方法，用于检查给定的路径是否与当前节点匹配
     *
     * @param node         当前树节点
     * @param pathParts    路径部分的数组
     * @param currentIndex 当前正在匹配的路径部分的索引
     * @return 如果路径匹配则返回true，否则返回false
     */
    private static boolean matchPath(TreeNode node, String[] pathParts, int currentIndex) {
        // 如果当前索引超出路径部分数组的长度，说明路径已匹配完，但未到达有效节点
        if (currentIndex >= pathParts.length) {
            return false;
        }

        // 获取当前要匹配的路径部分
        String currentPart = pathParts[currentIndex];

        // 处理通配符的情况：单星号(*)或双星号(**)
        if ("*".equals(currentPart) || "**".equals(currentPart)) {
            // 如果是路径的最后一部分，直接返回true
            if (currentIndex == pathParts.length - 1) {
                return true;
            }

            // 如果当前节点是目录
            if (node.isDirectory) {
                // 遍历所有子节点进行匹配
                for (TreeNode child : node.children) {
                    // 尝试匹配下一个路径部分
                    if (matchPath(child, pathParts, currentIndex + 1)) {
                        return true;
                    }
                    // 如果是双星号，则递归匹配当前路径部分（支持多级目录匹配）
                    if ("**".equals(currentPart) && child.isDirectory && matchPath(child, pathParts, currentIndex)) {
                        return true;
                    }
                }
            } else {
                // 如果不是目录，只有双星号才能匹配
                return "**".equals(currentPart);
            }
        } else {
            // 处理普通路径部分的情况
            if (node.name.equals(currentPart)) {
                // 如果是路径的最后一部分，匹配成功
                if (currentIndex == pathParts.length - 1) {
                    return true;
                }
                // 如果当前节点是目录且还有更多路径部分需要匹配
                if (node.isDirectory && currentIndex < pathParts.length - 1) {
                    // 遍历所有子节点继续匹配
                    for (TreeNode child : node.children) {
                        if (matchPath(child, pathParts, currentIndex + 1)) {
                            return true;
                        }
                    }
                }
            }
        }

        // 如果所有匹配尝试都失败，返回false
        return false;
    }


    /**
     * 根据路径模式递归克隆树节点及其子节点
     * 该方法会根据给定的路径部分数组和当前索引，创建一个新的树节点，
     * 并只克隆与路径模式匹配的子节点分支。支持通配符(*)和双星号(**)的路径匹配。
     *
     * @param node         要克隆的当前树节点
     * @param pathParts    路径部分的字符串数组，支持普通路径、通配符(*)和双星号(**)
     * @param currentIndex 当前正在处理的路径部分的索引位置
     * @return 克隆后的新树节点，包含与路径模式匹配的子节点结构
     */
    private static TreeNode cloneNodeWithPath(TreeNode node, String[] pathParts, int currentIndex) {
        // 创建新的树节点，复制原节点的名称和类型
        TreeNode newNode = new TreeNode(node.name, node.isDirectory, node.parent);

        // 如果当前节点是目录，则处理子节点的克隆
        if (node.isDirectory) {
            for (TreeNode child : node.children) {
                // 判断是否还有后续路径部分需要匹配
                if (currentIndex < pathParts.length - 1) {
                    // 获取下一个路径部分用于匹配
                    String nextPart = pathParts[currentIndex + 1];
                    // 如果下一个路径部分是通配符、双星号或子节点名称匹配，则递归克隆
                    if ("*".equals(nextPart) || "**".equals(nextPart) || child.name.equals(nextPart)) {
                        newNode.children.add(cloneNodeWithPath(child, pathParts, currentIndex + 1));
                    } else if ("**".equals(pathParts[currentIndex])) {
                        // 处理双星号的特殊匹配逻辑：支持跨级目录匹配

                        // 尝试将下一个路径部分与当前子节点进行匹配
                        if (matchPath(child, pathParts, currentIndex + 1)) {
                            newNode.children.add(cloneNodeWithPath(child, pathParts, currentIndex + 1));
                        }
                        // 如果子节点是目录且当前双星号也能匹配，则保持在当前索引继续递归
                        if (child.isDirectory && matchPath(child, pathParts, currentIndex)) {
                            newNode.children.add(cloneNodeWithPath(child, pathParts, currentIndex));
                        }
                    }
                } else {
                    // 已到达路径末尾，继续克隆剩余的所有子节点
                    newNode.children.add(cloneNodeWithPath(child, pathParts, currentIndex));
                }
            }
        }

        return newNode;
    }


    /**
     * 递归打印树形结构的节点
     *
     * @param nodes  当前层级的节点列表
     * @param prefix 当前层级的缩进前缀
     */
    private static void printTreeNode(List<TreeNode> nodes, String prefix) {
        // 遍历当前层级的所有节点
        for (int i = 0; i < nodes.size(); i++) {
            // 获取当前节点
            TreeNode node = nodes.get(i);
            // 判断是否为当前层级的最后一个节点
            boolean isLast = (i == nodes.size() - 1);

            // 打印节点名称，并根据是否为目录添加"/"后缀
            // 使用不同的连接符表示层级关系："└── "表示最后一个节点，"├── "表示其他节点
            log.info(prefix + (isLast ? "└── " : "├── ") + node.name + (node.isDirectory ? "/" : "")/* + "###########[" + node.getFullPath() + "]"*/);

            // 如果节点有子节点，则递归处理子节点
            if (!node.children.isEmpty()) {
                // 根据是否为最后一个节点，更新缩进前缀
                // 最后一个节点使用"    "，其他节点使用"│   "，以保持树形结构的视觉连贯性
                String newPrefix = prefix + (isLast ? "    " : "│   ");
                printTreeNode(node.children, newPrefix);
            }
        }
    }

    /**
     * 树节点类，用于表示文件系统中的文件或目录
     * 每个节点可以包含名称、类型（文件/目录）和子节点列表
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TreeNode {
        // 节点名称，表示文件或目录的名称
        String name;
        // 标识节点是否为目录，true表示目录，false表示文件
        boolean isDirectory;
        // 子节点列表，用于存储目录下的文件和子目录
        List<TreeNode> children = new ArrayList<>();
        TreeNode parent;

        /**
         * 构造函数，用于创建新的树节点
         *
         * @param name        节点名称
         * @param isDirectory 是否为目录的标志
         */
        TreeNode(String name, boolean isDirectory) {
            this.name = name;
            this.isDirectory = isDirectory;
        }

        TreeNode(String name, boolean isDirectory, TreeNode parent) {
            //TreeNode currentParent = new TreeNode(parent.name, parent.isDirectory);
            if (parent != null) {
                TreeNode currentParent = new TreeNode(parent.name, parent.isDirectory);
                copyParentChain(currentParent, parent.getParent());
                this.parent = currentParent;
            }
            this.name = name;
            this.isDirectory = isDirectory;
            //this.parent = currentParent;
            //this.parent.children = new ArrayList<>();
        }

        private void copyParentChain(TreeNode target, TreeNode source) {
            if (source == null) {
                return;
            }
            TreeNode newParent = new TreeNode(source.name, source.isDirectory);
            target.parent = newParent;
            copyParentChain(newParent, source.getParent());
        }
        public String getFullPath() {
            if (parent == null) {
                return name;
            }
            StringBuilder path = new StringBuilder(name);
            TreeNode current = parent;
            while (current != null) {
                path.insert(0, current.name + "/");
                current = current.parent;
            }
            String fullPath = path.toString();
            fullPath = fullPath.startsWith("root/") ? fullPath.substring(5) : fullPath;
            return fullPath;
        }
    }


    /**
     * 平台配置类，用于存储和管理API相关的配置信息
     * 包含树形结构API的URL、文件API的URL以及请求头信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class PlatformConfig {
        // 树形结构API的URL
        String treeApiUrl;
        // 文件API的URL
        String fileApiUrl;
        // 最大重试次数
        int maxRetries = 3;
        // 重试间隔时间，单位为毫秒
        int retriesSleep = 2000;
        // 请求头信息，使用Map存储键值对
        Map<String, String> headers;
        ProxyConfig proxy;
    }

    /**
     * 代理配置类，用于配置HTTP请求的代理设置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class ProxyConfig {
        String host;
        int port;
        String username;
        String password;
        boolean enabled;

        public ProxyConfig(String host, int port) {
            this.host = host;
            this.port = port;
            this.enabled = true;
        }
    }
}



