package wang.yeting.newyear.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import wang.yeting.newyear.mapper.UserMapper;
import wang.yeting.newyear.model.dto.UserDto;
import wang.yeting.newyear.model.po.User;
import wang.yeting.newyear.service.UserService;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-22 11:10
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserDto getDto(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }

    @Override
    public User getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, userId)
        );
    }

}
