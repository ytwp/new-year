package wang.yeting.newyear.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.yeting.newyear.manager.WeChatManager;
import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.vo.AccessTokenVo;
import wang.yeting.newyear.service.UserService;
import wang.yeting.newyear.service.WeChatService;
import wang.yeting.newyear.util.RedisUtils;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-06-08 12:11
 */

@Service
public class WeChatServiceImpl implements WeChatService {

    @Autowired
    private WeChatManager weChatManager;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserService userService;

    @Override
    public String getAccessToken() {
        String resultStr = HttpUtil.get("https://mall.yeting.wang/wechat/getAccessToken");
        if (resultStr != null) {
            Result<String> result = JSONUtil.toBean(resultStr, Result.class);
            return result.getData();
        }
        return null;
    }

    @Override
    public String getCenterAccessToken() {
        String accessTokenStr = (String) redisUtils.get("accessToken");
        if (StringUtils.isNotBlank(accessTokenStr)) {
            return accessTokenStr;
        } else {
            AccessTokenVo accessTokenVo = weChatManager.getAccessToken();
            if (accessTokenVo != null && accessTokenVo.getAccess_token() != null) {
                redisUtils.set("accessToken:wx", accessTokenVo.getAccess_token(), accessTokenVo.getExpires_in() / 2);
                return accessTokenVo.getAccess_token();
            }
        }
        return null;
    }

    @Override
    public Result<String> getQrcode(String page, String scene) {
        String qrcode = weChatManager.createQrcode(getAccessToken(), page, scene);
        return Result.success(qrcode);
    }

}
