package wang.yeting.newyear.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wang.yeting.newyear.service.UserService;
import wang.yeting.newyear.service.WeChatPayService;

import java.io.InputStream;

/**
 * @author : weipeng
 * @version : 1.0
 * @date : 2020-05-26 23:29
 */

@Service
public class WeChatPayServiceImpl implements WeChatPayService {

    @Value("${weChat.pay.appid}")
    private String appid;
    @Value("${weChat.pay.mch_id}")
    private String mch_id;
    @Value("${weChat.pay.partnerKey}")
    private String partnerKey;

    @Autowired
    private UserService userService;

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
