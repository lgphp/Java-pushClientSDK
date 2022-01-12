package com.lgphp.fastlivepush.sdk.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class CryptoUtil {
    public static String encrypt(String apiKey, String data) {
        JSONObject json = JSONUtil.parseObj(data);
        Set<String> keySet = json.keySet();
        Stream<String> sorted = keySet.stream().sorted(String::compareTo);
        StringBuilder sb = new StringBuilder();
        sorted.forEach(s -> sb.append(s).append("=").append(json.get(s)).append("&"));
        sb.append("apiKey=").append(apiKey);
        return SecureUtil.md5(sb.toString()).toUpperCase();
    }

    public static String encrypt(byte[] apiKey, String data) {
        return encrypt(KeyManager.byteKey2String(apiKey), data);
    }
}
