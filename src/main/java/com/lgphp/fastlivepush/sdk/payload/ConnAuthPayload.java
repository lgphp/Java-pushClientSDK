package com.lgphp.fastlivepush.sdk.payload;

import com.lgphp.fastlivepush.sdk.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lgphp
 * @className ConnAuthPayload
 * @date 12/7/21 17:29
 * @description
 */

@Data
@ToString(callSuper = true)
@Slf4j
public class ConnAuthPayload extends AbstractMessagePayload {
    // 客户端实例序号
    private String clientInsId;
    // 商户ID
    private String merchantID;
    // AppID
    private String appID;
    // ConnectAuth Key
    private byte[] authKey;

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
            ConnAuthPayload cap = (ConnAuthPayload) payload;
            ByteBufUtil.writeStringForByteLength(buf, cap.getClientInsId());
            ByteBufUtil.writeStringForByteLength(buf, cap.getMerchantID());
            ByteBufUtil.writeStringForByteLength(buf, cap.getAppID());
            buf.writeBytes(cap.getAuthKey());
        } catch (Exception e) {
            throw new RuntimeException("ConnAuth pack Exception:" + e.getMessage());
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
            ConnAuthPayload cap = new ConnAuthPayload();
            String insID = ByteBufUtil.readStringForByteLength(buf);
            String merID = ByteBufUtil.readStringForByteLength(buf);
            String aID = ByteBufUtil.readStringForByteLength(buf);
            byte[] ak = new byte[16];
            buf.readBytes(ak);
            cap.setClientInsId(insID);
            cap.setMerchantID(merID);
            cap.setAppID(aID);
            cap.setAuthKey(ak);
            return cap;
        } catch (Exception e) {
            throw new RuntimeException("ConnAuth unpack exception:" + e.getMessage());
        }

    }
}
