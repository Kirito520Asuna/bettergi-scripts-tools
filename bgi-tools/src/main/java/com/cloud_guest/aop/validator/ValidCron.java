package com.cloud_guest.aop.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

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
