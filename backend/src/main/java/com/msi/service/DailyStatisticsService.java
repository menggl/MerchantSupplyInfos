package com.msi.service;

import com.msi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DailyStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(DailyStatisticsService.class);

    private final MerchantRepository merchantRepository;
    private final ProductRepository productRepository;
    private final BuyRequestRepository buyRequestRepository;
    private final MerchantCallRecordRepository callRecordRepository;
    private final MerchantMemberIntegralSpendRepository integralSpendRepository;
    private final RestTemplate restTemplate;

    @Value("${wecom.stats.webhook.url:}")
    private String wecomStatsWebhookUrl;

    public DailyStatisticsService(MerchantRepository merchantRepository,
                                  ProductRepository productRepository,
                                  BuyRequestRepository buyRequestRepository,
                                  MerchantCallRecordRepository callRecordRepository,
                                  MerchantMemberIntegralSpendRepository integralSpendRepository) {
        this.merchantRepository = merchantRepository;
        this.productRepository = productRepository;
        this.buyRequestRepository = buyRequestRepository;
        this.callRecordRepository = callRecordRepository;
        this.integralSpendRepository = integralSpendRepository;
        this.restTemplate = new RestTemplate();
    }

    @Scheduled(cron = "0 10 0 * * ?") // 每天凌晨0:30执行
    public void sendDailyStatistics() {
        if (wecomStatsWebhookUrl == null || wecomStatsWebhookUrl.isEmpty()) {
            logger.warn("WeCom stats webhook URL is not configured, skipping daily statistics.");
            return;
        }

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = LocalDateTime.of(yesterday, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(yesterday, LocalTime.MAX);

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("【每日数据统计】\n");
            sb.append("统计日期：").append(yesterday).append("\n\n");

            // 1. 商户统计
            long newMerchants = merchantRepository.countByCreateTimeBetween(startOfDay, endOfDay);
            long totalMerchants = merchantRepository.count();
            long updatedMerchants = merchantRepository.countByUpdateTimeBetweenAndMerchantPhoneIsNotNull(startOfDay, endOfDay);
            long totalValidMerchants = merchantRepository.countByMerchantPhoneIsNotNull();

            sb.append("1. 商户数据\n");
            sb.append("- 新增商户：").append(newMerchants).append("\n");
            sb.append("- 总商户数：").append(totalMerchants).append("\n");
            sb.append("- 更新商户(手机号不为空)：").append(updatedMerchants).append("\n");
            sb.append("- 有效商户(手机号不为空)：").append(totalValidMerchants).append("\n\n");

            // 2. 新机数据 (productType=0)
            long newPhonesAdded = productRepository.countByProductTypeAndIsValidAndStateAndCreateTimeBetween(0, 1, 1, startOfDay, endOfDay);
            long newPhonesUpdated = productRepository.countByProductTypeAndIsValidAndStateAndUpdateTimeBetween(0, 1, 1, startOfDay, endOfDay);
            long totalNewPhones = productRepository.countByProductTypeAndIsValidAndState(0, 1, 1);
            long offlineValidNewPhones = productRepository.countByProductTypeAndIsValidAndState(0, 1, 2);
            long updatedBuyReqNew = buyRequestRepository.countByProductTypeAndIsValidAndStateAndUpdateTimeBetween(0, 1, 1, startOfDay, endOfDay);

            sb.append("2. 新机数据\n");
            sb.append("- 新增上架：").append(newPhonesAdded).append("\n");
            sb.append("- 更新上架：").append(newPhonesUpdated).append("\n");
            sb.append("- 总上架数：").append(totalNewPhones).append("\n");
            sb.append("- 未上架有效数：").append(offlineValidNewPhones).append("\n");
            sb.append("- 更新求购数：").append(updatedBuyReqNew).append("\n\n");

            // 3. 二手机数据 (productType=1)
            long secondHandAdded = productRepository.countByProductTypeAndIsValidAndStateAndCreateTimeBetween(1, 1, 1, startOfDay, endOfDay);
            long secondHandUpdated = productRepository.countByProductTypeAndIsValidAndStateAndUpdateTimeBetween(1, 1, 1, startOfDay, endOfDay);
            long totalSecondHand = productRepository.countByProductTypeAndIsValidAndState(1, 1, 1);
            long offlineValidSecondHand = productRepository.countByProductTypeAndIsValidAndState(1, 1, 2);
            long updatedBuyReqSecondHand = buyRequestRepository.countByProductTypeAndIsValidAndStateAndUpdateTimeBetween(1, 1, 1, startOfDay, endOfDay);

            sb.append("3. 二手机数据\n");
            sb.append("- 新增上架：").append(secondHandAdded).append("\n");
            sb.append("- 更新上架：").append(secondHandUpdated).append("\n");
            sb.append("- 总上架数：").append(totalSecondHand).append("\n");
            sb.append("- 未上架有效数：").append(offlineValidSecondHand).append("\n");
            sb.append("- 更新求购数：").append(updatedBuyReqSecondHand).append("\n\n");

            // 4. 沟通电话数据
            long callProductCount = callRecordRepository.countByCallTypeAndCreateTimeBetween(0, startOfDay, endOfDay);
            long callBuyRequestCount = callRecordRepository.countByCallTypeAndCreateTimeBetween(1, startOfDay, endOfDay);
            long distinctCallerProduct = callRecordRepository.countDistinctCallerMerchantIdByCallTypeAndCreateTimeBetween(0, startOfDay, endOfDay);
            long distinctCallerBuyRequest = callRecordRepository.countDistinctCallerMerchantIdByCallTypeAndCreateTimeBetween(1, startOfDay, endOfDay);

            sb.append("4. 沟通数据\n");
            sb.append("- 上架沟通次数：").append(callProductCount).append("\n");
            sb.append("- 求购沟通次数：").append(callBuyRequestCount).append("\n");
            sb.append("- 上架沟通商户数(去重)：").append(distinctCallerProduct).append("\n");
            sb.append("- 求购沟通商户数(去重)：").append(distinctCallerBuyRequest).append("\n\n");

            // 5. 积分与充值数据
            long signinCount = integralSpendRepository.countByChangeReasonAndChangeTimeBetween("签到送积分", startOfDay, endOfDay);
            long rechargeCount = integralSpendRepository.countByChangeReasonAndChangeTimeBetween("花钱充值积分", startOfDay, endOfDay);
            long rechargeTotalAmount = integralSpendRepository.sumChangeAmountByChangeReasonAndChangeTimeBetween("花钱充值积分", startOfDay, endOfDay);

            sb.append("5. 积分充值\n");
            sb.append("- 签到次数：").append(signinCount).append("\n");
            sb.append("- 充值次数：").append(rechargeCount).append("\n");
            sb.append("- 充值总额：").append(rechargeTotalAmount).append("\n");

            sendWecomMessage(sb.toString());

        } catch (Exception e) {
            logger.error("Failed to generate or send daily statistics", e);
        }
    }

    private void sendWecomMessage(String content) {
        try {
            Map<String, Object> text = new HashMap<>();
            text.put("content", content);

            Map<String, Object> body = new HashMap<>();
            body.put("msgtype", "text");
            body.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(wecomStatsWebhookUrl, request, String.class);
            logger.info("Sent daily statistics to WeCom");
        } catch (Exception e) {
            logger.error("Failed to send WeCom message", e);
        }
    }
}
