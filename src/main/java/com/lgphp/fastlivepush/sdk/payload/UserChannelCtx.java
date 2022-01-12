package com.lgphp.fastlivepush.sdk.payload;

import cn.hutool.crypto.symmetric.AES;
import io.netty.channel.Channel;
import lombok.Data;

/**
 * @author lgphp
 * @className UserChannelCtx
 * @date 12/6/21 10:34
 * @description
 */
@Data
public class UserChannelCtx {
    // 客户端实例ID
    private String cinsID;
    private String appId;
    private Channel ch;
    /**
     * 是否经过连接验证
     **/
    private boolean connConfirmed;
    // 每秒消息数量
    private int perSecMessageCount;
    //消息加密的Aes 实例
    private AES aesCBC;
}
