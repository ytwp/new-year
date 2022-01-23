package wang.yeting.newyear.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.yeting.newyear.manager.WeChatManager;
import wang.yeting.newyear.mapper.UserMapper;
import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.dto.LoginDto;
import wang.yeting.newyear.model.po.User;
import wang.yeting.newyear.model.vo.LoginVo;
import wang.yeting.newyear.service.LoginService;
import wang.yeting.newyear.service.UserService;
import wang.yeting.newyear.service.WeChatService;
import wang.yeting.newyear.util.RedisUtils;
import wang.yeting.newyear.util.TokenUtils;

import java.util.Map;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-22 11:10
 */

@Service
public class LoginServiceImpl extends ServiceImpl<UserMapper, User> implements LoginService {

    @Autowired
    private WeChatManager weChatManager;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private TokenUtils tokenUtils;

    @Override
    public Result<?> weChatLogin(LoginVo loginVo) {
        Map<String, String> map = weChatManager.getOpenId(loginVo.getCode());
        if (map == null) {
            return Result.exceptionError("登录失败");
        }
        String openId = map.get("openid");
        String unionId = map.get("unionid");
        String sessionKey = map.get("session_key");
        User user = getByWxOpenId(openId);
        LoginDto loginDto = new LoginDto();
        if (user == null) {
            loginDto.setNewUser(true);
            if (loginVo.getUserInfo() == null) {
                return Result.exceptionError("获取用户信息失败");
            }
            //注册
            User saveUser = new User();
            saveUser.setOpenId(openId);
            saveUser.setUnionId(unionId);
            saveUser.setSessionKey(sessionKey);
            Boolean saveBoolean = saveUser(saveUser);
            if (!saveBoolean) {
                return Result.exceptionError("注册失败");
            }
            user = saveUser;
        }
        //更新用户信息
        BeanUtils.copyProperties(loginVo.getUserInfo(), user);
        updateById(user);
        //redis
        BeanUtils.copyProperties(user, loginDto);
        String token = tokenUtils.login(user);
        loginDto.setToken(token);
        return Result.success(loginDto);
    }

    private User getByWxOpenId(String wxOpenId) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, wxOpenId));
    }

    private Boolean saveUser(User user) {
        return save(user);
    }

    @Override
    public Result<?> exist(UserBo user) {
        LoginDto loginDto = tokenUtils.expire(user);
        return Result.success(loginDto);
    }

}
