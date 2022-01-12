package com.lgphp.fastlivepush.sdk.payload;

import lombok.Data;

/**
 * @author lgphp
 * @className MessagePacket
 * @date 11/28/21 15:25
 * @description
 */
@Data
public class MessagePacket {
    private byte ver;
    private short payloadType;
    private AbstractMessagePayload payload;
}
