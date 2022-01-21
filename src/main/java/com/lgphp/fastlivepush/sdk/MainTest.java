package com.lgphp.fastlivepush.sdk;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONObject;
import com.lgphp.fastlivepush.sdk.common.PushMessageLevel;
import com.lgphp.fastlivepush.sdk.entity.AppInfo;
import com.lgphp.fastlivepush.sdk.entity.NotificationStatus;
import com.lgphp.fastlivepush.sdk.entity.PushNotification;
import com.lgphp.fastlivepush.sdk.listener.PushInitializedListener;
import com.lgphp.fastlivepush.sdk.listener.PushNotificationStatusListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @Description MainTest
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
@Slf4j
public class MainTest {
    // App商户端信息
    public static AppInfo appInfo;
    // FastLivePush接口地址
    public static String baseUrl;

    public static TimeInterval connectTime = new TimeInterval();
    public static TimeInterval msgTime = new TimeInterval();
    public static AtomicInteger connectedClient = new AtomicInteger(0);
    public static AtomicInteger deliveredClient = new AtomicInteger(0);
    public static AtomicInteger sentClient = new AtomicInteger(0);

    public static void init () {
        appInfo = new AppInfo();
        appInfo.setAppId("b4722bb12f30485582fb3e3a5c6157c6");
        appInfo.setAppKey("NUhONBRTxPxFtFkH78P9AJ2EDUJ1EeoaFzGVoJUz5BcYFtqiag0baRw61y1ycoZaYpkxp9BC08K2F8h2II4tyQ==");
        appInfo.setMerchantId("a127297f117c4a3fb095a15443bc96fc");
        baseUrl = "http://77.242.242.209:8080";
    }

    // 创建client
    public static List<FastLivePushClient> buildClient(int maxClientCnt) {
        List<FastLivePushClient> clients = new ArrayList<>(maxClientCnt);

        IntStream.rangeClosed(1,maxClientCnt).forEach(clientNum -> {
            FastLivePushClient fastLivePushClient = new FastLivePushClient(appInfo, baseUrl);
            fastLivePushClient.addPushInitializedListener(new PushInitializedListener() {
                @Override
                public void onInitialized(int code, String message) {
                    if (code == 200 && message.startsWith("Authentication")) {
                        // 连接成功
                        int curr = connectedClient.incrementAndGet();
                        JSONObject metricObject = new JSONObject().fluentPut("clientNum",clientNum).fluentPut("cost", connectTime.intervalMs(String.valueOf(clientNum))).fluentPut("connectedCnt",curr);
                        log.info("==========>Testing connection success#{}", metricObject);
                    }

                    if (code != 200) {
                        log.warn("PushInitializedListener.onInitialized----->code: {}, message: {}", code, message);
                    }
                }
            });
            fastLivePushClient.addPushNotificationStatusListener(new PushNotificationStatusListener() {
                @Override
                public void onPush(NotificationStatus notificationStatus) {
                    int statusCode = notificationStatus.getStatusCode();
                    // 发送失败
                    if (statusCode != 0) log.warn("NotificationStatusListener.onPush----->{}", JSONObject.toJSONString(notificationStatus));
                }
                @Override
                public void onSent(NotificationStatus notificationStatus) {
                    int statusCode = notificationStatus.getStatusCode();
                    String messageID = notificationStatus.getMessageID();
                    if (statusCode == 1) {
                        // Delivery
                        int curr = deliveredClient.incrementAndGet();
                        JSONObject metricObject = new JSONObject().fluentPut("clientNum",clientNum).fluentPut("msgId",messageID).fluentPut("cost", msgTime.intervalMs(messageID)).fluentPut("deliveredCnt",curr);
                        log.info("==========>Testing Delivery success#{}", metricObject);
                    }else if (statusCode == 3) {
                        // Sent Success
                        int curr = sentClient.incrementAndGet();
                        JSONObject metricObject = new JSONObject().fluentPut("clientNum",clientNum).fluentPut("msgId",messageID).fluentPut("cost", msgTime.intervalMs(messageID)).fluentPut("sentCnt",curr);
                        log.info("==========>Testing Sent success#{}", metricObject);
                    } else {
                        // failure
                        log.warn("NotificationStatusListener.onSent----->{}", JSONObject.toJSONString(notificationStatus));
                    }
                }
            });
            connectTime.start(String.valueOf(clientNum));
            fastLivePushClient.buildConnect();
            clients.add(fastLivePushClient);
        });

        return clients;
    }

    // 发送通知消息
    public static void sendMsg (List<FastLivePushClient> clients, int maxMsgCnt) {
        clients.forEach(fastLivePushClient -> {
            ThreadUtil.execAsync(() -> {
                IntStream.rangeClosed(1, maxMsgCnt).forEach(value -> {
                    String messageId = UUID.fastUUID().toString();
                    msgTime.start(messageId);
                    PushNotification pushNotification = new PushNotification();
                    pushNotification.setMessageId(messageId);
                    pushNotification.setToUID("8613810654610");
                    pushNotification.setMessagePriority(PushMessageLevel.LOW);
                    PushNotification.MessageBody messageBody = new PushNotification.MessageBody();
                    messageBody.setTitle(String.format("%s+:%s", "标题", "测试啊啊啊啊啊啊啊啊啊"));
                    messageBody.setBody("消息体");
                    pushNotification.setMessageBody(messageBody);
                    fastLivePushClient.sendPushNotification(pushNotification);
                });
            });
        });
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            log.error("请传入正确的指令：java -jar FastClient.jar [客户端实例数] [通知消息数]");
            return;
        }
        else{
            init();
            int maxClientCnt = Integer.parseInt(args[0]);
            int maxMsgCnt = Integer.parseInt(args[1]);
            log.info("Start FastClient maxClientCnt: {}, maxMsgCnt: {}", maxClientCnt, maxMsgCnt);
            List<FastLivePushClient> clients = buildClient(maxClientCnt);
            sendMsg(clients, maxMsgCnt);
        }

//        init();
//        List<FastLivePushClient> clients = buildClient(2);
//        sendMsg(clients, 10);

    }
}