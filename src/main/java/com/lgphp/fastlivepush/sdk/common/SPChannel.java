package com.lgphp.fastlivepush.sdk.common;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lgphp
 * @className SPChannel
 * @date 11/29/21 11:40
 * @description SP通道枚举
 *
 * 100 - 127 给短信子通道预留，请不要占用
 */

@AllArgsConstructor
public enum SPChannel {

    // 100 - 127 给短信子通道预留，请不要占用
    VIRTUAL((byte) 0, "Virtual Channel"),
    APNS((byte) 10, "PUSH-Apple APNS"),
    FCM((byte) 11, "PUSH-FCM"),
    HCM((byte) 12, "PUSH-HUAWEI"),
    PUSHKIT((byte) 60, "APPLE-VOIP-PUSHKIT"),
    HUAWEIPUSHKIT((byte) 61, "HW-VOIP-PUSHKIT"),
    SMS((byte) 30, "SMS"),
    ;
    private final byte code;
    private final String message;


    public byte getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static List<Byte> codes() {
        List<Byte> code = new ArrayList<>();
        for (SPChannel item : SPChannel.values()) {
            code.add(item.getCode());
        }
        return code;
    }


    public static SPChannel fromCode(byte code) {
        for (SPChannel item : SPChannel.values()) {
            if (code == item.getCode()) {
                return item;
            }
        }
        return null;
    }


}
