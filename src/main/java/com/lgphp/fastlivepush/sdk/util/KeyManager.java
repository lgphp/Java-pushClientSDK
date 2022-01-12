package com.lgphp.fastlivepush.sdk.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.codec.binary.Base64;

/**
 * @author lgphp
 * @className KeyManager
 * @date 12/9/21 17:03
 * @description
 */
public class KeyManager {

    /**
     * 获取APIKey
     *
     * @param appKey
     * @return
     */
    public static byte[] getApiKey(byte[] appKey) {
        ByteBuf buf = Unpooled.wrappedBuffer(appKey);
        byte[] apiKey = new byte[16];
        buf.readBytes(apiKey);
        buf.release();
        return apiKey;
    }

    /**
     * 获取连接Key
     *
     * @param appKey
     * @return
     */
    public static byte[] getAuthKey(byte[] appKey) {
        ByteBuf buf = Unpooled.wrappedBuffer(appKey);
        byte[] authKey = new byte[16];
        buf.skipBytes(16);
        buf.readBytes(authKey);
        buf.release();
        return authKey;
    }

    /**
     * 获取消息加密key
     *
     * @param appKey
     * @return
     */
    public static byte[] getMsgEncKey(byte[] appKey) {
        ByteBuf buf = Unpooled.wrappedBuffer(appKey);
        byte[] msgEncKey = new byte[16];
        buf.skipBytes(32);
        buf.readBytes(msgEncKey);
        buf.release();
        return msgEncKey;
    }

    /**
     * 获取AES加密的IV
     *
     * @param appKey
     * @return
     */
    public static byte[] getMsgEncAesIV(byte[] appKey) {
        ByteBuf buf = Unpooled.wrappedBuffer(appKey);
        byte[] iv = new byte[16];
        buf.skipBytes(48);
        buf.readBytes(iv);
        buf.release();
        return iv;
    }

    /**
     * key 数组转成字符串
     *
     * @param key
     * @return
     */
    public static String byteKey2String(byte[] key) {
        return Base64.encodeBase64String(key);
    }

    /**
     * key 字符串转成数组
     *
     * @param key
     * @return
     */
    public static byte[] stringKey2Byte(String key) {
        return Base64.decodeBase64(key);
    }
}
