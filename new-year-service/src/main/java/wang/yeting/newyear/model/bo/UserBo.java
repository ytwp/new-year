package wang.yeting.newyear.model.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author : weipeng
 * @date : 2020-10-10 12:19
 */

@Data
@Accessors(chain = true)
public class UserBo implements Serializable {

    private Long userId;
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
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    private String token;
}
