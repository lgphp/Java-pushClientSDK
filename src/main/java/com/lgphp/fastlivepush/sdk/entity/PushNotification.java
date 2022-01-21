package com.lgphp.fastlivepush.sdk.entity;

import com.lgphp.fastlivepush.sdk.common.PushMessageLevel;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Description PushNotification
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
@Data
public class PushNotification {
    private String messageId;
    private PushMessageLevel messagePriority;
    private String toUID;
    private MessageBody messageBody;
    @Data
    public static class MessageBody implements Serializable {
        private String title;
        private String body;
        private Map<String, String> data;
    }
}
