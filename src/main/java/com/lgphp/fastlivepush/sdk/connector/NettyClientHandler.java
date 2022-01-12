package com.lgphp.fastlivepush.sdk.connector;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.lgphp.fastlivepush.sdk.common.NotificationClassfyEnmu;
import com.lgphp.fastlivepush.sdk.common.PayloadType;
import com.lgphp.fastlivepush.sdk.common.ResponseStatusCodeEnum;
import com.lgphp.fastlivepush.sdk.entity.AppInfo;
import com.lgphp.fastlivepush.sdk.entity.NotificationAck;
import com.lgphp.fastlivepush.sdk.entity.PushNotification;
import com.lgphp.fastlivepush.sdk.listener.PushInitializedListener;
import com.lgphp.fastlivepush.sdk.listener.PushNotificationStatusListener;
import com.lgphp.fastlivepush.sdk.payload.AckMessagePayload;
import com.lgphp.fastlivepush.sdk.payload.ConnAuthRespPayload;
import com.lgphp.fastlivepush.sdk.payload.HeartBeatPayload;
import com.lgphp.fastlivepush.sdk.payload.PushMessagePayload;
import com.lgphp.fastlivepush.sdk.util.KeyManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Description NettyClientHandler
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private AppInfo appInfo;
    private ChannelHandlerContext channelHandlerContext;
    private PushInitializedListener pushInitializedListener;
    private PushNotificationStatusListener pushNotificationStatusListener;

    public NettyClientHandler(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("server: {} is active", ctx.channel().remoteAddress());
        channelHandlerContext = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            int pktLen = byteBuf.readInt();
            byte ver = byteBuf.readByte();
            short payloadTypeCode = byteBuf.readShort();
            if (payloadTypeCode == PayloadType.ConnAuthResp.getCode()) {
                ConnAuthRespPayload carp = (ConnAuthRespPayload) new ConnAuthRespPayload().unpack(ctx, byteBuf);
                if (carp.getStatus() == ResponseStatusCodeEnum.OK) {
                    pushInitializedListener.onInitialized(200, "Authentication of FastLivePush connection success");

                    // 定时发送sendHeartbeatPacket
                    ctx.executor().scheduleAtFixedRate(
                        new Runnable(){
                            @Override
                            public void run() {
                                log.debug("sendHeartbeatPacket");
                                sendHeartbeatPacket(ctx);
                            }
                        }, 0, 15, TimeUnit.SECONDS);
                } else {
                    log.warn("认证失败 : code:{}, reason:{}", carp.getStatus().getCode(), carp.getMessage());
                    pushInitializedListener.onInitialized(400, String.format("Authentication of FastLivePush connection failed: %s",carp.getMessage()));
                }
            }else if (payloadTypeCode == PayloadType.MessageACK.getCode()) {
                AckMessagePayload ackMessagePayload = (AckMessagePayload)new AckMessagePayload().unpack(ctx, byteBuf);
                NotificationAck ack = NotificationAck.builder().appID(ackMessagePayload.getAppID()).messageID(ackMessagePayload.getMessageID()).userID(ackMessagePayload.getUserID())
                                                .statusCode(ackMessagePayload.getStatusCode()).statusMessage(ackMessagePayload.getStatusMessage()).build();
                pushNotificationStatusListener.onReceived(ack);
            }else {
                log.warn("Unknown Payload");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            byteBuf.skipBytes(byteBuf.readableBytes());
            ReferenceCountUtil.release(byteBuf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("server: {} exceptionCaught: {}" , ctx.channel().remoteAddress(), cause.getLocalizedMessage());
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("server: {} is inactive", ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        switch (event.state()) {
            case READER_IDLE:
                break;
            case WRITER_IDLE:
                //sendHeartbeatPacket(ctx);
                break;
            case ALL_IDLE:
                break;
            default:
                super.userEventTriggered(ctx, evt);
                break;
        }
    }

    public void registerPushInitializedCallback(PushInitializedListener callback) {
        if (callback != null) {
            pushInitializedListener = callback;
        }
    }

    public void registerMsgStateCallback(PushNotificationStatusListener callback) {
        if (callback != null) {
            pushNotificationStatusListener = callback;
        }
    }

    public void sendPushNotification(PushNotification pushNotification) {
        ByteBuf buf = channelHandlerContext.channel().alloc().buffer();
        if (channelHandlerContext.channel().isActive()) {
            byte[] keys = KeyManager.stringKey2Byte(appInfo.getAppKey());
            buf.writeInt(0);
            buf.writeByte(1);
            buf.writeShort(PayloadType.PushMessage.getCode());

            PushMessagePayload pushMessagePayload = new PushMessagePayload();
            pushMessagePayload.setMessageID(UUID.fastUUID().toString());
            pushMessagePayload.setClassifier(NotificationClassfyEnmu.PUSH);
            pushMessagePayload.setFromAppID(appInfo.getAppId());
            pushMessagePayload.setFromMerchantID(appInfo.getMerchantId());
            pushMessagePayload.setToUID(pushNotification.getToUID());
            pushMessagePayload.setMessagePriority(pushNotification.getMessagePriority());

            PushMessagePayload.MessageBody messageBody = new PushMessagePayload.MessageBody();
            messageBody.setTitle(pushNotification.getMessageBody().getTitle());
            messageBody.setBody(pushNotification.getMessageBody().getBody());
            messageBody.setData(pushNotification.getMessageBody().getData());
            pushMessagePayload.setMessageBody(messageBody);
            pushMessagePayload.encode(pushMessagePayload, buf, KeyManager.getMsgEncKey(keys), KeyManager.getMsgEncAesIV(keys));
            int pktLen = buf.writerIndex() - 4;
            buf.setInt(0, pktLen);
            channelHandlerContext.channel().writeAndFlush(buf).addListener((ChannelFutureListener) ch -> {
                if (ch.isSuccess()) {
                    log.debug("sendPushMessage success-> {}", JSONObject.toJSONString(pushMessagePayload));
                }
            });
        } else {
            buf.release();
        }
    }

    private void sendHeartbeatPacket(ChannelHandlerContext ctx) {
        HeartBeatPayload payload = new HeartBeatPayload();
        payload.setZero((byte) 0);
        ByteBuf buf = ctx.channel().alloc().buffer();
        if (ctx.channel().isActive()) {
            buf.writeInt(0);
            buf.writeByte(1);
            buf.writeShort(PayloadType.HeartBeat.getCode());
            payload.pack(ctx, payload, buf);
            int pktLen = buf.writerIndex() - 4;
            buf.setInt(0, pktLen);
            ctx.channel().writeAndFlush(buf);
        } else {
            buf.release();
        }
    }
}