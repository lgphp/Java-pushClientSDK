package com.lgphp.fastlivepush.sdk.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @Description NotificationAck
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
@Data
@Builder
public class NotificationStatus {

    private String messageID;
    private String appID;
    private String userID;
    private int statusCode;
    private String statusMessage;
}
