package com.cloud_guest.utils.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 任务执行工具类
 * ruo yi 的 JobInvokeUtil 改造来 低耦合
 */
public class JobInvokeUtil {

    public static void invokeMethod(Map<String, Object> jobMap) throws Exception {
        String invokeTarget = (String) jobMap.get("invokeTarget");
        String beanName = getBeanName(invokeTarget);
        String methodName = getMethodName(invokeTarget);
        List<Object[]> methodParams = getMethodParams(invokeTarget);

        Object bean = isValidClassName(beanName)
                ? Class.forName(beanName).getDeclaredConstructor().newInstance()
                : SpringUtil.getBean(beanName);

        invokeMethod(bean, methodName, methodParams);
    }

    private static void invokeMethod(Object bean, String methodName, List<Object[]> methodParams)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (CollUtil.isNotEmpty(methodParams)) {
            Method method = bean.getClass().getMethod(methodName, getMethodParamsType(methodParams));
            method.invoke(bean, getMethodParamsValue(methodParams));
        } else {
            Method method = bean.getClass().getMethod(methodName);
            method.invoke(bean);
        }
    }

    public static boolean isValidClassName(String invokeTarget) {
        return StrUtil.count(invokeTarget, ".") > 1;
    }

    public static String getBeanName(String invokeTarget) {
        String beanAndMethod = StrUtil.subBefore(invokeTarget, "(", false);
        return StrUtil.sub(beanAndMethod, 0, beanAndMethod.lastIndexOf('.'));
    }

    public static String getMethodName(String invokeTarget) {
        String beanAndMethod = StrUtil.subBefore(invokeTarget, "(", false);
        return StrUtil.subSuf(beanAndMethod, beanAndMethod.lastIndexOf('.') + 1);
    }

    public static List<Object[]> getMethodParams(String invokeTarget) {
        String methodStr = StrUtil.subBetween(invokeTarget, "(", ")");
        if (StrUtil.isBlank(methodStr)) return null;

        // 正则支持带引号的参数安全分割
        String[] methodParams = methodStr.split(",(?=([^\"']*[\"'][^\"']*[\"'])*[^\"']*$)");

        List<Object[]> result = new LinkedList<>();
        for (String param : methodParams) {
            String str = StrUtil.trim(param);
            if (StrUtil.startWithAny(str, "'", "\"")) {
                result.add(new Object[]{StrUtil.sub(str, 1, str.length() - 1), String.class});
            } else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
                result.add(new Object[]{Boolean.valueOf(str), Boolean.class});
            } else if (StrUtil.endWithIgnoreCase(str, "L")) {
                result.add(new Object[]{Long.valueOf(StrUtil.sub(str, 0, str.length() - 1)), Long.class});
            } else if (StrUtil.endWithIgnoreCase(str, "D")) {
                result.add(new Object[]{Double.valueOf(StrUtil.sub(str, 0, str.length() - 1)), Double.class});
            } else {
                result.add(new Object[]{Integer.valueOf(str), Integer.class});
            }
        }
        return result;
    }

    public static Class<?>[] getMethodParamsType(List<Object[]> methodParams) {
        return methodParams.stream().map(p -> (Class<?>) p[1]).toArray(Class<?>[]::new);
    }

    public static Object[] getMethodParamsValue(List<Object[]> methodParams) {
        return methodParams.stream().map(p -> p[0]).toArray();
    }
}
