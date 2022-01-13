package com.lgphp.fastlivepush.sdk.common;

import lombok.AllArgsConstructor;

/**
 * @author lgphp
 * @className ResponseStatusCodeEnum
 * @date 11/28/21 15:24
 * @description
 */
@AllArgsConstructor
public enum ResponseStatusCodeEnum {
    OK(0, "success"),
    ParameterNull(10001, "Parameter is null"),
    UserNotFound(10404, "User not exist"),


    MerchantNotFound(10101, "MerchantID not exist"),
    MerchantHasExist(10102, "MerchantID has exist"),
    PhoneNumberHasExist(10103, "PhoneNumber has exist"),
    AppNotFound(10108, "App not exist"),
    AppHasExist(10104, "App has exist"),
    ClassifierNotMatchSpChannel(10105, "Classifier Not Match SPChannel"),
    OutofMaxClinetValue(10106, "Out of MaxClient Value"),
    OutofMaxSpeedRate(10107, "Out of MaxSpeedRate Value"),

    AuthServerException(81000, "BizUser ConnAuth  exception"),
    AuthFail(81001, "Auth failed"),
    SystemInnerError(90500, "Server Exception"),


    // 长链接认证异常相关
    ConnAuth_AuthKeyNotMatch(19001, "Auth Key Error"),
    ConnAuth_MaxConnectionExceeded(19002, "Max Connection Exceeded"),


    //DB 相关操作异常

    DBUpdateFailed(50101, "DB Update Failed"),
    ;


    private int code;
    private String Message;


    /**
     * 根据code获得枚举类型
     *
     * @param code
     * @return ResponseStatusCodeEnum
     */
    public static ResponseStatusCodeEnum fromCode(int code) {
        ResponseStatusCodeEnum[] values = ResponseStatusCodeEnum.values();
        for (ResponseStatusCodeEnum value : values) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("返回状态码不合法");
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
