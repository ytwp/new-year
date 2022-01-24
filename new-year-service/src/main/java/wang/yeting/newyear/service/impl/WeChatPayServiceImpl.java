package wang.yeting.newyear.service.impl;

import cn.hutool.json.JSONUtil;
import com.ijpay.core.enums.SignType;
import com.ijpay.core.enums.TradeType;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.model.OrderQueryModel;
import com.ijpay.wxpay.model.UnifiedOrderModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.po.RedPacket;
import wang.yeting.newyear.service.UserService;
import wang.yeting.newyear.service.WeChatPayService;

import java.io.InputStream;
import java.util.Map;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-26 23:29
 */

@Slf4j
@Service
public class WeChatPayServiceImpl implements WeChatPayService {

    @Value("${weChat.pay.appid}")
    private String appid;
    @Value("${weChat.pay.mch_id}")
    private String mchId;
    @Value("${weChat.pay.partnerKey}")
    private String partnerKey;

    @Autowired
    private UserService userService;

    @Override
    public Map<String, String> miniAppPay(UserBo user, RedPacket redPacket) {
        //需要通过授权来获取openId
        String openId = user.getOpenId();
        String ip = "127.0.0.1";
        Map<String, String> params = UnifiedOrderModel
                .builder()
                .appid(appid)
                .mch_id(mchId)
                .nonce_str(WxPayKit.generateStr())
                .body("粉毛生活-红包")
                .attach("")
                .out_trade_no(redPacket.getOutTradeNo())
                .total_fee(redPacket.getTotalFee().toString())
                .spbill_create_ip(ip)
                .notify_url("https://baidu.com")
                .trade_type(TradeType.JSAPI.getTradeType())
                .openid(openId)
                .build()
                .createSign(partnerKey, SignType.HMACSHA256);

        String xmlResult = WxPayApi.pushOrder(false, params);

        log.info(xmlResult);
        Map<String, String> result = WxPayKit.xmlToMap(xmlResult);

        String returnCode = result.get("return_code");
        String returnMsg = result.get("return_msg");
        if (!WxPayKit.codeIsOk(returnCode)) {
            return null;
        }
        String resultCode = result.get("result_code");
        if (!WxPayKit.codeIsOk(resultCode)) {
            return null;
        }
        // 以下字段在 return_code 和 result_code 都为 SUCCESS 的时候有返回
        String prepayId = result.get("prepay_id");
        Map<String, String> packageParams = WxPayKit.miniAppPrepayIdCreateSign(appid, prepayId,
                partnerKey, SignType.HMACSHA256);
        log.info("小程序支付的参数:" + JSONUtil.toJsonStr(packageParams));
        return packageParams;
    }

    /**
     * 查询订单
     *
     * @param transactionId
     * @param outTradeNo
     * @return 成功：transactionId， 失败：null
     */
    @Override
    public String queryOrder(String transactionId, String outTradeNo) {
        try {
            Map<String, String> params = OrderQueryModel.builder()
                    .appid(appid)
                    .mch_id(mchId)
                    .transaction_id(transactionId)
                    .out_trade_no(outTradeNo)
                    .nonce_str(WxPayKit.generateStr())
                    .build()
                    .createSign(partnerKey, SignType.MD5);
            log.info("请求参数：{}", WxPayKit.toXml(params));
            String query = WxPayApi.orderQuery(params);
            Map<String, String> result = WxPayKit.xmlToMap(query);
            log.info("查询结果: {}", JSONUtil.toJsonStr(result));
            //交易成功判断条件： return_code、result_code、trade_state 都为SUCCESS
            boolean res = "SUCCESS".equals(result.get("return_code"))
                    && "SUCCESS".equals(result.get("result_code"))
                    && "SUCCESS".equals(result.get("trade_state"));
            return result.get("transaction_id");
        } catch (Exception e) {
            log.error("查询订单支付信息错误", e);
        }
        return null;
    }

    /**
     * 企业付款到零钱
     */
    @Override
    public Boolean transfers(Long withdrawId) {
        InputStream cert = this.getClass().getClassLoader().getResourceAsStream("apiclient_cert.p12");
//        Withdraw withdraw = withdrawService.get(withdrawId);
//        User user = userService.getById(withdraw.getUserId());
//        Map<String, String> params = new HashMap<>();
//        params.put("mch_appid", appid);
//        params.put("mchid", mch_id);
//        String nonceStr = System.currentTimeMillis() + RandomCodeUtil.genNumStrByLen(5);
//        params.put("nonce_str", nonceStr);
//        String partnerTradeNo = System.currentTimeMillis() + RandomCodeUtil.genNumStrByLen(5);
//        withdraw.setWithdrawOrderId(partnerTradeNo);
//        params.put("partner_trade_no", partnerTradeNo);
//        params.put("openid", user.getWxOpenId());
//        params.put("check_name", "NO_CHECK");
//        params.put("amount", withdraw.getWithdrawRewardFee().toString());
//        params.put("desc", withdraw.getTitle());
//        String ip = "127.0.0.1";
//        params.put("spbill_create_ip", ip);
//        params.put("sign", WxPayKit.createSign(params, partnerKey, null));
//        // 提现
//        String transfers = WxPayApi.transfers(params, cert, mch_id);
//        Map<String, String> map = WxPayKit.xmlToMap(transfers);
//        String return_code = map.get("return_code");
//        String result_code;
//        if (("SUCCESS").equals(return_code)) {
//            result_code = map.get("result_code");
//            if (("SUCCESS").equals(result_code)) {
//                //提现成功
//                withdraw.setStatus(Withdraw.STATUS_ONLINE);
//                return withdrawService.update(withdraw);
//            }
//        }
        //提现失败
        return false;
    }

    /**
     * 查询企业付款到零钱
     */
    public void transferInfo(String withdrawOrderId) {
//        try {
//            Map<String, String> params = new HashMap<>();
//            params.put("nonce_str", System.currentTimeMillis() + "");
//            params.put("partner_trade_no", withdrawOrderId);
//            params.put("mch_id", mch_id);
//            params.put("appid", appid);
//            params.put("sign", WxPayKit.createSign(params, partnerKey));
//            InputStream cert = this.getClass().getClassLoader().getResourceAsStream("apiclient_cert.p12");
//            String transferInfo = WxPayApi.getTransferInfo(params, cert, mch_id);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
