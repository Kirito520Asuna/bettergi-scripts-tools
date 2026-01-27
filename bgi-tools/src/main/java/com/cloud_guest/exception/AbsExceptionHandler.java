package com.cloud_guest.exception;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalConfigException;
import com.cloud_guest.exception.exceptions.GlobalCustomException;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.result.Result;
import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


/**
 * @Author yan
 * @Date 2024/9/27 上午1:37:08
 * @Description
 */
public interface AbsExceptionHandler {
    default boolean isProd() {
        Environment env = SpringUtil.getBean(Environment.class);
        return ObjectUtil.equals(env.getProperty("spring.profiles.active"), "prod");
    }
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    @JsonView(BasicJsonView.BaseView.class)
    default Result notValidExceptionHandler(HttpServletRequest req, MethodArgumentNotValidException  e) {
        ApiCode validateFailed = ApiCode.VALIDATE_FAILED;
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        // 打印异常堆栈信息
        e.printStackTrace();
        return Result.result(validateFailed.getCode(),errorMessage);
    }


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @JsonView(BasicJsonView.BaseView.class)
    default Result exceptionHandler(HttpServletRequest req, Exception e) {
        // 打印异常堆栈信息
        e.printStackTrace();
        // 默认错误信息
        String errMessage = "系统繁忙";
        // 创建失败结果对象
        Result result = Result.fail();

        // 默认错误码和错误信息
        Integer code = ApiCode.FAIL.getCode();
        String message = ApiCode.FAIL.getMessage();
        // 判断异常类型
        if (e instanceof GlobalConfigException) {
            // 如果是全局配置异常
            ApiCode API_CODE = isProd() ? ApiCode.SERVICE_REPAIRING : ApiCode.SERVICE_CONFIG;
            code = API_CODE.getCode();
            message = API_CODE.getMessage();
        } else if (e instanceof GlobalCustomException) {
            // 如果是全局自定义异常
            GlobalCustomException exception = (GlobalCustomException) e;
            code = exception.getCode();
            message = exception.getMessage();
        } else if (e instanceof GlobalException) {
            // 如果是全局异常
            GlobalException exception = (GlobalException) e;
            code = exception.getCode();
            message = exception.getMessage();
        } else {
            // 如果是其他异常
            message = isProd() ? errMessage : e.getMessage();
        }
        // 设置错误码和错误信息
        result.setCode(code);
        result.setMessage(message);
        // 返回结果
        return result;
    }

}
