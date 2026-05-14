package com.cloud_guest.abs;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.abs.service.AbstractApiSaltService;
import com.cloud_guest.abs.service.AbstractKeyService;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.domain.api.SaltInfo;
import com.cloud_guest.domain.http.CachedBodyHttpServletRequest;
import com.cloud_guest.domain.key.KeyInfo;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalCustomException;
import com.cloud_guest.properties.auth.ApiProperties;
import com.cloud_guest.utils.DateUtils;
import com.cloud_guest.utils.MatcherUtils;
import com.cloud_guest.utils.RSAUtil;
import com.cloud_guest.utils.api.ApiUtil;
import com.cloud_guest.utils.api.SingleSignature;
import com.cloud_guest.utils.gateway.GatewayUtils;
import com.cloud_guest.utils.object.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.core.env.Environment;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2024/5/18 0018 2:36
 * @Description
 */
public interface AbsApiSign extends AbsBean {
    ObjectMapper objectMapper = ApiUtil.getObjectMapper();

    default ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 1. 获取 ContentCachingRequestWrapper
     *
     * @param request
     * @return
     */
    default ContentCachingRequestWrapper getContentCachingRequestWrapper(HttpServletRequest request) {
        return new ContentCachingRequestWrapper(request);
    }

    /**
     * 检查验签
     */
    default void checkApi(HttpServletRequest request, CachedBodyHttpServletRequest cachedBodyHttpServletRequest) {

        ApiProperties api = SpringUtil.getBean(ApiProperties.class);

        String springApplicationName = api.getName();
        String apiSalt = api.getSalt();
        String signAsName = api.getSignAsName();
        String timestampAsName = api.getTimestampAsName();
        List<String> pathsToMatch = api.getPathsToMatch();
        Boolean signEnable = api.getSignEnable();
        Boolean signMultipleEnable = api.getSignMultipleEnable();
        Long signTimeOut = api.getSignTimeOut();
        TimeUnit signTimeUnit = api.getSignTimeUnit();

        //时间戳验证
        String url = "";
        StringBuffer requestURL = request.getRequestURL();
        String remoteAddr = request.getRemoteAddr();
        String clientIP = JakartaServletUtil.getClientIP(request);
        getLogger().debug("请求ip:{},clientIP:{}", remoteAddr, clientIP);
        if (!ObjectUtils.isEmpty(requestURL)) {
            url = requestURL.toString();
        }
        boolean apiMatches = !ObjectUtil.isNotEmpty(pathsToMatch);
        String s = removePrefix(url);
        for (String match : pathsToMatch) {
            apiMatches = apiMatches || MatcherUtils.matches(s, match);
            if (apiMatches) {
                break;
            }
        }
        if (verifyIpWhiteList(remoteAddr, api.getIpWhitelist())) {
            log().debug("ip白名单");
            //白名单
        } else if (verifyIpBlackList(remoteAddr, api.getIpBlackList())) {
            log().error("ip={}黑名单", remoteAddr);
            throw new GlobalCustomException("ip 非法访问");
        } else if (apiMatches) {
            //log().debug("路径符合验签逻辑");
            if (ObjectUtil.isNotEmpty(signEnable) && signEnable) {
                //log().debug("api验签-已开启");
                verifyTimestamp(request, timestampAsName, signTimeOut,signTimeUnit);
                //验签
                List<String> exCollection = CollUtil.newArrayList(timestampAsName, signAsName);
                List<Boolean> verifySignList = CollUtil.newArrayList();

                List<SaltInfo> apiSaltList = CollUtil.newArrayList();
                AbstractApiSaltService apiSaltService = SpringUtil.getBean(AbstractApiSaltService.class);
                Collection<SaltInfo> saltList = apiSaltService.getSaltList();
                if (CollUtil.isNotEmpty(saltList)) {
                    apiSaltList.addAll(saltList);
                }

                if ((!signMultipleEnable) && StrUtil.isNotBlank(springApplicationName)) {
                    apiSaltList = apiSaltList.stream()
                            .filter(o -> ObjectUtil.equals(o.getServiceName(), springApplicationName))
                            .collect(Collectors.toList());
                    if (CollUtil.isEmpty(apiSaltList)) {
                        SaltInfo saltInfo = new SaltInfo().setSalt(apiSalt).setServiceName("通用");
                        apiSaltList.add(saltInfo);
                    }
                }

                boolean verifySign = false;
                for (SaltInfo salt : apiSaltList) {
                    String serviceName = salt.getServiceName();
                    try {
                        verifySign = verifySign(cachedBodyHttpServletRequest, signAsName, salt.getSalt(), serviceName, null, exCollection);
                        if (verifySign) {
                            //log().debug("[{}]==>验签通过", serviceName);
                            break;
                        }
                    } catch (GlobalCustomException e) {
                        log().error("<==验签失败==>{}", e.getMessage());
                    } finally {
                        verifySignList.add(verifySign);
                    }
                }

                if (!verifySignList.contains(true)) {
                    log().error("<==验签失败==>");
                    throw new GlobalCustomException("签名不合法");
                } else {
                    //log().info("验签通过");
                }

            }
        } else {
            //log().debug("非api验签");
        }
    }

