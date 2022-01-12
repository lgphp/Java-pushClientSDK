package com.lgphp.fastlivepush.sdk.common;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lgphp
 * @className SPChannel
 * @date 11/29/21 11:40
 * @description SP通道枚举
 */

@AllArgsConstructor
public enum NotificationClassfyEnmu {
    PUSH((byte) 1, "PUSH NOTIFICATION"),
    SMS((byte) 2, "SMS MESSAGE"),
    EMAIL((byte) 3, "EMAIL"),
    INNER((byte) 4, "INNER MESSAGE"),
    VOIP((byte) 5, "VOIP MESSAGE")
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
        for (NotificationClassfyEnmu item : NotificationClassfyEnmu.values()) {
            code.add(item.getCode());
        }
        return code;
    }


    public static NotificationClassfyEnmu fromCode(byte code) {
        for (NotificationClassfyEnmu item : NotificationClassfyEnmu.values()) {
            if (code == item.getCode()) {
                return item;
            }
        }
        return null;
    }


}
