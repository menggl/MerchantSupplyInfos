package com.msi.service;

import com.msi.domain.Merchant;
import com.msi.domain.MerchantRechargeOrder;
import com.msi.domain.WeChatPayNotifyLog;
import com.msi.repository.MerchantRechargeOrderRepository;
import com.msi.repository.MerchantRepository;
import com.msi.repository.WeChatPayNotifyLogRepository;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.cipher.SignatureResult;
import com.wechat.pay.java.core.cipher.Signer;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class WeChatPayService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatPayService.class);

    private final MerchantRechargeOrderRepository orderRepository;
    private final RechargeService rechargeService;
    private final MerchantRepository merchantRepository;
    private final WeChatPayNotifyLogRepository notifyLogRepository;

    @Autowired(required = false)
    private Config wechatPaySdkConfig;

    @Value("${wechat.pay.mchid:}")
    private String mchId;

    @Value("${wechat.miniapp.appid:}")
    private String appId;

    @Value("${wechat.pay.notify-url:}")
    private String notifyUrl;

    public WeChatPayService(MerchantRechargeOrderRepository orderRepository,
                            RechargeService rechargeService,
                            MerchantRepository merchantRepository,
                            WeChatPayNotifyLogRepository notifyLogRepository) {
        this.orderRepository = orderRepository;
        this.rechargeService = rechargeService;
        this.merchantRepository = merchantRepository;
        this.notifyLogRepository = notifyLogRepository;
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

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商户不存在"));

        MerchantRechargeOrder order = new MerchantRechargeOrder();
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        order.setOrderNo(orderNo);
        order.setMerchantId(merchantId);
        order.setRechargeType(rechargeType);
        order.setIntegralAmount(integralAmount);
        order.setMemberMonths(memberMonths);
        order.setTotalAmount(totalAmount);
        order.setPayStatus(0);

        try {
            if (wechatPaySdkConfig != null) {
                JsapiService service = new JsapiService.Builder().config(wechatPaySdkConfig).build();
                PrepayRequest request = new PrepayRequest();
                
                Amount amount = new Amount();
                amount.setTotal(totalAmount);
                amount.setCurrency("CNY");
                request.setAmount(amount);

                Payer payer = new Payer();
                payer.setOpenid(merchant.getWechatId()); // 使用商户的 openid
                request.setPayer(payer);

                request.setAppid(appId);
                request.setMchid(mchId);
                request.setDescription("会员充值/积分充值");
                request.setNotifyUrl(notifyUrl);
                request.setOutTradeNo(orderNo);

                PrepayResponse response = service.prepay(request);
                String prepayId = response.getPrepayId();

                // 生成签名参数
                long timestamp = System.currentTimeMillis() / 1000;
                String nonceStr = UUID.randomUUID().toString().replace("-", "");
                String packageStr = "prepay_id=" + prepayId;

                // 签名逻辑 (直连商户模式)
                // 构造签名串：appId\ntimeStamp\nnonceStr\npackage\n
                String message = appId + "\n"
                        + timestamp + "\n"
                        + nonceStr + "\n"
                        + packageStr + "\n";

                Signer signer = wechatPaySdkConfig.createSigner();
                SignatureResult signResult = signer.sign(message);
                String paySign = signResult.getSign();

                // 保存到订单表
                order.setAppId(appId);
                order.setPrepayId(prepayId);
                order.setNonceStr(nonceStr);
                order.setTimeStamp(String.valueOf(timestamp));
                order.setPackageVal(packageStr);
                order.setSignType("RSA");
                order.setPaySign(paySign);
                
                logger.info("微信支付下单成功: orderNo={}, prepayId={}", orderNo, prepayId);
            } else {
                // 模拟环境 (未配置微信支付)
                logger.warn("微信支付未配置，使用模拟数据");
                order.setAppId("wx8888888888888888");
                order.setPrepayId("wx201410272009395522657a690389285100");
                order.setNonceStr("5K8264ILTKCH16CQ2502SI8ZNMTM67VS");
                order.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
                order.setPackageVal("prepay_id=wx201410272009395522657a690389285100");
                order.setSignType("RSA");
                order.setPaySign("MOCK_SIGNATURE_FOR_TESTING");
            }
        } catch (Exception e) {
            logger.error("微信支付下单失败: {}", e.getMessage(), e);
            throw new RuntimeException("微信支付下单失败: " + e.getMessage());
        }

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
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
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
        order.setPayStatus(1);
        order.setWechatTransactionId(transactionId);
        orderRepository.save(order);
    }

    @Transactional
    public void processCallback(String signature, String nonce, String timestamp, String serial, String signatureType, String requestBody) {
        // 1. Log to DB (Raw)
        WeChatPayNotifyLog log = new WeChatPayNotifyLog();
        log.setRequestBody(requestBody);
        log.setProcessStatus("RECEIVED");
        // Extract basic info from JSON body (id, summary, resource_type, event_type)
        try {
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(requestBody);
            if (node.has("id")) log.setNotifyId(node.get("id").asText());
            if (node.has("event_type")) log.setEventType(node.get("event_type").asText());
            if (node.has("resource_type")) log.setResourceType(node.get("resource_type").asText());
            if (node.has("summary")) log.setSummary(node.get("summary").asText());
            if (node.has("resource") && node.get("resource").has("ciphertext")) {
                log.setResourceCiphertext(node.get("resource").get("ciphertext").asText());
            }
        } catch (Exception e) {
            logger.warn("Parse notify body failed", e);
        }
        notifyLogRepository.save(log);

        if (wechatPaySdkConfig == null) {
            log.setProcessStatus("SKIPPED_NO_CONFIG");
            notifyLogRepository.save(log);
            return;
        }

        try {
            // 2. Parse and Decrypt
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(serial)
                    .nonce(nonce)
                    .signature(signature)
                    .timestamp(timestamp)
                    .signType(signatureType)
                    .body(requestBody)
                    .build();

            NotificationParser parser = new NotificationParser((NotificationConfig) wechatPaySdkConfig);
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            
            log.setProcessStatus("DECRYPTED");
            notifyLogRepository.save(log);

            // 3. Business Logic
            if (transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                handlePaySuccess(transaction.getOutTradeNo(), transaction.getTransactionId(), transaction.getAmount().getTotal());
            }

            log.setProcessStatus("PROCESSED");
            notifyLogRepository.save(log);
            
        } catch (Exception e) {
            logger.error("Process callback failed", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 200) {
                errorMsg = errorMsg.substring(0, 200) + "...";
            }
            log.setProcessStatus("FAILED: " + errorMsg);
            notifyLogRepository.save(log);
            throw new RuntimeException(e);
        }
    }
}