    /**
     * 1. 验证请求时间戳
     *
     * @param request
     * @param timestampAsName 时间戳名称
     * @param signTimeOut     时间戳超时时间(单位: 分钟)
     * @return
     */
    default String verifyTimestamp(HttpServletRequest request, String timestampAsName, Long signTimeOut) {
        return verifyTimestamp(request, timestampAsName, signTimeOut, TimeUnit.MINUTES);
    }

    default String verifyTimestamp(HttpServletRequest request, String timestampAsName, Long signTimeOut,TimeUnit signTimeUnit) {
        String timestampHeader = request.getHeader(timestampAsName);
        if (StrUtil.isBlank(timestampHeader)) {
            log().error("timestampAsName is null");
            throw new GlobalCustomException(ApiCode.VALIDATE_FAILED, "请求时间戳不合法");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = DateUtils.longToLocalDateTime(Long.parseLong(timestampHeader));
        Duration between = Duration.between(localDateTime, now);
        TimeUnit timeUnit = ObjectUtils.defaultIfEmpty(signTimeUnit, TimeUnit.MINUTES);

        long value=0;
        switch (timeUnit) {
            case NANOSECONDS -> value = between.toNanos();
            case MICROSECONDS -> value = Math.toIntExact(between.toMillis() * 1000);
            case MILLISECONDS -> value = between.toMillis();
            case SECONDS -> value = between.getSeconds();
            case MINUTES -> value = between.toMinutes();
            case HOURS -> value = between.toHours();
            case DAYS -> value = between.toDays();
            default -> value = between.toMinutes();
        }

        long diffTime = Math.abs(value);
        if (diffTime >= signTimeOut) {
            log().error("{}=={},{},{}", timestampAsName, timestampHeader, DateUtils.LocalDateTimeTolong(now), diffTime);
            throw new GlobalCustomException(ApiCode.VALIDATE_FAILED, "请求时间戳不合法");
        }

/*
        if (StrUtil.isBlank(timestampHeader)) {
            log().error("timestampAsName is null");
            throw new GlobalCustomException(ApiCode.VALIDATE_FAILED, "请求时间戳不合法");
        }

        long timestampMillis = Long.parseLong(timestampHeader);
        long currentMillis = System.currentTimeMillis();

        long value=0;
        switch (timeUnit) {
            case NANOSECONDS -> value = 1;
            case MICROSECONDS -> value = 1000;
            case MILLISECONDS -> value = 1000 * 1000;
            case SECONDS -> value = 1000;
            case MINUTES -> value = 1000 * 60;
            case HOURS -> value = 1000 * 60 * 60;
            case DAYS -> value = 1000 * 60 * 60 * 24;
            default -> value = 1000 * 60;
        }
        long diffTime = Math.abs((currentMillis - timestampMillis) / (value));

        if (diffTime >= signTimeOut) {
            log().error("{}=={},{},{}", timestampAsName, timestampHeader, currentMillis, diffTime);
            throw new GlobalCustomException(ApiCode.VALIDATE_FAILED, "请求时间戳不合法");
        }
*/

        return timestampHeader;

    }
    @SneakyThrows
    default boolean verifySign(HttpServletRequest request, String signAsName, String salt, String serviceName, String url, Collection<String> exCollection) {
        String method = request.getMethod();
        url = StrUtil.isBlank(url) ? String.valueOf(request.getRequestURL()) : url;
        Map<String, String[]> parameterMap = Maps.newLinkedHashMap(request.getParameterMap());
        Map<String, Object> body = Maps.newLinkedHashMap();
        if (CollUtil.isEmpty(parameterMap)) {
            parameterMap = Maps.newLinkedHashMap();
        }
        CachedBodyHttpServletRequest cachedBodyHttpServletRequest = (CachedBodyHttpServletRequest) request;
        ServletInputStream requestInputStream = cachedBodyHttpServletRequest.getInputStream();
        String json = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(requestInputStream))) {
            json = reader.lines().collect(Collectors.joining("\n"));
        }
        if (StrUtil.isNotBlank(json)) {
            Map<String, Object> readValue = JSONUtil.toBean(json, Map.class);
            readValue.entrySet().stream().forEach(o -> {
                String empty = null;
                Object value = (o.getValue() == null || "null".equals(o.getValue())) ? empty : o.getValue();
                body.put(o.getKey(), value);
            });

            //body.putAll(readValue);
        }



