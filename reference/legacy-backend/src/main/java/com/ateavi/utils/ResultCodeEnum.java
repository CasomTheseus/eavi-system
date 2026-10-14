package com.ateavi.utils;

/**
 * ClassName:ResultCodeEnum
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2024/4/16 15:19
 * @Version 1.0
 */

import java.util.Date;

/**
 * 统一返回结果状态信息类
 *
 */
public enum ResultCodeEnum {

    SUCCESS(200,"success"),
    USERNAME_ERROR(501,"usernameError"),
    PASSWORD_ERROR(401,"passwordError"),
    NOTLOGIN(504,"notLogin"),
    USERNAME_USED(505,"userNameUsed");

    private Integer code;
    private String message;
    private String timestamp;
    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = new Date().toString();
    }
    public Integer getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    public String getTimestamp() {return timestamp;}
}
