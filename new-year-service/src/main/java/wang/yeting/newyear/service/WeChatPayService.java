package wang.yeting.newyear.service;

import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.po.RedPacket;

import java.util.Map;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-26 23:29
 */

public interface WeChatPayService {

    Boolean transfers(Long withdrawId);

    Map<String, String> miniAppPay(UserBo user, RedPacket redPacket);

    String queryOrder(String transactionId, String outTradeNo);
}
