package com.cloud_guest.enums;

/**
 * @Author yan
 * @Date 2026/2/25 18:16:03
 * @Description
 */
public enum OSType {
    WINDOWS,
    LINUX,
    MACOS,
    UNIX,
    UNKNOWN;

    /**
     * 通过路径格式判断操作系统
     */
    public static OSType detectByPathFormat(String path) {
        if (path == null || path.isEmpty() || path.startsWith("./")) {//相对路径无法判断
            return OSType.UNKNOWN;
        }

        // Windows路径特征
        if (path.contains("\\") ||
                (path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':')) {
            return OSType.WINDOWS;
        }

        // Unix/Linux/macOS路径特征
        if (path.startsWith("/")) {
            // 进一步区分Unix-like系统
            if (path.contains("/Users/") || path.contains("/Applications/")) {
                return OSType.MACOS;
            } else if (path.contains("/proc/") || path.contains("/sys/") || path.contains("/dev/")) {
                return OSType.LINUX;
            } else if (path.contains("/usr/") || path.contains("/etc/") || path.contains("/var/")) {
                return OSType.UNIX;
            }
            // 如果只是以/开头但没有特定目录特征，默认返回UNIX
            return OSType.UNIX;
        }

        return OSType.UNKNOWN;
    }

    // 添加获取OSType的方法
    public static OSType getCurrentOSType() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return OSType.WINDOWS;
        } else if (osName.contains("linux")) {
            return OSType.LINUX;
        } else if (osName.contains("mac")) {
            return OSType.MACOS;
        } else if (osName.contains("unix")) {
            return OSType.UNIX;
        } else {
            return OSType.UNKNOWN;
        }
    }

    // 添加根据OSType判断是否为Windows的方法
    public static boolean isWindows(OSType osType) {
        osType = osType != null ? getCurrentOSType() : osType;
        return osType == OSType.WINDOWS;
    }

    // 添加根据OSType判断是否为Unix-like系统的方法
    public static boolean isUnixLike(OSType osType) {
        osType = osType != null ? osType : getCurrentOSType();
        return osType == OSType.LINUX || osType == OSType.MACOS || osType == OSType.UNIX;
    }
}
