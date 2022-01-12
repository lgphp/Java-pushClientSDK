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
    ParameterNull(10001, "parameter is null"),
    UserNotFound(10404, "user not exist"),


    MerchantNotFound(10101, "merchant not exist"),
    MerchantHasExist(10102, "merchant has exist"),
    AppNotFound(10103, "app not exist"),
    AppHasExist(10104, "app has exist"),

    AuthServerException(81000, "BizUser ConnAuth  exception"),
    AuthFail(81001, "auth failed"),
    SystemInnerError(90500, "Self Server Exception"),
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
