package com.lgphp.fastlivepush.sdk.payload;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * @author lgphp
 * @className AbstractPayload
 * @date 11/29/21 12:11
 * @description
 */
public abstract class AbstractMessagePayload implements Serializable {
    /**
     * 编码
     *
     * @param ctx     ChannelHandlerContext
     * @param payload AbstractMessagePayload
     * @param buf     ByteBuf
     */
    public abstract void pack(ChannelHandlerContext ctx, AbstractMessagePayload payload, ByteBuf buf);

    /**
     * 解码
     *
     * @param ctx ChannelHandlerContext
     * @param buf ByteBuf
     * @return AbstractMessagePayload
     */
    public abstract AbstractMessagePayload unpack(ChannelHandlerContext ctx, ByteBuf buf);

    public String string() {
        return JSONObject.toJSONString(this);
    }

    @Override
    public String toString() {
        return this.string();
    }
}
