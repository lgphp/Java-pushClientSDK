package com.lgphp.fastlivepush.sdk.payload;

import com.lgphp.fastlivepush.sdk.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lgphp
 * @className TokenUploadPayload
 * @date 12/27/21 13:11
 * @description
 */
@Data
@ToString(callSuper = true)
@Slf4j
public class TokenUploadPayload extends AbstractMessagePayload {
    private String appID;
    private String userID;
    private byte spChannel;
    private String pushToken;
    private byte classifier;


    /**
     * 编码
     *
     * @param ctx     ChannelHandlerContext
     * @param payload AbstractMessagePayload
     * @param buf     ByteBuf
     */
    @Override
    public void pack(ChannelHandlerContext ctx, AbstractMessagePayload payload, ByteBuf buf) {
        log.info("无需编码");
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
        TokenUploadPayload tupp = new TokenUploadPayload();
        try {
            String appid = ByteBufUtil.readStringForByteLength(buf);
            String uid = ByteBufUtil.readStringForByteLength(buf);
            byte spch = buf.readByte();
            String token = ByteBufUtil.readStringForIntLength(buf);
            byte classfy = buf.readByte();
            tupp.setAppID(appid);
            tupp.setUserID(uid);
            tupp.setSpChannel(spch);
            tupp.setPushToken(token);
            tupp.setClassifier(classfy);
        } catch (Exception e) {
            throw new RuntimeException("TokenUploadPayload 解码异常");
        }
        return tupp;
    }
}
