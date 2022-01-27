package wang.yeting.newyear.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : weipeng
 * @since : 2022-01-23 22:51
 */
@Data
@Accessors(chain = true)
public class RedPacketVo {
    /**
     * nickName: '',
     * num: 1,
     * amount: 1.88,
     * redPacketType: '拼手气
     * receivingMethod: '打开
     * redPacketBlessing: '祝
     */
    private String userId;
    private String redPacketId;

    private String nickName;
    private Integer num;
    private Double amount;
    private Integer redPacketType;
    private Integer receivingMethod;
    private String redPacketBlessing;
    private String blessingWords;

    private Integer page = 1;
    private Integer size = 10;
}
