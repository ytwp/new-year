package wang.yeting.newyear.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author : weipeng
 * @since : 2022-01-24 11:58
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RedPacketShareDto {

    private String redPacketId;
    private String userId;
    private String nickName;
    private Integer redPacketType;
    private Integer receivingMethod;
    private String redPacketBlessing;
    private Integer status;
}
