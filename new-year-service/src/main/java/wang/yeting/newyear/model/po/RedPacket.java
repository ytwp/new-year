package wang.yeting.newyear.model.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author : weipeng
 * @since : 2022-01-23 23:21
 */
@Data
@Accessors(chain = true)
@TableName(autoResultMap = true)
public class RedPacket implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "red_packet_id", type = IdType.ASSIGN_ID)
    private String redPacketId;

    private String userId;
    private String nickName;
    private Integer num;
    private Integer totalFee;
    private Integer payTotalFee;
    private Integer redPacketType;
    private Integer receivingMethod;
    private String redPacketBlessing;
    private String outTradeNo;
    private String transactionId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> redPacketFeeList;

    /**
     * 10 支付成功
     * 0 未支付
     * -10 已领完
     * -5 已退钱
     */
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}