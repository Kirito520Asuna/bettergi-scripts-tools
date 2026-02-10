package com.cloud_guest.aop.log;


import com.cloud_guest.enums.BusinessType;
import com.cloud_guest.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.*;


/**
 * @author yan
 * @date 2023/4/12 0012 18:30
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SysLog {
    /**
     * 接口打印日志 ip 描述 请求地址 请求方式 请求参数 请求类方法
     */
    //打印控制台开关
    boolean flag() default true;

    /**
     * 开启响应输出
     * @return
     */
    boolean result() default true;

    /**
     * 标题
     * @return
     */
    String title() default "";


    // ========== 模块字段 ==========

    String application() default "";
    /**
     * 操作模块
     * <p>
     * 为空时，会尝试读取 {@link Tag#name()} 属性
     */
    String module() default "";

    /**
     * 操作名
     * <p>
     * 为空时，会尝试读取 {@link Operation#summary()} 属性
     */
    String name() default "";

    /**
     * 操作分类
     * <p>
     * 实际并不是数组，因为枚举不能设置 null 作为默认值
     */
    OperateTypeEnum type() default OperateTypeEnum.OTHER;

    /**
     * 业务类型
     * @return
     */
    BusinessType businessType() default BusinessType.OTHER;
    // ========== 开关字段持久化 ==========

    /**
     * 是否记录操作日志
     */
    boolean enableOperate() default false;

    /**
     * 是否记录方法参数
     */
    boolean logArgs() default true;

    /**
     * 是否记录方法结果的数据
     */
    boolean logResultData() default true;

    /**
     * 是否包装返回结果
     */
    boolean wrapResult() default false;
}


