package wang.yeting.newyear.service;

import com.baomidou.mybatisplus.extension.service.IService;
import wang.yeting.newyear.model.dto.UserDto;
import wang.yeting.newyear.model.po.User;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-22 11:09
 */

public interface UserService extends IService<User> {

    UserDto getDto(Long userId);

    User getByUserId(Long userId);

}
