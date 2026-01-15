package com.msi.service;

import com.msi.domain.MerchantRechargeOrder;
import com.msi.repository.MerchantRechargeOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class WeChatPayService {

    private final MerchantRechargeOrderRepository orderRepository;
    private final RechargeService rechargeService;

    public WeChatPayService(MerchantRechargeOrderRepository orderRepository,
                            RechargeService rechargeService,
                            MerchantService merchantService) {
        this.orderRepository = orderRepository;
        this.rechargeService = rechargeService;
    }

    @Transactional
    public MerchantRechargeOrder createRechargeOrder(Long merchantId,
                                                     Integer rechargeType,
                                                     Integer integralAmount,
                                                     Integer memberMonths,
                                                     Integer totalAmount) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (rechargeType == null || (rechargeType != 1 && rechargeType != 2)) {
            throw new IllegalArgumentException("充值类型不合法");
        }
        if (totalAmount == null || totalAmount <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        if (rechargeType == 1) {
            if (integralAmount == null || integralAmount <= 0) {
                throw new IllegalArgumentException("积分数量必须大于0");
            }
        } else {
            if (memberMonths == null || memberMonths <= 0) {
                throw new IllegalArgumentException("会员时长必须大于0");
            }
        }
        MerchantRechargeOrder order = new MerchantRechargeOrder();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setMerchantId(merchantId);
        order.setRechargeType(rechargeType);
        order.setIntegralAmount(integralAmount);
        order.setMemberMonths(memberMonths);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        return orderRepository.save(order);
    }

    @Transactional
    public void handlePaySuccess(String orderNo, String transactionId, Integer paidAmount) {
        if (orderNo == null || orderNo.isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        Optional<MerchantRechargeOrder> opt = orderRepository.findByOrderNo(orderNo);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("订单不存在");
        }
        MerchantRechargeOrder order = opt.get();
        if (order.getStatus() != null && order.getStatus() == 1) {
            return;
        }
        if (paidAmount == null || !paidAmount.equals(order.getTotalAmount())) {
            throw new IllegalArgumentException("支付金额不匹配");
        }
        Long merchantId = order.getMerchantId();
        if (order.getRechargeType() == 1) {
            rechargeService.rechargeIntegral(merchantId, order.getIntegralAmount());
        } else if (order.getRechargeType() == 2) {
            rechargeService.rechargeMember(merchantId, order.getMemberMonths());
        }
        order.setStatus(1);
        order.setWechatTransactionId(transactionId);
        orderRepository.save(order);
    }
}
