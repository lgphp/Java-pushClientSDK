package com.lgphp.fastlivepush.sdk.listener;

import com.lgphp.fastlivepush.sdk.entity.NotificationAck;

/**
 * @Description NotificationStatusListener
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
public interface PushNotificationStatusListener {

	/**
	 *  通知回执
	 * @param ack
	 */
	public void onReceived(NotificationAck ack);

}
