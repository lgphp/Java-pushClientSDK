package com.lgphp.fastlivepush.sdk.payload;

import com.lgphp.fastlivepush.sdk.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lgphp
 * @className AckMessagePayload
 * @date 12/31/21 19:26
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Slf4j
public class AckMessagePayload extends AbstractMessagePayload {

    private String messageID;
    private String appID;
    private String userID;
    private int statusCode;
    private String statusMessage;

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
            AckMessagePayload amp = (AckMessagePayload) payload;
            ByteBufUtil.writeStringForByteLength(buf, amp.getMessageID());
            ByteBufUtil.writeStringForByteLength(buf, amp.getAppID());
            ByteBufUtil.writeStringForByteLength(buf, amp.getUserID());
            buf.writeInt(amp.getStatusCode());
            ByteBufUtil.writeStringForIntLength(buf, amp.getStatusMessage());

        } catch (Exception e) {
            throw new RuntimeException("AckMessagePayload 编码异常: " + e.getMessage());
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
        try {
            AckMessagePayload ackMessagePayload = new AckMessagePayload();
            ackMessagePayload.setMessageID(ByteBufUtil.readStringForByteLength(buf));
            ackMessagePayload.setAppID(ByteBufUtil.readStringForByteLength(buf));
            ackMessagePayload.setUserID(ByteBufUtil.readStringForByteLength(buf));
            ackMessagePayload.setStatusCode(buf.readInt());
            ackMessagePayload.setStatusMessage(ByteBufUtil.readStringForIntLength(buf));
            return ackMessagePayload;
        } catch (Exception e) {
            throw new RuntimeException("AckMessagePayload unpack exception:" + e.getMessage());
        }
    }
}
