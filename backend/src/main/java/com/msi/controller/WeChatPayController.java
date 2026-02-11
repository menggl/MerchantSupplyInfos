package com.msi.controller;

import com.msi.domain.Merchant;
import com.msi.domain.MerchantRechargeOrder;
import com.msi.request.WeChatRechargeCreateRequest;
import com.msi.service.WeChatPayService;
import jakarta.servlet.http.HttpServletRequest;
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
            Map<String, Object> payment = new HashMap<>();
            payment.put("appId", order.getAppId());
            payment.put("timeStamp", order.getTimeStamp());
            payment.put("nonceStr", order.getNonceStr());
            payment.put("package", order.getPackageVal());
            payment.put("signType", order.getSignType());
            payment.put("paySign", order.getPaySign());

            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("out_trade_no", order.getOrderNo());
            orderInfo.put("amount", order.getTotalAmount());
            orderInfo.put("rechargeId", order.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("payment", payment);
            result.put("order", orderInfo);
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("创建微信充值订单失败: merchantId={}, {}", currentMerchant != null ? currentMerchant.getId() : null,
                    e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/notify")
    public ResponseEntity<String> handleNotify(HttpServletRequest request, @RequestBody String requestBody) {
        try {
            String signature = request.getHeader("Wechatpay-Signature");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String serial = request.getHeader("Wechatpay-Serial");
            String signatureType = request.getHeader("Wechatpay-Signature-Type");

            logger.info("收到微信支付回调: serial={}, timestamp={}, signature={}", serial, timestamp, signature);

            weChatPayService.processCallback(signature, nonce, timestamp, serial, signatureType, requestBody);
            
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            logger.error("处理微信支付回调失败", e);
            return ResponseEntity.status(500).body("fail");
        }
    }
}

