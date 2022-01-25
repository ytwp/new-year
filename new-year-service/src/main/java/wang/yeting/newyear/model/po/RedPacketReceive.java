package wang.yeting.newyear.model.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author : weipeng
 * @since : 2022-01-23 23:21
 */
@Data
@Accessors(chain = true)
public class RedPacketReceive implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "red_packet_receive_id", type = IdType.ASSIGN_ID)
    private String redPacketReceiveId;

    private String userId;
    private String openId;
    private String redPacketId;

    private String nickName;
    private String avatarUrl;
    private Integer feeIndex;
    private Integer fee;

    private String partnerTradeNo;
    private String paymentNo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> transferResultJson;

    /**
     * 10 付款成功
     * 0 待付款
     * -10 异常
     * -5 付款失败
     */
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}