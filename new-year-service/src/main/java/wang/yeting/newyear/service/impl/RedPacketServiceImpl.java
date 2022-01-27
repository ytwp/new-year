package wang.yeting.newyear.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ijpay.core.kit.WxPayKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wang.yeting.newyear.mapper.RedPacketMapper;
import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.dto.RedPacketReceiveDto;
import wang.yeting.newyear.model.dto.RedPacketShareDto;
import wang.yeting.newyear.model.po.RedPacket;
import wang.yeting.newyear.model.po.RedPacketReceive;
import wang.yeting.newyear.model.po.User;
import wang.yeting.newyear.model.vo.RedPacketInferVo;
import wang.yeting.newyear.model.vo.RedPacketVo;
import wang.yeting.newyear.service.RedPacketReceiveService;
import wang.yeting.newyear.service.RedPacketService;
import wang.yeting.newyear.service.UserService;
import wang.yeting.newyear.service.WeChatPayService;
import wang.yeting.newyear.util.CopyBeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : weipeng
 * @since : 2022-01-23 22:52
 */
@Slf4j
@Service
public class RedPacketServiceImpl extends ServiceImpl<RedPacketMapper, RedPacket> implements RedPacketService {

    @Autowired
    private WeChatPayService weChatPayService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedPacketReceiveService redPacketReceiveService;
    @Autowired
    private UserService userService;
    @Value("${infer.url}")
    private String inferUrl;

    /**
     * 发红包
     *
     * @param user
     * @param redPacketVo
     * @return
     */
    @Override
    public Result<?> send(UserBo user, RedPacketVo redPacketVo) {
        double fee = NumberUtil.mul(redPacketVo.getAmount(), new Double(100.00));
        if (redPacketVo.getNum() > fee) {
            return Result.parameterError("每人最少1分钱");
        }
        Integer redPacketType = redPacketVo.getRedPacketType();
        Integer receivingMethod = redPacketVo.getReceivingMethod();
        if (redPacketType == null || receivingMethod == null) {
            return Result.parameterError();
        }
        RedPacket redPacket = new RedPacket()
                .setNum(redPacketVo.getNum())
                .setRedPacketBlessing(redPacketVo.getRedPacketBlessing())
                .setRedPacketType(redPacketType)
                .setReceivingMethod(receivingMethod)
                .setNickName(redPacketVo.getNickName())
                .setUserId(user.getUserId())
                .setStatus(0)
                .setOutTradeNo(WxPayKit.generateStr());
        /*
         *  if (this.from_data.redPacketType == '拼手气红包') {
         * 		from.redPacketType = 1
         *  } else if (this.from_data.redPacketType == '普通红包') {
         * 		from.redPacketType = 2
         *  }
         * 	if (this.from_data.receivingMethod == '打开即可') {
         * 		from.receivingMethod = 1
         *  } else if (this.from_data.receivingMethod == '写祝福语') {
         * 		from.receivingMethod = 2
         *  } else if (this.from_data.receivingMethod == '手势拜年') {
         * 		from.receivingMethod = 3
         *  }
         */
        if (redPacketType.equals(1)) {
            BigDecimal roundTotalFee = NumberUtil.round(fee, 0);
            redPacket.setTotalFee(roundTotalFee.intValue());
            redPacket.setPayTotalFee(NumberUtil.mul(roundTotalFee, 1.03).intValue());
            List<Integer> redPacketFeeList = doubleMeanMethod(redPacket.getTotalFee(), redPacketVo.getNum());
            redPacket.setRedPacketFeeList(redPacketFeeList);
        } else if (redPacketType.equals(2)) {
            double totalFee = NumberUtil.mul(fee, redPacketVo.getNum().longValue());
            BigDecimal roundTotalFee = NumberUtil.round(totalFee, 0);
            redPacket.setTotalFee(roundTotalFee.intValue());
            redPacket.setPayTotalFee(NumberUtil.mul(roundTotalFee, 1.03).intValue());
            List<Integer> redPacketFeeList = new ArrayList<>();
            int roundFee = NumberUtil.round(fee, 0).intValue();
            for (int i = 0; i < redPacketVo.getNum(); i++) {
                redPacketFeeList.add(roundFee);
            }
            redPacket.setRedPacketFeeList(redPacketFeeList);
        }
        boolean save = save(redPacket);
        if (save) {
            Map<String, String> data = weChatPayService.miniAppPay(user, redPacket);
            data.put("redPacketId", redPacket.getRedPacketId().toString());
            return Result.success(data);
        }
        return Result.exceptionError("发红包失败");
    }

