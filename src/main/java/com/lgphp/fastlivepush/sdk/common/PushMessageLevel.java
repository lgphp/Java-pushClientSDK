package com.lgphp.fastlivepush.sdk.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lgphp
 * @className PushMessageLevel
 * @date 12/16/21 13:43
 * @description
 */
@AllArgsConstructor
@Getter
public enum PushMessageLevel {
    HIGH((byte) 0),
    MIDDLE((byte) 1),
    LOW((byte) 2);
    private final byte code;

    public static PushMessageLevel fromCode(byte code) {
        PushMessageLevel[] values = PushMessageLevel.values();
        for (PushMessageLevel v : values) {
            if (code == v.code) {
                return v;
            }
        }
        return null;
    }

}
