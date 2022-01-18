package com.lgphp.fastlivepush.sdk;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.lgphp.fastlivepush.sdk.common.PayloadType;
import com.lgphp.fastlivepush.sdk.handler.BizProcessorHandler;
import com.lgphp.fastlivepush.sdk.entity.AppInfo;
import com.lgphp.fastlivepush.sdk.entity.PushGateAddress;
import com.lgphp.fastlivepush.sdk.entity.PushNotification;
import com.lgphp.fastlivepush.sdk.listener.PushInitializedListener;
import com.lgphp.fastlivepush.sdk.listener.PushNotificationStatusListener;
import com.lgphp.fastlivepush.sdk.payload.ConnAuthPayload;
import com.lgphp.fastlivepush.sdk.util.KeyManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description FastLivePushClient
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
@Data
@Slf4j
public class FastLivePushClient {
    // clientId
    private String clientId;

    // appInfo
    private AppInfo appInfo;

    // baseUrl
    private String baseUrl;

    // pushGateAddress
    private PushGateAddress pushGateAddress;

    // HttpClient
    private FastLivePushHttpClient fastLivePushHttpClient;


    // Netty config
    private NioEventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private ChannelFuture pushChannelFuture;
    private BizProcessorHandler bizProcessorHandler;

    // listener
    private PushInitializedListener pushInitializedListener;
    private PushNotificationStatusListener pushNotificationStatusListener;

    // reconnect
    private AtomicInteger _reconnectCnt = new AtomicInteger(0);
    private AtomicBoolean _isReconnecting = new AtomicBoolean(false);
    private SingleThreadEventLoop _reconnectLoop = new DefaultEventLoop();

    public void addPushInitializedListener(PushInitializedListener pushInitializedListener) {this.pushInitializedListener = pushInitializedListener; }
    public void addPushNotificationStatusListener(PushNotificationStatusListener pushNotificationStatusListener) {this.pushNotificationStatusListener = pushNotificationStatusListener; }

    public FastLivePushClient(AppInfo appInfo, String baseUrl) {
        Objects.requireNonNull(appInfo,"AppInfo is required");
        Objects.requireNonNull(appInfo.getAppId(),"AppInfo.appId is required");
        Objects.requireNonNull(appInfo.getAppKey(),"AppInfo.appKey is required");
        Objects.requireNonNull(appInfo.getMerchantId(),"AppInfo.merchantId is required");

        this.appInfo = appInfo;
        this.baseUrl = baseUrl;
    }

    private PushGateAddress selectPushGateAddress() {
        try {
            JSONObject jsonObject = new JSONObject().fluentPut("appId", appInfo.getAppId());
            String response =
                fastLivePushHttpClient.postByJSON(String.format("%s/%s", baseUrl, "biz/push/list"), jsonObject.toJSONString());
            if (Objects.nonNull(response)) {
                JSONObject retJSON = JSONObject.parseObject(response);
                String[] data = retJSON.getJSONArray("data").toArray(new String[] {});
                if (Objects.nonNull(data)) {
                    String address = data[0];
                    String[] split = address.split(":");
                    PushGateAddress pushGateAddress = new PushGateAddress(split[0], Integer.valueOf(split[1]));
                    return pushGateAddress;
                }
            }
        }catch (Exception e) {
            log.warn("Selection of FastLivePush PushGateAddress failed: " + e);
        }
        pushInitializedListener.onInitialized(503, String.format("Selection of FastLivePush PushGateAddress failed: %s","PushGateAddress is Null"));
        return null;
    }

    public void buildConnect() {
        Objects.requireNonNull(appInfo,"AppInfo is required");
        Objects.requireNonNull(pushInitializedListener,"PushInitializedListener is required");
        Objects.requireNonNull(pushNotificationStatusListener,"PushNotificationStatusListener is required");

        this.clientId = UUID.randomUUID().toString();
        this.fastLivePushHttpClient = new FastLivePushHttpClient(appInfo);
        this.pushGateAddress = selectPushGateAddress();
        Objects.requireNonNull(pushGateAddress,"PushGateAddress is required");
        this.connect();
    }

    public void connect() {
        String pushGateAddressString = String.format("%s:%s", pushGateAddress.getIp(), pushGateAddress.getPort());
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bizProcessorHandler = new BizProcessorHandler(this);
        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 0));
                    pipeline.addLast(bizProcessorHandler);
                }
            });
        try {
            pushChannelFuture = bootstrap.connect(pushGateAddress.getIp(), pushGateAddress.getPort()).sync();
            pushChannelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    pushInitializedListener.onInitialized(200, String.format("Connection of FastLivePush: %s success",pushGateAddressString));
                    // send auth
                    sendAuthConnPacket();
                } else {
                    pushInitializedListener.onInitialized(503, String.format("Connection of FastLivePush: %s failed",pushGateAddressString));
                    // reconnect when connect failed
                    reconnect();
                }
            });
            // pushChannelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            pushInitializedListener.onInitialized(503, String.format("Connection of FastLivePush: %s failed",pushGateAddressString));
        }
    }

    public void reconnect() {
        if (_isReconnecting.get()) return;
        _isReconnecting.set(true);

        _reconnectLoop.schedule(() -> {
            log.info("Reconnecting to FastLivePush");
            while (_reconnectCnt.get() <= 61) {
                try {
                    if (_reconnectCnt.get() > 60) {
                        pushInitializedListener.onInitialized(504, String.format("ReConnection of FastLivePush failed exceed: {} times",_reconnectCnt.get()));
                        shutdownAllEventLoop();
                        return;
                    }
                    _reconnectCnt.incrementAndGet();
                    connect();
                    TimeUnit.MILLISECONDS.sleep(1000 * 4 + (_reconnectCnt.get() + 1));
                } catch (Exception e){
                    log.warn("ReConnection of FastLivePush failed: " + e);
                }
            }

        },10, TimeUnit.SECONDS);
    }

    public void shutdownAllEventLoop() {
        log.warn("FastLivePushClient is shutdown.");
        _reconnectLoop.shutdownGracefully(2, 5, TimeUnit.SECONDS);
        workerGroup.shutdownGracefully(1, 1, TimeUnit.SECONDS);
    }

    private void sendAuthConnPacket() {
        ByteBuf buf = this.pushChannelFuture.channel().alloc().buffer();
        if (this.pushChannelFuture.channel().isActive()) {
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
            this.pushChannelFuture.channel().writeAndFlush(buf);
        } else {
            buf.release();
        }
    }

    public void sendPushNotification(PushNotification pushNotification) {
        this.bizProcessorHandler.sendPushNotification(pushNotification);
    }
}