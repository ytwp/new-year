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
public class RedPacketReceiveDto {

    private String redPacketId;
    private String message;
    private String buttonContext;
    private Integer buttonMethod;
    private Boolean sendMoneyStatus;
    private Integer fee;
    private Boolean status;
    private Boolean gestureStatus = true;
}
