package wang.yeting.newyear.model.vo;

import lombok.Data;

/**
 * @author : weipeng
 * @date : 2020-10-12 6:08 下午
 */
@Data
public class LoginVo {

    private String code;
    private WeChatUserInfoVo userInfo;
}
