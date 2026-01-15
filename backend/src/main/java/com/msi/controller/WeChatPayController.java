package com.msi.controller;

import com.msi.domain.Merchant;
import com.msi.domain.MerchantRechargeOrder;
import com.msi.request.WeChatPayNotifyRequest;
import com.msi.request.WeChatRechargeCreateRequest;
import com.msi.service.WeChatPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay/wechat")
public class WeChatPayController {

    private static final Logger logger = LoggerFactory.getLogger(WeChatPayController.class);

    private final WeChatPayService weChatPayService;

    public WeChatPayController(WeChatPayService weChatPayService) {
        this.weChatPayService = weChatPayService;
    }

    @PostMapping("/recharge")
    public ResponseEntity<Map<String, Object>> createRechargeOrder(@RequestAttribute("merchant") Merchant currentMerchant,
                                                                   @RequestBody WeChatRechargeCreateRequest request) {
        try {
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            MerchantRechargeOrder order = weChatPayService.createRechargeOrder(
                    currentMerchant.getId(),
                    request.getRechargeType(),
                    request.getIntegralAmount(),
                    request.getMemberMonths(),
                    request.getTotalAmount()
            );
            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", order.getOrderNo());
            result.put("totalAmount", order.getTotalAmount());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("创建微信充值订单失败: merchantId={}, {}", currentMerchant != null ? currentMerchant.getId() : null,
                    e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/notify")
    public ResponseEntity<String> handleNotify(@RequestBody WeChatPayNotifyRequest notifyRequest) {
        try {
            if (notifyRequest == null || notifyRequest.getOutTradeNo() == null) {
                return ResponseEntity.badRequest().body("invalid request");
            }
            if (!"SUCCESS".equalsIgnoreCase(notifyRequest.getTradeState())) {
                return ResponseEntity.ok("success");
            }
            weChatPayService.handlePaySuccess(
                    notifyRequest.getOutTradeNo(),
                    notifyRequest.getTransactionId(),
                    notifyRequest.getTotalAmount()
            );
            return ResponseEntity.ok("success");
        } catch (IllegalArgumentException e) {
            logger.error("处理微信支付回调失败: orderNo={}, {}", notifyRequest != null ? notifyRequest.getOutTradeNo() : null,
                    e.getMessage(), e);
            return ResponseEntity.badRequest().body("fail");
        }
    }
}

