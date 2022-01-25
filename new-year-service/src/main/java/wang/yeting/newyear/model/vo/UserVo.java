package wang.yeting.newyear.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-22 11:09
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserVo implements Serializable {

    private String userId;
    private String openId;
    private String unionId;
    private String sessionKey;
    private String nickName;
    private String avatarUrl;
    private String language;
    private Integer gender;
    private String city;
    private String province;
    private String country;
    private String phone;
    private Integer status;

    private Integer page = 1;
    private Integer size = 10;
}