    /**
     * 抢红包
     *
     * @param user
     * @param redPacketVo
     * @return
     */
    @Override
    public Result<?> receive(UserBo user, RedPacketVo redPacketVo) {
        String redPacketId = redPacketVo.getRedPacketId();
        String redPacketUserId = redPacketVo.getUserId();
        if (StringUtils.isBlank(redPacketId) || StringUtils.isBlank(redPacketUserId)) {
            return Result.parameterError();
        }
        RedPacketReceiveDto redPacketReceiveDto = new RedPacketReceiveDto();
        redPacketReceiveDto.setRedPacketId(redPacketId);
        // 分布式锁
        RLock lock = redissonClient.getLock("RedPacket:receive:" + redPacketId);
        lock.lock();
        try {
            //查询红包
            RedPacket redPacket = getOne(new LambdaQueryWrapper<RedPacket>()
                    .eq(RedPacket::getRedPacketId, redPacketId)
                    .eq(RedPacket::getUserId, redPacketUserId)
            );
            if (redPacket != null) {
                if (redPacket.getStatus().equals(10)) {
                    List<RedPacketReceive> redPacketReceiveList = redPacketReceiveService.list(new LambdaQueryWrapper<RedPacketReceive>()
                            .eq(RedPacketReceive::getRedPacketId, redPacketId)
                    );
                    if (redPacket.getNum() == redPacketReceiveList.size()) {
                        redPacketReceiveDto.setStatus(false);
                        redPacketReceiveDto.setSendMoneyStatus(false);
                        redPacketReceiveDto.setMessage("来晚了，红包已被领完～");
                        redPacketReceiveDto.setButtonContext("我也发一个～");
                    } else {
                        List<RedPacketReceive> receivedList = redPacketReceiveList.stream().filter(redPacketReceive -> redPacketReceive.getUserId().equals(user.getUserId())).collect(Collectors.toList());
                        if (receivedList.size() > 0) {
                            redPacketReceiveDto.setStatus(false);
                            redPacketReceiveDto.setSendMoneyStatus(false);
                            redPacketReceiveDto.setMessage("已经领取过红包了哦～");
                            redPacketReceiveDto.setButtonContext("我也发一个～");
                        } else {
                            //发钱
                            int index = redPacketReceiveList.size();
                            Integer fee = redPacket.getRedPacketFeeList().get(index);
                            RedPacketReceive redPacketReceive = new RedPacketReceive()
                                    .setRedPacketId(redPacket.getRedPacketId())
                                    .setUserId(user.getUserId())
                                    .setNickName(user.getNickName())
                                    .setAvatarUrl(user.getAvatarUrl())
                                    .setOpenId(user.getOpenId())
                                    .setFeeIndex(index)
                                    .setFee(fee)
                                    .setPartnerTradeNo(WxPayKit.generateStr())
                                    .setStatus(0)
                                    .setBlessingWords(redPacketVo.getBlessingWords());
                            boolean saveReceive = redPacketReceiveService.save(redPacketReceive);
                            if (saveReceive) {
                                log.warn("发钱: {}", JSONUtil.toJsonStr(redPacketReceive));
                                Boolean transfers = weChatPayService.transfers(redPacketReceive);
                                boolean updateReceive = redPacketReceiveService.updateById(redPacketReceive);
                                if (transfers) {
                                    if (updateReceive) {
                                        //全部成功
                                        log.warn("发钱成功: {}", JSONUtil.toJsonStr(redPacketReceive));
                                    } else {
                                        log.error("警告，发钱成功，更新失败: {}", JSONUtil.toJsonStr(redPacketReceive));
                                    }
                                    redPacketReceiveDto.setStatus(true);
                                    redPacketReceiveDto.setSendMoneyStatus(true);
                                    redPacketReceiveDto.setMessage("¥ " + (NumberUtil.div(redPacketReceive.getFee().toString(), "100").setScale(2, RoundingMode.HALF_UP)) + " 领取成功,已存入微信钱包");
                                    redPacketReceiveDto.setButtonContext("我也发一个～");
                                } else {
                                    log.error("发钱失败: {}", JSONUtil.toJsonStr(redPacketReceive));
                                    if (!updateReceive) {
                                        log.error("警告，发钱失败，更新失败: {}", JSONUtil.toJsonStr(redPacketReceive));
                                    }
                                    redPacketReceiveDto.setStatus(true);
                                    redPacketReceiveDto.setSendMoneyStatus(false);
                                    redPacketReceiveDto.setMessage("领取成功，转账失败，如在24小时没收到，请联系客服处理～");
                                    redPacketReceiveDto.setButtonContext("我也发一个～");
                                }
                            } else {
                                redPacketReceiveDto.setStatus(false);
                                redPacketReceiveDto.setSendMoneyStatus(false);
                                redPacketReceiveDto.setMessage("领取失败，请稍后重试～");
                                redPacketReceiveDto.setButtonMethod(-10);
                                redPacketReceiveDto.setButtonContext("稍后重试");
                            }
                        }
                    }
                } else if (redPacket.getStatus().equals(-5) || redPacket.getStatus().equals(-10)) {
                    redPacketReceiveDto.setStatus(false);
                    redPacketReceiveDto.setSendMoneyStatus(false);
                    redPacketReceiveDto.setMessage("来晚了，红包已被领完～");
                    redPacketReceiveDto.setButtonContext("我也发一个～");
                } else {
                    redPacketReceiveDto.setStatus(false);
                    redPacketReceiveDto.setSendMoneyStatus(false);
                    redPacketReceiveDto.setMessage("红包未支付哦～");
                    redPacketReceiveDto.setButtonContext("我也发一个～");
                }
            } else {
                redPacketReceiveDto.setStatus(false);
                redPacketReceiveDto.setSendMoneyStatus(false);
                redPacketReceiveDto.setMessage("红包不正常哦～");
                redPacketReceiveDto.setButtonContext("我也发一个～");
            }
        } catch (Exception e) {
            log.error("领红包失败：{}", JSONUtil.toJsonStr(redPacketVo), e);
            redPacketReceiveDto.setStatus(false);
            redPacketReceiveDto.setSendMoneyStatus(false);
            redPacketReceiveDto.setMessage("领取失败，请稍后重试～");
            redPacketReceiveDto.setButtonMethod(-10);
            redPacketReceiveDto.setButtonContext("稍后重试");
        } finally {
            lock.unlock();
        }
        return Result.success(redPacketReceiveDto);
    }

