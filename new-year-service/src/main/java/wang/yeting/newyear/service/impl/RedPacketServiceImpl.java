package wang.yeting.newyear.service.impl;

import cn.hutool.core.util.NumberUtil;
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
import org.springframework.stereotype.Service;
import wang.yeting.newyear.mapper.RedPacketMapper;
import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.model.dto.RedPacketShareDto;
import wang.yeting.newyear.model.po.RedPacket;
import wang.yeting.newyear.model.vo.RedPacketVo;
import wang.yeting.newyear.service.RedPacketService;
import wang.yeting.newyear.service.WeChatPayService;
import wang.yeting.newyear.util.CopyBeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
            redPacket.setTotalFee(roundTotalFee.longValue());
            List<Long> redPacketFeeList = doubleMeanMethod(redPacket.getTotalFee(), redPacketVo.getNum());
            redPacket.setRedPacketFeeList(redPacketFeeList);
        } else if (redPacketType.equals(2)) {
            double totalFee = NumberUtil.mul(fee, redPacketVo.getNum().longValue());
            BigDecimal roundTotalFee = NumberUtil.round(totalFee, 0);
            redPacket.setTotalFee(roundTotalFee.longValue());
            List<Long> redPacketFeeList = new ArrayList<>();
            long roundFee = NumberUtil.round(fee, 0).longValue();
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
        // 分布式锁
        RLock lock = redissonClient.getLock("RedPacket:receive");
        lock.lock();
        //查询红包

        //发钱

        lock.unlock();
        return null;
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
                        .eq(RedPacket::getUserId, Long.parseLong(redPacketVo.getUserId()))
                        .eq(RedPacket::getRedPacketId, redPacketVo.getRedPacketId())
        );
        RedPacketShareDto redPacketShareDto = CopyBeanUtils.copyProperties(redPacket, RedPacketShareDto.class);
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
        RLock lock = redissonClient.getLock("RedPacket:update");
        lock.lock();
        boolean update = update(redPacket, wrapper);
        log.warn("红包修改结果：{} ，修改详情：{}", update, JSONUtil.toJsonStr(redPacket));
        lock.unlock();
        return update;
    }

    public static void main(String[] args) {
        doubleMeanMethod(100000, 24);
        doubleMeanMethod(8231, 24);
        doubleMeanMethod(32120, 555);
        doubleMeanMethod(1123, 6);
    }

    /**
     * 拼手气红包算法
     * 二倍均值算法（公平版）
     *
     * @param totalFee 红包总金额(分)
     * @param size     领取人数
     */
    public static List<Long> doubleMeanMethod(long totalFee, int size) {
        List<Long> result = new ArrayList<>();
        if (totalFee < 0 && size < 1) {
            return Collections.emptyList();
        }
        long amount, sum = 0;
        int remainingNumber = size;
        int i = 1;
        while (remainingNumber > 1) {
            amount = RandomUtils.nextLong(1, 2 * (totalFee / remainingNumber));
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
}