        log().debug("请求参数 parameter:{}", parameterMap);
        log().debug("请求参数 body:{}", body);
       /* String openFeign = request.getHeader(Openfeign.OPENFEIGN.getHeader());
        if (StrUtil.isNotBlank(openFeign)) {
            String host = request.getHeader("host");
            String[] urlSplit = url.split(host);
            url = new StringBuffer(urlSplit[0]).append(openFeign).append(urlSplit[urlSplit.length - 1]).toString();
            log().info("[{}] sign ...", Openfeign.OPENFEIGN.getDesc());
        }*/
        url = GatewayUtils.replaceUrl(request, url);

        String id = null;
        ApiProperties apiProperties = SpringUtil.getBean(ApiProperties.class);
        List<String> pathsToExclude = apiProperties.getEncryptionPathsToExclude();
        boolean isNotEmpty = ObjectUtils.isNotEmpty(pathsToExclude);
        boolean noExclude = true;
        if (isNotEmpty) {
            String replace = removePrefix(url);
            for (String exclude : pathsToExclude) {
                if (MatcherUtils.matches(replace, exclude)) {
                    noExclude = false;
                    break;
                }
            }
        }

        if (noExclude) {
            String idAsName = apiProperties.getIdAsName();
            id = request.getHeader(idAsName);
        }
        log().debug("请求参数 URL:{}", url);
        String generalSign = generalSign(salt, method, url, parameterMap, body, exCollection);
        String headerSign = request.getHeader(signAsName);
        headerSign = decryptSign(id, headerSign);
        log().debug("验签 {}:{}", signAsName, headerSign);
        log().debug("验签 {}:{}", "generalSign", generalSign);
        if (!ObjectUtil.equals(generalSign, headerSign)) {
            throw new GlobalCustomException("[serviceName]==>签名不合法".replace("serviceName", serviceName));
        }
        return true;
    }

    default String removePrefix(String url) {
        String replace = url.replace("://", "").replace(":", "");
        Environment bean = SpringUtil.getBean(Environment.class);
        String contextPath = bean.getProperty("server.servlet.context-path");
        if (StrUtil.isNotBlank(contextPath)) {
            int index = replace.indexOf(contextPath);
            if (index != -1) {
                replace = replace.substring(index + contextPath.length());
            } else {
                replace = ""; // 或者 return false;
            }
        } else {
            String str = "/";
            int index = replace.indexOf(str);
            if (index != -1) {
                replace = replace.substring(index);
            } else {
                replace = ""; // 或者 return false;
            }
        }

        return StrUtil.isBlank(replace) ? "/" : replace;
    }

    /**
     * 1. 验证请求IP是否在白名单中
     *
     * @param ip
     * @param ipWhitelist
     * @return
     */

    default boolean verifyIpWhiteList(String ip, List<String> ipWhitelist) {
        List<String> list = CollUtil.newArrayList();
        if (ObjectUtil.isNotEmpty(ipWhitelist)) {
            list.addAll(ipWhitelist);
        }
        return list.contains(ip);
    }

    /**
     * 1. 验证请求IP是否在黑名单中
     *
     * @param ip
     * @param ipBlackList
     * @return
     */
    default boolean verifyIpBlackList(String ip, List<String> ipBlackList) {
        List<String> list = CollUtil.newArrayList();
        if (ObjectUtil.isNotEmpty(ipBlackList)) {
            list.addAll(ipBlackList);
        }
        return list.contains(ip);
    }

    /**
     * @param salt         签名密钥
     * @param method       HTTP方法（GET、POST等）
     * @param url          请求的URL
     * @param params       请求参数的Map
     * @param body         请求body的Map
     * @param exCollection
     * @return 生成的签名字符串
     */
    @SneakyThrows
    default String generalSign(String salt, String method, String url, Map<String, String[]> params, Map<String, Object> body, Collection<String> exCollection) {
        String generalSign = ApiUtil.generalSign(new SingleSignature()
                .setSalt(salt)
                .setMethod(method)
                .setUrl(url)
                .setParams(params)
                .setBody(body)
                .setExCollection(exCollection));
        return generalSign;
    }

    /**
     * 默认方法：解密签名
     *
     * @param id   密钥ID
     * @param sign 待解密的签名
     * @return 解密后的签名
     * @throws GlobalCustomException 当请求非法时抛出
     */
    @SneakyThrows
    default String decryptSign(String id, String sign) {
        // 获取是否启用双重对称加密的配置
        Boolean enableDoubleSymmetricEncryption = SpringUtil.getBean(ApiProperties.class).getEnableDoubleSymmetricEncryption();
        if (enableDoubleSymmetricEncryption) {
            // 检查id是否为空
            if (StrUtil.isNotBlank(id)) {
                //id获取key
                long currentTimeMillis = System.currentTimeMillis();
                // 获取密钥服务实例
                AbstractKeyService service = SpringUtil.getBean(AbstractKeyService.class);
                // 根据id获取密钥信息
                KeyInfo keyInfoById = service.getKeyInfoById(id);
                // 检查密钥信息是否存在
                if (ObjectUtil.isEmpty(keyInfoById)) {
                    throw new GlobalCustomException("非法请求!");
                }
                // 获取密钥创建时间和有效时间间隔
                Long createTime = keyInfoById.getCreateTime();
                Long validTimeInterval = keyInfoById.getValidTimeInterval();
                long interval = currentTimeMillis - createTime;
                try {
                    // 检查密钥是否在有效期内
                    if (interval > validTimeInterval || interval < 0) {
                        throw new GlobalCustomException("非法请求!");
                    }
                    // 获取私钥并进行解密
                    String privateKeyBase64 = keyInfoById.getPrivateKeyBase64();
                    PrivateKey privateKey = RSAUtil.stringToPrivateKey(privateKeyBase64);
                    sign = RSAUtil.decryptByPrivateKey(sign, privateKey);
                } finally {
                    // 确保在异常情况下也执行密钥移除操作
                    try {
                        // 验证完成后移除key
                        service.remove(Arrays.asList(id));
                    } catch (Exception e) {
                        // 记录移除密钥时的错误日志
                        log().error("remove key error", e);
                    }
                    //验证完成移除key
                }
            } else {
                throw new GlobalCustomException("非法请求!");
            }
        }
        return sign;
    }

    @Deprecated
    @SneakyThrows
    default String generalSign(String id, String salt, String method, String url, Map<String, String[]> params, Map<String, Object> body, Collection<String> exCollection) {
        String generalSign = generalSign(salt, method, url, params, body, exCollection);

        Boolean enableDoubleSymmetricEncryption = SpringUtil.getBean(ApiProperties.class).getEnableDoubleSymmetricEncryption();
        if (enableDoubleSymmetricEncryption) {
            if (StrUtil.isNotBlank(id)) {
                //id获取key
                long currentTimeMillis = System.currentTimeMillis();
                AbstractKeyService service = SpringUtil.getBean(AbstractKeyService.class);
                KeyInfo keyInfoById = service.getKeyInfoById(id);
                if (ObjectUtil.isEmpty(keyInfoById)) {
                    throw new GlobalCustomException("非法请求!");
                }
                Long createTime = keyInfoById.getCreateTime();
                Long validTimeInterval = keyInfoById.getValidTimeInterval();
                long interval = currentTimeMillis - createTime;
                try {
                    if (interval > validTimeInterval || interval < 0) {
                        throw new GlobalCustomException("非法请求!");
                    }
                    String publicKeyBase64 = keyInfoById.getPublicKeyBase64();
                    PublicKey publicKey = RSAUtil.stringToPublicKey(publicKeyBase64);
                    generalSign = RSAUtil.encryptByPublicKey(generalSign, publicKey);
                    return generalSign;
                } finally {
                    try {
                        service.remove(Arrays.asList(id));
                    } catch (Exception e) {
                        log().error("remove key error", e);
                    }
                    //验证完成移除key
                }
            } else {
                throw new GlobalCustomException("非法请求!");
            }
        }

        return generalSign;
    }
}
