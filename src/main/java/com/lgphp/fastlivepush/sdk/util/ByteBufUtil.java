package com.lgphp.fastlivepush.sdk.util;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import org.apache.commons.codec.binary.StringUtils;

/**
 * @author lgphp
 * @className ByteBufUtil
 * @date 11/30/21 16:21
 * @description
 */
public class ByteBufUtil     {
    /**
     * 从byteBuf 读长度为byte的字符串
     * **/
    public static String readStringForByteLength(ByteBuf byteBuf){
        byte strLen = byteBuf.readByte();
        return readString(byteBuf , strLen);
    }

    /**
     * 从byteBuf 读长度为short的字符串
     * @param byteBuf
     * @return
     */
    public  static String readStringForShortLength(ByteBuf byteBuf){
        short strLen = byteBuf.readShort();
        return readString(byteBuf , strLen);
    }

    /**
     * 从byteBuf 读长度为int的字符串
     * @param byteBuf
     * @return
     */
    public static String readStringForIntLength(ByteBuf byteBuf){
        int strLen = byteBuf.readInt();
        return readString(byteBuf , strLen);
    }


    /**
     * 读一个对象，因为对象都是用JSONString编码的
     * @param byteBuf
     * @param clazz
     * @param <T>
     * @return
     */
    public  static <T> T readObjForIntLength(ByteBuf byteBuf , Class<T> clazz){
        int strLen = byteBuf.readInt();
        if (strLen==0) return null;
        String jsonString =  readString(byteBuf , strLen);
        return JSONObject.parseObject(jsonString, clazz);
    }

    private static  String readString(ByteBuf buf, int strLen){
        if (strLen==0) return "";
        byte[] str = new byte[strLen];
        buf.readBytes(str);
        return StringUtils.newStringUtf8(str);
    }


    private static  void writeString(ByteBuf buf , String str){
        if (str.getBytes().length>0) {
            buf.writeBytes(str.getBytes());
        }
    }

    public static  int writeStringForByteLength(ByteBuf byteBuf , String str){
        byteBuf.writeByte(str.getBytes().length);
        writeString(byteBuf, str);
        return str.getBytes().length+1;
    }

    public static  int writeStringForShortLength(ByteBuf byteBuf , String str){
        byteBuf.writeShort(str.getBytes().length);
        writeString(byteBuf, str);
        return str.getBytes().length+2;
    }

    public static  int writeStringForIntLength(ByteBuf byteBuf , String str){
        byteBuf.writeInt(str.getBytes().length);
        writeString(byteBuf, str);
        return str.getBytes().length+4;
    }

    public static  int writeObjectForIntLength(ByteBuf byteBuf , Object obj){
        String jsonString = JSONObject.toJSONString(obj);
        return writeStringForIntLength(byteBuf , jsonString);
    }
}
