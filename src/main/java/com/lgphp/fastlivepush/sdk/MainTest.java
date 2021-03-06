package com.lgphp.fastlivepush.sdk;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONObject;
import com.lgphp.fastlivepush.sdk.common.NotificationClassfyEnmu;
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
    public static TimeInterval finishTime = new TimeInterval();
    public static AtomicInteger connectedClient = new AtomicInteger(0);
    public static AtomicInteger deliveredClient = new AtomicInteger(0);
    public static AtomicInteger sentClient = new AtomicInteger(0);
    public static AtomicInteger sendMsg = new AtomicInteger(0);

    private static int maxClientCnt = 1 ;
    private static int maxMsgCnt = 2000;

    public static void init () {
        appInfo = new AppInfo();
        appInfo.setAppId("41a5b4cbce864955b8a212dbcdb51409");
        appInfo.setAppKey("+HBMq4BRiGq8mpCbce23/fxKJho1QVAZwppDVySFdrIolMNflXHPw1PW0TFjPPysg7Z4/lllDkui8UtDUUk4iA==");
        appInfo.setMerchantId("3dc2d214ab9e4daf9950ef657a156805");
        baseUrl = "http://77.242.242.209:8080";
    }

    // 创建client
    public static List<FastLivePushClient> buildClient() {
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
                        log.info("Build connection success#{}", metricObject);
                    }

                    if (code != 200) {
                        log.warn("PushInitializedListener.onInitialized----->code: {}, message: {}", code, message);
                    }
                }
            });
            fastLivePushClient.addPushNotificationStatusListener(new PushNotificationStatusListener() {
                @Override
                public void onSend(int code, String message) {
                    // 发送成功
                    if (200 == code) {
                        int curr = sendMsg.incrementAndGet();
                        if (curr == maxMsgCnt * maxClientCnt)
                            log.info("Finished SendMsg, Cnt: {}, cost: {} ms", curr, finishTime.intervalMs());
                    } else {
                        log.warn("SendMsg failed, code:{}, message:{}", code, message);
                    }
                }
                @Override
                public void onAck(NotificationStatus notificationStatus) {
                    int statusCode = notificationStatus.getStatusCode();
                    String messageID = notificationStatus.getMessageID();
                    if (statusCode == 1) {
                        // Delivery
                        int curr = deliveredClient.incrementAndGet();
                        if (curr == maxMsgCnt * maxClientCnt) {
                            log.info("Finished Delivery, Cnt: {}, cost: {} ms", curr, finishTime.intervalMs());
                        }

                        JSONObject metricObject = new JSONObject().fluentPut("clientNum",clientNum).fluentPut("msgId",messageID).fluentPut("cost", msgTime.intervalMs(messageID)).fluentPut("deliveredCnt",curr);
                        log.info("{}#{}", notificationStatus.getStatusMessage(), metricObject);
                    }else if (statusCode == 3) {
                        // Sent Success
                        int curr = sentClient.incrementAndGet();
                        if (curr == maxMsgCnt * maxClientCnt) {
                            log.info("Finished SentSuccess, Cnt: {}, cost: {} ms", curr, finishTime.intervalMs());
                        }

                        JSONObject metricObject = new JSONObject().fluentPut("clientNum",clientNum).fluentPut("msgId",messageID).fluentPut("cost", msgTime.intervalMs(messageID)).fluentPut("sentCnt",curr);
                        log.info("{}#{}", notificationStatus.getStatusMessage(), metricObject);
                    } else {
                        // failure
                        log.warn("Received NotificationStatus==========>{}", JSONObject.toJSONString(notificationStatus));
                    }
                }
            });
            connectTime.start(String.valueOf(clientNum));
            fastLivePushClient.sendBufferSize(10_000);
            fastLivePushClient.sendSpeed(2000);
            fastLivePushClient.buildConnect();
            clients.add(fastLivePushClient);
            ThreadUtil.sleep(500);
        });

        return clients;
    }

    // 发送通知消息
    public static void sendMsg (List<FastLivePushClient> clients) {
        finishTime.start();

        for (int i=0;i<clients.size();i++) {
            FastLivePushClient fastLivePushClient = clients.get(i);
            int finalI = i + 1;
            IntStream.rangeClosed(1, maxMsgCnt).forEach(value -> {
                    String messageId = UUID.fastUUID().toString();
                    msgTime.start(messageId);
                    PushNotification pushNotification = new PushNotification();
                    pushNotification.setMessageId(messageId);
                    pushNotification.setToUID("8613810654610");
                    pushNotification.setMessagePriority(PushMessageLevel.LOW);
                    pushNotification.setClassifier(NotificationClassfyEnmu.PUSH);
                    PushNotification.MessageBody messageBody = new PushNotification.MessageBody();
                    messageBody.setTitle(String.format("%s->%s-%s+:%s", "标题", finalI, value,"测试啊啊啊啊啊啊啊啊啊"));
                    messageBody.setBody("消息体");
                    pushNotification.setMessageBody(messageBody);
                    fastLivePushClient.sendPushNotification(pushNotification);
                });
        }
    }

    // 发送SMS
    public static void sendSMSMsg (List<FastLivePushClient> clients) {
        finishTime.start();

        for (int i=0;i<clients.size();i++) {
            FastLivePushClient fastLivePushClient = clients.get(i);
            int finalI = i + 1;
            IntStream.rangeClosed(1, maxMsgCnt).forEach(value -> {
                String messageId = UUID.fastUUID().toString();
                msgTime.start(messageId);
                PushNotification smsNotification = new PushNotification();
                smsNotification.setMessageId(messageId);
                smsNotification.setToUID("8613810654610");
                smsNotification.setMessagePriority(PushMessageLevel.LOW);
                smsNotification.setClassifier(NotificationClassfyEnmu.SMS);
                PushNotification.MessageBody messageBody = new PushNotification.MessageBody();
                messageBody.setTitle(String.format("%s->%s-%s+:%s", "标题", finalI, value,"测试SMS"));
                messageBody.setBody("短信验证码");
                smsNotification.setMessageBody(messageBody);
                fastLivePushClient.sendSMSNotification(smsNotification);
            });
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            log.error("请传入正确的指令：java -jar FastClient.jar [客户端实例数] [通知消息数]");
            return;
        }
        else{
            init();
            maxClientCnt = Integer.parseInt(args[0]);
            maxMsgCnt = Integer.parseInt(args[1]);
            if (maxClientCnt <= 0 || maxMsgCnt <= 0) {
                log.error("请传入正确的指令：java -jar FastClient.jar [客户端实例数] [通知消息数]");
                return;
            }
            log.info("Start FastClient maxClientCnt: {}, maxMsgCnt: {}", maxClientCnt, maxMsgCnt);
            List<FastLivePushClient> clients = buildClient();
            sendMsg(clients);
        }

//        init();
//        List<FastLivePushClient> clients = buildClient();
//        sendMsg(clients);
//        sendSMSMsg(clients);
    }
}