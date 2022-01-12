package com.lgphp.fastlivepush.sdk.payload;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author lgphp
 * @className MessageBody
 * @date 12/26/21 18:35
 * @description
 */
@Data
public class MessageBody implements Serializable {
    private String title;
    private String body;
    private Map<String, String> data;
}