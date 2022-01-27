package wang.yeting.newyear.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import wang.yeting.newyear.annotation.CurrentUser;
import wang.yeting.newyear.annotation.Permission;
import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.dto.RedPacketReceiveDto;
import wang.yeting.newyear.model.vo.RedPacketVo;
import wang.yeting.newyear.service.RedPacketService;

import java.io.IOException;
import java.util.List;

/**
 * @author : weipeng
 * @since : 2022-01-26 09:36
 */
@RestController
public class Tesr {

    @Autowired
    RedPacketService redPacketService;


}
