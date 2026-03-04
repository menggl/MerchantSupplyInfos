package com.msi.service;

import com.msi.domain.DailyStatistics;
import com.msi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.time.format.DateTimeFormatter;

@Service
public class DailyStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(DailyStatisticsService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MerchantRepository merchantRepository;
    private final ProductRepository productRepository;
    private final BuyRequestRepository buyRequestRepository;
    private final MerchantCallRecordRepository callRecordRepository;
    private final MerchantMemberIntegralSpendRepository integralSpendRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final MerchantRechargeOrderRepository rechargeOrderRepository;
    private final RestTemplate restTemplate;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Value("${wecom.stats.webhook.url:}")
    private String wecomStatsWebhookUrl;

    public DailyStatisticsService(MerchantRepository merchantRepository,
                                  ProductRepository productRepository,
                                  BuyRequestRepository buyRequestRepository,
                                  MerchantCallRecordRepository callRecordRepository,
                                  MerchantMemberIntegralSpendRepository integralSpendRepository,
                                  DailyStatisticsRepository dailyStatisticsRepository,
                                  MerchantRechargeOrderRepository rechargeOrderRepository,
                                  org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        this.merchantRepository = merchantRepository;
        this.productRepository = productRepository;
        this.buyRequestRepository = buyRequestRepository;
        this.callRecordRepository = callRecordRepository;
        this.integralSpendRepository = integralSpendRepository;
        this.dailyStatisticsRepository = dailyStatisticsRepository;
        this.rechargeOrderRepository = rechargeOrderRepository;
        this.restTemplate = new RestTemplate();
        this.redisTemplate = redisTemplate;
    }

    public void recordActiveUser(Long merchantId) {
        if (merchantId == null) {
            return;
        }
        
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        String redisKey = "daily_active_users:" + todayStr;
        
        // Add to Redis set
        redisTemplate.opsForSet().add(redisKey, merchantId.toString());
        // Set expire time (e.g. 2 days) to avoid memory leak, though logic only needs it for today
        redisTemplate.expire(redisKey, 1, java.util.concurrent.TimeUnit.DAYS);
        
        // Get current size
        Long size = redisTemplate.opsForSet().size(redisKey);
        int activeCount = (size != null) ? size.intValue() : 0;
        
        // Update DB
        Optional<DailyStatistics> existingStats = dailyStatisticsRepository.findByStatisticsDate(todayStr);
        DailyStatistics stats = existingStats.orElse(new DailyStatistics());
        if (stats.getStatisticsDate() == null) {
            stats.setStatisticsDate(todayStr);
        }
        stats.setDailyActiveUsers(activeCount);
        dailyStatisticsRepository.save(stats);
    }

    @Scheduled(cron = "0 1 0 * * ?") // 每天凌晨0:01执行
    @Transactional
    public void sendDailyStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String statisticsDateStr = yesterday.format(DATE_FORMATTER);
        LocalDateTime startOfDay = LocalDateTime.of(yesterday, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(yesterday, LocalTime.MAX);

        try {
            // Check if statistics already exist for yesterday
            Optional<DailyStatistics> existingStats = dailyStatisticsRepository.findByStatisticsDate(statisticsDateStr);
            DailyStatistics stats = existingStats.orElse(new DailyStatistics());
            stats.setStatisticsDate(statisticsDateStr);

            // 1. 商户统计
            long newMerchants = merchantRepository.countByIsValidAndCreateTimeBetween(1, startOfDay, endOfDay);
            long totalMerchants = merchantRepository.countByIsValid(1);
            long realRegisteredMerchants = merchantRepository.countByIsValidAndMerchantPhoneIsNotNullAndCreateTimeBetween(1, startOfDay, endOfDay);
            long totalValidMerchants = merchantRepository.countByIsValidAndMerchantPhoneIsNotNull(1);

            stats.setNewMerchantCount((int) newMerchants);
            stats.setTotalMerchantCount((int) totalMerchants);
            stats.setNewValidMerchantCount((int) realRegisteredMerchants);
            stats.setTotalValidMerchantCount((int) totalValidMerchants);

            // 2. 新机数据 (productType=0)
            long newPhonesAdded = productRepository.countByProductTypeAndIsValidAndStateAndCreateTimeBetween(0, 1, 1, startOfDay, endOfDay);
            long newPhonesUpdated = productRepository.countUpdatedProducts(0, 1, 1, startOfDay, endOfDay);
            
            long newPhonesAddedByNewUsers = productRepository.countNewUserProducts(0, startOfDay, endOfDay);
            long newPhonesAddedByOldUsers = productRepository.countOldUserProducts(0, startOfDay, endOfDay);
            long newPhonesUpdatedByNewUsers = productRepository.countNewUserProductUpdates(0, startOfDay, endOfDay);
            long newPhonesUpdatedByOldUsers = productRepository.countOldUserProductUpdates(0, startOfDay, endOfDay);

            long totalNewPhones = productRepository.countByProductTypeAndIsValidAndState(0, 1, 1);
            long offlineValidNewPhones = productRepository.countByProductTypeAndIsValidAndState(0, 1, 2);
            long totalBuyReqNew = buyRequestRepository.countByProductTypeAndIsValidAndState(0, 1, 1);
            long updatedBuyReqNew = buyRequestRepository.countByProductTypeAndIsValidAndStateAndUpdateTimeBetween(0, 1, 1, startOfDay, endOfDay); // Keep for report if needed

            stats.setNewProductNewCount((int) newPhonesAdded);
            stats.setNewProductNewUserCount((int) newPhonesAddedByNewUsers);
            stats.setNewProductOldUserCount((int) newPhonesAddedByOldUsers);
            stats.setNewProductUpdateCount((int) newPhonesUpdated);
            stats.setNewProductNewUserUpdateCount((int) newPhonesUpdatedByNewUsers);
            stats.setNewProductOldUserUpdateCount((int) newPhonesUpdatedByOldUsers);
            stats.setNewProductTotalCount((int) totalNewPhones);
            stats.setNewProductTotalOffCount((int) offlineValidNewPhones);
            stats.setNewProductBuyCount((int) totalBuyReqNew);

            // 3. 二手机数据 (productType=1)
            long secondHandAdded = productRepository.countByProductTypeAndIsValidAndStateAndCreateTimeBetween(1, 1, 1, startOfDay, endOfDay);
            long secondHandUpdated = productRepository.countUpdatedProducts(1, 1, 1, startOfDay, endOfDay);

            long secondHandAddedByNewUsers = productRepository.countNewUserProducts(1, startOfDay, endOfDay);
            long secondHandAddedByOldUsers = productRepository.countOldUserProducts(1, startOfDay, endOfDay);
            long secondHandUpdatedByNewUsers = productRepository.countNewUserProductUpdates(1, startOfDay, endOfDay);
            long secondHandUpdatedByOldUsers = productRepository.countOldUserProductUpdates(1, startOfDay, endOfDay);

            long totalSecondHand = productRepository.countByProductTypeAndIsValidAndState(1, 1, 1);
            long offlineValidSecondHand = productRepository.countByProductTypeAndIsValidAndState(1, 1, 2);
            long totalBuyReqSecondHand = buyRequestRepository.countByProductTypeAndIsValidAndState(1, 1, 1);
            long updatedBuyReqSecondHand = buyRequestRepository.countByProductTypeAndIsValidAndStateAndUpdateTimeBetween(1, 1, 1, startOfDay, endOfDay);

            stats.setSecondHandProductNewCount((int) secondHandAdded);
            stats.setSecondHandProductNewUserCount((int) secondHandAddedByNewUsers);
            stats.setSecondHandProductOldUserCount((int) secondHandAddedByOldUsers);
            stats.setSecondHandProductUpdateCount((int) secondHandUpdated);
            stats.setSecondHandProductNewUserUpdateCount((int) secondHandUpdatedByNewUsers);
            stats.setSecondHandProductOldUserUpdateCount((int) secondHandUpdatedByOldUsers);
            stats.setSecondHandProductTotalCount((int) totalSecondHand);
            stats.setSecondHandProductTotalOffCount((int) offlineValidSecondHand);
            stats.setSecondHandProductBuyCount((int) totalBuyReqSecondHand);

            // 4. 沟通电话数据
            long callProductCount = callRecordRepository.countByCallTypeAndCreateTimeBetween(0, startOfDay, endOfDay);
            long callBuyRequestCount = callRecordRepository.countByCallTypeAndCreateTimeBetween(1, startOfDay, endOfDay);
            long distinctCallerProduct = callRecordRepository.countDistinctCallerMerchantIdByCallTypeAndCreateTimeBetween(0, startOfDay, endOfDay);
            long distinctCallerBuyRequest = callRecordRepository.countDistinctCallerMerchantIdByCallTypeAndCreateTimeBetween(1, startOfDay, endOfDay);

            stats.setCallCountProduct((int) callProductCount);
            stats.setCallCountBuy((int) callBuyRequestCount);
            stats.setCallMerchantCountProduct((int) distinctCallerProduct);
            stats.setCallMerchantCountBuy((int) distinctCallerBuyRequest);

            // 5. 积分与充值数据
            long signinCount = integralSpendRepository.countByChangeReasonAndChangeTimeBetween("签到送积分", startOfDay, endOfDay);
            long rechargeCount = integralSpendRepository.countByChangeReasonAndChangeTimeBetween("花钱充值积分", startOfDay, endOfDay);
            // Schema: 截止当天总共充值金额 (Total recharge amount until today)
            Long totalRechargeAmount = rechargeOrderRepository.sumTotalAmountByPayStatusAndCreateTimeLessThanEqual(1, endOfDay);
            // Daily recharge amount for report (keep existing logic or use repository)
            long dailyRechargeAmount = integralSpendRepository.sumChangeAmountByChangeReasonAndChangeTimeBetween("花钱充值积分", startOfDay, endOfDay);

            stats.setDailySignInCount((int) signinCount);
            stats.setDailyRechargeCount((int) rechargeCount);
            stats.setTotalRechargeAmount(totalRechargeAmount);

            // Save to DB
            dailyStatisticsRepository.save(stats);
            logger.info("Daily statistics saved for {}", yesterday);

            // Send WeCom Message
            if (wecomStatsWebhookUrl == null || wecomStatsWebhookUrl.isEmpty()) {
                logger.warn("WeCom stats webhook URL is not configured, skipping WeCom notification.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("【每日数据统计】\n");
            sb.append("统计日期：").append(yesterday).append("\n\n");

            sb.append("1. 商户数据\n");
            sb.append("- 新增商户：").append(stats.getNewMerchantCount()).append("\n");
            sb.append("- 总商户数：").append(stats.getTotalMerchantCount()).append("\n");
            sb.append("- 真实注册商户：").append(stats.getNewValidMerchantCount()).append("\n");
            sb.append("- 有效商户(手机号不为空)：").append(stats.getTotalValidMerchantCount()).append("\n\n");

            sb.append("2. 新机数据\n");
            sb.append("- 新增上架：").append(stats.getNewProductNewCount()).append("\n");
            sb.append("  * 新用户新增：").append(stats.getNewProductNewUserCount()).append("\n");
            sb.append("  * 老用户新增：").append(stats.getNewProductOldUserCount()).append("\n");
            sb.append("- 更新上架：").append(stats.getNewProductUpdateCount()).append("\n");
            sb.append("  * 新用户更新：").append(stats.getNewProductNewUserUpdateCount()).append("\n");
            sb.append("  * 老用户更新：").append(stats.getNewProductOldUserUpdateCount()).append("\n");
            sb.append("- 总上架数：").append(stats.getNewProductTotalCount()).append("\n");
            sb.append("- 未上架有效数：").append(stats.getNewProductTotalOffCount()).append("\n");
            sb.append("- 更新求购数：").append(updatedBuyReqNew).append("\n\n"); // Using daily updated count for report consistency

            sb.append("3. 二手机数据\n");
            sb.append("- 新增上架：").append(stats.getSecondHandProductNewCount()).append("\n");
            sb.append("  * 新用户新增：").append(stats.getSecondHandProductNewUserCount()).append("\n");
            sb.append("  * 老用户新增：").append(stats.getSecondHandProductOldUserCount()).append("\n");
            sb.append("- 更新上架：").append(stats.getSecondHandProductUpdateCount()).append("\n");
            sb.append("  * 新用户更新：").append(stats.getSecondHandProductNewUserUpdateCount()).append("\n");
            sb.append("  * 老用户更新：").append(stats.getSecondHandProductOldUserUpdateCount()).append("\n");
            sb.append("- 总上架数：").append(stats.getSecondHandProductTotalCount()).append("\n");
            sb.append("- 未上架有效数：").append(stats.getSecondHandProductTotalOffCount()).append("\n");
            sb.append("- 更新求购数：").append(updatedBuyReqSecondHand).append("\n\n"); // Using daily updated count for report consistency

            sb.append("4. 沟通数据\n");
            sb.append("- 上架沟通次数：").append(stats.getCallCountProduct()).append("\n");
            sb.append("- 求购沟通次数：").append(stats.getCallCountBuy()).append("\n");
            sb.append("- 上架沟通商户数(去重)：").append(stats.getCallMerchantCountProduct()).append("\n");
            sb.append("- 求购沟通商户数(去重)：").append(stats.getCallMerchantCountBuy()).append("\n\n");

            sb.append("5. 积分充值\n");
            sb.append("- 签到次数：").append(stats.getDailySignInCount()).append("\n");
            sb.append("- 充值次数：").append(stats.getDailyRechargeCount()).append("\n");
            sb.append("- 充值总额：").append(dailyRechargeAmount).append("\n"); // Using daily amount for report consistency

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
