package com.cloud_guest.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.utils.ThreadMdcUtil;
import com.cloud_guest.utils.gateway.GatewayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author yan
 * @Date 2023/6/1 0001 17:03
 * @Description
 */
public interface AbsSysLog extends AbsAop {
    default SysLog getSysLog(JoinPoint joinPoint) {
        return getAnnotation(joinPoint, SysLog.class);
    }

    @Override
    @Pointcut(value = "@annotation(com.cloud_guest.aop.log.SysLog)")
    default void Aop() {
    }

    default Object aroundSysLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String traceId = ThreadMdcUtil.getTraceId();
        //获取是否有注解
        SysLog sysLog = getSysLog(joinPoint);
        /**
         * 开启请求日志
         */
        boolean openRequestLog = ObjectUtil.isNotEmpty(sysLog) && sysLog.flag();
        if (openRequestLog) {
            // 接收到请求，记录请求内容
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            //可能有空指针问题

            HttpServletRequest request = attributes.getRequest();
            // 记录下请求内容、
            String applicationName = SpringUtil.getBean(Environment.class)
                    .getProperty("spring.application.name", String.class);

            String method = request.getMethod();

            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            log().debug("parse:{}", JSONUtil.parse(paramMap, JSON_CONFIG));

            String title = sysLog.title();
            String remoteAddr = request.getRemoteAddr();
            StringBuffer requestURL = request.getRequestURL();

            Object[] pointArgs = joinPoint.getArgs();
            String args = JSONUtil.toJsonStr(JSONUtil.parse(CollUtil.isEmpty(paramMap) ? pointArgs : paramMap, JSON_CONFIG));

            String declaringTypeName = joinPoint.getSignature().getDeclaringTypeName();
            String name = joinPoint.getSignature().getName();
            String module = getModule(joinPoint);
            if (StrUtil.isBlank(title)) {
                title = getDescription(joinPoint);
            }

            String url = GatewayUtils.replaceUrl(request, requestURL.toString());
            log().info(new StringBuffer()
                            .append("\n====================================请求内容====================================")
                            .append("\n==>TRACE_ID : {} <==")
                            .append("\n==>请求服务名 : {} <==")
                            .append("\n==>请求模块名 : {} <==")
                            .append("\n==>请求描述 : {} <==")
                            .append("\n==>请求IP : {} <==")
                            .append("\n==>请求地址 : {} <==")
                            .append("\n==>请求方式 : {} <==")
                            .append("\n==>请求参数 : {} <==")
                            .append("\n==>请求类方法 : {}.{} <==")
                            .append("\n================================================================================")
                            .toString()
                    , traceId, applicationName, module, title, remoteAddr, url, method, args, declaringTypeName, name);
        }

        Object around = null;
        try {
            around = AbsAop.super.around(joinPoint);
            return around;
        } finally {
            boolean openResultLog = ObjectUtil.isNotEmpty(sysLog) && sysLog.result();
            if (openResultLog) {
                long end = System.currentTimeMillis();
                long totalMilliseconds = end - start;
                String timeFormat = formatDuration(totalMilliseconds);
                String resultStr = around == null ? "返回值为空" : JSONUtil.toJsonStr(around, JSON_CONFIG);

                log().info("\n====================================响应内容====================================\n" +
                                "==>TRACE_ID : {} <==\n" +
                                "==>耗时 : {} <==\n" +
                                "==>响应 : {} <==\n" +
                                "================================================================================",
                        traceId, timeFormat, resultStr);
            }
        }
    }
    /**
     * 将毫秒数转换为时分秒格式
     */
    default String formatDuration(long milliseconds) {
        long hours = milliseconds / 3600000;
        long remainingAfterHours = milliseconds % 3600000;
        long minutes = remainingAfterHours / 60000;
        long remainingAfterMinutes = remainingAfterHours % 60000;
        long seconds = remainingAfterMinutes / 1000;
        long millis = remainingAfterMinutes % 1000;

        return String.format("%02dh %02dm %02ds %03dms", hours, minutes, seconds, millis);
    }
    @Override
    default int getOrder() {
        return AopConstants.SysLogOrder;
    }
}
