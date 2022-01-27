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
import wang.yeting.newyear.model.vo.RedPacketInferVo;
import wang.yeting.newyear.model.vo.RedPacketVo;
import wang.yeting.newyear.service.RedPacketService;

import java.io.IOException;
import java.util.HashMap;

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
    @PostMapping("/infer")
    public Result<?> infer(@CurrentUser UserBo userBo, @RequestBody RedPacketInferVo redPacketInferVo) throws IOException {
        return redPacketService.infer(userBo, redPacketInferVo);
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

    @Permission
    @PostMapping("/meReceive")
    public Result<?> meReceive(@CurrentUser UserBo user, @RequestBody RedPacketVo redPacketVo) {
        return redPacketService.meReceive(user, redPacketVo);
    }

    /**
     * from_data: {
     * nickName: '',
     * num: 1,
     * amount: 1.88,
     * redPacketType: '拼手气',
     * receivingMethod: '打开即可',
     * redPacketBlessing: '祝你新年快乐，合家团圆!'
     * },
     *
     * @return
     */
    @PostMapping("/defaultValue")
    public Result<?> defaultValue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nickName", "");
        map.put("num", 1);
        map.put("amount", 1.88);
        map.put("redPacketType", "拼手气");
        map.put("receivingMethod", "手势拜年");
        map.put("redPacketBlessing", "祝你新年快乐，合家团圆!");
        return Result.success(map);
    }

}
