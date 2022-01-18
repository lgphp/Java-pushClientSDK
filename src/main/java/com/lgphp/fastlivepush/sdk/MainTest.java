package com.lgphp.fastlivepush.sdk;

import com.alibaba.fastjson.JSONObject;
import com.lgphp.fastlivepush.sdk.common.PushMessageLevel;
import com.lgphp.fastlivepush.sdk.entity.AppInfo;
import com.lgphp.fastlivepush.sdk.entity.NotificationStatus;
import com.lgphp.fastlivepush.sdk.entity.PushNotification;
import com.lgphp.fastlivepush.sdk.listener.PushInitializedListener;
import com.lgphp.fastlivepush.sdk.listener.PushNotificationStatusListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @Description MainTest
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
@Slf4j
public class MainTest {
    public static void main(String[] args) throws InterruptedException {
        // App商户端信息
        AppInfo appInfo = new AppInfo();
        appInfo.setAppId("b4722bb12f30485582fb3e3a5c6157c6");
        appInfo.setAppKey("NUhONBRTxPxFtFkH78P9AJ2EDUJ1EeoaFzGVoJUz5BcYFtqiag0baRw61y1ycoZaYpkxp9BC08K2F8h2II4tyQ==");
        appInfo.setMerchantId("a127297f117c4a3fb095a15443bc96fc");

        // FastLivePush接口地址
        String baseUrl = "http://77.242.242.209:8080";

        // 创建FastLivePush实例
        FastLivePushClient fastLivePushClient = new FastLivePushClient(appInfo, baseUrl);
        fastLivePushClient.addPushInitializedListener(new PushInitializedListener() {
            @Override public void onInitialized(int code, String message) {
                log.info("PushInitializedListener.onInitialized----->code: {}, message: {}", code, message);
            }
        });
        fastLivePushClient.addPushNotificationStatusListener(new PushNotificationStatusListener() {
            @Override public void onPush(NotificationStatus notificationStatus) {
                log.info("NotificationStatusListener.onPush----->{}", JSONObject.toJSONString(notificationStatus));
            }
            @Override public void onSent(NotificationStatus notificationStatus) {
                log.info("NotificationStatusListener.onSent----->{}", JSONObject.toJSONString(notificationStatus));
            }
        });
        fastLivePushClient.buildConnect();

        TimeUnit.SECONDS.sleep(5);

        // 发送10条通知
        IntStream.rangeClosed(1,10).forEach(num -> {
            PushNotification pushNotification = new PushNotification();
            pushNotification.setToUID("8613810654610");
            pushNotification.setMessagePriority(PushMessageLevel.LOW);
            PushNotification.MessageBody messageBody = new PushNotification.MessageBody();
            messageBody.setTitle(String.format("%s+:%s", "标题", num));
            messageBody.setBody("消息体");
            pushNotification.setMessageBody(messageBody);
            fastLivePushClient.sendPushNotification(pushNotification);
        });
    }
}