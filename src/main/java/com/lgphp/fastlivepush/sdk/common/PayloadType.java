package com.lgphp.fastlivepush.sdk.common;

import com.lgphp.fastlivepush.sdk.payload.*;
import lombok.AllArgsConstructor;

/**
 * @author lgphp
 * @className PayloadType
 * @date 11/30/21 15:40
 * @description
 */
@AllArgsConstructor
public enum PayloadType {
    HeartBeat((short) 10000, HeartBeatPayload.class),
    ConnAuth((short) 20000, ConnAuthPayload.class),
    ConnAuthResp((short) 20001, ConnAuthRespPayload.class),
    PushMessage((short) 30000, PushMessagePayload.class),
    MessageACK((short) 30001, AckMessagePayload.class),
    TokenUpload((short) 40000, TokenUploadPayload.class),
    ;
    private final short code;
    private final Class<? extends AbstractMessagePayload> cls;


    /**
     * 根据clazz获取code
     *
     * @param cls
     * @return
     */
    public static short getCode(Class<? extends AbstractMessagePayload> cls) {
        PayloadType[] values = PayloadType.values();
        for (PayloadType v : values) {
            if (cls.equals(v.cls)) {
                return v.code;
            }
        }
        return 0;
    }

    /**
     * 根据code获得枚举
     *
     * @param code
     * @return
     * PayloadType
     */
    public static PayloadType fromCode(short code) {
        PayloadType[] values = PayloadType.values();
        for (PayloadType v : values) {
            if (code == v.code) {
                return v;
            }
        }
        return null;
    }


    public short getCode() {
        return this.code;
    }

    public Class<?> getCls() {
        return this.cls;
    }
}
