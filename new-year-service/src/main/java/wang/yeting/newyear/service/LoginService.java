package wang.yeting.newyear.service;

import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.vo.LoginVo;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-22 11:09
 */

public interface LoginService {

    Result<?> weChatLogin(LoginVo loginVo);

    Result<?> exist(UserBo user);
}