    @Override
    public Result<?> queryPay(UserBo user, RedPacketVo redPacketVo) {
        RedPacket redPacket = getById(Long.parseLong(redPacketVo.getRedPacketId()));
        if (redPacket.getStatus().equals(10) && StringUtils.isNotBlank(redPacket.getTransactionId())) {
            return Result.success();
        }
        String res = weChatPayService.queryOrder(null, redPacket.getOutTradeNo());
        if (res != null) {
            redPacket.setStatus(10);
            redPacket.setTransactionId(res);
            boolean update = lockUpdate(redPacket, new LambdaQueryWrapper<RedPacket>()
                    .eq(RedPacket::getRedPacketId, redPacket.getRedPacketId())
                    .eq(RedPacket::getStatus, 0)
            );
            return Result.success(update);
        }
        return Result.success(false);
    }

    /**
     * 查询自己的红包
     *
     * @param user
     * @param redPacketVo
     * @return
     */
    @Override
    public Result<?> find(UserBo user, RedPacketVo redPacketVo) {
        Page<RedPacket> page = page(new Page<>(redPacketVo.getPage(), redPacketVo.getSize()),
                new LambdaQueryWrapper<RedPacket>()
                        .eq(RedPacket::getUserId, user.getUserId())
                        .orderByDesc(RedPacket::getCreateTime)
        );
        return Result.success(page.getRecords());
    }

