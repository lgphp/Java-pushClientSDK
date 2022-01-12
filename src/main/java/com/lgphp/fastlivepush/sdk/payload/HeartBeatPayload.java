package com.lgphp.fastlivepush.sdk.payload;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lgphp
 * @className HeartBeatPayload
 * @date 11/30/21 15:37
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HeartBeatPayload extends AbstractMessagePayload {
    private byte zero;

    /**
     * 编码
     *
     * @param ctx     ChannelHandlerContext
     * @param payload AbstractMessagePayload
     * @param buf     ByteBuf
     */
    @Override
    public void pack(ChannelHandlerContext ctx, AbstractMessagePayload payload, ByteBuf buf) {
        try {
            HeartBeatPayload hbp = (HeartBeatPayload) payload;
            buf.writeByte(0);
        } catch (Exception e) {
            throw new RuntimeException("HeartBeatPayload pack exception:" + e.getMessage());
        }
    }

    /**
     * 解码
     *
     * @param ctx ChannelHandlerContext
     * @param buf ByteBuf
     * @return AbstractMessagePayload
     */
    @Override
    public AbstractMessagePayload unpack(ChannelHandlerContext ctx, ByteBuf buf) {
        return null;
    }
}
