package com.lgphp.fastlivepush.sdk.payload;

import io.netty.util.AttributeKey;

/**
 * @author lgphp
 * @className ChannelAttrKey
 * @date 11/30/21 16:34
 * @description
 */
public interface ChannelAttrKey {

    public static final AttributeKey<UserChannelCtx> UserContextKey = AttributeKey.valueOf("user");
}
