package com.cloud_guest.properties;


/**
 * @Author yan
 * @Date 2024/11/14 下午1:59:08
 * @Description
 */
public class PropertiesConstants {

    public static final String CONFIG = "config";
    public static final String AUTH = "auth";
    public static final String SECURITY = "security";
    public static final String SHIRO = "shiro";
    public static final String ANNOTATION = "annotation";
    public static final String NAME = "name";
    public static final String PROP = "prop";

    public static final String FULL_PATH = "full-path";

    public static final String DOMAINS = "domains";
    public static final String CORS = "cors";
    public static final String GATEWAY = "gateway";
    public static final String FILTER = "filter";

    public static final String ENABLED = "enabled";
    public static final String DEFAULT_FILTER = "default-filter";
    public static final String WEB_FILTER = "web-filter";
    public static final String DISTINCT_HEADERS_FILTER = "distinct-headers-filter";
    public static final String HTTPS_TO_HTTP_FILTER = "https-to-http-filter";

    public static final String GATEWAY_CONFIG = CONFIG + "." + GATEWAY;

    public static final String GATEWAY_CORS_CONFIG = GATEWAY_CONFIG + "." + CORS;
    public static final String GATEWAY_CORS_PROP_CONFIG = GATEWAY_CORS_CONFIG + "." + PROP;
    public static final String GATEWAY_CORS_ENABLED_CONFIG = GATEWAY_CORS_CONFIG + "." + ENABLED;

    public static final String CORS_CONFIG = CONFIG + "." + CORS;
    public static final String CORS_PROP_CONFIG = CORS_CONFIG + "." + PROP;
    public static final String CORS_ENABLED_CONFIG = CORS_CONFIG + "." + ENABLED;

    public static final String CORS_PREFIX = CORS_PROP_CONFIG;
    public static final String CORS_ENABLED = CORS_ENABLED_CONFIG;
    public static final String CORS_GATEWAY_PREFIX = GATEWAY_CORS_PROP_CONFIG;
    public static final String CORS_GATEWAY_ENABLED = GATEWAY_CORS_ENABLED_CONFIG;

    public static final String CORS_GATEWAY_DEFAULT_FILTER = DEFAULT_FILTER;
    public static final String CORS_GATEWAY_WEB_FILTER = WEB_FILTER;
    public static final String CORS_GATEWAY_DISTINCT_HEADERS_FILTER = DISTINCT_HEADERS_FILTER;

}
