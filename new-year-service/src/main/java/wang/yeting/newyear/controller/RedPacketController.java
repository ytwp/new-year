package wang.yeting.newyear.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.yeting.newyear.annotation.CurrentUser;
import wang.yeting.newyear.annotation.Permission;
import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.vo.RedPacketVo;
import wang.yeting.newyear.service.RedPacketService;

/**
 * @author : weipeng
 * @since : 2022-01-23 22:49
 */
@RestController
@RequestMapping("/redPacket")
public class RedPacketController {

    @Autowired
    RedPacketService redPacketService;

    @Permission
    @PostMapping("/send")
    public Result<?> send(@CurrentUser UserBo user, @RequestBody RedPacketVo redPacketVo) {
        return redPacketService.send(user, redPacketVo);
    }

    @Permission
    @PostMapping("/receive")
    public Result<?> receive(@CurrentUser UserBo user, @RequestBody RedPacketVo redPacketVo) {
        return redPacketService.receive(user, redPacketVo);
    }

    @Permission
    @PostMapping("/queryPay")
    public Result<?> exist(@CurrentUser UserBo user, @RequestBody RedPacketVo redPacketVo) {
        return redPacketService.queryPay(user, redPacketVo);
//        return Result.success(false);
    }

    @Permission
    @PostMapping("/find")
    public Result<?> find(@CurrentUser UserBo user, @RequestBody RedPacketVo redPacketVo) {
        return redPacketService.find(user, redPacketVo);
    }

    @Permission
    @PostMapping("/get")
    public Result<?> get(@CurrentUser UserBo user, @RequestBody RedPacketVo redPacketVo) {
        return redPacketService.get(user, redPacketVo);
    }

    @PostMapping("/share/get")
    public Result<?> shareGet(@RequestBody RedPacketVo redPacketVo) {
        return redPacketService.shareGet(redPacketVo);
    }

}
