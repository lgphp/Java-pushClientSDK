package com.lgphp.fastlivepush.sdk;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.lgphp.fastlivepush.sdk.connector.NettyClient;
import com.lgphp.fastlivepush.sdk.entity.AppInfo;
import com.lgphp.fastlivepush.sdk.entity.PushGateAddress;
import com.lgphp.fastlivepush.sdk.entity.PushNotification;
import com.lgphp.fastlivepush.sdk.listener.PushInitializedListener;
import com.lgphp.fastlivepush.sdk.listener.PushNotificationStatusListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

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

    // 客户端信息
    private AppInfo appInfo;

    // baseUrl
    private String baseUrl;

    // PushGateAddress
    private PushGateAddress pushGateAddress;

    // HttpClient
    private FastLivePushHttpClient fastLivePushHttpClient;

    // NettyClient
    private NettyClient nettyClient;

    // listener
    private PushInitializedListener pushInitializedListener;
    private PushNotificationStatusListener pushNotificationStatusListener;


    public void addPushInitializedListener(PushInitializedListener pushInitializedListener) {this.pushInitializedListener = pushInitializedListener; }
    public void addPushNotificationStatusListener(PushNotificationStatusListener pushNotificationStatusListener) {this.pushNotificationStatusListener = pushNotificationStatusListener; }

    public FastLivePushClient(AppInfo appInfo, String baseUrl) {
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
            pushInitializedListener.onInitialized(400, String.format("Selection of FastLivePush PushGateAddress failed: %s",e.getMessage()));
        }
        return null;
    }

    public void buildConnect() {
        this.clientId = UUID.randomUUID().toString();
        this.fastLivePushHttpClient = new FastLivePushHttpClient(appInfo);
        this.pushGateAddress = selectPushGateAddress();
        this.nettyClient = new NettyClient(appInfo);

        this.nettyClient.connect(pushGateAddress, pushInitializedListener);
        this.nettyClient.registerPushInitializedCallback(pushInitializedListener);
        this.nettyClient.registerMsgStateCallback(pushNotificationStatusListener);
    }

    public void sendPushNotification (PushNotification pushNotification) {
        this.nettyClient.sendPushNotification(pushNotification);
    }
}