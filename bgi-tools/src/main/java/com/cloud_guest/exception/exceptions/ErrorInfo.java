package com.cloud_guest.exception.exceptions;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * @Author yan
 * @Date 2025/6/13 19:07:57
 * @Description
 */
@SuperBuilder
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {

    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 错误语言
     */
    private ErrorLanguage errorLanguage = ErrorLanguage.ZH_CN;
    /**
     * 错误信息 MAP
     * key: errorLanguage
     * value: errorMessage
     */
    private Map<ErrorLanguage, String> errorMap = Maps.newLinkedHashMap();

    public ErrorInfo addErrorMap(ErrorLanguage errorLanguage, String errorMessage) {
        Map<ErrorLanguage, String> errMap = Maps.newLinkedHashMap();
        errMap.put(errorLanguage, errorMessage);
        if (errorMap != null) {
            errMap.putAll(errorMap);
        }
        this.errorMap = errMap;
        return this;
    }

    public enum ErrorLanguage {
        ZH_CN, US_EN
    }

    public String toJson() {
        ErrorLanguage language = this.errorLanguage;
        if (language == null) {
            this.errorLanguage = ErrorLanguage.ZH_CN;
        }
        addErrorMap(this.errorLanguage, this.errorMessage);
        return JSONUtil.toJsonStr(this, JSONConfig.create().setIgnoreNullValue(false));
    }
}
