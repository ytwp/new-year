package wang.yeting.newyear.util;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.dto.LoginDto;
import wang.yeting.newyear.model.po.User;
import wang.yeting.newyear.service.UserService;

import java.util.UUID;

/**
 * @author : weipeng
 * @date : 2020-08-20 19:05
 */
@Component
public class TokenUtils {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private UserService userService;

    public static final String TOKEN_PREFIX = "login:";

    public boolean checkCms(String cmsToken) {
        return false;
    }

    public String generateToken() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public boolean logout(String token) {
        return redisUtils.delete(TOKEN_PREFIX + token);
    }

    public UserBo getLoginDtoByToken(String token) {
        return redisUtils.get(TOKEN_PREFIX + token, UserBo.class);
    }

    public String login(User user) {
        String token = generateToken();
        UserBo userBo = CopyBeanUtils.copyProperties(user, UserBo.class);
        userBo.setToken(token);
        redisUtils.set(TOKEN_PREFIX + token, JSONUtil.toJsonStr(userBo), 60 * 60 * 24 * 7);
        return token;
    }

    /**
     * 刷新用户信息 并且延长过期时间
     *
     * @param user
     */
    public UserBo refresh(UserBo user) {
        User userDb = userService.getById(user.getUserId());
        UserBo userBo = CopyBeanUtils.copyProperties(userDb, UserBo.class);
        userBo.setToken(user.getToken());
        redisUtils.set(TOKEN_PREFIX + user.getToken(), JSONUtil.toJsonStr(userBo), 60 * 60 * 24 * 7);
        return userBo;
    }

    /**
     * @param user
     * @return
     */
    public LoginDto expire(UserBo user) {
        UserBo userBo = refresh(user);
        return CopyBeanUtils.copyProperties(userBo, LoginDto.class);
    }
}
