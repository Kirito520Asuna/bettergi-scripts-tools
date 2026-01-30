package com.cloud_guest.aop.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author yan
 * @Date 2026/1/30 9:14:51
 * @Description
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyListValidator.class)
public @interface NotEmptyList {
    String message() default "集合不可为空";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
