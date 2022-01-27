package wang.yeting.newyear.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author : weipeng
 * @since : 2022-01-23 22:51
 */
@Data
@Accessors(chain = true)
public class RedPacketInferVo {

    private List<List<Integer>> imgArray;
    private Integer width;
    private Integer height;
    private String redPacketUserId;
    private String redPacketId;
}
