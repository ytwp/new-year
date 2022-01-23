package wang.yeting.newyear.model.vo;

import lombok.Data;

/**
 * @author : weipeng
 * @date : 2020-10-12 6:15 下午
 */
@Data
public class WeChatUserInfoVo {
    private String avatarUrl;
    private String city;
    private String country;
    private String language;
    private String nickName;
    private String province;
    private Integer gender;
}
