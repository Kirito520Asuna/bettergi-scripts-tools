package com.cloud_guest.aop;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import com.cloud_guest.aop.bean.AbsBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @Author yan
 * @Date 2025/5/18 15:38:27
 * @Description
 */
public interface AbsAop extends Ordered, AbsBean {
    JSONConfig JSON_CONFIG = JSONConfig.create().setIgnoreNullValue(false);

    @Override
    @PostConstruct
    default void init() {
        log().debug("[init]-[Aop]:[Order:{}]::[{}]", getOrder(), getAClassName());
    }
    @SneakyThrows
    default <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        T annotation = null;
        if (ObjectUtil.isNotEmpty(method)) {
            annotation = method.getAnnotation(annotationClass);
        }
        if (annotation == null) {
            // 获取目标类
            Class<?> targetClass = joinPoint.getTarget().getClass();
            // 获取类上的 注解
            annotation = targetClass.getAnnotation(annotationClass);
        }
        return annotation;
    }

    default String getModule(JoinPoint joinPoint) {
        Tag tag = getAnnotation(joinPoint, Tag.class);
        String module = "";
        if (ObjectUtil.isNotEmpty(tag)) {
            if (StrUtil.isNotEmpty(tag.name())) {
                // 优先读取 @Tag 的 name 属性
                module = tag.name();
            } else if (StrUtil.isNotEmpty(tag.description())) {
                // 没有的话，读取 @API 的 description 属性
                module = tag.description();
            }
        }

        if (StrUtil.isBlank(module)) {
            Api api = getAnnotation(joinPoint, Api.class);
            if (ObjectUtil.isNotEmpty(api)) {
                if (StrUtil.isNotEmpty(api.value())) {
                    // 优先读取 @Api 的 value 属性
                    module = api.value();
                } else if (StrUtil.isNotEmpty(api.description())) {
                    // 没有的话，读取 @Api 的 description 属性
                    module = api.description();
                }
            }
        }

        if (StrUtil.isBlank(module)) {
            // 获取方法签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            // 获取所在的类
            Class<?> declaringClass = signature.getDeclaringType();
            Tag tagAnnotation = declaringClass.getAnnotation(Tag.class);
            if (ObjectUtil.isNotEmpty(tagAnnotation)) {
                if (StrUtil.isNotEmpty(tagAnnotation.name())) {
                    // 优先读取 @Tag 的 name 属性
                    module = tagAnnotation.name();
                }else if (StrUtil.isNotEmpty(tagAnnotation.description())) {
                    // 没有的话，读取 @Tag 的 description 属性
                    module = tagAnnotation.description();
                }
            }else {
                Api apiAnnotation = declaringClass.getAnnotation(Api.class);
                if (ObjectUtil.isNotEmpty(apiAnnotation)) {
                    if (StrUtil.isNotEmpty(apiAnnotation.value())) {
                        // 优先读取 @Api 的 value 属性
                        module = apiAnnotation.value();
                    }else if (StrUtil.isNotEmpty(apiAnnotation.description())) {
                        // 没有的话，读取 @Api 的 description 属性
                        module = apiAnnotation.description();
                    }
                }
            }
        }
        return module;
    }

    default String getDescription(JoinPoint joinPoint) {
        Operation operation = getAnnotation(joinPoint, Operation.class);
        String description = "";
        if (ObjectUtil.isNotEmpty(operation)) {
            if (StrUtil.isNotEmpty(operation.summary())) {
                // 优先读取 @Operation 的 summary 属性
                description = operation.summary();
            } else if (StrUtil.isNotEmpty(operation.description())) {
                // 没有的话，读取 @Operation 的 description 属性
                description = operation.description();
            }
        }

        if (StrUtil.isBlank(description)) {
            ApiOperation apiOperation = getAnnotation(joinPoint, ApiOperation.class);
            if (ObjectUtil.isNotEmpty(apiOperation)) {
                if (StrUtil.isNotEmpty(apiOperation.value())) {
                    // 优先读取 @ApiOperation 的 value 属性
                    description = apiOperation.value();
                } else if (StrUtil.isNotEmpty(apiOperation.notes())) {
                    // 没有的话，读取 @Api 的 notes 属性
                    description = apiOperation.notes();
                }
            }
        }
        return description;
    }

    @Override
    default int getOrder() {
        return AopConstants.BaseOrder;
    }

    void Aop();

    default void doBefore(JoinPoint joinPoint) throws Exception {
    }

    default Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    default void doAfterReturning(JoinPoint joinPoint, Object reValue) {
    }
}