    @Override
    public Result<?> shareGet(RedPacketVo redPacketVo) {
        RedPacket redPacket = getOne(
                new LambdaQueryWrapper<RedPacket>()
                        .eq(RedPacket::getUserId, redPacketVo.getUserId())
                        .eq(RedPacket::getRedPacketId, redPacketVo.getRedPacketId())
        );
        User user = userService.getByUserId(redPacket.getUserId());
        List<RedPacketReceive> redPacketReceiveList = redPacketReceiveService.list(new LambdaQueryWrapper<RedPacketReceive>()
                .select(RedPacketReceive::getNickName, RedPacketReceive::getFee, RedPacketReceive::getUserId)
                .eq(RedPacketReceive::getRedPacketId, redPacketVo.getRedPacketId())
        );
        List<String> receiveList = redPacketReceiveList.stream().map(redPacketReceive -> {
            return redPacketReceive.getNickName() + "领取 ¥" + (NumberUtil.div(redPacketReceive.getFee().toString(), "100").setScale(2, RoundingMode.HALF_UP)) + "  元";
        }).collect(Collectors.toList());
        if (receiveList.size() >= redPacket.getNum()) {
            //更新已领完
            redPacket.setStatus(-10);
            lockUpdate(redPacket, new LambdaQueryWrapper<RedPacket>()
                    .eq(RedPacket::getRedPacketId, redPacket.getRedPacketId())
                    .eq(RedPacket::getStatus, 10));
        }
        RedPacketShareDto redPacketShareDto = CopyBeanUtils.copyProperties(redPacket, RedPacketShareDto.class);
        redPacketShareDto.setAvatarUrl(user.getAvatarUrl());
        redPacketShareDto.setReceiveList(receiveList);
        return Result.success(redPacketShareDto);
    }

    @Override
    public Result<?> get(UserBo user, RedPacketVo redPacketVo) {
        RedPacket redPacket = getOne(
                new LambdaQueryWrapper<RedPacket>()
                        .eq(RedPacket::getUserId, user.getUserId())
                        .eq(RedPacket::getRedPacketId, redPacketVo.getRedPacketId())
        );
        return Result.success(redPacket);
    }

    /**
     * 统一修改方法
     *
     * @param redPacket
     * @param wrapper
     * @return
     */
    public Boolean lockUpdate(RedPacket redPacket, LambdaQueryWrapper<RedPacket> wrapper) {
        // 分布式锁
        RLock lock = redissonClient.getLock("RedPacket:update:" + redPacket.getRedPacketId());
        lock.lock();
        boolean update = update(redPacket, wrapper);
        log.warn("红包修改结果：{} ，修改详情：{}", update, JSONUtil.toJsonStr(redPacket));
        lock.unlock();
        return update;
    }

    public static void main(String[] args) {
        System.out.println("¥ " + (NumberUtil.div("110", "100").setScale(2, RoundingMode.HALF_UP)) + " 领取成功,已存入微信钱包");
//        doubleMeanMethod(100000, 24);
//        doubleMeanMethod(8231, 24);
//        doubleMeanMethod(180, 6);
//        doubleMeanMethod(310, 10);
//        doubleMeanMethod(190, 3);
//        double totalFee = NumberUtil.mul(100, 1);
//        BigDecimal roundTotalFee = NumberUtil.round(totalFee, 0);
//        System.out.println(roundTotalFee.longValue());
//        System.out.println(NumberUtil.mul(roundTotalFee, 1.03).longValue());
    }

    /**
     * 拼手气红包
     * 二倍均值+最小金额 算法（公平版）
     *
     * @param totalFee 红包总金额(分)
     * @param size     领取人数
     */
    public static List<Integer> doubleMeanMethod(int totalFee, int size) {
        List<Integer> result = new ArrayList<>();
        if (totalFee < 0 && size < 1) {
            return Collections.emptyList();
        }
        int minFee = 30;
        int amount, sum = 0;
        int remainingNumber = size;
        int i = 1;
        while (remainingNumber > 1) {
            int maxFee = Math.min(2 * (totalFee / remainingNumber), totalFee - ((size - i + 1) * minFee) + minFee);
            amount = RandomUtils.nextInt(minFee, maxFee);
            sum += amount;
            System.out.println("第" + i + "个人领取的红包金额为：" + amount);
            totalFee -= amount;
            remainingNumber--;
            result.add(amount);
            i++;
        }
        result.add(totalFee);
        System.out.println("第" + i + "个人领取的红包金额为：" + totalFee);
        sum += totalFee;
        System.out.println("验证发出的红包总金额为：" + sum);
        return result;

    }

