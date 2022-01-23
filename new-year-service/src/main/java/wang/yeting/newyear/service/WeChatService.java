package wang.yeting.newyear.service;

import wang.yeting.newyear.model.Result;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-06-08 12:10
 */

public interface WeChatService {

    String getAccessToken();

    String getCenterAccessToken();

    Result<String> getQrcode(String page, String scene);

}
