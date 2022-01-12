package com.lgphp.fastlivepush.sdk.payload;

import cn.hutool.core.util.ZipUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lgphp.fastlivepush.sdk.common.NotificationClassfyEnmu;
import com.lgphp.fastlivepush.sdk.common.PushMessageLevel;
import com.lgphp.fastlivepush.sdk.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author lgphp
 * @className PushMessagePayload
 * @date 12/9/21 11:03
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Slf4j
public class PushMessagePayload extends AbstractMessagePayload {
    private static int needCompressSize = 1024;
    private String messageID;
    // 通知分类
    private NotificationClassfyEnmu classifier;
    private String fromMerchantID;
    private String fromAppID;
    private PushMessageLevel messagePriority;
    private String toUID;
    private long serverTime;
    private MessageBody messageBody;
    // 以下字段发送回执用
    private String clientInstanceID;
    private String pushGateActorServer;

    @Data
    public static class MessageBody implements Serializable {
        private String title;
        private String body;
        private Map<String, String> data;
    }

    /**
     * 编码
     *
     * @param payload
     * @param buf
     * @return
     */
    @Override
    public void pack(ChannelHandlerContext ctx, AbstractMessagePayload payload, ByteBuf buf) {
        try {
            PushMessagePayload pmp = (PushMessagePayload) payload;
            ByteBufUtil.writeStringForByteLength(buf, pmp.getMessageID());
            buf.writeByte(pmp.getClassifier().getCode());
            ByteBufUtil.writeStringForByteLength(buf, pmp.getFromMerchantID());
            ByteBufUtil.writeStringForByteLength(buf, pmp.getFromAppID());
            byte messageFlag = 2;
            byte[] finallyBody;
            // 获取加密实例
            AES aesCBC = ctx.channel().attr(ChannelAttrKey.UserContextKey).get().getAesCBC();
            // 加密数据
            finallyBody = aesCBC.encrypt(JSON.toJSONString(pmp.getMessageBody()));
            if (finallyBody.length > PushMessagePayload.needCompressSize) {
                messageFlag = 3;
                // 压缩
                finallyBody = ZipUtil.gzip(finallyBody);
            }
            buf.writeByte(messageFlag);
            buf.writeByte(pmp.getMessagePriority().getCode());
            ByteBufUtil.writeStringForByteLength(buf, pmp.getToUID());
            buf.writeBytes(finallyBody);
        } catch (Exception e) {
            throw new RuntimeException("PushMessage 编码异常: " + e.getMessage());
        }
    }


    /**
     * 手动编码
     *
     * @param payload
     * @param buf
     * @param encMsgKey
     */
    public void encode(AbstractMessagePayload payload, ByteBuf buf, byte[] encMsgKey, byte[] iv) {
        try {
            PushMessagePayload pmp = (PushMessagePayload) payload;
            ByteBufUtil.writeStringForByteLength(buf, pmp.getMessageID());
            buf.writeByte(pmp.getClassifier().getCode());
            ByteBufUtil.writeStringForByteLength(buf, pmp.getFromMerchantID());
            ByteBufUtil.writeStringForByteLength(buf, pmp.getFromAppID());
            byte messageFlag = 2;
            byte[] finallyBody;
            // 获取加密key
            AES aesCBC = new AES(Mode.CBC, Padding.PKCS5Padding, encMsgKey, iv);
            // 加密数据
            finallyBody = aesCBC.encrypt(JSON.toJSONString(pmp.getMessageBody()));
            if (finallyBody.length > PushMessagePayload.needCompressSize) {
                messageFlag = 3;
                // 压缩
                finallyBody = ZipUtil.gzip(finallyBody);
            }
            buf.writeByte(messageFlag);
            buf.writeByte(pmp.getMessagePriority().getCode());
            ByteBufUtil.writeStringForByteLength(buf, pmp.getToUID());
            buf.writeBytes(finallyBody);
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * 解码
     *
     * @param buf
     * @return
     */
    @Override
    public AbstractMessagePayload unpack(ChannelHandlerContext ctx, ByteBuf buf) {
        try {
            PushMessagePayload pmp = new PushMessagePayload();
            String mid = ByteBufUtil.readStringForByteLength(buf);
            byte classfy = buf.readByte();
            String merchantID = ByteBufUtil.readStringForByteLength(buf);
            String appID = ByteBufUtil.readStringForByteLength(buf);
            byte flag = buf.readByte();
            byte priority = buf.readByte();
            String uid = ByteBufUtil.readStringForByteLength(buf);
            byte[] body = new byte[buf.readableBytes()];
            buf.readBytes(body);
            AES aesCBC = ctx.channel().attr(ChannelAttrKey.UserContextKey).get().getAesCBC();
            if (flag == 2) {
                //只加密
                if (Objects.isNull(aesCBC)) throw new IllegalArgumentException("PushMessage 解码异常: 获取不到 AES实例");
                body = aesCBC.decrypt(body);
            }
            if (flag == 3) {
                // 压缩且加密 ，先解压缩
                body = ZipUtil.unGzip(body);
                // 解密
                if (Objects.isNull(aesCBC)) throw new IllegalArgumentException("PushMessage 解码异常: 获取不到 AES实例");
                body = aesCBC.decrypt(body);
            }
            pmp.setMessageID(mid);
            pmp.setClassifier(NotificationClassfyEnmu.fromCode(classfy));
            pmp.setFromMerchantID(merchantID);
            pmp.setFromAppID(appID);
            pmp.setMessagePriority(PushMessageLevel.fromCode(priority));
            pmp.setToUID(uid);
            pmp.setMessageBody(JSONObject.parseObject(body, MessageBody.class));
            return pmp;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("PushMessage 解码异常:" + e.getMessage());
        }

    }


}
