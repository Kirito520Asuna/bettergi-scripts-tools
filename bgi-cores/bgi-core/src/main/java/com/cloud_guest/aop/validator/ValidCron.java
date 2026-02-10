package com.cloud_guest.aop.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author yan
 * @Date 2026/1/12 17:44:07
 * @Description
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CronValidator.class)
public @interface ValidCron {
    String message() default "cron格式不正确";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
