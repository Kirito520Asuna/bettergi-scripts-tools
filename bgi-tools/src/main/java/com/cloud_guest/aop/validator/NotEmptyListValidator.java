package com.cloud_guest.aop.validator;


import cn.hutool.core.util.ObjectUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * @Author yan
 * @Date 2025/10/10 13:48:08
 * @Description
 */
public class NotEmptyListValidator implements ConstraintValidator<NotEmptyList, List<?>> {
    @Override
    public void initialize(NotEmptyList constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<?> list, ConstraintValidatorContext constraintValidatorContext) {
        return ObjectUtil.isNotEmpty(list) && list.size() > 0;
    }
}
