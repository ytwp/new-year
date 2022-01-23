package wang.yeting.newyear.model.dto;

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
public class LoginDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long inviteCode;
    private Long parentId;

    private String wxOpenId;
    private String nickName;
    private String avatarUrl;
    private String userSign;
    private String token;
    private Integer userType;
    private Integer status;
    private Boolean newUser = false;
}