    @Override
    public Result<?> infer(UserBo userBo, RedPacketInferVo redPacketInferVo) {
        long s = System.currentTimeMillis();
        String post = HttpUtil.post(inferUrl + "/infer/array_buffer", JSONUtil.toJsonStr(redPacketInferVo));
        long e = System.currentTimeMillis();
        System.out.println("识别用时：" + (e - s));
        JSONObject jsonObject = JSONUtil.parseObj(post);
        if (new Integer(20000).equals(jsonObject.getInt("code"))) {
            JSONArray dataArray = jsonObject.getJSONArray("data");
            if (dataArray != null && dataArray.size() > 0) {
                JSONObject data = dataArray.getJSONObject(0);
                JSONObject left_shoulder = data.getJSONObject("left_shoulder");
                Double left_shoulder_x = left_shoulder.getDouble("x");
                Double left_shoulder_y = left_shoulder.getDouble("y");
                JSONObject right_shoulder = data.getJSONObject("right_shoulder");
                Double right_shoulder_x = right_shoulder.getDouble("x");
                Double right_shoulder_y = right_shoulder.getDouble("y");
                JSONObject left_elbow = data.getJSONObject("left_elbow");
                Double left_elbow_x = left_elbow.getDouble("x");
                Double left_elbow_y = left_elbow.getDouble("y");
                JSONObject right_elbow = data.getJSONObject("right_elbow");
                Double right_elbow_x = right_elbow.getDouble("x");
                Double right_elbow_y = right_elbow.getDouble("y");
                JSONObject left_wrist = data.getJSONObject("left_wrist");
                Double left_wrist_x = left_wrist.getDouble("x");
                Double left_wrist_y = left_wrist.getDouble("y");
                JSONObject right_wrist = data.getJSONObject("right_wrist");
                Double right_wrist_x = right_wrist.getDouble("x");
                Double right_wrist_y = right_wrist.getDouble("y");
                if (Math.abs(left_shoulder_y - right_shoulder_y) < 20) {
                    if (Math.abs(left_shoulder_x - right_shoulder_x) > 70) {
                        if (Math.abs(left_wrist_x - right_wrist_x) < 70) {
                            if (Math.abs(left_wrist_y - right_wrist_y) < 20) {
                                if (right_elbow_y > right_shoulder_y || left_elbow_y > left_shoulder_y) {
                                    if (right_wrist_y < right_elbow_y || left_wrist_y < left_elbow_y) {
                                        if ((right_wrist_x > right_elbow_x && right_wrist_x > right_shoulder_x)
                                                || (left_wrist_x < left_elbow_x && left_wrist_x < left_shoulder_x)) {
                                            System.out.println("姿势正确");
                                            return receive(userBo, new RedPacketVo()
                                                    .setRedPacketId(redPacketInferVo.getRedPacketId())
                                                    .setUserId(redPacketInferVo.getRedPacketUserId())
                                            );
                                        } else {
                                            System.out.println("手不再中间");
                                        }
                                    } else {
                                        System.out.println("手大于肘子");
                                    }
                                } else {
                                    System.out.println("肘子小于肩膀");
                                }
                            } else {
                                System.out.println("手腕高度异常");
                            }
                        } else {
                            System.out.println("手腕宽度异常");
                        }
                    } else {
                        System.out.println("肩膀宽度异常");
                    }
                } else {
                    System.out.println("肩膀高度异常");
                }
            }
        }
        return Result.success(new RedPacketReceiveDto()
                .setRedPacketId(redPacketInferVo.getRedPacketId())
                .setGestureStatus(false)
                .setStatus(false)
                .setMessage("未识别到拜年姿势")
        );
    }

    @Override
    public Result<?> meReceive(UserBo user, RedPacketVo redPacketVo) {
        RedPacketReceive redPacketReceive = redPacketReceiveService.getOne(new LambdaQueryWrapper<RedPacketReceive>()
                .select(RedPacketReceive::getNickName, RedPacketReceive::getFee, RedPacketReceive::getUserId)
                .eq(RedPacketReceive::getRedPacketId, redPacketVo.getRedPacketId())
                .eq(RedPacketReceive::getUserId, user.getUserId())
        );
        return Result.success(redPacketReceive != null);
    }
}
