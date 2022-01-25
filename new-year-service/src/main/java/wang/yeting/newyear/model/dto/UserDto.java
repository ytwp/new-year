package wang.yeting.newyear.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-22 11:09
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String openId;
    private String nickName;
    private String avatarUrl;
    private String language;
    private Integer gender;
    private String city;
    private String province;
    private String country;
    private Integer status;
    private LocalDateTime createTime;
}
