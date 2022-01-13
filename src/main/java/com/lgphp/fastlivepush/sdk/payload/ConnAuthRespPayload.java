package com.lgphp.fastlivepush.sdk.payload;

import com.lgphp.fastlivepush.sdk.common.ResponseStatusCodeEnum;
import com.lgphp.fastlivepush.sdk.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lgphp
 * @className ConnAuthRespPayload
 * @date 12/8/21 10:43
 * @description
 */
@Data
@ToString(callSuper = true)
@Slf4j
public class ConnAuthRespPayload  extends AbstractMessagePayload{

    private ResponseStatusCodeEnum status;
    private  String message;
    private  long  serverTime;
    private  short speedLimit;


    public void setResponseStatus(ResponseStatusCodeEnum status){
        this.status = status;
        this.message = status.getMessage();
        this.serverTime = System.currentTimeMillis();
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
            ConnAuthRespPayload carp = (ConnAuthRespPayload) payload;
            buf.writeInt(carp.getStatus().getCode());
            ByteBufUtil.writeStringForIntLength(buf, carp.getMessage());
            buf.writeLong(carp.getServerTime());
            buf.writeShort(carp.getSpeedLimit());
        } catch (Exception e) {
            throw new RuntimeException("ConnAuthRespPayload pack exception:" + e.getMessage());
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
            ConnAuthRespPayload carp = new ConnAuthRespPayload();
            int code = buf.readInt();
            String message = ByteBufUtil.readStringForIntLength(buf);
            long stime = buf.readLong();
            short speed = buf.readShort();
            carp.setStatus(ResponseStatusCodeEnum.fromCode(code));
            carp.setMessage(message);
            carp.setServerTime(stime);

            if (speed == 0) {
                speedLimit = 1000/1;
            }else if (speed > 1000) {
                speedLimit = 1000 / 1000;
            }else {
                speedLimit = (short) (1000 / speed);
            }
            carp.setSpeedLimit(speedLimit);
            return carp;
        } catch (Exception e) {
            throw new RuntimeException("ConnAuthRespPayload unpack exception:" + e.getMessage());
        }
    }
}
