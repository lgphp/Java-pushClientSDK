package com.lgphp.fastlivepush.sdk.connector;

import cn.hutool.core.lang.UUID;
import com.lgphp.fastlivepush.sdk.common.PayloadType;
import com.lgphp.fastlivepush.sdk.entity.AppInfo;
import com.lgphp.fastlivepush.sdk.entity.PushGateAddress;
import com.lgphp.fastlivepush.sdk.entity.PushNotification;
import com.lgphp.fastlivepush.sdk.listener.PushInitializedListener;
import com.lgphp.fastlivepush.sdk.listener.PushNotificationStatusListener;
import com.lgphp.fastlivepush.sdk.payload.ConnAuthPayload;
import com.lgphp.fastlivepush.sdk.util.KeyManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description TODO
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
@Slf4j
public class NettyClient {
    private AppInfo appInfo;
    private NettyClientHandler nettyClientHandler;

    public NettyClient(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public void connect(PushGateAddress pushGateAddress, PushInitializedListener pushInitializedListener) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        nettyClientHandler = new NettyClientHandler(appInfo);
        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 0));
                    pipeline.addLast(nettyClientHandler);
                }
            });
        try {
            String pushGateAddressString = String.format("%s:%s", pushGateAddress.getIp(), pushGateAddress.getPort());
            final ChannelFuture pushChannelFuture = bootstrap.connect(pushGateAddress.getIp(), pushGateAddress.getPort()).sync();
            pushChannelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    pushInitializedListener.onInitialized(200, String.format("Connection of FastLivePush: %s success",pushGateAddressString));
                    // send auth
                    sendAuthConnPacket(pushChannelFuture);
                } else {
                    pushInitializedListener.onInitialized(400, String.format("Connection of FastLivePush: %s failed",pushGateAddressString));
                    workerGroup.shutdownGracefully();
                }
            });
            // pushChannelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendAuthConnPacket(ChannelFuture pushChannelFuture) {
        ByteBuf buf = pushChannelFuture.channel().alloc().buffer();
        if (pushChannelFuture.channel().isActive()) {
            byte[] keys = KeyManager.stringKey2Byte(appInfo.getAppKey());
            ConnAuthPayload connAuthPayload = new ConnAuthPayload();
            connAuthPayload.setClientInsId(UUID.randomUUID().toString());
            connAuthPayload.setMerchantID(appInfo.getMerchantId());
            connAuthPayload.setAppID(appInfo.getAppId());
            connAuthPayload.setAuthKey(KeyManager.getAuthKey(keys));
            buf.writeInt(0);
            buf.writeByte(1);
            buf.writeShort(PayloadType.ConnAuth.getCode());
            connAuthPayload.pack(null, connAuthPayload, buf);
            int pktLen = buf.writerIndex() - 4;
            buf.setInt(0, pktLen);
            pushChannelFuture.channel().writeAndFlush(buf);
        } else {
            buf.release();
        }
    }

    public void sendPushNotification(PushNotification pushNotification) {
        nettyClientHandler.sendPushNotification(pushNotification);
    }

    public void registerMsgStateCallback(PushNotificationStatusListener pushNotificationStatusListener) {
        nettyClientHandler.registerMsgStateCallback(pushNotificationStatusListener);
    }

    public void registerPushInitializedCallback(PushInitializedListener pushInitializedListener) {
        nettyClientHandler.registerPushInitializedCallback(pushInitializedListener);
    }
}