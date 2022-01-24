package wang.yeting.newyear.service;

import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.vo.RedPacketVo;

/**
 * @author : weipeng
 * @since : 2022-01-23 22:52
 */

public interface RedPacketService {
    Result<?> send(UserBo user, RedPacketVo redPacketVo);

    Result<?> queryPay(UserBo user, RedPacketVo redPacketVo);

    Result<?> find(UserBo user, RedPacketVo redPacketVo);

    Result<?> shareGet(RedPacketVo redPacketVo);

    Result<?> get(UserBo user, RedPacketVo redPacketVo);

    Result<?> receive(UserBo user, RedPacketVo redPacketVo);
}
