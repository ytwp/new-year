package wang.yeting.newyear.model.vo;

import lombok.Data;

/**
 * @author : weipeng
 * @date : 2020-10-20 3:37 下午
 */
@Data
public class AccessTokenVo {

    private String access_token;

    private Integer expires_in;
}
