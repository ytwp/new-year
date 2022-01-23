package wang.yeting.newyear.manager;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wang.yeting.newyear.model.vo.AccessTokenVo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-22 12:38
 */

@Slf4j
@Component
public class WeChatManager {

    @Value("${weChat.serverUrl}")
    private String serverUrl;
    @Value("${weChat.appId}")
    private String appId;
    @Value("${weChat.appSecret}")
    private String appSecret;

    public Map<String, String> getOpenId(String code) {
        String url = serverUrl + "/sns/jscode2session?appid=" + appId + "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";
        try {
            String json = HttpUtil.get(url);
            Map map = JSONUtil.toBean(json, Map.class);
            return map;
        } catch (Exception e) {
            log.error("getOpenId---{}", e);
        }
        return null;
    }

    public AccessTokenVo getAccessToken() {
        String url = serverUrl + "/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
        try {
            String json = HttpUtil.get(url);
            return JSONUtil.toBean(json, AccessTokenVo.class);
        } catch (Exception e) {
            log.error("getOpenId---{}", e);
        }
        return null;
    }

    public String createQrcode(String accessToken, String page, String scene) {
        if (accessToken == null) {
            return null;
        }
        String url = serverUrl + "/wxa/getwxacodeunlimit?access_token=" + accessToken;
        HashMap<String, Object> params = new HashMap<>();
        params.put("scene", scene);
        params.put("page", page);
        params.put("width", 280);
        params.put("is_hyaline", true);
        params.put("auto_color", true);
        InputStream weixinIn = null;
        try {
//            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
//            StringEntity entity = new StringEntity(JSONUtil.toJsonStr(params), "utf-8");
//            entity.setContentType("image/png");
//            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "UTF-8"));
//            httpPost.setEntity(entity);
//            HttpResponse weixinResponse = httpClient.execute(httpPost);
//            weixinIn = weixinResponse.getEntity().getContent();
//
//            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
//            byte[] buff = new byte[100];
//            int rc = 0;
//            while ((rc = weixinIn.read(buff, 0, 100)) > 0) {
//                swapStream.write(buff, 0, rc);
//            }
//            byte[] in2b = swapStream.toByteArray();
//            if (in2b.length > 512) {
//                return Base64.encodeBase64String(in2b);
//            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (weixinIn != null) {
                try {
                    weixinIn.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
        return null;
    }

}
