package com.lgphp.fastlivepush.sdk.listener;

import com.lgphp.fastlivepush.sdk.entity.NotificationStatus;

/**
 * @Description NotificationStatusListener
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
public interface PushNotificationStatusListener {

	/**
	 *  监听通知发送状态
	 * @param notificationStatus
	 */
	public void onPush(NotificationStatus notificationStatus);

	/**
	 *  监听通知回执状态
	 * @param notificationStatus
	 */
	public void onSent(NotificationStatus notificationStatus);

}
