package com.cloud_guest.aop.validator;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Author yan
 * @Date 2025/10/10 13:48:08
 * @Description
 */
public class CronValidator implements ConstraintValidator<ValidCron, String> {
    @Override
    public void initialize(ValidCron constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String cronExpression, ConstraintValidatorContext constraintValidatorContext) {
        CronType type= CronType.QUARTZ;
        
        CronDefinition cronDefinition; 
        switch (type) {
            case QUARTZ : cronDefinition=CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ); break;
            case SPRING : cronDefinition=CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING); break;
            case UNIX : cronDefinition=CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX); break;
            default : throw new IllegalArgumentException("不支持的类型");
        };
        CronParser parser = new CronParser(cronDefinition);

        try {
            Cron cron =parser.parse(cronExpression).validate();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
