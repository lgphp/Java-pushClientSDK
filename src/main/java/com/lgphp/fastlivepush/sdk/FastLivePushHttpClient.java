package com.lgphp.fastlivepush.sdk;

import com.lgphp.fastlivepush.sdk.entity.AppInfo;
import com.lgphp.fastlivepush.sdk.util.CryptoUtil;
import com.lgphp.fastlivepush.sdk.util.KeyManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @Description HttpClient
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
@Slf4j
public class FastLivePushHttpClient {

    private AppInfo appInfo;
    private CloseableHttpClient httpClient;

    public FastLivePushHttpClient(AppInfo appInfo) {
        this.appInfo = appInfo;
        initHttpClient();
    }

    public void initHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClients.createDefault();
        }
    }

    public String postByJSON(String requestUri, String requestBody) {
        if (httpClient == null) httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(requestUri);
        CloseableHttpResponse response=null;
        try {
            byte[] apiKey = KeyManager.getApiKey(KeyManager.stringKey2Byte(appInfo.getAppKey()));
            String signature = CryptoUtil.encrypt(apiKey, requestBody);
            post.setHeader("Content-Type", "application/json;charset=UTF-8");
            post.setHeader("API-SIGNATURE", signature);
            post.setHeader("APP-ID", appInfo.getAppId());

            StringEntity se = new StringEntity(requestBody );
            se.setContentEncoding("UTF-8");
            se.setContentType("application/json");
            post.setEntity(se);

            response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            String resData = EntityUtils.toString(response.getEntity());
            // log.debug("HttpClient.postByJSON {} success: {}", requestUri, resData);
            return resData;
        }catch (Exception e) {
            log.warn("HttpClient.postByJSON {} failed: " + e, requestUri);
            return null;
        }finally {
            post.releaseConnection();
            if (response!=null)
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}