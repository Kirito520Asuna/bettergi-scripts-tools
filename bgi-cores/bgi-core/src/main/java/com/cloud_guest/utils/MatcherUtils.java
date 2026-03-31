package com.cloud_guest.utils;

import org.springframework.util.AntPathMatcher;

/**
 * @Author yan
 * @Date 2026/3/31 18:38:40
 * @Description
 */
public class MatcherUtils {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    //private static final String API_PATTERN = "/api/**";
    //
    ///**
    // * 判断路径是否匹配 /api/**
    // */
    //public static boolean matches(String path) {
    //    if (path == null || path.isEmpty()) {
    //        return false;
    //    }
    //    return PATH_MATCHER.match(API_PATTERN, path);
    //}

    /**
     * 自定义前缀的 API 路径匹配
     */
    public static boolean matches(String path, String pattern) {
        return matches(path, pattern, false);
    }

    public static boolean matches(String path, String pattern, boolean suffixMatch) {
        if (path == null || path.isEmpty() || pattern == null) {
            return false;
        }
        // 如果 pattern 以 ** 开头，提取后面的部分进行后缀匹配
        if (suffixMatch && pattern.startsWith("**")) {
            String actualPattern = pattern.substring(2);
            // 移除开头的 /（如果有）
            if (actualPattern.startsWith("/")) {
                actualPattern = actualPattern.substring(1);
            }
            // 检查 path 是否以该模式结尾
            return path.endsWith(actualPattern) || PATH_MATCHER.match(actualPattern, path);
        }
        return PATH_MATCHER.match(pattern, path);
    }

    public static void main(String[] args) {
        //System.out.println(matches("/api/user"));           // true
        //System.out.println(matches("/api/user/123"));       // true
        //System.out.println(matches("/api/v1/orders"));      // true
        //System.out.println(matches("/user/api"));           // false
        //System.out.println(matches("/api"));
        System.out.println(matches("/bgi/api/11", "/api/**"));

        System.out.println(matches("/api", "**/api/key/a"));
        System.out.println(matches("/api", "**/api/key/a",true));
        System.out.println(matches("/api/key/a", "**/api/key/a"));
        System.out.println(matches("/api/key/a", "/api/key/a"));
        System.out.println(matches("/api/key/a", "**/api/key/a",true));
        String url = "http://localhost:8080/bgi/api/11";
        int index = url.indexOf("/bgi");
        System.out.println(url.substring(index));
    }

}
