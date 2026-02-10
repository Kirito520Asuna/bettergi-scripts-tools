package com.cloud_guest.result;


import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 通用返回对象封装类
 *
 * @author yan
 */
@Schema(description = "通用返回对象")
@Data
@NoArgsConstructor
@AllArgsConstructor @Accessors(chain = true)
@JsonView(BasicJsonView.BaseView.class)
public class Result<T> implements Serializable, AbsResult {
    private static final long serialVersionUID = 8831550221241490078L;

    /**
     * 返回代码
     */
    @JsonProperty("code")
    @Schema(description = "返回代码", example = "200")
    @JsonView(BasicJsonView.BaseView.class)
    private Integer code;
    /**
     * 返回信息
     */
    @JsonProperty("message")
    @Schema(description = "返回信息", example = "操作成功")
    @JsonView(BasicJsonView.BaseView.class)
    private String message;

    /**
     *
     */
    @JsonProperty("resultTime")
    @Schema(description = "响应时间")
    @JsonView(BasicJsonView.BaseView.class)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Long resultTime = System.currentTimeMillis();
    /**
     * 返回数据
     */
    @JsonProperty("data")
    @JsonView(BasicJsonView.BaseView.class)
    @Schema(description = "返回数据")
    private T data;

    /**
     * 操作成功
     *
     * @param <T> 泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> ok() {
        return result(ApiCode.OK);
    }

    /**
     * 操作成功
     *
     * @param data 返回数据
     * @param <T>  泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> ok(T data) {
        return result(ApiCode.OK, data);
    }

    /**
     * 操作成功
     *
     * @param data    返回数据
     * @param message 返回信息
     * @param <T>     泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> ok(T data, String message) {
        return result(ApiCode.OK.getCode(), message, data);
    }

    /**
     * 操作失败
     *
     * @param <T> 泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> fail() {
        return result(ApiCode.FAIL);
    }

    /**
     * 操作失败
     *
     * @param message 返回信息
     * @param <T>     泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> fail(String message) {
        return result(ApiCode.FAIL.getCode(), message);
    }

    /**
     * 参数检验失败
     *
     * @param <T> 泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> validateFail() {
        return result(ApiCode.VALIDATE_FAILED);
    }

    /**
     * 参数检验失败
     *
     * @param message 返回信息
     * @param <T>     泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> validateFail(String message) {
        return result(ApiCode.VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 暂未登录或令牌已经过期
     *
     * @param <T> 泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> unauthorized() {
        return result(ApiCode.UNAUTHORIZED);
    }

    /**
     * 暂未登录或令牌已经过期
     *
     * @param message 返回信息
     * @param <T>     泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> unauthorized(String message) {
        return result(ApiCode.UNAUTHORIZED.getCode(), message);
    }

    /**
     * 没有相关权限
     *
     * @param <T> 泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> forbidden() {
        return result(ApiCode.FORBIDDEN);
    }

    /**
     * 没有相关权限
     *
     * @param message 返回信息
     * @param <T>     泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> forbidden(String message) {
        return result(ApiCode.FORBIDDEN.getCode(), message);
    }

    /**
     * 返回通用返回对象
     *
     * @param apiCode 结果代码
     * @param <T>     泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> result(ApiCode apiCode) {
        return result(apiCode.getCode(), apiCode.getMessage());
    }

    /**
     * 返回通用返回对象
     *
     * @param apiCode 结果代码
     * @param data    返回数据
     * @param <T>     泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> result(ApiCode apiCode, T data) {
        return result(apiCode.getCode(), apiCode.getMessage(), data);
    }

    /**
     * 返回通用返回对象
     *
     * @param code    返回代码
     * @param message 返回信息
     * @param <T>     泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> result(Integer code, String message) {
        return result(code, message, null);
    }

    /**
     * 返回通用返回对象
     *
     * @param code    返回代码
     * @param message 返回信息
     * @param data    返回数据
     * @param <T>     泛型
     * @return 通用返回对象
     */
    public static <T> Result<T> result(Integer code, String message, T data) {
        return new Result<>(code, message,System.currentTimeMillis(), data);
    }
    public boolean validateOk() {
        return Integer.valueOf(200).equals(this.code);
    }

}
