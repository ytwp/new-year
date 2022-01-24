package wang.yeting.newyear.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import wang.yeting.newyear.annotation.CurrentUser;
import wang.yeting.newyear.annotation.Permission;
import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.vo.LoginVo;
import wang.yeting.newyear.service.LoginService;

/**
 * @author : weipeng
 * @date : 2020-10-10 22:04
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginVo loginVo) {
        return loginService.weChatLogin(loginVo);
    }

    /**
     * 拿到用户token 验证有没有登陆 和 延长缓存
     *
     * @param user
     * @return
     */
    @Permission
    @GetMapping("/exist")
    public Result<?> exist(@CurrentUser UserBo user) {
        return loginService.exist(user);
    }

    @Permission
    @PostMapping("/update")
    public Result<?> update(@CurrentUser UserBo user, @RequestBody LoginVo loginVo) {
        return loginService.updateUserInfo(user, loginVo);
    }

}
