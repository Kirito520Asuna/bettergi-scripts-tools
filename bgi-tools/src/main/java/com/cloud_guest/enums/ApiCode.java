package com.cloud_guest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务结果代码枚举类
 *
 * @author yan
 */

@AllArgsConstructor
@Getter
public enum ApiCode {
    /**
     * OK               200 操作成功
     * VALIDATE_FAILED  400 参数检验失败
     * UNAUTHORIZED     401 暂未登录或令牌已经过期
     * FORBIDDEN        403 没有相关权限
     * NOT_FOUND        404 资源不存在
     * SIZE_OUT_LIMIT   406 文件大小超限
     * METHOD_NOT_ALLOWED 405 请求方法不支持
     * FAIL             500 操作失败
     * SERVICE_BUSYNESS 504 服务繁忙
     * SERVICE_UNREACHABLE 506 服务下线
     */
    OK(200, "操作成功"),
    VALIDATE_FAILED(400, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或令牌已经过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405,"请求方法不支持"),
    UNSUPPORTED_MEDIA_TYPE(415, "请求格式不支持"),
    SIZE_OUT_LIMIT(406,"文件大小超限"),
    FAIL(500, "操作失败"),
    LOGIN_FAIL(501, "登录失败"),
    USERNAME_PASSWORD_EORR(502, "用户名或者密码错误"),
    SERVICE_BUSYNESS(504,"服务暂不可用"),
    SERVICE_REPAIRING(505,"服务维护中"),
    SERVICE_UNREACHABLE(506, "服务下线"),
    SERVICE_CONFIG(507, "服务配置异常")
    ;
    /**
     * 返回代码
     */
    private final int code;
    /**
     * 返回信息
     */
    private final String message;
}
