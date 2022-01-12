package com.lgphp.fastlivepush.sdk.entity;

import lombok.Data;

/**
 * @Description PushGateAddress
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
@Data
public class PushGateAddress {
    private String ip;
    private int port;

    public PushGateAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}